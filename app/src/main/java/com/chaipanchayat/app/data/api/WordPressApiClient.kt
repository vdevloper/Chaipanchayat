package com.chaipanchayat.app.data.api

import com.chaipanchayat.app.data.model.WPCategory
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.utils.HtmlUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WordPressApiClient {

    private const val BASE_URL = "https://chaipanchayat.com/wp-json/wp/v2"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetchPosts(
        page: Int = 1,
        perPage: Int = 20,
        categoryId: Long? = null,
        search: String? = null
    ): List<WPPost> = withContext(Dispatchers.IO) {
        val urlBuilder = "$BASE_URL/posts".toHttpUrlOrNull()?.newBuilder()
            ?: throw IllegalArgumentException("Invalid URL")

        urlBuilder.addQueryParameter("per_page", perPage.toString())
        urlBuilder.addQueryParameter("page", page.toString())
        urlBuilder.addQueryParameter("orderby", "date")
        urlBuilder.addQueryParameter("order", "desc")
        urlBuilder.addQueryParameter("_embed", "1")

        if (categoryId != null && categoryId > 0) {
            urlBuilder.addQueryParameter("categories", categoryId.toString())
        }
        if (!search.isNullOrBlank()) {
            urlBuilder.addQueryParameter("search", search.trim())
        }

        val request = Request.Builder()
            .url(urlBuilder.build())
            .get()
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("WordPress API error: ${response.code}")
        }
        val responseBody = response.body?.string().orEmpty()
        val jsonArray = JSONArray(responseBody)
        val posts = mutableListOf<WPPost>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            posts.add(parsePost(obj))
        }
        posts
    }

    suspend fun fetchPost(id: Long): WPPost = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/posts/$id?_embed=1"
        val request = Request.Builder().url(url).get().build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("WordPress API error: ${response.code}")
        }
        val responseBody = response.body?.string().orEmpty()
        val obj = JSONObject(responseBody)
        parsePost(obj)
    }

    suspend fun fetchCategories(): List<WPCategory> = withContext(Dispatchers.IO) {
        val urlBuilder = "$BASE_URL/categories".toHttpUrlOrNull()?.newBuilder()
            ?: throw IllegalArgumentException("Invalid URL")

        urlBuilder.addQueryParameter("per_page", "100")
        urlBuilder.addQueryParameter("orderby", "count")
        urlBuilder.addQueryParameter("order", "desc")
        urlBuilder.addQueryParameter("hide_empty", "1")

        val request = Request.Builder().url(urlBuilder.build()).get().build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("WordPress API error: ${response.code}")
        }
        val responseBody = response.body?.string().orEmpty()
        val jsonArray = JSONArray(responseBody)
        val categories = mutableListOf<WPCategory>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val slug = obj.optString("slug", "")
            if (slug != "uncategorized") {
                categories.add(
                    WPCategory(
                        id = obj.getLong("id"),
                        name = HtmlUtils.decodeEntities(obj.optString("name", "")),
                        slug = slug,
                        count = obj.optInt("count", 0),
                        description = obj.optString("description", null)
                    )
                )
            }
        }
        categories
    }

    private fun parsePost(obj: JSONObject): WPPost {
        val id = obj.getLong("id")
        val date = obj.optString("date", "")
        val link = obj.optString("link", "")
        val slug = obj.optString("slug", "")

        val titleObj = obj.optJSONObject("title")
        val rawTitle = titleObj?.optString("rendered", "") ?: ""
        val cleanTitle = HtmlUtils.stripHtml(rawTitle)

        val excerptObj = obj.optJSONObject("excerpt")
        val rawExcerpt = excerptObj?.optString("rendered", "") ?: ""
        val cleanExcerpt = HtmlUtils.stripHtml(rawExcerpt)

        val contentObj = obj.optJSONObject("content")
        val rawContent = contentObj?.optString("rendered", "") ?: ""

        val categories = mutableListOf<Long>()
        val catArr = obj.optJSONArray("categories")
        if (catArr != null) {
            for (i in 0 until catArr.length()) {
                categories.add(catArr.getLong(i))
            }
        }

        var featuredImage: String? = null
        var primaryCategory: String? = null
        var authorName: String? = null

        val embedded = obj.optJSONObject("_embedded")
        if (embedded != null) {
            val mediaArr = embedded.optJSONArray("wp:featuredmedia")
            if (mediaArr != null && mediaArr.length() > 0) {
                val mediaObj = mediaArr.optJSONObject(0)
                featuredImage = mediaObj?.optString("source_url")
            }

            val termOuterArr = embedded.optJSONArray("wp:term")
            if (termOuterArr != null) {
                for (i in 0 until termOuterArr.length()) {
                    val termInnerArr = termOuterArr.optJSONArray(i)
                    if (termInnerArr != null) {
                        for (j in 0 until termInnerArr.length()) {
                            val termObj = termInnerArr.optJSONObject(j)
                            if (termObj?.optString("taxonomy") == "category") {
                                primaryCategory = termObj.optString("name")
                                break
                            }
                        }
                    }
                    if (primaryCategory != null) break
                }
            }

            val authorArr = embedded.optJSONArray("author")
            if (authorArr != null && authorArr.length() > 0) {
                authorName = authorArr.optJSONObject(0)?.optString("name")
            }
        }

        return WPPost(
            id = id,
            date = date,
            link = link,
            slug = slug,
            rawTitle = rawTitle,
            cleanTitle = cleanTitle,
            rawExcerpt = rawExcerpt,
            cleanExcerpt = cleanExcerpt,
            rawContent = rawContent,
            categories = categories,
            featuredImageUrl = featuredImage,
            primaryCategory = primaryCategory?.let { HtmlUtils.decodeEntities(it) },
            authorName = authorName
        )
    }
}
