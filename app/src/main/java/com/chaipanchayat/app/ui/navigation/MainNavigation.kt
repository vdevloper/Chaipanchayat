package com.chaipanchayat.app.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chaipanchayat.app.ui.screens.ArticleScreen
import com.chaipanchayat.app.ui.screens.CategoriesScreen
import com.chaipanchayat.app.ui.screens.CategoryFeedScreen
import com.chaipanchayat.app.ui.screens.HomeScreen
import com.chaipanchayat.app.ui.screens.SavedScreen
import com.chaipanchayat.app.ui.screens.SearchScreen
import com.chaipanchayat.app.ui.screens.SettingsScreen
import com.chaipanchayat.app.ui.screens.VideosScreen
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily

private data class TabBarItem(
    val tab: MainTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun MainAppNavigation(
    initialArticleId: Long? = null
) {
    val rootNavController = rememberNavController()

    androidx.compose.runtime.LaunchedEffect(initialArticleId) {
        if (initialArticleId != null && initialArticleId > 0L) {
            rootNavController.navigate(NavRoutes.articleRoute(initialArticleId))
        }
    }

    NavHost(
        navController = rootNavController,
        startDestination = NavRoutes.MAIN,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(NavRoutes.MAIN) {
            MainTabsScaffold(
                onNavigateToArticle = { id ->
                    rootNavController.navigate(NavRoutes.articleRoute(id))
                },
                onNavigateToCategory = { id, name ->
                    rootNavController.navigate(NavRoutes.categoryFeedRoute(id, name))
                },
                onNavigateToSearch = {
                    rootNavController.navigate(NavRoutes.SEARCH)
                }
            )
        }

        composable(NavRoutes.SEARCH) {
            SearchScreen(
                onNavigateToArticle = { id ->
                    rootNavController.navigate(NavRoutes.articleRoute(id))
                },
                onBack = { rootNavController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.ARTICLE,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getLong("id") ?: 0L
            ArticleScreen(
                postId = postId,
                onBack = { rootNavController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.CATEGORY_FEED,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("id") ?: 0L
            val rawName = backStackEntry.arguments?.getString("name").orEmpty()
            val categoryName = java.net.URLDecoder.decode(rawName, "UTF-8")
            CategoryFeedScreen(
                categoryId = categoryId,
                categoryName = categoryName,
                onNavigateToArticle = { id ->
                    rootNavController.navigate(NavRoutes.articleRoute(id))
                },
                onBack = { rootNavController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainTabsScaffold(
    onNavigateToArticle: (Long) -> Unit,
    onNavigateToCategory: (Long, String) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }

    val tabs = listOf(
        TabBarItem(MainTab.HOME, Icons.Filled.Home, Icons.Outlined.Home, "tab-home"),
        TabBarItem(MainTab.VIDEOS, Icons.Filled.PlayCircle, Icons.Outlined.PlayCircleOutline, "tab-videos"),
        TabBarItem(MainTab.CATEGORIES, Icons.Filled.GridView, Icons.Outlined.GridView, "tab-categories"),
        TabBarItem(MainTab.SAVED, Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder, "tab-saved"),
        TabBarItem(MainTab.SETTINGS, Icons.Filled.Settings, Icons.Outlined.Settings, "tab-settings")
    )

    Scaffold(
        bottomBar = {
            val isLiquid = ChaiTheme.extended.isLiquidGlass
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            if (isLiquid) ChaiTheme.extended.glassBorderBrush
                            else androidx.compose.ui.graphics.SolidColor(ChaiTheme.extended.border.copy(alpha = 0.7f))
                        )
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    tabs.forEach { item ->
                        val isSelected = selectedTab == item.tab
                        val activeColor = ChaiSaffron
                        val indicatorBg = ChaiSaffron.copy(alpha = 0.12f)

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = item.tab },
                            icon = {
                                Box {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.tab.title,
                                        modifier = Modifier.size(23.dp)
                                    )
                                    // Live red badge indicator on VIDEOS tab
                                    if (item.tab == MainTab.VIDEOS) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(ChaiCrimson, CircleShape)
                                                .align(Alignment.TopEnd)
                                                .offset(x = 3.dp, y = (-2).dp)
                                        )
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = item.tab.title,
                                    fontFamily = InterFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 10.5.sp,
                                    letterSpacing = 0.2.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = activeColor,
                                selectedTextColor = activeColor,
                                indicatorColor = indicatorBg,
                                unselectedIconColor = ChaiTheme.extended.muted,
                                unselectedTextColor = ChaiTheme.extended.muted
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220))) togetherWith
                        (fadeOut(animationSpec = tween(200)))
            },
            label = "tab_content_transition"
        ) { targetTab ->
            when (targetTab) {
                MainTab.HOME -> HomeScreen(
                    onNavigateToArticle = onNavigateToArticle,
                    onNavigateToSearch = onNavigateToSearch,
                    modifier = Modifier.padding(innerPadding)
                )

                MainTab.VIDEOS -> VideosScreen(
                    onNavigateToArticle = onNavigateToArticle,
                    modifier = Modifier.padding(innerPadding)
                )

                MainTab.CATEGORIES -> CategoriesScreen(
                    onNavigateToCategory = onNavigateToCategory,
                    modifier = Modifier.padding(innerPadding)
                )

                MainTab.SAVED -> SavedScreen(
                    onNavigateToArticle = onNavigateToArticle,
                    modifier = Modifier.padding(innerPadding)
                )

                MainTab.SETTINGS -> SettingsScreen(
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
