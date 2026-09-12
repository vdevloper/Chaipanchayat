package com.chaipanchayat.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiGold
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.chaipanchayat.app.utils.DateUtils

/**
 * Spec Section 08 — “☕ आज की चाय”
 * Signature Chai Panchayat feature:
 * Title: ☕ आज की चाय
 * Subtitle: आज की 5 बड़ी खबरें — 2 मिनट में
 * Display five compact stories: 01 … 02 … 03 … 04 … 05 …
 * Each item opens the relevant article.
 */
@Composable
fun TodayChaiDigest(
    posts: List<WPPost>,
    onPostClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (posts.isEmpty()) return

    val digestStories = posts.take(5)
    val cardShape = RoundedCornerShape(18.dp)
    val isLiquid = ChaiTheme.extended.isLiquidGlass

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = ChaiTheme.extended.surfaceSecondary),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLiquid) 4.dp else 2.dp),
        modifier = modifier
            .testTag("today-chai-digest")
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .then(
                if (isLiquid) {
                    Modifier.border(1.dp, ChaiTheme.extended.glassBorderBrush, cardShape)
                } else {
                    Modifier.border(1.dp, ChaiTheme.extended.border.copy(alpha = 0.8f), cardShape)
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: ☕ आज की चाय
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Stylized Cup Icon Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ChaiSaffron.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ChaiSaffron.copy(alpha = 0.3f)),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "☕",
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "आज की चाय",
                            fontFamily = NotoSerifFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "आज की 5 बड़ी खबरें — 2 मिनट में",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = ChaiTheme.extended.muted
                        )
                    }
                }

                // Quick badge
                Surface(
                    shape = CircleShape,
                    color = ChaiSaffron.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, ChaiSaffron.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "5 MIN",
                        color = ChaiSaffron,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 0.8.dp, color = ChaiTheme.extended.border.copy(alpha = 0.5f))

            // 5 Stories List
            digestStories.forEachIndexed { index, post ->
                val numberStr = String.format("%02d", index + 1)
                DigestItemRow(
                    number = numberStr,
                    post = post,
                    onClick = { onPostClick(post.id) }
                )
                if (index < digestStories.size - 1) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = ChaiTheme.extended.border.copy(alpha = 0.4f),
                        modifier = Modifier.padding(start = 36.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DigestItemRow(
    number: String,
    post: WPPost,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "digest_item_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Number badge
        Text(
            text = number,
            fontFamily = InterFamily,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            color = ChaiSaffron,
            letterSpacing = 0.5.sp,
            modifier = Modifier.width(32.dp)
        )

        // Headline & Meta
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(
                text = post.cleanTitle,
                fontFamily = NotoSerifFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!post.primaryCategory.isNullOrBlank()) {
                    Text(
                        text = post.primaryCategory.uppercase(),
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = ChaiGold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = " • ",
                        color = ChaiTheme.extended.muted,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = DateUtils.timeAgo(post.date),
                    fontFamily = InterFamily,
                    fontSize = 10.5.sp,
                    color = ChaiTheme.extended.muted
                )
            }
        }

        // Mini forward chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = ChaiTheme.extended.muted.copy(alpha = 0.7f),
            modifier = Modifier.size(15.dp)
        )
    }
}
