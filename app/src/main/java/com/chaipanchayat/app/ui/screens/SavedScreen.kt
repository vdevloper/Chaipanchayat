package com.chaipanchayat.app.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chaipanchayat.app.data.model.BookmarkEntity
import com.chaipanchayat.app.data.repository.BookmarkRepository
import com.chaipanchayat.app.ui.components.EmptyState
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.chaipanchayat.app.utils.DateUtils
import kotlinx.coroutines.launch

@Composable
fun SavedScreen(
    onNavigateToArticle: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bookmarkRepo = BookmarkRepository.getInstance(context)
    val bookmarks by bookmarkRepo.allBookmarks.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Top Brand Ribbon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(ChaiBrandGradient)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(22.dp)
                                .background(ChaiBrandGradient, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "सहेजी गई खबरें",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = NotoSerifFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                    }
                    Text(
                        text = if (bookmarks.isEmpty()) "बाद में पढ़ने के लिए अपनी पसंदीदा खबरें यहाँ रखें।" else "${bookmarks.size} खबरें सुरक्षित हैं",
                        color = ChaiTheme.extended.muted,
                        fontFamily = InterFamily,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 14.dp, top = 2.dp)
                    )
                }

                if (bookmarks.isNotEmpty()) {
                    Surface(
                        shape = CircleShape,
                        color = ChaiTheme.extended.brandTertiary
                    ) {
                        Text(
                            text = "${bookmarks.size} सहेजे गए",
                            color = ChaiSaffron,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        if (bookmarks.isEmpty()) {
            EmptyState(
                title = "अभी कुछ सेव नहीं किया",
                message = "किसी खबर पर 🔖 दबाएँ और वह बाद में यहाँ दिखाई देगी।",
                icon = Icons.Outlined.BookmarkBorder
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(bookmarks, key = { it.id }) { item ->
                    SavedArticleCard(
                        bookmark = item,
                        onClick = { onNavigateToArticle(item.id) },
                        onRemove = {
                            coroutineScope.launch {
                                bookmarkRepo.removeBookmark(item.id)
                            }
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

@Composable
fun SavedArticleCard(
    bookmark: BookmarkEntity,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "saved_scale"
    )

    val cardShape = RoundedCornerShape(14.dp)
    val thumbShape = RoundedCornerShape(10.dp)

    val isLiquid = ChaiTheme.extended.isLiquidGlass

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = ChaiTheme.extended.surfaceSecondary),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLiquid) 3.dp else 1.dp),
        modifier = Modifier
            .testTag("saved-card-${bookmark.id}")
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
                        width = 0.8.dp,
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 75.dp)
                    .clip(thumbShape)
                    .background(ChaiTheme.extended.skeleton)
            ) {
                if (!bookmark.image.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(bookmark.image)
                            .crossfade(true)
                            .build(),
                        contentDescription = bookmark.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (!bookmark.category.isNullOrBlank()) {
                    Text(
                        text = bookmark.category.uppercase(),
                        color = ChaiSaffron,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = bookmark.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = DateUtils.timeAgo(bookmark.date),
                    color = ChaiTheme.extended.muted,
                    fontFamily = InterFamily,
                    fontSize = 11.sp
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(32.dp)
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.testTag("remove-bookmark-${bookmark.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Remove bookmark",
                        tint = ChaiTheme.extended.muted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
