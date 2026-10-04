package com.example.ui.templates

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BlockType
import com.example.generator.TemplateDefinition
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandRose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Authentic 4-Bit Retro Quick-Look Modal.
 * Offers live CRT scanline visualization, retro chassis casing, 8-bit soundchip audio demo,
 * multi-device viewport toggling, interactive block simulations, and 1-click template selection.
 */
@Composable
fun Anime4BitQuickLookModal(
    template: TemplateDefinition,
    all4BitTemplates: List<TemplateDefinition>,
    isPremierUnlocked: Boolean,
    onDismiss: () -> Unit,
    onSelectTemplate: (TemplateDefinition) -> Unit,
    onApply: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedViewport by remember { mutableStateOf(RetroViewport.GAMEBOY) }
    var enableScanlines by remember { mutableStateOf(true) }
    var isPlayingSound by remember { mutableStateOf(false) }

    // Palette colors derived from the active 4-bit template
    val themeColors = remember(template.id, template.themePreset) {
        get4BitPalette(template)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(horizontal = 10.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            // Main Retro Arcade Enclosure Casing
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.96f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(3.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                    .testTag("anime_4bit_quick_look_modal"),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A), // Dark Retro Chassis
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // =========================================================================
                    // 1. TOP ARCADE MARQUEE HEADER
                    // =========================================================================
                    Surface(
                        color = Color(0xFF1E293B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Blinking Green Retro LED
                                val infiniteTransition = rememberInfiniteTransition(label = "power_led")
                                val ledAlpha by infiniteTransition.animateFloat(
                                    initialValue = 0.4f,
                                    targetValue = 1.0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(600),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "led_pulse"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981).copy(alpha = ledAlpha))
                                )

                                Text(
                                    text = "★ 4-BIT ANIME QUICK-LOOK ★",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp,
                                    color = Color(0xFF38BDF8)
                                )

                                if (template.isPremier) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = BrandAmber
                                    ) {
                                        Text(
                                            text = if (isPremierUnlocked) "PREMIER UNLOCKED" else "PREMIER (AD-GATE)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 8-Bit Chiptune Soundchip Trigger Button
                                OutlinedButton(
                                    onClick = {
                                        isPlayingSound = true
                                        coroutineScope.launch(Dispatchers.Default) {
                                            play8BitJingle()
                                            isPlayingSound = false
                                        }
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFFBBF24)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.6f)),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingSound) Icons.Default.MusicNote else Icons.Default.VolumeUp,
                                        contentDescription = "Chiptune Audio",
                                        modifier = Modifier.size(13.dp),
                                        tint = Color(0xFFFBBF24)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isPlayingSound) "Playing..." else "8-Bit SFX",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                // Close Button
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF334155))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close Quick-Look",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // =========================================================================
                    // 2. QUICK TEMPLATE CAROUSEL SWITCHER
                    // =========================================================================
                    Surface(
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            all4BitTemplates.forEach { item ->
                                val isSelected = item.id == template.id
                                val itemAccent = if (item.isPremier) BrandAmber else BrandCyan

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) itemAccent.copy(alpha = 0.25f) else Color(0xFF1E293B),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) itemAccent else Color(0xFF334155)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onSelectTemplate(item) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (item.isPremier) "👑" else "👾",
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = item.name.substringBefore("~").trim(),
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    // =========================================================================
                    // 3. SCROLLABLE INTERACTIVE PREVIEW & CRT BEZEL
                    // =========================================================================
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Viewport & CRT Controls Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Viewport mode selector
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                RetroViewport.entries.forEach { vp ->
                                    val isCurrent = selectedViewport == vp
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isCurrent) themeColors.accent else Color(0xFF1E293B),
                                        border = BorderStroke(1.dp, if (isCurrent) themeColors.accent else Color(0xFF334155)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { selectedViewport = vp }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = vp.icon,
                                                contentDescription = null,
                                                tint = if (isCurrent) Color.Black else Color(0xFF94A3B8),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = vp.label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (isCurrent) Color.Black else Color(0xFFCBD5E1)
                                            )
                                        }
                                    }
                                }
                            }

                            // Scanlines Toggle
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (enableScanlines) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (enableScanlines) Color(0xFF10B981) else Color(0xFF334155)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { enableScanlines = !enableScanlines }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (enableScanlines) Color(0xFF10B981) else Color(0xFF64748B))
                                    )
                                    Text(
                                        text = if (enableScanlines) "CRT: ON" else "CRT: OFF",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (enableScanlines) Color(0xFF10B981) else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }

                        // THE RETRO CRT SCREEN FRAME
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(selectedViewport.frameHeight)
                                .clip(RoundedCornerShape(12.dp))
                                .background(themeColors.bgPrimary)
                                .border(2.dp, themeColors.border, RoundedCornerShape(12.dp))
                        ) {
                            // Actual Interactive 4-Bit Website UI Component
                            Interactive4BitWebsiteContent(
                                template = template,
                                colors = themeColors,
                                viewport = selectedViewport
                            )

                            // Procedural CRT Scanline Overlay
                            if (enableScanlines) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val step = 4f
                                    var y = 0f
                                    while (y < size.height) {
                                        drawLine(
                                            color = Color.Black.copy(alpha = 0.22f),
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = 1.2f
                                        )
                                        y += step
                                    }
                                }
                            }
                        }

                        // =========================================================================
                        // 4. DESIGN SYSTEM & PALETTE SPECS
                        // =========================================================================
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "4-BIT HARDWARE SPECIFICATIONS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = "Audio: 8-Bit Square Synth",
                                        fontSize = 9.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF38BDF8)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 4-Color Swatch
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0F172A),
                                        border = BorderStroke(1.dp, Color(0xFF334155))
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = "PALETTE SWATCH",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFF64748B)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                themeColors.swatch.forEach { col ->
                                                    Box(
                                                        modifier = Modifier
                                                            .size(18.dp)
                                                            .clip(RoundedCornerShape(3.dp))
                                                            .background(col)
                                                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Typography Box
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0F172A),
                                        border = BorderStroke(1.dp, Color(0xFF334155))
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = "ARCADE FONT",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFF64748B)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = template.fontFamily.split(",").firstOrNull()?.trim() ?: "Press Start 2P",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFFF1F5F9),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Authentic Pixel Glyphs",
                                                fontSize = 9.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }

                                // Key Features Strip
                                Text(
                                    text = template.description,
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 16.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    // =========================================================================
                    // 5. BOTTOM ACTION FOOTER
                    // =========================================================================
                    Surface(
                        color = Color(0xFF1E293B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF64748B))
                            ) {
                                Text(
                                    text = "Back to Catalog",
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = onApply,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (template.isPremier && !isPremierUnlocked) BrandAmber else Color(0xFF38BDF8)
                                ),
                                modifier = Modifier.testTag("apply_4bit_quick_look_btn")
                            ) {
                                if (template.isPremier && !isPremierUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Unlock (Watch Ad)",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.Black,
                                        fontSize = 12.sp
                                    )
                                } else {
                                    Text(
                                        text = "Use 4-Bit Template",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.Black,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive 4-Bit Website Content that dynamically simulates the chosen template.
 */
@Composable
private fun Interactive4BitWebsiteContent(
    template: TemplateDefinition,
    colors: Palette4Bit,
    viewport: RetroViewport
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Retro Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.border)
                .background(colors.bgCard)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "▶",
                    fontSize = 10.sp,
                    color = colors.accent,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = template.name.substringBefore("~").trim().uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = colors.textPrimary,
                    maxLines = 1
                )
            }

            Surface(
                shape = RoundedCornerShape(2.dp),
                color = colors.accent
            ) {
                Text(
                    text = if (template.id.contains("store") || template.id.contains("sound")) "CART [0]" else "LVL 99",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }

        // 2. High-Impact Pixel Hero Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, colors.accent)
                .background(colors.bgCard)
                .padding(10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Pixel Badge
                Text(
                    text = "【 4-BIT ANIME ARCADE SPEC 】",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = colors.accent
                )

                // Main Title
                Text(
                    text = template.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                // Japanese Subtitle
                Text(
                    text = when {
                        template.id.contains("akiba") -> "秋葉原レトロゲーム・IPSカスタム本体専門店"
                        template.id.contains("chibi_craft") || template.id.contains("commission") -> "完全特注 ドット絵グラフィック工房"
                        template.id.contains("lounge") || template.id.contains("cyber") -> "電脳サイバーバー＆メカシート予約"
                        template.id.contains("sound") -> "8ビットFM音源ROM・LSDJカセット販売"
                        template.id.contains("aoi") -> "バーチャル神社・おみくじ＆配信ターミナル"
                        template.id.contains("quest") -> "王道ドット絵インディーRPG公式ギルド"
                        template.id.contains("cafe") -> "ネコ耳メイド＆8ビットオムライス喫茶"
                        template.id.contains("mecha") -> "汎用人型メカ搭乗パイロットシステム"
                        else -> "ローファイ8ビットサウンドトラック"
                    },
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                // Interactive Primary Action
                Row(
                    modifier = Modifier
                        .border(1.dp, colors.accent)
                        .background(colors.accent)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (template.id.contains("whatsapp") || template.isPremier) "DISPATCH TO WHATSAPP ▶" else "PRESS START ▶",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black
                    )
                }
            }
        }

        // 3. Archetype-Tailored Interactive Block Simulation
        when {
            // E-Commerce / WhatsApp Store (Akiba Cartridges / Vocal-Bit)
            template.id.contains("store") || template.id.contains("sound") -> {
                Text(
                    text = "▼ 4-BIT CARTRIDGE INVENTORY ▼",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = colors.accent
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val items = if (template.id.contains("sound")) {
                        listOf("FM Chip ROM $45", "LSDJ Cart $68", "Battle FX $25")
                    } else {
                        listOf("Chrono Bit $48", "IPS GameBoy $125", "Pixel Art $19")
                    }
                    items.forEach { item ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(2.dp),
                            color = colors.bgCard,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("📦 [4-BIT]", fontSize = 8.sp, color = colors.accent, fontFamily = FontFamily.Monospace)
                                Text(item, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontFamily = FontFamily.Monospace, maxLines = 1)
                                Text("READY STOCK", fontSize = 7.sp, color = colors.textSecondary, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            // Multi-step Customization Wizard (Chibi Craft / Cyber Mech Lounge)
            template.id.contains("commission") || template.id.contains("lounge") || template.id.contains("wizard") -> {
                Text(
                    text = "▼ BESPOKE CONFIGURATION WIZARD ▼",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = colors.accent
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("1. Select Tier", "2. Attach Spec", "3. WhatsApp Confirm").forEachIndexed { idx, step ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(2.dp),
                            color = if (idx == 0) colors.accent.copy(alpha = 0.3f) else colors.bgCard,
                            border = BorderStroke(1.dp, if (idx == 0) colors.accent else colors.border)
                        ) {
                            Text(
                                text = step,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (idx == 0) colors.accent else colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // VTuber / Anime Shrine (Aoi Ch.)
            template.id.contains("aoi") -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(2.dp),
                    color = colors.bgCard,
                    border = BorderStroke(1.dp, colors.accent)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🔴 LIVE NOW: 4-BIT RETRO SPEEDRUN", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444), fontFamily = FontFamily.Monospace)
                            Text("Chat with Aoi Ch. & Send Pixel Superchat", fontSize = 8.sp, color = colors.textSecondary, fontFamily = FontFamily.Monospace)
                        }
                        Surface(shape = RoundedCornerShape(2.dp), color = colors.accent) {
                            Text("WATCH", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Indie RPG (Chibi Quest)
            else -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("⚔️ WARRIOR HP:999", "🔮 MAGE MP:450", "🏹 ROGUE SPD:99").forEach { stat ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(2.dp),
                            color = colors.bgCard,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Text(
                                text = stat,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // 4. Footer Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.border)
                .background(colors.bgCard)
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "© 2026 ${template.id.uppercase()} [4-BIT ENGINE]",
                fontSize = 7.5.sp,
                fontFamily = FontFamily.Monospace,
                color = colors.textSecondary
            )
            Text(
                text = "CREDIT 00",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = colors.accent
            )
        }
    }
}

/**
 * 8-Bit Procedural Chiptune Audio Synthesizer.
 * Generates an authentic square-wave arpeggio using Android's AudioTrack PCM stream.
 */
private fun play8BitJingle() {
    try {
        val sampleRate = 22050
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        val noteDuration = 0.08 // seconds per note
        val totalSamples = (sampleRate * noteDuration * notes.size).toInt()
        val buffer = ShortArray(totalSamples)

        var sampleIdx = 0
        for (freq in notes) {
            val samplesPerNote = (sampleRate * noteDuration).toInt()
            for (i in 0 until samplesPerNote) {
                val t = i.toDouble() / sampleRate
                val cycle = (t * freq) % 1.0
                // 50% square wave with decay
                val amp = if (cycle < 0.5) 12000 else -12000
                val decay = 1.0 - (i.toDouble() / samplesPerNote) * 0.4
                buffer[sampleIdx++] = (amp * decay).toInt().toShort()
            }
        }

        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            buffer.size * 2,
            AudioTrack.MODE_STATIC
        )
        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
    } catch (_: Throwable) {
        // Graceful fallback if device/environment audio is restricted
    }
}

/**
 * Viewport dimensions and labels for the Retro CRT enclosure.
 */
enum class RetroViewport(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val frameHeight: androidx.compose.ui.unit.Dp) {
    GAMEBOY("Game Boy DMG", Icons.Default.PhoneAndroid, 340.dp),
    ARCADE("Arcade CRT 4:3", Icons.Default.Tv, 360.dp),
    WIDESCREEN("Cyber Wide", Icons.Default.Computer, 380.dp)
}

/**
 * Palette colors for 4-bit simulation.
 */
data class Palette4Bit(
    val bgPrimary: Color,
    val bgCard: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val border: Color,
    val swatch: List<Color>
)

private fun get4BitPalette(template: TemplateDefinition): Palette4Bit {
    return when {
        // Game Boy DMG (Olive Greens)
        template.themePreset == "gameboy-4bit" || template.id.contains("sound") -> Palette4Bit(
            bgPrimary = Color(0xFF0F380F),
            bgCard = Color(0xFF306230),
            textPrimary = Color(0xFF9BBC0F),
            textSecondary = Color(0xFF8BAC0F),
            accent = Color(0xFF8BAC0F),
            border = Color(0xFF8BAC0F),
            swatch = listOf(Color(0xFF0F380F), Color(0xFF306230), Color(0xFF8BAC0F), Color(0xFF9BBC0F))
        )
        // Retro Arcade Green Phosphor
        template.themePreset == "retro-arcade-4bit" || template.id.contains("commission") || template.id.contains("chibi_quest") -> Palette4Bit(
            bgPrimary = Color(0xFF001100),
            bgCard = Color(0xFF002800),
            textPrimary = Color(0xFF33FF33),
            textSecondary = Color(0xFF00AA00),
            accent = Color(0xFF33FF33),
            border = Color(0xFF008800),
            swatch = listOf(Color(0xFF001100), Color(0xFF003300), Color(0xFF00AA00), Color(0xFF33FF33))
        )
        // Akiba Neon & Cyber Magenta
        template.id.contains("akiba") || template.id.contains("cyber") || template.id.contains("mecha") -> Palette4Bit(
            bgPrimary = Color(0xFF0A0017),
            bgCard = Color(0xFF1B0A33),
            textPrimary = Color(0xFFF1F5F9),
            textSecondary = Color(0xFF00F0FF),
            accent = Color(0xFFFF0055),
            border = Color(0xFF00F0FF),
            swatch = listOf(Color(0xFF0A0017), Color(0xFFFF0055), Color(0xFF00F0FF), Color(0xFFFFE600))
        )
        // Default Anime 4-Bit Cyber
        else -> Palette4Bit(
            bgPrimary = Color(0xFF0A0A14),
            bgCard = Color(0xFF161628),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xFF00F0FF),
            accent = Color(0xFFF43F5E),
            border = Color(0xFF00F0FF),
            swatch = listOf(Color(0xFF0A0A14), Color(0xFF161628), Color(0xFFF43F5E), Color(0xFF00F0FF))
        )
    }
}
