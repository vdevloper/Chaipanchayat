package com.kyant.backdrop

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackdropSizeSafetyTest {

    @Test
    fun testInitialSizeIsUnspecifiedAndSafe() {
        val scope = object : BackdropEffectScopeImpl() {
            override val shape: Shape = RoundedCornerShape(16.dp)
        }

        // 1. Verify initial size is Unspecified
        assertEquals(Size.Unspecified, scope.size)
        assertFalse(scope.size.isSpecified)

        // 2. Applying effects on unspecified size must not throw IllegalStateException: Size is unspecified
        var applied = false
        try {
            scope.apply {
                vibrancy()
                blur(18f)
                lens(refractionHeight = 12f, refractionAmount = 24f)
            }
            applied = true
        } catch (e: IllegalStateException) {
            applied = false
        }
        assertTrue("Applying effects on unspecified size must not throw", applied)

        // 3. Update with valid measurement dimensions
        val density = Density(density = 2.75f, fontScale = 1.0f)
        val measuredSize = Size(1080f, 168f)
        val changed = scope.update(density, measuredSize, LayoutDirection.Ltr)

        assertTrue(changed)
        assertTrue(scope.size.isSpecified)
        assertEquals(1080f, scope.size.width, 0.01f)
        assertEquals(168f, scope.size.height, 0.01f)

        // 4. Applying effects on valid measured size must not throw
        scope.apply {
            vibrancy()
            blur(18f)
            lens(refractionHeight = 12f, refractionAmount = 24f)
        }
        assertTrue(scope.padding >= 0f)

        // 5. Test reset returning to unspecified cleanly
        scope.reset()
        assertEquals(Size.Unspecified, scope.size)
        assertFalse(scope.size.isSpecified)
    }

    @Test
    fun testGlassPillMotionSizeSafetyAndSnap() {
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined)
        val motion = com.chaipanchayat.app.engine.liquidglass.GlassPillMotion(scope, initialIndex = 2)

        assertEquals(2f, motion.position, 0.001f)
        assertTrue(motion.isFlat)

        // Verify liveSize is safe when given Size.Unspecified
        val unspecifiedResult = motion.liveSize(Size.Unspecified, Size(200f, 100f))
        assertFalse(unspecifiedResult.isSpecified)

        val validResult = motion.liveSize(Size(180f, 90f), Size(200f, 108f))
        assertTrue(validResult.isSpecified)
        assertEquals(180f, validResult.width, 0.01f)
        assertEquals(90f, validResult.height, 0.01f)

        // Verify snapTo updates position and maintains flat state
        motion.snapTo(4)
        assertEquals(4, motion.index)
        assertEquals(4f, motion.position, 0.001f)
        assertTrue(motion.isFlat)
    }
}
