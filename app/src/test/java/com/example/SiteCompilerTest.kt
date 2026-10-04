package com.example

import com.example.data.model.BlockType
import com.example.data.model.WebBlockEntity
import com.example.data.model.WebsiteEntity
import com.example.generator.SiteCompiler
import com.example.generator.WebsiteTemplates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipFile

class SiteCompilerTest {

    @Test
    fun testSiteCompilationProducesValidHtmlAndCss() {
        val site = WebsiteEntity(
            id = 1,
            title = "Test Studio",
            slug = "test-studio",
            description = "A responsive test website",
            themePreset = "modern-dark",
            fontFamily = "Inter, sans-serif"
        )

        val blocks = listOf(
            WebBlockEntity(
                websiteId = 1,
                orderIndex = 0,
                type = BlockType.NAVBAR,
                title = "Test Studio",
                content = "Home|Features|Pricing"
            ),
            WebBlockEntity(
                websiteId = 1,
                orderIndex = 1,
                type = BlockType.HERO,
                title = "Build Better Web",
                subtitle = "Fast and responsive",
                content = "Created with mobile builder",
                buttonText = "Start Now",
                buttonUrl = "#pricing"
            ),
            WebBlockEntity(
                websiteId = 1,
                orderIndex = 2,
                type = BlockType.FOOTER,
                title = "Test Studio",
                content = "All rights reserved"
            )
        )

        val compiled = SiteCompiler.compile(site, blocks)

        assertNotNull(compiled)
        assertTrue("HTML should contain DOCTYPE", compiled.html.contains("<!DOCTYPE html>"))
        assertTrue("HTML should contain responsive viewport meta", compiled.html.contains("viewport"))
        assertTrue("HTML should contain site title", compiled.html.contains("Test Studio"))
        assertTrue("HTML should contain hero title", compiled.html.contains("Build Better Web"))
        assertTrue("CSS should contain theme colors", compiled.css.contains("--accent") || compiled.css.contains("--bg-primary"))
        assertTrue("Manifest should have short_name", compiled.manifestJson.contains("Test Studio"))
    }

