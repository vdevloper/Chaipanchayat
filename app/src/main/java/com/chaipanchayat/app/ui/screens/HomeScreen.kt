package com.chaipanchayat.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Language
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.data.api.TranslationService
import com.chaipanchayat.app.data.model.AppLanguage
import com.chaipanchayat.app.data.model.VideoItem
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.data.repository.NewsRepository
import com.chaipanchayat.app.data.repository.SettingsRepository
import com.chaipanchayat.app.engine.liquidglass.ChaiGlassHeader
import com.chaipanchayat.app.engine.liquidglass.LiquidGlassBackdrop
import com.chaipanchayat.app.engine.liquidglass.LiquidGlassEngine
import com.chaipanchayat.app.engine.liquidglass.liquidGlass
import com.chaipanchayat.app.ui.components.BreakingNewsTicker
import com.chaipanchayat.app.ui.components.CategoryChipItem
import com.chaipanchayat.app.ui.components.CategoryChips
import com.chaipanchayat.app.ui.components.ChaiBrewLoadingScreen
import com.chaipanchayat.app.ui.components.EmptyState
import com.chaipanchayat.app.ui.components.HeroCarousel
import com.chaipanchayat.app.ui.components.HeroSkeleton
import com.chaipanchayat.app.ui.components.HomeVideoStrip
import com.chaipanchayat.app.ui.components.LanguagePickerSheet
import com.chaipanchayat.app.ui.components.Logo
import com.chaipanchayat.app.ui.components.NewStoriesPill
import com.chaipanchayat.app.ui.components.NewsCard
import com.chaipanchayat.app.ui.components.NewsCardSkeleton
import com.chaipanchayat.app.ui.components.OfflineBanner
import com.chaipanchayat.app.ui.components.TodayChaiDigest
import com.chaipanchayat.app.ui.components.chaiFeedItemReveal
import com.chaipanchayat.app.ui.components.chaiSharedBounds
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.LocalChaiStrings
import com.chaipanchayat.app.utils.ChaiAudioReader
import com.chaipanchayat.app.utils.rememberChaiHaptics
import com.kyant.backdrop.Capsule
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToArticle: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptics = rememberChaiHaptics()
    val repository = remember { NewsRepository.getInstance() }
    val settingsRepo = remember { SettingsRepository.getInstance(context) }
    val currentAppLang by settingsRepo.appLanguage.collectAsState()
    val showOriginalHindi by settingsRepo.showOriginalHindi.collectAsState()
    val strings = LocalChaiStrings.current
    val isHindiUi = currentAppLang == AppLanguage.HINDI || showOriginalHindi
    var showLanguagePicker by remember { mutableStateOf(false) }

    var translatedTitles by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
    var isTranslatingFeed by remember { mutableStateOf(false) }

    val initialCachedPosts = remember { repository.getCachedPosts(null).orEmpty() }
    val initialCachedCats = remember { repository.getCachedCategories().orEmpty() }
    val initialCachedVideos = remember { repository.getCachedVideos().orEmpty() }

    var posts by remember { mutableStateOf(initialCachedPosts) }
    var categories by remember { mutableStateOf(initialCachedCats) }
    var homeVideos by remember { mutableStateOf<List<VideoItem>>(initialCachedVideos) }
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

    // Track scroll direction to smoothly condense/expand the Breaking News ticker
    var isTickerVisible by remember { mutableStateOf(true) }
    LaunchedEffect(listState) {
        var prevIndex = 0
        var prevOffset = 0
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                if (index == 0 && offset < 80) {
                    isTickerVisible = true
                } else {
                    val delta = (index - prevIndex) * 1000 + (offset - prevOffset)
                    if (delta > 36) {
                        isTickerVisible = false
                    } else if (delta < -28) {
                        isTickerVisible = true
                    }
                }
                prevIndex = index
                prevOffset = offset
            }
    }

    // Smooth reading progress along the feed header bottom edge
    val rawScrollProgress by remember {
        derivedStateOf {
            val total = listState.layoutInfo.totalItemsCount
            if (total <= 2) {
                0f
            } else {
                val visibleFraction = (listState.firstVisibleItemIndex.toFloat() +
                    (listState.firstVisibleItemScrollOffset / 520f).coerceIn(0f, 1f)) / (total - 1).coerceAtLeast(1)
                visibleFraction.coerceIn(0f, 1f)
            }
        }
    }
    val animatedScrollProgress by animateFloatAsState(
        targetValue = rawScrollProgress,
        animationSpec = spring(stiffness = 480f),
        label = "home_scroll_progress"
    )

    // Subtle vertical parallax on top lead carousel when near top
    val heroParallaxPx by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else {
                0f
            }
        }
    }

    // Floating Back-to-Top button visibility
    val showBackToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex >= 3 }
    }

    // Auto-translate feed headlines when global language is not Hindi
    LaunchedEffect(posts, currentAppLang, showOriginalHindi) {
        if (currentAppLang != AppLanguage.HINDI && !showOriginalHindi && posts.isNotEmpty()) {
            isTranslatingFeed = true
            try {
                val batch = TranslationService.translatePostsTitles(posts, currentAppLang)
                translatedTitles = batch
            } catch (_: Exception) {
            } finally {
                isTranslatingFeed = false
            }
        } else {
            translatedTitles = emptyMap()
            isTranslatingFeed = false
        }
    }

    fun getHeadline(post: WPPost): String {
        return if (showOriginalHindi) post.cleanTitle else (translatedTitles[post.id] ?: post.cleanTitle)
    }

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
            fetchedPosts.firstOrNull()?.let { topPost ->
                ChaiAudioReader.getInstance(context).seedFeaturedArticle(
                    postId = topPost.id,
                    title = topPost.cleanTitle,
                    contentHtml = topPost.rawContent,
                    imageUrl = topPost.featuredImageUrl,
                    category = topPost.primaryCategory ?: "ताज़ा ख़बरें"
                )
            }
            isOffline = false
            newStoriesCount = 0
        } catch (_: Exception) {
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

    suspend fun loadHomeVideos(forceRefresh: Boolean = false) {
        try {
            homeVideos = repository.getVideoPosts(forceRefresh = forceRefresh)
        } catch (_: Exception) {
        }
    }

    LaunchedEffect(Unit) {
        loadCategories(forceRefresh = true)
        loadHomeVideos(forceRefresh = false)
    }

    LaunchedEffect(selectedCategoryId) {
        loadFeed(showLoader = true)
    }

    val chipItems = remember(categories, isHindiUi) {
        val allLabel = if (isHindiUi) "सभी" else "All"
        val list = mutableListOf(CategoryChipItem(0L, allLabel))
        categories
            .filter { cat ->
                val name = cat.name.trim().lowercase()
                val slug = cat.slug.trim().lowercase()
                !name.contains("latest") && !slug.contains("latest") && !name.contains("ताज़ा")
            }
            .forEach {
                list.add(CategoryChipItem(it.id, it.name))
            }
        list
    }

    val isLiquid = ChaiTheme.extended.isLiquidGlass
    val isLightGlass = ChaiTheme.extended.isLightGlass
    val homeFeedBackdrop = rememberLayerBackdrop()
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isLiquid) Color.Transparent else MaterialTheme.colorScheme.surface)
    ) {
        // Layer 1: Scrollable News Feed recorded into homeFeedBackdrop so top floating header refracts it!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isLiquid) Modifier.layerBackdrop(homeFeedBackdrop) else Modifier)
        ) {
            if (isLiquid) {
                LiquidGlassBackdrop(settings = LiquidGlassEngine.current)
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    refreshRotation += 360f
                    isRefreshing = true
                    coroutineScope.launch {
                        loadCategories(forceRefresh = true)
                        loadHomeVideos(forceRefresh = true)
                        loadFeed(showLoader = false)
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                androidx.compose.animation.Crossfade(
                    targetState = isLoading,
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    label = "feed_loading_crossfade"
                ) { loading ->
                    if (loading && posts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = headerHeightDp)
                        ) {
                            ChaiBrewLoadingScreen()
                        }
                    } else if (loading) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = headerHeightDp),
                            userScrollEnabled = false
                        ) {
                            item { HeroSkeleton() }
                            items(4) { NewsCardSkeleton() }
                        }
                    } else if (posts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = headerHeightDp)
                        ) {
                            EmptyState(
                                title = "No stories found",
                                message = if (isOffline) "Please check your internet connection." else "No articles published in this category yet.",
                                actionLabel = "Retry",
                                onAction = {
                                    coroutineScope.launch { loadFeed(showLoader = true) }
                                }
                            )
                        }
                    } else {
                        val leadCount = if (posts.size >= 4) 4 else 1
                        val leadPosts = posts.take(leadCount)
                        val remainingPosts = posts.drop(leadCount)
                        val firstBatch = remainingPosts.take(3)
                        val middleBatch = remainingPosts.drop(3).take(4)
                        val tailBatch = remainingPosts.drop(7)

                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(top = headerHeightDp + 4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // 1. Multi-Story Lead Briefing Carousel (Top 4 Lead Stories with depth & parallax)
                            item(key = "hero_carousel_${selectedCategoryId}_${leadPosts.first().id}") {
                                HeroCarousel(
                                    leadPosts = leadPosts,
                                    getHeadline = ::getHeadline,
                                    onNavigateToArticle = onNavigateToArticle,
                                    parallaxOffsetPx = heroParallaxPx,
                                    isHindiUi = isHindiUi,
                                    modifier = Modifier.animateItem()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            // 2. Clean Localized Section Sub-Header
                            item(key = "section_header_$selectedCategoryId") {
                                val sectionTitle = if (selectedCategoryId == 0L) {
                                    if (isHindiUi) "मुख्य समाचार" else "TOP STORIES"
                                } else {
                                    categories.find { it.id == selectedCategoryId }?.name?.uppercase() ?: "STORIES"
                                }
                                val countLabel = if (isHindiUi) "${posts.size} लेख" else "${posts.size} stories"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                        .animateItem(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(19.dp)
                                                .background(ChaiSaffron, RoundedCornerShape(2.dp))
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = sectionTitle,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = InterFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    if (isLiquid) {
                                        Box(
                                            modifier = Modifier
                                                .liquidGlass(
                                                    shape = Capsule(),
                                                    blurRadiusDp = 16.dp
                                                )
                                                .padding(horizontal = 10.dp, vertical = 3.5.dp)
                                        ) {
                                            Text(
                                                text = countLabel,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontFamily = InterFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = countLabel,
                                            color = ChaiTheme.extended.muted,
                                            fontFamily = InterFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            // 3. First Editorial Batch with smooth entrance reveal
                            itemsIndexed(firstBatch, key = { _, post -> "b1_${post.id}" }) { index, post ->
                                NewsCard(
                                    post = post,
                                    onClick = { onNavigateToArticle(post.id) },
                                    displayTitle = getHeadline(post),
                                    modifier = Modifier
                                        .animateItem()
                                        .chaiFeedItemReveal(staggerIndex = index)
                                )
                            }

                            // 4. Mid-Feed Horizontal Video Bulletin Strip (when videos are loaded)
                            if (selectedCategoryId == 0L && homeVideos.isNotEmpty()) {
                                item(key = "home_video_strip") {
                                    HomeVideoStrip(
                                        videos = homeVideos,
                                        onVideoClick = onNavigateToArticle,
                                        isHindiUi = isHindiUi,
                                        modifier = Modifier
                                            .animateItem()
                                            .chaiFeedItemReveal(staggerIndex = 1)
                                    )
                                }
                            }

                            // 5. Middle Story Batch
                            itemsIndexed(middleBatch, key = { _, post -> "b2_${post.id}" }) { index, post ->
                                NewsCard(
                                    post = post,
                                    onClick = { onNavigateToArticle(post.id) },
                                    displayTitle = getHeadline(post),
                                    modifier = Modifier
                                        .animateItem()
                                        .chaiFeedItemReveal(staggerIndex = index)
                                )
                            }

                            // 6. Signature "☕ आज की चाय" 2-Minute Briefing Digest
                            if (selectedCategoryId == 0L && posts.size >= 6) {
                                item(key = "today_chai_digest") {
                                    TodayChaiDigest(
                                        posts = posts.take(5),
                                        onPostClick = onNavigateToArticle,
                                        translatedTitles = if (showOriginalHindi) emptyMap() else translatedTitles,
                                        modifier = Modifier
                                            .animateItem()
                                            .chaiFeedItemReveal(staggerIndex = 0)
                                    )
                                }
                            }

                            // 7. Remaining Feed Stories
                            itemsIndexed(tailBatch, key = { _, post -> "b3_${post.id}" }) { index, post ->
                                NewsCard(
                                    post = post,
                                    onClick = { onNavigateToArticle(post.id) },
                                    displayTitle = getHeadline(post),
                                    modifier = Modifier
                                        .animateItem()
                                        .chaiFeedItemReveal(staggerIndex = index.coerceAtMost(3))
                                )
                            }

                            item(key = "feed_bottom_spacer") {
                                Spacer(modifier = Modifier.height(if (isLiquid) 172.dp else 44.dp))
                            }
                        }
                    }
                }
            }
        }

        // Layer 2: Floating Refractive Collapsible Top Header + Scroll Progress Hairline
        ChaiGlassHeader(
            backdrop = homeFeedBackdrop,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged { size -> headerHeightPx = size.height }
        ) {
            // Editorial Masthead Header with generous spacing around Logo & Verified badge
            Surface(
                color = if (isLiquid) Color.Transparent else MaterialTheme.colorScheme.surface,
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

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        val headerCapsule = remember { Capsule() }

                        // Compact Capsule Language Switcher Pill (🇮🇳 HI ▾) for generous header breathing room
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isLiquid) Color.Transparent else ChaiTheme.extended.surfaceSecondary.copy(alpha = 0.9f),
                            border = if (isLiquid) null else BorderStroke(
                                1.dp,
                                ChaiTheme.extended.border.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .height(36.dp)
                                .then(
                                    if (isLiquid) {
                                        Modifier.liquidGlass(
                                            shape = headerCapsule,
                                            blurRadiusDp = 18.dp,
                                            backdrop = homeFeedBackdrop
                                        )
                                    } else Modifier
                                )
                                .clickable {
                                    haptics.click()
                                    showLanguagePicker = true
                                }
                                .testTag("header-language-button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = currentAppLang.flag,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = currentAppLang.code.uppercase(),
                                    fontFamily = InterFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.4.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Change Language",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        // Search Action Button with Shared Element Bounds
                        Surface(
                            shape = CircleShape,
                            color = if (isLiquid) Color.Transparent else ChaiTheme.extended.surfaceSecondary.copy(alpha = 0.8f),
                            border = if (isLiquid) null else BorderStroke(
                                1.dp,
                                ChaiTheme.extended.border.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .size(38.dp)
                                .chaiSharedBounds("search_bar")
                                .then(
                                    if (isLiquid) {
                                        Modifier.liquidGlass(
                                            shape = CircleShape,
                                            blurRadiusDp = 18.dp,
                                            backdrop = homeFeedBackdrop
                                        )
                                    } else Modifier
                                )
                        ) {
                            IconButton(
                                onClick = {
                                    haptics.click()
                                    onNavigateToSearch()
                                },
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
                            color = if (isLiquid) Color.Transparent else ChaiTheme.extended.surfaceSecondary.copy(alpha = 0.8f),
                            border = if (isLiquid) null else BorderStroke(
                                1.dp,
                                ChaiTheme.extended.border.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .size(38.dp)
                                .then(
                                    if (isLiquid) {
                                        Modifier.liquidGlass(
                                            shape = CircleShape,
                                            blurRadiusDp = 18.dp,
                                            backdrop = homeFeedBackdrop
                                        )
                                    } else Modifier
                                )
                        ) {
                            IconButton(
                                onClick = {
                                    haptics.click()
                                    refreshRotation += 360f
                                    coroutineScope.launch {
                                        isRefreshing = true
                                        loadCategories(forceRefresh = true)
                                        loadHomeVideos(forceRefresh = true)
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

            HorizontalDivider(thickness = 0.5.dp, color = ChaiTheme.extended.border.copy(alpha = if (isLiquid) 0.25f else 0.5f))

            // Collapsible Animated Breaking News Ticker (condenses on downward scroll, expands on upward scroll)
            AnimatedVisibility(
                visible = posts.isNotEmpty() && isTickerVisible,
                enter = expandVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) + fadeIn(tween(200)),
                exit = shrinkVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) + fadeOut(tween(150))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    BreakingNewsTicker(
                        posts = posts.take(6),
                        onPostClick = onNavigateToArticle,
                        translatedTitles = if (showOriginalHindi) emptyMap() else translatedTitles
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = ChaiTheme.extended.border.copy(alpha = if (isLiquid) 0.25f else 0.5f))
                }
            }

            // Global Feed Translation Status & 1-Tap Original Hindi Toggle
            if (currentAppLang != AppLanguage.HINDI) {
                Surface(
                    color = if (isLiquid) {
                        if (isLightGlass) Color(0x120F172A) else Color(0x38161618)
                    } else ChaiTheme.extended.surfaceSecondary.copy(alpha = 0.95f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (showOriginalHindi) ChaiCrimson.copy(alpha = 0.35f) else ChaiSaffron.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = if (showOriginalHindi) Icons.Outlined.Language else Icons.Outlined.Translate,
                                contentDescription = null,
                                tint = if (showOriginalHindi) ChaiCrimson else ChaiSaffron,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (showOriginalHindi) {
                                    "🇮🇳 Showing Original Hindi"
                                } else if (isTranslatingFeed) {
                                    "Translating feed to ${currentAppLang.englishName}..."
                                } else {
                                    "Translated to ${currentAppLang.englishName}"
                                },
                                fontFamily = InterFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (showOriginalHindi) ChaiSaffron.copy(alpha = 0.15f) else ChaiCrimson.copy(alpha = 0.12f),
                            border = BorderStroke(
                                1.dp,
                                if (showOriginalHindi) ChaiSaffron.copy(alpha = 0.5f) else ChaiCrimson.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .clickable { settingsRepo.toggleShowOriginalHindi() }
                                .testTag("toggle-original-feed-button")
                        ) {
                            Text(
                                text = if (showOriginalHindi) "🌐 ${strings.translate}" else "🇮🇳 ${strings.original}",
                                fontFamily = InterFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = if (showOriginalHindi) ChaiSaffron else ChaiCrimson,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Offline notice
            OfflineBanner(visible = isOffline)

            // Categories Chips Row
            CategoryChips(
                chips = chipItems,
                selectedId = selectedCategoryId,
                onSelect = { selectedCategoryId = it },
                isLoading = categories.isEmpty() && isLoading,
                backdrop = homeFeedBackdrop
            )

            // Thin Saffron Scroll Progress Hairline along header bottom edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        if (!isLiquid) ChaiTheme.extended.border.copy(alpha = 0.4f) else Color.Transparent
                    )
            ) {
                if (animatedScrollProgress > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedScrollProgress)
                            .height(2.dp)
                            .background(ChaiBrandGradient)
                    )
                }
            }
        }

        // Layer 3: New stories floating pill
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
                .padding(top = headerHeightDp + 16.dp)
        )

        // Layer 4: Floating Back-to-Top Pill when scrolled down
        AnimatedVisibility(
            visible = showBackToTop,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = 0.65f, stiffness = 460f)) + slideInVertically { it / 2 },
            exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.85f) + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (isLiquid) 168.dp else 14.dp)
        ) {
            val capsuleShape = remember { Capsule() }
            Surface(
                shape = CircleShape,
                color = if (isLiquid) Color.Transparent else Color.Transparent,
                shadowElevation = if (isLiquid) 0.dp else 6.dp,
                modifier = Modifier
                    .clip(CircleShape)
                    .then(
                        if (isLiquid) {
                            Modifier.liquidGlass(
                                shape = capsuleShape,
                                blurRadiusDp = 22.dp,
                                backdrop = homeFeedBackdrop,
                                withShadow = true
                            )
                        } else {
                            Modifier.background(
                                brush = Brush.horizontalGradient(
                                    listOf(ChaiSaffron, ChaiCrimson)
                                ),
                                shape = CircleShape
                            )
                        }
                    )
                    .clickable {
                        haptics.click()
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    }
                    .testTag("home-back-to-top-pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Back to top",
                        tint = if (isLiquid) MaterialTheme.colorScheme.onSurface else Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (isHindiUi) "ऊपर जाएं" else "Top",
                        color = if (isLiquid) MaterialTheme.colorScheme.onSurface else Color.White,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (showLanguagePicker) {
            LanguagePickerSheet(
                currentLanguage = currentAppLang,
                onLanguageSelected = { lang ->
                    settingsRepo.setAppLanguage(lang)
                },
                onDismiss = { showLanguagePicker = false }
            )
        }
    }
}
