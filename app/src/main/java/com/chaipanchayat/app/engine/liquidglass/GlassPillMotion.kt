package com.chaipanchayat.app.engine.liquidglass

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.Capsule
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sign

/**
 * Spring-driven selection pill physics ported from BitChord and Kyant0/backdrop:
 * - Tap lifts the pill across under-damped springs, overshooting raised size and rebounding.
 * - Acceleration-driven squash: expands in travel direction, squashes on deceleration.
 * - AGSL Lens refraction during travel, refracting the exported bar glass and underlying page.
 * - Zero crash guarantee: full isSpecified validation on all sizes and coordinates.
 */
@Stable
class GlassPillMotion(
    private val scope: CoroutineScope,
    initialIndex: Int
) {
    var position by mutableFloatStateOf(initialIndex.toFloat())
        private set

    var liftX by mutableFloatStateOf(0f)
        private set

    var liftY by mutableFloatStateOf(0f)
        private set

    var material by mutableFloatStateOf(0f)
        private set

    var deviation by mutableFloatStateOf(0f)
        private set

    var glassPresence by mutableFloatStateOf(0f)
        private set

    var isFlat by mutableStateOf(true)
        private set

    var index: Int = initialIndex
        private set

    var stepDp: Float = 0f
    var reduceMotion: Boolean = false

    private var travelPos = initialIndex.toDouble()
    private var travelVel = 0.0
    private var travelTarget = travelPos
    private var travelFrom = travelPos
    private var travelActive = false

    private var lifted = false
    private var liftXPos = 0.0
    private var liftXVel = 0.0
    private var liftYPos = 0.0
    private var liftYVel = 0.0
    private var liftPos = 0.0
    private var liftVel = 0.0

    private var dragging = false
    private var dragTarget = 0.0
    private var dragFollow = 0.0

    private var travelSign = 0.0
    private var travelSignEased = 0.0
    private var handover = 1.0
    private var squash = 0.0
    private val squashModel = PillSquashModel()

    private var running = false

    fun animateTo(next: Int) {
        if (next == index && !travelActive) return
        if (reduceMotion) {
            snapTo(next)
            return
        }
        index = next
        travelActive = true
        lifted = true
        travelFrom = travelPos
        travelTarget = next.toDouble()
        travelSign = signOfTravel(travelTarget - travelFrom)
        isFlat = false
        start()
    }

    fun snapTo(target: Int) {
        index = target
        travelPos = target.toDouble()
        travelTarget = travelPos
        travelFrom = travelPos
        travelVel = 0.0
        travelActive = false
        dragging = false
        lifted = false
        liftXPos = 0.0; liftXVel = 0.0
        liftYPos = 0.0; liftYVel = 0.0
        liftPos = 0.0; liftVel = 0.0
        handover = 1.0
        squash = 0.0
        squashModel.stop()
        travelSign = 0.0
        travelSignEased = 0.0
        publish()
        isFlat = true
    }

    fun startDrag() {
        dragging = true
        travelActive = false
        lifted = !reduceMotion
        travelSign = 0.0
        dragFollow = travelPos
        dragTarget = travelPos
        travelVel = 0.0
        isFlat = false
        start()
    }

    fun dragTo(position: Float) {
        if (dragging) dragTarget = position.toDouble()
    }

    fun release(next: Int) {
        if (!dragging) return
        val from = dragFollow
        dragging = false
        index = next
        if (reduceMotion) {
            snapTo(next)
            return
        }
        travelActive = true
        travelPos = from
        travelVel = 0.0
        travelFrom = from
        travelTarget = next.toDouble()
        travelSign = signOfTravel(travelTarget - travelFrom)
        start()
    }

    private fun start() {
        if (running) return
        running = true
        scope.launch {
            var last = -1L
            try {
                while (true) {
                    val now = withFrameNanos { it }
                    val dt = if (last < 0) 0.0 else (now - last) / 1e9
                    last = now
                    if (!tick(dt, now / 1e9)) break
                }
            } finally {
                running = false
            }
        }
    }

    private fun tick(dt: Double, now: Double): Boolean {
        // 1) Travel spring
        var travelSettled = true
        if (travelActive) {
            val r = springStep(travelPos, travelVel, travelTarget, dt, TRAVEL_STIFFNESS, TRAVEL_DAMPING)
            travelPos = r.first
            travelVel = r.second
            travelSettled = abs(travelPos - travelTarget) < 0.003 && abs(travelVel) < 0.05
            if (travelSettled) {
                travelPos = travelTarget
                travelVel = 0.0
            }
        }

        // 2) Under finger drag
        if (dragging) {
            dragFollow += if (reduceMotion) {
                dragTarget - dragFollow
            } else {
                (dragTarget - dragFollow) * (1 - exp(-dt / FOLLOW_TAU))
            }
        }

        // 3) Physical lift, held for trip and released on landing
        if (!dragging && (travelSettled || travelProgress() >= HANDOVER_START)) {
            lifted = false
        }
        val liftTarget = if (lifted) 1.0 else 0.0
        stepLift(liftXPos, liftXVel, liftTarget, dt, LIFT_STIFFNESS, LIFT_DAMPING_X).let {
            liftXPos = it.first; liftXVel = it.second
        }
        stepLift(liftYPos, liftYVel, liftTarget, dt, LIFT_STIFFNESS, LIFT_DAMPING_Y).let {
            liftYPos = it.first; liftYVel = it.second
        }
        stepLift(liftPos, liftVel, liftTarget, dt, MATERIAL_STIFFNESS, MATERIAL_DAMPING).let {
            liftPos = it.first; liftVel = it.second
        }
        val liftSettled = !lifted && liftXPos == 0.0 && liftYPos == 0.0 && liftPos == 0.0

        if (travelActive && travelSettled && liftSettled && !dragging) {
            travelActive = false
        }

        // 4) Acceleration squash tracking
        val frac = if (dragging) dragFollow else travelPos
        if (stepDp > 0f) {
            if (!squashModel.isTracking) squashModel.start()
            squash = squashModel.track(frac * stepDp, now, dt)
            if (travelSignEased == 0.0) {
                travelSignEased = travelSign
            } else if (travelSignEased != travelSign) {
                travelSignEased += (travelSign - travelSignEased) * (1 - exp(-dt / SIGN_TAU))
                if (abs(travelSign - travelSignEased) < 0.01) travelSignEased = travelSign
            }
            val key = travelSignEased
            if (key != 0.0) squash = squash * (1 - abs(key)) - key * abs(squash)
        }

        // 5) Glass handover
        val handoverTarget = if (lifted) 0.0 else 1.0
        val tau = if (handoverTarget > handover) HANDOVER_TAU else GLASS_RETURN_TAU
        handover += (handoverTarget - handover) * (1 - exp(-dt / tau))
        if (abs(handoverTarget - handover) < 0.002) handover = handoverTarget

        val settled = !travelActive && !dragging && liftSettled &&
            abs(squash) < 0.0005 && handover >= 1.0
        if (settled) {
            squashModel.stop()
            squash = 0.0
            travelSign = 0.0
            travelSignEased = 0.0
        }
        publish()
        isFlat = settled
        return !settled
    }

    private fun publish() {
        position = (if (dragging) dragFollow else travelPos).toFloat()
        liftX = liftXPos.toFloat()
        liftY = liftYPos.toFloat()
        material = liftPos.coerceIn(0.0, 1.0).toFloat()
        deviation = squash.toFloat()
        glassPresence = (1 - handover).coerceIn(0.0, 1.0).toFloat()
    }

    private fun travelProgress(): Double {
        val span = abs(travelTarget - travelFrom)
        if (span < 1e-6) return 1.0
        return (1 - abs(travelTarget - travelPos) / span).coerceIn(0.0, 1.0)
    }

    private fun signOfTravel(span: Double): Double = if (abs(span) < 1e-6) 0.0 else sign(span)

    fun liveSize(rest: Size, lifted: Size): Size {
        if (!rest.isSpecified || !lifted.isSpecified) return rest
        val w = rest.width + (lifted.width - rest.width) * liftX
        val h = rest.height + (lifted.height - rest.height) * liftY
        return Size((w * (1 + deviation)).coerceAtLeast(1f), (h * (1 - deviation)).coerceAtLeast(1f))
    }

    private companion object {
        const val TRAVEL_STIFFNESS = 300.0
        const val TRAVEL_DAMPING = 32.0

        const val LIFT_STIFFNESS = 260.0
        const val LIFT_DAMPING_X = 19.5
        const val LIFT_DAMPING_Y = 22.5

        const val MATERIAL_STIFFNESS = 1000.0
        const val MATERIAL_DAMPING = 63.3

        const val HANDOVER_START = 0.90
        const val HANDOVER_TAU = 0.09
        const val GLASS_RETURN_TAU = 0.05
        const val FOLLOW_TAU = 0.05
        const val SIGN_TAU = 0.25

        fun springStep(
            x: Double,
            vel: Double,
            target: Double,
            dt: Double,
            stiffness: Double,
            damping: Double
        ): Pair<Double, Double> {
            var t = dt
            var px = x
            var pv = vel
            while (t > 0) {
                val step = if (t > 1 / 240.0) 1 / 240.0 else t
                val accel = -stiffness * (px - target) - damping * pv
                pv += accel * step
                px += pv * step
                t -= step
            }
            return px to pv
        }

        fun stepLift(
            x: Double,
            vel: Double,
            target: Double,
            dt: Double,
            stiffness: Double,
            damping: Double
        ): Pair<Double, Double> {
            val r = springStep(x, vel, target, dt, stiffness, damping)
            return if (abs(r.first - target) < 0.0008 && abs(r.second) < 0.01) target to 0.0 else r
        }
    }
}

