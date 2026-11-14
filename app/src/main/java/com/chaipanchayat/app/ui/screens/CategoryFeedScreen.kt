package com.chaipanchayat.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.data.api.TranslationService
import com.chaipanchayat.app.data.model.AppLanguage
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.data.repository.NewsRepository
import com.chaipanchayat.app.data.repository.SettingsRepository
import com.chaipanchayat.app.engine.liquidglass.LiquidGlassBackdrop
import com.chaipanchayat.app.engine.liquidglass.LiquidGlassEngine
import com.chaipanchayat.app.engine.liquidglass.liquidGlass
import com.chaipanchayat.app.ui.components.ChaiBrewLoadingScreen
import com.chaipanchayat.app.ui.components.EmptyState
import com.chaipanchayat.app.ui.components.HeroCard
import com.chaipanchayat.app.ui.components.HeroSkeleton
import com.chaipanchayat.app.ui.components.NewsCard
import com.chaipanchayat.app.ui.components.NewsCardSkeleton
import com.chaipanchayat.app.ui.components.TopBar
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.kyant.backdrop.Capsule
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { NewsRepository.getInstance() }
    val settingsRepo = remember { SettingsRepository.getInstance(context) }
    val currentAppLang by settingsRepo.appLanguage.collectAsState()
    val showOriginalHindi by settingsRepo.showOriginalHindi.collectAsState()
    var translatedTitles by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }

    val initialCached = remember(categoryId) { repository.getCachedPosts(categoryId).orEmpty() }
    var posts by remember { mutableStateOf(initialCached) }
    var isLoading by remember { mutableStateOf(initialCached.isEmpty()) }
    var isRefreshing by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(posts, currentAppLang, showOriginalHindi) {
        if (currentAppLang != AppLanguage.HINDI && !showOriginalHindi && posts.isNotEmpty()) {
            try {
                translatedTitles = TranslationService.translatePostsTitles(posts, currentAppLang)
            } catch (_: Exception) {
            }
        } else {
            translatedTitles = emptyMap()
        }
    }

    fun getHeadline(post: WPPost): String {
        return if (showOriginalHindi) post.cleanTitle else (translatedTitles[post.id] ?: post.cleanTitle)
    }

    suspend fun loadPosts(showLoader: Boolean = true) {
        val cached = repository.getCachedPosts(categoryId)
        if (!cached.isNullOrEmpty()) {
            posts = cached
            isLoading = false
        } else if (showLoader && posts.isEmpty()) {
            isLoading = true
        }
        try {
            val isTagFeed = categoryId < 0L
            posts = if (isTagFeed) {
                repository.getPostsByTag(tagId = -categoryId, forceRefresh = isRefreshing)
            } else {
                repository.getPosts(categoryId = categoryId, forceRefresh = isRefreshing)
            }
        } catch (_: Exception) {
        } finally {
            isLoading = false
            isRefreshing = false
        }
    }

    LaunchedEffect(categoryId) {
        loadPosts(showLoader = true)
    }

    val isLiquid = ChaiTheme.extended.isLiquidGlass
    val feedBackdrop = rememberLayerBackdrop()
    var topBarHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val topBarHeightDp = with(density) { topBarHeightPx.toDp() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isLiquid) Color.Transparent else MaterialTheme.colorScheme.surface)
    ) {
        // Layer 1: Recorded Feed Backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isLiquid) Modifier.layerBackdrop(feedBackdrop) else Modifier)
        ) {
            if (isLiquid) {
                LiquidGlassBackdrop(settings = LiquidGlassEngine.current)
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    coroutineScope.launch { loadPosts(showLoader = false) }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    isLoading && posts.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize().padding(top = topBarHeightDp)) {
                            ChaiBrewLoadingScreen()
                        }
                    }

                    isLoading -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = topBarHeightDp),
                            userScrollEnabled = false
                        ) {
                            item { HeroSkeleton() }
                            items(4) { NewsCardSkeleton() }
                        }
                    }

                    posts.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize().padding(top = topBarHeightDp)) {
                            EmptyState(
                                title = "कोई लेख उपलब्ध नहीं",
                                message = "$categoryName श्रेणी में अभी कोई समाचार प्रकाशित नहीं हुआ है।",
                                actionLabel = "पुनः प्रयास करें",
                                onAction = {
                                    coroutineScope.launch { loadPosts(showLoader = true) }
                                }
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = topBarHeightDp)
                        ) {
                            // Category Editorial Banner
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(22.dp)
                                                .background(ChaiSaffron, RoundedCornerShape(2.dp))
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = categoryName,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = NotoSerifFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 22.sp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        if (isLiquid) {
                                            Box(
                                                modifier = Modifier
                                                    .liquidGlass(
                                                        shape = Capsule(),
                                                        blurRadiusDp = 16.dp
                                                    )
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "${posts.size} समाचार",
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontFamily = InterFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.5.sp
                                                )
                                            }
                                        }
                                    }
                                    if (!isLiquid) {
                                        Text(
                                            text = "${posts.size} समाचार • चाय पंचायत विशेष कवरेज",
                                            color = ChaiTheme.extended.muted,
                                            fontFamily = InterFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(start = 14.dp, top = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Lead Story in Category
                            item(key = "cat_hero_${posts.first().id}") {
                                val hero = posts.first()
                                HeroCard(
                                    post = hero,
                                    onClick = { onNavigateToArticle(hero.id) },
                                    displayTitle = getHeadline(hero)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            // Remaining Category Stories
                            items(posts.drop(1), key = { it.id }) { post ->
                                NewsCard(
                                    post = post,
                                    onClick = { onNavigateToArticle(post.id) },
                                    displayTitle = getHeadline(post)
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(36.dp))
                            }
                        }
                    }
                }
            }
        }

        // Layer 2: Floating Refractive Glass TopBar
        TopBar(
            title = categoryName,
            onBack = onBack,
            backdrop = if (isLiquid) feedBackdrop else null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { topBarHeightPx = it.height }
        )
    }
}
