package com.chaipanchayat.app.data.repository

import android.content.Context
import android.util.LruCache
import com.chaipanchayat.app.data.api.WordPressApiClient
import com.chaipanchayat.app.data.db.AppDatabase
import com.chaipanchayat.app.data.db.PostCacheDao
import com.chaipanchayat.app.data.model.CacheMetadataEntity
import com.chaipanchayat.app.data.model.CachedPostEntity
import com.chaipanchayat.app.data.model.CachedTaxonomyEntity
import com.chaipanchayat.app.data.model.VideoItem
import com.chaipanchayat.app.data.model.WPCategory
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.data.model.WPTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class NewsRepository private constructor(private val cacheDao: PostCacheDao?) {

    private val postsMutex = Mutex()
    private val videosMutex = Mutex()
    private val categoriesMutex = Mutex()
    private val tagsMutex = Mutex()

    // In-memory caches for instantaneous screen and tab switching
    private var homePostsCache: List<WPPost>? = null
    private val categoryPostsCache = mutableMapOf<Long, List<WPPost>>()
    private val tagPostsCache = mutableMapOf<Long, List<WPPost>>()
    private var categoriesCache: List<WPCategory>? = null
    private var tagsCache: List<WPTag>? = null
    private var videoPostsCache: List<VideoItem>? = null
    private val articleLruCache = LruCache<Long, WPPost>(60)

    private val freshnessThresholdMs = 8 * 60 * 1000L // 8 minutes local freshness

    fun getCachedPosts(categoryId: Long?): List<WPPost>? {
        return if (categoryId == null || categoryId == 0L) {
            homePostsCache
        } else {
            categoryPostsCache[categoryId]
        }
    }

    fun getCachedCategories(): List<WPCategory>? = categoriesCache
    fun getCachedTags(): List<WPTag>? = tagsCache
    fun getCachedVideos(): List<VideoItem>? = videoPostsCache

    private fun postToEntity(feedKey: String, post: WPPost, index: Int): CachedPostEntity {
        return CachedPostEntity(
            feedKey = feedKey,
            id = post.id,
            date = post.date,
            link = post.link,
            slug = post.slug,
            rawTitle = post.rawTitle,
            cleanTitle = post.cleanTitle,
            rawExcerpt = post.rawExcerpt,
            cleanExcerpt = post.cleanExcerpt,
            rawContent = post.rawContent,
            categoriesCsv = post.categories.joinToString(","),
            featuredImageUrl = post.featuredImageUrl,
            primaryCategory = post.primaryCategory,
            authorName = post.authorName,
            youtubeId = post.youtubeId,
            videoUrl = post.videoUrl,
            sortOrder = index
        )
    }

    private fun entityToPost(entity: CachedPostEntity): WPPost {
        return WPPost(
            id = entity.id,
            date = entity.date,
            link = entity.link,
            slug = entity.slug,
            rawTitle = entity.rawTitle,
            cleanTitle = entity.cleanTitle,
            rawExcerpt = entity.rawExcerpt,
            cleanExcerpt = entity.cleanExcerpt,
            rawContent = entity.rawContent,
            categories = if (entity.categoriesCsv.isBlank()) emptyList() else entity.categoriesCsv.split(",").mapNotNull { it.trim().toLongOrNull() },
            featuredImageUrl = entity.featuredImageUrl,
            primaryCategory = entity.primaryCategory,
            authorName = entity.authorName,
            youtubeId = entity.youtubeId,
            videoUrl = entity.videoUrl
        )
    }

    suspend fun getPosts(
        categoryId: Long? = null,
        forceRefresh: Boolean = false
    ): List<WPPost> = postsMutex.withLock {
        val feedKey = if (categoryId == null || categoryId == 0L) "feed_home" else "feed_cat_$categoryId"
        val catId = if (categoryId != null && categoryId > 0L) categoryId else null

        // 1. Check in-memory cache
        val memoryCached = getCachedPosts(categoryId)

        // 2. If memory is empty, check Room local disk database cache
        val localCached = if (memoryCached.isNullOrEmpty() && cacheDao != null) {
            withContext(Dispatchers.IO) {
                try {
                    val dbEntities = cacheDao.getPostsForFeed(feedKey)
                    if (dbEntities.isNotEmpty()) {
                        val posts = dbEntities.map { entityToPost(it) }
                        if (catId == null) homePostsCache = posts else categoryPostsCache[catId] = posts
                        posts
                    } else null
                } catch (_: Exception) { null }
            }
        } else memoryCached

        val metadata = if (cacheDao != null) {
            withContext(Dispatchers.IO) {
                try { cacheDao.getMetadata(feedKey) } catch (_: Exception) { null }
            }
        } else null

        val now = System.currentTimeMillis()

        // 3. If not forced refresh and cache is fresh, return immediately without network call
        if (!forceRefresh && !localCached.isNullOrEmpty()) {
            val isFresh = metadata != null && (now - metadata.lastFetchedAt < freshnessThresholdMs)
            if (isFresh) {
                return@withLock localCached
            }
        }

        // 4. DATA SAVING LOGIC: If we already have cached posts, check if content was actually updated on the server!
        // We do this by requesting ONLY the latest 1 post id and date (~40 bytes vs ~100KB full feed).
        if (!localCached.isNullOrEmpty() && metadata != null) {
            val latestServerHeader = WordPressApiClient.fetchLatestPostIdAndDate(categoryId = catId)
            if (latestServerHeader != null) {
                val (serverLatestId, serverLatestDate) = latestServerHeader
                if (serverLatestId == metadata.latestPostId && serverLatestDate == metadata.latestPostDate) {
                    // Content is NOT updated on server!
                    // Refresh timestamp so we don't re-check unnecessarily soon
                    withContext(Dispatchers.IO) {
                        try {
                            cacheDao?.saveMetadata(metadata.copy(lastFetchedAt = now))
                        } catch (_: Exception) { }
                    }
                    // Return local storage cached posts with ZERO full feed payload downloaded!
                    return@withLock localCached
                }
            }
        }

        // 5. Server has new content or we have no local cache: fetch full feed
        val freshPosts = WordPressApiClient.fetchPosts(
            categoryId = catId,
            perPage = 15,
            forceNetwork = forceRefresh
        )

        // Save to in-memory cache
        if (catId == null) {
            homePostsCache = freshPosts
        } else {
            categoryPostsCache[catId] = freshPosts
        }

        // Persist to local Room database for offline access & future data saving
        if (cacheDao != null && freshPosts.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val entities = freshPosts.mapIndexed { idx, post -> postToEntity(feedKey, post, idx) }
                    cacheDao.deleteFeed(feedKey)
                    cacheDao.insertPosts(entities)
                    val first = freshPosts.first()
                    cacheDao.saveMetadata(
                        CacheMetadataEntity(
                            feedKey = feedKey,
                            lastFetchedAt = now,
                            latestPostId = first.id,
                            latestPostDate = first.date
                        )
                    )
                } catch (_: Exception) { }
            }
        }

        freshPosts
    }

    suspend fun getPostsByTag(
        tagId: Long,
        forceRefresh: Boolean = false
    ): List<WPPost> = postsMutex.withLock {
        val feedKey = "feed_tag_$tagId"
        val memoryCached = tagPostsCache[tagId]
        val localCached = if (memoryCached.isNullOrEmpty() && cacheDao != null) {
            withContext(Dispatchers.IO) {
                try {
                    val dbEntities = cacheDao.getPostsForFeed(feedKey)
                    if (dbEntities.isNotEmpty()) {
                        val posts = dbEntities.map { entityToPost(it) }
                        tagPostsCache[tagId] = posts
                        posts
                    } else null
                } catch (_: Exception) { null }
            }
        } else memoryCached

        val metadata = if (cacheDao != null) {
            withContext(Dispatchers.IO) {
                try { cacheDao.getMetadata(feedKey) } catch (_: Exception) { null }
            }
        } else null

        val now = System.currentTimeMillis()
        if (!forceRefresh && !localCached.isNullOrEmpty()) {
            if (metadata != null && (now - metadata.lastFetchedAt < freshnessThresholdMs)) {
                return@withLock localCached
            }
        }

        // Check if tag feed has updated
        if (!localCached.isNullOrEmpty() && metadata != null) {
            val latestServerHeader = WordPressApiClient.fetchLatestPostIdAndDate(tagId = tagId)
            if (latestServerHeader != null) {
                val (serverLatestId, serverLatestDate) = latestServerHeader
                if (serverLatestId == metadata.latestPostId && serverLatestDate == metadata.latestPostDate) {
                    withContext(Dispatchers.IO) {
                        try { cacheDao?.saveMetadata(metadata.copy(lastFetchedAt = now)) } catch (_: Exception) { }
                    }
                    return@withLock localCached
                }
            }
        }

        val freshPosts = WordPressApiClient.fetchPosts(
            tagId = tagId,
            perPage = 15,
            forceNetwork = forceRefresh
        )
        tagPostsCache[tagId] = freshPosts

        if (cacheDao != null && freshPosts.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val entities = freshPosts.mapIndexed { idx, post -> postToEntity(feedKey, post, idx) }
                    cacheDao.deleteFeed(feedKey)
                    cacheDao.insertPosts(entities)
                    val first = freshPosts.first()
                    cacheDao.saveMetadata(
                        CacheMetadataEntity(
                            feedKey = feedKey,
                            lastFetchedAt = now,
                            latestPostId = first.id,
                            latestPostDate = first.date
                        )
                    )
                } catch (_: Exception) { }
            }
        }

        freshPosts
    }

    suspend fun getVideoPosts(forceRefresh: Boolean = false): List<VideoItem> = videosMutex.withLock {
        if (!forceRefresh && !videoPostsCache.isNullOrEmpty()) {
            return@withLock videoPostsCache!!
        }

        val feedKey = "feed_videos"
        val localCached = if (cacheDao != null) {
            withContext(Dispatchers.IO) {
                try {
                    val dbEntities = cacheDao.getPostsForFeed(feedKey)
                    if (dbEntities.isNotEmpty()) {
                        dbEntities.map { entity ->
                            VideoItem(
                                id = entity.id,
                                title = entity.cleanTitle,
                                youtubeId = entity.youtubeId ?: entity.slug,
                                youtubeUrl = entity.link,
                                thumbnail = entity.featuredImageUrl ?: "",
                                category = entity.primaryCategory,
                                date = entity.date,
                                articleId = entity.id,
                                description = entity.cleanExcerpt,
                                videoUrl = entity.videoUrl
                            )
                        }
                    } else null
                } catch (_: Exception) { null }
            }
        } else null

        val metadata = if (cacheDao != null) {
            withContext(Dispatchers.IO) {
                try { cacheDao.getMetadata(feedKey) } catch (_: Exception) { null }
            }
        } else null

        val now = System.currentTimeMillis()
        if (!forceRefresh && !localCached.isNullOrEmpty()) {
            videoPostsCache = localCached
            if (metadata != null && (now - metadata.lastFetchedAt < freshnessThresholdMs)) {
                return@withLock localCached
            }
        }

        val videos = WordPressApiClient.fetchVideoPosts(perPage = 25, forceNetwork = forceRefresh)
        videoPostsCache = videos

        if (cacheDao != null && videos.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val entities = videos.mapIndexed { idx, v ->
                        CachedPostEntity(
                            feedKey = feedKey,
                            id = v.id,
                            date = v.date,
                            link = v.youtubeUrl,
                            slug = v.youtubeId,
                            rawTitle = v.title,
                            cleanTitle = v.title,
                            rawExcerpt = v.description,
                            cleanExcerpt = v.description,
                            rawContent = "",
                            categoriesCsv = "",
                            featuredImageUrl = v.thumbnail,
                            primaryCategory = v.category,
                            authorName = null,
                            youtubeId = v.youtubeId,
                            videoUrl = v.videoUrl,
                            sortOrder = idx
                        )
                    }
                    cacheDao.deleteFeed(feedKey)
                    cacheDao.insertPosts(entities)
                    cacheDao.saveMetadata(
                        CacheMetadataEntity(
                            feedKey = feedKey,
                            lastFetchedAt = now,
                            latestPostId = videos.first().id,
                            latestPostDate = videos.first().date
                        )
                    )
                } catch (_: Exception) { }
            }
        }

        videos
    }

    suspend fun getCategories(forceRefresh: Boolean = false): List<WPCategory> = categoriesMutex.withLock {
        if (!forceRefresh && !categoriesCache.isNullOrEmpty()) {
            return@withLock categoriesCache!!
        }

        if (!forceRefresh && cacheDao != null) {
            val dbCats = withContext(Dispatchers.IO) {
                try {
                    val items = cacheDao.getTaxonomies("category")
                    if (items.isNotEmpty()) {
                        items.map { WPCategory(it.id, it.name, it.slug, it.count, it.description) }
                    } else null
                } catch (_: Exception) { null }
            }
            if (!dbCats.isNullOrEmpty()) {
                categoriesCache = dbCats
                return@withLock dbCats
            }
        }

        val freshCategories = WordPressApiClient.fetchCategories(forceNetwork = forceRefresh)
        categoriesCache = freshCategories

        if (cacheDao != null && freshCategories.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val items = freshCategories.map {
                        CachedTaxonomyEntity("category", it.id, it.name, it.slug, it.count, it.description)
                    }
                    cacheDao.clearTaxonomies("category")
                    cacheDao.insertTaxonomies(items)
                } catch (_: Exception) { }
            }
        }

        freshCategories
    }

    suspend fun getTags(forceRefresh: Boolean = false): List<WPTag> = tagsMutex.withLock {
        if (!forceRefresh && !tagsCache.isNullOrEmpty()) {
            return@withLock tagsCache!!
        }

        if (!forceRefresh && cacheDao != null) {
            val dbTags = withContext(Dispatchers.IO) {
                try {
                    val items = cacheDao.getTaxonomies("tag")
                    if (items.isNotEmpty()) {
                        items.map { WPTag(it.id, it.name, it.slug, it.count) }
                    } else null
                } catch (_: Exception) { null }
            }
            if (!dbTags.isNullOrEmpty()) {
                tagsCache = dbTags
                return@withLock dbTags
            }
        }

        val freshTags = WordPressApiClient.fetchTags(forceNetwork = forceRefresh)
        tagsCache = freshTags

        if (cacheDao != null && freshTags.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val items = freshTags.map {
                        CachedTaxonomyEntity("tag", it.id, it.name, it.slug, it.count)
                    }
                    cacheDao.clearTaxonomies("tag")
                    cacheDao.insertTaxonomies(items)
                } catch (_: Exception) { }
            }
        }

        freshTags
    }

    suspend fun getPost(id: Long, forceRefresh: Boolean = false): WPPost {
        if (!forceRefresh) {
            val cached = articleLruCache.get(id)
            if (cached != null) return cached

            if (cacheDao != null) {
                val dbPost = withContext(Dispatchers.IO) {
                    try {
                        val entity = cacheDao.getPostById(id)
                        if (entity != null && entity.rawContent.isNotBlank()) {
                            entityToPost(entity)
                        } else null
                    } catch (_: Exception) { null }
                }
                if (dbPost != null) {
                    articleLruCache.put(id, dbPost)
                    return dbPost
                }
            }
        }

        val post = WordPressApiClient.fetchPost(id)
        articleLruCache.put(id, post)

        if (cacheDao != null) {
            withContext(Dispatchers.IO) {
                try {
                    cacheDao.insertPosts(listOf(postToEntity("article_detail", post, 0)))
                } catch (_: Exception) { }
            }
        }

        return post
    }

    companion object {
        @Volatile
        private var INSTANCE: NewsRepository? = null

        fun init(context: Context) {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        val db = AppDatabase.getDatabase(context.applicationContext)
                        INSTANCE = NewsRepository(db.postCacheDao())
                    }
                }
            }
        }

        fun getInstance(): NewsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NewsRepository(null).also { INSTANCE = it }
            }
        }
    }
}
