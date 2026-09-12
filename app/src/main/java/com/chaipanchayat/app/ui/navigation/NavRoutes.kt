package com.chaipanchayat.app.ui.navigation

object NavRoutes {
    const val MAIN = "main"
    const val SEARCH = "search"
    const val ARTICLE = "article/{id}"
    const val CATEGORY_FEED = "category/{id}/{name}"

    fun articleRoute(id: Long) = "article/$id"
    fun categoryFeedRoute(id: Long, name: String) = "category/$id/${java.net.URLEncoder.encode(name, "UTF-8")}"
}

enum class MainTab(val title: String) {
    HOME("Home"),
    VIDEOS("Videos"),
    CATEGORIES("Categories"),
    SAVED("Saved"),
    SETTINGS("Settings")
}
