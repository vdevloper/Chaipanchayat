package com.chaipanchayat.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cached_posts",
    primaryKeys = ["feedKey", "id"],
    indices = [Index(value = ["feedKey", "sortOrder"])]
)
data class CachedPostEntity(
    val feedKey: String, // e.g., "feed_home", "cat_12", "tag_4", "article_detail"
    val id: Long,
    val date: String,
    val link: String,
    val slug: String,
    val rawTitle: String,
    val cleanTitle: String,
    val rawExcerpt: String,
    val cleanExcerpt: String,
    val rawContent: String,
    val categoriesCsv: String,
    val featuredImageUrl: String?,
    val primaryCategory: String?,
    val authorName: String?,
    val youtubeId: String?,
    val videoUrl: String?,
    val sortOrder: Int = 0,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cache_metadata")
data class CacheMetadataEntity(
    @PrimaryKey val feedKey: String,
    val lastFetchedAt: Long,
    val latestPostId: Long,
    val latestPostDate: String
)

@Entity(
    tableName = "cached_taxonomies",
    primaryKeys = ["type", "id"]
)
data class CachedTaxonomyEntity(
    val type: String, // "category" or "tag"
    val id: Long,
    val name: String,
    val slug: String,
    val count: Int,
    val description: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)
