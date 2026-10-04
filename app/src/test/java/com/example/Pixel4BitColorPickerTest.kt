package com.example

import com.example.ui.components.PixelArtPalettes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Pixel4BitColorPickerTest {

    @Test
    fun testClassicThemesExist() {
        val themes = PixelArtPalettes.CLASSIC_THEMES
        assertTrue("Should have at least 10 classic retro themes", themes.size >= 10)

        val gameboy = themes.find { it.id == "gameboy_dmg" }
        assertNotNull("Gameboy DMG theme must exist", gameboy)
        assertEquals("#9BBC0F", gameboy?.bgHex)
        assertEquals("#0F380F", gameboy?.textHex)

        val nes = themes.find { it.id == "nes_famicom" }
        assertNotNull("NES Famicom theme must exist", nes)

        val pico8 = themes.find { it.id == "pico8_fantasy" }
        assertNotNull("PICO-8 theme must exist", pico8)

        val akiba = themes.find { it.id == "akiba_neon" }
        assertNotNull("Akiba Cyber Neon theme must exist", akiba)
    }

    @Test
    fun testPaletteHexFormatsAreValid() {
        for (theme in PixelArtPalettes.CLASSIC_THEMES) {
            assertTrue("Theme ID should not be blank", theme.id.isNotBlank())
            assertTrue("Theme Name should not be blank", theme.name.isNotBlank())
            assertTrue("Theme Era should not be blank", theme.era.isNotBlank())

            // Validate bgHex
            assertTrue("bgHex format invalid: ${theme.bgHex}", theme.bgHex.matches(Regex("^#[0-9a-fA-F]{6}$")))
            // Validate textHex
            assertTrue("textHex format invalid: ${theme.textHex}", theme.textHex.matches(Regex("^#[0-9a-fA-F]{6}$")))
            // Validate accentHex
            assertTrue("accentHex format invalid: ${theme.accentHex}", theme.accentHex.matches(Regex("^#[0-9a-fA-F]{6}$")))

            // Validate all swatches
            for (swatch in theme.palette) {
                assertTrue("Swatch format invalid in ${theme.id}: $swatch", swatch.matches(Regex("^#[0-9a-fA-F]{6}$")))
            }
        }
    }

    @Test
    fun testFindThemeFallback() {
        val found = PixelArtPalettes.findTheme("gameboy_dmg")
        assertEquals("Game Boy DMG-01", found.name)

        val fallback = PixelArtPalettes.findTheme("non_existent_theme_id")
        assertNotNull("Fallback must provide a valid theme", fallback)
        assertEquals("gameboy_dmg", fallback.id)
    }
}
