package com.example.generator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * 4-Bit Dithering Algorithms matching vintage retro anime, PC-98, and classic computing aesthetics.
 */
enum class DitherAlgorithm(val displayName: String, val badge: String, val description: String) {
    BAYER_4X4(
        displayName = "Bayer 4x4 (PC-98 Anime VN)",
        badge = "PC-98",
        description = "Iconic ordered crosshatch dither seen in 90s Japanese PC-98 visual novels and anime adventure games"
    ),
    BAYER_8X8(
        displayName = "Bayer 8x8 (Fine Matrix)",
        badge = "8X8",
        description = "Higher-density ordered matrix with smooth graduation and subtle checkerboard texture"
    ),
    ATKINSON(
        displayName = "Atkinson (Anime Linework)",
        badge = "ATKINSON",
        description = "Bill Atkinson error-diffusion: preserves sharp character eyes, outlines, and hair highlights without bleed"
    ),
    FLOYD_STEINBERG(
        displayName = "Floyd-Steinberg (Smooth Gradient)",
        badge = "F-S",
        description = "Classic balanced error diffusion delivering smooth color transitions and rich photographic depth"
    ),
    MANGA_HALFTONE(
        displayName = "Manga Screentone (Dot Pattern)",
        badge = "MANGA",
        description = "Authentic printed Japanese manga screentone dots mimicking printed doujinshi and Shonen magazine pages"
    ),
    SIERRA_LITE(
        displayName = "Sierra Lite (Fast Diffusion)",
        badge = "SIERRA",
        description = "Lightweight two-line error diffusion with clean pixel definition and minimal artifacts"
    ),
    POSTERIZE_FLAT(
        displayName = "Solid Cel-Shading (No Dither)",
        badge = "CEL-4B",
        description = "Pure color quantization to 16 4-bit shades without stippling, delivering clean anime cel-shading"
    )
}

/**
 * Curated 4-Bit & Retro Anime Color Palettes.
 * Each palette contains authentic 16-color (or 4-shade) hex color codes.
 */
