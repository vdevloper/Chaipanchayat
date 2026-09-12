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

    suspend fun loadCategories() {
        try {
            categories = repository.getCategories()
        } catch (_: Exception) {
        }
    }

    LaunchedEffect(Unit) {
        loadCategories()
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
        // Subtle top gradient ribbon (Brand Accent)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(ChaiBrandGradient)
        )

        // Editorial App Header
        val isLiquid = ChaiTheme.extended.isLiquidGlass

        Surface(
            color = if (isLiquid) com.chaipanchayat.app.ui.theme.LiquidGlassSurface else MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Logo()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Search Action Button
                    Surface(
                        shape = CircleShape,
                        color = ChaiTheme.extended.surfaceSecondary,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isLiquid) Color(0x4038BDF8) else ChaiTheme.extended.border.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateToSearch,
                            modifier = Modifier.testTag("header-search-button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Refresh Button with Spin Effect
                    Surface(
                        shape = CircleShape,
                        color = ChaiTheme.extended.surfaceSecondary,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isLiquid) Color(0x4038BDF8) else ChaiTheme.extended.border.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(
                            onClick = {
                                refreshRotation += 360f
                                coroutineScope.launch {
                                    isRefreshing = true
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
                                    .size(20.dp)
                                    .rotate(animatedRotation)
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.8.dp)
                .background(
                    if (isLiquid) ChaiTheme.extended.glassBorderBrush
                    else androidx.compose.ui.graphics.SolidColor(ChaiTheme.extended.border.copy(alpha = 0.7f))
                )
        )

        // Animated Breaking News Ticker (Live Headline updates)
        if (posts.isNotEmpty()) {
            BreakingNewsTicker(
                posts = posts.take(6),
                onPostClick = onNavigateToArticle
            )
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

        HorizontalDivider(thickness = 0.8.dp, color = ChaiTheme.extended.border.copy(alpha = 0.7f))

        // Main Feed with Pull to Refresh
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    refreshRotation += 360f
                    isRefreshing = true
                    coroutineScope.launch {
                        loadFeed(showLoader = false)
                    }
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
                        // Hero Item (Featured Post)
                        item {
                            val heroPost = posts.first()
                            HeroCard(
                                post = heroPost,
                                onClick = { onNavigateToArticle(heroPost.id) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // Signature Chai Panchayat Feature: ☕ आज की चाय (Section 08)
                        if (selectedCategoryId == 0L && posts.size >= 3) {
                            item {
                                TodayChaiDigest(
                                    posts = posts.drop(1).take(5),
                                    onPostClick = onNavigateToArticle
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }

                        // Section Header with stylized badge
                        item {
                            val sectionTitle = if (selectedCategoryId == 0L) "ताज़ा खबरें • LATEST STORIES" else {
                                categories.find { it.id == selectedCategoryId }?.name?.uppercase() ?: "STORIES"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(18.dp)
                                            .background(ChaiBrandGradient, RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = sectionTitle,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontFamily = InterFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        letterSpacing = 0.8.sp
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

                        // Remaining Posts
                        itemsIndexed(posts.drop(1), key = { _, post -> post.id }) { _, post ->
                            NewsCard(
                                post = post,
                                onClick = { onNavigateToArticle(post.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(36.dp))
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
