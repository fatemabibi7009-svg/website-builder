package com.example.ui.components

import android.graphics.Bitmap
import kotlin.math.roundToInt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.generator.AnimePalettePreset
import com.example.generator.DitherAlgorithm
import com.example.generator.DitherConfig
import com.example.generator.PixelDitherEngine
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandRose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Curated Quick Anime Presets for instant 1-tap style transformation.
 */
data class AnimeQuickPreset(
    val name: String,
    val icon: String,
    val config: DitherConfig,
    val description: String
)

val ANIME_QUICK_PRESETS = listOf(
    AnimeQuickPreset(
        name = "PC-98 Anime VN",
        icon = "🌸",
        config = DitherConfig(
            algorithm = DitherAlgorithm.BAYER_4X4,
            palette = AnimePalettePreset.PC98_ANIME,
            pixelScale = 2,
            contrastBoost = 1.15f,
            saturationBoost = 1.25f,
            edgeEnhance = true,
            ditherStrength = 0.85f,
            scanlines = false
        ),
        description = "Iconic PC-98 visual novel crosshatch aesthetic with peach anime skin & violet shadows"
    ),
    AnimeQuickPreset(
        name = "Akiba Cyber Neon",
        icon = "⚡",
        config = DitherConfig(
            algorithm = DitherAlgorithm.ATKINSON,
            palette = AnimePalettePreset.NEO_TOKYO_CYBER,
            pixelScale = 2,
            contrastBoost = 1.25f,
            saturationBoost = 1.4f,
            edgeEnhance = true,
            ditherStrength = 0.9f,
            scanlines = false
        ),
        description = "Sharp Atkinson linework with vivid synthwave magenta & laser cyan highlights"
    ),
    AnimeQuickPreset(
        name = "Manga Screentone",
        icon = "📖",
        config = DitherConfig(
            algorithm = DitherAlgorithm.MANGA_HALFTONE,
            palette = AnimePalettePreset.MANGA_SCREENTONE,
            pixelScale = 2,
            contrastBoost = 1.3f,
            saturationBoost = 0.0f,
            edgeEnhance = true,
            ditherStrength = 0.95f,
            scanlines = false
        ),
        description = "4-level newsprint screentone dots mimicking traditional Japanese doujinshi & manga prints"
    ),
    AnimeQuickPreset(
        name = "90s City Pop Pastel",
        icon = "📼",
        config = DitherConfig(
            algorithm = DitherAlgorithm.FLOYD_STEINBERG,
            palette = AnimePalettePreset.CITY_POP_PASTEL,
            pixelScale = 2,
            contrastBoost = 1.1f,
            saturationBoost = 1.15f,
            edgeEnhance = true,
            ditherStrength = 0.8f,
            scanlines = false
        ),
        description = "Smooth error diffusion with nostalgic warm pastel tones from 90s anime"
    ),
    AnimeQuickPreset(
        name = "Famicom 8-Bit",
        icon = "🎮",
        config = DitherConfig(
            algorithm = DitherAlgorithm.BAYER_8X8,
            palette = AnimePalettePreset.NES_FAMICOM,
            pixelScale = 3,
            contrastBoost = 1.2f,
            saturationBoost = 1.2f,
            edgeEnhance = false,
            ditherStrength = 0.9f,
            scanlines = false
        ),
        description = "Authentic 1983 Nintendo console palette with chunky pixel blocks"
    ),
    AnimeQuickPreset(
        name = "Game Boy DMG",
        icon = "👾",
        config = DitherConfig(
            algorithm = DitherAlgorithm.ATKINSON,
            palette = AnimePalettePreset.GAMEBOY_DMG,
            pixelScale = 3,
            contrastBoost = 1.25f,
            saturationBoost = 0.0f,
            edgeEnhance = true,
            ditherStrength = 0.9f,
            scanlines = true,
            scanlineIntensity = 0.2f
        ),
        description = "Authentic 4-shade olive green dot-matrix LCD with subtle horizontal scanlines"
    ),
    AnimeQuickPreset(
        name = "Anime Cel-Shading",
        icon = "🎨",
        config = DitherConfig(
            algorithm = DitherAlgorithm.POSTERIZE_FLAT,
            palette = AnimePalettePreset.PC98_ANIME,
            pixelScale = 1,
            contrastBoost = 1.15f,
            saturationBoost = 1.2f,
            edgeEnhance = true,
            ditherStrength = 0.0f,
            scanlines = false
        ),
        description = "Crisp 16-color cel shading with zero stippling, delivering pure anime animation frames"
    )
)