enum class AnimePalettePreset(
    val id: String,
    val title: String,
    val era: String,
    val hexColors: List<String>,
    val description: String
) {
    PC98_ANIME(
        id = "pc98_anime",
        title = "NEC PC-9801 Anime (16-Color)",
        era = "1992 PC-9821 VN",
        description = "Authentic Japanese visual novel palette: deep midnight violets, anime peach skin tones, cyan, and vibrant highlights",
        hexColors = listOf(
            "#000000", "#1C1C38", "#493C6E", "#83629E",
            "#B88AC9", "#E8BCE8", "#FFFFFF", "#3A5C94",
            "#5C96CF", "#94D2F2", "#1C6B49", "#44B06E",
            "#A4E88A", "#BD5A3A", "#E69C5A", "#F8DC8A"
        )
    ),
    NEO_TOKYO_CYBER(
        id = "neo_tokyo",
        title = "Akiba Cyber Neon (16-Color)",
        era = "PC-98 & City Pop",
        description = "Vibrant synthwave neon palette: electric magenta, cyan laser, arcade violet, and vivid anime highlights",
        hexColors = listOf(
            "#0B0318", "#1A0836", "#2D1B4E", "#58287F",
            "#8C38B0", "#D04EC6", "#FF007F", "#FF5C9D",
            "#00F0FF", "#5DF9FF", "#39FF14", "#A2FF70",
            "#FFE600", "#FFF475", "#F0E6FF", "#FFFFFF"
        )
    ),
    MANGA_SCREENTONE(
        id = "manga_screen",
        title = "Manga Screentone (4-Level)",
        era = "Shonen Print Tone",
        description = "Pure Japanese manga ink tones: Newsprint paper white, 25% screen gray, 65% dense dot shadow, and deep black ink",
        hexColors = listOf(
            "#111111", "#555555", "#AAAAAA", "#F6F6F4"
        )
    ),
    CITY_POP_PASTEL(
        id = "city_pop",
        title = "90s City Pop Pastel (16-Color)",
        era = "1994 Magical Girl",
        description = "Soft, warm pastel tones reminiscent of 90s magical anime, nostalgic summer city pop, and retro vaporwave",
        hexColors = listOf(
            "#1B1626", "#3A2A45", "#6B4968", "#A66D88",
            "#D896A8", "#F7C4C0", "#FFF0EA", "#405075",
            "#6C89A8", "#A3C4D4", "#527568", "#82A88D",
            "#BFD6B8", "#CCA17A", "#E5C89C", "#FFFFFF"
        )
    ),
    GAMEBOY_DMG(
        id = "gameboy_dmg",
        title = "Game Boy DMG-01 (4-Shade Olive)",
        era = "1989 Nintendo DMG",
        description = "Iconic 4-shade pea-soup green reflective LCD dot-matrix",
        hexColors = listOf(
            "#0F380F", "#306230", "#8BAC0F", "#9BBC0F"
        )
    ),
    GAMEBOY_POCKET(
        id = "gameboy_pocket",
        title = "Pocket Monochrome (4-Level Gray)",
        era = "1996 Silver LCD",
        description = "Crisp, high-contrast 4-level grayscale reflective dot-matrix",
        hexColors = listOf(
            "#141414", "#4C5350", "#949C94", "#E0E5D0"
        )
    ),
    NES_FAMICOM(
        id = "nes_famicom",
        title = "Famicom Anime (16-Color)",
        era = "1983 8-Bit Cartridge",
        description = "Iconic Nintendo Famicom / NES arcade palette with bold primaries and classic anime sprites",
        hexColors = listOf(
            "#000000", "#7C7C7C", "#BCBCBC", "#FCFCFC",
            "#0000FC", "#0078F8", "#3CBCFC", "#00A800",
            "#58D854", "#D82800", "#F87858", "#F85898",
            "#FC9838", "#FCE0A8", "#6888FC", "#9878F8"
        )
    ),
    PICO8_RETRO(
        id = "pico8_fantasy",
        title = "PICO-8 Fantasy (16-Color)",
        era = "Chiptune Console",
        description = "Harmonious indie fantasy console 16-color palette with warm character and balanced contrast",
        hexColors = listOf(
            "#000000", "#1D2B53", "#7E2553", "#008751",
            "#AB5236", "#5F574F", "#C2C3C7", "#FFF1E8",
            "#FF004D", "#FFA300", "#FFEC27", "#00E436",
            "#29ADFF", "#83769C", "#FF77A8", "#FFCCAA"
        )
    ),
    CGA_CYBERPUNK(
        id = "cga_cyber",
        title = "CGA Mode 1 (Cyber 4-Color)",
        era = "1981 IBM PC DOS",
        description = "Classic high-contrast 4-color PC DOS: Electric Cyan, Hot Magenta, White and Black",
        hexColors = listOf(
            "#000000", "#55FFFF", "#FF55FF", "#FFFFFF"
        )
    ),
    PHOSPHOR_GREEN(
        id = "phosphor_green",
        title = "Phosphor CRT Glow (5-Shade)",
        era = "1982 Mainframe Terminal",
        description = "Luminescent cyberpunk green phosphor tube CRT terminal monitor glow",
        hexColors = listOf(
            "#041006", "#0A420A", "#168B16", "#33FF33", "#A3FFA3"
        )
    ),
    CUSTOM(
        id = "custom",
        title = "Custom Editor Palette",
        era = "User Palette",
        description = "Custom colors configured through the 4-bit pixel theme editor",
        hexColors = emptyList()
    )
}

/**
 * Full configuration for 4-Bit Dither post-processing.
 */
data class DitherConfig(
    val algorithm: DitherAlgorithm = DitherAlgorithm.BAYER_4X4,
    val palette: AnimePalettePreset = AnimePalettePreset.PC98_ANIME,
    val customColorsHex: List<String>? = null,
    val pixelScale: Int = 2, // 1 = Crisp 1:1, 2 = HD Pixel Art, 3 = Retro PC-98, 4 = Arcade 160p, 5 = Chunky 8-Bit
    val contrastBoost: Float = 1.15f, // 0.7f to 1.8f
    val saturationBoost: Float = 1.25f, // 0.0f (monochrome) to 2.0f
    val edgeEnhance: Boolean = true, // Sharpen linework prior to dither for anime cel lines
    val ditherStrength: Float = 0.85f, // 0.0f (pure posterize) to 1.0f (full dither)
    val scanlines: Boolean = false, // Subtle CRT scanlines overlay
    val scanlineIntensity: Float = 0.18f
)

/**
 * High-performance 4-Bit Anime Post-Processing Dither Engine.
 * Features fast integer arithmetic, precomputed color lookup, and authentic Japanese retro algorithms.
 */
