package com.chaipanchayat.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
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
fun MainAppNavigation() {
    val rootNavController = rememberNavController()

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
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                tabs.forEach { item ->
                    val isSelected = selectedTab == item.tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = item.tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.tab.title
                            )
                        },
                        label = {
                            Text(
                                text = item.tab.title,
                                fontFamily = InterFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ChaiSaffron,
                            selectedTextColor = ChaiSaffron,
                            indicatorColor = ChaiTheme.extended.brandTertiary,
                            unselectedIconColor = ChaiTheme.extended.muted,
                            unselectedTextColor = ChaiTheme.extended.muted
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
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
