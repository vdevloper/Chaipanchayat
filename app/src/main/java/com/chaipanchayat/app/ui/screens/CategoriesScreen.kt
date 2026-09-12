package com.chaipanchayat.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.chaipanchayat.app.data.model.WPCategory
import com.chaipanchayat.app.data.repository.NewsRepository
import com.chaipanchayat.app.ui.components.EmptyState
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily

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
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "Categories",
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = NotoSerifFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            lineHeight = 34.sp
        )

        Text(
            text = "Browse stories by section",
            color = ChaiTheme.extended.muted,
            fontFamily = InterFamily,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Filter categories...",
                    color = ChaiTheme.extended.muted,
                    fontFamily = InterFamily,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = ChaiTheme.extended.muted,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ChaiTheme.extended.surfaceSecondary,
                unfocusedContainerColor = ChaiTheme.extended.surfaceSecondary,
                focusedBorderColor = ChaiSaffron,
                unfocusedBorderColor = ChaiTheme.extended.border
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("category-search-input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ChaiSaffron)
            }
        } else if (isError && categories.isEmpty()) {
            EmptyState(
                title = "Couldn't load categories",
                message = "Please check your network connection and try again.",
                actionLabel = "Retry",
                onAction = {
                    isLoading = true
                    // reload
                }
            )
        } else if (filteredCategories.isEmpty()) {
            EmptyState(
                title = "No categories found",
                message = "No sections match \"$searchQuery\"."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredCategories, key = { it.id }) { cat ->
                    CategoryGridCard(
                        category = cat,
                        onClick = { onNavigateToCategory(cat.id, cat.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryGridCard(
    category: WPCategory,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(10.dp)

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = ChaiTheme.extended.surfaceSecondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .testTag("category-card-${category.id}")
            .fillMaxWidth()
            .border(width = 0.5.dp, color = ChaiTheme.extended.border, shape = cardShape)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = category.name,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = NotoSerifFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ChaiTheme.extended.brandTertiary)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${category.count} ${if (category.count == 1) "story" else "stories"}",
                    color = ChaiSaffron,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
