package com.chaipanchayat.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.ui.theme.ChaiAmber
import com.chaipanchayat.app.ui.theme.ChaiAmberGradient
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiGold
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium custom loading animation for Chai Panchayat.
 * Features an authentic hand-crafted Kulhad tea cup with:
 * - Undulating warm chai liquid surface physics
 * - Smooth quadratic bezier rising steam ribbons with alpha fade
 * - Glowing celestial amber aura and rotating spice orbit
 * - Popping aromatic tea pearl particles
 */
@Composable
fun ChaiBrewLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 68.dp,
    tintColor: Color = ChaiSaffron,
    showAura: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chai_brew_loop")

    // Liquid wave phase
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "liquid_wave"
    )

    // Steam rise offset (0f to 1f)
    val steamProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "steam_rise"
    )

    // Ambient pulsing aura
    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse"
    )

    // Orbital ring rotation
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag("chai_brew_loading_indicator"),
        contentAlignment = Alignment.Center
    ) {
        // Soft pulsing ambient halo
        if (showAura) {
            Box(
                modifier = Modifier
                    .size(size * 0.9f)
                    .scale(auraPulse)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                tintColor.copy(alpha = 0.35f),
                                tintColor.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // 1. Orbital celestial dash ring around the cup
            val ringRadius = w * 0.46f
            val ringCenter = Offset(w / 2f, h / 2f + h * 0.05f)

            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        tintColor.copy(alpha = 0.05f),
                        tintColor.copy(alpha = 0.45f),
                        ChaiGold.copy(alpha = 0.75f),
                        tintColor.copy(alpha = 0.05f)
                    ),
                    center = ringCenter
                ),
                radius = ringRadius,
                center = ringCenter,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Orbiting spice pearl
            val angleRad = Math.toRadians(ringRotation.toDouble())
            val pearlX = ringCenter.x + ringRadius * cos(angleRad).toFloat()
            val pearlY = ringCenter.y + ringRadius * sin(angleRad).toFloat()
            drawCircle(
                color = ChaiGold,
                radius = 2.5.dp.toPx(),
                center = Offset(pearlX, pearlY)
            )

            // 2. Rising Steam Ribbons (3 curved sinusoidal steam trails)
            val steamBaseY = h * 0.42f
            val steamMaxHeight = h * 0.38f

            val steamPaths = listOf(
                Triple(w * 0.42f, -12f, 0.0f),  // Left ribbon
                Triple(w * 0.50f, 15f, 0.33f),  // Center ribbon
                Triple(w * 0.58f, -10f, 0.66f)  // Right ribbon
            )

            for ((startX, swayAmp, phaseOffset) in steamPaths) {
                val individualProgress = (steamProgress + phaseOffset) % 1f
                val steamY = steamBaseY - individualProgress * steamMaxHeight
                val currentAlpha = sin(individualProgress * PI).toFloat().coerceIn(0f, 1f) * 0.85f

                val swayX = sin(wavePhase + phaseOffset * 2 * PI).toFloat() * swayAmp.dp.toPx()

                val steamPath = Path().apply {
                    moveTo(startX, steamBaseY)
                    cubicTo(
                        startX + swayX * 0.5f, steamBaseY - steamMaxHeight * 0.33f,
                        startX - swayX * 0.7f, steamBaseY - steamMaxHeight * 0.66f,
                        startX + swayX, steamY
                    )
                }

                drawPath(
                    path = steamPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0f),
                            Color(0xFFFFF3E0).copy(alpha = currentAlpha * 0.9f),
                            ChaiAmber.copy(alpha = currentAlpha * 0.5f)
                        ),
                        startY = steamY,
                        endY = steamBaseY
                    ),
                    style = Stroke(
                        width = (2.2f + individualProgress * 1.5f).dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Tiny rising aroma bubble at the steam tip
                if (individualProgress > 0.3f && individualProgress < 0.85f) {
                    drawCircle(
                        color = Color.White.copy(alpha = currentAlpha * 0.6f),
                        radius = (1.5f + individualProgress * 0.8f).dp.toPx(),
                        center = Offset(startX + swayX, steamY)
                    )
                }
            }

            // 3. Terracotta Kulhad Glass / Clay Cup Geometry
            // Traditional tapered Indian earthen tea glass
            val cupTopY = h * 0.44f
            val cupBottomY = h * 0.85f
            val cupTopWidth = w * 0.48f
            val cupBottomWidth = w * 0.32f

            val topLeft = Offset((w - cupTopWidth) / 2f, cupTopY)
            val topRight = Offset((w + cupTopWidth) / 2f, cupTopY)
            val bottomLeft = Offset((w - cupBottomWidth) / 2f, cupBottomY)
            val bottomRight = Offset((w + cupBottomWidth) / 2f, cupBottomY)

            // Saucer / Base Shadow
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.22f),
                topLeft = Offset(bottomLeft.x - w * 0.06f, cupBottomY + h * 0.01f),
                size = Size(cupBottomWidth + w * 0.12f, h * 0.05f),
                cornerRadius = CornerRadius(6.dp.toPx(), 4.dp.toPx())
            )

            // Cup Body Path
            val cupPath = Path().apply {
                moveTo(topLeft.x, topLeft.y)
                // Left tapered side
                lineTo(bottomLeft.x, bottomLeft.y - h * 0.02f)
                // Bottom curved base
                quadraticTo(
                    w / 2f, bottomLeft.y + h * 0.02f,
                    bottomRight.x, bottomRight.y - h * 0.02f
                )
                // Right tapered side
                lineTo(topRight.x, topRight.y)
                // Top curved rim
                quadraticTo(
                    w / 2f, cupTopY + h * 0.025f,
                    topLeft.x, topLeft.y
                )
                close()
            }

            // Terracotta Clay Cup Fill (Warm earthen gradient)
            val clayBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF8B3A14), // Dark terracotta shadow
                    Color(0xFFB45309), // Warm baked clay
                    Color(0xFFD97706), // Sunlit clay highlight
                    Color(0xFFA14316)  // Outer rim edge
                ),
                startX = topLeft.x,
                endX = topRight.x
            )
            drawPath(path = cupPath, brush = clayBrush, style = Fill)

            // 4. Kulhad Earthen Ridges (Classic Indian ribbed clay texture)
            val ridgeY1 = cupTopY + (cupBottomY - cupTopY) * 0.32f
            val ridgeY2 = cupTopY + (cupBottomY - cupTopY) * 0.58f

            val ridgeWidth1 = cupTopWidth * 0.88f
            val ridgeWidth2 = cupTopWidth * 0.74f

            drawLine(
                color = Color(0xFF6B2B0C).copy(alpha = 0.6f),
                start = Offset((w - ridgeWidth1) / 2f, ridgeY1),
                end = Offset((w + ridgeWidth1) / 2f, ridgeY1),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFFBBF24).copy(alpha = 0.35f),
                start = Offset((w - ridgeWidth1) / 2f, ridgeY1 + 1.2.dp.toPx()),
                end = Offset((w + ridgeWidth1) / 2f, ridgeY1 + 1.2.dp.toPx()),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )

            drawLine(
                color = Color(0xFF6B2B0C).copy(alpha = 0.6f),
                start = Offset((w - ridgeWidth2) / 2f, ridgeY2),
                end = Offset((w + ridgeWidth2) / 2f, ridgeY2),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 5. Steaming Hot Chai Liquid Surface (Top ellipse with undulating wave)
            val liquidRimY = cupTopY + h * 0.015f
            val liquidRimWidth = cupTopWidth * 0.94f
            val liquidRimHeight = h * 0.065f

            // Steaming hot chai gradient
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFEF3C7), // Frothy golden cream center
                        Color(0xFFF59E0B), // Saffron milk tea
                        Color(0xFFB45309)  // Deep spiced brew edge
                    ),
                    center = Offset(w / 2f, liquidRimY + liquidRimHeight / 2f),
                    radius = liquidRimWidth / 2f
                ),
                topLeft = Offset((w - liquidRimWidth) / 2f, liquidRimY),
                size = Size(liquidRimWidth, liquidRimHeight)
            )

            // Undulating wave highlights across the chai foam
            val waveOffset = sin(wavePhase).toFloat() * 1.8.dp.toPx()
            drawLine(
                color = Color.White.copy(alpha = 0.75f),
                start = Offset(w / 2f - liquidRimWidth * 0.28f, liquidRimY + liquidRimHeight * 0.45f + waveOffset),
                end = Offset(w / 2f + liquidRimWidth * 0.28f, liquidRimY + liquidRimHeight * 0.45f - waveOffset),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Cup Rim Highlight
            drawOval(
                color = Color(0xFFFFD8A8).copy(alpha = 0.5f),
                topLeft = Offset((w - cupTopWidth) / 2f, cupTopY),
                size = Size(cupTopWidth, h * 0.045f),
                style = Stroke(width = 1.2.dp.toPx())
            )
        }
    }
}

