package com.chaipanchayat.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chaipanchayat.app.data.model.CacheMetadataEntity
import com.chaipanchayat.app.data.model.CachedPostEntity
import com.chaipanchayat.app.data.model.CachedTaxonomyEntity

@Dao
interface PostCacheDao {

    @Query("SELECT * FROM cached_posts WHERE feedKey = :feedKey ORDER BY sortOrder ASC")
    fun getPostsForFeed(feedKey: String): List<CachedPostEntity>

    @Query("SELECT * FROM cached_posts WHERE id = :id LIMIT 1")
    fun getPostById(id: Long): CachedPostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPosts(posts: List<CachedPostEntity>): List<Long>

    @Query("DELETE FROM cached_posts WHERE feedKey = :feedKey")
    fun deleteFeed(feedKey: String): Int

    @Query("SELECT * FROM cache_metadata WHERE feedKey = :feedKey LIMIT 1")
    fun getMetadata(feedKey: String): CacheMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveMetadata(metadata: CacheMetadataEntity): Long

    @Query("SELECT * FROM cached_taxonomies WHERE type = :type ORDER BY count DESC")
    fun getTaxonomies(type: String): List<CachedTaxonomyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTaxonomies(taxonomies: List<CachedTaxonomyEntity>): List<Long>

    @Query("DELETE FROM cached_taxonomies WHERE type = :type")
    fun clearTaxonomies(type: String): Int
}
