package com.example.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WebBlockEntity
import com.example.data.model.WebsiteEntity
import com.example.generator.SiteCompiler
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo

enum class EditorViewMode {
    SECTIONS,
    SPLIT,
    FULL_PREVIEW
}

@Composable
fun EditorScreen(
    website: WebsiteEntity?,
    blocks: List<WebBlockEntity>,
    onEditBlock: (WebBlockEntity) -> Unit,
    onAddBlockClick: () -> Unit,
    onMoveUp: (WebBlockEntity) -> Unit,
    onMoveDown: (WebBlockEntity) -> Unit,
    onDuplicate: (WebBlockEntity) -> Unit,
    onDelete: (WebBlockEntity) -> Unit,
    onToggleVisibility: (WebBlockEntity) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTemplateSelection: () -> Unit = {},
    onOpenThemeCustomizer: () -> Unit = {},
    onReorderBlocks: ((List<WebBlockEntity>) -> Unit)? = null,
    onUpdateBlock: (WebBlockEntity) -> Unit = onEditBlock,
    selectedPageSlug: String = "index",
    onSelectPage: (String) -> Unit = {},
    onAddPage: (title: String, slug: String) -> Unit = { _, _ -> },
    onRemovePage: (String) -> Unit = {},
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    undoActionTitle: String? = null,
    redoActionTitle: String? = null
) {
    var localBlocks by remember(blocks) { mutableStateOf(blocks) }
    var viewMode by remember { mutableStateOf(EditorViewMode.SECTIONS) }
    var showAssetManager by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    val pages = remember(website?.pagesJson) {
        SiteCompiler.parsePages(website?.pagesJson ?: "")
    }

    val pageBlocks = remember(localBlocks, selectedPageSlug) {
        localBlocks.filter { block ->
            val slug = block.pageSlug.trim()
            slug == "all" ||
            (selectedPageSlug == "index" && (slug.isEmpty() || slug == "index")) ||
            (selectedPageSlug != "index" && slug == selectedPageSlug)
        }
    }

    var showAddPageDialog by remember { mutableStateOf(false) }
    var newPageTitle by remember { mutableStateOf("") }
    var newPageSlug by remember { mutableStateOf("") }

    val dragDropState = rememberDragDropState(
        lazyListState = lazyListState,
        headerItemCount = 2,
        onMove = { fromIndex, toIndex ->
            if (fromIndex in pageBlocks.indices && toIndex in pageBlocks.indices) {
                val fromBlock = pageBlocks[fromIndex]
                val toBlock = pageBlocks[toIndex]
                val globalFrom = localBlocks.indexOfFirst { it.id == fromBlock.id }
                val globalTo = localBlocks.indexOfFirst { it.id == toBlock.id }
                if (globalFrom != -1 && globalTo != -1) {
                    val updated = localBlocks.toMutableList()
                    val moved = updated.removeAt(globalFrom)
                    updated.add(globalTo, moved)
                    localBlocks = updated
                }
            }
        },
        onDragEnd = {
            onReorderBlocks?.invoke(localBlocks)
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (viewMode == EditorViewMode.FULL_PREVIEW) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Full Live Website Preview",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedButton(
                        onClick = { viewMode = EditorViewMode.SECTIONS },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Back to Sections", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }

                EditorLivePreviewPane(
                    website = website,
                    blocks = localBlocks,
                    isExpanded = true,
                    activePageSlug = selectedPageSlug,
                    onSelectPage = onSelectPage,
                    onClose = { viewMode = EditorViewMode.SECTIONS },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .dragContainer(dragDropState, enabled = localBlocks.isNotEmpty()),
                        contentPadding = PaddingValues(top = 16.dp, bottom = if (viewMode == EditorViewMode.SPLIT) 16.dp else 96.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = website?.title ?: "Website",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(BrandIndigo.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${localBlocks.size} Sections",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = BrandIndigo
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(BrandCyan.copy(alpha = 0.15f))
                                            .clickable { onOpenThemeCustomizer() }
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Palette,
                                                contentDescription = null,
                                                tint = BrandCyan,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Text(
                                                text = website?.themePreset ?: "modern-dark",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = BrandCyan
                                            )
                                        }
                                    }
                                    if (!website?.fontFamily.isNullOrBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable { onOpenThemeCustomizer() }
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = website!!.fontFamily.substringBefore(","),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = onUndo,
                                    enabled = canUndo,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("button_editor_undo")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Undo,
                                        contentDescription = if (!undoActionTitle.isNullOrBlank()) "Undo: $undoActionTitle" else "Undo",
                                        tint = if (canUndo) BrandIndigo else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onRedo,
                                    enabled = canRedo,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("button_editor_redo")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Redo,
                                        contentDescription = if (!redoActionTitle.isNullOrBlank()) "Redo: $redoActionTitle" else "Redo",
                                        tint = if (canRedo) BrandIndigo else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                OutlinedButton(
                                    onClick = onOpenThemeCustomizer,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("button_open_theme_customizer")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = "Customize Theme & Styles",
                                        tint = BrandIndigo,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Theme",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                OutlinedButton(
                                    onClick = onOpenTemplateSelection,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("button_switch_template")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Choose Template",
                                        tint = BrandCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Templates",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showAssetManager = true },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("button_open_asset_manager")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "Manage Photos & Assets",
                                        tint = BrandIndigo,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Photos",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                IconButton(
                                    onClick = onOpenSettings,
                                    modifier = Modifier.testTag("button_open_settings")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Website Settings",
                                        tint = BrandIndigo
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // In-Editor Mode Toggle: Sections vs Split Live Pane vs Full Live Preview
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (viewMode == EditorViewMode.SECTIONS) BrandIndigo else Color.Transparent)
                                        .clickable { viewMode = EditorViewMode.SECTIONS }
                                        .padding(vertical = 8.dp)
                                        .testTag("tab_editor_sections"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription = null,
                                            tint = if (viewMode == EditorViewMode.SECTIONS) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Sections (${localBlocks.size})",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (viewMode == EditorViewMode.SECTIONS) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (viewMode == EditorViewMode.SECTIONS) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (viewMode == EditorViewMode.SPLIT) BrandIndigo else Color.Transparent)
                                        .clickable { viewMode = EditorViewMode.SPLIT }
                                        .padding(vertical = 8.dp)
                                        .testTag("tab_editor_split_pane"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = if (viewMode == EditorViewMode.SPLIT) Color.White else BrandCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Split Live",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (viewMode == EditorViewMode.SPLIT) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (viewMode == EditorViewMode.SPLIT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (viewMode == EditorViewMode.FULL_PREVIEW) BrandIndigo else Color.Transparent)
                                        .clickable { viewMode = EditorViewMode.FULL_PREVIEW }
                                        .padding(vertical = 8.dp)
                                        .testTag("tab_editor_full_preview"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = if (viewMode == EditorViewMode.FULL_PREVIEW) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Full Preview",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (viewMode == EditorViewMode.FULL_PREVIEW) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (viewMode == EditorViewMode.FULL_PREVIEW) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Multi-Page Selector Bar
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("editor_page_tabs_bar")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "PAGES:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 2.dp)
                                )
                                for (pg in pages) {
                                    val isCurrentPage = pg.slug == selectedPageSlug
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isCurrentPage) BrandIndigo else MaterialTheme.colorScheme.surface,
                                        border = if (isCurrentPage) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .clickable { onSelectPage(pg.slug) }
                                            .testTag("page_tab_${pg.slug}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = if (pg.slug == "index") "🏠 ${pg.title}" else "📄 ${pg.title}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isCurrentPage) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isCurrentPage) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (pg.slug != "index") {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove page ${pg.title}",
                                                    tint = if (isCurrentPage) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier
                                                        .size(13.dp)
                                                        .clickable {
                                                            onRemovePage(pg.slug)
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Add Page button
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, BrandIndigo.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .clickable {
                                            newPageTitle = ""
                                            newPageSlug = ""
                                            showAddPageDialog = true
                                        }
                                        .testTag("button_add_page")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = BrandIndigo,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Add Page",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = BrandIndigo
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Subheader with Instructions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentPageName = pages.firstOrNull { it.slug == selectedPageSlug }?.title ?: "Home"
                    Text(
                        text = "$currentPageName Sections (${pageBlocks.size})".uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Drag ⠿ handle to reorder in real-time",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = BrandCyan
                    )
                }
            }

            // Blocks List
            if (pageBlocks.isEmpty()) {
                item {
                    EmptyBlocksView(
                        onAddBlockClick = onAddBlockClick,
                        onOpenTemplateSelection = onOpenTemplateSelection
                    )
                }
            } else {
                itemsIndexed(
                    items = pageBlocks,
                    key = { _, block -> block.id }
                ) { index, block ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .draggableItemLayer(dragDropState, index)
                    ) {
                        BlockItemCard(
                            index = index,
                            totalCount = pageBlocks.size,
                            block = block,
                            dragDropState = dragDropState,
                            onClick = { onEditBlock(block) },
                            onMoveUp = { onMoveUp(block) },
                            onMoveDown = { onMoveDown(block) },
                            onDuplicate = { onDuplicate(block) },
                            onDelete = { onDelete(block) },
                            onToggleVisibility = { onToggleVisibility(block) }
                        )
                    }
                }
            }
        }
    }

    if (viewMode == EditorViewMode.SPLIT) {
        // Bottom pane: In-Editor Live Preview Pane that renders real-time changes as users drag and drop
        Box(
            modifier = Modifier
                .weight(1.15f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            EditorLivePreviewPane(
                website = website,
                blocks = localBlocks,
                isDragging = dragDropState.isDragging,
                draggingItemIndex = dragDropState.draggingItemIndex,
                activePageSlug = selectedPageSlug,
                onSelectPage = onSelectPage,
                onClose = { viewMode = EditorViewMode.SECTIONS },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
}

        // Active dragging status pill
        AnimatedVisibility(
            visible = dragDropState.isDragging,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = BrandIndigo,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, BrandCyan)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = null,
                        tint = BrandCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val currentPos = (dragDropState.draggingItemIndex ?: 0) + 1
                    Text(
                        text = "Moving section to position #$currentPos • Release to place",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        if (viewMode != EditorViewMode.FULL_PREVIEW) {
            FloatingActionButton(
                onClick = onAddBlockClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("fab_add_block"),
                containerColor = BrandIndigo,
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Section")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Section", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showAssetManager) {
            TemplateAssetManagerSheet(
                website = website,
                blocks = localBlocks,
                onUpdateBlock = { updated ->
                    val newBlocks = localBlocks.map { if (it.id == updated.id) updated else it }
                    localBlocks = newBlocks
                    onUpdateBlock(updated)
                },
                onDismiss = { showAssetManager = false }
            )
        }

        if (showAddPageDialog) {
            AlertDialog(
                onDismissRequest = { showAddPageDialog = false },
                title = { Text("Add New Page", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Create a secondary page (e.g. About, Contact, Portfolio). Sections can be placed specifically on this page.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = newPageTitle,
                            onValueChange = {
                                newPageTitle = it
                                if (newPageSlug.isBlank() || newPageSlug == newPageTitle.lowercase().dropLast(1)) {
                                    newPageSlug = it.lowercase().trim().replace(Regex("[^a-z0-9]+"), "-")
                                }
                            },
                            label = { Text("Page Title") },
                            placeholder = { Text("e.g. About Us, Services, Portfolio") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_new_page_title"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = newPageSlug,
                            onValueChange = { newPageSlug = it.lowercase().replace(" ", "-") },
                            label = { Text("URL Slug (e.g. about, services)") },
                            placeholder = { Text("e.g. about") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_new_page_slug"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPageTitle.isNotBlank()) {
                                val cleanSlug = newPageSlug.ifBlank {
                                    newPageTitle.lowercase().trim().replace(Regex("[^a-z0-9]+"), "-")
                                }
                                onAddPage(newPageTitle, cleanSlug)
                                showAddPageDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("confirm_create_page_button")
                    ) {
                        Text("Create Page", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showAddPageDialog = false },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun BlockItemCard(
    index: Int,
    totalCount: Int,
    block: WebBlockEntity,
    dragDropState: DragDropState,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onToggleVisibility: () -> Unit
) {
    val icon = getIconForBlockType(block.type)
    val isDimmed = !block.isVisible
    val isDragging = dragDropState.draggingItemIndex == index

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("block_card_${block.orderIndex}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging)
                BrandIndigo.copy(alpha = 0.18f)
            else if (isDimmed)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isDragging) 2.dp else 1.dp,
            color = if (isDragging) BrandCyan else if (isDimmed) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drag & Drop Handle with immediate vertical drag
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isDragging) BrandCyan.copy(alpha = 0.25f)
                            else BrandIndigo.copy(alpha = 0.12f)
                        )
                        .dragHandleGesture(dragDropState, index)
                        .testTag("block_drag_handle_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Drag to move section #${index + 1}",
                        tint = if (isDragging) BrandCyan else BrandIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isDragging) BrandCyan else BrandIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "#${index + 1} ${block.type.displayName}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDragging) BrandCyan else if (isDimmed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                        )
                        if (block.pageSlug == "all") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = BrandIndigo.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "🌐 Global",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BrandIndigo,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        } else if (block.pageSlug.isNotBlank() && block.pageSlug != "index") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "📄 ${block.pageSlug}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        if (block.animationEffect.isNotBlank() && block.animationEffect != "default" && block.animationEffect != "none") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = BrandCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "✨ ${block.animationEffect}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = BrandCyan,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        if (!block.isVisible) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(Hidden)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    val previewText = block.title.ifBlank { block.content.take(60) }.ifBlank { "Tap to configure content" }
                    Text(
                        text = previewText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (block.buttonText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = BrandIndigo.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, BrandIndigo.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = BrandIndigo,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                val target = if (block.buttonUrl.isNotBlank()) block.buttonUrl else "#contact"
                                Text(
                                    text = "${block.buttonText} ➔ $target",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                    color = BrandIndigo,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Up / Down reorder controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Move Up",
                            tint = if (index > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Move Down",
                            tint = if (index < totalCount - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action toolbar for block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (block.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Visibility",
                            tint = if (block.isVisible) BrandEmerald else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate Section",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Section",
                            tint = BrandIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Section",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyBlocksView(
    onAddBlockClick: () -> Unit,
    onOpenTemplateSelection: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = BrandIndigo,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Your Website is Blank",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Start from a pre-built template or build custom block by block.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onOpenTemplateSelection,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("empty_state_browse_templates_button")
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Browse Templates", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onAddBlockClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("empty_state_add_section_button")
                ) {
                    Text("Add Section")
                }
            }
        }
    }
}
