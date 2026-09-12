package com.chaipanchayat.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val excerpt: String,
    val link: String,
    val date: String,
    val image: String?,
    val category: String?,
    val savedAt: Long = System.currentTimeMillis()
)
