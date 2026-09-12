package com.chaipanchayat.app.data.repository

import android.util.LruCache
import com.chaipanchayat.app.data.api.WordPressApiClient
import com.chaipanchayat.app.data.model.VideoItem
import com.chaipanchayat.app.data.model.WPCategory
import com.chaipanchayat.app.data.model.WPPost
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NewsRepository private constructor() {

    private val postsMutex = Mutex()
    private val videosMutex = Mutex()
    private val categoriesMutex = Mutex()

    // In-memory caches for instantaneous screen and tab switching
    private var homePostsCache: List<WPPost>? = null
    private val categoryPostsCache = mutableMapOf<Long, List<WPPost>>()
    private var categoriesCache: List<WPCategory>? = null
    private var videoPostsCache: List<VideoItem>? = null
    private val articleLruCache = LruCache<Long, WPPost>(50)

    fun getCachedPosts(categoryId: Long?): List<WPPost>? {
        return if (categoryId == null || categoryId == 0L) {
            homePostsCache
        } else {
            categoryPostsCache[categoryId]
        }
    }

    fun getCachedCategories(): List<WPCategory>? = categoriesCache

    fun getCachedVideos(): List<VideoItem>? = videoPostsCache

    suspend fun getPosts(
        categoryId: Long? = null,
        forceRefresh: Boolean = false
    ): List<WPPost> = postsMutex.withLock {
        val cached = getCachedPosts(categoryId)
        if (!forceRefresh && !cached.isNullOrEmpty()) {
            return@withLock cached
        }

        val catId = if (categoryId != null && categoryId > 0L) categoryId else null
        val freshPosts = WordPressApiClient.fetchPosts(categoryId = catId, perPage = 15)

        if (catId == null) {
            homePostsCache = freshPosts
        } else {
            categoryPostsCache[catId] = freshPosts
        }

        freshPosts
    }

    suspend fun getVideoPosts(forceRefresh: Boolean = false): List<VideoItem> = videosMutex.withLock {
        if (!forceRefresh && !videoPostsCache.isNullOrEmpty()) {
            return@withLock videoPostsCache!!
        }

        val videos = WordPressApiClient.fetchVideoPosts(perPage = 25)
        videoPostsCache = videos
        videos
    }

    suspend fun getCategories(forceRefresh: Boolean = false): List<WPCategory> = categoriesMutex.withLock {
        if (!forceRefresh && !categoriesCache.isNullOrEmpty()) {
            return@withLock categoriesCache!!
        }

        val freshCategories = WordPressApiClient.fetchCategories()
        categoriesCache = freshCategories
        freshCategories
    }

    suspend fun getPost(id: Long, forceRefresh: Boolean = false): WPPost {
        if (!forceRefresh) {
            val cached = articleLruCache.get(id)
            if (cached != null) return cached
        }

        val post = WordPressApiClient.fetchPost(id)
        articleLruCache.put(id, post)
        return post
    }

    companion object {
        @Volatile
        private var INSTANCE: NewsRepository? = null

        fun getInstance(): NewsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NewsRepository().also { INSTANCE = it }
            }
        }
    }
}
