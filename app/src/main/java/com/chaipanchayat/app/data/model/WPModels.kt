package com.chaipanchayat.app.data.model

data class WPPost(
    val id: Long,
    val date: String,
    val link: String,
    val slug: String,
    val rawTitle: String,
    val cleanTitle: String,
    val rawExcerpt: String,
    val cleanExcerpt: String,
    val rawContent: String,
    val categories: List<Long>,
    val featuredImageUrl: String?,
    val primaryCategory: String?,
    val authorName: String?,
    val youtubeId: String? = null
)

data class VideoItem(
    val id: Long,
    val title: String,
    val youtubeId: String,
    val youtubeUrl: String,
    val thumbnail: String,
    val category: String?,
    val date: String,
    val articleId: Long,
    val description: String = ""
)

data class WPCategory(
    val id: Long,
    val name: String,
    val slug: String,
    val count: Int,
    val description: String? = null
)
