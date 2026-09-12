package com.chaipanchayat.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.data.api.WordPressApiClient
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.ui.components.EmptyState
import com.chaipanchayat.app.ui.components.HeroCard
import com.chaipanchayat.app.ui.components.HeroSkeleton
import com.chaipanchayat.app.ui.components.NewsCard
import com.chaipanchayat.app.ui.components.NewsCardSkeleton
import com.chaipanchayat.app.ui.components.TopBar
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFeedScreen(
    categoryId: Long,
    categoryName: String,
    onNavigateToArticle: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var posts by remember { mutableStateOf<List<WPPost>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    suspend fun loadFeed(showLoader: Boolean = true) {
        if (showLoader) isLoading = true
        try {
            posts = WordPressApiClient.fetchPosts(categoryId = categoryId)
            isError = false
        } catch (_: Exception) {
            isError = true
        } finally {
            isLoading = false
            isRefreshing = false
        }
    }

    LaunchedEffect(categoryId) {
        loadFeed(showLoader = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TopBar(onBack = onBack)

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = categoryName,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = NotoSerifFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                lineHeight = 32.sp
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    coroutineScope.launch { loadFeed(showLoader = false) }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                if (isLoading) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = false
                    ) {
                        item { HeroSkeleton() }
                        items(4) { NewsCardSkeleton() }
                    }
                } else if (isError && posts.isEmpty()) {
                    EmptyState(
                        title = "Could not load stories",
                        message = "Please check your network connection and try again.",
                        actionLabel = "Retry",
                        onAction = { coroutineScope.launch { loadFeed(showLoader = true) } }
                    )
                } else if (posts.isEmpty()) {
                    EmptyState(
                        title = "No stories yet",
                        message = "No articles have been published under $categoryName yet."
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            val hero = posts.first()
                            HeroCard(
                                post = hero,
                                onClick = { onNavigateToArticle(hero.id) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        itemsIndexed(posts.drop(1), key = { _, post -> post.id }) { _, post ->
                            NewsCard(
                                post = post,
                                onClick = { onNavigateToArticle(post.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}