object PixelDitherEngine {

    // 4x4 Bayer Matrix
    private val BAYER_4X4 = intArrayOf(
        0, 8, 2, 10,
        12, 4, 14, 6,
        3, 11, 1, 9,
        15, 7, 13, 5
    )

    // 8x8 Bayer Matrix
    private val BAYER_8X8 = intArrayOf(
        0, 32, 8, 40, 2, 34, 10, 42,
        48, 16, 56, 24, 50, 18, 58, 26,
        12, 44, 4, 36, 14, 46, 6, 38,
        60, 28, 52, 20, 62, 30, 54, 22,
        3, 35, 11, 43, 1, 33, 9, 41,
        51, 19, 59, 27, 49, 17, 57, 25,
        15, 47, 7, 39, 13, 45, 5, 37,
        63, 31, 55, 23, 61, 29, 53, 21
    )

    /**
     * Resolves palette color integers from config.
     */
    fun resolvePaletteColors(config: DitherConfig): IntArray {
        val hexList = if (config.palette == AnimePalettePreset.CUSTOM && !config.customColorsHex.isNullOrEmpty()) {
            config.customColorsHex
        } else {
            config.palette.hexColors
        }

        if (hexList.isEmpty()) {
            return AnimePalettePreset.PC98_ANIME.hexColors.map { parseHexColor(it) }.toIntArray()
        }

        return hexList.map { parseHexColor(it) }.toIntArray()
    }

    private fun parseHexColor(hex: String): Int {
        return try {
            val clean = if (hex.startsWith("#")) hex else "#$hex"
            AndroidColor.parseColor(clean)
        } catch (_: Throwable) {
            AndroidColor.BLACK
        }
    }

    /**
     * Finds the nearest color in the given palette using weighted perceptual distance.
     * Weights: 0.299 Red, 0.587 Green, 0.114 Blue (Rec. 601 Luminance standard)
     */
    fun findNearestColor(r: Int, g: Int, b: Int, palette: IntArray): Int {
        var bestColor = palette[0]
        var minDistance = Long.MAX_VALUE

        val cr = r.coerceIn(0, 255)
        val cg = g.coerceIn(0, 255)
        val cb = b.coerceIn(0, 255)

        for (color in palette) {
            val pr = (color shr 16) and 0xFF
            val pg = (color shr 8) and 0xFF
            val pb = color and 0xFF

            val dr = cr - pr
            val dg = cg - pg
            val db = cb - pb

            // Weighted Euclidean distance in Rec.601 color space
            val dist = (dr * dr * 299L) + (dg * dg * 587L) + (db * db * 114L)
            if (dist < minDistance) {
                minDistance = dist
                bestColor = color
                if (dist == 0L) break
            }
        }

        return bestColor
    }

