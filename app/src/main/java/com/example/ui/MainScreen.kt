package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.deploy.DeployScreen
import com.example.ui.editor.AddBlockBottomSheet
import com.example.ui.editor.BlockEditorBottomSheet
import com.example.ui.editor.EditorScreen
import com.example.ui.editor.SiteSettingsDialog
import com.example.ui.localhost.LocalhostScreen
import com.example.ui.preview.PreviewScreen
import com.example.ui.templates.TemplateSelectionScreen
import com.example.ui.templates.TemplatesScreen
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val viewportMode by viewModel.viewportMode.collectAsState()
    val currentWebsite by viewModel.currentWebsite.collectAsState()
    val blocks by viewModel.blocks.collectAsState()
    val compiledSite by viewModel.compiledSite.collectAsState()
    val editingBlock by viewModel.editingBlock.collectAsState()
    val isAddBlockSheetOpen by viewModel.isAddBlockSheetOpen.collectAsState()
    val isSiteSettingsOpen by viewModel.isSiteSettingsOpen.collectAsState()
    val isTemplateSelectionActive by viewModel.isTemplateSelectionActive.collectAsState()
    val serverRunning by viewModel.serverRunning.collectAsState()
    val serverPort by viewModel.serverPort.collectAsState()
    val serverLocalIp by viewModel.serverLocalIp.collectAsState()
    val serverLogs by viewModel.serverLogs.collectAsState()
    val netlifyToken by viewModel.netlifyToken.collectAsState()
    val netlifyUser by viewModel.netlifyUser.collectAsState()
    val customSiteName by viewModel.customSiteName.collectAsState()
    val deployProgress by viewModel.deployProgress.collectAsState()
    val deployments by viewModel.deployments.collectAsState()
    val distZip by viewModel.distZip.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    if (isTemplateSelectionActive) {
        TemplateSelectionScreen(
            currentWebsite = currentWebsite,
            onSelectTemplate = { template ->
                viewModel.selectTemplateAndEnterEditor(template)
            },
            onContinueExisting = {
                viewModel.dismissTemplateSelection()
            }
        )
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrandIndigo),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Web Builder",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Server Indicator Pill
                            if (serverRunning) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(BrandEmerald.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(BrandEmerald)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = ":$serverPort",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = BrandEmerald
                                        )
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.showTemplateSelection() },
                            modifier = Modifier.testTag("topbar_templates_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Choose Template",
                                tint = BrandCyan
                            )
                        }

                        IconButton(
                            onClick = { viewModel.openSiteSettings() },
                            modifier = Modifier.testTag("topbar_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Website Settings",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    NavigationTabItem(NavigationTab.EDITOR, Icons.Default.Dashboard, "Editor"),
                    NavigationTabItem(NavigationTab.TEMPLATES, Icons.Default.AutoAwesome, "Templates"),
                    NavigationTabItem(NavigationTab.PREVIEW, Icons.Default.Devices, "Preview"),
                    NavigationTabItem(NavigationTab.LOCALHOST, Icons.Default.Dns, "Localhost", hasBadge = serverRunning),
                    NavigationTabItem(NavigationTab.DEPLOY, Icons.Default.RocketLaunch, "Dist/Deploy")
                )

                tabs.forEach { item ->
                    NavigationBarItem(
                        selected = selectedTab == item.tab,
                        onClick = { viewModel.selectTab(item.tab) },
                        icon = {
                            if (item.hasBadge) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = BrandEmerald)
                                    }
                                ) {
                                    Icon(imageVector = item.icon, contentDescription = item.label)
                                }
                            } else {
                                Icon(imageVector = item.icon, contentDescription = item.label)
                            }
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == item.tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandIndigo,
                            selectedTextColor = BrandIndigo,
                            indicatorColor = BrandIndigo.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${item.tab.name.lowercase()}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    NavigationTab.EDITOR -> {
                        EditorScreen(
                            website = currentWebsite,
                            blocks = blocks,
                            onEditBlock = { viewModel.editBlock(it) },
                            onAddBlockClick = { viewModel.openAddBlockSheet() },
                            onMoveUp = { viewModel.moveBlockUp(it) },
                            onMoveDown = { viewModel.moveBlockDown(it) },
                            onDuplicate = { viewModel.duplicateBlock(it) },
                            onDelete = { viewModel.deleteBlock(it) },
                            onToggleVisibility = { viewModel.toggleBlockVisibility(it) },
                            onOpenSettings = { viewModel.openSiteSettings() },
                            onOpenTemplateSelection = { viewModel.showTemplateSelection() },
                            onReorderBlocks = { viewModel.reorderBlocks(it) }
                        )
                    }
                    NavigationTab.TEMPLATES -> {
                        TemplatesScreen(
                            onSelectTemplate = { viewModel.applyTemplate(it) },
                            onExportJson = { viewModel.exportSiteAsJson() },
                            onImportJson = { viewModel.importSiteFromJson(it) }
                        )
                    }
                    NavigationTab.PREVIEW -> {
                        PreviewScreen(
                            compiledSite = compiledSite,
                            viewportMode = viewportMode,
                            onSelectViewport = { viewModel.setViewport(it) },
                            serverRunning = serverRunning,
                            serverPort = serverPort
                        )
                    }
                    NavigationTab.LOCALHOST -> {
                        LocalhostScreen(
                            serverRunning = serverRunning,
                            port = serverPort,
                            localIp = serverLocalIp,
                            logs = serverLogs,
                            onToggleServer = { viewModel.toggleServer() },
                            onClearLogs = { viewModel.clearServerLogs() }
                        )
                    }
                    NavigationTab.DEPLOY -> {
                        DeployScreen(
                            compiledSite = compiledSite,
                            distZipFile = distZip,
                            netlifyToken = netlifyToken,
                            netlifyUser = netlifyUser,
                            customSiteName = customSiteName,
                            defaultSlug = currentWebsite?.slug ?: "my-site",
                            deployProgress = deployProgress,
                            deployments = deployments,
                            onSetNetlifyToken = { viewModel.setNetlifyToken(it) },
                            onDeleteNetlifyToken = { viewModel.deleteNetlifyToken() },
                            onVerifyToken = { viewModel.verifyNetlifyToken() },
                            onSetCustomSiteName = { viewModel.setCustomSiteName(it) },
                            onDeployClick = { viewModel.deployToNetlify(useSimulationIfNoToken = true) },
                            onResetDeployState = { viewModel.resetDeployState() }
                        )
                    }
                }
            }
        }
    }
}

    // Modal Add Block Bottom Sheet
    if (isAddBlockSheetOpen) {
        AddBlockBottomSheet(
            onDismiss = { viewModel.closeAddBlockSheet() },
            onSelectType = { viewModel.addBlock(it) }
        )
    }

    // Modal Block Editor Bottom Sheet
    editingBlock?.let { block ->
        BlockEditorBottomSheet(
            block = block,
            onDismiss = { viewModel.closeBlockEditor() },
            onSave = { viewModel.saveEditedBlock(it) }
        )
    }

    // Modal Site Settings Dialog
    if (isSiteSettingsOpen && currentWebsite != null) {
        SiteSettingsDialog(
            website = currentWebsite!!,
            onDismiss = { viewModel.closeSiteSettings() },
            onSave = { title, slug, desc, theme, font, css ->
                viewModel.updateSiteSettings(title, slug, desc, theme, font, css)
            }
        )
    }
}

data class NavigationTabItem(
    val tab: NavigationTab,
    val icon: ImageVector,
    val label: String,
    val hasBadge: Boolean = false
)
