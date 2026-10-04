package com.example.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.WebsiteEntity
import com.example.ui.components.Pixel4BitColorPicker
import com.example.ui.components.PixelArtPalettes
import com.example.ui.components.PixelColorTarget
import com.example.ui.components.playPixelAudioBlip
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandIndigo

data class ThemePresetInfo(
    val id: String,
    val name: String,
    val mood: String,
    val bgPrimary: Color,
    val bgCard: Color,
    val textPrimary: Color,
    val accent: Color
)

data class FontOptionInfo(
    val fontFamily: String,
    val displayName: String,
    val category: String,
    val description: String,
    val isSerif: Boolean = false,
    val isMono: Boolean = false
)

data class ButtonRadiusOption(
    val id: String,
    val label: String,
    val radiusDp: Int,
    val description: String
)

data class ButtonStyleOption(
    val id: String,
    val label: String,
    val description: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeCustomizerDialog(
    website: WebsiteEntity,
    onDismiss: () -> Unit,
    onApply: (
        themePreset: String,
        fontFamily: String,
        buttonStyle: String,
        buttonRadius: String,
        customPrimaryColor: String,
        customBackgroundColor: String
    ) -> Unit
) {
    // Current working draft state
    var selectedPreset by remember { mutableStateOf(website.themePreset) }
    var selectedFont by remember { mutableStateOf(website.fontFamily) }
    var selectedButtonStyle by remember { mutableStateOf(website.buttonStyle.ifBlank { "gradient" }) }
    var selectedButtonRadius by remember { mutableStateOf(website.buttonRadius.ifBlank { "pill" }) }
    var customPrimaryColor by remember { mutableStateOf(website.customPrimaryColor) }
    var customBackgroundColor by remember { mutableStateOf(website.customBackgroundColor) }

    var isCustomColorsActive by remember {
        mutableStateOf(website.customPrimaryColor.isNotBlank() || website.customBackgroundColor.isNotBlank())
    }

    var activeTab by remember { mutableIntStateOf(0) }

    val presets = remember {
        listOf(
            ThemePresetInfo("modern-dark", "Modern Dark", "Indigo & Slate", Color(0xFF090D16), Color(0xFF131C2E), Color(0xFFF8FAFC), Color(0xFF6366F1)),
            ThemePresetInfo("clean-light", "Clean Light", "Snow & Royal Indigo", Color(0xFFFFFFFF), Color(0xFFFFFFFF), Color(0xFF0F172A), Color(0xFF4F46E5)),
            ThemePresetInfo("minimal-light", "Minimal Light", "Zinc & Electric Blue", Color(0xFFFAFAFA), Color(0xFFFFFFFF), Color(0xFF18181B), Color(0xFF2563EB)),
            ThemePresetInfo("luxury-gold", "Luxury Gold", "Onyx & Champagne Gold", Color(0xFF0B0B0E), Color(0xFF16161B), Color(0xFFFAF6EE), Color(0xFFD4AF37)),
            ThemePresetInfo("pastel-candy", "Pastel Sweet", "Soft Rose & Pink", Color(0xFFFFF9FB), Color(0xFFFFFFFF), Color(0xFF1E141D), Color(0xFFEC4899)),
            ThemePresetInfo("crimson-energy", "Crimson Energy", "Obsidian & Racing Red", Color(0xFF0A0D14), Color(0xFF141926), Color(0xFFF8FAFC), Color(0xFFEF4444)),
            ThemePresetInfo("ocean-cyan", "Ocean Cyan", "Deep Abyss & Cyan Blue", Color(0xFF030A14), Color(0xFF071829), Color(0xFFECFEFF), Color(0xFF06B6D4)),
            ThemePresetInfo("cyber-neon", "Cyber Neon", "Purple & Neon Cyan", Color(0xFF0A051B), Color(0xFF140B33), Color(0xFFFAF5FF), Color(0xFFA855F7)),
            ThemePresetInfo("emerald-minimal", "Emerald Mint", "Dark Forest & Jade", Color(0xFF05100A), Color(0xFF0B2117), Color(0xFFF0FDF4), Color(0xFF10B981)),
            ThemePresetInfo("sunset-warm", "Sunset Warm", "Amber & Warm Cream", Color(0xFFFDFBF7), Color(0xFFFFFFFF), Color(0xFF1F1607), Color(0xFFF59E0B)),
            ThemePresetInfo("retro-synth", "Retro Synth", "Neon Violet & Hot Pink", Color(0xFF12072B), Color(0xFF230F52), Color(0xFFFDF4FF), Color(0xFFF43F5E)),
            ThemePresetInfo("anime-4bit", "Anime 4-Bit", "Chiptune & Neon Pink", Color(0xFF0F051D), Color(0xFF260E4A), Color(0xFFFEF08A), Color(0xFFFF2A85)),
            ThemePresetInfo("retro-arcade-4bit", "8-Bit Arcade", "Pixel Lime & Cyan", Color(0xFF050505), Color(0xFF181818), Color(0xFF39FF14), Color(0xFFFF0055)),
            ThemePresetInfo("gameboy-4bit", "4-Bit Gameboy", "Classic Dot Matrix", Color(0xFF8B956D), Color(0xFF9BBC0F), Color(0xFF0F380F), Color(0xFF306230))
        )
    }

    val fonts = remember {
        listOf(
            FontOptionInfo("Inter, sans-serif", "Inter", "Modern Sans", "Clean, versatile and razor sharp for UI"),
            FontOptionInfo("Plus Jakarta Sans, sans-serif", "Plus Jakarta Sans", "Trendy Sans", "Modern tech, vibrant feel and friendly curves"),
            FontOptionInfo("Outfit, sans-serif", "Outfit", "Geometric Sans", "Contemporary headings and geometric balance"),
            FontOptionInfo("Space Grotesk, sans-serif", "Space Grotesk", "Cyber / Tech", "Futuristic tech, distinctive proportions"),
            FontOptionInfo("Poppins, sans-serif", "Poppins", "Geometric", "Warm, open and rounded geometric clarity"),
            FontOptionInfo("DM Sans, sans-serif", "DM Sans", "Minimalist", "High-legibility sans with Scandinavian restraint"),
            FontOptionInfo("Playfair Display, serif", "Playfair Display", "Luxury Serif", "Editorial elegance, high contrast flourishes", isSerif = true),
            FontOptionInfo("Fraunces, serif", "Fraunces", "Artisan Serif", "Warm vintage feel with distinct personality", isSerif = true),
            FontOptionInfo("JetBrains Mono, monospace", "JetBrains Mono", "Developer Mono", "Code aesthetic, engineered terminal look", isMono = true),
            FontOptionInfo("'Press Start 2P', monospace", "Press Start 2P", "4-Bit Arcade", "Authentic 8-bit & 4-bit arcade pixel font", isMono = true),
            FontOptionInfo("'DotGothic16', sans-serif", "DotGothic16", "Anime Pixel", "Retro Japanese anime & gaming 16-dot pixel font", isMono = true),
            FontOptionInfo("'Silkscreen', cursive", "Silkscreen", "Pixel Screen", "Crisp digital retro pixelated typeface", isMono = true),
            FontOptionInfo("'VT323', monospace", "VT323", "Retro CRT", "Vintage terminal phosphor dot matrix mono", isMono = true),
            FontOptionInfo("Syne, sans-serif", "Syne", "Art & Design", "Sculptural, bold fashion and creative impact"),
            FontOptionInfo("Georgia, serif", "Georgia", "Classic Serif", "Timeless literary serif with classic proportions", isSerif = true)
        )
    }

    val radiusOptions = remember {
        listOf(
            ButtonRadiusOption("pill", "Pill (Capsule)", 9999, "Ultra modern, approachable and sleek"),
            ButtonRadiusOption("rounded", "Rounded (12px)", 12, "Contemporary SaaS and Material balance"),
            ButtonRadiusOption("soft", "Soft (6px)", 6, "Subtle crisp corners, technical precision"),
            ButtonRadiusOption("sharp", "Sharp (0px)", 0, "Brutalist, 4-bit pixel arcade edge")
        )
    }

    val styleOptions = remember {
        listOf(
            ButtonStyleOption("gradient", "Lush Gradient", "Dual-tone accent gradient with top highlight and glow"),
            ButtonStyleOption("pixel-4bit", "4-Bit Pixel Bevel", "Authentic stepped pixel border with 3D arcade press offset"),
            ButtonStyleOption("solid", "Crisp Solid", "High-contrast flat color with clean lift shadow"),
            ButtonStyleOption("glass", "Frosted Glass", "Translucent backdrop filter with luminous border"),
            ButtonStyleOption("brutalist", "Neo-Brutalist", "Bold 2.5px contrast border with sharp offset shadow"),
            ButtonStyleOption("soft-glow", "Soft Glow", "Tinted neon capsule with ambient perimeter glow")
        )
    }

    val brandPaletteSwatches = listOf(
        "#6366F1", "#2563EB", "#06B6D4", "#10B981", "#14B8A6",
        "#F59E0B", "#F97316", "#EF4444", "#F43F5E", "#A855F7",
        "#EC4899", "#D4AF37", "#8B5CF6", "#0EA5E9", "#84CC16"
    )

    val backgroundToneSwatches = listOf(
        "#090D16" to "Obsidian",
        "#000000" to "Pure Black",
        "#0F172A" to "Slate Navy",
        "#030A14" to "Abyss",
        "#12072B" to "Midnight",
        "#FFFFFF" to "Pure Snow",
        "#FAFAFA" to "Soft Zinc",
        "#FDFBF7" to "Warm Cream"
    )

    // Current active preview colors
    val activePreset = presets.find { it.id == selectedPreset } ?: presets.first()
    val previewAccent = if (isCustomColorsActive && customPrimaryColor.isNotBlank()) {
        parseHexToColor(customPrimaryColor) ?: activePreset.accent
    } else {
        activePreset.accent
    }

    val previewBg = if (isCustomColorsActive && customBackgroundColor.isNotBlank()) {
        parseHexToColor(customBackgroundColor) ?: activePreset.bgPrimary
    } else {
        activePreset.bgPrimary
    }

    val previewIsDark = isDarkColor(previewBg)
    val previewCardBg = if (previewIsDark) {
        blendColor(previewBg, Color.White, 0.08f)
    } else {
        Color.White
    }
    val previewTextPrimary = if (previewIsDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val previewTextSecondary = if (previewIsDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val previewBorder = if (previewIsDark) blendColor(previewBg, Color.White, 0.16f) else Color(0xFFE2E8F0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("theme_customizer_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(previewAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = previewAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Global Site Theme & Styling",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Customize colors, typography & buttons across the whole website",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("button_close_theme_customizer")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = activeTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = BrandIndigo,
                    indicator = { tabPositions ->
                        if (activeTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                                height = 3.dp,
                                color = previewAccent
                            )
                        }
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        modifier = Modifier.testTag("tab_colors"),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Color Palettes", fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        modifier = Modifier.testTag("tab_typography"),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Typography", fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        modifier = Modifier.testTag("tab_buttons"),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.SmartButton, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Button Styles", fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Scrollable Body Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    when (activeTab) {
                        // TAB 0: Color Palettes
                        0 -> {
                            // Live Preview Snippet
                            ThemeLivePreviewCard(
                                title = "Live Color Palette Preview",
                                bg = previewBg,
                                cardBg = previewCardBg,
                                textPrimary = previewTextPrimary,
                                textSecondary = previewTextSecondary,
                                accent = previewAccent,
                                border = previewBorder,
                                fontName = selectedFont,
                                buttonRadius = selectedButtonRadius,
                                buttonStyle = selectedButtonStyle
                            )

                            // Curated Presets
                            Text(
                                text = "Curated Aesthetic Palettes",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                presets.forEach { preset ->
                                    val isSelected = selectedPreset == preset.id && !isCustomColorsActive
                                    PresetOptionCard(
                                        preset = preset,
                                        isSelected = isSelected,
                                        onClick = {
                                            selectedPreset = preset.id
                                            isCustomColorsActive = false
                                            customPrimaryColor = ""
                                            customBackgroundColor = ""
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Custom Color Override Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                border = BorderStroke(
                                    1.dp,
                                    if (isCustomColorsActive) previewAccent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FormatPaint,
                                                contentDescription = null,
                                                tint = if (isCustomColorsActive) previewAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Column {
                                                Text(
                                                    text = "Custom Color Overrides",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Manually fine-tune primary brand color & background tone",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = isCustomColorsActive,
                                            onCheckedChange = {
                                                isCustomColorsActive = it
                                                if (it && customPrimaryColor.isBlank()) {
                                                    customPrimaryColor = colorToHex(activePreset.accent)
                                                }
                                                if (it && customBackgroundColor.isBlank()) {
                                                    customBackgroundColor = colorToHex(activePreset.bgPrimary)
                                                }
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = previewAccent
                                            ),
                                            modifier = Modifier.testTag("switch_custom_colors")
                                        )
                                    }

                                    AnimatedVisibility(visible = isCustomColorsActive) {
                                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                            // Primary Accent Color
                                            Text(
                                                text = "Brand Primary / Accent Color",
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            // Accent Swatches
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                brandPaletteSwatches.forEach { hex ->
                                                    val color = parseHexToColor(hex) ?: Color.White
                                                    val isPicked = customPrimaryColor.equals(hex, ignoreCase = true)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(color)
                                                            .border(
                                                                width = if (isPicked) 3.dp else 1.dp,
                                                                color = if (isPicked) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.3f),
                                                                shape = CircleShape
                                                            )
                                                            .clickable {
                                                                customPrimaryColor = hex
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (isPicked) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = if (isDarkColor(color)) Color.White else Color.Black,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            OutlinedTextField(
                                                value = customPrimaryColor,
                                                onValueChange = { customPrimaryColor = it },
                                                label = { Text("Accent Color Hex (#RRGGBB)") },
                                                leadingIcon = {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(previewAccent)
                                                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                                    )
                                                },
                                                singleLine = true,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("input_custom_accent"),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = previewAccent,
                                                    cursorColor = previewAccent
                                                )
                                            )

                                            // Background Tone
                                            Text(
                                                text = "Site Background Canvas",
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                backgroundToneSwatches.forEach { (hex, label) ->
                                                    val color = parseHexToColor(hex) ?: Color.Black
                                                    val isPicked = customBackgroundColor.equals(hex, ignoreCase = true)
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(MaterialTheme.colorScheme.surface)
                                                            .border(
                                                                width = if (isPicked) 2.dp else 1.dp,
                                                                color = if (isPicked) previewAccent else MaterialTheme.colorScheme.outlineVariant,
                                                                shape = RoundedCornerShape(8.dp)
                                                            )
                                                            .clickable {
                                                                customBackgroundColor = hex
                                                            }
                                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(16.dp)
                                                                .clip(CircleShape)
                                                                .background(color)
                                                                .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = label,
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                fontWeight = if (isPicked) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                        )
                                                    }
                                                }
                                            }

                                            OutlinedTextField(
                                                value = customBackgroundColor,
                                                onValueChange = { customBackgroundColor = it },
                                                label = { Text("Background Tone Hex (#RRGGBB)") },
                                                leadingIcon = {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(previewBg)
                                                            .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                                                    )
                                                },
                                                singleLine = true,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("input_custom_bg"),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = previewAccent,
                                                    cursorColor = previewAccent
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 4-Bit Pixel-Art Studio in Theme Customizer
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("theme_pixel_4bit_studio"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F111A)),
                                border = BorderStroke(1.5.dp, BrandAmber.copy(alpha = 0.6f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                                    text = "Pixel-Art Color Themes",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "Apply authentic 8-bit & 4-bit palettes across your site",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF94A3B8)
                                                )
                                            }
                                        }
                                    }

                                    // Quick Theme Chips Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        PixelArtPalettes.CLASSIC_THEMES.forEach { theme ->
                                            val isSelected = customBackgroundColor.equals(theme.bgHex, ignoreCase = true) &&
                                                    customPrimaryColor.equals(theme.accentHex, ignoreCase = true)

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSelected) Color(0xFF222B3D) else Color(0xFF141724),
                                                border = BorderStroke(
                                                    if (isSelected) 1.5.dp else 1.dp,
                                                    if (isSelected) BrandAmber else Color(0xFF2E354A)
                                                ),
                                                modifier = Modifier
                                                    .width(130.dp)
                                                    .clickable {
                                                        playPixelAudioBlip(880.0, 0.04)
                                                        isCustomColorsActive = true
                                                        customBackgroundColor = theme.bgHex
                                                        customPrimaryColor = theme.accentHex
                                                    }
                                                    .testTag("global_theme_pixel_${theme.id}")
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(8.dp),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
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
                                                        color = if (isSelected) BrandAmber else Color.White,
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

                                    // Full interactive Pixel-Art Color Picker component
                                    Pixel4BitColorPicker(
                                        currentBgHex = customBackgroundColor.ifBlank { colorToHex(activePreset.bgPrimary) },
                                        currentTextHex = colorToHex(activePreset.textPrimary),
                                        currentAccentHex = customPrimaryColor.ifBlank { colorToHex(activePreset.accent) },
                                        onColorChanged = { target, hex ->
                                            isCustomColorsActive = true
                                            when (target) {
                                                PixelColorTarget.BACKGROUND -> customBackgroundColor = hex
                                                PixelColorTarget.ACCENT -> customPrimaryColor = hex
                                                PixelColorTarget.TEXT -> { /* Global text inherits high contrast */ }
                                            }
                                        },
                                        onApplyFullTheme = { theme ->
                                            isCustomColorsActive = true
                                            customBackgroundColor = theme.bgHex
                                            customPrimaryColor = theme.accentHex
                                        }
                                    )
                                }
                            }
                        }

                        // TAB 1: Typography
                        1 -> {
                            // Live Typography Showcase
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = previewBg),
                                border = BorderStroke(1.dp, previewBorder)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "LIVE TYPOGRAPHY SPECIMEN",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = previewAccent
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Build Bold Websites in Minutes",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = (-0.5).sp
                                        ),
                                        color = previewTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Active Family: $selectedFont",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontStyle = FontStyle.Italic
                                        ),
                                        color = previewAccent
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Experience high-performance client rendering, frictionless responsive block compositions, and instant one-tap deployment to global edge CDN networks.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            lineHeight = 18.sp
                                        ),
                                        color = previewTextSecondary
                                    )
                                }
                            }

                            Text(
                                text = "Select Global Font Family",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                fonts.forEach { fontOption ->
                                    val isSelected = selectedFont.equals(fontOption.fontFamily, ignoreCase = true)
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedFont = fontOption.fontFamily }
                                            .testTag("font_option_${fontOption.displayName.replace(" ", "_")}"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) previewAccent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) previewAccent else MaterialTheme.colorScheme.outlineVariant
                                        )
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
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSelected) previewAccent else MaterialTheme.colorScheme.surfaceVariant),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "Aa",
                                                        style = MaterialTheme.typography.titleSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = when {
                                                                fontOption.isSerif -> FontFamily.Serif
                                                                fontOption.isMono -> FontFamily.Monospace
                                                                else -> FontFamily.Default
                                                            }
                                                        ),
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Column {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = fontOption.displayName,
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = fontOption.category,
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = fontOption.description,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.RadioButtonChecked,
                                                    contentDescription = null,
                                                    tint = previewAccent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // TAB 2: Button Styles
                        2 -> {
                            // Live Interactive Button Preview
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = previewBg),
                                border = BorderStroke(1.dp, previewBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "LIVE INTERACTIVE BUTTON PREVIEW",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = previewAccent
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Render Primary Button according to selected radius and style
                                    LiveButtonRenderer(
                                        text = "Get Started Today",
                                        isPrimary = true,
                                        radiusId = selectedButtonRadius,
                                        styleId = selectedButtonStyle,
                                        accent = previewAccent,
                                        bgCard = previewCardBg,
                                        textPrimary = previewTextPrimary,
                                        onClick = {}
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Render Secondary Button
                                    LiveButtonRenderer(
                                        text = "Explore Features",
                                        isPrimary = false,
                                        radiusId = selectedButtonRadius,
                                        styleId = selectedButtonStyle,
                                        accent = previewAccent,
                                        bgCard = previewCardBg,
                                        textPrimary = previewTextPrimary,
                                        onClick = {}
                                    )
                                }
                            }

                            // Button Corner Radius
                            Text(
                                text = "Button Corner Radius",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                radiusOptions.forEach { opt ->
                                    val isSelected = selectedButtonRadius.equals(opt.id, ignoreCase = true)
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedButtonRadius = opt.id }
                                            .testTag("button_radius_${opt.id}"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) previewAccent.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) previewAccent else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(28.dp)
                                                    .clip(
                                                        when (opt.id) {
                                                            "sharp" -> RoundedCornerShape(0.dp)
                                                            "soft" -> RoundedCornerShape(4.dp)
                                                            "rounded" -> RoundedCornerShape(8.dp)
                                                            else -> CircleShape
                                                        }
                                                    )
                                                    .background(if (isSelected) previewAccent else MaterialTheme.colorScheme.outlineVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "CTA",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                            }
                                            Text(
                                                text = opt.label.substringBefore(" "),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }

                            // Button Visual Style
                            Text(
                                text = "Button Visual Style & Finish",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                styleOptions.forEach { styleOpt ->
                                    val isSelected = selectedButtonStyle.equals(styleOpt.id, ignoreCase = true)
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedButtonStyle = styleOpt.id }
                                            .testTag("button_style_${styleOpt.id}"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) previewAccent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) previewAccent else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = styleOpt.label,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = styleOpt.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Outlined.CheckCircle,
                                                    contentDescription = null,
                                                    tint = previewAccent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Bottom Action Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("button_cancel_theme")
                    ) {
                        Text("Cancel")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onApply(
                                    selectedPreset,
                                    selectedFont,
                                    selectedButtonStyle,
                                    selectedButtonRadius,
                                    if (isCustomColorsActive) customPrimaryColor.trim() else "",
                                    if (isCustomColorsActive) customBackgroundColor.trim() else ""
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = previewAccent,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("button_apply_theme")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Apply Globally", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetOptionCard(
    preset: ThemePresetInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("preset_option_${preset.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) preset.accent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) preset.accent else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 4 Swatch Circles
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-6).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(preset.bgPrimary)
                            .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(preset.bgCard)
                            .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(preset.textPrimary)
                            .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(preset.accent)
                            .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    )
                }

                Column {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = preset.mood,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.RadioButtonChecked,
                    contentDescription = null,
                    tint = preset.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ThemeLivePreviewCard(
    title: String,
    bg: Color,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    accent: Color,
    border: Color,
    fontName: String,
    buttonRadius: String,
    buttonStyle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(1.dp, border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = accent
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = fontName.substringBefore(","),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Next-Generation Web Design",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Dynamic CSS variables, typography pairing, and responsive block components compiled directly to zero-dependency HTML/CSS/JS.",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = textSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Embedded preview card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Active Palette Accent",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary
                        )
                        Text(
                            text = colorToHex(accent),
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }

                    LiveButtonRenderer(
                        text = "Explore",
                        isPrimary = true,
                        radiusId = buttonRadius,
                        styleId = buttonStyle,
                        accent = accent,
                        bgCard = cardBg,
                        textPrimary = textPrimary,
                        modifier = Modifier.width(100.dp),
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveButtonRenderer(
    text: String,
    isPrimary: Boolean,
    radiusId: String,
    styleId: String,
    accent: Color,
    bgCard: Color,
    textPrimary: Color,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onClick: () -> Unit
) {
    val cornerShape = when (radiusId.lowercase()) {
        "sharp", "square" -> RoundedCornerShape(0.dp)
        "soft" -> RoundedCornerShape(6.dp)
        "rounded" -> RoundedCornerShape(12.dp)
        else -> CircleShape // pill
    }

    if (isPrimary) {
        when (styleId.lowercase()) {
            "solid" -> {
                Box(
                    modifier = modifier
                        .clip(cornerShape)
                        .background(accent)
                        .clickable { onClick() }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
            "glass" -> {
                Box(
                    modifier = modifier
                        .clip(cornerShape)
                        .background(accent.copy(alpha = 0.22f))
                        .border(1.5.dp, accent, cornerShape)
                        .clickable { onClick() }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    )
                }
            }
            "brutalist" -> {
                Box(
                    modifier = modifier
                        .clip(cornerShape)
                        .background(accent)
                        .border(2.5.dp, textPrimary, cornerShape)
                        .clickable { onClick() }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text.uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    )
                }
            }
            "soft-glow" -> {
                Box(
                    modifier = modifier
                        .clip(cornerShape)
                        .background(accent.copy(alpha = 0.18f))
                        .border(1.5.dp, accent.copy(alpha = 0.6f), cornerShape)
                        .clickable { onClick() }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                    )
                }
            }
            else -> { // gradient
                val gradient = Brush.horizontalGradient(
                    colors = listOf(accent, blendColor(accent, Color.Black, 0.15f))
                )
                Box(
                    modifier = modifier
                        .clip(cornerShape)
                        .background(gradient)
                        .clickable { onClick() }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    } else {
        // Secondary Button
        val secondaryBorder = if (styleId.equals("brutalist", ignoreCase = true)) {
            BorderStroke(2.5.dp, textPrimary)
        } else {
            BorderStroke(1.dp, textPrimary.copy(alpha = 0.25f))
        }

        Box(
            modifier = modifier
                .clip(cornerShape)
                .background(bgCard)
                .border(secondaryBorder.width, secondaryBorder.brush, cornerShape)
                .clickable { onClick() }
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (styleId.equals("brutalist", ignoreCase = true)) text.uppercase() else text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (styleId.equals("brutalist", ignoreCase = true)) FontWeight.Bold else FontWeight.Medium,
                    color = textPrimary
                )
            )
        }
    }
}

// Utility color helpers
private fun parseHexToColor(hex: String): Color? {
    return try {
        val clean = hex.removePrefix("#").trim()
        if (clean.length == 6) {
            val r = clean.substring(0, 2).toInt(16)
            val g = clean.substring(2, 4).toInt(16)
            val b = clean.substring(4, 6).toInt(16)
            Color(r, g, b)
        } else null
    } catch (e: Exception) {
        null
    }
}

private fun colorToHex(color: Color): String {
    val r = (color.red * 255).toInt().coerceIn(0, 255)
    val g = (color.green * 255).toInt().coerceIn(0, 255)
    val b = (color.blue * 255).toInt().coerceIn(0, 255)
    return String.format("#%02X%02X%02X", r, g, b)
}

private fun isDarkColor(color: Color): Boolean {
    val lum = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
    return lum < 0.5f
}

private fun blendColor(base: Color, overlay: Color, factor: Float): Color {
    val r = base.red + (overlay.red - base.red) * factor
    val g = base.green + (overlay.green - base.green) * factor
    val b = base.blue + (overlay.blue - base.blue) * factor
    return Color(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f))
}