/**
 * Compact inline spinner variant.
 * Perfect drop-in replacement for any [androidx.compose.material3.CircularProgressIndicator].
 */
@Composable
fun ChaiBrewSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    tintColor: Color = ChaiSaffron
) {
    ChaiBrewLoadingIndicator(
        modifier = modifier,
        size = size,
        tintColor = tintColor,
        showAura = false
    )
}

/**
 * Fullscreen or card-level loading container for Chai Panchayat.
 * Renders the custom animated Kulhad tea cup, pulsing warm glow,
 * and cycling editorial Hindi loading quotes.
 */
@Composable
fun ChaiBrewLoadingScreen(
    modifier: Modifier = Modifier,
    customMessage: String? = null,
    showQuotes: Boolean = true
) {
    val editorialQuotes = remember {
        listOf(
            "चाय पक रही है... ताज़ा ख़बरें आ रही हैं...",
            "संपादकीय पन्ने पलट रहे हैं...",
            "निष्पक्ष पत्रकारिता, तथ्य-आधारित विश्लेषण...",
            "चाय की चुस्की के साथ ताज़ा पंचायत...",
            "विश्वसनीय और स्वतंत्र ग्राउंड रिपोर्टिंग..."
        )
    }

    var quoteIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2400)
            quoteIndex = (quoteIndex + 1) % editorialQuotes.size
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .testTag("chai_brew_loading_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Hero Custom Animated Chai Cup
            ChaiBrewLoadingIndicator(
                size = 88.dp,
                tintColor = ChaiSaffron,
                showAura = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Brand title pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ChaiSaffron.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ChaiSaffron.copy(alpha = 0.35f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(ChaiBrandGradient, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "चाय पंचायत • लाइव अपडेट",
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = ChaiTheme.extended.brandText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic cycling status text
            if (customMessage != null) {
                Text(
                    text = customMessage,
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            } else if (showQuotes) {
                AnimatedContent(
                    targetState = quoteIndex,
                    transitionSpec = {
                        (fadeIn(tween(400)) + androidx.compose.animation.scaleIn(initialScale = 0.94f)) togetherWith
                                (fadeOut(tween(300)) + androidx.compose.animation.scaleOut(targetScale = 1.04f))
                    },
                    label = "quote_anim"
                ) { index ->
                    Text(
                        text = editorialQuotes[index],
                        fontFamily = NotoSerifFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Text(
                text = "कृपया प्रतीक्षा करें • सामग्री लोड हो रही है",
                fontFamily = InterFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = ChaiTheme.extended.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
