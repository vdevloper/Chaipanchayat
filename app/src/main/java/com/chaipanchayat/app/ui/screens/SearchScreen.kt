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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TrendingUp
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.data.api.TranslationService
import com.chaipanchayat.app.data.model.AppLanguage
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.data.repository.NewsRepository
import com.chaipanchayat.app.data.repository.SettingsRepository
import com.chaipanchayat.app.engine.liquidglass.ChaiGlassHeader
import com.chaipanchayat.app.engine.liquidglass.LiquidGlassBackdrop
import com.chaipanchayat.app.engine.liquidglass.LiquidGlassEngine
import com.chaipanchayat.app.engine.liquidglass.liquidGlass
import com.chaipanchayat.app.ui.components.ChaiBrewLoadingIndicator
import com.chaipanchayat.app.ui.components.EmptyState
import com.chaipanchayat.app.ui.components.NewsCard
import com.chaipanchayat.app.ui.components.chaiSharedBounds
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.kyant.backdrop.Capsule
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    onNavigateToArticle: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { NewsRepository.getInstance() }
    val settingsRepo = remember { SettingsRepository.getInstance(context) }
    val currentAppLang by settingsRepo.appLanguage.collectAsState()
    val showOriginalHindi by settingsRepo.showOriginalHindi.collectAsState()
    var translatedTitles by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }

    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<WPPost>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    LaunchedEffect(results, currentAppLang, showOriginalHindi) {
        if (currentAppLang != AppLanguage.HINDI && !showOriginalHindi && results.isNotEmpty()) {
            try {
                translatedTitles = TranslationService.translatePostsTitles(results, currentAppLang)
            } catch (_: Exception) {
            }
        } else {
            translatedTitles = emptyMap()
        }
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val trendingTopics = listOf(
        "गोरखपुर (Gorakhpur)",
        "उत्तर प्रदेश (UP News)",
        "राजनीति (Politics)",
        "अपराध (Crime)",
        "खेल (Sports)",
        "मनोरंजन (Cinema)"
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Debounced live search
    LaunchedEffect(query) {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            results = emptyList()
            isSearching = false
            hasSearched = false
            return@LaunchedEffect
        }

        isSearching = true
        delay(350)
        try {
            results = com.chaipanchayat.app.data.api.WordPressApiClient.fetchPosts(search = trimmed, perPage = 20)
            hasSearched = true
        } catch (_: Exception) {
            results = emptyList()
            hasSearched = true
        } finally {
            isSearching = false
        }
    }

    val isLiquid = ChaiTheme.extended.isLiquidGlass
    val searchBackdrop = rememberLayerBackdrop()
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val headerHeightDp = with(density) { headerHeightPx.toDp() }
    val capsuleShape = remember { Capsule() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isLiquid) Color.Transparent else MaterialTheme.colorScheme.surface)
    ) {
        // Layer 1: Recorded Search Backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isLiquid) Modifier.layerBackdrop(searchBackdrop) else Modifier)
        ) {
            if (isLiquid) {
                LiquidGlassBackdrop(settings = LiquidGlassEngine.current)
            }

            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = headerHeightDp),
                    contentAlignment = Alignment.Center
                ) {
                    ChaiBrewLoadingIndicator(
                        size = 72.dp,
                        tintColor = ChaiSaffron,
                        showAura = true
                    )
                }
            } else if (!hasSearched && query.isBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = headerHeightDp)
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
                            color = ChaiTheme.extended.textSecondary,
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
                            if (isLiquid) {
                                Box(
                                    modifier = Modifier
                                        .liquidGlass(
                                            shape = capsuleShape,
                                            blurRadiusDp = 18.dp,
                                            withShadow = true
                                        )
                                        .clickable { query = topic.substringBefore(" (") }
                                ) {
                                    Text(
                                        text = topic,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontFamily = InterFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                                    )
                                }
                            } else {
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
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        trendingTopics.drop(3).forEach { topic ->
                            if (isLiquid) {
                                Box(
                                    modifier = Modifier
                                        .liquidGlass(
                                            shape = capsuleShape,
                                            blurRadiusDp = 18.dp,
                                            withShadow = true
                                        )
                                        .clickable { query = topic.substringBefore(" (") }
                                ) {
                                    Text(
                                        text = topic,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontFamily = InterFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                                    )
                                }
                            } else {
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
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    EmptyState(
                        title = "चाय पंचायत खोजें",
                        message = "किसी भी खबर, मुद्दे या नेता के बारे में जानने के लिए सर्च करें।",
                        icon = Icons.Outlined.Search
                    )
                }
            } else if (hasSearched && results.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(top = headerHeightDp)) {
                    EmptyState(
                        title = "कोई परिणाम नहीं मिला",
                        message = "\"$query\" से संबंधित कोई लेख नहीं मिला। कृपया अन्य शब्द आज़माएं।",
                        icon = Icons.Outlined.Search
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = headerHeightDp)
                ) {
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
                            onClick = { onNavigateToArticle(post.id) },
                            displayTitle = if (showOriginalHindi) post.cleanTitle else (translatedTitles[post.id] ?: post.cleanTitle)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }

        // Layer 2: Floating Refractive Search Header
        if (isLiquid) {
            ChaiGlassHeader(
                backdrop = searchBackdrop,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .onSizeChanged { headerHeightPx = it.height }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .liquidGlass(
                                shape = CircleShape,
                                blurRadiusDp = 18.dp,
                                backdrop = searchBackdrop
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("search-back-button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = {
                            Text(
                                text = "समाचार, विषय या शहर खोजें...",
                                color = ChaiTheme.extended.textSecondary,
                                fontFamily = InterFamily,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = ChaiSaffron,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(
                                    onClick = { query = "" },
                                    modifier = Modifier.testTag("search-clear-button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = ChaiTheme.extended.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChaiSaffron,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = ChaiSaffron
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { keyboardController?.hide() }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .chaiSharedBounds("search_bar")
                            .liquidGlass(
                                shape = RoundedCornerShape(24.dp),
                                blurRadiusDp = 18.dp,
                                backdrop = searchBackdrop
                            )
                            .focusRequester(focusRequester)
                            .testTag("search-input-field")
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .onSizeChanged { headerHeightPx = it.height }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("search-back-button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = {
                            Text(
                                text = "समाचार, विषय या शहर खोजें...",
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
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(
                                    onClick = { query = "" },
                                    modifier = Modifier.testTag("search-clear-button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = ChaiTheme.extended.muted,
                                        modifier = Modifier.size(18.dp)
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
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { keyboardController?.hide() }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .chaiSharedBounds("search_bar")
                            .focusRequester(focusRequester)
                            .testTag("search-input-field")
                    )
                }

                HorizontalDivider(thickness = 0.8.dp, color = ChaiTheme.extended.border.copy(alpha = 0.7f))
            }
        }
    }
}
