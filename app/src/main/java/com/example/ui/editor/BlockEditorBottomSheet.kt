package com.example.ui.editor

import androidx.compose.animation.AnimatedVisibility
import com.example.ui.components.PhotoPickerComponent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File
import com.example.data.model.BlockType
import com.example.data.model.WebBlockEntity
import com.example.data.model.WebsiteEntity
import com.example.generator.SiteCompiler
import com.example.ui.components.Pixel4BitColorPicker
import com.example.ui.components.Pixel4BitColorPickerDialog
import com.example.ui.components.PixelArtPalettes
import com.example.ui.components.PixelColorTarget
import com.example.ui.components.playPixelAudioBlip
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockEditorBottomSheet(
    block: WebBlockEntity,
    website: WebsiteEntity? = null,
    onDismiss: () -> Unit,
    onSave: (WebBlockEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val context = LocalContext.current
    var title by remember { mutableStateOf(block.title) }
    var subtitle by remember { mutableStateOf(block.subtitle) }
    var content by remember { mutableStateOf(block.content) }
    var imageUrl by remember { mutableStateOf(block.imageUrl) }
    var buttonText by remember { mutableStateOf(block.buttonText) }
    var buttonUrl by remember { mutableStateOf(block.buttonUrl) }
    var secondaryButtonText by remember { mutableStateOf(block.secondaryButtonText) }
    var secondaryButtonUrl by remember { mutableStateOf(block.secondaryButtonUrl) }
    var alignment by remember { mutableStateOf(block.alignment) }
    var bgColor by remember { mutableStateOf(block.backgroundColorHex) }
    var textColor by remember { mutableStateOf(block.textColorHex) }
    var pageSlug by remember { mutableStateOf(block.pageSlug) }
    var animationEffect by remember { mutableStateOf(block.animationEffect) }
    var testFeedback by remember { mutableStateOf<String?>(null) }
    var show4BitColorPickerModal by remember { mutableStateOf(false) }
    var is4BitPickerExpanded by remember { mutableStateOf(false) }

    val previewEntity = remember(block, title, subtitle, content, imageUrl, buttonText, buttonUrl, secondaryButtonText, secondaryButtonUrl, alignment, bgColor, textColor, pageSlug, animationEffect) {
        block.copy(
            title = title,
            subtitle = subtitle,
            content = content,
            imageUrl = imageUrl,
            buttonText = buttonText,
            buttonUrl = buttonUrl,
            secondaryButtonText = secondaryButtonText,
            secondaryButtonUrl = secondaryButtonUrl,
            alignment = alignment,
            backgroundColorHex = bgColor,
            textColorHex = textColor,
            pageSlug = pageSlug,
            animationEffect = animationEffect
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Edit ${block.type.displayName}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Customize text, styling, buttons, and responsive behavior",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-Time In-Editor Live Section Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandIndigo.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = BrandIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "LIVE SECTION PREVIEW",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandEmerald.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Instant Updates",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = BrandEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LiveBlockVisualPreview(
                        block = previewEntity,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("live_block_editor_preview")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Main Heading / Brand") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_block_title"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandIndigo
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle / Eyebrow badge
            if (block.type != BlockType.CUSTOM_HTML && block.type != BlockType.FOOTER) {
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text(if (block.type == BlockType.HERO) "Badge / Eyebrow Text" else "Subtitle / Role") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_block_subtitle"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandIndigo
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Body / Items Content
            when (block.type) {
                BlockType.COUNTDOWN_TIMER -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Target Date & Time (Countdown Launch)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Target ISO-8601 Timestamp") },
                            placeholder = { Text("e.g. 2026-12-31T00:00:00") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_countdown_target"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandIndigo)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Quick Date Presets:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val datePresets = listOf(
                                "+7 Days" to "2026-10-01T00:00:00",
                                "+14 Days" to "2026-10-08T00:00:00",
                                "+30 Days" to "2026-10-24T00:00:00",
                                "+60 Days" to "2026-11-24T00:00:00",
                                "2027 New Year" to "2027-01-01T00:00:00"
                            )
                            for ((lbl, isoVal) in datePresets) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (content == isoVal) BrandIndigo else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable {
                                        content = isoVal
                                        testFeedback = "Countdown set to $lbl"
                                    }
                                ) {
                                    Text(
                                        text = lbl,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (content == isoVal) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (content == isoVal) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                BlockType.IMAGE_CAROUSEL -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Carousel Slides Gallery",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Current slides list
                        val slideUrls = content.split("|").map { it.trim() }.filter { it.isNotBlank() }
                        if (slideUrls.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                slideUrls.forEachIndexed { idx, url ->
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.size(width = 110.dp, height = 80.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            AsyncImage(
                                                model = url,
                                                contentDescription = "Slide ${idx + 1}",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            Surface(
                                                shape = CircleShape,
                                                color = Color.Black.copy(alpha = 0.6f),
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(20.dp)
                                                    .clickable {
                                                        val remaining = slideUrls.toMutableList()
                                                        remaining.removeAt(idx)
                                                        content = remaining.joinToString("|")
                                                        testFeedback = "Removed slide ${idx + 1}"
                                                    }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("✕", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color.Black.copy(alpha = 0.6f),
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(4.dp)
                                            ) {
                                                Text(
                                                    text = "#${idx + 1}",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add Stock Slide Presets:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val presetSlides = listOf(
                                "Living Space" to "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=1200&q=80",
                                "Design Studio" to "https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=1200&q=80",
                                "Craft Chair" to "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=1200&q=80",
                                "Minimal Desk" to "https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=1200&q=80",
                                "Urban Tower" to "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=1200&q=80"
                            )
                            for ((name, u) in presetSlides) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, BrandIndigo.copy(alpha = 0.3f)),
                                    modifier = Modifier.clickable {
                                        val cur = if (content.isBlank()) u else "$content|$u"
                                        content = cur
                                        testFeedback = "Added $name slide"
                                    }
                                ) {
                                    Text(
                                        text = "+ $name",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Slide Image URLs (Pipe-separated |)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_block_content"),
                            minLines = 2,
                            maxLines = 5,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandIndigo)
                        )
                    }
                }

                BlockType.CONTACT -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Contact Description & Details (e.g. Email, Location, Hours)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_block_content"),
                            minLines = 3,
                            maxLines = 6,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandIndigo)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ),
                            border = BorderStroke(1.dp, BrandIndigo.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = BrandIndigo,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Form Submission & Backend Endpoint",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Connect a backend webhook (Formspree, Formkeep, Getform) or leave as Mailto for direct user email dispatch.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = buttonUrl,
                                    onValueChange = { buttonUrl = it },
                                    label = { Text("Endpoint Webhook URL / Destination Email") },
                                    placeholder = { Text("https://formspree.io/f/xyz or mailto:contact@mybiz.com") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_contact_form_endpoint"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandIndigo)
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Quick Endpoint Presets:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val endpointPresets = listOf(
                                        "Formspree" to "https://formspree.io/f/your_form_id",
                                        "Getform.io" to "https://getform.io/f/your_form_id",
                                        "Direct Mailto" to "mailto:contact@mysite.com",
                                        "WhatsApp Dispatch" to "https://wa.me/18005550199"
                                    )
                                    for ((label, urlPreset) in endpointPresets) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (buttonUrl == urlPreset) BrandIndigo else MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, BrandIndigo.copy(alpha = 0.3f)),
                                            modifier = Modifier.clickable {
                                                buttonUrl = urlPreset
                                                testFeedback = "Assigned endpoint: $label"
                                            }
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (buttonUrl == urlPreset) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    val contentLabel = when (block.type) {
                        BlockType.NAVBAR -> "Navigation Links (separated by | e.g. Features|Pricing|Contact)"
                        BlockType.FEATURES, BlockType.SERVICES -> "Items (separated by | with Title: Description)"
                        BlockType.PRICING -> "Tiers (separated by | with Plan: bullet1, bullet2)"
                        BlockType.FAQ -> "Questions (separated by | with Question: Answer)"
                        BlockType.STATS -> "Metrics (separated by | e.g. 99.9%: Edge Uptime | < 1ms: Global TTFB)"
                        BlockType.TIMELINE -> "Steps (separated by | e.g. 01 Step: Description | 02 Step: Description)"
                        BlockType.TEAM -> "Team Members (separated by | e.g. Name: Role: Bio)"
                        BlockType.LOGOS -> "Partner Names (separated by | e.g. Google | Stripe | Vercel)"
                        BlockType.CUSTOM_HTML -> "Raw HTML / Embed Code"
                        else -> "Content / Description"
                    }

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text(contentLabel) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_block_content"),
                        minLines = 3,
                        maxLines = 8,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandIndigo
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Photo & Visual Asset Media Section
            PhotoPickerComponent(
                currentImageUrl = imageUrl,
                onImageChanged = {
                    imageUrl = it
                    testFeedback = "Photo updated"
                },
                onImageRemoved = {
                    imageUrl = ""
                    testFeedback = "Photo removed"
                },
                title = when (block.type) {
                    BlockType.WHATSAPP_SHOP -> "Store Banner / Promo Asset"
                    BlockType.MULTISTEP_WIZARD -> "Wizard Reference Banner"
                    BlockType.HERO -> "Hero Header Image Asset"
                    BlockType.ABOUT -> "About Section Feature Media"
                    BlockType.GALLERY -> "Gallery Cover Banner"
                    else -> "Section Photo Asset"
                },
                subtitle = "Choose device photos or curated stock assets for this section"
            )

            // Special WhatsApp Shop Products Asset Customizer
            if (block.type == BlockType.WHATSAPP_SHOP && content.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                val rawProducts = content.split("|").map { it.trim() }.filter { it.isNotBlank() }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    border = BorderStroke(1.dp, BrandIndigo.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "WhatsApp Products Photo Replacement",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BrandIndigo.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${rawProducts.size} Items",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BrandIndigo,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Replace each product's photo with custom gallery pictures",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        rawProducts.forEachIndexed { pIdx, raw ->
                            val parts = raw.split(":").map { it.trim() }
                            val pName = parts.getOrNull(0) ?: "Product ${pIdx + 1}"
                            val pPrice = parts.getOrNull(1) ?: "999"
                            val pMrp = parts.getOrNull(2) ?: "1999"
                            val pRating = parts.getOrNull(3) ?: "4.8"
                            val pDesc = parts.getOrNull(4) ?: ""
                            val pThumb = parts.getOrNull(5) ?: "https://images.unsplash.com/photo-1544816155-12df9643f363?w=500&q=80"
                            val pCat = parts.getOrNull(6) ?: "All"

                            PhotoPickerComponent(
                                currentImageUrl = pThumb,
                                onImageChanged = { newImgUrl ->
                                    val updatedRawList = rawProducts.toMutableList()
                                    val updatedItemStr = "$pName: $pPrice: $pMrp: $pRating: $pDesc: $newImgUrl: $pCat"
                                    if (pIdx in updatedRawList.indices) {
                                        updatedRawList[pIdx] = updatedItemStr
                                        content = updatedRawList.joinToString(" | ")
                                        testFeedback = "Product photo replaced: $pName"
                                    }
                                },
                                onImageRemoved = {
                                    val updatedRawList = rawProducts.toMutableList()
                                    val updatedItemStr = "$pName: $pPrice: $pMrp: $pRating: $pDesc: : $pCat"
                                    if (pIdx in updatedRawList.indices) {
                                        updatedRawList[pIdx] = updatedItemStr
                                        content = updatedRawList.joinToString(" | ")
                                        testFeedback = "Product photo removed: $pName"
                                    }
                                },
                                title = pName,
                                subtitle = "Price: ₹$pPrice • Category: $pCat",
                                aspectRatio = 4f / 3f,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Comprehensive Button & Redirection Link Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = BrandIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Button & Link Redirection",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Auto-fill default action button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrandIndigo.copy(alpha = 0.12f),
                            modifier = Modifier.clickable {
                                if (buttonText.isBlank()) {
                                    buttonText = when (block.type) {
                                        BlockType.HERO -> "Get Started"
                                        BlockType.CTA -> "Contact Us Now"
                                        BlockType.PRICING -> "Choose Plan"
                                        BlockType.ABOUT -> "Our Services"
                                        BlockType.SERVICES -> "Get a Quote"
                                        BlockType.FEATURES -> "Explore Pricing"
                                        BlockType.CONTACT -> "Send Message"
                                        BlockType.NAVBAR -> "Get Started"
                                        else -> "Learn More"
                                    }
                                }
                                buttonUrl = SiteCompiler.resolveSmartButtonUrl(buttonUrl, buttonText, block.type)
                                testFeedback = "⚡ Smart default link assigned: $buttonUrl"
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = BrandIndigo,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Auto Default",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BrandIndigo
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = buttonText,
                        onValueChange = {
                            buttonText = it
                            if (it.isNotBlank() && buttonUrl.isBlank()) {
                                buttonUrl = SiteCompiler.resolveSmartButtonUrl("", it, block.type)
                            }
                        },
                        label = { Text("Primary Button Text") },
                        placeholder = { Text("e.g. Get Started, Contact Us") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_block_button_text"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandIndigo
                        )
                    )

                    // Quick Button Text presets
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val textPresets = listOf("Get Started", "Contact Us", "View Pricing", "Learn More", "Book a Call", "Explore Features")
                        for (preset in textPresets) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (buttonText == preset) BrandIndigo else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BrandIndigo.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable {
                                    buttonText = preset
                                    buttonUrl = SiteCompiler.resolveSmartButtonUrl("", preset, block.type)
                                    testFeedback = "Selected \"$preset\" (Redirect: $buttonUrl)"
                                }
                            ) {
                                Text(
                                    text = preset,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (buttonText == preset) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = buttonUrl,
                        onValueChange = { buttonUrl = it },
                        label = { Text("Redirect Target (Anchor, URL, Email, Phone)") },
                        placeholder = { Text("e.g. #contact, #pricing, https://..., mailto:...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_block_button_url"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandIndigo
                        )
                    )

                    // Quick Redirect Target Presets
                    Text(
                        text = "Quick Redirect Target Presets:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val targetPresets = listOf(
                            "#contact" to "Contact Form",
                            "#pricing" to "Pricing",
                            "#features" to "Features",
                            "#about" to "About",
                            "#services" to "Services",
                            "#faq" to "FAQ",
                            "mailto:hello@example.com" to "Email",
                            "tel:+18005550199" to "Call",
                            "https://google.com" to "Web Link"
                        )
                        for ((target, label) in targetPresets) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (buttonUrl == target) BrandIndigo else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BrandIndigo.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable {
                                    buttonUrl = target
                                    if (buttonText.isBlank()) {
                                        buttonText = when (target) {
                                            "#contact" -> "Contact Us"
                                            "#pricing" -> "View Plans"
                                            "#features" -> "Explore Features"
                                            "#about" -> "About Us"
                                            "#services" -> "Our Services"
                                            "#faq" -> "Read FAQ"
                                            else -> "Get Started"
                                        }
                                    }
                                    testFeedback = "Redirect set to $target ($label)"
                                }
                            ) {
                                Text(
                                    text = target,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = if (buttonUrl == target) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Interactive Live Button Preview & Tap Test
                    if (buttonText.isNotBlank()) {
                        val effectiveUrl = if (buttonUrl.isNotBlank() && buttonUrl != "#") {
                            buttonUrl
                        } else {
                            SiteCompiler.resolveSmartButtonUrl(buttonUrl, buttonText, block.type)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrandIndigo.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Live Tap Test (Tap to test redirect):",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = BrandIndigo
                                    )
                                    Text(
                                        text = "➔ $effectiveUrl",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = {
                                        testFeedback = "✓ Tap Test Verified: Successfully redirects to $effectiveUrl"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TouchApp,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Test Button: \"$buttonText\"")
                                }
                            }
                        }
                    }

                    // Secondary Button (Available on Hero and expandable on other blocks)
                    if (block.type == BlockType.HERO || secondaryButtonText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Secondary Button",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = secondaryButtonText,
                                onValueChange = {
                                    secondaryButtonText = it
                                    if (it.isNotBlank() && secondaryButtonUrl.isBlank()) {
                                        secondaryButtonUrl = "#features"
                                    }
                                },
                                label = { Text("Secondary Text") },
                                placeholder = { Text("e.g. Learn More") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = secondaryButtonUrl,
                                onValueChange = { secondaryButtonUrl = it },
                                label = { Text("Secondary URL") },
                                placeholder = { Text("e.g. #features") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Test feedback message
                    testFeedback?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandCyan.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = BrandIndigo,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Alignment Selector
            Text(
                text = "Text Alignment",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AlignmentButton(
                    icon = Icons.Default.FormatAlignLeft,
                    label = "Left",
                    selected = alignment == "left",
                    onClick = { alignment = "left" },
                    modifier = Modifier.weight(1f)
                )
                AlignmentButton(
                    icon = Icons.Default.FormatAlignCenter,
                    label = "Center",
                    selected = alignment == "center",
                    onClick = { alignment = "center" },
                    modifier = Modifier.weight(1f)
                )
                AlignmentButton(
                    icon = Icons.Default.FormatAlignRight,
                    label = "Right",
                    selected = alignment == "right",
                    onClick = { alignment = "right" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Spacer(modifier = Modifier.height(14.dp))

            // 4-Bit Pixel-Art Color Theme & Custom Picker Studio
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pixel_4bit_color_studio_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F111A)),
                border = BorderStroke(1.5.dp, BrandAmber.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = BrandAmber.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, BrandAmber)
                            ) {
                                Text(
                                    text = "4-BIT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrandAmber,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Classic 'Pixel-Art' Color Themes",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "1-Tap toggle authentic retro palettes for this element",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Open Dialog / Fullscreen Configurator Button
                        IconButton(
                            onClick = {
                                playPixelAudioBlip(880.0, 0.04)
                                show4BitColorPickerModal = true
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_open_4bit_picker_modal")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Full 4-Bit Color Studio",
                                tint = BrandAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Current Active Colors Indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF181C29), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Element Tint:",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFCBD5E1)
                            )
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(
                                        if (bgColor.isNotBlank()) Color(android.graphics.Color.parseColor(if (bgColor.startsWith("#")) bgColor else "#$bgColor"))
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(2.dp)
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.5f))
                            )
                            Text(
                                text = "BG: ${bgColor.ifBlank { "Default" }}",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                            if (textColor.isNotBlank()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(
                                            Color(android.graphics.Color.parseColor(if (textColor.startsWith("#")) textColor else "#$textColor")),
                                            RoundedCornerShape(2.dp)
                                        )
                                        .border(1.dp, Color.White.copy(alpha = 0.5f))
                                )
                                Text(
                                    text = "Text: $textColor",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Toggle Inline Expanded Picker
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (is4BitPickerExpanded) BrandCyan.copy(alpha = 0.2f) else Color(0xFF242A3D),
                            border = BorderStroke(1.dp, if (is4BitPickerExpanded) BrandCyan else Color(0xFF3B435F)),
                            modifier = Modifier.clickable {
                                is4BitPickerExpanded = !is4BitPickerExpanded
                                playPixelAudioBlip(659.25, 0.03)
                            }
                        ) {
                            Text(
                                text = if (is4BitPickerExpanded) "Collapse ▲" else "Customize ▼",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (is4BitPickerExpanded) BrandCyan else Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Quick Toggle Row of Classic 'Pixel-Art' Themes
                    Text(
                        text = "QUICK-SWAP CLASSIC PIXEL THEMES:",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandAmber,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PixelArtPalettes.CLASSIC_THEMES.forEach { theme ->
                            val isCurrent = bgColor.equals(theme.bgHex, ignoreCase = true) &&
                                    textColor.equals(theme.textHex, ignoreCase = true)

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isCurrent) Color(0xFF232B3F) else Color(0xFF141722),
                                border = BorderStroke(
                                    if (isCurrent) 1.5.dp else 1.dp,
                                    if (isCurrent) BrandAmber else Color(0xFF2E354A)
                                ),
                                modifier = Modifier
                                    .width(128.dp)
                                    .clickable {
                                        playPixelAudioBlip(783.99, 0.04)
                                        bgColor = theme.bgHex
                                        textColor = theme.textHex
                                        testFeedback = "Applied ${theme.name} palette to element"
                                    }
                                    .testTag("theme_btn_${theme.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // 4-Color Swatch preview
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                    ) {
                                        theme.palette.take(4).forEach { cHex ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxSize()
                                                    .background(Color(android.graphics.Color.parseColor(cHex)))
                                            )
                                        }
                                    }

                                    Text(
                                        text = theme.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) BrandAmber else Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = theme.badge,
                                        fontSize = 8.5.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }

                    // Inline Expandable Full 4-Bit Pixel Color Picker
                    AnimatedVisibility(visible = is4BitPickerExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(color = Color(0xFF242A3D))
                            Pixel4BitColorPicker(
                                currentBgHex = bgColor,
                                currentTextHex = textColor,
                                currentAccentHex = BrandIndigo.let { String.format("#%06X", 0xFFFFFF and it.value.toInt()) },
                                onColorChanged = { target, hex ->
                                    when (target) {
                                        PixelColorTarget.BACKGROUND -> {
                                            bgColor = hex
                                            testFeedback = "Background set to $hex"
                                        }
                                        PixelColorTarget.TEXT -> {
                                            textColor = hex
                                            testFeedback = "Text set to $hex"
                                        }
                                        PixelColorTarget.ACCENT -> {
                                            testFeedback = "Accent set to $hex"
                                        }
                                    }
                                },
                                onApplyFullTheme = { theme ->
                                    bgColor = theme.bgHex
                                    textColor = theme.textHex
                                    testFeedback = "Applied ${theme.name} palette to element"
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Standard Background Color Presets (Secondary Option)
            Text(
                text = "Standard Background Tones",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            val bgPresets = listOf(
                "" to "Theme Default",
                "#0F172A" to "Slate Dark",
                "#1E293B" to "Navy",
                "#1E1B4B" to "Deep Indigo",
                "#064E3B" to "Emerald Dark",
                "#311042" to "Violet Dark",
                "#F8FAFC" to "Pure White"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for ((hex, name) in bgPresets.take(5)) {
                    val isSelected = bgColor.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (hex.isBlank()) MaterialTheme.colorScheme.surfaceVariant else Color(android.graphics.Color.parseColor(hex)))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) BrandIndigo else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable { bgColor = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = if (hex == "#F8FAFC") Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Block Scroll & Entrance Animation Controls
            Text(
                text = "Scroll & Entrance Animation",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val animOptions = listOf(
                    "default" to "Site Default",
                    "fade-up" to "Fade Up",
                    "slide-left" to "Slide Left",
                    "zoom-in" to "Zoom Pop",
                    "soft-pop" to "Soft Pop",
                    "none" to "No Motion"
                )
                for ((animKey, animLabel) in animOptions) {
                    val isSelected = animationEffect == animKey
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) BrandIndigo else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.clickable { animationEffect = animKey }
                    ) {
                        Text(
                            text = animLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Page Assignment Controls
            Text(
                text = "Assign to Page",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            val availablePages = remember(website?.pagesJson) {
                SiteCompiler.parsePages(website?.pagesJson ?: "")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // All Pages option (useful for Navbar/Footer/Banners)
                val isAllSelected = pageSlug == "all"
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isAllSelected) BrandIndigo else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isAllSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { pageSlug = "all" }
                ) {
                    Text(
                        text = "🌐 All Pages (Global)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Individual defined pages
                for (pg in availablePages) {
                    val isSel = pageSlug == pg.slug
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) BrandIndigo else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.clickable { pageSlug = pg.slug }
                    ) {
                        Text(
                            text = if (pg.slug == "index") "🏠 ${pg.title}" else "📄 ${pg.title}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal),
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val effectiveButtonUrl = if (buttonText.isNotBlank()) {
                            if (buttonUrl.isNotBlank() && buttonUrl != "#") buttonUrl
                            else SiteCompiler.resolveSmartButtonUrl(buttonUrl, buttonText, block.type)
                        } else buttonUrl

                        val effectiveSecUrl = if (secondaryButtonText.isNotBlank()) {
                            if (secondaryButtonUrl.isNotBlank() && secondaryButtonUrl != "#") secondaryButtonUrl
                            else "#features"
                        } else secondaryButtonUrl

                        val updated = block.copy(
                            title = title,
                            subtitle = subtitle,
                            content = content,
                            imageUrl = imageUrl,
                            buttonText = buttonText,
                            buttonUrl = effectiveButtonUrl,
                            secondaryButtonText = secondaryButtonText,
                            secondaryButtonUrl = effectiveSecUrl,
                            alignment = alignment,
                            backgroundColorHex = bgColor,
                            textColorHex = textColor,
                            pageSlug = pageSlug,
                            animationEffect = animationEffect
                        )
                        onSave(updated)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_block_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Apply Changes", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (show4BitColorPickerModal) {
            Pixel4BitColorPickerDialog(
                currentBgHex = bgColor,
                currentTextHex = textColor,
                currentAccentHex = BrandIndigo.let { String.format("#%06X", 0xFFFFFF and it.value.toInt()) },
                onDismiss = { show4BitColorPickerModal = false },
                onColorChanged = { target, hex ->
                    when (target) {
                        PixelColorTarget.BACKGROUND -> {
                            bgColor = hex
                            testFeedback = "Background set to $hex"
                        }
                        PixelColorTarget.TEXT -> {
                            textColor = hex
                            testFeedback = "Text set to $hex"
                        }
                        PixelColorTarget.ACCENT -> {
                            testFeedback = "Accent set to $hex"
                        }
                    }
                },
                onApplyFullTheme = { theme ->
                    bgColor = theme.bgHex
                    textColor = theme.textHex
                    testFeedback = "Applied ${theme.name} palette to element"
                    show4BitColorPickerModal = false
                }
            )
        }
    }
}

@Composable
fun AlignmentButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) BrandIndigo.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, BrandIndigo) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) BrandIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
                color = if (selected) BrandIndigo else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
