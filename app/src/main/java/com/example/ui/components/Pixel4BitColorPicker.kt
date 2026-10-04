package com.example.ui.components

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo

/**
 * Classic Pixel-Art Theme Specification.
 * Represents an authentic historical or fantasy pixel-art color palette.
 */
data class PixelArtTheme(
    val id: String,
    val name: String,
    val era: String,
    val badge: String,
    val description: String,
    val bgHex: String,
    val textHex: String,
    val accentHex: String,
    val borderHex: String,
    val palette: List<String>
)

/**
 * Which element styling property is being edited with the 4-bit picker.
 */
enum class PixelColorTarget(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    BACKGROUND("Background", Icons.Default.FormatColorFill),
    TEXT("Text Color", Icons.Default.FormatColorText),
    ACCENT("Accent / Button", Icons.Default.SmartButton)
}

/**
 * Curated classic 'pixel-art' color themes from retro gaming & computing history.
 */
object PixelArtPalettes {

    val CLASSIC_THEMES: List<PixelArtTheme> = listOf(
        PixelArtTheme(
            id = "gameboy_dmg",
            name = "Game Boy DMG-01",
            era = "1989 Nintendo DMG",
            badge = "DMG-01",
            description = "Classic 4-shade olive green reflective dot-matrix LCD",
            bgHex = "#9BBC0F",
            textHex = "#0F380F",
            accentHex = "#306230",
            borderHex = "#0F380F",
            palette = listOf("#0F380F", "#306230", "#8BAC0F", "#9BBC0F")
        ),
        PixelArtTheme(
            id = "gameboy_pocket",
            name = "Pocket Monochrome",
            era = "1996 Silver LCD",
            badge = "GBP-4BIT",
            description = "High-contrast true 4-level gray reflective dot-matrix",
            bgHex = "#E0E5D0",
            textHex = "#141414",
            accentHex = "#4C5350",
            borderHex = "#141414",
            palette = listOf("#141414", "#4C5350", "#949C94", "#E0E5D0")
        ),
        PixelArtTheme(
            id = "akiba_neon",
            name = "Akiba Cyber Neon",
            era = "PC-98 & 4-Bit Anime",
            badge = "PC-98",
            description = "16-color Japanese anime sci-fi & Akihabara arcade neon",
            bgHex = "#0B0318",
            textHex = "#FEF08A",
            accentHex = "#FF007F",
            borderHex = "#00F0FF",
            palette = listOf(
                "#0B0318", "#1A0836", "#FF007F", "#00F0FF",
                "#39FF14", "#FFE600", "#9945FF", "#FFFFFF"
            )
        ),
        PixelArtTheme(
            id = "nes_famicom",
            name = "NES Famicom",
            era = "1983 8-Bit Cartridge",
            badge = "FC-8BIT",
            description = "Iconic Nintendo Famicom arcade & console palette",
            bgHex = "#000000",
            textHex = "#FCFCFC",
            accentHex = "#D82800",
            borderHex = "#FC9838",
            palette = listOf(
                "#000000", "#D82800", "#FC9838", "#0058F8",
                "#00A800", "#FCFCFC", "#7C7C7C", "#BCBCBC"
            )
        ),
        PixelArtTheme(
            id = "pico8_fantasy",
            name = "PICO-8 Fantasy",
            era = "Indie Chiptune Console",
            badge = "PICO-16",
            description = "Beloved 16-color fantasy console palette with warm harmony",
            bgHex = "#1D2B53",
            textHex = "#FFF1E8",
            accentHex = "#FF004D",
            borderHex = "#29ADFF",
            palette = listOf(
                "#000000", "#1D2B53", "#7E2553", "#008751",
                "#AB5236", "#5F574F", "#C2C3C7", "#FFF1E8",
                "#FF004D", "#FFA300", "#FFEC27", "#00E436",
                "#29ADFF", "#83769C", "#FF77A8", "#FFCCAA"
            )
        ),
        PixelArtTheme(
            id = "cga_mode1",
            name = "CGA Mode 1 (Cyber)",
            era = "1981 IBM PC DOS",
            badge = "CGA-CYBER",
            description = "High-contrast 4-color legacy PC: Cyan, Magenta, White & Black",
            bgHex = "#000000",
            textHex = "#FFFFFF",
            accentHex = "#55FFFF",
            borderHex = "#FF55FF",
            palette = listOf("#000000", "#55FFFF", "#FF55FF", "#FFFFFF")
        ),
        PixelArtTheme(
            id = "cga_mode2",
            name = "CGA Mode 2 (Arcade)",
            era = "1981 IBM PC DOS",
            badge = "CGA-RGB",
            description = "Classic DOS 4-color: Lime Green, Crimson Red, Yellow & Black",
            bgHex = "#000000",
            textHex = "#FFFF55",
            accentHex = "#55FF55",
            borderHex = "#FF5555",
            palette = listOf("#000000", "#55FF55", "#FF55FF", "#FFFF55")
        ),
        PixelArtTheme(
            id = "phosphor_matrix",
            name = "Phosphor Matrix Green",
            era = "1982 Mainframe Terminal",
            badge = "VT-GREEN",
            description = "Hacker cyberpunk CRT green phosphor tube glow with scanlines",
            bgHex = "#041006",
            textHex = "#33FF33",
            accentHex = "#168B16",
            borderHex = "#33FF33",
            palette = listOf("#041006", "#0A420A", "#168B16", "#33FF33", "#A3FFA3")
        ),
        PixelArtTheme(
            id = "phosphor_amber",
            name = "Phosphor Amber CRT",
            era = "1983 VT220 Terminal",
            badge = "VT-AMBER",
            description = "Warm amber monochrome terminal luminescence",
            bgHex = "#1A0F00",
            textHex = "#FFCC00",
            accentHex = "#FF9E00",
            borderHex = "#FF9E00",
            palette = listOf("#1A0F00", "#663B00", "#B36800", "#FF9E00", "#FFCC00")
        ),
        PixelArtTheme(
            id = "neotokyo_pastel",
            name = "Neo-Tokyo Pastel",
            era = "90s Anime & Chibi",
            badge = "CHIBI-90S",
            description = "Pastel pixel palette from 90s magical anime & city pop",
            bgHex = "#1E182A",
            textHex = "#FFFFFF",
            accentHex = "#FFAAA7",
            borderHex = "#A8E6CF",
            palette = listOf("#1E182A", "#FFAAA7", "#FFD3B5", "#DCEDC2", "#A8E6CF", "#DED2F9", "#FFFFFF")
        ),
        PixelArtTheme(
            id = "commodore_64",
            name = "Commodore 64 VIC-II",
            era = "1982 8-Bit Computer",
            badge = "C64-16",
            description = "The legendary 16-color breadbox home computer palette",
            bgHex = "#0000AA",
            textHex = "#AAFFEE",
            accentHex = "#EEEE77",
            borderHex = "#CC44CC",
            palette = listOf(
                "#000000", "#FFFFFF", "#880000", "#AAFFEE",
                "#CC44CC", "#00CC55", "#0000AA", "#EEEE77",
                "#DD8855", "#664400", "#FF7777", "#333333"
            )
        ),
        PixelArtTheme(
            id = "neogeo_arcade",
            name = "Neo-Geo MVS Arcade",
            era = "1990 SNK 100 Mega",
            badge = "MVS-MEGA",
            description = "Punchy Japanese arcade coin-op fighter & action cabinet palette",
            bgHex = "#0C0814",
            textHex = "#FFFFFF",
            accentHex = "#FF3366",
            borderHex = "#00CCFF",
            palette = listOf("#0C0814", "#FF3366", "#00CCFF", "#FFE600", "#00FF88", "#FFFFFF")
        )
    )