    /**
     * Core Dither Processor. Takes a source Bitmap and applies the complete 4-bit pipeline.
     */
    fun processBitmap(sourceBitmap: Bitmap, config: DitherConfig): Bitmap {
        val srcW = sourceBitmap.width
        val srcH = sourceBitmap.height

        val scale = config.pixelScale.coerceAtLeast(1)
        val targetW = (srcW / scale).coerceAtLeast(16)
        val targetH = (srcH / scale).coerceAtLeast(16)

        // 1. Resample down to pixel grid if scale > 1
        val scaledBitmap = if (scale > 1) {
            Bitmap.createScaledBitmap(sourceBitmap, targetW, targetH, true)
        } else {
            sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        val w = scaledBitmap.width
        val h = scaledBitmap.height
        val pixels = IntArray(w * h)
        scaledBitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        // 2. Pre-process: Contrast, Saturation, and Anime Linework Sharpening
        preProcessImage(pixels, w, h, config)

        // 3. Resolve target palette
        val palette = resolvePaletteColors(config)

        // 4. Execute dithering algorithm
        when (config.algorithm) {
            DitherAlgorithm.BAYER_4X4 -> applyBayerOrderedDither(pixels, w, h, palette, is8x8 = false, config.ditherStrength)
            DitherAlgorithm.BAYER_8X8 -> applyBayerOrderedDither(pixels, w, h, palette, is8x8 = true, config.ditherStrength)
            DitherAlgorithm.ATKINSON -> applyAtkinsonDither(pixels, w, h, palette, config.ditherStrength)
            DitherAlgorithm.FLOYD_STEINBERG -> applyFloydSteinbergDither(pixels, w, h, palette, config.ditherStrength)
            DitherAlgorithm.MANGA_HALFTONE -> applyMangaHalftoneDither(pixels, w, h, palette, config.ditherStrength)
            DitherAlgorithm.SIERRA_LITE -> applySierraLiteDither(pixels, w, h, palette, config.ditherStrength)
            DitherAlgorithm.POSTERIZE_FLAT -> applyFlatPosterize(pixels, w, h, palette)
        }

        // 5. Optional CRT scanlines
        if (config.scanlines) {
            applyScanlines(pixels, w, h, config.scanlineIntensity)
        }

        // 6. Write back to bitmap
        val processedSmall = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        processedSmall.setPixels(pixels, 0, w, 0, 0, w, h)

        // 7. Upscale back to original dimension with NEAREST-NEIGHBOR interpolation for crisp pixel blocks!
        return if (scale > 1) {
            val finalW = targetW * scale
            val finalH = targetH * scale
            val upscaled = Bitmap.createScaledBitmap(processedSmall, finalW, finalH, false)
            if (processedSmall != scaledBitmap) processedSmall.recycle()
            if (scaledBitmap != sourceBitmap) scaledBitmap.recycle()
            upscaled
        } else {
            processedSmall
        }
    }

    /**
     * Pre-adjusts contrast, saturation, and applies an edge sharpening kernel for anime line art.
     */
    private fun preProcessImage(pixels: IntArray, w: Int, h: Int, config: DitherConfig) {
        val contrast = config.contrastBoost
        val saturation = config.saturationBoost
        val hasAdjustments = contrast != 1.0f || saturation != 1.0f

        if (hasAdjustments) {
            for (i in pixels.indices) {
                val color = pixels[i]
                val a = (color ushr 24) and 0xFF
                var r = (color shr 16) and 0xFF
                var g = (color shr 8) and 0xFF
                var b = color and 0xFF

                // Contrast
                if (contrast != 1.0f) {
                    r = ((r - 128) * contrast + 128).roundToInt().coerceIn(0, 255)
                    g = ((g - 128) * contrast + 128).roundToInt().coerceIn(0, 255)
                    b = ((b - 128) * contrast + 128).roundToInt().coerceIn(0, 255)
                }

                // Saturation
                if (saturation != 1.0f) {
                    val lum = 0.299f * r + 0.587f * g + 0.114f * b
                    r = (lum + (r - lum) * saturation).roundToInt().coerceIn(0, 255)
                    g = (lum + (g - lum) * saturation).roundToInt().coerceIn(0, 255)
                    b = (lum + (b - lum) * saturation).roundToInt().coerceIn(0, 255)
                }

                pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        // Anime Linework Enhancement (Sharpening filter to keep eyes and outlines crisp)
        if (config.edgeEnhance && w > 2 && h > 2) {
            val copy = pixels.clone()
            for (y in 1 until h - 1) {
                val rowOffset = y * w
                for (x in 1 until w - 1) {
                    val idx = rowOffset + x
                    val center = copy[idx]
                    val top = copy[idx - w]
                    val bottom = copy[idx + w]
                    val left = copy[idx - 1]
                    val right = copy[idx + 1]

                    fun sharpenChannel(cIdx: Int): Int {
                        val cVal = (center shr cIdx) and 0xFF
                        val tVal = (top shr cIdx) and 0xFF
                        val bVal = (bottom shr cIdx) and 0xFF
                        val lVal = (left shr cIdx) and 0xFF
                        val rVal = (right shr cIdx) and 0xFF
                        // 3x3 Laplace sharpen: 5*center - top - bottom - left - right
                        val res = (5 * cVal - tVal - bVal - lVal - rVal)
                        return res.coerceIn(0, 255)
                    }

                    val nr = sharpenChannel(16)
                    val ng = sharpenChannel(8)
                    val nb = sharpenChannel(0)
                    val a = (center ushr 24) and 0xFF
                    pixels[idx] = (a shl 24) or (nr shl 16) or (ng shl 8) or nb
                }
            }
        }
    }

    /**
     * Bayer Ordered Dithering (PC-98 & Classic Computing).
     * Applies deterministic matrix threshold offsets to create iconic crosshatch dithering.
     */
    private fun applyBayerOrderedDither(
        pixels: IntArray,
        w: Int,
        h: Int,
        palette: IntArray,
        is8x8: Boolean,
        strength: Float
    ) {
        val matrix = if (is8x8) BAYER_8X8 else BAYER_4X4
        val size = if (is8x8) 8 else 4
        val div = if (is8x8) 64f else 16f
        val spread = 54f * strength.coerceIn(0.1f, 1.5f)

        for (y in 0 until h) {
            val rowOffset = y * w
            val matrixRow = (y % size) * size
            for (x in 0 until w) {
                val idx = rowOffset + x
                val c = pixels[idx]
                val a = (c ushr 24) and 0xFF
                if (a < 10) continue

                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF

                val matrixVal = matrix[matrixRow + (x % size)]
                val offset = ((matrixVal / div) - 0.5f) * spread

                val dr = (r + offset).roundToInt().coerceIn(0, 255)
                val dg = (g + offset).roundToInt().coerceIn(0, 255)
                val db = (b + offset).roundToInt().coerceIn(0, 255)

                val nearest = findNearestColor(dr, dg, db, palette)
                pixels[idx] = (a shl 24) or (nearest and 0x00FFFFFF)
            }
        }
    }

    /**
     * Atkinson Error Diffusion.
     * Propagates 3/4 of quantization error across 6 surrounding pixels (1/8 each).
     * Discarding 2/8 of error preserves razor-sharp anime line art and prevents bleeding!
     */
    private fun applyAtkinsonDither(
        pixels: IntArray,
        w: Int,
        h: Int,
        palette: IntArray,
        strength: Float
    ) {
        // Working floating-point error buffers
        val rBuf = FloatArray(w * h)
        val gBuf = FloatArray(w * h)
        val bBuf = FloatArray(w * h)

        for (i in pixels.indices) {
            val c = pixels[i]
            rBuf[i] = ((c shr 16) and 0xFF).toFloat()
            gBuf[i] = ((c shr 8) and 0xFF).toFloat()
            bBuf[i] = (c and 0xFF).toFloat()
        }

        val effStrength = strength.coerceIn(0f, 1.2f)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val idx = y * w + x
                val a = (pixels[idx] ushr 24) and 0xFF
                if (a < 10) continue

                val oldR = rBuf[idx].roundToInt().coerceIn(0, 255)
                val oldG = gBuf[idx].roundToInt().coerceIn(0, 255)
                val oldB = bBuf[idx].roundToInt().coerceIn(0, 255)

                val nearest = findNearestColor(oldR, oldG, oldB, palette)
                pixels[idx] = (a shl 24) or (nearest and 0x00FFFFFF)

                val nR = (nearest shr 16) and 0xFF
                val nG = (nearest shr 8) and 0xFF
                val nB = nearest and 0xFF

                val errR = (oldR - nR) * effStrength / 8f
                val errG = (oldG - nG) * effStrength / 8f
                val errB = (oldB - nB) * effStrength / 8f

                // Atkinson distribution (6 neighbors, 1/8 each)
                fun distribute(px: Int, py: Int) {
                    if (px in 0 until w && py in 0 until h) {
                        val nIdx = py * w + px
                        rBuf[nIdx] += errR
                        gBuf[nIdx] += errG
                        bBuf[nIdx] += errB
                    }
                }

                distribute(x + 1, y)
                distribute(x + 2, y)
                distribute(x - 1, y + 1)
                distribute(x, y + 1)
                distribute(x + 1, y + 1)
                distribute(x, y + 2)
            }
        }
    }

    /**
     * Floyd-Steinberg Error Diffusion.
     * Standard error distribution: 7/16, 3/16, 5/16, 1/16.
     */
    private fun applyFloydSteinbergDither(
        pixels: IntArray,
        w: Int,
        h: Int,
        palette: IntArray,
        strength: Float
    ) {
        val rBuf = FloatArray(w * h)
        val gBuf = FloatArray(w * h)
        val bBuf = FloatArray(w * h)

        for (i in pixels.indices) {
            val c = pixels[i]
            rBuf[i] = ((c shr 16) and 0xFF).toFloat()
            gBuf[i] = ((c shr 8) and 0xFF).toFloat()
            bBuf[i] = (c and 0xFF).toFloat()
        }

        val effStrength = strength.coerceIn(0f, 1.2f)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val idx = y * w + x
                val a = (pixels[idx] ushr 24) and 0xFF
                if (a < 10) continue

                val oldR = rBuf[idx].roundToInt().coerceIn(0, 255)
                val oldG = gBuf[idx].roundToInt().coerceIn(0, 255)
                val oldB = bBuf[idx].roundToInt().coerceIn(0, 255)

                val nearest = findNearestColor(oldR, oldG, oldB, palette)
                pixels[idx] = (a shl 24) or (nearest and 0x00FFFFFF)

                val nR = (nearest shr 16) and 0xFF
                val nG = (nearest shr 8) and 0xFF
                val nB = nearest and 0xFF

                val errR = (oldR - nR) * effStrength
                val errG = (oldG - nG) * effStrength
                val errB = (oldB - nB) * effStrength

                fun distribute(px: Int, py: Int, weight16: Float) {
                    if (px in 0 until w && py in 0 until h) {
                        val nIdx = py * w + px
                        val factor = weight16 / 16f
                        rBuf[nIdx] += errR * factor
                        gBuf[nIdx] += errG * factor
                        bBuf[nIdx] += errB * factor
                    }
                }

                distribute(x + 1, y, 7f)
                distribute(x - 1, y + 1, 3f)
                distribute(x, y + 1, 5f)
                distribute(x + 1, y + 1, 1f)
            }
        }
    }

    /**
     * Sierra Lite (Fast 2-line error diffusion).
     * Distribution:
     * (x+1, y) += 2/4
     * (x-1, y+1) += 1/4
     * (x, y+1) += 1/4
     */
    private fun applySierraLiteDither(
        pixels: IntArray,
        w: Int,
        h: Int,
        palette: IntArray,
        strength: Float
    ) {
        val rBuf = FloatArray(w * h)
        val gBuf = FloatArray(w * h)
        val bBuf = FloatArray(w * h)

        for (i in pixels.indices) {
            val c = pixels[i]
            rBuf[i] = ((c shr 16) and 0xFF).toFloat()
            gBuf[i] = ((c shr 8) and 0xFF).toFloat()
            bBuf[i] = (c and 0xFF).toFloat()
        }

        val effStrength = strength.coerceIn(0f, 1.2f)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val idx = y * w + x
                val a = (pixels[idx] ushr 24) and 0xFF
                if (a < 10) continue

                val oldR = rBuf[idx].roundToInt().coerceIn(0, 255)
                val oldG = gBuf[idx].roundToInt().coerceIn(0, 255)
                val oldB = bBuf[idx].roundToInt().coerceIn(0, 255)

                val nearest = findNearestColor(oldR, oldG, oldB, palette)
                pixels[idx] = (a shl 24) or (nearest and 0x00FFFFFF)

                val nR = (nearest shr 16) and 0xFF
                val nG = (nearest shr 8) and 0xFF
                val nB = nearest and 0xFF

                val errR = (oldR - nR) * effStrength
                val errG = (oldG - nG) * effStrength
                val errB = (oldB - nB) * effStrength

                fun distribute(px: Int, py: Int, frac: Float) {
                    if (px in 0 until w && py in 0 until h) {
                        val nIdx = py * w + px
                        rBuf[nIdx] += errR * frac
                        gBuf[nIdx] += errG * frac
                        bBuf[nIdx] += errB * frac
                    }
                }

                distribute(x + 1, y, 0.5f)
                distribute(x - 1, y + 1, 0.25f)
                distribute(x, y + 1, 0.25f)
            }
        }
    }

    /**
     * Manga Screentone (Halftone dot matrix).
     * Modulates local luminance against a circular dot raster for print manga aesthetic.
     */
    private fun applyMangaHalftoneDither(
        pixels: IntArray,
        w: Int,
        h: Int,
        palette: IntArray,
        strength: Float
    ) {
        val cellSize = 4
        val maxDist = sqrt((cellSize / 2.0).let { it * it + it * it }).toFloat()
        val spread = 64f * strength.coerceIn(0.1f, 1.5f)

        for (y in 0 until h) {
            val rowOffset = y * w
            val cy = (y % cellSize) - (cellSize / 2f)
            for (x in 0 until w) {
                val idx = rowOffset + x
                val c = pixels[idx]
                val a = (c ushr 24) and 0xFF
                if (a < 10) continue

                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF

                val cx = (x % cellSize) - (cellSize / 2f)
                val dist = sqrt(cx * cx + cy * cy) / maxDist
                val offset = (dist - 0.5f) * spread

                val dr = (r + offset).roundToInt().coerceIn(0, 255)
                val dg = (g + offset).roundToInt().coerceIn(0, 255)
                val db = (b + offset).roundToInt().coerceIn(0, 255)

                val nearest = findNearestColor(dr, dg, db, palette)
                pixels[idx] = (a shl 24) or (nearest and 0x00FFFFFF)
            }
        }
    }

    /**
     * Solid 4-Bit Cel-Shading Posterize without dither.
     */
    private fun applyFlatPosterize(pixels: IntArray, w: Int, h: Int, palette: IntArray) {
        for (i in pixels.indices) {
            val c = pixels[i]
            val a = (c ushr 24) and 0xFF
            if (a < 10) continue
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            val nearest = findNearestColor(r, g, b, palette)
            pixels[i] = (a shl 24) or (nearest and 0x00FFFFFF)
        }
    }

    /**
     * Applies subtle horizontal CRT scanline darkening.
     */
    private fun applyScanlines(pixels: IntArray, w: Int, h: Int, intensity: Float) {
        val factor = 1f - intensity.coerceIn(0.05f, 0.5f)
        for (y in 0 until h) {
            if (y % 2 == 1) {
                val rowOffset = y * w
                for (x in 0 until w) {
                    val idx = rowOffset + x
                    val c = pixels[idx]
                    val a = (c ushr 24) and 0xFF
                    val r = (((c shr 16) and 0xFF) * factor).roundToInt().coerceIn(0, 255)
                    val g = (((c shr 8) and 0xFF) * factor).roundToInt().coerceIn(0, 255)
                    val b = ((c and 0xFF) * factor).roundToInt().coerceIn(0, 255)
                    pixels[idx] = (a shl 24) or (r shl 16) or (g shl 8) or b
                }
            }
        }
    }

    /**
     * Loads a Bitmap safely from local file path or remote HTTP/HTTPS URL with downsampling.
     */
    suspend fun loadBitmapFromPathOrUrl(context: Context, pathOrUrl: String, maxDimension: Int = 1024): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                if (pathOrUrl.isBlank()) return@withContext null

                var inputStream: InputStream? = null
                try {
                    when {
                        pathOrUrl.startsWith("file://") -> {
                            val path = pathOrUrl.removePrefix("file://")
                            val file = File(path)
                            if (file.exists()) inputStream = file.inputStream()
                        }
                        pathOrUrl.startsWith("content://") -> {
                            val uri = Uri.parse(pathOrUrl)
                            inputStream = context.contentResolver.openInputStream(uri)
                        }
                        pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://") -> {
                            val conn = (URL(pathOrUrl).openConnection() as HttpURLConnection).apply {
                                connectTimeout = 8000
                                readTimeout = 8000
                                instanceFollowRedirects = true
                            }
                            inputStream = conn.inputStream
                        }
                        else -> {
                            val file = File(pathOrUrl)
                            if (file.exists()) inputStream = file.inputStream()
                        }
                    }

                    if (inputStream == null) return@withContext null

                    // Decode bounds first
                    val bytes = inputStream.readBytes()
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

                    // Compute sample size
                    var sampleSize = 1
                    val maxSide = maxOf(options.outWidth, options.outHeight)
                    while (maxSide / (sampleSize * 2) >= maxDimension) {
                        sampleSize *= 2
                    }

                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }

                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
                } finally {
                    inputStream?.close()
                }
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Processes an image and saves the dithered result to the app's persistent storage.
     * Returns a "file://..." URI pointing to the saved dithered PNG file.
     */
    suspend fun processAndSaveImage(
        context: Context,
        sourcePathOrUrl: String,
        config: DitherConfig
    ): String? = withContext(Dispatchers.Default) {
        val srcBitmap = loadBitmapFromPathOrUrl(context, sourcePathOrUrl) ?: return@withContext null
        val ditheredBitmap = processBitmap(srcBitmap, config)

        withContext(Dispatchers.IO) {
            try {
                val storageDir = File(context.filesDir, "site_assets")
                if (!storageDir.exists()) storageDir.mkdirs()

                val fileName = "dither_${config.palette.id}_${System.currentTimeMillis()}.png"
                val destFile = File(storageDir, fileName)

                FileOutputStream(destFile).use { out ->
                    ditheredBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                if (ditheredBitmap != srcBitmap) {
                    ditheredBitmap.recycle()
                }
                srcBitmap.recycle()

                "file://${destFile.absolutePath}"
            } catch (e: Exception) {
                null
            }
        }
    }
}
