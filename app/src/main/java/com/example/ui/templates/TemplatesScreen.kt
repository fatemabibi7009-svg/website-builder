package com.example.ui.templates

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator.TemplateDefinition
import com.example.generator.WebsiteTemplates
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo

@Composable
fun TemplatesScreen(
    onSelectTemplate: (TemplateDefinition) -> Unit,
    onExportJson: () -> String,
    onImportJson: (String) -> Boolean,
    isPremierUnlocked: Boolean = false,
    onWatchAdToUnlock: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }

    var isGridView by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var previewTemplate by remember { mutableStateOf<TemplateDefinition?>(null) }
    var showWatchAdDialog by remember { mutableStateOf(false) }
    var pendingPremierTemplate by remember { mutableStateOf<TemplateDefinition?>(null) }

    val categories = listOf(
        "All",
        "🎌 Anime & 4-Bit Pixel",
        "👑 Premier & WhatsApp",
        "Cyber & Gaming",
        "Luxury & Fashion",
        "Fitness & Health",
        "Medical & Dental",
        "Real Estate & Villas",
        "Music & Festival",
        "Media & Podcast",
        "Hospitality",
        "Startup & Tech",
        "Agency & Studio",
        "E-Commerce",
        "Portfolio",
        "Blog",
        "Creator",
        "Developer",
        "Blank Canvas"
    )

    val filteredTemplates = remember(selectedCategory, searchQuery) {
        WebsiteTemplates.allTemplates.filter { tmpl ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "🎌 Anime & 4-Bit Pixel" -> tmpl.category.contains("Anime", ignoreCase = true) ||
                    tmpl.category.contains("4-Bit", ignoreCase = true) ||
                    tmpl.id.contains("anime", ignoreCase = true) ||
                    tmpl.id.contains("4bit", ignoreCase = true) ||
                    tmpl.id.contains("pixel", ignoreCase = true) ||
                    tmpl.name.contains("Anime", ignoreCase = true) ||
                    tmpl.name.contains("4-Bit", ignoreCase = true)
                "👑 Premier & WhatsApp" -> tmpl.isPremier || tmpl.id.contains("whatsapp") || tmpl.category.contains("Premier")
                "Cyber & Gaming" -> tmpl.category.contains("Gaming", ignoreCase = true) || tmpl.id.contains("cyber")
                "Luxury & Fashion" -> tmpl.category.contains("Luxury", ignoreCase = true) || tmpl.id.contains("luxury")
                "Fitness & Health" -> tmpl.category.contains("Fitness", ignoreCase = true) || tmpl.id.contains("fitness")
                "Medical & Dental" -> tmpl.category.contains("Medical", ignoreCase = true) || tmpl.id.contains("dental") || tmpl.id.contains("clinic")
                "Real Estate & Villas" -> tmpl.category.contains("Real Estate", ignoreCase = true) || tmpl.id.contains("villa") || tmpl.id.contains("estate")
                "Music & Festival" -> tmpl.category.contains("Music", ignoreCase = true) || tmpl.id.contains("festival") || tmpl.id.contains("concert")
                "Media & Podcast" -> tmpl.category.contains("Podcast", ignoreCase = true) || tmpl.id.contains("podcast")
                "Startup & Tech" -> tmpl.category.contains("Startup", ignoreCase = true) || tmpl.id.contains("saas")
                "Agency & Studio" -> tmpl.category.contains("Agency", ignoreCase = true) || tmpl.id.contains("agency")
                "E-Commerce" -> tmpl.category.contains("Commerce", ignoreCase = true) || tmpl.id.contains("store")
                "Portfolio" -> tmpl.category.contains("Creative", ignoreCase = true) || tmpl.id.contains("portfolio")
                "Blog" -> tmpl.category.contains("Blog", ignoreCase = true) || tmpl.id.contains("blog")
                "Hospitality" -> tmpl.category.contains("Hospitality", ignoreCase = true) || tmpl.id.contains("cafe") || tmpl.id.contains("matcha")
                "Creator" -> tmpl.category.contains("Creator", ignoreCase = true) || tmpl.id.contains("link")
                "Developer" -> tmpl.category.contains("Resume", ignoreCase = true) || tmpl.id.contains("resume")
                "Blank Canvas" -> tmpl.category.contains("Custom", ignoreCase = true) || tmpl.id.contains("blank")
                else -> true
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                tmpl.name.lowercase().contains(q) ||
                    tmpl.description.lowercase().contains(q) ||
                    tmpl.category.lowercase().contains(q) ||
                    tmpl.themePreset.lowercase().contains(q) ||
                    tmpl.badge.lowercase().contains(q)
            }

            matchesCategory && matchesSearch
        }
    }

    // Top Header content including Open-Source banner, search bar, and category chips
    val headerSection: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Open Source & Community Hub Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BrandIndigo.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, BrandIndigo.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = BrandAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "100% Free & Open-Source",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(BrandEmerald.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "MIT License",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = BrandEmerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Built for community growth and customization. Export website schemas as JSON to share with friends, or import custom community templates instantly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                exportedJsonText = onExportJson()
                                showExportDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                importJsonText = ""
                                showImportDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo)
                        ) {
                            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import JSON", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Premier Status Banner (Ad-gated unlock trigger)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("premier_status_banner_templates"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPremierUnlocked) BrandEmerald.copy(alpha = 0.12f) else BrandAmber.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, if (isPremierUnlocked) BrandEmerald.copy(alpha = 0.45f) else BrandAmber.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isPremierUnlocked) BrandEmerald else BrandAmber),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPremierUnlocked) Icons.Default.Check else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isPremierUnlocked) "👑 Premier Stores Unlocked" else "👑 Premier Section • Watch 1 Ad to Unlock",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isPremierUnlocked) "Full WhatsApp shopping app with multi-step checkout & photo upload active." else "Unlock multi-step WhatsApp store, interactive cart & custom photo upload.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!isPremierUnlocked) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showWatchAdDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandAmber),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_unlock_premier_ad_templates")
                        ) {
                            Text("Watch Ad", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search templates (e.g., SaaS, Store, Portfolio)...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = BrandIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandIndigo,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("templates_search_input")
            )

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) BrandIndigo else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Template Header and Grid/List view Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredTemplates.size} TEMPLATES AVAILABLE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isGridView) BrandIndigo else Color.Transparent)
                                .clickable { isGridView = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("toggle_grid_mode_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "Grid View",
                                    tint = if (isGridView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Grid",
                                    fontSize = 11.sp,
                                    fontWeight = if (isGridView) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isGridView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isGridView) BrandIndigo else Color.Transparent)
                                .clickable { isGridView = false }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("toggle_list_mode_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ViewList,
                                    contentDescription = "List View",
                                    tint = if (!isGridView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "List",
                                    fontSize = 11.sp,
                                    fontWeight = if (!isGridView) FontWeight.Bold else FontWeight.Normal,
                                    color = if (!isGridView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val handleSelectWithAdGate: (TemplateDefinition) -> Unit = { template ->
        if (template.isPremier && !isPremierUnlocked) {
            pendingPremierTemplate = template
            showWatchAdDialog = true
        } else {
            onSelectTemplate(template)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isGridView) {
            TemplateThumbnailGrid(
                templates = filteredTemplates,
                onSelectTemplate = handleSelectWithAdGate,
                onPreviewTemplate = { previewTemplate = it },
                modifier = Modifier.fillMaxSize(),
                isPremierUnlocked = isPremierUnlocked,
                headerContent = headerSection
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    headerSection()
                }

                // Templates Cards in List Mode with preview buttons
                items(filteredTemplates) { template ->
                    TemplateCard(
                        template = template,
                        isPremierUnlocked = isPremierUnlocked,
                        onApply = { handleSelectWithAdGate(template) },
                        onPreview = { previewTemplate = template }
                    )
                }
            }
        }

        // Full Interactive Template Preview Modal (with dedicated 4-Bit Quick-Look Modal)
        previewTemplate?.let { template ->
            if (WebsiteTemplates.is4BitAnime(template)) {
                Anime4BitQuickLookModal(
                    template = template,
                    all4BitTemplates = WebsiteTemplates.allTemplates.filter { WebsiteTemplates.is4BitAnime(it) },
                    isPremierUnlocked = isPremierUnlocked,
                    onDismiss = { previewTemplate = null },
                    onSelectTemplate = { previewTemplate = it },
                    onApply = {
                        val tmpl = previewTemplate ?: template
                        previewTemplate = null
                        handleSelectWithAdGate(tmpl)
                    }
                )
            } else {
                TemplatePreviewDialog(
                    template = template,
                    onDismiss = { previewTemplate = null },
                    onSelect = {
                        val tmpl = template
                        previewTemplate = null
                        handleSelectWithAdGate(tmpl)
                    }
                )
            }
        }
    }

    // Watch Ad Dialog for Premier Templates
    if (showWatchAdDialog) {
        WatchAdDialog(
            onDismiss = {
                showWatchAdDialog = false
                pendingPremierTemplate = null
            },
            onRewardEarned = {
                onWatchAdToUnlock()
                pendingPremierTemplate?.let { tmpl ->
                    onSelectTemplate(tmpl)
                    pendingPremierTemplate = null
                }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Text(
                    text = "Exported Website Schema",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Copy this JSON to backup your design or share with other users in the community:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportedJsonText))
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Website Template JSON")
                            putExtra(Intent.EXTRA_TEXT, exportedJsonText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Template JSON"))
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo)
                ) {
                    Text("Copy & Share")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = {
                Text(
                    text = "Import Community Template",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Paste a valid Web Builder JSON schema below:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("{\"title\": \"...\", \"blocks\": [...]}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            onImportJson(importJsonText)
                        }
                        showImportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo)
                ) {
                    Text("Import Template")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TemplateCard(
    template: TemplateDefinition,
    isPremierUnlocked: Boolean = false,
    onApply: () -> Unit,
    onPreview: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreview?.invoke() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandIndigo.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = BrandIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = template.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (template.isPremier) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isPremierUnlocked) BrandEmerald.copy(alpha = 0.2f) else BrandAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isPremierUnlocked) Icons.Default.Check else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isPremierUnlocked) BrandEmerald else BrandAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isPremierUnlocked) "PREMIER UNLOCKED" else "PREMIER",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isPremierUnlocked) BrandEmerald else BrandAmber
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BrandCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = template.badge,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = BrandCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = template.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Theme: ${template.themePreset}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onPreview != null) {
                        OutlinedButton(
                            onClick = onPreview,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("preview_template_btn_${template.id}")
                        ) {
                            Icon(
                                imageVector = if (WebsiteTemplates.is4BitAnime(template)) Icons.Default.AutoAwesome else Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (WebsiteTemplates.is4BitAnime(template)) BrandAmber else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (WebsiteTemplates.is4BitAnime(template)) "4-Bit Quick-Look" else "Preview",
                                color = if (WebsiteTemplates.is4BitAnime(template)) BrandAmber else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Button(
                        onClick = onApply,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (template.isPremier && !isPremierUnlocked) BrandAmber else BrandIndigo
                        ),
                        modifier = Modifier.testTag("apply_template_${template.id}")
                    ) {
                        if (template.isPremier && !isPremierUnlocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Unlock (Watch Ad)", fontWeight = FontWeight.Bold, color = Color.Black)
                        } else {
                            Text("Use Template", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
