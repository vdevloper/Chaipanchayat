package com.chaipanchayat.app.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.data.repository.SettingsRepository
import com.chaipanchayat.app.data.repository.TextSizePreference
import com.chaipanchayat.app.data.repository.ThemeMode
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.chaipanchayat.app.worker.ChaiNewsNotificationWorker
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsRepo = remember { SettingsRepository.getInstance(context) }

    val themeMode by settingsRepo.themeMode.collectAsState()
    val textSize by settingsRepo.textSize.collectAsState()
    val pushNotifications by settingsRepo.pushNotifications.collectAsState()
    val breakingAlerts by settingsRepo.breakingAlerts.collectAsState()
    val dailyDigest by settingsRepo.dailyDigest.collectAsState()
    val autoPlayVideos by settingsRepo.autoPlayVideos.collectAsState()
    val saveArticles by settingsRepo.saveArticles.collectAsState()
    val language by settingsRepo.language.collectAsState()
    val cacheSize by settingsRepo.cacheSizeFormatted.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var isCheckingNow by remember { mutableStateOf(false) }
    var isClearingCache by remember { mutableStateOf(false) }

    // Dialog States
    var showProfileDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showTextSizeDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showClearCacheConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .testTag("settings-screen"),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        // Top Ribbon Accent
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(ChaiBrandGradient)
            )
        }

        // Screen Header: सेटिंग्स
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(24.dp)
                            .background(ChaiBrandGradient, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "सेटिंग्स",
                        fontFamily = NotoSerifFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "प्राथमिकताएं एवं ऐप नियंत्रण",
                    fontFamily = InterFamily,
                    fontSize = 13.sp,
                    color = ChaiTheme.extended.muted,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }

        // ==========================================
        // SECTION: ACCOUNT
        // ==========================================
        item {
            SettingsSectionHeader(title = "ACCOUNT")
            SettingsNavRow(
                icon = Icons.Outlined.Person,
                title = "Profile",
                subtitle = "चाय पंचायत पाठक",
                onClick = { showProfileDialog = true },
                testTag = "settings-profile-row"
            )
            SettingsNavRow(
                icon = Icons.Outlined.Notifications,
                title = "Notifications",
                subtitle = if (pushNotifications) "सक्रिय (पुश एवं ब्रेकिंग अलर्ट्स)" else "निष्क्रिय",
                onClick = { showNotificationsDialog = true },
                testTag = "settings-notifications-row"
            )
            HorizontalDivider(thickness = 0.5.dp, color = ChaiTheme.extended.border, modifier = Modifier.padding(vertical = 8.dp))
        }

        // ==========================================
        // SECTION: READING
        // ==========================================
        item {
            SettingsSectionHeader(title = "READING")
            SettingsNavRow(
                icon = Icons.Outlined.Language,
                title = "Language",
                trailingValue = language,
                onClick = { showLanguageDialog = true },
                testTag = "settings-language-row"
            )
            SettingsNavRow(
                icon = Icons.Outlined.FormatSize,
                title = "Text size",
                trailingValue = when (textSize) {
                    TextSizePreference.SMALL -> "Small"
                    TextSizePreference.MEDIUM -> "Medium"
                    TextSizePreference.LARGE -> "Large"
                    TextSizePreference.EXTRA_LARGE -> "Extra Large"
                },
                onClick = { showTextSizeDialog = true },
                testTag = "settings-textsize-row"
            )
            HorizontalDivider(thickness = 0.5.dp, color = ChaiTheme.extended.border, modifier = Modifier.padding(vertical = 8.dp))
        }

        // ==========================================
        // SECTION: APPEARANCE
        // ==========================================
        item {
            SettingsSectionHeader(title = "APPEARANCE")
            SettingsNavRow(
                icon = Icons.Outlined.DarkMode,
                title = "Theme",
                trailingValue = when (themeMode) {
                    ThemeMode.DARK -> "Obsidian Dark"
                    ThemeMode.LIGHT -> "Editorial Light"
                    ThemeMode.SYSTEM -> "System"
                    ThemeMode.LIQUID_GLASS -> "Liquid Glass"
                },
                onClick = { showThemeDialog = true },
                testTag = "settings-theme-row"
            )
            HorizontalDivider(thickness = 0.5.dp, color = ChaiTheme.extended.border, modifier = Modifier.padding(vertical = 8.dp))
        }

        // ==========================================
        // SECTION: GENERAL
        // ==========================================
        item {
            SettingsSectionHeader(title = "GENERAL")
            SettingsToggleRow(
                title = "Auto-play videos",
                subtitle = "वीडियो स्वतः प्रारंभ करें",
                checked = autoPlayVideos,
                onCheckedChange = { settingsRepo.setAutoPlayVideos(it) },
                testTag = "settings-autoplay-switch"
            )
            SettingsToggleRow(
                title = "Save articles",
                subtitle = "ऑफलाइन पढ़ने हेतु सहेजें",
                checked = saveArticles,
                onCheckedChange = { settingsRepo.setSaveArticles(it) },
                testTag = "settings-savearticles-switch"
            )
            SettingsNavRow(
                icon = Icons.Outlined.CleaningServices,
                title = "Clear cache",
                trailingValue = cacheSize,
                onClick = { showClearCacheConfirm = true },
                testTag = "settings-clearcache-row"
            )
            HorizontalDivider(thickness = 0.5.dp, color = ChaiTheme.extended.border, modifier = Modifier.padding(vertical = 8.dp))
        }

        // ==========================================
        // SECTION: ABOUT
        // ==========================================
        item {
            SettingsSectionHeader(title = "ABOUT")
            SettingsNavRow(
                icon = Icons.Outlined.Info,
                title = "About Chai Panchayat",
                onClick = { showAboutDialog = true },
                testTag = "settings-about-row"
            )
            SettingsNavRow(
                icon = Icons.Outlined.PrivacyTip,
                title = "Privacy Policy",
                onClick = { showPrivacyDialog = true },
                testTag = "settings-privacy-row"
            )
            SettingsNavRow(
                icon = Icons.Outlined.VerifiedUser,
                title = "Terms",
                onClick = { showTermsDialog = true },
                testTag = "settings-terms-row"
            )
            SettingsInfoRow(
                title = "App Version",
                value = "2.0 (Chai Editorial)"
            )
        }
    }

    // ==========================================
    // DIALOGS & ACTION SHEETS
    // ==========================================

    // Profile Dialog
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Text(
                    text = "पाठक प्रोफ़ाइल",
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "चाय पंचायत पाठक",
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "सदस्यता: सक्रिय पाठक (गोरखपुर एवं पूर्वांचल डेस्क)",
                        fontFamily = InterFamily,
                        fontSize = 13.sp,
                        color = ChaiTheme.extended.muted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "सच्ची, बेबाक और जनहितकारी पत्रकारिता के साथ जुड़े रहने के लिए धन्यवाद।",
                        fontFamily = InterFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("ठीक है", color = ChaiSaffron, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Notifications Dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Text(
                    text = "सूचनाएं एवं अपडेट्स",
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    SettingsToggleRow(
                        title = "पुश सूचनाएं",
                        subtitle = "बैकग्राउंड में नई खबरों की जांच",
                        checked = pushNotifications,
                        onCheckedChange = { settingsRepo.setPushNotifications(it) }
                    )
                    SettingsToggleRow(
                        title = "ब्रेकिंग अलर्ट्स",
                        subtitle = "महत्वपूर्ण तात्कालिक घटनाएं",
                        checked = breakingAlerts,
                        onCheckedChange = { settingsRepo.setBreakingAlerts(it) }
                    )
                    SettingsToggleRow(
                        title = "आज की चाय डाइजेस्ट",
                        subtitle = "सुबह का दैनिक बुलेटिन",
                        checked = dailyDigest,
                        onCheckedChange = { settingsRepo.setDailyDigest(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isCheckingNow = true
                            ChaiNewsNotificationWorker.checkOnceNow(context)
                            Toast.makeText(context, "खबरों की जांच की जा रही है...", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(1200)
                                isCheckingNow = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ChaiSaffron),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isCheckingNow) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Outlined.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("ताज़ा खबरों की तुरंत जांच करें", fontFamily = InterFamily, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("पूर्ण", color = ChaiSaffron, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = "भाषा चुनें",
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    listOf("हिन्दी", "English").forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsRepo.setLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = language == lang,
                                onClick = {
                                    settingsRepo.setLanguage(lang)
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = ChaiSaffron)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = lang,
                                fontFamily = InterFamily,
                                fontWeight = if (language == lang) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("रद्द करें", color = ChaiTheme.extended.muted)
                }
            }
        )
    }

    // Text Size Dialog
    if (showTextSizeDialog) {
        AlertDialog(
            onDismissRequest = { showTextSizeDialog = false },
            title = {
                Text(
                    text = "Text size",
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    listOf(
                        TextSizePreference.SMALL to "Small",
                        TextSizePreference.MEDIUM to "Medium",
                        TextSizePreference.LARGE to "Large",
                        TextSizePreference.EXTRA_LARGE to "Extra Large"
                    ).forEach { (pref, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsRepo.setTextSize(pref)
                                    showTextSizeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = textSize == pref,
                                onClick = {
                                    settingsRepo.setTextSize(pref)
                                    showTextSizeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = ChaiSaffron)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                fontFamily = InterFamily,
                                fontWeight = if (textSize == pref) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTextSizeDialog = false }) {
                    Text("रद्द करें", color = ChaiTheme.extended.muted)
                }
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text(
                    text = "थीम चुनें",
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    listOf(
                        ThemeMode.DARK to "Obsidian Dark (डार्क - डिफ़ॉल्ट)",
                        ThemeMode.LIGHT to "Editorial Light (लाइट)",
                        ThemeMode.SYSTEM to "System (सिस्टम अनुसार)",
                        ThemeMode.LIQUID_GLASS to "Liquid Glass (प्रायोगिक)"
                    ).forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsRepo.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    settingsRepo.setThemeMode(mode)
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = ChaiSaffron)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                fontFamily = InterFamily,
                                fontWeight = if (themeMode == mode) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("रद्द करें", color = ChaiTheme.extended.muted)
                }
            }
        )
    }

    // Clear Cache Confirm Dialog
    if (showClearCacheConfirm) {
        AlertDialog(
            onDismissRequest = { showClearCacheConfirm = false },
            title = {
                Text(text = "कैशे साफ़ करें?", fontFamily = NotoSerifFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "क्या आप संग्रहीत अस्थायी छवियां और डेटा साफ़ करना चाहते हैं? इससे लगभग $cacheSize मेमोरी मुक्त होगी।",
                    fontFamily = InterFamily,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearCacheConfirm = false
                        isClearingCache = true
                        coroutineScope.launch {
                            settingsRepo.clearApplicationCache()
                            isClearingCache = false
                            Toast.makeText(context, "कैशे साफ़ कर दिया गया", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("साफ़ करें", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheConfirm = false }) {
                    Text("रद्द करें", color = ChaiTheme.extended.muted)
                }
            }
        )
    }

    // About Chai Panchayat Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(
                    text = "चाय पंचायत के बारे में",
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "चाय पंचायत (Chai Panchayat)",
                        fontFamily = NotoSerifFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ChaiSaffron
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "उत्तर प्रदेश एवं पूर्वांचल की विश्वसनीय आवाज़। निष्पक्ष पत्रकारिता, ग्राउंड रिपोर्ट्स और सामयिक विश्लेषण का डिजिटल मंच।",
                        fontFamily = InterFamily,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "संस्करण: 2.0 (Chai Editorial)\nमुख्यालय: गोरखपुर, उत्तर प्रदेश",
                        fontFamily = InterFamily,
                        fontSize = 12.sp,
                        color = ChaiTheme.extended.muted
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("ठीक है", color = ChaiSaffron, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(
                    text = "गोपनीयता नीति (Privacy Policy)",
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "आपकी गोपनीयता हमारे लिए सर्वोपरि है। चाय पंचायत ऐप किसी भी प्रकार का व्यक्तिगत डेटा अनधिकृत रूप से तीसरे पक्ष के साथ साझा नहीं करता। सभी सहेजे गए लेख एवं थीम सेटिंग्स आपके उपकरण में स्थानीय रूप से सुरक्षित रहते हैं।",
                        fontFamily = InterFamily,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("स्वीकार है", color = ChaiSaffron, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Terms Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = "नियम एवं शर्तें (Terms)",
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "चाय पंचायत पर प्रकाशित सभी समाचार, वीडियो और सामग्री कॉपीराइट द्वारा संरक्षित हैं। सामग्री का उपयोग केवल व्यक्तिगत और गैर-व्यावसायिक पढ़ने के लिए किया जा सकता है।",
                        fontFamily = InterFamily,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("स्वीकार है", color = ChaiSaffron, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ==========================================
// SUB-COMPONENTS FOR SPEC-COMPLIANT SETTINGS
// ==========================================

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp,
        color = ChaiTheme.extended.muted,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector? = null,
    title: String,
    subtitle: String? = null,
    trailingValue: String? = null,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ChaiTheme.extended.muted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = InterFamily,
                    fontSize = 12.sp,
                    color = ChaiTheme.extended.muted
                )
            }
        }

        if (trailingValue != null) {
            Text(
                text = trailingValue,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.5.sp,
                color = ChaiSaffron
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = ChaiTheme.extended.muted.copy(alpha = 0.6f),
            modifier = Modifier.size(13.dp)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = InterFamily,
                    fontSize = 12.sp,
                    color = ChaiTheme.extended.muted
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ChaiSaffron,
                uncheckedThumbColor = ChaiTheme.extended.muted,
                uncheckedTrackColor = ChaiTheme.extended.surfaceSecondary
            )
        )
    }
}

@Composable
private fun SettingsInfoRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontFamily = InterFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = value,
            fontFamily = InterFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.5.sp,
            color = ChaiTheme.extended.muted
        )
    }
}
