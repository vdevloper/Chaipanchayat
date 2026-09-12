package com.chaipanchayat.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chaipanchayat.app.data.api.WordPressApiClient
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.data.repository.BookmarkRepository
import com.chaipanchayat.app.data.repository.NewsRepository
import com.chaipanchayat.app.data.repository.SettingsRepository
import com.chaipanchayat.app.ui.components.ArticleHtmlView
import com.chaipanchayat.app.ui.components.ArticleSkeleton
import com.chaipanchayat.app.ui.components.EmptyState
import com.chaipanchayat.app.ui.components.TopBar
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.chaipanchayat.app.utils.DateUtils
import kotlinx.coroutines.launch

@Composable
fun ArticleScreen(
    postId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bookmarkRepo = BookmarkRepository.getInstance(context)
    val settingsRepo = SettingsRepository.getInstance(context)

    val textSize by settingsRepo.textSize.collectAsState()
    val isBookmarked by bookmarkRepo.isBookmarked(postId).collectAsState(initial = false)

    var post by remember { mutableStateOf<WPPost?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(postId) {
        isLoading = true
        try {
            val fetched = NewsRepository.getInstance().getPost(postId)
            post = fetched
            isError = false
        } catch (e: Exception) {
            isError = true
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Top Navigation Bar
        TopBar(
            onBack = onBack,
            rightActions = {
                post?.let { p ->
                    IconButton(
                        onClick = {
                            if (p.link.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(p.link))
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.testTag("article-open-web")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = "Open in browser",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        )

        // Content Area
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                ArticleSkeleton()
            } else if (isError || post == null) {
                EmptyState(
                    title = "Story unavailable",
                    message = "Could not load this story. Please check your network connection.",
                    actionLabel = "Retry",
                    onAction = {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                post = WordPressApiClient.fetchPost(postId)
                                isError = false
                            } catch (_: Exception) {
                                isError = true
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            } else {
                val currentPost = post!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Kicker
                    if (!currentPost.primaryCategory.isNullOrBlank()) {
                        Text(
                            text = currentPost.primaryCategory.uppercase(),
                            color = ChaiSaffron,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Headline
                    Text(
                        text = currentPost.cleanTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = NotoSerifFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        lineHeight = 33.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Date & Author Byline
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DateUtils.formatDateTime(currentPost.date),
                            color = ChaiTheme.extended.muted,
                            fontFamily = InterFamily,
                            fontSize = 12.sp
                        )
                        if (!currentPost.authorName.isNullOrBlank()) {
                            Text(
                                text = " · by ${currentPost.authorName}",
                                color = ChaiTheme.extended.muted,
                                fontFamily = InterFamily,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Featured Image
                    if (!currentPost.featuredImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(currentPost.featuredImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = currentPost.cleanTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ChaiTheme.extended.skeleton)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Native Parsed Article Body HTML
                    ArticleHtmlView(
                        html = currentPost.rawContent,
                        multiplier = textSize.multiplier
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Sticky Bottom Action Bar
        post?.let { currentPost ->
            HorizontalDivider(thickness = 0.5.dp, color = ChaiTheme.extended.border)

            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bookmark Toggle
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                bookmarkRepo.toggleBookmark(currentPost)
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (isBookmarked) ChaiSaffron else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("article-bookmark-button")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Saved" else "Save",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBookmarked) "Saved" else "Save",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    // Text Size Cycle
                    TextButton(
                        onClick = { settingsRepo.cycleTextSize() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("article-textsize-button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FormatSize,
                            contentDescription = "Text Size",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = textSize.displayName,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    // Share Button
                    TextButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "${currentPost.cleanTitle}\n\n${currentPost.link}")
                                putExtra(Intent.EXTRA_SUBJECT, currentPost.cleanTitle)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Article")
                            context.startActivity(shareIntent)
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("article-share-button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