@Composable
fun Anime4BitDitherDialog(
    sourceImageUrl: String,
    onDismiss: () -> Unit,
    onApplyDither: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialConfig: DitherConfig = ANIME_QUICK_PRESETS.first().config
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Config states
    var selectedAlgorithm by remember { mutableStateOf(initialConfig.algorithm) }
    var selectedPalette by remember { mutableStateOf(initialConfig.palette) }
    var pixelScale by remember { mutableIntStateOf(initialConfig.pixelScale) }
    var contrastBoost by remember { mutableFloatStateOf(initialConfig.contrastBoost) }
    var saturationBoost by remember { mutableFloatStateOf(initialConfig.saturationBoost) }
    var edgeEnhance by remember { mutableStateOf(initialConfig.edgeEnhance) }
    var ditherStrength by remember { mutableFloatStateOf(initialConfig.ditherStrength) }
    var scanlines by remember { mutableStateOf(initialConfig.scanlines) }
    var scanlineIntensity by remember { mutableFloatStateOf(initialConfig.scanlineIntensity) }

    // Live state
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewDitheredBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingSource by remember { mutableStateOf(true) }
    var isProcessingPreview by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var showOriginalCompare by remember { mutableStateOf(false) }
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Presets & Palette, 1 = Dither & Pixel Grid, 2 = Tuning & CRT
    var soundEnabled by remember { mutableStateOf(true) }

    // Debounced preview processor job
    var previewJob by remember { mutableStateOf<Job?>(null) }

    // 1. Initial Load of Source Image Bitmap
    LaunchedEffect(sourceImageUrl) {
        isLoadingSource = true
        withContext(Dispatchers.IO) {
            val bmp = PixelDitherEngine.loadBitmapFromPathOrUrl(context, sourceImageUrl, maxDimension = 600)
            sourceBitmap = bmp
            isLoadingSource = false
        }
    }

    // 2. Reactive Preview Dither Generator
    fun triggerPreviewUpdate() {
        val src = sourceBitmap ?: return
        previewJob?.cancel()
        previewJob = coroutineScope.launch {
            isProcessingPreview = true
            delay(40) // debounce rapid slider movements
            val currentConfig = DitherConfig(
                algorithm = selectedAlgorithm,
                palette = selectedPalette,
                pixelScale = pixelScale,
                contrastBoost = contrastBoost,
                saturationBoost = saturationBoost,
                edgeEnhance = edgeEnhance,
                ditherStrength = ditherStrength,
                scanlines = scanlines,
                scanlineIntensity = scanlineIntensity
            )
            val dithered = withContext(Dispatchers.Default) {
                PixelDitherEngine.processBitmap(src, currentConfig)
            }
            previewDitheredBitmap = dithered
            isProcessingPreview = false
        }
    }

    LaunchedEffect(
        sourceBitmap,
        selectedAlgorithm,
        selectedPalette,
        pixelScale,
        contrastBoost,
        saturationBoost,
        edgeEnhance,
        ditherStrength,
        scanlines,
        scanlineIntensity
    ) {
        if (sourceBitmap != null) {
            triggerPreviewUpdate()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(10.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, Color(0xFF00F0FF), RoundedCornerShape(16.dp))
                .testTag("anime_4bit_dither_dialog"),
            color = Color(0xFF0B0716),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF007F).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFFF007F), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👾", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "4-BIT ANIME DITHER STUDIO",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFF00F0FF)
                            )
                            Text(
                                text = "PC-98 & Retro Anime Post-Processing Filter",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                soundEnabled = !soundEnabled
                                if (soundEnabled) playPixelAudioBlip(880.0, 0.04)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Toggle Audio",
                                tint = if (soundEnabled) BrandAmber else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Visual Preview Screen with CRT Frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF020205))
                            .border(2.dp, Color(0xFF282D42), RoundedCornerShape(12.dp))
                    ) {
                        when {
                            isLoadingSource -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF00F0FF), modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Decoding image stream...", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                }
                            }
                            showOriginalCompare -> {
                                // Display Original
                                AsyncImage(
                                    model = sourceImageUrl,
                                    contentDescription = "Original Source Image",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ORIGINAL IMAGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            previewDitheredBitmap != null -> {
                                Image(
                                    bitmap = previewDitheredBitmap!!.asImageBitmap(),
                                    contentDescription = "4-Bit Dithered Anime Preview",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Bottom Overlay: Live Specs Badge & Compare Toggle
                        Surface(
                            color = Color(0xFF0B0318).copy(alpha = 0.85f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${selectedPalette.era} • ${selectedAlgorithm.badge} • ${pixelScale}x Block",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00F0FF)
                                    )
                                    if (isProcessingPreview) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        CircularProgressIndicator(
                                            color = Color(0xFFFF007F),
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            showOriginalCompare = !showOriginalCompare
                                            if (soundEnabled) playPixelAudioBlip(750.0, 0.03)
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (showOriginalCompare) Color(0xFFFF007F) else Color.White
                                        ),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Compare,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (showOriginalCompare) "View Dither" else "Compare Original",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Anime Style Presets Row
                    Text(
                        text = "⚡ QUICK ANIME PRESETS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = BrandAmber
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (preset in ANIME_QUICK_PRESETS) {
                            val isSelected = selectedAlgorithm == preset.config.algorithm &&
                                    selectedPalette == preset.config.palette &&
                                    pixelScale == preset.config.pixelScale

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFFFF007F).copy(alpha = 0.25f) else Color(0xFF131724),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFFFF007F) else Color(0xFF282D42)
                                ),
                                modifier = Modifier
                                    .clickable {
                                        if (soundEnabled) playPixelAudioBlip(987.77, 0.04)
                                        selectedAlgorithm = preset.config.algorithm
                                        selectedPalette = preset.config.palette
                                        pixelScale = preset.config.pixelScale
                                        contrastBoost = preset.config.contrastBoost
                                        saturationBoost = preset.config.saturationBoost
                                        edgeEnhance = preset.config.edgeEnhance
                                        ditherStrength = preset.config.ditherStrength
                                        scanlines = preset.config.scanlines
                                        scanlineIntensity = preset.config.scanlineIntensity
                                    }
                                    .testTag("preset_${preset.name.replace(" ", "_")}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(preset.icon, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = preset.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                        )
                                        Text(
                                            text = preset.config.palette.era,
                                            fontSize = 9.sp,
                                            color = if (isSelected) Color(0xFF00F0FF) else Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Segmented Control Tabs for Detailed Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF131724))
                            .padding(2.dp)
                    ) {
                        listOf("🎨 Palette & Algo", "🔲 Pixel & Density", "✨ Linework & CRT").forEachIndexed { idx, tabTitle ->
                            val isTabSelected = activeTab == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isTabSelected) Color(0xFF00F0FF).copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable {
                                        if (soundEnabled) playPixelAudioBlip(750.0, 0.02)
                                        activeTab = idx
                                    }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tabTitle,
                                    fontSize = 11.sp,
                                    fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isTabSelected) Color(0xFF00F0FF) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab 0: Palette & Algorithm
                    if (activeTab == 0) {
                        Column {
                            Text(
                                text = "DITHER ALGORITHM",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (algo in DitherAlgorithm.values()) {
                                    val isChosen = selectedAlgorithm == algo
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isChosen) Color(0xFF1A0836) else Color(0xFF0E121E),
                                        border = BorderStroke(1.dp, if (isChosen) Color(0xFF00F0FF) else Color(0xFF1E2436)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (soundEnabled) playPixelAudioBlip(880.0, 0.03)
                                                selectedAlgorithm = algo
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isChosen) Color(0xFF00F0FF) else Color(0xFF282D42)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isChosen) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = algo.displayName,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isChosen) Color.White else Color(0xFFCBD5E1)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(3.dp))
                                                            .background(Color(0xFF282D42))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(algo.badge, fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Text(
                                                    text = algo.description,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF64748B),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "4-BIT COLOR PALETTE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (pal in AnimePalettePreset.values()) {
                                    if (pal == AnimePalettePreset.CUSTOM) continue
                                    val isChosen = selectedPalette == pal
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isChosen) Color(0xFF1E1030) else Color(0xFF0E121E),
                                        border = BorderStroke(1.dp, if (isChosen) Color(0xFFFF007F) else Color(0xFF1E2436)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (soundEnabled) playPixelAudioBlip(1046.5, 0.03)
                                                selectedPalette = pal
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = pal.title,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isChosen) Color.White else Color(0xFFCBD5E1)
                                                )
                                                Text(
                                                    text = "${pal.era} • ${pal.hexColors.size} shades",
                                                    fontSize = 10.sp,
                                                    color = if (isChosen) Color(0xFFFF007F) else Color(0xFF64748B)
                                                )
                                            }

                                            // Swatch dots preview
                                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                pal.hexColors.take(8).forEach { hex ->
                                                    val color = try {
                                                        Color(android.graphics.Color.parseColor(hex))
                                                    } catch (_: Throwable) {
                                                        Color.Black
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clip(RoundedCornerShape(3.dp))
                                                            .background(color)
                                                            .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Tab 1: Pixel Size & Dither Density
                    if (activeTab == 1) {
                        Column {
                            Text(
                                text = "PIXEL BLOCK RESOLUTION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    1 to "1x Crisp HD",
                                    2 to "2x Anime Pixel",
                                    3 to "3x PC-98 Retro",
                                    4 to "4x Arcade 160p",
                                    5 to "5x Chunky 8-Bit"
                                ).forEach { (scale, label) ->
                                    val isSelected = pixelScale == scale
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) Color(0xFF00F0FF) else Color(0xFF131724),
                                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF00F0FF) else Color(0xFF282D42)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                if (soundEnabled) playPixelAudioBlip(700.0 + scale * 80.0, 0.03)
                                                pixelScale = scale
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "${scale}X",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isSelected) Color.Black else Color.White
                                            )
                                            Text(
                                                text = label.split(" ").last(),
                                                fontSize = 9.sp,
                                                color = if (isSelected) Color.Black else Color(0xFF94A3B8),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Dither Strength Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DITHER STRENGTH / STIPPLING",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "${(ditherStrength * 100).roundToInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF00F0FF)
                                )
                            }
                            Slider(
                                value = ditherStrength,
                                onValueChange = { ditherStrength = it },
                                valueRange = 0.0f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF00F0FF),
                                    activeTrackColor = Color(0xFF00F0FF),
                                    inactiveTrackColor = Color(0xFF282D42)
                                )
                            )
                        }
                    }

                    // Tab 2: Linework, Contrast & CRT
                    if (activeTab == 2) {
                        Column {
                            // Edge Enhance / Anime Linework Switch
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF131724),
                                border = BorderStroke(1.dp, Color(0xFF282D42)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Anime Cel-Linework Sharpening",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Emphasizes eyes, hair strands & character contours before dither quantization",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    Switch(
                                        checked = edgeEnhance,
                                        onCheckedChange = {
                                            if (soundEnabled) playPixelAudioBlip(880.0, 0.03)
                                            edgeEnhance = it
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFF00F0FF),
                                            checkedTrackColor = Color(0xFF00F0FF).copy(alpha = 0.4f)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Scanlines Switch
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF131724),
                                border = BorderStroke(1.dp, Color(0xFF282D42)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "CRT 15kHz Scanlines",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Adds authentic retro monitor horizontal cathode scanline lines",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    Switch(
                                        checked = scanlines,
                                        onCheckedChange = {
                                            if (soundEnabled) playPixelAudioBlip(880.0, 0.03)
                                            scanlines = it
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFFFF007F),
                                            checkedTrackColor = Color(0xFFFF007F).copy(alpha = 0.4f)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Contrast boost
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("CONTRAST BOOST", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                                Text("${(contrastBoost * 100).roundToInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = BrandAmber)
                            }
                            Slider(
                                value = contrastBoost,
                                onValueChange = { contrastBoost = it },
                                valueRange = 0.7f..1.8f,
                                colors = SliderDefaults.colors(
                                    thumbColor = BrandAmber,
                                    activeTrackColor = BrandAmber,
                                    inactiveTrackColor = Color(0xFF282D42)
                                )
                            )

                            // Saturation boost
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("SATURATION BOOST", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                                Text("${(saturationBoost * 100).roundToInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFFFF007F))
                            }
                            Slider(
                                value = saturationBoost,
                                onValueChange = { saturationBoost = it },
                                valueRange = 0.0f..2.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFFF007F),
                                    activeTrackColor = Color(0xFFFF007F),
                                    inactiveTrackColor = Color(0xFF282D42)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF1E2436))
                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }

                    Button(
                        onClick = {
                            isExporting = true
                            if (soundEnabled) playPixelAudioBlip(1200.0, 0.06)
                            coroutineScope.launch {
                                val finalConfig = DitherConfig(
                                    algorithm = selectedAlgorithm,
                                    palette = selectedPalette,
                                    pixelScale = pixelScale,
                                    contrastBoost = contrastBoost,
                                    saturationBoost = saturationBoost,
                                    edgeEnhance = edgeEnhance,
                                    ditherStrength = ditherStrength,
                                    scanlines = scanlines,
                                    scanlineIntensity = scanlineIntensity
                                )
                                val savedUri = PixelDitherEngine.processAndSaveImage(context, sourceImageUrl, finalConfig)
                                isExporting = false
                                if (savedUri != null) {
                                    if (soundEnabled) playPixelAudioBlip(1567.98, 0.08)
                                    onApplyDither(savedUri)
                                    onDismiss()
                                }
                            }
                        },
                        enabled = !isExporting && sourceBitmap != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00F0FF)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_apply_4bit_dither")
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exporting 4-Bit...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Apply 4-Bit Dither",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
