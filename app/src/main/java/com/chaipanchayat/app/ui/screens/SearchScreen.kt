package com.chaipanchayat.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.data.api.WordPressApiClient
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.ui.components.EmptyState
import com.chaipanchayat.app.ui.components.NewsCard
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    onNavigateToArticle: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<WPPost>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    val trendingTopics = listOf(
        "राजनीति (Politics)",
        "क्रिकेट (Cricket)",
        "उत्तर प्रदेश (UP)",
        "चुनाव (Elections)",
        "सिनेमा (Cinema)",
        "अपराध (Crime)"
    )

    LaunchedEffect(query) {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            results = emptyList()
            isSearching = false
            hasSearched = false
            return@LaunchedEffect
        }
        delay(400) // debounce
        isSearching = true
        try {
            val posts = WordPressApiClient.fetchPosts(search = trimmed)
            results = posts
            hasSearched = true
        } catch (_: Exception) {
            results = emptyList()
        } finally {
            isSearching = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Top Brand Accent Ribbon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(ChaiBrandGradient)
        )

        // Search Input Top Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = ChaiTheme.extended.surfaceSecondary,
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = {
                        Text(
                            text = "खबर, मुद्दा या व्यक्ति खोजें...",
                            color = ChaiTheme.extended.muted,
                            fontFamily = InterFamily,
                            fontSize = 14.sp
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = { query = "" },
                                modifier = Modifier.testTag("search-clear-button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Clear search",
                                    tint = ChaiTheme.extended.muted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ChaiTheme.extended.surfaceSecondary,
                        unfocusedContainerColor = ChaiTheme.extended.surfaceSecondary,
                        focusedBorderColor = ChaiSaffron,
                        unfocusedBorderColor = ChaiTheme.extended.border
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                        .testTag("search-input")
                )
            }
        }

        HorizontalDivider(thickness = 0.8.dp, color = ChaiTheme.extended.border.copy(alpha = 0.7f))

        // Results state
        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ChaiSaffron)
            }
        } else if (!hasSearched && query.isBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = ChaiSaffron,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ट्रेंडिंग विषय • TRENDING SEARCHES",
                        color = ChaiTheme.extended.muted,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    trendingTopics.take(3).forEach { topic ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ChaiTheme.extended.surfaceSecondary,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ChaiTheme.extended.border),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { query = topic.substringBefore(" (") }
                        ) {
                            Text(
                                text = topic,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = InterFamily,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    trendingTopics.drop(3).forEach { topic ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ChaiTheme.extended.surfaceSecondary,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ChaiTheme.extended.border),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { query = topic.substringBefore(" (") }
                        ) {
                            Text(
                                text = topic,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = InterFamily,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                EmptyState(
                    title = "चाय पंचायत खोजें",
                    message = "किसी भी खबर, मुद्दे या नेता के बारे में जानने के लिए सर्च करें।",
                    icon = Icons.Outlined.Search
                )
            }
        } else if (hasSearched && results.isEmpty()) {
            EmptyState(
                title = "कोई परिणाम नहीं मिला",
                message = "\"$query\" से संबंधित कोई लेख नहीं मिला। कृपया अन्य शब्द आज़माएं।",
                icon = Icons.Outlined.Search
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        text = "${results.size} परिणाम मिले • RESULTS",
                        color = ChaiTheme.extended.muted,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                items(results, key = { it.id }) { post ->
                    NewsCard(
                        post = post,
                        onClick = { onNavigateToArticle(post.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}