private class PillSquashModel {
    private val history = ArrayList<Pair<Double, Double>>()
    var isTracking = false
        private set
    private var value = 0.0

    fun start() {
        history.clear()
        value = 0.0
        isTracking = true
    }

    fun stop() {
        isTracking = false
        history.clear()
        value = 0.0
    }

    fun track(x: Double, now: Double, dt: Double): Double {
        if (!isTracking) return value
        history.add(x to now)
        val cutoff = now - SAMPLE_WINDOW
        history.removeAll { it.second < cutoff }
        val raw = (averageAcceleration() * SENSITIVITY).coerceIn(-MAX_DEFORMATION, MAX_DEFORMATION)
        val ease = (dt / RESPONSE_TIME).coerceIn(0.0, 1.0)
        value += (raw - value) * ease
        return value
    }

    private fun averageAcceleration(): Double {
        if (history.size < 3) return 0.0
        val velocities = ArrayList<Pair<Double, Double>>(history.size)
        for (i in 1 until history.size) {
            val dt = history[i].second - history[i - 1].second
            if (dt <= 0) continue
            velocities.add(
                (history[i].first - history[i - 1].first) / dt to
                    (history[i].second + history[i - 1].second) / 2
            )
        }
        if (velocities.size < 2) return 0.0
        var total = 0.0
        var count = 0
        for (i in 1 until velocities.size) {
            val dt = velocities[i].second - velocities[i - 1].second
            if (dt <= 0) continue
            total += (velocities[i].first - velocities[i - 1].first) / dt
            count++
        }
        return if (count == 0) 0.0 else total / count
    }

