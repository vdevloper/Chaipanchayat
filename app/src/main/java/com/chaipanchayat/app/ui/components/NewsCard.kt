package com.chaipanchayat.app.ui.components

import android.content.Intent
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.data.repository.BookmarkRepository
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.chaipanchayat.app.utils.DateUtils
import kotlinx.coroutines.launch

@Composable
fun NewsCard(
    post: WPPost,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)
    val thumbShape = RoundedCornerShape(12.dp)
    val context = LocalContext.current
    val bookmarkRepo = remember { BookmarkRepository.getInstance(context) }
    val isBookmarked by bookmarkRepo.isBookmarked(post.id).collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
        label = "card_scale"
    )

    val isLiquid = ChaiTheme.extended.isLiquidGlass

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = ChaiTheme.extended.surfaceSecondary),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLiquid) 3.dp else 1.dp),
        modifier = modifier
            .testTag("news-card-${post.id}")
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .scale(scale)
            .then(
                if (isLiquid) {
                    Modifier.border(
                        width = 1.dp,
                        brush = ChaiTheme.extended.glassBorderBrush,
                        shape = cardShape
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = ChaiTheme.extended.border.copy(alpha = 0.7f),
                        shape = cardShape
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Thumbnail with aspect ratio & rounded corners
            Box(
                modifier = Modifier
                    .size(width = 118.dp, height = 98.dp)
                    .clip(thumbShape)
                    .background(ChaiTheme.extended.skeleton)
            ) {
                if (!post.featuredImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(post.featuredImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = post.cleanTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Body Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(98.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Category Chip / Tag
                if (!post.primaryCategory.isNullOrBlank()) {
                    Text(
                        text = post.primaryCategory.uppercase(),
                        color = ChaiSaffron,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.6.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Headline
                Text(
                    text = post.cleanTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Meta and Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateUtils.timeAgo(post.date),
                        color = ChaiTheme.extended.muted,
                        fontFamily = InterFamily,
                        fontSize = 11.5.sp,
                        maxLines = 1
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Bookmark
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    bookmarkRepo.toggleBookmark(post)
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Save story",
                                tint = if (isBookmarked) ChaiSaffron else ChaiTheme.extended.muted,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Quick Share
                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "${post.cleanTitle}\n\nपूरी खबर पढ़ें: ${post.link}\n\nचाय पंचायत - www.chaipanchayat.com"
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "खबर शेयर करें"))
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share story",
                                tint = ChaiTheme.extended.muted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
