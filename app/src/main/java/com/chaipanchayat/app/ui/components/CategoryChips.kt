package com.chaipanchayat.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily

data class CategoryChipItem(
    val id: Long, // 0 for Latest
    val name: String
)

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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .width(76.dp)
                        .background(ChaiTheme.extended.skeleton, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        } else {
            chips.forEach { chip ->
                val isSelected = chip.id == selectedId
                Surface(
                    onClick = { onSelect(chip.id) },
                    shape = CircleShape,
                    color = if (isSelected) ChaiSaffron else ChaiTheme.extended.surfaceSecondary,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) ChaiSaffron else ChaiTheme.extended.border
                    ),
                    modifier = Modifier
                        .testTag("chip-${chip.id}")
                        .height(36.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chip.name,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontFamily = InterFamily,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
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
