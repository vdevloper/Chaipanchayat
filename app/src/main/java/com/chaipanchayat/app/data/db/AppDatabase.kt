package com.chaipanchayat.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chaipanchayat.app.data.model.BookmarkEntity
import com.chaipanchayat.app.data.model.CacheMetadataEntity
import com.chaipanchayat.app.data.model.CachedPostEntity
import com.chaipanchayat.app.data.model.CachedTaxonomyEntity

@Database(
    entities = [
        BookmarkEntity::class,
        CachedPostEntity::class,
        CacheMetadataEntity::class,
        CachedTaxonomyEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao
    abstract fun postCacheDao(): PostCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chai_panchayat.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