    @Test
    fun testCompileToZipArchive() {
        val site = WebsiteEntity(
            id = 2,
            title = "Zip Export Test",
            slug = "zip-test"
        )
        val blocks = listOf(
            WebBlockEntity(
                websiteId = 2,
                orderIndex = 0,
                type = BlockType.HERO,
                title = "Zip Hero"
            )
        )
        val compiled = SiteCompiler.compile(site, blocks)

        val tempFile = File.createTempFile("dist_test", ".zip")
        try {
            val zipResult = SiteCompiler.compileToZip(compiled, tempFile)
            assertTrue("Zip file should exist and have size", zipResult.exists() && zipResult.length() > 0)

            ZipFile(tempFile).use { zip ->
                assertNotNull("index.html should be inside zip", zip.getEntry("index.html"))
                assertNotNull("styles.css should be inside zip", zip.getEntry("styles.css"))
                assertNotNull("main.js should be inside zip", zip.getEntry("main.js"))
                assertNotNull("manifest.json should be inside zip", zip.getEntry("manifest.json"))
            }
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun testTemplatesDefinitions() {
        val templates = WebsiteTemplates.allTemplates
        assertTrue("Should have at least 5 templates", templates.size >= 5)

        for (tmpl in templates) {
            val site = tmpl.createWebsite()
            val blocks = tmpl.createBlocks(site.id)
            assertTrue("Template ${tmpl.name} should generate blocks", blocks.isNotEmpty())
            val compiled = SiteCompiler.compile(site, blocks)
            val expectedEscapedTitle = site.title.replace("&", "&amp;")
            assertTrue("Compiled HTML should contain template site title", compiled.html.contains(expectedEscapedTitle))
        }
    }

    @Test
    fun testSmartButtonUrlResolutionAndRedirectAttributes() {
        assertEquals("#contact", SiteCompiler.resolveSmartButtonUrl("", "Get In Touch", BlockType.CONTACT))
        assertEquals("#pricing", SiteCompiler.resolveSmartButtonUrl("", "View Plans", BlockType.HERO))
        assertEquals("#newsletter", SiteCompiler.resolveSmartButtonUrl("", "Join Newsletter", BlockType.NEWSLETTER))
        assertEquals("https://maps.google.com", SiteCompiler.resolveSmartButtonUrl("", "Get Directions", BlockType.CONTACT))

        val site = WebsiteEntity(id = 1, title = "Link Test Site", slug = "link-test")
        val blocks = listOf(
            WebBlockEntity(
                websiteId = 1,
                orderIndex = 0,
                type = BlockType.HERO,
                title = "Hero Section",
                buttonText = "Contact Us",
                buttonUrl = "#contact"
            ),
            WebBlockEntity(
                websiteId = 1,
                orderIndex = 1,
                type = BlockType.CONTACT,
                title = "Contact Section"
            )
        )

        val compiled = SiteCompiler.compile(site, blocks)
        assertTrue("HTML should have anchor target for contact section", compiled.html.contains("id=\"contact\""))
        assertTrue("HTML should have button link with data-redirect", compiled.html.contains("data-redirect=\"#contact\""))
        assertTrue("JS should contain link redirect handler", compiled.js.contains("data-redirect") && compiled.js.contains("AndroidBridge"))
    }

    @Test
    fun testTemplateThumbnailProperties() {
        val templates = WebsiteTemplates.allTemplates
        for (tmpl in templates) {
            assertTrue("Template id should not be blank", tmpl.id.isNotBlank())
            assertTrue("Template name should not be blank", tmpl.name.isNotBlank())
            assertTrue("Template category should not be blank", tmpl.category.isNotBlank())
            assertTrue("Template themePreset should not be blank", tmpl.themePreset.isNotBlank())
            val sampleBlocks = tmpl.createBlocks(0)
            assertTrue("Template ${tmpl.id} should have sample blocks for preview", sampleBlocks.isNotEmpty())
        }
    }

    @Test
    fun testTemplatePreviewKeyFeatures() {
        val templates = WebsiteTemplates.allTemplates
        for (tmpl in templates) {
            val features = com.example.ui.templates.getTemplateKeyFeatures(tmpl)
            assertTrue("Template ${tmpl.id} should have key features defined", features.isNotEmpty())
            assertTrue("Template ${tmpl.id} should have at least 3 features", features.size >= 3)
            for (feature in features) {
                assertTrue("Feature text should not be blank", feature.isNotBlank())
            }
        }
    }

    @Test
    fun testElevatedStylingAndCssPolish() {
        val site = WebsiteEntity(
            id = 1,
            title = "Apex Tech",
            slug = "apex-tech",
            description = "High end SaaS",
            themePreset = "cyberpunk",
            fontFamily = "Plus Jakarta Sans, sans-serif"
        )
        val blocks = listOf(
            WebBlockEntity(
                websiteId = 1,
                orderIndex = 0,
                type = BlockType.HERO,
                title = "Next Gen Cloud",
                subtitle = "Powering tomorrow",
                content = "Deploy globally with one click"
            )
        )
        val compiled = SiteCompiler.compile(site, blocks)
        assertTrue("CSS should have backdrop-filter blur support", compiled.css.contains("backdrop-filter: blur"))
        assertTrue("CSS should have linear-gradient accent on buttons", compiled.css.contains("linear-gradient(135deg, var(--accent)"))
        assertTrue("CSS should have mesh/radial glow background", compiled.css.contains("radial-gradient"))
    }

    @Test
    fun testGlobalThemeCustomizationCustomColorsAndButtonStyles() {
        val site = WebsiteEntity(
            id = 1,
            title = "Custom Palette Site",
            slug = "custom-palette-site",
            description = "Custom colors and styles test",
            themePreset = "modern-dark",
            fontFamily = "Outfit, sans-serif",
            buttonStyle = "brutalist",
            buttonRadius = "sharp",
            customPrimaryColor = "#06B6D4",
            customBackgroundColor = "#030A14"
        )
        val blocks = listOf(
            WebBlockEntity(
                websiteId = 1,
                orderIndex = 0,
                type = BlockType.HERO,
                title = "Brutalist Cyan",
                buttonText = "Explore Now",
                buttonUrl = "#"
            )
        )
        val compiled = SiteCompiler.compile(site, blocks)

        // Verifies custom colors resolved into CSS
        assertTrue("CSS should resolve custom accent color", compiled.css.contains("--accent: #06B6D4;"))
        assertTrue("CSS should resolve custom background tone", compiled.css.contains("--bg-primary: #030A14;"))

        // Verifies button radius variable
        assertTrue("CSS should resolve sharp button radius (0px)", compiled.css.contains("--btn-radius: 0px;"))

        // Verifies brutalist button styling rule
        assertTrue("CSS should apply neo-brutalist border styling", compiled.css.contains("border: 2.5px solid var(--text-primary)"))

        // Verifies font import for Outfit
        assertTrue("HTML should import Google Font for Outfit", compiled.html.contains("family=Outfit"))
    }

    @Test
    fun testButtonRadiusAndGlassEffect() {
        val site = WebsiteEntity(
            id = 1,
            title = "Glassmorphism Studio",
            slug = "glassmorphism-studio",
            description = "Glass button test",
            themePreset = "modern-dark",
            fontFamily = "Space Grotesk, sans-serif",
            buttonStyle = "glass",
            buttonRadius = "rounded"
        )
        val blocks = listOf(
            WebBlockEntity(
                websiteId = 1,
                orderIndex = 0,
                type = BlockType.HERO,
                title = "Glass UI",
                buttonText = "Launch",
                buttonUrl = "#"
            )
        )
        val compiled = SiteCompiler.compile(site, blocks)

        assertTrue("CSS should resolve rounded button radius (12px)", compiled.css.contains("--btn-radius: 12px;"))
        assertTrue("CSS should contain glass button backdrop-filter", compiled.css.contains("backdrop-filter: blur(12px)"))
        assertTrue("HTML should import Google Font for Space Grotesk", compiled.html.contains("family=Space+Grotesk"))
    }
}