    fun findTheme(id: String): PixelArtTheme {
        return CLASSIC_THEMES.find { it.id == id } ?: CLASSIC_THEMES.first()
    }
}

/**
 * Plays an authentic 8-bit square-wave audio click/blip using Android AudioTrack PCM stream.
 */
fun playPixelAudioBlip(freq: Double = 880.0, durationSec: Double = 0.04) {
    try {
        val sampleRate = 22050
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val cycle = (t * freq) % 1.0
            val amp = if (cycle < 0.5) 10000 else -10000
            val decay = 1.0 - (i.toDouble() / totalSamples) * 0.5
            buffer[i] = (amp * decay).toInt().toShort()
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
        // Fallback silently if audio stream restricted
    }
}

/**
 * Custom 4-Bit Pixel-Art Color Picker component.
 * Allows toggling between classic 'pixel-art' color themes and assigning individual pixel swatches.
 */
@Composable
fun Pixel4BitColorPicker(
    currentBgHex: String,
    currentTextHex: String,
    currentAccentHex: String = "",
    onColorChanged: (target: PixelColorTarget, hex: String) -> Unit,
    onApplyFullTheme: (theme: PixelArtTheme) -> Unit,
    modifier: Modifier = Modifier,
    initialThemeId: String = "gameboy_dmg"
) {
    var selectedThemeId by remember { mutableStateOf(initialThemeId) }
    var activeTarget by remember { mutableStateOf(PixelColorTarget.BACKGROUND) }
    var crtScanlinesEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var manualHexInput by remember { mutableStateOf("") }

    val activeTheme = remember(selectedThemeId) { PixelArtPalettes.findTheme(selectedThemeId) }

    val effectiveBg = currentBgHex.ifBlank { activeTheme.bgHex }
    val effectiveText = currentTextHex.ifBlank { activeTheme.textHex }
    val effectiveAccent = currentAccentHex.ifBlank { activeTheme.accentHex }

    val bgParsed = remember(effectiveBg) { parsePixelHex(effectiveBg) }
    val textParsed = remember(effectiveText) { parsePixelHex(effectiveText) }
    val accentParsed = remember(effectiveAccent) { parsePixelHex(effectiveAccent) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp))
            .background(Color(0xFF0D0E15))
            .border(2.dp, Color(0xFF383C52))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Pixel Arcade Header Bar
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
                    shape = RoundedCornerShape(0.dp),
                    color = Color(0xFF1F2233),
                    border = BorderStroke(1.5.dp, BrandAmber)
                ) {
                    Text(
                        text = "4-BIT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = BrandAmber,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "PIXEL-ART COLOR STUDIO",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFF1F5F9),
                    letterSpacing = 0.5.sp
                )
            }

            // Quick Toolbar: Sound & CRT Toggles
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // CRT Scanlines Toggle
                Surface(
                    shape = RoundedCornerShape(0.dp),
                    color = if (crtScanlinesEnabled) BrandCyan.copy(alpha = 0.2f) else Color(0xFF1E2130),
                    border = BorderStroke(1.dp, if (crtScanlinesEnabled) BrandCyan else Color(0xFF3F4460)),
                    modifier = Modifier.clickable {
                        crtScanlinesEnabled = !crtScanlinesEnabled
                        if (soundEnabled) playPixelAudioBlip(1200.0, 0.03)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = "CRT Scanlines",
                            tint = if (crtScanlinesEnabled) BrandCyan else Color(0xFF94A3B8),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (crtScanlinesEnabled) "CRT ON" else "CRT OFF",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (crtScanlinesEnabled) BrandCyan else Color(0xFF94A3B8)
                        )
                    }
                }

                // Sound Effect Toggle
                Surface(
                    shape = RoundedCornerShape(0.dp),
                    color = if (soundEnabled) BrandEmerald.copy(alpha = 0.2f) else Color(0xFF1E2130),
                    border = BorderStroke(1.dp, if (soundEnabled) BrandEmerald else Color(0xFF3F4460)),
                    modifier = Modifier.clickable {
                        soundEnabled = !soundEnabled
                        if (!soundEnabled) {
                            // turning off
                        } else {
                            playPixelAudioBlip(880.0, 0.05)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "SFX Toggle",
                        tint = if (soundEnabled) BrandEmerald else Color(0xFF64748B),
                        modifier = Modifier
                            .padding(4.dp)
                            .size(14.dp)
                    )
                }
            }
        }

        // Live Interactive 4-Bit Element Preview Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.5.dp, Color(0xFF262A3D))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgParsed)
                    .border(2.dp, accentParsed.copy(alpha = 0.8f))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Meta row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "◆ [ELEMENT LIVE PREVIEW]",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = textParsed.copy(alpha = 0.75f)
                    )
                    Text(
                        text = activeTheme.badge,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = accentParsed
                    )
                }

                // Sample Content
                Text(
                    text = "RETRO 4-BIT HERO BLOCK",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = textParsed,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Clean pixel-aligned typography rendering with high-contrast 4-bit palette harmony.",
                    fontSize = 11.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = textParsed.copy(alpha = 0.9f),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Pixel Button Sample
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(0.dp),
                        color = accentParsed,
                        border = BorderStroke(2.dp, textParsed.copy(alpha = 0.8f)),
                        modifier = Modifier.clickable {
                            if (soundEnabled) playPixelAudioBlip(987.77, 0.05)
                        }
                    ) {
                        Text(
                            text = "► PRESS START",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isDarkColor(accentParsed)) Color.White else Color.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(0.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.5.dp, textParsed.copy(alpha = 0.6f)),
                        modifier = Modifier.clickable {
                            if (soundEnabled) playPixelAudioBlip(659.25, 0.04)
                        }
                    ) {
                        Text(
                            text = "INSPECT",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = textParsed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // CRT Scanline Shader Overlay
            if (crtScanlinesEnabled) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                ) {
                    val step = 4.dp.toPx()
                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = Color(0x28000000),
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(size.width, y),
                            strokeWidth = 1.2f
                        )
                        y += step
                    }
                }
            }
        }

        // Active Colors Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF151824))
                .border(1.dp, Color(0xFF262B3F))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorStatChip("BG", effectiveBg, bgParsed)
            ColorStatChip("TEXT", effectiveText, textParsed)
            ColorStatChip("ACCENT", effectiveAccent, accentParsed)

            // Quick 1-Tap Apply Full Theme Button
            Button(
                onClick = {
                    if (soundEnabled) playPixelAudioBlip(1046.5, 0.08)
                    onApplyFullTheme(activeTheme)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandAmber),
                shape = RoundedCornerShape(0.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.testTag("btn_apply_full_pixel_theme")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "APPLY THEME",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black
                )
            }
        }

        // Target Property Mode Selector (Background vs Text vs Accent)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "TARGET ELEMENT ATTRIBUTE:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF94A3B8)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PixelColorTarget.values().forEach { target ->
                    val isSelected = activeTarget == target
                    val activeColor = when (target) {
                        PixelColorTarget.BACKGROUND -> bgParsed
                        PixelColorTarget.TEXT -> textParsed
                        PixelColorTarget.ACCENT -> accentParsed
                    }

                    Surface(
                        shape = RoundedCornerShape(0.dp),
                        color = if (isSelected) Color(0xFF1E2338) else Color(0xFF12141F),
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) BrandCyan else Color(0xFF2B3045)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                activeTarget = target
                                if (soundEnabled) playPixelAudioBlip(750.0, 0.03)
                            }
                            .testTag("target_mode_${target.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 7.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(activeColor)
                                    .border(1.dp, Color.White.copy(alpha = 0.5f))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = target.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Theme Carousel / Selector
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CLASSIC 'PIXEL-ART' PALETTES:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF94A3B8)
                )

                Text(
                    text = activeTheme.era,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = BrandAmber
                )
            }

            // Scrollable Themes Carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PixelArtPalettes.CLASSIC_THEMES.forEach { theme ->
                    val isCurrent = selectedThemeId == theme.id

                    Surface(
                        shape = RoundedCornerShape(0.dp),
                        color = if (isCurrent) Color(0xFF1E2436) else Color(0xFF131520),
                        border = BorderStroke(
                            if (isCurrent) 2.dp else 1.dp,
                            if (isCurrent) BrandAmber else Color(0xFF2E3348)
                        ),
                        modifier = Modifier
                            .width(136.dp)
                            .clickable {
                                selectedThemeId = theme.id
                                if (soundEnabled) playPixelAudioBlip(523.25, 0.04)
                            }
                            .testTag("theme_chip_${theme.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // Mini Color Bars
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .border(1.dp, Color(0xFF383E58))
                            ) {
                                theme.palette.take(4).forEach { hex ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxSize()
                                            .background(parsePixelHex(hex))
                                    )
                                }
                            }

                            Text(
                                text = theme.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isCurrent) BrandAmber else Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = theme.badge,
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF8E95AF)
                            )
                        }
                    }
                }
            }
        }

        // Chunky 4-Bit Pixel Swatches Matrix
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "SWATCHES (${activeTheme.name.uppercase()}):",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF94A3B8)
            )

            // Swatches Row / Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeTheme.palette.forEachIndexed { index, hex ->
                    val color = parsePixelHex(hex)
                    val activeVal = when (activeTarget) {
                        PixelColorTarget.BACKGROUND -> effectiveBg
                        PixelColorTarget.TEXT -> effectiveText
                        PixelColorTarget.ACCENT -> effectiveAccent
                    }
                    val isChosen = activeVal.equals(hex, ignoreCase = true)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // Chunky 4-bit Pixel Swatch Box with 3D Bevel
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(color)
                                .border(
                                    width = if (isChosen) 3.dp else 1.5.dp,
                                    color = if (isChosen) Color.White else Color(0xFF3B4058)
                                )
                                .clickable {
                                    if (soundEnabled) {
                                        val freq = 440.0 + (index * 75.0)
                                        playPixelAudioBlip(freq, 0.04)
                                    }
                                    onColorChanged(activeTarget, hex)
                                }
                                .testTag("swatch_${activeTheme.id}_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isChosen) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = if (isDarkColor(color)) Color.White else Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = hex.removePrefix("#").take(6),
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (isChosen) Color.White else Color(0xFF6E7594)
                        )
                    }
                }
            }
        }

        // Custom Hex / Manual Input Slot
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = manualHexInput,
                onValueChange = { manualHexInput = it },
                label = { Text("Custom Hex (#RRGGBB)", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                placeholder = { Text("#33FF33", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_pixel_custom_hex"),
                shape = RoundedCornerShape(0.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandCyan,
                    unfocusedBorderColor = Color(0xFF33384D),
                    focusedContainerColor = Color(0xFF131522),
                    unfocusedContainerColor = Color(0xFF131522),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color(0xFFE2E8F0)
                )
            )

            Button(
                onClick = {
                    val formatted = if (manualHexInput.startsWith("#")) manualHexInput else "#$manualHexInput"
                    if (formatted.length == 7) {
                        if (soundEnabled) playPixelAudioBlip(880.0, 0.05)
                        onColorChanged(activeTarget, formatted.uppercase())
                        manualHexInput = ""
                    }
                },
                enabled = manualHexInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.testTag("btn_apply_custom_hex")
            ) {
                Text(
                    text = "SET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black
                )
            }
        }
    }
}

