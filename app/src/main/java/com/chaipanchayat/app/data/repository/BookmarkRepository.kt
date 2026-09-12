package com.chaipanchayat.app.data.repository

import android.content.Context
import com.chaipanchayat.app.data.db.AppDatabase
import com.chaipanchayat.app.data.model.BookmarkEntity
import com.chaipanchayat.app.data.model.WPPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BookmarkRepository(context: Context) {

    private val bookmarkDao = AppDatabase.getDatabase(context).bookmarkDao()

    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    fun isBookmarked(id: Long): Flow<Boolean> = bookmarkDao.isBookmarked(id)

    suspend fun toggleBookmark(post: WPPost): Boolean = withContext(Dispatchers.IO) {
        val exists = bookmarkDao.isBookmarkedSync(post.id)
        if (exists) {
            bookmarkDao.deleteBookmark(post.id)
            false
        } else {
            val entity = BookmarkEntity(
                id = post.id,
                title = post.cleanTitle,
                excerpt = post.cleanExcerpt,
                link = post.link,
                date = post.date,
                image = post.featuredImageUrl,
                category = post.primaryCategory,
                savedAt = System.currentTimeMillis()
            )
            bookmarkDao.insertBookmark(entity)
            true
        }
    }

    suspend fun removeBookmark(id: Long) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(id)
    }

    companion object {
        @Volatile
        private var INSTANCE: BookmarkRepository? = null

        fun getInstance(context: Context): BookmarkRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BookmarkRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
