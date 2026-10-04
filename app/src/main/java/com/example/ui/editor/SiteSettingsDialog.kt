package com.example.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WebsiteEntity
import com.example.ui.theme.BrandIndigo

@Composable
fun SiteSettingsDialog(
    website: WebsiteEntity,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        slug: String,
        description: String,
        themePreset: String,
        fontFamily: String,
        customCss: String,
        animationStyle: String,
        enableVisitorThemeToggle: Boolean,
        formEndpoint: String,
        ogImageUrl: String
    ) -> Unit,
    onOpenThemeCustomizer: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(website.title) }
    var slug by remember { mutableStateOf(website.slug) }
    var description by remember { mutableStateOf(website.description) }
    var themePreset by remember { mutableStateOf(website.themePreset) }
    var fontFamily by remember { mutableStateOf(website.fontFamily) }
    var customCss by remember { mutableStateOf(website.customCss) }
    var animationStyle by remember { mutableStateOf(website.animationStyle) }
    var enableVisitorThemeToggle by remember { mutableStateOf(website.enableVisitorThemeToggle) }
    var formEndpoint by remember { mutableStateOf(website.formEndpoint) }
    var ogImageUrl by remember { mutableStateOf(website.ogImageUrl) }

    data class ThemeOption(
        val key: String,
        val title: String,
        val subtitle: String,
        val accentColor: Color,
        val bgColor: Color
    )

    val themes = listOf(
        ThemeOption("modern-dark", "Modern Dark", "Indigo & Slate", Color(0xFF6366F1), Color(0xFF090D16)),
        ThemeOption("clean-light", "Clean Light", "Snow & Royal Indigo", Color(0xFF4F46E5), Color(0xFFFFFFFF)),
        ThemeOption("minimal-light", "Minimal Light", "Zinc & Electric Blue", Color(0xFF2563EB), Color(0xFFFAFAFA)),
        ThemeOption("luxury-gold", "Luxury Gold", "Onyx & Champagne Gold", Color(0xFFD4AF37), Color(0xFF0B0B0E)),
        ThemeOption("pastel-candy", "Pastel Sweet", "Soft Rose & Pink", Color(0xFFEC4899), Color(0xFFFFF9FB)),
        ThemeOption("crimson-energy", "Crimson Energy", "Obsidian & Racing Red", Color(0xFFEF4444), Color(0xFF0A0D14)),
        ThemeOption("ocean-cyan", "Ocean Cyan", "Deep Abyss & Cyan Blue", Color(0xFF06B6D4), Color(0xFF030A14)),
        ThemeOption("cyber-neon", "Cyber Neon", "Purple & Neon Cyan", Color(0xFFA855F7), Color(0xFF0A051B)),
        ThemeOption("emerald-minimal", "Emerald Mint", "Dark Forest & Jade", Color(0xFF10B981), Color(0xFF05100A)),
        ThemeOption("sunset-warm", "Sunset Warm", "Amber & Warm Cream", Color(0xFFF59E0B), Color(0xFFFDFBF7)),
        ThemeOption("retro-synth", "Retro Synth", "Neon Violet & Hot Pink", Color(0xFFF43F5E), Color(0xFF12072B)),
        ThemeOption("anime-4bit", "Anime 4-Bit", "Chiptune & Neon Pink", Color(0xFFFF2A85), Color(0xFF0F051D)),
        ThemeOption("retro-arcade-4bit", "8-Bit Arcade", "Pixel Lime & Cyan", Color(0xFFFF0055), Color(0xFF050505)),
        ThemeOption("gameboy-4bit", "4-Bit Gameboy", "Classic Dot Matrix", Color(0xFF0F380F), Color(0xFF8B956D))
    )

    val fonts = listOf(
        "Inter, sans-serif" to "Inter (Modern Sans)",
        "Plus Jakarta Sans, sans-serif" to "Jakarta (Trendy Sans)",
        "Space Grotesk, sans-serif" to "Space Grotesk (Tech)",
        "'Press Start 2P', monospace" to "Press Start 2P (4-Bit Arcade)",
        "'DotGothic16', sans-serif" to "DotGothic16 (Anime Pixel)",
        "'Silkscreen', cursive" to "Silkscreen (Pixel Screen)",
        "Playfair Display, serif" to "Playfair Display (Editorial Serif)",
        "JetBrains Mono, monospace" to "JetBrains Mono (Code Monospace)",
        "Georgia, serif" to "Georgia (Classic Serif)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = BrandIndigo,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Website Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Website Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_site_title"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = slug,
                    onValueChange = { slug = it.lowercase().replace(" ", "-") },
                    label = { Text("Project Slug (URL subdomain)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_site_slug"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("SEO Meta Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp)
                )

                // Theme Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Color Theme & Palette",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    if (onOpenThemeCustomizer != null) {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onOpenThemeCustomizer()
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("button_open_full_theme_customizer")
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(14.dp), tint = BrandIndigo)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Theme Studio", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (theme in themes) {
                        val isSelected = themePreset == theme.key
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { themePreset = theme.key },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) BrandIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, BrandIndigo) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) BrandIndigo else Color.Transparent)
                                            .border(1.5.dp, if (isSelected) BrandIndigo else MaterialTheme.colorScheme.outline, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = theme.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) BrandIndigo else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = theme.subtitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Visual color swatch preview
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(theme.bgColor)
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(theme.accentColor)
                                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }

                // Typography Presets
                Text(
                    text = "Primary Typography",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for ((fontKey, fontLabel) in fonts) {
                        val isSelected = fontFamily == fontKey
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { fontFamily = fontKey },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) BrandIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, BrandIndigo) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) BrandIndigo else Color.Transparent)
                                        .border(1.5.dp, if (isSelected) BrandIndigo else MaterialTheme.colorScheme.outline, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = fontLabel,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) BrandIndigo else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Motion & Scroll Animations
                Text(
                    text = "Scroll & Entrance Animations",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val animOptions = listOf(
                        "fade-up" to "Fade Up",
                        "slide-left" to "Slide",
                        "zoom-in" to "Zoom",
                        "soft-pop" to "Pop",
                        "none" to "Off"
                    )
                    for ((optKey, optLabel) in animOptions) {
                        val isSel = animationStyle == optKey
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) BrandIndigo else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { animationStyle = optKey }
                        ) {
                            Text(
                                text = optLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal),
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // Visitor Theme Switcher Toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Visitor Light/Dark Mode Toggle",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Displays a floating toggle button on published website",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = enableVisitorThemeToggle,
                            onCheckedChange = { enableVisitorThemeToggle = it },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BrandIndigo
                            )
                        )
                    }
                }

                // Contact Form Webhook
                OutlinedTextField(
                    value = formEndpoint,
                    onValueChange = { formEndpoint = it },
                    label = { Text("Contact Form Webhook Endpoint (Optional)") },
                    placeholder = { Text("https://formspree.io/f/xxx or Web3Forms") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_site_form_endpoint"),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "Formspree" to "https://formspree.io/f/your_form_id",
                        "Web3Forms" to "https://api.web3forms.com/submit",
                        "Getform.io" to "https://getform.io/f/your_form_id",
                        "Clear" to ""
                    )
                    for ((label, urlPreset) in presets) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (formEndpoint == urlPreset && urlPreset.isNotBlank()) BrandIndigo else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, BrandIndigo.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable { formEndpoint = urlPreset }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (formEndpoint == urlPreset && urlPreset.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Social & SEO Preview
                Text(
                    text = "Social Share / OpenGraph Preview",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Simulated og:image banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (ogImageUrl.isNotBlank()) "🖼️ Custom OG Image Set" else "🌐 ${slug.ifBlank { "site" }}.preview.dev",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = title.ifBlank { "Website Title" },
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = title.ifBlank { "My Website" },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = description.ifBlank { "Explore fast, responsive pages built with Dist Maker." },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = ogImageUrl,
                    onValueChange = { ogImageUrl = it },
                    label = { Text("OpenGraph Image URL (Optional)") },
                    placeholder = { Text("https://example.com/banner.jpg") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Custom CSS
                OutlinedTextField(
                    value = customCss,
                    onValueChange = { customCss = it },
                    label = { Text("Custom CSS Overrides") },
                    placeholder = { Text("/* e.g. .hero-title { letter-spacing: 2px; } */") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title,
                        slug,
                        description,
                        themePreset,
                        fontFamily,
                        customCss,
                        animationStyle,
                        enableVisitorThemeToggle,
                        formEndpoint,
                        ogImageUrl
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_site_settings_button")
            ) {
                Text("Save Settings", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