/**
 * Compact chip for displaying a color hex code and visual preview.
 */
@Composable
private fun ColorStatChip(
    label: String,
    hex: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color)
                .border(1.dp, Color.White.copy(alpha = 0.5f))
        )
        Text(
            text = "$label: ${hex.ifBlank { "NONE" }}",
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFCBD5E1)
        )
    }
}

/**
 * Modal Dialog wrapper for the Pixel-Art 4-Bit Color Picker.
 */
@Composable
fun Pixel4BitColorPickerDialog(
    currentBgHex: String,
    currentTextHex: String,
    currentAccentHex: String = "",
    onDismiss: () -> Unit,
    onColorChanged: (target: PixelColorTarget, hex: String) -> Unit,
    onApplyFullTheme: (theme: PixelArtTheme) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            color = Color(0xFF0A0C14),
            shape = RoundedCornerShape(0.dp),
            border = BorderStroke(2.dp, Color(0xFF4B5270))
        ) {
            Column {
                // Header with close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF131724))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👾 4-BIT PIXEL PALETTE CONFIGURATOR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = BrandAmber
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFF282D42))

                Pixel4BitColorPicker(
                    currentBgHex = currentBgHex,
                    currentTextHex = currentTextHex,
                    currentAccentHex = currentAccentHex,
                    onColorChanged = onColorChanged,
                    onApplyFullTheme = { theme ->
                        onApplyFullTheme(theme)
                    }
                )
            }
        }
    }
}

private fun parsePixelHex(hex: String): Color {
    return try {
        if (hex.isBlank()) return Color.Transparent
        val clean = if (hex.startsWith("#")) hex else "#$hex"
        Color(android.graphics.Color.parseColor(clean))
    } catch (_: Throwable) {
        Color(0xFF0F380F)
    }
}

private fun isDarkColor(color: Color): Boolean {
    val luminance = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
    return luminance < 0.5f
}
