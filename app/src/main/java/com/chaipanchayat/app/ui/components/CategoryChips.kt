package com.chaipanchayat.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily

data class CategoryChipItem(
    val id: Long, // 0 for Latest
    val name: String
)

private fun getCategoryEmoji(name: String): String {
    val lower = name.lowercase()
    return when {
        lower.contains("latest") || lower.contains("ताज़ा") -> "🔥"
        lower.contains("politic") || lower.contains("राजनीति") -> "🏛️"
        lower.contains("nation") || lower.contains("देश") || lower.contains("भारत") -> "🇮🇳"
        lower.contains("crime") || lower.contains("अपराध") -> "🚨"
        lower.contains("sport") || lower.contains("खेल") || lower.contains("cricket") -> "🏏"
        lower.contains("cinema") || lower.contains("bollywood") || lower.contains("मनोरंजन") -> "🎬"
        lower.contains("tech") || lower.contains("तकनीक") -> "📱"
        lower.contains("world") || lower.contains("विदेश") -> "🌍"
        lower.contains("business") || lower.contains("व्यापार") -> "📈"
        else -> "📰"
    }
}

@Composable
fun CategoryChips(
    chips: List<CategoryChipItem>,
    selectedId: Long,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .testTag("category-chip-row")
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            val shimmerBrush = rememberShimmerBrush()
            repeat(5) {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .width(82.dp)
                        .clip(CircleShape)
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.width(9.dp))
            }
        } else {
            chips.forEach { chip ->
                val isSelected = chip.id == selectedId
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.94f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
                    label = "chip_scale"
                )

                Surface(
                    shape = CircleShape,
                    color = if (isSelected) Color.Transparent else ChaiTheme.extended.surfaceSecondary,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) ChaiSaffron else ChaiTheme.extended.border.copy(alpha = 0.7f)
                    ),
                    shadowElevation = if (isSelected) 3.dp else 0.dp,
                    modifier = Modifier
                        .testTag("chip-${chip.id}")
                        .height(38.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .then(
                            if (isSelected) Modifier.background(ChaiBrandGradient) else Modifier
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelect(chip.id) }
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getCategoryEmoji(chip.name),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = chip.name,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontFamily = InterFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            letterSpacing = 0.2.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}
