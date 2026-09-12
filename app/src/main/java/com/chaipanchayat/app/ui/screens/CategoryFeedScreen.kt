package com.chaipanchayat.app.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.data.repository.NewsRepository
import com.chaipanchayat.app.ui.components.EmptyState
import com.chaipanchayat.app.ui.components.HeroCard
import com.chaipanchayat.app.ui.components.HeroSkeleton
import com.chaipanchayat.app.ui.components.NewsCard
import com.chaipanchayat.app.ui.components.NewsCardSkeleton
import com.chaipanchayat.app.ui.components.TopBar
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
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
    val repository = remember { NewsRepository.getInstance() }
    val initialCached = remember { repository.getCachedPosts(categoryId).orEmpty() }

    var posts by remember { mutableStateOf(initialCached) }
    var isLoading by remember { mutableStateOf(initialCached.isEmpty()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    suspend fun loadFeed(showLoader: Boolean = true) {
        val cached = repository.getCachedPosts(categoryId)
        if (!cached.isNullOrEmpty()) {
            posts = cached
            isLoading = false
        } else if (showLoader && posts.isEmpty()) {
            isLoading = true
        }

        try {
            posts = repository.getPosts(categoryId = categoryId, forceRefresh = isRefreshing)
            isError = false
        } catch (_: Exception) {
            if (posts.isEmpty()) {
                isError = true
            }
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
        TopBar(onBack = onBack, title = categoryName)

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(20.dp)
                            .background(ChaiBrandGradient, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = categoryName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = NotoSerifFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        lineHeight = 28.sp
                    )
                }

                if (posts.isNotEmpty()) {
                    Surface(
                        shape = CircleShape,
                        color = ChaiTheme.extended.brandTertiary
                    ) {
                        Text(
                            text = "${posts.size} लेख",
                            color = ChaiSaffron,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
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
                        title = "खबरें लोड नहीं हो सकीं",
                        message = "कृपया अपना इंटरनेट कनेक्शन जांचें और पुनः प्रयास करें।",
                        actionLabel = "पुनः प्रयास करें",
                        onAction = { coroutineScope.launch { loadFeed(showLoader = true) } }
                    )
                } else if (posts.isEmpty()) {
                    EmptyState(
                        title = "इस श्रेणी में कोई खबर नहीं है",
                        message = "$categoryName में फिलहाल कोई लेख प्रकाशित नहीं हुआ है।"
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            val hero = posts.first()
                            HeroCard(
                                post = hero,
                                onClick = { onNavigateToArticle(hero.id) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        itemsIndexed(posts.drop(1), key = { _, post -> post.id }) { _, post ->
                            NewsCard(
                                post = post,
                                onClick = { onNavigateToArticle(post.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(28.dp))
                        }
                    }
                }
            }
        }
    }
}
