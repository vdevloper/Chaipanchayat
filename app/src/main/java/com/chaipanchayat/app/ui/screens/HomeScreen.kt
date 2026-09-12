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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.data.api.WordPressApiClient
import com.chaipanchayat.app.data.model.WPCategory
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.data.repository.NewsRepository
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
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
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
        val list = mutableListOf(CategoryChipItem(0L, "Latest"))
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
        // App Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Logo()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.testTag("header-search-button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = {
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
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = ChaiTheme.extended.border)

        // Offline notice
        OfflineBanner(visible = isOffline)

        // Categories Chips Row
        CategoryChips(
            chips = chipItems,
            selectedId = selectedCategoryId,
            onSelect = { selectedCategoryId = it },
            isLoading = categories.isEmpty() && isLoading
        )

        HorizontalDivider(thickness = 0.5.dp, color = ChaiTheme.extended.border)

        // Main Feed with Pull to Refresh
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
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
                        // Hero Item (First Post)
                        item {
                            val heroPost = posts.first()
                            HeroCard(
                                post = heroPost,
                                onClick = { onNavigateToArticle(heroPost.id) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Section Header
                        item {
                            val sectionTitle = if (selectedCategoryId == 0L) "LATEST STORIES" else {
                                categories.find { it.id == selectedCategoryId }?.name?.uppercase() ?: "STORIES"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(14.dp)
                                        .background(ChaiSaffron, RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sectionTitle,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = InterFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
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
                            Spacer(modifier = Modifier.height(24.dp))
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
