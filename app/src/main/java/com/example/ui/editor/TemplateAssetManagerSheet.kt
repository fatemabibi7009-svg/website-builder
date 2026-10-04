package com.example.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockType
import com.example.data.model.WebBlockEntity
import com.example.data.model.WebsiteEntity
import com.example.generator.PixelDitherEngine
import com.example.ui.components.ANIME_QUICK_PRESETS
import com.example.ui.components.PhotoPickerComponent
import com.example.ui.components.playPixelAudioBlip
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo
import kotlinx.coroutines.launch

data class TemplateAssetEntry(
    val key: String,
    val blockId: String,
    val blockTitle: String,
    val blockType: BlockType,
    val roleTitle: String,
    val currentUrl: String,
    val onReplace: (String) -> Unit,
    val onRemove: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateAssetManagerSheet(
    website: WebsiteEntity?,
    blocks: List<WebBlockEntity>,
    onUpdateBlock: (WebBlockEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeFilter by remember { mutableStateOf("All") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isBatchDithering by remember { mutableStateOf(false) }
    var batchProgress by remember { mutableStateOf<String?>(null) }

    // Parse all image assets across the current template/website
    val assetEntries = remember(blocks) {
        val list = mutableListOf<TemplateAssetEntry>()

        for (block in blocks) {
            // 1. Primary Block Image (Hero, About, Banner, Wizard)
            if (block.imageUrl.isNotBlank() || block.type in listOf(BlockType.HERO, BlockType.ABOUT, BlockType.WHATSAPP_SHOP, BlockType.MULTISTEP_WIZARD)) {
                val roleName = when (block.type) {
                    BlockType.HERO -> "Hero Header Banner"
                    BlockType.ABOUT -> "About Section Feature Image"
                    BlockType.WHATSAPP_SHOP -> "WhatsApp Store Promo Banner"
                    BlockType.MULTISTEP_WIZARD -> "Multi-Step Wizard Showcase Banner"
                    BlockType.GALLERY -> "Gallery Cover Banner"
                    else -> "${block.type.displayName} Image Asset"
                }

                list.add(
                    TemplateAssetEntry(
                        key = "${block.id}_primary",
                        blockId = block.id,
                        blockTitle = block.title.ifBlank { block.type.displayName },
                        blockType = block.type,
                        roleTitle = roleName,
                        currentUrl = block.imageUrl,
                        onReplace = { newUrl ->
                            onUpdateBlock(block.copy(imageUrl = newUrl))
                        },
                        onRemove = {
                            onUpdateBlock(block.copy(imageUrl = ""))
                        }
                    )
                )
            }

            // 2. Individual WhatsApp Shop Products
            if (block.type == BlockType.WHATSAPP_SHOP && block.content.isNotBlank()) {
                val rawProducts = block.content.split("|").map { it.trim() }.filter { it.isNotBlank() }
                rawProducts.forEachIndexed { pIdx, raw ->
                    val parts = raw.split(":").map { it.trim() }
                    val pName = parts.getOrNull(0) ?: "Product ${pIdx + 1}"
                    val pPrice = parts.getOrNull(1) ?: "999"
                    val pMrp = parts.getOrNull(2) ?: "1999"
                    val pRating = parts.getOrNull(3) ?: "4.8"
                    val pDesc = parts.getOrNull(4) ?: ""
                    val pThumb = parts.getOrNull(5) ?: "https://images.unsplash.com/photo-1544816155-12df9643f363?w=500&q=80"
                    val pCat = parts.getOrNull(6) ?: "All"

                    list.add(
                        TemplateAssetEntry(
                            key = "${block.id}_prod_$pIdx",
                            blockId = block.id,
                            blockTitle = block.title.ifBlank { "WhatsApp Store" },
                            blockType = BlockType.WHATSAPP_SHOP,
                            roleTitle = "Product: $pName (₹$pPrice)",
                            currentUrl = pThumb,
                            onReplace = { newImgUrl ->
                                val updatedRawList = rawProducts.toMutableList()
                                val updatedItemStr = "$pName: $pPrice: $pMrp: $pRating: $pDesc: $newImgUrl: $pCat"
                                if (pIdx in updatedRawList.indices) {
                                    updatedRawList[pIdx] = updatedItemStr
                                    val newContent = updatedRawList.joinToString(" | ")
                                    onUpdateBlock(block.copy(content = newContent))
                                }
                            },
                            onRemove = {
                                val updatedRawList = rawProducts.toMutableList()
                                val updatedItemStr = "$pName: $pPrice: $pMrp: $pRating: $pDesc: : $pCat"
                                if (pIdx in updatedRawList.indices) {
                                    updatedRawList[pIdx] = updatedItemStr
                                    val newContent = updatedRawList.joinToString(" | ")
                                    onUpdateBlock(block.copy(content = newContent))
                                }
                            }
                        )
                    )
                }
            }

            // 3. Gallery & Image Carousel Items
            if ((block.type == BlockType.GALLERY || block.type == BlockType.IMAGE_CAROUSEL) && block.content.isNotBlank()) {
                val rawImages = block.content.split("|").map { it.trim() }.filter { it.isNotBlank() }
                rawImages.forEachIndexed { gIdx, gUrl ->
                    list.add(
                        TemplateAssetEntry(
                            key = "${block.id}_gallery_$gIdx",
                            blockId = block.id,
                            blockTitle = block.title.ifBlank { if (block.type == BlockType.IMAGE_CAROUSEL) "Image Carousel" else "Photo Gallery" },
                            blockType = block.type,
                            roleTitle = "${if (block.type == BlockType.IMAGE_CAROUSEL) "Slide" else "Gallery Item"} #${gIdx + 1}",
                            currentUrl = gUrl,
                            onReplace = { newImgUrl ->
                                val updatedImages = rawImages.toMutableList()
                                if (gIdx in updatedImages.indices) {
                                    updatedImages[gIdx] = newImgUrl
                                    onUpdateBlock(block.copy(content = updatedImages.joinToString(" | ")))
                                }
                            },
                            onRemove = {
                                val updatedImages = rawImages.toMutableList()
                                if (gIdx in updatedImages.indices) {
                                    updatedImages.removeAt(gIdx)
                                    onUpdateBlock(block.copy(content = updatedImages.joinToString(" | ")))
                                }
                            }
                        )
                    )
                }
            }
        }

        list
    }

    val filteredAssets = remember(assetEntries, activeFilter) {
        when (activeFilter) {
            "Gallery Uploads" -> assetEntries.filter { it.currentUrl.startsWith("file://") || it.currentUrl.startsWith("content://") }
            "Template Stock" -> assetEntries.filter { it.currentUrl.startsWith("http://") || it.currentUrl.startsWith("https://") }
            "Empty" -> assetEntries.filter { it.currentUrl.isBlank() }
            else -> assetEntries
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("template_asset_manager_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandIndigo.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = BrandIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Template Photos & Assets",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Select any photo to replace it with your device gallery",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BrandIndigo.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${assetEntries.size} Assets",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = BrandIndigo
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filters = listOf("All", "Gallery Uploads", "Template Stock", "Empty")
                for (f in filters) {
                    val isSelected = activeFilter == f
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) BrandIndigo else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (isSelected) BrandIndigo else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { activeFilter = f }
                    ) {
                        Text(
                            text = f,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Batch 4-Bit Anime Dither Card
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_batch_4bit_dither"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF130924)),
                border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌸", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "4-BIT ANIME DITHER PIPELINE",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFF00F0FF)
                            )
                        }
                        Text(
                            text = batchProgress ?: "Convert all template images into 4-bit PC-98 anime pixel art",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isBatchDithering) Color(0xFFFF007F) else Color(0xFF94A3B8)
                        )
                    }

                    Button(
                        onClick = {
                            if (isBatchDithering) return@Button
                            isBatchDithering = true
                            playPixelAudioBlip(1046.5, 0.05)
                            coroutineScope.launch {
                                val activeAssets = assetEntries.filter { it.currentUrl.isNotBlank() }
                                val preset = ANIME_QUICK_PRESETS.first().config
                                for ((idx, asset) in activeAssets.withIndex()) {
                                    batchProgress = "Dithering asset ${idx + 1}/${activeAssets.size}..."
                                    val saved = PixelDitherEngine.processAndSaveImage(context, asset.currentUrl, preset)
                                    if (saved != null) {
                                        asset.onReplace(saved)
                                    }
                                }
                                batchProgress = "✓ All ${activeAssets.size} assets 4-bit dithered!"
                                isBatchDithering = false
                                playPixelAudioBlip(1567.98, 0.08)
                            }
                        },
                        enabled = !isBatchDithering && assetEntries.any { it.currentUrl.isNotBlank() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isBatchDithering) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dithering...", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dither All", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Asset List
            if (filteredAssets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No assets matching filter",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add Hero, Shop, About, or Gallery sections to place photos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredAssets, key = { it.key }) { asset ->
                        PhotoPickerComponent(
                            currentImageUrl = asset.currentUrl,
                            onImageChanged = asset.onReplace,
                            onImageRemoved = asset.onRemove,
                            title = asset.roleTitle,
                            subtitle = "Section: ${asset.blockTitle} • ${asset.blockType.displayName}",
                            aspectRatio = if (asset.blockType == BlockType.WHATSAPP_SHOP) 4f / 3f else 16f / 9f
                        )
                    }
                }
            }
        }
    }
}
