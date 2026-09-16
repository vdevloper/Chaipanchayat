package com.chaipanchayat.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.data.repository.NewsRepository
import com.chaipanchayat.app.ui.components.BreakingNewsTicker
import com.chaipanchayat.app.ui.components.CategoryChipItem
import com.chaipanchayat.app.ui.components.CategoryChips
import com.chaipanchayat.app.ui.components.EmptyState
import com.chaipanchayat.app.ui.components.HeroCard
import com.chaipanchayat.app.ui.components.HeroSkeleton
import com.chaipanchayat.app.ui.components.Logo
import com.chaipanchayat.app.ui.components.NewStoriesPill
import com.chaipanchayat.app.ui.components.NewsCard
import com.chaipanchayat.app.ui.components.NewsCardSkeleton
import com.chaipanchayat.app.ui.components.OfflineBanner
import com.chaipanchayat.app.ui.components.TodayChaiDigest
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToArticle: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { NewsRepository.getInstance() }
    val initialCachedPosts = remember { repository.getCachedPosts(null).orEmpty() }
    val initialCachedCats = remember { repository.getCachedCategories().orEmpty() }

    var posts by remember { mutableStateOf(initialCachedPosts) }
    var categories by remember { mutableStateOf(initialCachedCats) }
    var selectedCategoryId by remember { mutableLongStateOf(0L) }
    var isLoading by remember { mutableStateOf(initialCachedPosts.isEmpty()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }
    var newStoriesCount by remember { mutableIntStateOf(0) }

    // Refresh icon spin animation
    var refreshRotation by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = refreshRotation,
        animationSpec = tween(durationMillis = 600),
        label = "refresh_rotation"
    )

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    suspend fun loadFeed(showLoader: Boolean = true) {
        val catId = if (selectedCategoryId == 0L) null else selectedCategoryId
        val cached = repository.getCachedPosts(catId)
        if (!cached.isNullOrEmpty()) {
            posts = cached
            isLoading = false
        } else if (showLoader && posts.isEmpty()) {
            isLoading = true
        }

        try {
            val fetchedPosts = repository.getPosts(categoryId = catId, forceRefresh = isRefreshing)
            posts = fetchedPosts
            isOffline = false
            newStoriesCount = 0
        } catch (e: Exception) {
            if (posts.isEmpty()) {
                isOffline = true
            }
        } finally {
            isLoading = false
            isRefreshing = false
        }
    }

    suspend fun loadCategories(forceRefresh: Boolean = false) {
        try {
            categories = repository.getCategories(forceRefresh = forceRefresh)
        } catch (_: Exception) {
        }
    }

    LaunchedEffect(Unit) {
        loadCategories(forceRefresh = true)
    }

    LaunchedEffect(selectedCategoryId) {
        loadFeed(showLoader = true)
    }

    val chipItems = remember(categories) {
        val list = mutableListOf(CategoryChipItem(0L, "ताज़ा (Latest)"))
        categories.forEach {
            list.add(CategoryChipItem(it.id, it.name))
        }
        list
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Editorial Masthead Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Logo()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search Action Button
                    Surface(
                        shape = CircleShape,
                        color = ChaiTheme.extended.surfaceSecondary.copy(alpha = 0.8f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            ChaiTheme.extended.border.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateToSearch,
                            modifier = Modifier.testTag("header-search-button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // Refresh Button with Spin Effect
                    Surface(
                        shape = CircleShape,
                        color = ChaiTheme.extended.surfaceSecondary.copy(alpha = 0.8f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            ChaiTheme.extended.border.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(
                            onClick = {
                                refreshRotation += 360f
                                coroutineScope.launch {
                                    isRefreshing = true
                                    loadCategories(forceRefresh = true)
                                    loadFeed(showLoader = false)
                                }
                            },
                            modifier = Modifier.testTag("header-refresh-button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .size(19.dp)
                                    .rotate(animatedRotation)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = ChaiTheme.extended.border.copy(alpha = 0.5f))

        // Animated Breaking News Ticker (Live Headline updates)
        if (posts.isNotEmpty()) {
            BreakingNewsTicker(
                posts = posts.take(6),
                onPostClick = onNavigateToArticle
            )
            HorizontalDivider(thickness = 0.5.dp, color = ChaiTheme.extended.border.copy(alpha = 0.5f))
        }

        // Offline notice
        OfflineBanner(visible = isOffline)

        // Categories Chips Row
        CategoryChips(
            chips = chipItems,
            selectedId = selectedCategoryId,
            onSelect = { selectedCategoryId = it },
            isLoading = categories.isEmpty() && isLoading
        )

        HorizontalDivider(thickness = 0.5.dp, color = ChaiTheme.extended.border.copy(alpha = 0.5f))

        // Main Feed with Pull to Refresh
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    refreshRotation += 360f
                    isRefreshing = true
                    coroutineScope.launch {
                        loadCategories(forceRefresh = true)
                        loadFeed(showLoader = false)
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                androidx.compose.animation.Crossfade(
                    targetState = isLoading,
                    animationSpec = tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                    label = "feed_loading_crossfade"
                ) { loading ->
                    if (loading) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            userScrollEnabled = false
                        ) {
                            item { HeroSkeleton() }
                            items(4) { NewsCardSkeleton() }
                        }
                    } else if (posts.isEmpty()) {
                        EmptyState(
                            title = "No stories found",
                            message = if (isOffline) "Please check your internet connection." else "No articles published in this category yet.",
                            actionLabel = "Retry",
                            onAction = {
                                coroutineScope.launch { loadFeed(showLoader = true) }
                            }
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // 1. Hero Item (Lead Story)
                            item(key = "hero_${posts.first().id}") {
                                val heroPost = posts.first()
                                HeroCard(
                                    post = heroPost,
                                    onClick = { onNavigateToArticle(heroPost.id) },
                                    modifier = Modifier.animateItem()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            // 2. Section Header
                            item(key = "section_header_$selectedCategoryId") {
                                val sectionTitle = if (selectedCategoryId == 0L) "ताज़ा समाचार • LATEST STORIES" else {
                                    categories.find { it.id == selectedCategoryId }?.name?.uppercase() ?: "STORIES"
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .animateItem(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .width(3.5.dp)
                                                .height(16.dp)
                                                .background(ChaiSaffron, RoundedCornerShape(2.dp))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = sectionTitle,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = InterFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    Text(
                                        text = "${posts.size} लेख",
                                        color = ChaiTheme.extended.muted,
                                        fontFamily = InterFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }

                            // 3. Editorial Flow: Immediate access to news feed
                            if (selectedCategoryId == 0L && posts.size >= 3) {
                                // First 2 standard news feed items directly beneath the header
                                val topStories = posts.subList(1, minOf(3, posts.size))
                                items(topStories, key = { it.id }) { post ->
                                    NewsCard(
                                        post = post,
                                        onClick = { onNavigateToArticle(post.id) },
                                        modifier = Modifier.animateItem()
                                    )
                                }

                                // Mid-feed signature feature: ☕ आज की चाय (Today's Tea Digest)
                                item(key = "chai_digest") {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    TodayChaiDigest(
                                        posts = posts.take(5),
                                        onPostClick = onNavigateToArticle,
                                        modifier = Modifier.animateItem()
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                // Remaining Feed Stories
                                val remainingStories = posts.drop(3)
                                items(remainingStories, key = { it.id }) { post ->
                                    NewsCard(
                                        post = post,
                                        onClick = { onNavigateToArticle(post.id) },
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            } else {
                                // Category view or short list
                                items(posts.drop(1), key = { it.id }) { post ->
                                    NewsCard(
                                        post = post,
                                        onClick = { onNavigateToArticle(post.id) },
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            }

                            item(key = "feed_bottom_spacer") {
                                Spacer(modifier = Modifier.height(36.dp))
                            }
                        }
                    }
                }
            }

            // New stories floating pill
            NewStoriesPill(
                count = newStoriesCount,
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                        loadFeed(showLoader = false)
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }
    }
}
