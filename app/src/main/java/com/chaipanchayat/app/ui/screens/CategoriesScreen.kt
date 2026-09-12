package com.chaipanchayat.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Theaters
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.VideoLibrary
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.data.model.WPCategory
import com.chaipanchayat.app.data.repository.NewsRepository
import com.chaipanchayat.app.ui.components.EmptyState
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import kotlinx.coroutines.launch

@Composable
fun CategoriesScreen(
    onNavigateToCategory: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { NewsRepository.getInstance() }
    val initialCats = remember { repository.getCachedCategories().orEmpty() }

    var categories by remember { mutableStateOf(initialCats) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(initialCats.isEmpty()) }
    var isError by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (categories.isEmpty()) isLoading = true
        try {
            categories = repository.getCategories()
            isError = false
        } catch (_: Exception) {
            if (categories.isEmpty()) {
                isError = true
            }
        } finally {
            isLoading = false
        }
    }

    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) {
            categories
        } else {
            categories.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .testTag("categories-screen")
    ) {
        // Top Brand Ribbon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(ChaiBrandGradient)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // Spec Section 12 Header: Title: विषय, Subtitle: विषय अनुसार ताज़ा खबरें और विशेष विश्लेषण
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .background(ChaiBrandGradient, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "विषय",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = NotoSerifFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                    }
                    Text(
                        text = "विषय अनुसार ताज़ा खबरें और विशेष विश्लेषण",
                        color = ChaiTheme.extended.muted,
                        fontFamily = InterFamily,
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(start = 14.dp, top = 2.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = ChaiTheme.extended.surfaceSecondary,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ChaiTheme.extended.border)
                ) {
                    Text(
                        text = "${categories.size} विषय",
                        color = ChaiSaffron,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Filter Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "विषय खोजें (उदा. राजनीति, खेल, अपराध)...",
                        color = ChaiTheme.extended.muted,
                        fontFamily = InterFamily,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search categories",
                        tint = ChaiTheme.extended.muted,
                        modifier = Modifier.size(19.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Clear search",
                                tint = ChaiTheme.extended.muted,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChaiSaffron,
                    unfocusedBorderColor = ChaiTheme.extended.border,
                    focusedContainerColor = ChaiTheme.extended.surfaceSecondary,
                    unfocusedContainerColor = ChaiTheme.extended.surfaceSecondary,
                    cursorColor = ChaiSaffron
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("categories-search-input")
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Content Area
        if (isLoading && categories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ChaiSaffron,
                    modifier = Modifier.size(36.dp)
                )
            }
        } else if (isError && categories.isEmpty()) {
            EmptyState(
                title = "विषय लोड नहीं हो सके",
                message = "कृपया अपना इंटरनेट कनेक्शन जांचें और पुनः प्रयास करें।",
                actionLabel = "पुनः प्रयास करें",
                onAction = {
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            categories = repository.getCategories(forceRefresh = true)
                            isError = false
                        } catch (_: Exception) {
                            isError = true
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )
        } else if (filteredCategories.isEmpty()) {
            EmptyState(
                title = "कोई विषय नहीं मिला",
                message = "\"$searchQuery\" नाम का कोई विषय उपलब्ध नहीं है।",
                icon = Icons.Outlined.Search
            )
        } else {
            // Spec Section 12: Clean category index rather than giant two-column cards
            LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredCategories, key = { it.id }) { cat ->
                    CategoryIndexRow(
                        category = cat,
                        onClick = { onNavigateToCategory(cat.id, cat.name) }
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = ChaiTheme.extended.border.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 64.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryIndexRow(
    category: WPCategory,
    onClick: () -> Unit
) {
    val style = remember(category.name) { getCategoryIconStyle(category.name) }

    Row(
        modifier = Modifier
            .testTag("category-row-${category.id}")
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Compact Icon Container
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = style.iconTint.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(0.8.dp, style.iconTint.copy(alpha = 0.3f)),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Category Name
        Text(
            text = category.name,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = NotoSerifFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        // Article Count Pill
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = ChaiTheme.extended.surfaceSecondary,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, ChaiTheme.extended.border)
        ) {
            Text(
                text = "${category.count} लेख",
                color = ChaiTheme.extended.muted,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = ChaiTheme.extended.muted,
            modifier = Modifier.size(13.dp)
        )
    }
}

private data class CategoryIconStyle(
    val icon: ImageVector,
    val iconTint: Color
)

private fun getCategoryIconStyle(name: String): CategoryIconStyle {
    val lower = name.lowercase()
    return when {
        lower.contains("crime") || lower.contains("अपराध") -> CategoryIconStyle(
            icon = Icons.Outlined.Gavel,
            iconTint = Color(0xFFE52B2B)
        )
        lower.contains("politic") || lower.contains("राजनीति") -> CategoryIconStyle(
            icon = Icons.Outlined.AccountBalance,
            iconTint = ChaiSaffron
        )
        lower.contains("sport") || lower.contains("खेल") || lower.contains("cricket") -> CategoryIconStyle(
            icon = Icons.Outlined.EmojiEvents,
            iconTint = Color(0xFF10B981)
        )
        lower.contains("cinema") || lower.contains("entertainment") || lower.contains("मनोरंजन") || lower.contains("bollywood") -> CategoryIconStyle(
            icon = Icons.Outlined.Theaters,
            iconTint = Color(0xFFD9A441)
        )
        lower.contains("tech") || lower.contains("तकनीक") || lower.contains("gadget") -> CategoryIconStyle(
            icon = Icons.Outlined.Devices,
            iconTint = Color(0xFF0284C7)
        )
        lower.contains("business") || lower.contains("व्यापार") || lower.contains("अर्थ") -> CategoryIconStyle(
            icon = Icons.Outlined.TrendingUp,
            iconTint = Color(0xFFF59E0B)
        )
        lower.contains("state") || lower.contains("राज्य") || lower.contains("uttar") || lower.contains("bihar") || lower.contains("गोरखपुर") -> CategoryIconStyle(
            icon = Icons.Outlined.LocationOn,
            iconTint = Color(0xFF6366F1)
        )
        lower.contains("health") || lower.contains("स्वास्थ्य") -> CategoryIconStyle(
            icon = Icons.Outlined.HealthAndSafety,
            iconTint = Color(0xFF14B8A6)
        )
        lower.contains("lifestyle") || lower.contains("जीवन") -> CategoryIconStyle(
            icon = Icons.Outlined.AutoAwesome,
            iconTint = Color(0xFFA855F7)
        )
        lower.contains("video") || lower.contains("वीडियो") -> CategoryIconStyle(
            icon = Icons.Outlined.VideoLibrary,
            iconTint = Color(0xFFE52B2B)
        )
        else -> CategoryIconStyle(
            icon = Icons.Outlined.Article,
            iconTint = ChaiSaffron
        )
    }
}