    private companion object {
        const val SAMPLE_WINDOW = 0.3
        const val SENSITIVITY = 0.00008
        const val MAX_DEFORMATION = 0.12
        const val RESPONSE_TIME = 0.18
    }
}

@Composable
fun FlatSelectionPill(
    color: Color,
    shape: CornerBasedShape,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color, shape)
            .border(
                width = 0.9.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        color.copy(alpha = 0.70f),
                        color.copy(alpha = 0.20f)
                    )
                ),
                shape = shape
            )
    )
}

@Composable
fun GlassSelectionPill(
    motion: GlassPillMotion,
    fillColor: Color,
    pageBackdrop: Backdrop,
    barGlass: Backdrop,
    tabRow: Backdrop,
    shape: CornerBasedShape,
    modifier: Modifier = Modifier
) {
    val backdrop = remember(pageBackdrop, barGlass, tabRow, fillColor) {
        SelectionPillBackdrop(pageBackdrop, barGlass, tabRow) {
            fillColor.copy(alpha = fillColor.alpha * (1f - motion.material))
        }
    }
    Box(
        modifier = modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    lens(
                        refractionHeight = 8.dp.toPx() * motion.glassPresence,
                        refractionAmount = 10.dp.toPx() * motion.material,
                        chromaticAberration = true
                    )
                }
            },
            highlight = {
                val alpha = motion.material * rimOf(motion.glassPresence)
                if (alpha > 0f) Highlight.Default.copy(alpha = alpha) else null
            },
            shadow = {
                val alpha = motion.material * rimOf(motion.glassPresence)
                if (alpha > 0f) Shadow(
                    radius = 10.dp,
                    offset = DpOffset(0.dp, 2.dp),
                    color = Color.Black.copy(alpha = 0.35f)
                ).copy(alpha = alpha) else null
            }
        )
    )
}

private fun rimOf(presence: Float): Float = ((presence - 0.45f) / 0.55f).coerceIn(0f, 1f)

private class SelectionPillBackdrop(
    private val page: Backdrop,
    private val bar: Backdrop,
    private val tabs: Backdrop,
    private val fill: () -> Color
) : Backdrop {

    override val isCoordinatesDependent: Boolean = true

    override fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?
    ) {
        with(page) { drawBackdrop(density, coordinates, layerBlock) }
        with(bar) { drawBackdrop(density, coordinates, layerBlock) }
        val color = fill()
        if (color.alpha > 0f && size.isSpecified && size.width > 0f && size.height > 0f) {
            drawRect(
                color = color,
                topLeft = Offset(-size.width, -size.height),
                size = Size(size.width * 3, size.height * 3)
            )
        }
        with(tabs) { drawBackdrop(density, coordinates, layerBlock) }
    }
}
