package com.example

import android.graphics.Bitmap
import android.graphics.Color
import com.example.generator.AnimePalettePreset
import com.example.generator.DitherAlgorithm
import com.example.generator.DitherConfig
import com.example.generator.PixelDitherEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PixelDitherEngineTest {

    @Test
    fun testAnimePalettePresetsValidity() {
        val palettes = AnimePalettePreset.values()
        assertTrue("Should have multiple anime presets", palettes.size >= 8)

        val pc98 = AnimePalettePreset.PC98_ANIME
        assertEquals("nec_pc9801_anime".let { "pc98_anime" }, pc98.id)
        assertEquals(16, pc98.hexColors.size)

        for (preset in palettes) {
            if (preset == AnimePalettePreset.CUSTOM) continue
            assertTrue("Palette ${preset.id} must have colors", preset.hexColors.isNotEmpty())
            for (hex in preset.hexColors) {
                assertTrue("Invalid hex: $hex in ${preset.id}", hex.matches(Regex("^#[0-9a-fA-F]{6}$")))
            }
        }
    }

    @Test
    fun testResolvePaletteColors() {
        val config = DitherConfig(
            palette = AnimePalettePreset.GAMEBOY_DMG
        )
        val colors = PixelDitherEngine.resolvePaletteColors(config)
        assertEquals(4, colors.size)
        // Check first color #0F380F
        val expectedFirst = Color.parseColor("#0F380F")
        assertEquals(expectedFirst, colors[0])
    }

    @Test
    fun testFindNearestColor() {
        val palette = intArrayOf(
            Color.BLACK,
            Color.WHITE,
            Color.RED,
            Color.GREEN,
            Color.BLUE
        )

        // Exact red
        val nearestRed = PixelDitherEngine.findNearestColor(255, 0, 0, palette)
        assertEquals(Color.RED, nearestRed)

        // Near white
        val nearestWhite = PixelDitherEngine.findNearestColor(240, 245, 250, palette)
        assertEquals(Color.WHITE, nearestWhite)

        // Near black
        val nearestBlack = PixelDitherEngine.findNearestColor(10, 15, 12, palette)
        assertEquals(Color.BLACK, nearestBlack)
    }

    @Test
    fun testProcessBitmapWithDifferentAlgorithms() {
        // Create 32x32 test bitmap with gradient
        val testBmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        for (y in 0 until 32) {
            for (x in 0 until 32) {
                testBmp.setPixel(x, y, Color.rgb(x * 8, y * 8, (x + y) * 4))
            }
        }

        // 1. Bayer 4x4 Dither
        val bayerConfig = DitherConfig(
            algorithm = DitherAlgorithm.BAYER_4X4,
            palette = AnimePalettePreset.PC98_ANIME,
            pixelScale = 2
        )
        val bayerResult = PixelDitherEngine.processBitmap(testBmp, bayerConfig)
        assertNotNull(bayerResult)
        assertEquals(32, bayerResult.width)
        assertEquals(32, bayerResult.height)

        // 2. Atkinson Dither
        val atkinsonConfig = DitherConfig(
            algorithm = DitherAlgorithm.ATKINSON,
            palette = AnimePalettePreset.NEO_TOKYO_CYBER,
            pixelScale = 1
        )
        val atkinsonResult = PixelDitherEngine.processBitmap(testBmp, atkinsonConfig)
        assertNotNull(atkinsonResult)
        assertEquals(32, atkinsonResult.width)
        assertEquals(32, atkinsonResult.height)

        // 3. Manga Screentone Halftone
        val mangaConfig = DitherConfig(
            algorithm = DitherAlgorithm.MANGA_HALFTONE,
            palette = AnimePalettePreset.MANGA_SCREENTONE,
            pixelScale = 2
        )
        val mangaResult = PixelDitherEngine.processBitmap(testBmp, mangaConfig)
        assertNotNull(mangaResult)
        assertEquals(32, mangaResult.width)
        assertEquals(32, mangaResult.height)
    }
}
