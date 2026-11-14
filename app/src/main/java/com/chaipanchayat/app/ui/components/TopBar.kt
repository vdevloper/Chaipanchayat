package com.chaipanchayat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.engine.liquidglass.ChaiGlassHeader
import com.chaipanchayat.app.engine.liquidglass.LocalBackdrop
import com.chaipanchayat.app.engine.liquidglass.liquidGlass
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.kyant.backdrop.Backdrop

@Composable
fun TopBar(
    title: String? = null,
    onBack: (() -> Unit)? = null,
    rightActions: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backdrop: Backdrop? = LocalBackdrop.current,
    modifier: Modifier = Modifier
) {
    val isLiquid = ChaiTheme.extended.isLiquidGlass

    if (isLiquid && backdrop != null) {
        ChaiGlassHeader(
            backdrop = backdrop,
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (onBack != null) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .liquidGlass(
                                    shape = CircleShape,
                                    blurRadiusDp = 18.dp,
                                    backdrop = backdrop
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("topbar-back-button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    if (title != null) {
                        Text(
                            text = title,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = NotoSerifFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Logo()
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rightActions?.invoke()
                    actions()
                }
            }
        }
    } else {
        Surface(
            color = if (isLiquid) Color(0xCC121214) else MaterialTheme.colorScheme.surface,
            modifier = modifier.fillMaxWidth()
        ) {
            androidx.compose.foundation.layout.Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(58.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (onBack != null) {
                            Surface(
                                shape = CircleShape,
                                color = ChaiTheme.extended.surfaceSecondary,
                                modifier = Modifier.size(38.dp)
                            ) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier.testTag("topbar-back-button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        if (title != null) {
                            Text(
                                text = title,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = NotoSerifFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Logo()
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rightActions?.invoke()
                        actions()
                    }
                }

                HorizontalDivider(
                    thickness = 0.8.dp,
                    color = ChaiTheme.extended.border.copy(alpha = 0.7f)
                )
            }
        }
    }
}
