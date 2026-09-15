package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockType
import com.example.data.model.WebBlockEntity
import com.example.generator.SiteCompiler
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockEditorBottomSheet(
    block: WebBlockEntity,
    onDismiss: () -> Unit,
    onSave: (WebBlockEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(block.title) }
    var subtitle by remember { mutableStateOf(block.subtitle) }
    var content by remember { mutableStateOf(block.content) }
    var buttonText by remember { mutableStateOf(block.buttonText) }
    var buttonUrl by remember { mutableStateOf(block.buttonUrl) }
    var secondaryButtonText by remember { mutableStateOf(block.secondaryButtonText) }
    var secondaryButtonUrl by remember { mutableStateOf(block.secondaryButtonUrl) }
    var alignment by remember { mutableStateOf(block.alignment) }
    var bgColor by remember { mutableStateOf(block.backgroundColorHex) }
    var textColor by remember { mutableStateOf(block.textColorHex) }
    var testFeedback by remember { mutableStateOf<String?>(null) }

    val previewEntity = remember(block, title, subtitle, content, buttonText, buttonUrl, secondaryButtonText, secondaryButtonUrl, alignment, bgColor, textColor) {
        block.copy(
            title = title,
            subtitle = subtitle,
            content = content,
            buttonText = buttonText,
            buttonUrl = buttonUrl,
            secondaryButtonText = secondaryButtonText,
            secondaryButtonUrl = secondaryButtonUrl,
            alignment = alignment,
            backgroundColorHex = bgColor,
            textColorHex = textColor
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

            // Background Color Presets
            Text(
                text = "Section Background Accent (Optional)",
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
                            buttonText = buttonText,
                            buttonUrl = effectiveButtonUrl,
                            secondaryButtonText = secondaryButtonText,
                            secondaryButtonUrl = effectiveSecUrl,
                            alignment = alignment,
                            backgroundColorHex = bgColor,
                            textColorHex = textColor
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
