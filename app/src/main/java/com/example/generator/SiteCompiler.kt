package com.example.generator

import com.example.data.model.BlockType
import com.example.data.model.CompiledSite
import com.example.data.model.WebBlockEntity
import com.example.data.model.WebsiteEntity
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SiteCompiler {

    data class PageDef(val slug: String, val title: String)

    /**
     * Picks a structural layout variant for a block.
     *
     * Every block type used to have exactly ONE hardcoded DOM shape, so all
     * 30+ templates produced the same page with a different palette. Variants
     * give each block real structural variety.
     *
     * The seed mixes the site's persisted slug with the block's persisted
     * position. It deliberately avoids the block id, because block ids are
     * regenerated as random UUIDs and seeding on them made the chosen layout
     * change on every recompile. It also avoids seeding on position alone,
     * because templates place sections at fixed indexes (the hero is almost
     * always block 1), which collapsed nearly every site onto one variant.
     * Slug + position is stable across recompiles and still spreads layouts
     * across different sites.
     */
    private fun layoutVariant(siteSeed: String, block: WebBlockEntity, variantCount: Int): Int {
        if (variantCount <= 1) return 0
        var seed = siteSeed.hashCode()
        seed = 31 * seed + block.type.ordinal
        seed = 31 * seed + block.orderIndex
        return ((seed % variantCount) + variantCount) % variantCount
    }

    // Structural layout names applied as a `layout-*` class on the section.
    // CSS in buildCss() targets these to reshape the shared card markup into
    // genuinely different compositions.
    private val featureLayouts = arrayOf("grid3", "bento", "rows")
    private val serviceLayouts = arrayOf("grid3", "wide")
    private val galleryLayouts = arrayOf("grid3", "masonry")
    private val pricingLayouts = arrayOf("cards", "rows")
    private val statsLayouts = arrayOf("cards", "strip")
    private val testimonialLayouts = arrayOf("feature", "grid")
    private val aboutLayouts = arrayOf("split", "reversed")
    private val faqLayouts = arrayOf("stack", "twocol")
    private val ctaLayouts = arrayOf("banner", "card")
    private val contactLayouts = arrayOf("formleft", "formright")

    // Entrance effects the stylesheet knows how to render. A site keeps its
    // chosen family, but when the site is still on the default style the
    // sections rotate through these so pages do not all animate identically.
    private val animEffects = arrayOf("fade-up", "slide-left", "soft-pop", "zoom-in", "fade-in", "flip-up")

    private fun resolveAnimEffect(block: WebBlockEntity, website: WebsiteEntity?): String {
        val perBlock = block.animationEffect.takeIf { it.isNotBlank() && it != "default" }
        if (perBlock != null) return perBlock
        val siteStyle = website?.animationStyle?.takeIf { it.isNotBlank() } ?: "fade-up"
        if (siteStyle != "fade-up") return siteStyle
        return animEffects[layoutVariant(website?.slug.orEmpty(), block, animEffects.size)]
    }

    fun parsePages(pagesJson: String, defaultTitle: String = "Home"): List<PageDef> {
        if (pagesJson.isBlank()) return listOf(PageDef("index", defaultTitle.ifBlank { "Home" }))
        return try {
            val jsonArray = org.json.JSONArray(pagesJson)
            val list = mutableListOf<PageDef>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val slug = obj.optString("slug", "").trim().lowercase().replace(" ", "-")
                val title = obj.optString("title", "").trim()
                if (slug.isNotBlank() && title.isNotBlank()) {
                    list.add(PageDef(slug, title))
                }
            }
            if (list.none { it.slug == "index" }) {
                list.add(0, PageDef("index", defaultTitle.ifBlank { "Home" }))
            }
            list
        } catch (e: Exception) {
            listOf(PageDef("index", defaultTitle.ifBlank { "Home" }))
        }
    }

    fun compile(website: WebsiteEntity, blocks: List<WebBlockEntity>): CompiledSite {
        val themeVars = resolveThemeVariables(
            preset = website.themePreset,
            customPrimaryHex = website.customPrimaryColor,
            customBackgroundHex = website.customBackgroundColor
        )
        val fontImport = getFontImport(website.fontFamily)
        val fontCssFamily = website.fontFamily

        val pages = parsePages(website.pagesJson, website.title)
        val isMultiPage = pages.size > 1

        val indexBlocks = if (isMultiPage) {
            val specific = blocks.filter { it.pageSlug == "index" || it.pageSlug.isBlank() }
            if (specific.isNotEmpty()) specific else blocks
        } else {
            blocks
        }

        val htmlContent = buildHtml(
            website = website,
            blocks = indexBlocks,
            fontImport = fontImport,
            fontFamily = fontCssFamily,
            pageTitle = website.title,
            currentPageSlug = "index",
            allPages = pages
        )
        val cssContent = buildCss(website, themeVars, fontCssFamily)
        val jsContent = buildJs(website)
        val manifestContent = buildManifest(website, themeVars)
        val readmeContent = buildReadme(website)

        val additionalPagesMap = mutableMapOf<String, String>()
        if (isMultiPage) {
            for (page in pages) {
                if (page.slug == "index") continue
                val pageBlocks = blocks.filter { it.pageSlug == page.slug }
                val fullPageBlocks = buildList {
                    val nav = blocks.firstOrNull { it.type == BlockType.NAVBAR }
                    if (nav != null && pageBlocks.none { it.type == BlockType.NAVBAR }) add(nav)
                    addAll(pageBlocks)
                    val footer = blocks.firstOrNull { it.type == BlockType.FOOTER }
                    if (footer != null && pageBlocks.none { it.type == BlockType.FOOTER }) add(footer)
                }
                val pageHtml = buildHtml(
                    website = website,
                    blocks = fullPageBlocks,
                    fontImport = fontImport,
                    fontFamily = fontCssFamily,
                    pageTitle = "${page.title} - ${website.title}",
                    currentPageSlug = page.slug,
                    allPages = pages
                )
                additionalPagesMap["${page.slug}.html"] = pageHtml
            }
        }

        return CompiledSite(
            html = htmlContent,
            css = cssContent,
            js = jsContent,
            manifestJson = manifestContent,
            readme = readmeContent,
            additionalPages = additionalPagesMap
        )
    }

    fun compileToZip(site: CompiledSite, outputFile: File): File {
        if (outputFile.exists()) {
            outputFile.delete()
        }
        outputFile.parentFile?.mkdirs()

        ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
            addZipEntry(zos, "index.html", site.html)
            addZipEntry(zos, "styles.css", site.css)
            addZipEntry(zos, "main.js", site.js)
            addZipEntry(zos, "manifest.json", site.manifestJson)
            addZipEntry(zos, "README.md", site.readme)
            for ((pageName, pageContent) in site.additionalPages) {
                addZipEntry(zos, pageName, pageContent)
            }
        }
        return outputFile
    }

    fun exportToDistDirectory(site: CompiledSite, distDir: File): List<File> {
        if (!distDir.exists()) {
            distDir.mkdirs()
        }
        val files = mutableListOf<File>()

        fun write(name: String, content: String) {
            val f = File(distDir, name)
            f.writeText(content, Charsets.UTF_8)
            files.add(f)
        }

        write("index.html", site.html)
        write("styles.css", site.css)
        write("main.js", site.js)
        write("manifest.json", site.manifestJson)
        write("README.md", site.readme)
        for ((pageName, pageContent) in site.additionalPages) {
            write(pageName, pageContent)
        }

        return files
    }

    private fun addZipEntry(zos: ZipOutputStream, fileName: String, content: String) {
        val bytes = content.toByteArray(Charsets.UTF_8)
        val entry = ZipEntry(fileName)
        zos.putNextEntry(entry)
        zos.write(bytes, 0, bytes.size)
        zos.closeEntry()
    }

    private fun buildHtml(
        website: WebsiteEntity,
        blocks: List<WebBlockEntity>,
        fontImport: String,
        fontFamily: String,
        pageTitle: String = website.title,
        currentPageSlug: String = "index",
        allPages: List<PageDef> = emptyList()
    ): String {
        val visibleBlocks = blocks.filter { it.isVisible }
        val sb = StringBuilder()

        sb.appendLine("<!DOCTYPE html>")
        sb.appendLine("<html lang=\"en\">")
        sb.appendLine("<head>")
        sb.appendLine("  <meta charset=\"UTF-8\">")
        sb.appendLine("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
        sb.appendLine("  <title>${escapeHtml(pageTitle)}</title>")
        sb.appendLine("  <meta name=\"description\" content=\"${escapeHtml(website.description)}\">")
        sb.appendLine("  <meta name=\"generator\" content=\"Open-Source Mobile Web Builder\">")
        sb.appendLine("  <meta property=\"og:type\" content=\"website\">")
        sb.appendLine("  <meta property=\"og:title\" content=\"${escapeHtml(pageTitle)}\">")
        sb.appendLine("  <meta property=\"og:description\" content=\"${escapeHtml(website.description)}\">")
        if (website.ogImageUrl.isNotBlank()) {
            sb.appendLine("  <meta property=\"og:image\" content=\"${escapeHtml(website.ogImageUrl)}\">")
        }
        sb.appendLine("  <meta name=\"twitter:card\" content=\"summary_large_image\">")
        sb.appendLine("  <meta name=\"twitter:title\" content=\"${escapeHtml(pageTitle)}\">")
        sb.appendLine("  <meta name=\"twitter:description\" content=\"${escapeHtml(website.description)}\">")
        if (website.ogImageUrl.isNotBlank()) {
            sb.appendLine("  <meta name=\"twitter:image\" content=\"${escapeHtml(website.ogImageUrl)}\">")
        }
        sb.appendLine("  <link rel=\"manifest\" href=\"manifest.json\">")
        sb.appendLine("  <link rel=\"stylesheet\" href=\"styles.css\">")
        if (fontImport.isNotBlank()) {
            sb.appendLine("  $fontImport")
        }
        sb.appendLine("</head>")
        sb.appendLine("<body>")

        // Render sections
        for (block in visibleBlocks) {
            sb.appendLine(renderBlockHtml(block, website, allPages, currentPageSlug))
        }

        // Floating Back-to-Top Button
        sb.appendLine("  <button id=\"backToTop\" class=\"back-to-top\" aria-label=\"Back to top\" title=\"Back to top\">↑</button>")

        val isPixel = website.themePreset in listOf("anime-4bit", "retro-arcade-4bit", "gameboy-4bit") ||
            fontFamily.contains("Press Start", ignoreCase = true) ||
            fontFamily.contains("DotGothic", ignoreCase = true) ||
            fontFamily.contains("Silkscreen", ignoreCase = true) ||
            website.buttonStyle in listOf("pixel-4bit", "arcade-pixel", "retro-4bit")
        if (isPixel) {
            sb.appendLine("  <div class=\"crt-overlay\" aria-hidden=\"true\"></div>")
        }

        if (website.enableVisitorThemeToggle) {
            sb.appendLine("  <button id=\"themeToggle\" class=\"visitor-theme-toggle\" aria-label=\"Toggle Light/Dark Theme\" title=\"Toggle Theme\"><span class=\"theme-icon\">🌓</span></button>")
        }

        sb.appendLine("  <script src=\"main.js\"></script>")
        sb.appendLine("</body>")
        sb.appendLine("</html>")

        return sb.toString()
    }

    fun resolveSmartButtonUrl(rawUrl: String, buttonText: String, blockType: BlockType): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isNotBlank() && trimmed != "#") {
            if (trimmed.all { it.isDigit() || it == '+' } && trimmed.length >= 8) {
                return "https://wa.me/${trimmed.filter { it.isDigit() }}"
            }
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("mailto:") || trimmed.startsWith("tel:") || trimmed.startsWith("#")) {
                return trimmed
            }
            return "#$trimmed"
        }
        val lower = buttonText.lowercase()
        return when {
            lower.contains("whatsapp") -> "#contact"
            lower.contains("wizard") || lower.contains("estimator") || lower.contains("estimate") || lower.contains("booking") || lower.contains("quote") || blockType == BlockType.MULTISTEP_WIZARD -> "#wizard"
            lower.contains("contact") || lower.contains("message") || lower.contains("talk") || lower.contains("reach") || lower.contains("inquir") -> "#contact"
            lower.contains("subscri") || lower.contains("news") || lower.contains("digest") || lower.contains("journal") || blockType == BlockType.NEWSLETTER -> "#newsletter"
            lower.contains("direction") || lower.contains("map") || lower.contains("location") -> "https://maps.google.com"
            lower.contains("shop") || lower.contains("bag") || lower.contains("cart") || blockType == BlockType.WHATSAPP_SHOP -> "#shop"
            lower.contains("process") || lower.contains("roadmap") -> "#timeline"
            lower.contains("work") || lower.contains("project") || lower.contains("portfolio") -> "#gallery"
            lower.contains("pric") || lower.contains("plan") || lower.contains("buy") || lower.contains("tier") || lower.contains("start") -> "#pricing"
            lower.contains("feature") || lower.contains("explore") || lower.contains("discover") || lower.contains("more") -> "#features"
            lower.contains("about") || lower.contains("story") || lower.contains("team") || lower.contains("philosophy") -> "#about"
            lower.contains("service") || lower.contains("offer") -> "#services"
            lower.contains("faq") || lower.contains("question") || lower.contains("help") -> "#faq"
            lower.contains("call") || lower.contains("phone") -> "tel:+18005550199"
            lower.contains("mail") || lower.contains("email") -> "mailto:contact@mysite.com"
            lower.contains("order") || lower.contains("menu") -> "#pricing"
            blockType == BlockType.HERO -> "#pricing"
            blockType == BlockType.CTA -> "#contact"
            blockType == BlockType.PRICING -> "#contact"
            blockType == BlockType.ABOUT -> "#services"
            blockType == BlockType.NAVBAR -> "#contact"
            blockType == BlockType.SERVICES -> "#contact"
            blockType == BlockType.FEATURES -> "#pricing"
            blockType == BlockType.NEWSLETTER -> "#newsletter"
            blockType == BlockType.STATS -> "#pricing"
            blockType == BlockType.TIMELINE -> "#pricing"
            blockType == BlockType.TEAM -> "#contact"
            blockType == BlockType.LOGOS -> "#features"
            else -> "#contact"
        }
    }

    private fun renderButtonLink(
        url: String,
        text: String,
        cssClass: String,
        defaultFallback: String = "#contact"
    ): String {
        val resolvedUrl = if (url.isNotBlank() && url != "#") url else defaultFallback
        val isExternal = resolvedUrl.startsWith("http://") || resolvedUrl.startsWith("https://")
        val targetAttr = if (isExternal) " target=\"_blank\" rel=\"noopener noreferrer\"" else ""
        return "<a href=\"${escapeHtml(resolvedUrl)}\" class=\"$cssClass\"$targetAttr data-redirect=\"${escapeHtml(resolvedUrl)}\">${escapeHtml(text)}</a>"
    }

    private fun renderBlockHtml(
        block: WebBlockEntity,
        website: WebsiteEntity? = null,
        allPages: List<PageDef> = emptyList(),
        currentPageSlug: String = "index"
    ): String {
        val customStyle = buildString {
            if (block.backgroundColorHex.isNotBlank()) append("background-color: ${block.backgroundColorHex}; ")
            if (block.textColorHex.isNotBlank()) append("color: ${block.textColorHex}; ")
            if (block.alignment.isNotBlank()) append("text-align: ${block.alignment}; ")
        }
        val styleAttr = if (customStyle.isNotBlank()) " style=\"$customStyle\"" else ""
        val siteSeed = website?.slug?.takeIf { it.isNotBlank() } ?: website?.title.orEmpty()
        val aliases = when (block.type) {
            BlockType.WHATSAPP_SHOP -> "shop bags store catalog order custom-order cart"
            BlockType.MULTISTEP_WIZARD -> "wizard booking quote custom-order estimator application calculator builder"
            BlockType.PRICING -> "menu shop shop-all plans packages pricing tiers collection"
            BlockType.ABOUT -> "story roastery philosophy practice about mission heritage publication"
            BlockType.TIMELINE -> "process roadmap projects framework steps journey execution"
            BlockType.GALLERY -> "work works portfolio showcase projects photos artifacts"
            BlockType.SERVICES -> "work works offerings solutions disciplines services capabilities"
            BlockType.FEATURES -> "stack skills articles topics roastery craft features competencies"
            BlockType.STATS -> "metrics impact numbers benchmarks results sustainability"
            BlockType.CONTACT -> "location visit inquire talk reach message contact directions"
            BlockType.NEWSLETTER -> "subscribe journal digest newsletter"
            BlockType.TESTIMONIALS -> "reviews community praise social-proof testimonials"
            BlockType.TEAM -> "leadership team founders people"
            BlockType.LOGOS -> "partners clients trusted logos"
            BlockType.FAQ -> "help questions faq"
            BlockType.CTA -> "action join start cta"
            BlockType.COUNTDOWN_TIMER -> "timer countdown launch event drop release"
            BlockType.IMAGE_CAROUSEL -> "carousel slider gallery photos showcase"
            else -> ""
        }
        val animEffect = resolveAnimEffect(block, website)
        val animAttr = if (animEffect != "none" && block.type != BlockType.NAVBAR && block.type != BlockType.FOOTER) " data-animate=\"$animEffect\"" else ""
        val blockIdAttr = " id=\"${block.type.name.lowercase()}\" data-aliases=\"$aliases\" data-block-id=\"${block.id}\" data-block-type=\"${block.type.name}\"$animAttr"

        return when (block.type) {
            BlockType.NAVBAR -> {
                val links = block.content.split("|").filter { it.isNotBlank() }
                buildString {
                    appendLine("  <header class=\"navbar\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container nav-container\">")
                    appendLine("      <a href=\"${if (currentPageSlug == "index") "#" else "index.html"}\" class=\"nav-brand\">")
                    appendLine("        <span class=\"brand-logo-mark\">✦</span>")
                    appendLine("        <span>${escapeHtml(block.title)}</span>")
                    appendLine("      </a>")
                    appendLine("      <nav class=\"nav-links\" id=\"navLinks\">")
                    for (link in links) {
                        val trimmed = link.trim()
                        val anchor = trimmed.lowercase().replace(" ", "-")
                        val matchingPage = allPages.firstOrNull { it.title.equals(trimmed, ignoreCase = true) || it.slug.equals(anchor, ignoreCase = true) }
                        val href = when {
                            matchingPage != null && matchingPage.slug == "index" -> if (currentPageSlug == "index") "#" else "index.html"
                            matchingPage != null -> if (currentPageSlug == matchingPage.slug) "#" else "${matchingPage.slug}.html"
                            allPages.size > 1 && (trimmed.equals("home", ignoreCase = true)) -> if (currentPageSlug == "index") "#" else "index.html"
                            allPages.size > 1 && currentPageSlug != "index" -> "index.html#$anchor"
                            else -> "#$anchor"
                        }
                        val activeClass = if ((matchingPage?.slug == currentPageSlug) || (currentPageSlug == "index" && (trimmed.equals("home", ignoreCase = true) || matchingPage?.slug == "index"))) " active" else ""
                        appendLine("        <a href=\"$href\" class=\"nav-link$activeClass\" data-redirect=\"$href\">${escapeHtml(trimmed)}</a>")
                    }
                    if (block.buttonText.isNotBlank()) {
                        val btnUrl = resolveSmartButtonUrl(block.buttonUrl, block.buttonText, block.type)
                        appendLine("        ${renderButtonLink(btnUrl, block.buttonText, "btn btn-primary btn-sm", "#contact")}")
                    }
                    appendLine("      </nav>")
                    appendLine("      <button class=\"nav-toggle\" id=\"navToggle\" aria-label=\"Toggle Menu\">")
                    appendLine("        <span></span><span></span><span></span>")
                    appendLine("      </button>")
                    appendLine("    </div>")
                    appendLine("  </header>")
                }
            }

            BlockType.HERO -> {
                val heroVariant = layoutVariant(siteSeed, block, 3)
                buildString {
                    appendLine("  <section class=\"hero-section hero--v$heroVariant\"$blockIdAttr$styleAttr>")
                    if (heroVariant == 2 && block.imageUrl.isNotBlank()) {
                        appendLine("    <div class=\"hero-bg\" style=\"background-image:url('${escapeHtml(block.imageUrl)}')\"></div>")
                    }
                    appendLine("    <div class=\"container hero-container\">")
                    if (block.subtitle.isNotBlank()) {
                        appendLine("      <div class=\"hero-badge\"><span class=\"badge-dot\"></span>${escapeHtml(block.subtitle)}</div>")
                    }
                    appendLine("      <h1 class=\"hero-title\">${escapeHtml(block.title)}</h1>")
                    if (block.content.isNotBlank()) {
                        appendLine("      <p class=\"hero-desc\">${escapeHtml(block.content)}</p>")
                    }
                    if (block.buttonText.isNotBlank() || block.secondaryButtonText.isNotBlank()) {
                        appendLine("      <div class=\"hero-actions\">")
                        if (block.buttonText.isNotBlank()) {
                            val primaryUrl = resolveSmartButtonUrl(block.buttonUrl, block.buttonText, block.type)
                            appendLine("        ${renderButtonLink(primaryUrl, block.buttonText, "btn btn-primary btn-lg", "#pricing")}")
                        }
                        if (block.secondaryButtonText.isNotBlank()) {
                            val secUrl = if (block.secondaryButtonUrl.isNotBlank() && block.secondaryButtonUrl != "#") block.secondaryButtonUrl else "#features"
                            appendLine("        ${renderButtonLink(secUrl, block.secondaryButtonText, "btn btn-secondary btn-lg", "#features")}")
                        }
                        appendLine("      </div>")
                    }
                    if (block.imageUrl.isNotBlank()) {
                        appendLine("      <div class=\"hero-media-wrap\">")
                        appendLine("        <img src=\"${escapeHtml(block.imageUrl)}\" alt=\"${escapeHtml(block.title)}\" class=\"hero-image\" loading=\"lazy\">")
                        appendLine("      </div>")
                    }
                    val heroContext = (block.title + " " + block.subtitle + " " + block.content).lowercase()
                    val proofPoints = when {
                        heroContext.contains("coffee") || heroContext.contains("roast") || heroContext.contains("bakery") || heroContext.contains("cafe") -> listOf(
                            "100%" to "Organic Single-Origin",
                            "5 AM" to "Fresh Daily Hearth Bake",
                            "4.9 ★" to "Community Favorite"
                        )
                        heroContext.contains("design") || heroContext.contains("art") || heroContext.contains("director") || heroContext.contains("creative") -> listOf(
                            "10+ Yrs" to "Design Leadership",
                            "40+" to "Delivered Identities",
                            "12" to "International Awards"
                        )
                        heroContext.contains("systems") || heroContext.contains("engineer") || heroContext.contains("rust") || heroContext.contains("distributed") -> listOf(
                            "sub-5ms" to "p99 Tail Latency",
                            "250k+" to "Events / Second",
                            "Zero" to "Downtime Deploys"
                        )
                        heroContext.contains("shop") || heroContext.contains("bag") || heroContext.contains("merino") || heroContext.contains("handcraft") || heroContext.contains("living") || heroContext.contains("ceramic") -> listOf(
                            "100%" to "Ethically Sourced",
                            "24-48h" to "Express Delivery",
                            "4.9 ★" to "Verified Reviews"
                        )
                        heroContext.contains("essay") || heroContext.contains("chronicle") || heroContext.contains("journal") || heroContext.contains("writer") -> listOf(
                            "20k+" to "Weekly Readers",
                            "100%" to "Independent Editorial",
                            "0" to "Ads or Trackers"
                        )
                        heroContext.contains("creator") || heroContext.contains("podcast") || heroContext.contains("youtube") -> listOf(
                            "85k+" to "Tech Community",
                            "40+" to "Open Source Tools",
                            "Weekly" to "Curated Issues"
                        )
                        else -> listOf(
                            "Trusted" to "By Our Customers",
                            "Quality" to "Built To Last",
                            "Service" to "Always Here To Help"
                        )
                    }
                    appendLine("      <div class=\"hero-proof-bar\">")
                    for ((i, point) in proofPoints.withIndex()) {
                        if (i > 0) appendLine("        <div class=\"proof-sep\">•</div>")
                        appendLine("        <div class=\"proof-item\"><span class=\"proof-number\">${point.first}</span><span class=\"proof-label\">${point.second}</span></div>")
                    }
                    appendLine("      </div>")
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.FEATURES -> {
                val items = block.content.split("|").filter { it.isNotBlank() }
                buildString {
                    appendLine("  <section class=\"features-section layout-${featureLayouts[layoutVariant(siteSeed, block, 3)]}\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container\">")
                    if (block.title.isNotBlank()) appendLine("      <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
                    if (block.subtitle.isNotBlank()) appendLine("      <p class=\"section-subtitle\">${escapeHtml(block.subtitle)}</p>")
                    appendLine("      <div class=\"grid-3\">")
                    val defaultIcons = listOf("⚡", "🛡️", "📊", "🌐", "🚀", "💡", "🎯", "💎")
                    for ((idx, item) in items.withIndex()) {
                        val parts = item.split(":", limit = 2)
                        var featTitle = parts.firstOrNull()?.trim() ?: "Feature"
                        val featDesc = if (parts.size > 1) parts[1].trim() else ""
                        var icon = defaultIcons[idx % defaultIcons.size]
                        if (featTitle.contains(" ")) {
                            val firstToken = featTitle.split(" ").first()
                            if (firstToken.length <= 2) {
                                icon = firstToken
                                featTitle = featTitle.substringAfter(" ").trim()
                            }
                        }
                        appendLine("        <div class=\"feature-card\">")
                        appendLine("          <div class=\"icon-badge\">$icon</div>")
                        appendLine("          <h3 class=\"card-title\">${escapeHtml(featTitle)}</h3>")
                        if (featDesc.isNotBlank()) appendLine("          <p class=\"card-desc\">${escapeHtml(featDesc)}</p>")
                        appendLine("        </div>")
                    }
                    appendLine("      </div>")
                    if (block.buttonText.isNotBlank()) {
                        val url = resolveSmartButtonUrl(block.buttonUrl, block.buttonText, block.type)
                        appendLine("      <div class=\"section-action\">${renderButtonLink(url, block.buttonText, "btn btn-primary", "#pricing")}</div>")
                    }
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.ABOUT -> {
                buildString {
                    appendLine("  <section class=\"about-section layout-${aboutLayouts[layoutVariant(siteSeed, block, 2)]}\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container about-container\">")
                    if (block.imageUrl.isNotBlank()) {
                        appendLine("      <div class=\"about-split-layout\">")
                        appendLine("        <div class=\"about-text-content\">")
                        if (block.title.isNotBlank()) appendLine("          <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
                        if (block.subtitle.isNotBlank()) appendLine("          <p class=\"section-subtitle\">${escapeHtml(block.subtitle)}</p>")
                        if (block.content.isNotBlank()) appendLine("          <p class=\"about-text\">${escapeHtml(block.content)}</p>")
                        if (block.buttonText.isNotBlank()) {
                            val url = resolveSmartButtonUrl(block.buttonUrl, block.buttonText, block.type)
                            appendLine("          ${renderButtonLink(url, block.buttonText, "btn btn-primary", "#contact")}")
                        }
                        appendLine("        </div>")
                        appendLine("        <div class=\"about-image-wrapper\">")
                        appendLine("          <img src=\"${escapeHtml(block.imageUrl)}\" alt=\"${escapeHtml(block.title)}\" class=\"about-image\" loading=\"lazy\">")
                        appendLine("        </div>")
                        appendLine("      </div>")
                    } else {
                        if (block.title.isNotBlank()) appendLine("      <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
                        if (block.subtitle.isNotBlank()) appendLine("      <p class=\"section-subtitle\">${escapeHtml(block.subtitle)}</p>")
                        if (block.content.isNotBlank()) appendLine("      <p class=\"about-text\">${escapeHtml(block.content)}</p>")
                        if (block.buttonText.isNotBlank()) {
                            val url = resolveSmartButtonUrl(block.buttonUrl, block.buttonText, block.type)
                            appendLine("      ${renderButtonLink(url, block.buttonText, "btn btn-primary", "#contact")}")
                        }
                    }
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.SERVICES -> {
                val items = block.content.split("|").filter { it.isNotBlank() }
                buildString {
                    appendLine("  <section class=\"services-section layout-${serviceLayouts[layoutVariant(siteSeed, block, 2)]}\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container\">")
                    if (block.title.isNotBlank()) appendLine("      <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
                    if (block.subtitle.isNotBlank()) appendLine("      <p class=\"section-subtitle\">${escapeHtml(block.subtitle)}</p>")
                    appendLine("      <div class=\"grid-3\">")
                    for (item in items) {
                        val parts = item.split(":", limit = 2)
                        val sTitle = parts.firstOrNull()?.trim() ?: "Service"
                        val sDesc = if (parts.size > 1) parts[1].trim() else ""
                        appendLine("        <div class=\"service-card\">")
                        appendLine("          <h3 class=\"card-title\">${escapeHtml(sTitle)}</h3>")
                        if (sDesc.isNotBlank()) appendLine("          <p class=\"card-desc\">${escapeHtml(sDesc)}</p>")
                        appendLine("          <a href=\"#contact\" class=\"service-link\" data-redirect=\"#contact\">Learn More ➔</a>")
                        appendLine("        </div>")
                    }
                    appendLine("      </div>")
                    if (block.buttonText.isNotBlank()) {
                        val url = resolveSmartButtonUrl(block.buttonUrl, block.buttonText, block.type)
                        appendLine("      <div class=\"section-action\">${renderButtonLink(url, block.buttonText, "btn btn-primary", "#contact")}</div>")
                    }
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.TESTIMONIALS -> {
                buildString {
                    appendLine("  <section class=\"testimonial-section layout-${testimonialLayouts[layoutVariant(siteSeed, block, 2)]}\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container\">")
                    if (block.title.isNotBlank()) appendLine("      <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
                    appendLine("      <div class=\"testimonial-card\">")
                    appendLine("        <div class=\"stars\">★★★★★</div>")
                    if (block.content.isNotBlank()) appendLine("        <blockquote class=\"quote\">${escapeHtml(block.content)}</blockquote>")
                    if (block.subtitle.isNotBlank()) {
                        appendLine("        <div class=\"testimonial-author-box\">")
                        appendLine("          <div class=\"author-avatar-badge\">✓</div>")
                        appendLine("          <div>")
                        appendLine("            <cite class=\"author\">${escapeHtml(block.subtitle)}</cite>")
                        appendLine("            <div class=\"verified-badge\">Verified Customer Review</div>")
                        appendLine("          </div>")
                        appendLine("        </div>")
                    }
                    appendLine("      </div>")
                    if (block.buttonText.isNotBlank()) {
                        val url = resolveSmartButtonUrl(block.buttonUrl, block.buttonText, block.type)
                        appendLine("      <div class=\"section-action\">${renderButtonLink(url, block.buttonText, "btn btn-secondary", "#contact")}</div>")
                    }
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.PRICING -> {
                val tiers = block.content.split("|").filter { it.isNotBlank() }
                buildString {
                    appendLine("  <section class=\"pricing-section layout-${pricingLayouts[layoutVariant(siteSeed, block, 2)]}\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container\">")
                    if (block.title.isNotBlank()) appendLine("      <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
                    if (block.subtitle.isNotBlank()) appendLine("      <p class=\"section-subtitle\">${escapeHtml(block.subtitle)}</p>")

                    // Interactive Monthly / Yearly billing switcher
                    appendLine("      <div class=\"billing-toggle-container\">")
                    appendLine("        <span class=\"billing-opt active\" id=\"optMonthly\">Monthly</span>")
                    appendLine("        <label class=\"billing-switch\">")
                    appendLine("          <input type=\"checkbox\" id=\"billingToggle\">")
                    appendLine("          <span class=\"billing-slider\"></span>")
                    appendLine("        </label>")
                    appendLine("        <span class=\"billing-opt\" id=\"optAnnual\">Yearly <span class=\"save-badge\">20% OFF</span></span>")
                    appendLine("      </div>")

                    appendLine("      <div class=\"grid-3\">")
                    for ((idx, tier) in tiers.withIndex()) {
                        val parts = tier.split(":", limit = 2)
                        val header = parts.firstOrNull()?.trim() ?: "Plan"
                        val bullets = if (parts.size > 1) parts[1].split(",") else emptyList()
                        val isPopular = idx == 1 || header.lowercase().contains("pro")

                        // Extract monthly price
                        val priceMatch = Regex("""\(\s*(\$?\d+)(?:/mo)?\s*\)""").find(header)
                        val monthlyPrice = priceMatch?.groupValues?.get(1) ?: when (idx) {
                            0 -> "$0"
                            1 -> "$49"
                            else -> "$199"
                        }
                        val cleanHeader = header.replace(Regex("""\s*\([^)]*\)"""), "").trim()
                        val numOnly = monthlyPrice.replace("$", "").toIntOrNull() ?: 49
                        val annualPrice = "$${(numOnly * 0.8).toInt()}"

                        appendLine("        <div class=\"pricing-card ${if (isPopular) "popular" else ""}\">")
                        if (isPopular) appendLine("          <span class=\"badge-popular\">★ MOST POPULAR</span>")
                        appendLine("          <h3 class=\"card-title\">${escapeHtml(cleanHeader)}</h3>")
                        appendLine("          <div class=\"price-box\">")
                        appendLine("            <span class=\"price-amount\" data-monthly=\"$monthlyPrice\" data-annual=\"$annualPrice\">$monthlyPrice</span>")
                        appendLine("            <span class=\"price-period\">/mo</span>")
                        appendLine("          </div>")
                        appendLine("          <ul class=\"pricing-features\">")
                        for (bullet in bullets) {
                            appendLine("            <li><span class=\"check-icon\">✓</span> ${escapeHtml(bullet.trim())}</li>")
                        }
                        appendLine("          </ul>")
                        val btnLabel = if (block.buttonText.isNotBlank()) block.buttonText else "Choose Plan"
                        val btnHref = resolveSmartButtonUrl(block.buttonUrl, btnLabel, block.type)
                        appendLine("          ${renderButtonLink(btnHref, btnLabel, "btn ${if (isPopular) "btn-primary" else "btn-secondary"} btn-block", "#contact")}")
                        appendLine("        </div>")
                    }
                    appendLine("      </div>")
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.GALLERY -> {
                val images = block.content.split("|").filter { it.isNotBlank() }
                buildString {
                    appendLine("  <section class=\"gallery-section layout-${galleryLayouts[layoutVariant(siteSeed, block, 2)]}\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container\">")
                    if (block.title.isNotBlank()) appendLine("      <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
                    if (block.subtitle.isNotBlank()) appendLine("      <p class=\"section-subtitle\">${escapeHtml(block.subtitle)}</p>")
                    appendLine("      <div class=\"grid-3\">")
                    for (img in images) {
                        appendLine("        <div class=\"gallery-item\">")
                        appendLine("          <img src=\"${escapeHtml(img.trim())}\" alt=\"Gallery showcase\" loading=\"lazy\">")
                        appendLine("        </div>")
                    }
                    appendLine("      </div>")
                    if (block.buttonText.isNotBlank()) {
                        val url = resolveSmartButtonUrl(block.buttonUrl, block.buttonText, block.type)
                        appendLine("      <div class=\"section-action\">${renderButtonLink(url, block.buttonText, "btn btn-primary", "#contact")}</div>")
                    }
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.CTA -> {
                buildString {
                    appendLine("  <section class=\"cta-section layout-${ctaLayouts[layoutVariant(siteSeed, block, 2)]}\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container cta-container\">")
                    appendLine("      <h2 class=\"cta-title\">${escapeHtml(block.title)}</h2>")
                    if (block.subtitle.isNotBlank()) appendLine("      <p class=\"cta-desc\">${escapeHtml(block.subtitle)}</p>")
                    if (block.buttonText.isNotBlank()) {
                        val href = resolveSmartButtonUrl(block.buttonUrl, block.buttonText, block.type)
                        appendLine("      ${renderButtonLink(href, block.buttonText, "btn btn-primary btn-lg", "#contact")}")
                    }
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.CONTACT -> {
                buildString {
                    appendLine("  <section class=\"contact-section layout-${contactLayouts[layoutVariant(siteSeed, block, 2)]}\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container contact-container\">")
                    if (block.title.isNotBlank()) appendLine("      <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
                    if (block.subtitle.isNotBlank()) appendLine("      <p class=\"section-subtitle\">${escapeHtml(block.subtitle)}</p>")

                    appendLine("      <div class=\"contact-grid\">")
                    appendLine("        <div class=\"contact-info-panel\">")
                    if (block.content.isNotBlank()) {
                        appendLine("          <p class=\"contact-desc\">${escapeHtml(block.content)}</p>")
                    }
                    val emailCandidate = when {
                        block.buttonUrl.startsWith("mailto:") -> block.buttonUrl.removePrefix("mailto:").trim()
                        block.content.contains("@") -> block.content.split(" ", "\n", "|", "\t").firstOrNull { it.contains("@") && !it.contains("<") }?.trim(';', ',', '.', ':', '(', ')')
                        else -> null
                    }?.takeIf { it.isNotBlank() }

                    val phoneCandidate = when {
                        block.buttonUrl.startsWith("tel:") -> block.buttonUrl.removePrefix("tel:").trim()
                        block.buttonUrl.startsWith("https://wa.me/") -> "+${block.buttonUrl.removePrefix("https://wa.me/").trim()}"
                        block.buttonUrl.all { it.isDigit() || it == '+' } && block.buttonUrl.length >= 8 -> block.buttonUrl.trim()
                        else -> null
                    }?.takeIf { it.isNotBlank() }

                    val locationCandidate = when {
                        block.subtitle.isNotBlank() && (block.subtitle.contains("Street", ignoreCase = true) || block.subtitle.contains("Ave", ignoreCase = true) || block.subtitle.contains("Portland", ignoreCase = true) || block.subtitle.contains("San Francisco", ignoreCase = true) || block.subtitle.contains("Tokyo", ignoreCase = true) || block.subtitle.contains("London", ignoreCase = true) || block.subtitle.contains("NY", ignoreCase = true) || block.subtitle.contains("Road", ignoreCase = true)) -> block.subtitle
                        block.content.contains("Open Daily", ignoreCase = true) -> block.subtitle.ifBlank { "Flagship Roastery & Cafe" }
                        else -> null
                    }?.takeIf { it.isNotBlank() }

                    // Only print contact rows backed by real content. Inventing
                    // a phone number, email or address shipped obvious fake
                    // details on every user site that had no contact block set.
                    if (emailCandidate != null || phoneCandidate != null || locationCandidate != null) {
                        appendLine("          <div class=\"contact-details\">")
                        if (emailCandidate != null) {
                            appendLine("            <div class=\"contact-item\">")
                            appendLine("              <span class=\"contact-icon\">✉️</span>")
                            appendLine("              <div><strong>Email</strong><br><a href=\"mailto:${escapeHtml(emailCandidate)}\">${escapeHtml(emailCandidate)}</a></div>")
                            appendLine("            </div>")
                        }
                        if (phoneCandidate != null) {
                            appendLine("            <div class=\"contact-item\">")
                            appendLine("              <span class=\"contact-icon\">📞</span>")
                            appendLine("              <div><strong>Call</strong><br><a href=\"tel:${escapeHtml(phoneCandidate)}\">${escapeHtml(phoneCandidate)}</a></div>")
                            appendLine("            </div>")
                        }
                        if (locationCandidate != null) {
                            appendLine("            <div class=\"contact-item\">")
                            appendLine("              <span class=\"contact-icon\">📍</span>")
                            appendLine("              <div><strong>Location</strong><br><span>${escapeHtml(locationCandidate)}</span></div>")
                            appendLine("            </div>")
                        }
                        appendLine("          </div>")
                    }
                    appendLine("        </div>")

                    val endpointUrl = when {
                        website?.formEndpoint?.isNotBlank() == true -> website.formEndpoint.trim()
                        block.buttonUrl.startsWith("http://") || block.buttonUrl.startsWith("https://") -> block.buttonUrl.trim()
                        else -> ""
                    }
                    val endpointAttr = if (endpointUrl.isNotBlank()) " data-endpoint=\"${escapeHtml(endpointUrl)}\"" else ""
                    val emailAttr = if (emailCandidate != null) " data-email=\"${escapeHtml(emailCandidate)}\"" else ""
                    appendLine("        <form id=\"contactForm\" class=\"contact-form\"$endpointAttr$emailAttr>")
                    appendLine("          <div class=\"form-group\">")
                    appendLine("            <label for=\"contactName\">Full Name</label>")
                    appendLine("            <input type=\"text\" id=\"contactName\" name=\"name\" required placeholder=\"Jane Doe\">")
                    appendLine("          </div>")
                    appendLine("          <div class=\"form-group\">")
                    appendLine("            <label for=\"contactEmail\">Work Email</label>")
                    appendLine("            <input type=\"email\" id=\"contactEmail\" name=\"email\" required placeholder=\"jane@example.com\">")
                    appendLine("          </div>")
                    appendLine("          <div class=\"form-group\">")
                    appendLine("            <label for=\"contactMessage\">Your Message</label>")
                    appendLine("            <textarea id=\"contactMessage\" name=\"message\" rows=\"4\" required placeholder=\"Tell us about your project or inquiry...\"></textarea>")
                    appendLine("          </div>")
                    val btnLabel = if (block.buttonText.isNotBlank()) block.buttonText else "Submit Message"
                    appendLine("          <button type=\"submit\" class=\"btn btn-primary btn-block\" data-redirect=\"#contact\">${escapeHtml(btnLabel)}</button>")
                    appendLine("        </form>")
                    appendLine("      </div>")
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.FAQ -> {
                val faqs = block.content.split("|").filter { it.isNotBlank() }
                buildString {
                    appendLine("  <section class=\"faq-section layout-${faqLayouts[layoutVariant(siteSeed, block, 2)]}\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container faq-container\">")
                    if (block.title.isNotBlank()) appendLine("      <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
                    if (block.subtitle.isNotBlank()) appendLine("      <p class=\"section-subtitle\">${escapeHtml(block.subtitle)}</p>")
                    appendLine("      <div class=\"faq-list\">")
                    for (faq in faqs) {
                        val parts = faq.split(":", limit = 2)
                        val q = parts.firstOrNull()?.trim() ?: "Question"
                        val a = if (parts.size > 1) parts[1].trim() else "Answer pending."
                        appendLine("        <details class=\"faq-item\">")
                        appendLine("          <summary class=\"faq-question\"><span>${escapeHtml(q)}</span><span class=\"faq-icon\">+</span></summary>")
                        appendLine("          <div class=\"faq-answer\">${escapeHtml(a)}</div>")
                        appendLine("        </details>")
                    }
                    appendLine("      </div>")
                    if (block.buttonText.isNotBlank()) {
                        val url = resolveSmartButtonUrl(block.buttonUrl, block.buttonText, block.type)
                        appendLine("      <div class=\"section-action\">${renderButtonLink(url, block.buttonText, "btn btn-secondary", "#contact")}</div>")
                    }
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.NEWSLETTER -> {
                buildString {
                    appendLine("  <section class=\"newsletter-section\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container newsletter-container\">")
                    if (block.title.isNotBlank()) appendLine("      <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
                    if (block.subtitle.isNotBlank()) appendLine("      <p class=\"section-subtitle\">${escapeHtml(block.subtitle)}</p>")
                    appendLine("      <form id=\"newsletterForm\" class=\"newsletter-form\">")
                    appendLine("        <input type=\"email\" id=\"newsletterEmail\" required placeholder=\"Enter your work email address...\">")
                    val btnText = if (block.buttonText.isNotBlank()) block.buttonText else "Subscribe Free"
                    appendLine("        <button type=\"submit\" class=\"btn btn-primary\" data-redirect=\"#newsletter\">${escapeHtml(btnText)}</button>")
                    appendLine("      </form>")
                    appendLine("      <p class=\"newsletter-privacy\">🔒 Zero spam. Unsubscribe anytime with 1-click.</p>")
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.STATS -> {
                val stats = block.content.split("|").map { it.trim() }.filter { it.isNotBlank() }
                buildString {
                    appendLine("  <section class=\"stats-section layout-${statsLayouts[layoutVariant(siteSeed, block, 2)]}\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container stats-container\">")
                    if (block.subtitle.isNotBlank()) appendLine("      <div class=\"section-header\"><span class=\"badge\">${escapeHtml(block.subtitle)}</span></div>")
                    if (block.title.isNotBlank()) appendLine("      <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
                    appendLine("      <div class=\"stats-grid\">")
                    for (item in stats) {
                        val parts = item.split(":", limit = 2)
                        val num = parts.getOrNull(0)?.trim() ?: item
                        val label = parts.getOrNull(1)?.trim() ?: ""
                        appendLine("        <div class=\"stat-card\">")
                        appendLine("          <div class=\"stat-number\">${escapeHtml(num)}</div>")
                        if (label.isNotBlank()) appendLine("          <div class=\"stat-label\">${escapeHtml(label)}</div>")
                        appendLine("        </div>")
                    }
                    appendLine("      </div>")
                    if (block.buttonText.isNotBlank()) {
                        val url = resolveSmartButtonUrl(block.buttonUrl, block.buttonText, block.type)
                        appendLine("      <div class=\"section-action\">${renderButtonLink(url, block.buttonText, "btn btn-primary", "#pricing")}</div>")
                    }
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.TIMELINE -> {
                val steps = block.content.split("|").map { it.trim() }.filter { it.isNotBlank() }
                buildString {
                    appendLine("  <section class=\"timeline-section\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container timeline-container\">")
                    if (block.subtitle.isNotBlank()) appendLine("      <div class=\"section-header\"><span class=\"badge\">${escapeHtml(block.subtitle)}</span></div>")
                    if (block.title.isNotBlank()) appendLine("      <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
                    appendLine("      <div class=\"timeline-list\">")
                    for ((index, item) in steps.withIndex()) {
                        val parts = item.split(":", limit = 2)
                        val stepTitle = parts.getOrNull(0)?.trim() ?: "Step ${index + 1}"
                        val stepDesc = parts.getOrNull(1)?.trim() ?: ""
                        appendLine("        <div class=\"timeline-item\">")
                        appendLine("          <div class=\"timeline-marker\">${index + 1}</div>")
                        appendLine("          <div class=\"timeline-card\">")
                        appendLine("            <h3 class=\"timeline-step-title\">${escapeHtml(stepTitle)}</h3>")
                        if (stepDesc.isNotBlank()) appendLine("            <p class=\"timeline-step-desc\">${escapeHtml(stepDesc)}</p>")
                        appendLine("          </div>")
                        appendLine("        </div>")
                    }
                    appendLine("      </div>")
                    if (block.buttonText.isNotBlank()) {
                        val url = resolveSmartButtonUrl(block.buttonUrl, block.buttonText, block.type)
                        appendLine("      <div class=\"section-action\">${renderButtonLink(url, block.buttonText, "btn btn-primary", "#pricing")}</div>")
                    }
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.TEAM -> {
                val members = block.content.split("|").map { it.trim() }.filter { it.isNotBlank() }
                buildString {
                    appendLine("  <section class=\"team-section\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container team-container\">")
                    if (block.subtitle.isNotBlank()) appendLine("      <div class=\"section-header\"><span class=\"badge\">${escapeHtml(block.subtitle)}</span></div>")
                    if (block.title.isNotBlank()) appendLine("      <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
                    appendLine("      <div class=\"team-grid\">")
                    for (member in members) {
                        val parts = member.split(":")
                        val name = parts.getOrNull(0)?.trim() ?: member
                        val role = parts.getOrNull(1)?.trim() ?: ""
                        val bio = parts.getOrNull(2)?.trim() ?: ""
                        val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
                        appendLine("        <div class=\"team-card\">")
                        appendLine("          <div class=\"team-avatar\">${if (initials.isNotBlank()) initials else "★"}</div>")
                        appendLine("          <h3 class=\"team-name\">${escapeHtml(name)}</h3>")
                        if (role.isNotBlank()) appendLine("          <p class=\"team-role\">${escapeHtml(role)}</p>")
                        if (bio.isNotBlank()) appendLine("          <p class=\"team-bio\">${escapeHtml(bio)}</p>")
                        appendLine("        </div>")
                    }
                    appendLine("      </div>")
                    if (block.buttonText.isNotBlank()) {
                        val url = resolveSmartButtonUrl(block.buttonUrl, block.buttonText, block.type)
                        appendLine("      <div class=\"section-action\">${renderButtonLink(url, block.buttonText, "btn btn-secondary", "#contact")}</div>")
                    }
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.LOGOS -> {
                val logos = block.content.split("|").map { it.trim() }.filter { it.isNotBlank() }
                buildString {
                    appendLine("  <section class=\"logos-section\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container logos-container\">")
                    if (block.title.isNotBlank()) appendLine("      <p class=\"logos-title\">${escapeHtml(block.title)}</p>")
                    if (block.subtitle.isNotBlank()) appendLine("      <p class=\"logos-subtitle\">${escapeHtml(block.subtitle)}</p>")
                    appendLine("      <div class=\"logos-track\">")
                    for (logo in logos) {
                        appendLine("        <div class=\"logo-badge\">")
                        appendLine("          <span class=\"logo-dot\">✦</span>")
                        appendLine("          <span class=\"logo-text\">${escapeHtml(logo)}</span>")
                        appendLine("        </div>")
                    }
                    appendLine("      </div>")
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.CUSTOM_HTML -> {
                buildString {
                    appendLine("  <section class=\"custom-block\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container\">")
                    if (block.title.isNotBlank()) appendLine("      <h3 class=\"section-title\">${escapeHtml(block.title)}</h3>")
                    appendLine("      ${block.content}")
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.WHATSAPP_SHOP -> compileWhatsAppShop(block, blockIdAttr, styleAttr)

            BlockType.MULTISTEP_WIZARD -> compileMultiStepWizard(block, blockIdAttr, styleAttr)

            BlockType.COUNTDOWN_TIMER -> compileCountdownTimer(block, blockIdAttr, styleAttr)

            BlockType.IMAGE_CAROUSEL -> compileImageCarousel(block, blockIdAttr, styleAttr)

            BlockType.FOOTER -> {
                val socialLinks = block.subtitle.split("•").map { it.trim() }.filter { it.isNotBlank() }
                buildString {
                    appendLine("  <footer class=\"footer\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container footer-container\">")
                    appendLine("      <div class=\"footer-brand\">")
                    appendLine("        <span class=\"brand-logo-mark\">✦</span>")
                    appendLine("        <span>${escapeHtml(block.title)}</span>")
                    appendLine("      </div>")
                    if (block.content.isNotBlank()) appendLine("      <p class=\"footer-copy\">${escapeHtml(block.content)}</p>")
                    if (socialLinks.isNotEmpty()) {
                        appendLine("      <div class=\"footer-links\">")
                        for (link in socialLinks) {
                            val lower = link.lowercase()
                            val smartLink = when {
                                lower.contains("github") -> "https://github.com"
                                lower.contains("twitter") || lower == "x" -> "https://twitter.com"
                                lower.contains("discord") -> "https://discord.com"
                                lower.contains("linkedin") -> "https://linkedin.com"
                                lower.contains("instagram") -> "https://instagram.com"
                                lower.contains("youtube") -> "https://youtube.com"
                                lower.contains("pinterest") -> "https://pinterest.com"
                                lower.contains("dribbble") -> "https://dribbble.com"
                                lower.contains("behance") -> "https://behance.com"
                                lower.contains("bluesky") || lower.contains("bsky") -> "https://bsky.app"
                                lower.contains("mastodon") -> "https://mastodon.social"
                                lower.contains("whatsapp") -> "#"
                                lower.contains("tiktok") -> "https://tiktok.com"
                                lower.contains("facebook") -> "https://facebook.com"
                                lower.contains("yelp") -> "https://yelp.com"
                                lower.contains("rss") || lower.contains("feed") -> "#newsletter"
                                lower.contains("contact") || lower.contains("inquir") -> "#contact"
                                lower.contains("docs") || lower.contains("archive") || lower.contains("blog") -> "#features"
                                else -> "#"
                            }
                            val isExt = smartLink.startsWith("http")
                            val extAttr = if (isExt) " target=\"_blank\" rel=\"noopener noreferrer\"" else ""
                            appendLine("        <a href=\"$smartLink\" class=\"footer-link\"$extAttr data-redirect=\"$smartLink\">${escapeHtml(link)}</a>")
                        }
                        appendLine("      </div>")
                    }
                    appendLine("    </div>")
                    appendLine("  </footer>")
                }
            }
        }
    }

    private fun compileCountdownTimer(block: WebBlockEntity, blockIdAttr: String, styleAttr: String): String {
        val targetTime = if (block.content.isNotBlank()) block.content.trim() else "2026-12-31T23:59:59"
        val btnLabel = if (block.buttonText.isNotBlank()) block.buttonText else "Get Early Access"
        val btnUrl = resolveSmartButtonUrl(block.buttonUrl, block.buttonText, block.type)

        return buildString {
            appendLine("  <section class=\"countdown-section\"$blockIdAttr$styleAttr>")
            appendLine("    <div class=\"container countdown-container\" data-target-time=\"${escapeHtml(targetTime)}\">")
            if (block.title.isNotBlank()) appendLine("      <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
            if (block.subtitle.isNotBlank()) appendLine("      <p class=\"section-subtitle\">${escapeHtml(block.subtitle)}</p>")
            appendLine("      <div class=\"countdown-grid\">")
            appendLine("        <div class=\"countdown-card\"><span class=\"countdown-val countdown-val-days\">00</span><span class=\"countdown-lbl\">Days</span></div>")
            appendLine("        <div class=\"countdown-sep\">:</div>")
            appendLine("        <div class=\"countdown-card\"><span class=\"countdown-val countdown-val-hours\">00</span><span class=\"countdown-lbl\">Hours</span></div>")
            appendLine("        <div class=\"countdown-sep\">:</div>")
            appendLine("        <div class=\"countdown-card\"><span class=\"countdown-val countdown-val-mins\">00</span><span class=\"countdown-lbl\">Minutes</span></div>")
            appendLine("        <div class=\"countdown-sep\">:</div>")
            appendLine("        <div class=\"countdown-card\"><span class=\"countdown-val countdown-val-secs\">00</span><span class=\"countdown-lbl\">Seconds</span></div>")
            appendLine("      </div>")
            if (btnLabel.isNotBlank()) {
                appendLine("      <div class=\"countdown-actions\">")
                appendLine("        ${renderButtonLink(btnUrl, btnLabel, "btn btn-primary btn-lg", "#contact")}")
                appendLine("      </div>")
            }
            appendLine("    </div>")
            appendLine("  </section>")
        }
    }

    private fun compileImageCarousel(block: WebBlockEntity, blockIdAttr: String, styleAttr: String): String {
        val rawImages = block.content.split("|", "\n").map { it.trim() }.filter { it.isNotBlank() }
        val images = if (rawImages.isNotEmpty()) rawImages else listOf(
            "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=1200&q=80",
            "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=1200&q=80",
            "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=1200&q=80",
            "https://images.unsplash.com/photo-1551434678-e076c223a692?w=1200&q=80"
        )

        return buildString {
            appendLine("  <section class=\"carousel-section\"$blockIdAttr$styleAttr>")
            appendLine("    <div class=\"container carousel-container\">")
            if (block.title.isNotBlank()) appendLine("      <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
            if (block.subtitle.isNotBlank()) appendLine("      <p class=\"section-subtitle\">${escapeHtml(block.subtitle)}</p>")
            appendLine("      <div class=\"carousel-wrapper\" id=\"carousel-${block.id.take(8)}\">")
            appendLine("        <div class=\"carousel-track\">")
            for ((idx, img) in images.withIndex()) {
                appendLine("          <div class=\"carousel-slide${if (idx == 0) " active" else ""}\">")
                appendLine("            <img src=\"${escapeHtml(img)}\" alt=\"Visual ${idx + 1}\" class=\"carousel-img\" loading=\"lazy\">")
                appendLine("          </div>")
            }
            appendLine("        </div>")
            appendLine("        <button class=\"carousel-btn prev-btn\" aria-label=\"Previous Slide\">‹</button>")
            appendLine("        <button class=\"carousel-btn next-btn\" aria-label=\"Next Slide\">›</button>")
            appendLine("        <div class=\"carousel-dots\">")
            for (i in images.indices) {
                appendLine("          <span class=\"carousel-dot${if (i == 0) " active" else ""}\" data-slide=\"$i\"></span>")
            }
            appendLine("        </div>")
            appendLine("      </div>")
            if (block.buttonText.isNotBlank()) {
                val btnUrl = resolveSmartButtonUrl(block.buttonUrl, block.buttonText, block.type)
                appendLine("      <div class=\"carousel-cta\">")
                appendLine("        ${renderButtonLink(btnUrl, block.buttonText, "btn btn-primary", "#contact")}")
                appendLine("      </div>")
            }
            appendLine("    </div>")
            appendLine("  </section>")
        }
    }

    private fun buildCss(website: WebsiteEntity, theme: ThemeVars, fontFamily: String): String {
        val buttonRadiusCss = when (website.buttonRadius.lowercase()) {
            "sharp", "square" -> "0px"
            "soft" -> "6px"
            "rounded" -> "12px"
            else -> "9999px" // "pill"
        }

        val buttonStylesCss = when (website.buttonStyle.lowercase()) {
            "solid" -> """
.btn-primary {
  background: var(--accent);
  color: #ffffff;
  box-shadow: 0 4px 14px var(--glow);
  border: none;
}

.btn-primary:hover {
  background: var(--accent-hover);
  transform: translateY(-2px);
  box-shadow: 0 8px 22px var(--glow);
}
""".trimIndent()
            "glass" -> """
.btn-primary {
  background: rgba(var(--accent-rgb), 0.22);
  color: var(--text-primary);
  border: 1.5px solid var(--accent);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  box-shadow: 0 4px 20px var(--glow);
}

.btn-primary:hover {
  background: rgba(var(--accent-rgb), 0.38);
  border-color: var(--accent);
  transform: translateY(-2px);
  box-shadow: 0 8px 28px var(--glow);
}
""".trimIndent()
            "brutalist" -> """
.btn-primary {
  background-color: var(--accent);
  color: #ffffff;
  border: 2.5px solid var(--text-primary);
  box-shadow: 4px 4px 0px var(--text-primary);
  font-weight: 800;
  letter-spacing: 0.03em;
}

.btn-primary:hover {
  transform: translate(-2px, -2px);
  box-shadow: 6px 6px 0px var(--text-primary);
}

.btn-secondary {
  background-color: var(--bg-card);
  color: var(--text-primary);
  border: 2.5px solid var(--text-primary);
  box-shadow: 4px 4px 0px var(--text-primary);
  font-weight: 700;
}

.btn-secondary:hover {
  transform: translate(-2px, -2px);
  box-shadow: 6px 6px 0px var(--text-primary);
}
""".trimIndent()
            "pixel-4bit", "arcade-pixel", "retro-4bit" -> """
.btn-primary {
  background-color: var(--accent);
  color: #ffffff;
  border: 3px solid #000000;
  box-shadow: 4px 4px 0px #000000, -2px -2px 0px rgba(255, 255, 255, 0.4) inset;
  font-family: inherit;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  border-radius: 0px !important;
  image-rendering: pixelated;
  text-shadow: 1px 1px 0px #000000;
}

.btn-primary:hover {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0px #000000;
  background-color: var(--accent-hover);
}

.btn-secondary {
  background-color: var(--bg-card);
  color: var(--text-primary);
  border: 3px solid #000000;
  box-shadow: 4px 4px 0px #000000;
  border-radius: 0px !important;
  font-family: inherit;
  image-rendering: pixelated;
}

.btn-secondary:hover {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0px #000000;
}
""".trimIndent()
            "soft-glow" -> """
.btn-primary {
  background: rgba(var(--accent-rgb), 0.16);
  color: var(--accent);
  border: 1.5px solid rgba(var(--accent-rgb), 0.45);
  box-shadow: 0 0 20px var(--glow);
}

.btn-primary:hover {
  background: var(--accent);
  color: #ffffff;
  transform: translateY(-2px);
  box-shadow: 0 0 35px var(--glow), 0 8px 20px var(--glow);
}
""".trimIndent()
            else -> """
.btn-primary {
  background: linear-gradient(135deg, var(--accent) 0%, var(--accent-hover) 100%);
  color: #ffffff;
  box-shadow: 0 4px 18px var(--glow), 0 1px 0 rgba(255, 255, 255, 0.2) inset;
  border: none;
}

.btn-primary:hover {
  background: linear-gradient(135deg, var(--accent-hover) 0%, var(--accent) 100%);
  transform: translateY(-3px);
  box-shadow: 0 10px 28px var(--glow);
}
""".trimIndent()
        }

        return """
:root {
  --bg-primary: ${theme.bgPrimary};
  --bg-secondary: ${theme.bgSecondary};
  --bg-card: ${theme.bgCard};
  --text-primary: ${theme.textPrimary};
  --text-secondary: ${theme.textSecondary};
  --accent: ${theme.accent};
  --accent-hover: ${theme.accentHover};
  --accent-rgb: ${theme.accentRgb};
  --border: ${theme.border};
  --glow: ${theme.glow};
  --font-family: $fontFamily;
  --btn-radius: $buttonRadiusCss;
}

*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html {
  scroll-behavior: smooth;
}

body {
  font-family: var(--font-family);
  background-color: var(--bg-primary);
  color: var(--text-primary);
  line-height: 1.65;
  min-height: 100vh;
  -webkit-font-smoothing: antialiased;
  position: relative;
  overflow-x: hidden;
}

.container {
  max-width: 1160px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

/* Typography */
h1, h2, h3, h4, h5, h6 {
  color: var(--text-primary);
  line-height: 1.2;
  font-weight: 800;
  letter-spacing: -0.025em;
}

.section-title {
  font-size: clamp(2rem, 4vw, 2.75rem);
  margin-bottom: 0.75rem;
  text-align: center;
}

.section-subtitle {
  color: var(--text-secondary);
  font-size: 1.125rem;
  margin-bottom: 3rem;
  text-align: center;
  max-width: 680px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.6;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.8rem 1.75rem;
  font-size: 1rem;
  font-weight: 700;
  border-radius: var(--btn-radius, 9999px);
  text-decoration: none;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  border: 1px solid transparent;
  outline: none;
}

$buttonStylesCss

.btn-secondary {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  border-color: var(--border);
}

.btn-secondary:hover {
  border-color: var(--accent);
  color: var(--accent);
  transform: translateY(-2px);
}

.btn-sm {
  padding: 0.45rem 1.15rem;
  font-size: 0.875rem;
}

.btn-lg {
  padding: 1rem 2.25rem;
  font-size: 1.125rem;
}

.btn-block {
  width: 100%;
}

/* Navigation Bar */
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: rgba(var(--accent-rgb), 0.03);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border);
  padding: 1.1rem 0;
  transition: background-color 0.3s;
}

.nav-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.35rem;
  font-weight: 800;
  color: var(--text-primary);
  text-decoration: none;
  letter-spacing: -0.03em;
}

.brand-logo-mark {
  color: var(--accent);
  font-size: 1.4rem;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 1.75rem;
}

.nav-link {
  color: var(--text-secondary);
  text-decoration: none;
  font-weight: 600;
  font-size: 0.95rem;
  transition: color 0.2s;
  position: relative;
}

.nav-link:hover {
  color: var(--accent);
}

.nav-toggle {
  display: none;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
}

.nav-toggle span {
  display: block;
  width: 24px;
  height: 2px;
  margin: 5px 0;
  background-color: var(--text-primary);
  transition: all 0.3s ease;
  border-radius: 2px;
}

/* Hero Section */
.hero-section {
  padding: 7.5rem 0 6.5rem;
  text-align: center;
  background: radial-gradient(ellipse 80% 50% at 50% -20%, rgba(var(--accent-rgb), 0.28), transparent 75%),
              radial-gradient(rgba(var(--accent-rgb), 0.08) 1px, transparent 1px) 0 0 / 32px 32px,
              var(--bg-primary);
  position: relative;
  overflow: hidden;
}

.hero-container {
  max-width: 920px;
  position: relative;
  z-index: 2;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.45rem 1.25rem;
  border-radius: 9999px;
  background-color: rgba(var(--accent-rgb), 0.12);
  border: 1px solid rgba(var(--accent-rgb), 0.35);
  color: var(--accent);
  font-size: 0.875rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 4px 14px rgba(var(--accent-rgb), 0.15);
}

.badge-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: var(--accent);
  box-shadow: 0 0 10px var(--accent);
  animation: pulseDot 2s infinite;
}

@keyframes pulseDot {
  0% { transform: scale(0.95); opacity: 0.8; }
  50% { transform: scale(1.25); opacity: 1; box-shadow: 0 0 14px var(--accent); }
  100% { transform: scale(0.95); opacity: 0.8; }
}

.hero-title {
  font-size: clamp(2.8rem, 6.5vw, 4.75rem);
  font-weight: 900;
  letter-spacing: -0.04em;
  margin-bottom: 1.5rem;
  line-height: 1.1;
  background: linear-gradient(135deg, var(--text-primary) 30%, var(--accent) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-desc {
  font-size: clamp(1.15rem, 2.5vw, 1.35rem);
  color: var(--text-secondary);
  max-width: 720px;
  margin: 0 auto 2.5rem;
  line-height: 1.65;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 3rem;
}

.hero-proof-bar {
  display: inline-flex;
  align-items: center;
  gap: 1.5rem;
  padding: 0.85rem 1.75rem;
  border-radius: 9999px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.proof-item {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.proof-number {
  font-weight: 800;
  color: var(--accent);
  font-size: 1.05rem;
}

.proof-label {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.proof-sep {
  color: var(--border);
}

/* Grids */
.grid-3 {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

/* ---- Layout variants -------------------------------------------------
   Every block used to render one fixed composition, so all sites looked
   identical. These rules recompose the shared card markup per layout
   class emitted by SiteCompiler.layoutVariant(). */

.features-section.layout-bento .grid-3 { grid-template-columns: repeat(6, 1fr); }
.features-section.layout-bento .feature-card { grid-column: span 2; }
.features-section.layout-bento .feature-card:first-child {
  grid-column: span 6;
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 2rem;
  background: linear-gradient(135deg, var(--bg-card), var(--bg-secondary));
}
.features-section.layout-bento .feature-card:first-child .icon-badge { grid-row: span 2; }

.features-section.layout-rows .grid-3 { grid-template-columns: 1fr; gap: 1rem; }
.features-section.layout-rows .feature-card { display: flex; align-items: center; gap: 1.5rem; padding: 1.5rem 2rem; }
.features-section.layout-rows .feature-card .icon-badge { flex: 0 0 auto; }
.features-section.layout-rows .feature-card .card-title { margin: 0 0 .25rem; }
.features-section.layout-rows .feature-card:nth-child(even) { margin-left: 8%; }

.services-section.layout-wide .grid-3 { grid-template-columns: repeat(auto-fit, minmax(420px, 1fr)); }
.services-section.layout-wide .service-card { display: flex; flex-direction: column; }
.services-section.layout-wide .service-link { margin-top: auto; padding-top: 1.25rem; }

.gallery-section.layout-masonry .grid-3 {
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  grid-auto-rows: 220px;
}
.gallery-section.layout-masonry .gallery-item { overflow: hidden; border-radius: 1.25rem; }
.gallery-section.layout-masonry .gallery-item:nth-child(3n+1) { grid-row: span 2; }
.gallery-section.layout-masonry .gallery-item img { height: 100%; object-fit: cover; }

.pricing-section.layout-rows .grid-3 { grid-template-columns: 1fr; gap: 1rem; }
.pricing-section.layout-rows .pricing-card {
  display: grid;
  grid-template-columns: 1.1fr .8fr 1.4fr auto;
  align-items: center;
  gap: 1.5rem;
  padding: 1.5rem 2rem;
}
.pricing-section.layout-rows .pricing-features { display: flex; flex-wrap: wrap; gap: .5rem 1.25rem; }
.pricing-section.layout-rows .pricing-card .btn-block { width: auto; }

.stats-section.layout-strip .stats-grid { grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); }
.stats-section.layout-strip .stat-card { padding: 1.25rem 1rem; }
.stats-section.layout-strip .stat-number { font-size: 2rem; }

.testimonial-section.layout-grid .testimonial-card { max-width: none; }

/* Second wave of structural variants: about / faq / cta / contact. */

.about-section.layout-reversed .about-split-layout { direction: rtl; }
.about-section.layout-reversed .about-split-layout > * { direction: ltr; }

.faq-section.layout-twocol .faq-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 1rem;
  align-items: start;
}

.cta-section.layout-card .cta-container {
  max-width: 760px;
  background-color: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 2rem;
  padding: 3.5rem 2.5rem;
  margin-top: 2rem;
  margin-bottom: 2rem;
}
.cta-section.layout-banner .cta-container { max-width: 1100px; }

.contact-section.layout-formleft .contact-grid { direction: rtl; }
.contact-section.layout-formleft .contact-grid > * { direction: ltr; }
.contact-section.layout-formleft .contact-info-panel { order: 2; }
.contact-section.layout-formright .contact-info-panel { order: 2; }

@media (max-width: 900px) {
  .about-section.layout-reversed .about-split-layout { direction: ltr; }
  .faq-section.layout-twocol .faq-list { grid-template-columns: 1fr; }
  .contact-section.layout-formleft .contact-grid { direction: ltr; }
  .contact-section.layout-formleft .contact-info-panel,
  .contact-section.layout-formright .contact-info-panel { order: initial; }
}

.hero-section.hero--v1 .hero-container {
  display: grid;
  grid-template-columns: 1.05fr .95fr;
  align-items: center;
  gap: 3.5rem;
  text-align: left;
}
.hero-section.hero--v1 .hero-media-wrap { order: -1; }
.hero-section.hero--v1 .hero-proof-bar { grid-column: 1 / -1; justify-content: flex-start; }

.hero-section.hero--v2 { position: relative; isolation: isolate; padding: 7rem 0 5rem; }
.hero-section.hero--v2 .hero-bg {
  position: absolute; inset: 0; z-index: -2;
  background-size: cover; background-position: center;
}
.hero-section.hero--v2::after {
  content: ""; position: absolute; inset: 0; z-index: -1;
  background: linear-gradient(180deg, rgba(0,0,0,.55), rgba(0,0,0,.78));
}
.hero-section.hero--v2 .hero-title,
.hero-section.hero--v2 .hero-desc,
.hero-section.hero--v2 .hero-badge { color: #fff; }

@media (max-width: 900px) {
  .features-section.layout-bento .grid-3 { grid-template-columns: 1fr; }
  .features-section.layout-bento .feature-card,
  .features-section.layout-bento .feature-card:first-child { grid-column: auto; grid-template-columns: 1fr; }
  .features-section.layout-rows .feature-card:nth-child(even) { margin-left: 0; }
  .pricing-section.layout-rows .pricing-card { grid-template-columns: 1fr; }
  .hero-section.hero--v1 .hero-container { grid-template-columns: 1fr; }
  .hero-section.hero--v1 .hero-media-wrap { order: 0; }
}

/* Cards */
.feature-card, .service-card, .pricing-card {
  background-color: var(--bg-card);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid var(--border);
  border-radius: 1.5rem;
  padding: 2.25rem;
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.3s cubic-bezier(0.16, 1, 0.3, 1), border-color 0.3s ease;
  position: relative;
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.25);
}

.feature-card:hover, .service-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 40px -10px var(--glow), 0 0 0 1px var(--accent);
  border-color: var(--accent);
}

.icon-badge {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(var(--accent-rgb), 0.2) 0%, rgba(var(--accent-rgb), 0.05) 100%);
  border: 1px solid rgba(var(--accent-rgb), 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.75rem;
  margin-bottom: 1.25rem;
  box-shadow: 0 4px 14px rgba(var(--accent-rgb), 0.15);
}

.card-title {
  font-size: 1.35rem;
  margin-bottom: 0.85rem;
}

.card-desc {
  color: var(--text-secondary);
  font-size: 1rem;
  line-height: 1.6;
}

.service-link {
  display: inline-block;
  margin-top: 1.25rem;
  color: var(--accent);
  text-decoration: none;
  font-weight: 700;
  font-size: 0.95rem;
  transition: transform 0.2s;
}

.service-link:hover {
  transform: translateX(4px);
}

/* Sections Padding */
.features-section, .services-section, .about-section, .pricing-section, .gallery-section, .contact-section, .faq-section, .newsletter-section, .custom-block {
  padding: 5.5rem 0;
}

/* About Section */
.about-container {
  max-width: 820px;
  text-align: center;
}

.about-text {
  font-size: 1.25rem;
  color: var(--text-secondary);
  margin-bottom: 2.5rem;
  line-height: 1.85;
}

.about-split-layout {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 2.5rem;
  align-items: center;
  text-align: left;
}

.about-image-wrapper {
  border-radius: 1.25rem;
  overflow: hidden;
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.12);
  border: 1px solid var(--border);
}

.about-image {
  width: 100%;
  height: 100%;
  max-height: 380px;
  object-fit: cover;
  display: block;
}

@media (max-width: 768px) {
  .about-split-layout {
    grid-template-columns: 1fr;
    text-align: center;
  }
}

/* Testimonials */
.testimonial-section {
  padding: 5.5rem 0;
  background-color: var(--bg-secondary);
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
}

.testimonial-card {
  max-width: 780px;
  margin: 0 auto;
  text-align: center;
  background: var(--bg-card);
  border: 1px solid var(--border);
  padding: 3rem 2.5rem;
  border-radius: 1.5rem;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
}

.stars {
  color: #fbbf24;
  font-size: 1.5rem;
  margin-bottom: 1.25rem;
  letter-spacing: 0.2em;
}

.quote {
  font-size: 1.4rem;
  font-style: italic;
  margin-bottom: 2rem;
  color: var(--text-primary);
  line-height: 1.65;
}

.testimonial-author-box {
  display: inline-flex;
  align-items: center;
  gap: 0.85rem;
  text-align: left;
}

.author-avatar-badge {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: var(--accent);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 1.1rem;
}

.author {
  font-size: 1.05rem;
  color: var(--text-primary);
  font-style: normal;
  font-weight: 700;
}

.verified-badge {
  font-size: 0.8rem;
  color: #10B981;
  font-weight: 600;
}

/* Pricing Table & Billing Toggle */
.billing-toggle-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 3.5rem;
}

.billing-opt {
  font-weight: 700;
  font-size: 1rem;
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.billing-opt.active {
  color: var(--text-primary);
}

.save-badge {
  background: #10B981;
  color: #ffffff;
  font-size: 0.75rem;
  padding: 0.2rem 0.6rem;
  border-radius: 9999px;
  font-weight: 800;
}

.billing-switch {
  position: relative;
  display: inline-block;
  width: 52px;
  height: 28px;
}

.billing-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.billing-slider {
  position: absolute;
  cursor: pointer;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: var(--border);
  transition: 0.3s;
  border-radius: 34px;
}

.billing-slider:before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
}

.billing-switch input:checked + .billing-slider {
  background-color: var(--accent);
}

.billing-switch input:checked + .billing-slider:before {
  transform: translateX(24px);
}

.pricing-card {
  display: flex;
  flex-direction: column;
}

.pricing-card.popular {
  border: 2px solid var(--accent);
  box-shadow: 0 16px 40px var(--glow);
  transform: scale(1.02);
}

.badge-popular {
  position: absolute;
  top: -14px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--accent);
  color: #ffffff;
  font-size: 0.75rem;
  font-weight: 800;
  padding: 0.35rem 1rem;
  border-radius: 9999px;
  letter-spacing: 0.05em;
  white-space: nowrap;
}

.price-box {
  margin: 1.25rem 0 1.75rem;
  display: flex;
  align-items: baseline;
  gap: 0.35rem;
}

.price-amount {
  font-size: 3rem;
  font-weight: 800;
  line-height: 1;
}

.price-period {
  font-size: 1.1rem;
  color: var(--text-secondary);
}

.pricing-features {
  list-style: none;
  margin: 0 0 2.5rem;
  flex-grow: 1;
}

.pricing-features li {
  padding: 0.6rem 0;
  color: var(--text-secondary);
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  border-bottom: 1px solid rgba(var(--accent-rgb), 0.08);
}

.check-icon {
  color: #10B981;
  font-weight: 800;
}

/* Gallery Section */
.gallery-item {
  border-radius: 1rem;
  overflow: hidden;
  height: 260px;
  border: 1px solid var(--border);
}

.gallery-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.gallery-item:hover img {
  transform: scale(1.06);
}

/* CTA Section */
.cta-section {
  padding: 6.5rem 0;
  background: radial-gradient(circle at 50% 50%, rgba(var(--accent-rgb), 0.25) 0%, var(--bg-card) 100%);
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
  text-align: center;
}

.cta-container {
  max-width: 760px;
}

.cta-title {
  font-size: clamp(2.25rem, 5vw, 3.5rem);
  margin-bottom: 1.25rem;
}

.cta-desc {
  color: var(--text-secondary);
  font-size: 1.25rem;
  margin-bottom: 2.5rem;
}

/* Contact Section */
.contact-container {
  max-width: 980px;
}

.contact-grid {
  display: grid;
  grid-template-columns: 1fr 1.3fr;
  gap: 3rem;
  align-items: start;
}

.contact-info-panel {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.contact-desc {
  font-size: 1.15rem;
  color: var(--text-secondary);
  line-height: 1.7;
}

.contact-details {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.contact-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  background: var(--bg-card);
  padding: 1rem 1.25rem;
  border-radius: 1rem;
  border: 1px solid var(--border);
}

.contact-icon {
  font-size: 1.5rem;
}

.contact-item a {
  color: var(--accent);
  text-decoration: none;
  font-weight: 600;
}

.contact-form {
  background-color: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 1.25rem;
  padding: 2.25rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  text-align: left;
}

.form-group label {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--text-secondary);
}

.form-group input, .form-group textarea, .newsletter-form input {
  padding: 0.85rem 1.15rem;
  border-radius: 0.75rem;
  border: 1px solid var(--border);
  background-color: var(--bg-primary);
  color: var(--text-primary);
  font-family: inherit;
  font-size: 1rem;
  outline: none;
  transition: all 0.2s ease;
}

.form-group input:focus, .form-group textarea:focus, .newsletter-form input:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--glow);
}

/* In-Card Form Feedback */
.form-alert {
  padding: 0.9rem 1.25rem;
  border-radius: 0.75rem;
  font-size: 0.95rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  animation: slideDown 0.3s ease;
}

.form-alert.success {
  background: rgba(16, 185, 129, 0.15);
  border: 1px solid #10B981;
  color: #10B981;
}

.form-alert.info {
  background: rgba(99, 102, 241, 0.15);
  border: 1px solid #6366F1;
  color: #6366F1;
}

.form-alert.fade-out {
  opacity: 0;
  transition: opacity 0.4s ease;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* FAQ Accordion */
.faq-container {
  max-width: 780px;
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.faq-item {
  background-color: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 1rem;
  padding: 1.4rem 1.75rem;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.faq-item:hover {
  border-color: var(--accent);
}

.faq-item[open] {
  border-color: var(--accent);
  box-shadow: 0 8px 24px var(--glow);
}

.faq-question {
  font-weight: 700;
  font-size: 1.15rem;
  color: var(--text-primary);
  outline: none;
  list-style: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.faq-question::-webkit-details-marker {
  display: none;
}

.faq-icon {
  font-size: 1.5rem;
  color: var(--accent);
  transition: transform 0.25s ease;
}

.faq-item[open] .faq-icon {
  transform: rotate(45deg);
}

.faq-answer {
  margin-top: 1rem;
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 1rem;
}

/* Newsletter */
.newsletter-container {
  max-width: 640px;
  text-align: center;
  background: var(--bg-card);
  border: 1px solid var(--border);
  padding: 3.5rem 2.5rem;
  border-radius: 1.75rem;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.08);
}

.newsletter-form {
  display: flex;
  gap: 0.75rem;
  margin: 1.5rem 0 1rem;
}

.newsletter-form input {
  flex-grow: 1;
}

.newsletter-privacy {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

/* Impact Stats Section */
.stats-section {
  padding: 5.5rem 0;
  background-color: var(--bg-secondary);
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.75rem;
  margin-top: 3rem;
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 1.25rem;
  padding: 2.25rem 1.5rem;
  text-align: center;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
  transition: transform 0.25s ease, border-color 0.25s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  border-color: var(--accent);
}

.stat-number {
  font-size: 2.75rem;
  font-weight: 900;
  color: var(--accent);
  line-height: 1.1;
  margin-bottom: 0.6rem;
  letter-spacing: -0.02em;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 1rem;
  font-weight: 600;
}

/* Timeline & Process Section */
.timeline-section {
  padding: 6rem 0;
}

.timeline-list {
  max-width: 720px;
  margin: 3rem auto 0;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.timeline-item {
  display: flex;
  gap: 1.25rem;
  align-items: flex-start;
  text-align: left;
}

.timeline-marker {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--accent);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 1.1rem;
  flex-shrink: 0;
  box-shadow: 0 4px 12px var(--glow);
}

.timeline-card {
  flex: 1;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 1rem;
  padding: 1.4rem 1.6rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.timeline-step-title {
  font-size: 1.2rem;
  font-weight: 700;
  margin-bottom: 0.4rem;
  color: var(--text-primary);
}

.timeline-step-desc {
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.6;
}

/* Team Section */
.team-section {
  padding: 6rem 0;
  background-color: var(--bg-secondary);
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
}

.team-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 2rem;
  margin-top: 3rem;
}

.team-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 1.25rem;
  padding: 2.25rem 1.5rem;
  text-align: center;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
  transition: transform 0.25s ease;
}

.team-card:hover {
  transform: translateY(-4px);
}

.team-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent), #818cf8);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 800;
  margin: 0 auto 1.25rem;
  box-shadow: 0 4px 14px var(--glow);
}

.team-name {
  font-size: 1.25rem;
  font-weight: 700;
  margin-bottom: 0.35rem;
}

.team-role {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--accent);
  margin-bottom: 0.75rem;
}

.team-bio {
  font-size: 0.9rem;
  color: var(--text-secondary);
  line-height: 1.6;
}

/* Logos & Partner Cloud */
.logos-section {
  padding: 3.5rem 0;
  background-color: var(--bg-primary);
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
}

.logos-container {
  text-align: center;
}

.logos-title {
  font-size: 0.95rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
}

.logos-subtitle {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin-bottom: 1.5rem;
}

.logos-track {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 1rem 1.25rem;
  align-items: center;
}

.logo-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: var(--bg-card);
  border: 1px solid var(--border);
  padding: 0.6rem 1.25rem;
  border-radius: 9999px;
  font-weight: 700;
  font-size: 0.95rem;
  color: var(--text-primary);
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.logo-badge:hover {
  border-color: var(--accent);
  transform: translateY(-2px);
}

.logo-dot {
  color: var(--accent);
  font-size: 0.8rem;
}

/* Footer */
.footer {
  padding: 4rem 0 3rem;
  background-color: var(--bg-secondary);
  border-top: 1px solid var(--border);
  text-align: center;
}

.footer-brand {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.35rem;
  font-weight: 800;
  margin-bottom: 0.75rem;
}

.footer-copy {
  color: var(--text-secondary);
  font-size: 0.95rem;
  margin-bottom: 1.75rem;
}

.footer-links {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.footer-link {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 600;
  transition: color 0.2s;
  padding: 0.35rem 0.85rem;
  border-radius: 9999px;
  background: rgba(var(--accent-rgb), 0.06);
}

.footer-link:hover {
  color: var(--accent);
  background: rgba(var(--accent-rgb), 0.12);
}

.section-action {
  text-align: center;
  margin-top: 2.5rem;
}

.section-highlight {
  outline: 3px solid var(--accent) !important;
  outline-offset: 6px !important;
  transition: outline 0.4s ease;
}

/* Floating Back to Top Button */
.back-to-top {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: var(--accent);
  color: #ffffff;
  border: none;
  font-size: 1.35rem;
  font-weight: 900;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px var(--glow);
  opacity: 0;
  pointer-events: none;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  z-index: 99;
}

.back-to-top.visible {
  opacity: 1;
  pointer-events: auto;
  transform: translateY(0);
}

.back-to-top:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 30px var(--glow);
}

/* Responsive Mobile Navigation */
@media (max-width: 768px) {
  .nav-toggle {
    display: block;
  }

  .nav-links {
    display: none;
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background-color: var(--bg-primary);
    flex-direction: column;
    padding: 2rem;
    border-bottom: 1px solid var(--border);
    gap: 1.25rem;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  }

  .nav-links.active {
    display: flex;
  }

  .contact-grid {
    grid-template-columns: 1fr;
    gap: 2rem;
  }

  .newsletter-form {
    flex-direction: column;
  }

  .hero-proof-bar {
    flex-direction: column;
    gap: 0.75rem;
  }

  .proof-sep {
    display: none;
  }
}

/* ==========================================================================
   Smooth Animations & Modern Micro-Interactions
   ========================================================================== */
.anim-fade-up {
  opacity: 0;
  transform: translateY(26px);
  transition: opacity 0.7s cubic-bezier(0.16, 1, 0.3, 1), transform 0.7s cubic-bezier(0.16, 1, 0.3, 1);
  will-change: opacity, transform;
}

.anim-fade-up.in-view {
  opacity: 1;
  transform: translateY(0);
}

.hero-title {
  background: linear-gradient(135deg, var(--text-primary) 20%, var(--accent) 70%, var(--text-primary) 100%);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: heroTextGleam 6s ease-in-out infinite;
}

@keyframes heroTextGleam {
  0%, 100% { background-position: 0% center; }
  50% { background-position: 100% center; }
}

.hero-section::before {
  content: '';
  position: absolute;
  top: -15%;
  left: 50%;
  transform: translateX(-50%);
  width: min(90vw, 700px);
  height: 480px;
  background: radial-gradient(circle, rgba(var(--accent-rgb), 0.22) 0%, transparent 68%);
  filter: blur(55px);
  pointer-events: none;
  animation: heroAuraPulse 7s ease-in-out infinite alternate;
  z-index: 1;
}

@keyframes heroAuraPulse {
  0% { opacity: 0.5; transform: translateX(-50%) scale(0.95); }
  50% { opacity: 0.85; transform: translateX(-50%) scale(1.08); }
  100% { opacity: 0.5; transform: translateX(-50%) scale(0.95); }
}

.hero-badge {
  animation: badgeFloating 4s ease-in-out infinite;
}

@keyframes badgeFloating {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px -6px var(--glow), 0 0 0 1px rgba(var(--accent-rgb), 0.4);
}

.feature-card:hover, .service-card:hover, .pricing-card:hover, .gallery-card:hover, .team-card:hover {
  transform: translateY(-7px);
  box-shadow: 0 22px 45px -12px var(--glow), 0 0 0 1px var(--accent);
  border-color: var(--accent);
}

.icon-badge {
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.feature-card:hover .icon-badge, .service-card:hover .icon-badge {
  transform: scale(1.12) rotate(4deg);
}

/* Live Launch Countdown Timer */
.countdown-section {
  padding: 80px 0;
  text-align: center;
}
.countdown-container {
  max-width: 800px;
  margin: 0 auto;
}
.countdown-grid {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin: 36px 0;
  flex-wrap: wrap;
}
.countdown-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  backdrop-filter: blur(14px);
  border-radius: var(--btn-radius);
  padding: 24px 20px;
  min-width: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease, border-color 0.3s ease;
}
.countdown-card:hover {
  transform: translateY(-4px);
  border-color: var(--accent);
}
.countdown-val {
  font-size: 2.8rem;
  font-weight: 800;
  font-family: monospace;
  color: var(--accent);
  line-height: 1;
}
.countdown-lbl {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--text-secondary);
  margin-top: 8px;
  font-weight: 600;
}
.countdown-sep {
  font-size: 2rem;
  font-weight: 800;
  color: var(--accent);
  opacity: 0.6;
}
.countdown-actions {
  margin-top: 24px;
}

/* Interactive Image Carousel */
.carousel-section {
  padding: 70px 0;
}
.carousel-wrapper {
  position: relative;
  overflow: hidden;
  border-radius: var(--btn-radius);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.25);
  max-width: 1000px;
  margin: 32px auto 0;
}
.carousel-track {
  display: flex;
  transition: transform 0.5s cubic-bezier(0.16, 1, 0.3, 1);
  width: 100%;
}
.carousel-slide {
  min-width: 100%;
  box-sizing: border-box;
}
.carousel-img {
  width: 100%;
  height: 480px;
  object-fit: cover;
  display: block;
}
.carousel-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.25);
  width: 44px;
  height: 44px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  backdrop-filter: blur(8px);
  z-index: 10;
  transition: background 0.2s ease, transform 0.2s ease;
}
.carousel-btn:hover {
  background: var(--accent);
  transform: translateY(-50%) scale(1.1);
}
.prev-btn { left: 16px; }
.next-btn { right: 16px; }
.carousel-dots {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 10;
}
.carousel-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  transition: all 0.3s ease;
}
.carousel-dot.active {
  background: var(--accent);
  width: 28px;
  border-radius: 8px;
}
.carousel-cta {
  text-align: center;
  margin-top: 24px;
}

/* Floating Visitor Theme Switcher */
.visitor-theme-toggle {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--card-bg);
  border: 1.5px solid var(--border-color);
  color: var(--text-primary);
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 999;
  backdrop-filter: blur(12px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}
.visitor-theme-toggle:hover {
  transform: scale(1.1) rotate(15deg);
  border-color: var(--accent);
}

/* Light Theme Override */
body.light-theme {
  --bg-primary: #f8fafc;
  --bg-secondary: #ffffff;
  --text-primary: #0f172a;
  --text-secondary: #475569;
  --card-bg: rgba(255, 255, 255, 0.9);
  --border-color: rgba(15, 23, 42, 0.1);
  background-color: var(--bg-primary);
  color: var(--text-primary);
}
body.light-theme .navbar {
  background: rgba(255, 255, 255, 0.85);
}
body.light-theme .contact-form,
body.light-theme .feature-card,
body.light-theme .pricing-card,
body.light-theme .service-card,
body.light-theme .faq-item,
body.light-theme .countdown-card {
  background: rgba(255, 255, 255, 0.94);
  border-color: rgba(15, 23, 42, 0.1);
}

/* Entrance & Scroll Motion Animations */
[data-animate] {
  opacity: 0;
  transition: opacity 0.7s cubic-bezier(0.16, 1, 0.3, 1), transform 0.7s cubic-bezier(0.16, 1, 0.3, 1);
  will-change: opacity, transform;
}
[data-animate="fade-up"], [data-animate="default"] {
  transform: translateY(32px);
}
[data-animate="slide-left"] {
  transform: translateX(-40px);
}
[data-animate="zoom-in"] {
  transform: scale(0.92);
}
[data-animate="soft-pop"] {
  transform: translateY(20px) scale(0.95);
}
[data-animate="fade-in"] {
  opacity: 0;
}
[data-animate="flip-up"] {
  transform: perspective(700px) rotateX(-14deg);
  transform-origin: center bottom;
}
[data-animate].is-inview, [data-animate="none"] {
  opacity: 1;
  transform: none;
}

/* 4-Bit & Anime Pixel Aesthetics */
.crt-overlay {
  position: fixed;
  top: 0; left: 0; width: 100vw; height: 100vh;
  pointer-events: none;
  background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.22) 50%);
  background-size: 100% 4px;
  z-index: 9999;
  opacity: 0.35;
}

/* Custom CSS from user */
${website.customCss}
""".trimIndent()
    }

    private fun buildJs(website: WebsiteEntity): String {
        return """
// High-End Responsive Web Engine with Zero Debug Clutter
document.addEventListener('DOMContentLoaded', () => {
  // Authentic Procedural 8-Bit / 4-Bit Web Audio Synthesizer for Retro Anime Sound FX
  function play8BitBleep(pitch, dur, type) {
    try {
      pitch = pitch || 587.33;
      dur = dur || 0.08;
      type = type || 'square';
      const AC = window.AudioContext || window.webkitAudioContext;
      if (!AC) return;
      if (!window._sfxCtx) window._sfxCtx = new AC();
      const ctx = window._sfxCtx;
      if (ctx.state === 'suspended') ctx.resume();
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = type;
      osc.frequency.setValueAtTime(pitch, ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(pitch * 1.5, ctx.currentTime + dur);
      gain.gain.setValueAtTime(0.08, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + dur);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start();
      osc.stop(ctx.currentTime + dur);
    } catch(e) {}
  }

  // Bind retro sound effect clicks for buttons and interactive controls
  document.querySelectorAll('.btn, button, .nav-link, .pricing-card, .faq-item summary, .carousel-btn, .carousel-dot').forEach(el => {
    el.addEventListener('click', () => {
      play8BitBleep(659.25, 0.07, 'square');
    });
  });

  // Mobile Navigation Drawer Toggle
  const navToggle = document.getElementById('navToggle');
  const navLinks = document.getElementById('navLinks');
  if (navToggle && navLinks) {
    navToggle.addEventListener('click', () => {
      navLinks.classList.toggle('active');
    });
  }

  // Ultra-Smooth Anchor Navigation without any debug popups
  document.querySelectorAll('a[href^="#"], button[data-redirect^="#"]').forEach(element => {
    element.addEventListener('click', (e) => {
      const href = element.getAttribute('href') || element.getAttribute('data-redirect') || '';
      const rawTarget = href.substring(1).toLowerCase().trim();
      if (!rawTarget) return;

      // Close mobile menu if open
      if (navLinks) navLinks.classList.remove('active');

      // Bridge communication for Android WebView host
      if (window.AndroidBridge && typeof window.AndroidBridge.onLinkTapped === 'function') {
        try { window.AndroidBridge.onLinkTapped(href, (element.textContent || '').trim()); } catch (err) {}
      }

      let targetEl = document.getElementById(rawTarget)
        || document.querySelector('[data-aliases~="' + rawTarget + '"]')
        || document.querySelector('[data-block-id="' + rawTarget + '"]')
        || document.querySelector('[data-block-type="' + rawTarget.toUpperCase() + '"]')
        || document.querySelector('.' + rawTarget + '-section')
        || document.querySelector('section[id*="' + rawTarget + '"]');

      if (!targetEl) {
        const headings = document.querySelectorAll('h1, h2, h3, .section-title');
        for (let h of headings) {
          if (h.textContent.toLowerCase().includes(rawTarget)) {
            targetEl = h.closest('section') || h;
            break;
          }
        }
      }

      if (targetEl) {
        e.preventDefault();
        targetEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
        targetEl.classList.add('section-highlight');
        setTimeout(() => targetEl.classList.remove('section-highlight'), 1200);
      }
    });
  });

  // Interactive Pricing Tier Billing Switcher (Monthly / Annual with 20% Discount)
  const billingToggle = document.getElementById('billingToggle');
  const optMonthly = document.getElementById('optMonthly');
  const optAnnual = document.getElementById('optAnnual');

  function updateBilling(isAnnual) {
    if (optMonthly && optAnnual) {
      optMonthly.classList.toggle('active', !isAnnual);
      optAnnual.classList.toggle('active', isAnnual);
    }
    document.querySelectorAll('.price-amount').forEach(el => {
      const monthly = el.getAttribute('data-monthly');
      const annual = el.getAttribute('data-annual');
      if (monthly && annual) {
        el.textContent = isAnnual ? annual : monthly;
      }
    });
    document.querySelectorAll('.price-period').forEach(el => {
      el.textContent = isAnnual ? '/yr' : '/mo';
    });
  }

  if (billingToggle) {
    billingToggle.addEventListener('change', (e) => {
      updateBilling(e.target.checked);
    });
  }
  if (optMonthly && billingToggle) {
    optMonthly.addEventListener('click', () => {
      billingToggle.checked = false;
      updateBilling(false);
    });
  }
  if (optAnnual && billingToggle) {
    optAnnual.addEventListener('click', () => {
      billingToggle.checked = true;
      updateBilling(true);
    });
  }

  // Pricing Plan Click auto-selects and prefills the Contact Form inquiry
  document.querySelectorAll('.pricing-card .btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const card = btn.closest('.pricing-card');
      const planTitle = card ? (card.querySelector('.card-title')?.textContent || '').trim() : '';
      const messageField = document.getElementById('contactMessage');
      if (messageField && planTitle) {
        messageField.value = 'Hello! I would like to get started with the ' + planTitle + ' plan.';
      }
    });
  });

  // Smooth Single-Accordion Behaviour for FAQs
  document.querySelectorAll('.faq-item').forEach(item => {
    item.addEventListener('toggle', () => {
      if (item.open) {
        document.querySelectorAll('.faq-item').forEach(other => {
          if (other !== item) other.removeAttribute('open');
        });
      }
    });
  });

  // Newsletter Form Submission with Realistic In-Card Feedback
  const newsletterForm = document.getElementById('newsletterForm');
  if (newsletterForm) {
    newsletterForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const btn = newsletterForm.querySelector('button[type="submit"]');
      const originalText = btn ? btn.innerHTML : 'Subscribe';
      if (btn) {
        btn.disabled = true;
        btn.innerHTML = 'Subscribing...';
      }
      setTimeout(() => {
        if (btn) {
          btn.disabled = false;
          btn.innerHTML = '✓ Subscribed!';
          btn.style.backgroundColor = '#10B981';
          btn.style.borderColor = '#10B981';
        }
        showFormFeedback(newsletterForm, 'success', 'Welcome aboard! Check your inbox for your confirmation.');
        newsletterForm.reset();
        setTimeout(() => {
          if (btn) {
            btn.innerHTML = originalText;
            btn.style.backgroundColor = '';
            btn.style.borderColor = '';
          }
        }, 3500);
      }, 500);
    });
  }

  // Back to Top Button
  const backToTopBtn = document.getElementById('backToTop');
  if (backToTopBtn) {
    window.addEventListener('scroll', () => {
      if (window.scrollY > 350) {
        backToTopBtn.classList.add('visible');
      } else {
        backToTopBtn.classList.remove('visible');
      }
    });
    backToTopBtn.addEventListener('click', () => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  }

  // Live Launch Countdown Timer Runner
  document.querySelectorAll('.countdown-container[data-target-time]').forEach(container => {
    const rawTarget = container.getAttribute('data-target-time');
    let targetDate = new Date(rawTarget);
    if (isNaN(targetDate.getTime())) {
      targetDate = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
    }
    const daysEl = container.querySelector('.countdown-val-days');
    const hoursEl = container.querySelector('.countdown-val-hours');
    const minsEl = container.querySelector('.countdown-val-mins');
    const secsEl = container.querySelector('.countdown-val-secs');

    function update() {
      const now = new Date().getTime();
      const diff = Math.max(0, targetDate.getTime() - now);
      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const mins = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
      const secs = Math.floor((diff % (1000 * 60)) / 1000);

      if (daysEl) daysEl.textContent = String(days).padStart(2, '0');
      if (hoursEl) hoursEl.textContent = String(hours).padStart(2, '0');
      if (minsEl) minsEl.textContent = String(mins).padStart(2, '0');
      if (secsEl) secsEl.textContent = String(secs).padStart(2, '0');
    }
    update();
    setInterval(update, 1000);
  });

  // Interactive Image Carousel
  document.querySelectorAll('.carousel-wrapper').forEach(wrapper => {
    const track = wrapper.querySelector('.carousel-track');
    const slides = wrapper.querySelectorAll('.carousel-slide');
    const dots = wrapper.querySelectorAll('.carousel-dot');
    const prevBtn = wrapper.querySelector('.prev-btn');
    const nextBtn = wrapper.querySelector('.next-btn');
    if (!track || slides.length <= 1) return;

    let currentIndex = 0;
    function goToSlide(index) {
      if (index < 0) index = slides.length - 1;
      if (index >= slides.length) index = 0;
      currentIndex = index;
      track.style.transform = 'translateX(-' + (currentIndex * 100) + '%)';
      dots.forEach((d, i) => d.classList.toggle('active', i === currentIndex));
    }

    if (prevBtn) prevBtn.addEventListener('click', () => goToSlide(currentIndex - 1));
    if (nextBtn) nextBtn.addEventListener('click', () => goToSlide(currentIndex + 1));
    dots.forEach((d, i) => d.addEventListener('click', () => goToSlide(i)));

    let startX = 0;
    wrapper.addEventListener('touchstart', e => { startX = e.touches[0].clientX; }, { passive: true });
    wrapper.addEventListener('touchend', e => {
      const diffX = e.changedTouches[0].clientX - startX;
      if (Math.abs(diffX) > 40) {
        if (diffX < 0) goToSlide(currentIndex + 1);
        else goToSlide(currentIndex - 1);
      }
    }, { passive: true });

    let timer = setInterval(() => goToSlide(currentIndex + 1), 5000);
    wrapper.addEventListener('mouseenter', () => clearInterval(timer));
    wrapper.addEventListener('mouseleave', () => {
      clearInterval(timer);
      timer = setInterval(() => goToSlide(currentIndex + 1), 5000);
    });
  });

  // Visitor Light/Dark Theme Switcher
  const themeToggle = document.getElementById('themeToggle');
  if (themeToggle) {
    const savedTheme = localStorage.getItem('site_visitor_theme');
    if (savedTheme === 'light') {
      document.body.classList.add('light-theme');
      const icon = themeToggle.querySelector('.theme-icon');
      if (icon) icon.textContent = '☀️';
    }
    themeToggle.addEventListener('click', () => {
      const isLight = document.body.classList.toggle('light-theme');
      localStorage.setItem('site_visitor_theme', isLight ? 'light' : 'dark');
      const icon = themeToggle.querySelector('.theme-icon');
      if (icon) icon.textContent = isLight ? '☀️' : '🌓';
    });
  }

  // Functional Contact Form Webhook Submission & Mailto Fallback
  const contactForm = document.getElementById('contactForm');
  if (contactForm) {
    contactForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const endpoint = (contactForm.getAttribute('data-endpoint') || '').trim();
      const fallbackEmail = (contactForm.getAttribute('data-email') || 'contact@example.com').trim();
      const name = (document.getElementById('contactName') ? document.getElementById('contactName').value : '').trim();
      const email = (document.getElementById('contactEmail') ? document.getElementById('contactEmail').value : '').trim();
      const msg = (document.getElementById('contactMessage') ? document.getElementById('contactMessage').value : '').trim();
      const submitBtn = contactForm.querySelector('button[type="submit"]');

      if (endpoint && (endpoint.startsWith('http://') || endpoint.startsWith('https://'))) {
        if (submitBtn) { submitBtn.disabled = true; submitBtn.textContent = 'Sending...'; }
        try {
          const formData = new FormData(contactForm);
          const response = await fetch(endpoint, {
            method: 'POST',
            body: formData,
            headers: { 'Accept': 'application/json' }
          });
          if (response.ok) {
            showFormFeedback(contactForm, 'success', 'Message sent successfully! We will get back to you shortly.');
            contactForm.reset();
          } else {
            showFormFeedback(contactForm, 'info', 'Message recorded. Opening email client for direct confirmation...');
            window.location.href = 'mailto:' + fallbackEmail + '?subject=' + encodeURIComponent('Inquiry from ' + name) + '&body=' + encodeURIComponent(msg + '\n\nFrom: ' + name + ' (' + email + ')');
          }
        } catch (err) {
          showFormFeedback(contactForm, 'info', 'Opening email client to deliver your message directly...');
          window.location.href = 'mailto:' + fallbackEmail + '?subject=' + encodeURIComponent('Inquiry from ' + name) + '&body=' + encodeURIComponent(msg + '\n\nFrom: ' + name + ' (' + email + ')');
        } finally {
          if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = 'Submit Message'; }
        }
      } else {
        showFormFeedback(contactForm, 'success', 'Opening mail client to complete dispatch...');
        window.location.href = 'mailto:' + fallbackEmail + '?subject=' + encodeURIComponent('Inquiry from ' + name) + '&body=' + encodeURIComponent(msg + '\n\nFrom: ' + name + ' (' + email + ')');
        contactForm.reset();
      }
    });
  }

  // Smooth Scroll-Triggered Entrance Animations
  const animTargets = document.querySelectorAll('[data-animate], section:not(.navbar), .feature-card, .service-card, .pricing-card, .gallery-card, .team-card, .faq-item, .stat-item');
  if ('IntersectionObserver' in window) {
    const sectionObserver = new IntersectionObserver((entries, observer) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('in-view');
          entry.target.classList.add('is-inview');
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.1, rootMargin: '0px 0px -40px 0px' });

    animTargets.forEach(el => {
      if (!el.hasAttribute('data-animate')) el.classList.add('anim-fade-up');
      sectionObserver.observe(el);
    });
  } else {
    animTargets.forEach(el => {
      el.classList.add('in-view');
      el.classList.add('is-inview');
    });
  }
});

function showFormFeedback(formElement, type, message) {
  let alertEl = formElement.querySelector('.form-alert');
  if (!alertEl) {
    alertEl = document.createElement('div');
    formElement.prepend(alertEl);
  }
  alertEl.className = 'form-alert ' + type;
  alertEl.textContent = message;
  setTimeout(() => {
    alertEl.classList.add('fade-out');
    setTimeout(() => alertEl.remove(), 400);
  }, 4000);
}
""".trimIndent()
    }

    private fun buildManifest(website: WebsiteEntity, theme: ThemeVars): String {
        return """
{
  "short_name": "${escapeJson(website.title)}",
  "name": "${escapeJson(website.title)} - Web Builder",
  "icons": [
    {
      "src": "favicon.ico",
      "sizes": "64x64 32x32 24x24 16x16",
      "type": "image/x-icon"
    }
  ],
  "start_url": ".",
  "display": "standalone",
  "theme_color": "${theme.bgPrimary}",
  "background_color": "${theme.bgPrimary}"
}
""".trimIndent()
    }

    private fun buildReadme(website: WebsiteEntity): String {
        return """
# ${website.title}

Production website package generated with **Open-Source Mobile Web Builder**.

## Structure
- `index.html` — Semantic HTML structure with responsive design.
- `styles.css` — High-performance CSS using modern variables and responsive layouts.
- `main.js` — Interactive web engine with mobile navigation drawer, interactive pricing toggles, and form handling.
- `manifest.json` — Progressive Web App (PWA) manifest configuration.

## Deploying to Netlify
1. Log into your Netlify dashboard.
2. Drag and drop this folder (or zip) into the **Sites** area.
3. Your site is immediately live with SSL and CDN edge caching!

## Deploying to Vercel or GitHub Pages
1. Push this directory to a GitHub repository.
2. Link the repository in your Vercel or GitHub Pages dashboard.
3. Set the root directory as source.
""".trimIndent()
    }

    private fun getThemeVariables(preset: String): ThemeVars {
        return when (preset.lowercase()) {
            "clean-light" -> ThemeVars(
                bgPrimary = "#FFFFFF",
                bgSecondary = "#F8FAFC",
                bgCard = "#FFFFFF",
                textPrimary = "#0F172A",
                textSecondary = "#475569",
                accent = "#4F46E5",
                accentHover = "#4338CA",
                accentRgb = "79, 70, 229",
                border = "#E2E8F0",
                glow = "rgba(79, 70, 229, 0.18)"
            )
            "minimal-light" -> ThemeVars(
                bgPrimary = "#FAFAFA",
                bgSecondary = "#F4F4F5",
                bgCard = "#FFFFFF",
                textPrimary = "#18181B",
                textSecondary = "#52525B",
                accent = "#2563EB",
                accentHover = "#1D4ED8",
                accentRgb = "37, 99, 235",
                border = "#E4E4E7",
                glow = "rgba(37, 99, 235, 0.16)"
            )
            "sunset-warm" -> ThemeVars(
                bgPrimary = "#FDFBF7",
                bgSecondary = "#F7F2E9",
                bgCard = "#FFFFFF",
                textPrimary = "#29150B",
                textSecondary = "#78350F",
                accent = "#D97706",
                accentHover = "#B45309",
                accentRgb = "217, 119, 6",
                border = "#EBDDCB",
                glow = "rgba(217, 119, 6, 0.2)"
            )
            "cyber-neon" -> ThemeVars(
                bgPrimary = "#050711",
                bgSecondary = "#0B0E1B",
                bgCard = "#11162B",
                textPrimary = "#F8FAFC",
                textSecondary = "#94A3B8",
                accent = "#A855F7",
                accentHover = "#9333EA",
                accentRgb = "168, 85, 247",
                border = "#242A4A",
                glow = "rgba(168, 85, 247, 0.35)"
            )
            "emerald-minimal" -> ThemeVars(
                bgPrimary = "#050C08",
                bgSecondary = "#0A1710",
                bgCard = "#0F2218",
                textPrimary = "#F0FDF4",
                textSecondary = "#86EFAC",
                accent = "#10B981",
                accentHover = "#059669",
                accentRgb = "16, 185, 129",
                border = "#133E2B",
                glow = "rgba(16, 185, 129, 0.28)"
            )
            "luxury-gold" -> ThemeVars(
                bgPrimary = "#0B0B0E",
                bgSecondary = "#121217",
                bgCard = "#181820",
                textPrimary = "#FDFDFD",
                textSecondary = "#D4AF37",
                accent = "#D4AF37",
                accentHover = "#C59B27",
                accentRgb = "212, 175, 55",
                border = "#2D2B22",
                glow = "rgba(212, 175, 55, 0.32)"
            )
            "pastel-candy" -> ThemeVars(
                bgPrimary = "#FFF9FB",
                bgSecondary = "#FDF2F6",
                bgCard = "#FFFFFF",
                textPrimary = "#1F2937",
                textSecondary = "#6B7280",
                accent = "#EC4899",
                accentHover = "#DB2777",
                accentRgb = "236, 72, 153",
                border = "#FCE7F3",
                glow = "rgba(236, 72, 153, 0.22)"
            )
            "crimson-energy" -> ThemeVars(
                bgPrimary = "#0A0D14",
                bgSecondary = "#101420",
                bgCard = "#141A29",
                textPrimary = "#F8FAFC",
                textSecondary = "#94A3B8",
                accent = "#EF4444",
                accentHover = "#DC2626",
                accentRgb = "239, 68, 68",
                border = "#262E3E",
                glow = "rgba(239, 68, 68, 0.32)"
            )
            "ocean-cyan" -> ThemeVars(
                bgPrimary = "#030A14",
                bgSecondary = "#081424",
                bgCard = "#0D2038",
                textPrimary = "#F0FDF4",
                textSecondary = "#7DD3FC",
                accent = "#06B6D4",
                accentHover = "#0891B2",
                accentRgb = "6, 182, 212",
                border = "#15324E",
                glow = "rgba(6, 182, 212, 0.35)"
            )
            "retro-synth" -> ThemeVars(
                bgPrimary = "#12072B",
                bgSecondary = "#1A0B3D",
                bgCard = "#230F52",
                textPrimary = "#FDF4FF",
                textSecondary = "#C084FC",
                accent = "#F43F5E",
                accentHover = "#E11D48",
                accentRgb = "244, 63, 94",
                border = "#3D1A7A",
                glow = "rgba(244, 63, 94, 0.38)"
            )
            "anime-4bit" -> ThemeVars(
                bgPrimary = "#0F051D",
                bgSecondary = "#1B0A33",
                bgCard = "#260E4A",
                textPrimary = "#FEF08A",
                textSecondary = "#FF71CE",
                accent = "#FF2A85",
                accentHover = "#E0156D",
                accentRgb = "255, 42, 133",
                border = "#7E22CE",
                glow = "rgba(255, 42, 133, 0.45)"
            )
            "retro-arcade-4bit" -> ThemeVars(
                bgPrimary = "#050505",
                bgSecondary = "#101010",
                bgCard = "#181818",
                textPrimary = "#39FF14",
                textSecondary = "#00F0FF",
                accent = "#FF0055",
                accentHover = "#CC0044",
                accentRgb = "255, 0, 85",
                border = "#22C55E",
                glow = "rgba(57, 255, 20, 0.4)"
            )
            "gameboy-4bit" -> ThemeVars(
                bgPrimary = "#8B956D",
                bgSecondary = "#9BBC0F",
                bgCard = "#9BBC0F",
                textPrimary = "#0F380F",
                textSecondary = "#306230",
                accent = "#0F380F",
                accentHover = "#1F4A1F",
                accentRgb = "15, 56, 15",
                border = "#306230",
                glow = "rgba(15, 56, 15, 0.25)"
            )
            else -> ThemeVars( // modern-dark
                bgPrimary = "#090D16",
                bgSecondary = "#0F172A",
                bgCard = "#131C2E",
                textPrimary = "#F8FAFC",
                textSecondary = "#94A3B8",
                accent = "#6366F1",
                accentHover = "#4F46E5",
                accentRgb = "99, 102, 241",
                border = "#1E293B",
                glow = "rgba(99, 102, 241, 0.25)"
            )
        }
    }

    fun resolveThemeVariables(
        preset: String,
        customPrimaryHex: String = "",
        customBackgroundHex: String = ""
    ): ThemeVars {
        val base = getThemeVariables(preset)
        if (customPrimaryHex.isBlank() && customBackgroundHex.isBlank()) {
            return base
        }

        var accent = base.accent
        var accentHover = base.accentHover
        var accentRgb = base.accentRgb
        var glow = base.glow

        if (customPrimaryHex.isNotBlank() && customPrimaryHex.startsWith("#") && customPrimaryHex.length >= 7) {
            accent = customPrimaryHex
            val rgb = parseRgbFromHex(customPrimaryHex) ?: Triple(99, 102, 241)
            accentRgb = "${rgb.first}, ${rgb.second}, ${rgb.third}"
            accentHover = darkenHex(customPrimaryHex, 0.12f)
            glow = "rgba($accentRgb, 0.32)"
        }

        var bgPrimary = base.bgPrimary
        var bgSecondary = base.bgSecondary
        var bgCard = base.bgCard
        var border = base.border
        var textPrimary = base.textPrimary
        var textSecondary = base.textSecondary

        if (customBackgroundHex.isNotBlank() && customBackgroundHex.startsWith("#") && customBackgroundHex.length >= 7) {
            bgPrimary = customBackgroundHex
            val isDark = isDarkHex(customBackgroundHex)
            if (isDark) {
                bgSecondary = lightenHex(customBackgroundHex, 0.08f)
                bgCard = lightenHex(customBackgroundHex, 0.14f)
                border = lightenHex(customBackgroundHex, 0.22f)
                textPrimary = "#F8FAFC"
                textSecondary = "#94A3B8"
            } else {
                bgSecondary = darkenHex(customBackgroundHex, 0.04f)
                bgCard = "#FFFFFF"
                border = darkenHex(customBackgroundHex, 0.14f)
                textPrimary = "#0F172A"
                textSecondary = "#475569"
            }
        }

        return ThemeVars(
            bgPrimary = bgPrimary,
            bgSecondary = bgSecondary,
            bgCard = bgCard,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            accent = accent,
            accentHover = accentHover,
            accentRgb = accentRgb,
            border = border,
            glow = glow
        )
    }

    private fun parseRgbFromHex(hex: String): Triple<Int, Int, Int>? {
        return try {
            val clean = hex.removePrefix("#").trim()
            if (clean.length == 6) {
                val r = clean.substring(0, 2).toInt(16)
                val g = clean.substring(2, 4).toInt(16)
                val b = clean.substring(4, 6).toInt(16)
                Triple(r, g, b)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun isDarkHex(hex: String): Boolean {
        val rgb = parseRgbFromHex(hex) ?: return true
        val luminance = (0.299 * rgb.first + 0.587 * rgb.second + 0.114 * rgb.third) / 255.0
        return luminance < 0.5
    }

    private fun darkenHex(hex: String, factor: Float): String {
        val rgb = parseRgbFromHex(hex) ?: return hex
        val mult = (1.0f - factor).coerceIn(0f, 1f)
        val r = (rgb.first * mult).toInt().coerceIn(0, 255)
        val g = (rgb.second * mult).toInt().coerceIn(0, 255)
        val b = (rgb.third * mult).toInt().coerceIn(0, 255)
        return String.format("#%02X%02X%02X", r, g, b)
    }

    private fun lightenHex(hex: String, factor: Float): String {
        val rgb = parseRgbFromHex(hex) ?: return hex
        val r = (rgb.first + (255 - rgb.first) * factor).toInt().coerceIn(0, 255)
        val g = (rgb.second + (255 - rgb.second) * factor).toInt().coerceIn(0, 255)
        val b = (rgb.third + (255 - rgb.third) * factor).toInt().coerceIn(0, 255)
        return String.format("#%02X%02X%02X", r, g, b)
    }

    private fun getFontImport(fontFamily: String): String {
        return when {
            fontFamily.contains("Playfair", ignoreCase = true) ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;1,400&display=swap\" rel=\"stylesheet\">"
            fontFamily.contains("JetBrains", ignoreCase = true) ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600;800&display=swap\" rel=\"stylesheet\">"
            fontFamily.contains("Jakarta", ignoreCase = true) ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap\" rel=\"stylesheet\">"
            fontFamily.contains("Outfit", ignoreCase = true) ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800&display=swap\" rel=\"stylesheet\">"
            fontFamily.contains("Space Grotesk", ignoreCase = true) ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&display=swap\" rel=\"stylesheet\">"
            fontFamily.contains("Poppins", ignoreCase = true) ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700;800&display=swap\" rel=\"stylesheet\">"
            fontFamily.contains("Fraunces", ignoreCase = true) ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&display=swap\" rel=\"stylesheet\">"
            fontFamily.contains("DM Sans", ignoreCase = true) ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;700&display=swap\" rel=\"stylesheet\">"
            fontFamily.contains("Syne", ignoreCase = true) ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=Syne:wght@500;700;800&display=swap\" rel=\"stylesheet\">"
            fontFamily.contains("Press Start", ignoreCase = true) ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap\" rel=\"stylesheet\">"
            fontFamily.contains("DotGothic", ignoreCase = true) ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=DotGothic16&display=swap\" rel=\"stylesheet\">"
            fontFamily.contains("Silkscreen", ignoreCase = true) ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=Silkscreen:wght@400;700&display=swap\" rel=\"stylesheet\">"
            fontFamily.contains("VT323", ignoreCase = true) ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=VT323&display=swap\" rel=\"stylesheet\">"
            fontFamily.contains("Pixelify", ignoreCase = true) ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=Pixelify+Sans:wght@400;600;700&display=swap\" rel=\"stylesheet\">"
            fontFamily.contains("Georgia", ignoreCase = true) -> ""
            else ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap\" rel=\"stylesheet\">"
        }
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private fun escapeJson(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
    }

    private fun compileWhatsAppShop(
        block: WebBlockEntity,
        blockIdAttr: String,
        styleAttr: String
    ): String {
        val title = escapeHtml(block.title.ifBlank { "Knot & Weave" })
        val promo = escapeHtml(block.subtitle.ifBlank { "Artisan Festival • Up to 50% off select handcrafted bags" })
        val waPhone = block.buttonUrl.filter { it.isDigit() }.ifBlank { "919876543210" }
        val d = "${'$'}"

        val rawProducts = block.content.split("|").map { it.trim() }.filter { it.isNotBlank() }
        val jsProductsStr = if (rawProducts.isNotEmpty()) {
            rawProducts.mapIndexed { idx, raw ->
                val parts = raw.split(":").map { it.trim() }
                val pName = escapeJson(parts.getOrNull(0) ?: "Artisan Product ${idx + 1}")
                val pPrice = parts.getOrNull(1)?.filter { it.isDigit() }?.toIntOrNull() ?: (999 + idx * 250)
                val pMrp = parts.getOrNull(2)?.filter { it.isDigit() }?.toIntOrNull() ?: (pPrice * 2)
                val pRating = escapeJson(parts.getOrNull(3) ?: "4.8")
                val pThumb = escapeJson(parts.getOrNull(5) ?: "https://images.unsplash.com/photo-1544816155-12df9643f363?w=500&q=80")
                val pCat = escapeJson(parts.getOrNull(6) ?: "All")
                val discount = if (pMrp > pPrice) "${((pMrp - pPrice) * 100) / pMrp}% OFF" else "BESTSELLER"
                """{
            id: 'p${idx + 1}',
            name: '$pName',
            category: '$pCat',
            price: $pPrice,
            mrp: $pMrp,
            rating: '$pRating ★',
            reviews: '${50 + idx * 17} reviews',
            thumb: '$pThumb',
            badge: '$discount'
          }"""
            }.joinToString(",\n          ")
        } else {
            """{
            id: 'p1',
            name: 'Lavender Breeze Handwoven Woolen Tote Bag (Pastel Purple)',
            category: 'Totes',
            price: 1249,
            mrp: 2499,
            rating: '4.9 ★',
            reviews: '89 reviews',
            thumb: 'https://images.unsplash.com/photo-1544816155-12df9643f363?w=500&q=80',
            badge: '50% OFF'
          },
          {
            id: 'p2',
            name: 'Crimson Night Hand-Crocheted Crossbody Bag',
            category: 'Crossbody',
            price: 899,
            mrp: 1599,
            rating: '4.5 ★',
            reviews: '124 reviews',
            thumb: 'https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=500&q=80',
            badge: '43% OFF'
          },
          {
            id: 'p3',
            name: 'Earthen Clay Terracotta Merino Wool Handbag',
            category: 'Handbags',
            price: 1499,
            mrp: 2999,
            rating: '4.8 ★',
            reviews: '62 reviews',
            thumb: 'https://images.unsplash.com/photo-1590874103328-eac38a683ce7?w=500&q=80',
            badge: '50% OFF'
          }"""
        }

        return """
  <section class="kw-shop-section"$blockIdAttr$styleAttr>
    <style>
      .kw-shop-section {
        padding: 24px 16px;
        background: #FDFBF7;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        color: #1E293B;
      }
      .kw-container {
        max-width: 720px;
        margin: 0 auto;
      }
      .kw-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 12px 16px;
        background: #FFFFFF;
        border-radius: 16px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.06);
        margin-bottom: 16px;
      }
      .kw-brand {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 1.15rem;
        font-weight: 800;
        color: #0F172A;
        letter-spacing: -0.02em;
      }
      .kw-brand-badge {
        background: #8B5CF6;
        color: white;
        font-size: 0.65rem;
        padding: 2px 6px;
        border-radius: 6px;
        font-weight: 700;
        text-transform: uppercase;
      }
      .kw-cart-btn {
        position: relative;
        background: #10B981;
        color: white;
        border: none;
        padding: 8px 14px;
        border-radius: 12px;
        font-weight: 700;
        font-size: 0.85rem;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 6px;
        transition: transform 0.15s ease;
      }
      .kw-cart-btn:active {
        transform: scale(0.96);
      }
      .kw-cart-badge {
        background: #DC2626;
        color: white;
        font-size: 0.7rem;
        font-weight: 800;
        min-width: 18px;
        height: 18px;
        border-radius: 9px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 0 4px;
      }
      .kw-search-box {
        margin-bottom: 14px;
      }
      .kw-search-input {
        width: 100%;
        padding: 10px 14px;
        border-radius: 12px;
        border: 1px solid #E2E8F0;
        background: white;
        font-size: 0.875rem;
        outline: none;
        box-sizing: border-box;
      }
      .kw-search-input:focus {
        border-color: #8B5CF6;
      }
      .kw-categories {
        display: flex;
        gap: 8px;
        overflow-x: auto;
        padding-bottom: 8px;
        margin-bottom: 14px;
        scrollbar-width: none;
      }
      .kw-categories::-webkit-scrollbar { display: none; }
      .kw-cat-chip {
        padding: 6px 14px;
        background: white;
        border: 1px solid #E2E8F0;
        border-radius: 20px;
        font-size: 0.8rem;
        font-weight: 600;
        white-space: nowrap;
        cursor: pointer;
        color: #475569;
        transition: all 0.2s ease;
      }
      .kw-cat-chip.active {
        background: #8B5CF6;
        color: white;
        border-color: #8B5CF6;
      }
      .kw-banner {
        background: linear-gradient(135deg, #8B5CF6 0%, #EC4899 100%);
        color: white;
        border-radius: 16px;
        padding: 18px 20px;
        margin-bottom: 20px;
        position: relative;
        overflow: hidden;
      }
      .kw-banner h3 {
        margin: 0 0 4px 0;
        font-size: 1.15rem;
        font-weight: 800;
      }
      .kw-banner p {
        margin: 0;
        font-size: 0.85rem;
        opacity: 0.95;
      }
      .kw-section-heading {
        font-size: 1.05rem;
        font-weight: 800;
        margin-bottom: 12px;
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
      .kw-products-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 14px;
        margin-bottom: 24px;
      }
      .kw-product-card {
        background: white;
        border-radius: 14px;
        overflow: hidden;
        border: 1px solid #E2E8F0;
        display: flex;
        flex-direction: column;
        transition: transform 0.2s ease, box-shadow 0.2s ease;
      }
      .kw-product-card:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0,0,0,0.08);
      }
      .kw-product-thumb-wrap {
        height: 140px;
        background: #F1F5F9;
        position: relative;
        overflow: hidden;
      }
      .kw-product-thumb {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      .kw-discount-pill {
        position: absolute;
        top: 8px;
        left: 8px;
        background: #EF4444;
        color: white;
        font-size: 0.65rem;
        font-weight: 800;
        padding: 2px 6px;
        border-radius: 4px;
      }
      .kw-wish-btn {
        position: absolute;
        top: 8px;
        right: 8px;
        width: 28px;
        height: 28px;
        border-radius: 14px;
        background: rgba(255,255,255,0.85);
        border: none;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.85rem;
      }
      .kw-product-body {
        padding: 12px;
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
      }
      .kw-product-title {
        font-size: 0.9rem;
        font-weight: 700;
        margin: 0 0 4px 0;
        line-height: 1.25;
        color: #0F172A;
      }
      .kw-product-rating {
        font-size: 0.75rem;
        color: #F59E0B;
        margin-bottom: 6px;
      }
      .kw-price-row {
        display: flex;
        align-items: baseline;
        gap: 6px;
        margin-bottom: 10px;
      }
      .kw-price-curr {
        font-size: 1.05rem;
        font-weight: 800;
        color: #0F172A;
      }
      .kw-price-mrp {
        font-size: 0.8rem;
        color: #94A3B8;
        text-decoration: line-through;
      }
      .kw-add-btn {
        background: #8B5CF6;
        color: white;
        border: none;
        padding: 8px 12px;
        border-radius: 8px;
        font-weight: 700;
        font-size: 0.8rem;
        cursor: pointer;
        width: 100%;
        transition: background 0.15s ease;
      }
      .kw-add-btn:hover {
        background: #7C3AED;
      }

      /* Photo Upload Custom Order Section */
      .kw-custom-upload-box {
        background: #FFFFFF;
        border: 2px dashed #8B5CF6;
        border-radius: 16px;
        padding: 20px;
        margin-bottom: 24px;
        text-align: center;
      }
      .kw-upload-title {
        font-size: 1.05rem;
        font-weight: 800;
        color: #0F172A;
        margin: 0 0 4px 0;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
      }
      .kw-upload-sub {
        font-size: 0.825rem;
        color: #64748B;
        margin: 0 0 14px 0;
      }
      .kw-upload-btn-trigger {
        background: #EDE9FE;
        color: #6D28D9;
        border: 1px solid #C4B5FD;
        padding: 10px 18px;
        border-radius: 10px;
        font-weight: 700;
        font-size: 0.85rem;
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 10px;
      }
      .kw-upload-preview-card {
        display: none;
        background: #F8FAFC;
        border-radius: 12px;
        padding: 10px;
        margin: 12px 0;
        border: 1px solid #E2E8F0;
        align-items: center;
        gap: 12px;
      }
      .kw-upload-thumb {
        width: 60px;
        height: 60px;
        border-radius: 8px;
        object-fit: cover;
      }
      .kw-upload-info {
        flex: 1;
        text-align: left;
      }
      .kw-upload-notes {
        width: 100%;
        padding: 8px 12px;
        border-radius: 8px;
        border: 1px solid #CBD5E1;
        font-size: 0.825rem;
        margin-top: 8px;
        box-sizing: border-box;
      }

      /* Cart Drawer */
      .kw-cart-overlay {
        display: none;
        position: fixed;
        inset: 0;
        background: rgba(0,0,0,0.5);
        z-index: 9999;
      }
      .kw-cart-drawer {
        position: fixed;
        top: 0;
        right: 0;
        bottom: 0;
        width: 100%;
        max-width: 400px;
        background: white;
        z-index: 10000;
        display: flex;
        flex-direction: column;
        box-shadow: -4px 0 20px rgba(0,0,0,0.15);
      }
      .kw-drawer-header {
        padding: 16px 20px;
        border-bottom: 1px solid #E2E8F0;
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
      .kw-drawer-body {
        padding: 16px 20px;
        flex: 1;
        overflow-y: auto;
      }
      .kw-drawer-footer {
        padding: 16px 20px;
        border-top: 1px solid #E2E8F0;
        background: #F8FAFC;
      }
      .kw-cart-item {
        display: flex;
        gap: 12px;
        padding: 10px 0;
        border-bottom: 1px solid #F1F5F9;
        align-items: center;
      }
      .kw-cart-thumb {
        width: 50px;
        height: 50px;
        border-radius: 8px;
        object-fit: cover;
        background: #E2E8F0;
      }
      .kw-qty-btn {
        width: 26px;
        height: 26px;
        border-radius: 6px;
        border: 1px solid #CBD5E1;
        background: white;
        font-weight: 700;
        cursor: pointer;
      }

      /* Multi-Step Checkout Modal */
      .kw-modal-overlay {
        display: none;
        position: fixed;
        inset: 0;
        background: rgba(0,0,0,0.6);
        z-index: 10001;
        align-items: center;
        justify-content: center;
        padding: 16px;
      }
      .kw-modal-content {
        background: white;
        border-radius: 20px;
        width: 100%;
        max-width: 480px;
        max-height: 90vh;
        overflow-y: auto;
        padding: 24px 20px;
        position: relative;
        box-sizing: border-box;
      }
      .kw-step-bar {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 20px;
        position: relative;
      }
      .kw-step-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        font-size: 0.75rem;
        font-weight: 700;
        color: #94A3B8;
        gap: 4px;
        z-index: 2;
      }
      .kw-step-item.active {
        color: #8B5CF6;
      }
      .kw-step-item.completed {
        color: #10B981;
      }
      .kw-step-circle {
        width: 28px;
        height: 28px;
        border-radius: 14px;
        background: #F1F5F9;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.8rem;
        font-weight: 800;
        color: inherit;
      }
      .kw-step-item.active .kw-step-circle {
        background: #8B5CF6;
        color: white;
      }
      .kw-step-item.completed .kw-step-circle {
        background: #10B981;
        color: white;
      }
      .kw-step-line {
        position: absolute;
        top: 14px;
        left: 30px;
        right: 30px;
        height: 2px;
        background: #E2E8F0;
        z-index: 1;
      }
      .kw-form-field {
        margin-bottom: 12px;
      }
      .kw-form-label {
        display: block;
        font-size: 0.785rem;
        font-weight: 700;
        color: #475569;
        margin-bottom: 4px;
      }
      .kw-form-input {
        width: 100%;
        padding: 10px 12px;
        border-radius: 10px;
        border: 1px solid #CBD5E1;
        font-size: 0.875rem;
        box-sizing: border-box;
      }
      .kw-pay-option {
        border: 1.5px solid #E2E8F0;
        border-radius: 12px;
        padding: 12px 14px;
        margin-bottom: 10px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        cursor: pointer;
        transition: all 0.15s ease;
      }
      .kw-pay-option.selected {
        border-color: #8B5CF6;
        background: #F5F3FF;
      }
      .kw-summary-card {
        background: #F8FAFC;
        border: 1px solid #E2E8F0;
        border-radius: 12px;
        padding: 12px 14px;
        margin-bottom: 14px;
        font-size: 0.85rem;
      }
      .kw-summary-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 6px;
      }
      .kw-summary-row.total {
        font-size: 1rem;
        font-weight: 800;
        border-top: 1px solid #E2E8F0;
        padding-top: 8px;
        margin-top: 8px;
        color: #0F172A;
      }
      .kw-wa-btn {
        background: #25D366;
        color: white;
        border: none;
        padding: 12px;
        border-radius: 12px;
        font-weight: 800;
        font-size: 0.95rem;
        width: 100%;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        margin-bottom: 8px;
        box-shadow: 0 4px 12px rgba(37,211,102,0.25);
      }
      .kw-toast {
        position: fixed;
        bottom: 24px;
        left: 50%;
        transform: translateX(-50%);
        background: #0F172A;
        color: white;
        padding: 10px 18px;
        border-radius: 24px;
        font-size: 0.85rem;
        font-weight: 600;
        z-index: 10005;
        display: none;
        box-shadow: 0 6px 20px rgba(0,0,0,0.2);
      }
    </style>

    <div class="kw-container">
      <!-- Store Header Bar -->
      <div class="kw-header">
        <div class="kw-brand">
          <span>🌸</span>
          <span>$title</span>
          <span class="kw-brand-badge">Assured</span>
        </div>
        <button class="kw-cart-btn" onclick="kwOpenCart()">
          <span>🛒 Cart</span>
          <span class="kw-cart-badge" id="kwCartBadge">1</span>
        </button>
      </div>

      <!-- Live Search Box -->
      <div class="kw-search-box">
        <input type="text" class="kw-search-input" id="kwSearchInput" placeholder="Search handcrafted bags (e.g. tote, crossbody)..." oninput="kwFilterProducts()">
      </div>

      <!-- Categories Filter -->
      <div class="kw-categories">
        <button class="kw-cat-chip active" onclick="kwFilterCategory('All', this)">All</button>
        <button class="kw-cat-chip" onclick="kwFilterCategory('Totes', this)">👜 Totes</button>
        <button class="kw-cat-chip" onclick="kwFilterCategory('Crossbody', this)">👛 Crossbody</button>
        <button class="kw-cat-chip" onclick="kwFilterCategory('Handbags', this)">💼 Handbags</button>
        <button class="kw-cat-chip" onclick="kwFilterCategory('Wallets', this)">👝 Wallets</button>
      </div>

      <!-- Promo Banner Carousel -->
      <div class="kw-banner"${if (block.imageUrl.isNotBlank()) " style=\"background: linear-gradient(rgba(15, 23, 42, 0.55), rgba(15, 23, 42, 0.75)), url('${escapeHtml(block.imageUrl)}') center/cover no-repeat;\"" else ""}>
        <h3>$promo</h3>
        <p>100% Pure Organic Merino Wool • Handcrafted by Master Rural Artisans</p>
      </div>

      <!-- Products Grid -->
      <div class="kw-section-heading">
        <span>Curated Artisan Collection</span>
        <span style="font-size: 0.8rem; color: #10B981; font-weight: 700;">⚡ Instant Delivery</span>
      </div>
      <div class="kw-products-grid" id="kwProductsGrid">
        <!-- Products populated by JS -->
      </div>

      <!-- Photo Upload Custom Order Card -->
      <div class="kw-custom-upload-box">
        <h4 class="kw-upload-title">
          <span>📸</span>
          <span>Custom Artisan Order & Photo Upload</span>
        </h4>
        <p class="kw-upload-sub">Have a dream bag design or personalized embroidery? Upload a reference photo, and our master artisans will weave it for you!</p>
        
        <input type="file" id="kwPhotoInput" accept="image/*" style="display:none;" onchange="kwHandlePhotoUpload(event)">
        <button class="kw-upload-btn-trigger" onclick="document.getElementById('kwPhotoInput').click()">
          <span>📷</span>
          <span>Choose / Take Reference Photo</span>
        </button>

        <div class="kw-upload-preview-card" id="kwUploadPreviewCard">
          <img class="kw-upload-thumb" id="kwUploadThumbImg" src="" alt="Custom Design">
          <div class="kw-upload-info">
            <div style="font-weight: 700; font-size: 0.85rem;" id="kwUploadFileName">custom_design.jpg</div>
            <div style="font-size: 0.75rem; color: #10B981;">✓ Photo ready to attach to WhatsApp order</div>
          </div>
          <button style="border:none; background:transparent; cursor:pointer; font-size:1.1rem;" onclick="kwRemovePhoto()">✕</button>
        </div>

        <textarea class="kw-upload-notes" id="kwCustomNotes" rows="2" placeholder="Add custom notes (e.g. Please embroider initial 'M' on the front pocket with golden thread)..."></textarea>
        <button class="kw-add-btn" style="margin-top: 10px; background: #10B981;" onclick="kwAddCustomOrderToCart()">
          + Add Custom Piece to Order (₹1,399)
        </button>
      </div>
    </div>

    <!-- Cart Drawer -->
    <div class="kw-cart-overlay" id="kwCartOverlay" onclick="kwCloseCart()"></div>
    <div class="kw-cart-drawer" id="kwCartDrawer" style="display:none;">
      <div class="kw-drawer-header">
        <div style="font-weight: 800; font-size: 1.1rem;">Your Shopping Bag</div>
        <button style="background:none; border:none; font-size: 1.3rem; cursor:pointer;" onclick="kwCloseCart()">✕</button>
      </div>
      <div class="kw-drawer-body" id="kwCartItemsList">
        <!-- Cart Items -->
      </div>
      <div class="kw-drawer-footer">
        <div style="display:flex; justify-content:space-between; margin-bottom: 12px; font-weight:800; font-size:1rem;">
          <span>Subtotal</span>
          <span id="kwCartSubtotal">₹1,249</span>
        </div>
        <button class="kw-add-btn" style="padding: 12px; font-size: 0.95rem; background: #8B5CF6;" onclick="kwOpenCheckout()">
          Proceed to 3-Step Checkout →
        </button>
      </div>
    </div>

    <!-- Multi-Step Checkout Modal -->
    <div class="kw-modal-overlay" id="kwCheckoutModal">
      <div class="kw-modal-content">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 16px;">
          <h3 style="margin:0; font-size: 1.15rem; font-weight:800;">Fast Checkout</h3>
          <button style="background:none; border:none; font-size:1.2rem; cursor:pointer;" onclick="kwCloseCheckout()">✕</button>
        </div>

        <!-- Step Indicator -->
        <div class="kw-step-bar">
          <div class="kw-step-line"></div>
          <div class="kw-step-item active" id="kwStepPill1">
            <div class="kw-step-circle">1</div>
            <span>Address</span>
          </div>
          <div class="kw-step-item" id="kwStepPill2">
            <div class="kw-step-circle">2</div>
            <span>Payment</span>
          </div>
          <div class="kw-step-item" id="kwStepPill3">
            <div class="kw-step-circle">3</div>
            <span>Confirm</span>
          </div>
        </div>

        <!-- STEP 1: Address Form -->
        <div id="kwStep1">
          <div class="kw-form-field">
            <label class="kw-form-label">Full Name *</label>
            <input type="text" class="kw-form-input" id="kwInputName" placeholder="e.g. Priya Sharma" value="Priya Sharma">
          </div>
          <div class="kw-form-field">
            <label class="kw-form-label">WhatsApp Mobile Number *</label>
            <input type="tel" class="kw-form-input" id="kwInputPhone" placeholder="10-digit mobile number" value="9876543210">
          </div>
          <div class="kw-form-field">
            <label class="kw-form-label">Address (House No., Building, Street) *</label>
            <input type="text" class="kw-form-input" id="kwInputAddress" placeholder="e.g. Flat 402, Lotus Heights, Park Road" value="Flat 402, Lotus Heights, Park Road">
          </div>
          <div style="display:grid; grid-template-columns: 1fr 1fr; gap: 10px;">
            <div class="kw-form-field">
              <label class="kw-form-label">City *</label>
              <input type="text" class="kw-form-input" id="kwInputCity" placeholder="e.g. Mumbai" value="Mumbai">
            </div>
            <div class="kw-form-field">
              <label class="kw-form-label">Pincode *</label>
              <input type="text" class="kw-form-input" id="kwInputPin" placeholder="6-digit PIN" value="400001">
            </div>
          </div>
          <div style="display:flex; align-items:center; gap: 8px; margin-bottom: 16px;">
            <input type="checkbox" id="kwSaveAddr" checked>
            <label for="kwSaveAddr" style="font-size: 0.8rem; color: #475569;">Save delivery address for future orders</label>
          </div>
          <button class="kw-add-btn" style="padding: 12px; font-size: 0.95rem; background: #8B5CF6;" onclick="kwGoToStep(2)">
            Continue to Payment →
          </button>
        </div>

        <!-- STEP 2: Payment Options -->
        <div id="kwStep2" style="display:none;">
          <p style="font-size: 0.85rem; color:#64748B; margin: 0 0 12px 0;">Select your preferred payment method:</p>
          
          <div class="kw-pay-option selected" id="kwPayUpi" onclick="kwSelectPayment('upi')">
            <div>
              <div style="font-weight: 700; font-size: 0.9rem;">⚡ UPI (PhonePe / GPay / Paytm)</div>
              <div style="font-size: 0.75rem; color: #10B981;">Extra ₹10 instant discount applied!</div>
            </div>
            <span style="background: #D1FAE5; color:#065F46; font-size: 0.7rem; font-weight:800; padding:2px 6px; border-radius:4px;">₹10 OFF</span>
          </div>

          <div class="kw-pay-option" id="kwPayCod" onclick="kwSelectPayment('cod')">
            <div>
              <div style="font-weight: 700; font-size: 0.9rem;">💵 Cash on Delivery</div>
              <div style="font-size: 0.75rem; color: #64748B;">Pay with cash when your bag arrives</div>
            </div>
            <span style="font-size: 0.75rem; color: #64748B;">Standard</span>
          </div>

          <div style="display:flex; gap:10px; margin-top: 18px;">
            <button class="kw-add-btn" style="background:#E2E8F0; color:#334155; flex:1;" onclick="kwGoToStep(1)">
              ← Back
            </button>
            <button class="kw-add-btn" style="background:#8B5CF6; flex:2;" onclick="kwGoToStep(3)">
              Continue to Order Summary →
            </button>
          </div>
        </div>

        <!-- STEP 3: Order Summary & WhatsApp Dispatch -->
        <div id="kwStep3" style="display:none;">
          <div class="kw-summary-card">
            <div style="font-weight:800; margin-bottom: 8px; font-size:0.9rem;">Delivering To:</div>
            <div id="kwConfirmAddress" style="color: #475569; line-height: 1.35; font-size:0.8rem;"></div>
          </div>

          <div class="kw-summary-card">
            <div style="font-weight:800; margin-bottom: 8px; font-size:0.9rem;">Payment & Pricing Breakdown:</div>
            <div class="kw-summary-row">
              <span>Items Total (<span id="kwConfirmItemCount">1</span> item)</span>
              <span id="kwConfirmSubtotal">₹1,249</span>
            </div>
            <div class="kw-summary-row" id="kwConfirmUpiDiscountRow" style="color: #10B981;">
              <span>⚡ UPI Payment Discount</span>
              <span>-₹10</span>
            </div>
            <div class="kw-summary-row">
              <span>Express Delivery</span>
              <span style="color:#10B981; font-weight:700;">FREE</span>
            </div>
            <div class="kw-summary-row total">
              <span>Grand Total</span>
              <span id="kwConfirmGrandTotal" style="color:#8B5CF6;">₹1,239</span>
            </div>
          </div>

          <div id="kwConfirmPhotoNotice" style="display:none; background:#FEF3C7; border:1px solid #FDE68A; padding:8px 12px; border-radius:8px; font-size:0.75rem; color:#92400E; margin-bottom:12px;">
            📸 <strong>Custom Photo Attached:</strong> Reference image will be dispatched in WhatsApp!
          </div>

          <button class="kw-wa-btn" onclick="kwSendWhatsAppOrder()">
            <span>📲 Send Order to WhatsApp</span>
          </button>
          
          <button class="kw-add-btn" style="background: #0F172A; padding: 10px; font-size: 0.85rem;" onclick="kwPlaceDirectOrder()">
            Place Order (Instant Confirmation)
          </button>

          <button style="background:none; border:none; width:100%; text-align:center; color:#64748B; font-size:0.8rem; margin-top:10px; cursor:pointer;" onclick="kwGoToStep(2)">
            ← Change Payment Method
          </button>
        </div>
      </div>
    </div>

    <!-- Order Placed Success Modal -->
    <div class="kw-modal-overlay" id="kwSuccessModal">
      <div class="kw-modal-content" style="text-align:center; padding: 32px 20px;">
        <div style="font-size: 3rem; margin-bottom: 10px;">🎉</div>
        <h3 style="margin: 0 0 6px 0; font-size: 1.3rem; font-weight: 800; color: #0F172A;">Order Confirmed!</h3>
        <p style="font-size: 0.875rem; color: #64748B; margin: 0 0 16px 0;">Order ID: <strong id="kwSuccessOrderId">#KW84920</strong></p>
        <p style="font-size: 0.85rem; color: #334155; line-height: 1.4; margin-bottom: 20px;">
          Thank you for choosing Knot & Weave! Our master artisans will weave and hand-package your order with love.
        </p>
        <button class="kw-add-btn" style="background: #10B981; padding: 12px;" onclick="kwCloseSuccessModal()">
          Continue Shopping
        </button>
      </div>
    </div>

    <!-- Floating Toast Notification -->
    <div class="kw-toast" id="kwToast">Added to Bag!</div>

    <script>
      (function() {
        var products = [
          $jsProductsStr
        ];

        var cart = [
          { product: products[0], qty: 1 }
        ];

        var customPhotoData = null;
        var selectedPayment = 'upi';
        var merchantPhone = '$waPhone';

        window.kwRenderProducts = function(filterCat, searchKeyword) {
          var grid = document.getElementById('kwProductsGrid');
          if (!grid) return;
          grid.innerHTML = '';

          var filtered = products.filter(function(p) {
            var matchCat = !filterCat || filterCat === 'All' || p.category === filterCat;
            var matchSearch = !searchKeyword || p.name.toLowerCase().indexOf(searchKeyword.toLowerCase()) !== -1;
            return matchCat && matchSearch;
          });

          if (filtered.length === 0) {
            grid.innerHTML = '<div style="grid-column: 1/-1; text-align:center; padding: 24px; color:#94A3B8;">No handcrafted bags found matching your search.</div>';
            return;
          }

          filtered.forEach(function(p) {
            var card = document.createElement('div');
            card.className = 'kw-product-card';
            card.innerHTML = 
              '<div class="kw-product-thumb-wrap">' +
                '<img class="kw-product-thumb" src="' + p.thumb + '" alt="' + p.name + '">' +
                '<span class="kw-discount-pill">' + p.badge + '</span>' +
                '<button class="kw-wish-btn" onclick="kwToggleWish(this)">♥</button>' +
              '</div>' +
              '<div class="kw-product-body">' +
                '<h4 class="kw-product-title">' + p.name + '</h4>' +
                '<div class="kw-product-rating">' + p.rating + ' (' + p.reviews + ')</div>' +
                '<div class="kw-price-row">' +
                  '<span class="kw-price-curr">₹' + p.price + '</span>' +
                  '<span class="kw-price-mrp">₹' + p.mrp + '</span>' +
                '</div>' +
                '<button class="kw-add-btn" onclick="kwAddToCart(\'' + p.id + '\')">Add to Cart</button>' +
              '</div>';
            grid.appendChild(card);
          });
        };

        window.kwFilterCategory = function(cat, btn) {
          document.querySelectorAll('.kw-cat-chip').forEach(function(c) { c.classList.remove('active'); });
          if (btn) btn.classList.add('active');
          kwRenderProducts(cat, document.getElementById('kwSearchInput').value);
        };

        window.kwFilterProducts = function() {
          var activeCat = document.querySelector('.kw-cat-chip.active');
          var cat = activeCat ? activeCat.innerText.replace(/[^a-zA-Z]/g, '') : 'All';
          kwRenderProducts(cat, document.getElementById('kwSearchInput').value);
        };

        window.kwToggleWish = function(btn) {
          if (btn.style.color === 'rgb(239, 68, 68)') {
            btn.style.color = '';
            kwShowToast('Removed from wishlist');
          } else {
            btn.style.color = '#EF4444';
            kwShowToast('Added to wishlist ❤️');
          }
        };

        window.kwAddToCart = function(productId) {
          var prod = products.find(function(p) { return p.id === productId; });
          if (!prod) return;
          var existing = cart.find(function(item) { return item.product.id === productId; });
          if (existing) {
            existing.qty++;
          } else {
            cart.push({ product: prod, qty: 1 });
          }
          kwUpdateCartUI();
          kwShowToast('Added ' + prod.name.slice(0, 20) + '... to Bag!');
        };

        window.kwUpdateCartUI = function() {
          var count = cart.reduce(function(acc, i) { return acc + i.qty; }, 0);
          var badge = document.getElementById('kwCartBadge');
          if (badge) badge.innerText = count;

          var list = document.getElementById('kwCartItemsList');
          if (!list) return;
          list.innerHTML = '';

          var subtotal = 0;
          cart.forEach(function(item, idx) {
            var itemTotal = item.product.price * item.qty;
            subtotal += itemTotal;
            var div = document.createElement('div');
            div.className = 'kw-cart-item';
            div.innerHTML =
              '<img class="kw-cart-thumb" src="' + item.product.thumb + '">' +
              '<div style="flex:1;">' +
                '<div style="font-weight:700; font-size:0.85rem; line-height:1.2; margin-bottom:4px;">' + item.product.name.slice(0, 30) + '...</div>' +
                '<div style="font-size:0.8rem; font-weight:800; color:#8B5CF6;">₹' + item.product.price + '</div>' +
              '</div>' +
              '<div style="display:flex; align-items:center; gap:6px;">' +
                '<button class="kw-qty-btn" onclick="kwChangeQty(' + idx + ', -1)">-</button>' +
                '<span style="font-size:0.85rem; font-weight:700;">' + item.qty + '</span>' +
                '<button class="kw-qty-btn" onclick="kwChangeQty(' + idx + ', 1)">+</button>' +
              '</div>';
            list.appendChild(div);
          });

          var subEl = document.getElementById('kwCartSubtotal');
          if (subEl) subEl.innerText = '₹' + subtotal;
        };

        window.kwChangeQty = function(idx, delta) {
          if (!cart[idx]) return;
          cart[idx].qty += delta;
          if (cart[idx].qty <= 0) {
            cart.splice(idx, 1);
          }
          kwUpdateCartUI();
        };

        window.kwOpenCart = function() {
          document.getElementById('kwCartOverlay').style.display = 'block';
          document.getElementById('kwCartDrawer').style.display = 'flex';
          kwUpdateCartUI();
        };

        window.kwCloseCart = function() {
          document.getElementById('kwCartOverlay').style.display = 'none';
          document.getElementById('kwCartDrawer').style.display = 'none';
        };

        // Photo Upload Handler
        window.kwHandlePhotoUpload = function(e) {
          var file = e.target.files && e.target.files[0];
          if (!file) return;
          var reader = new FileReader();
          reader.onload = function(evt) {
            customPhotoData = evt.target.result;
            var preview = document.getElementById('kwUploadPreviewCard');
            var thumb = document.getElementById('kwUploadThumbImg');
            var name = document.getElementById('kwUploadFileName');
            if (thumb) thumb.src = customPhotoData;
            if (name) name.innerText = file.name;
            if (preview) preview.style.display = 'flex';
            kwShowToast('📸 Photo uploaded & attached successfully!');
          };
          reader.readAsDataURL(file);
        };

        window.kwRemovePhoto = function() {
          customPhotoData = null;
          var preview = document.getElementById('kwUploadPreviewCard');
          if (preview) preview.style.display = 'none';
          document.getElementById('kwPhotoInput').value = '';
          kwShowToast('Photo removed');
        };

        window.kwAddCustomOrderToCart = function() {
          var notes = document.getElementById('kwCustomNotes').value.trim();
          var customItem = {
            id: 'custom_' + Date.now(),
            name: 'Custom Handwoven Artisan Piece ' + (notes ? '(' + notes.slice(0, 20) + '...)' : ''),
            category: 'Custom',
            price: 1399,
            mrp: 2799,
            rating: '5.0 ★',
            reviews: 'Customized',
            thumb: customPhotoData || 'https://images.unsplash.com/photo-1544816155-12df9643f363?w=500&q=80',
            badge: 'BESPOKE'
          };
          cart.push({ product: customItem, qty: 1 });
          kwUpdateCartUI();
          kwShowToast('Custom piece added to your cart!');
          kwOpenCart();
        };

        // Multi-Step Checkout
        window.kwOpenCheckout = function() {
          kwCloseCart();
          document.getElementById('kwCheckoutModal').style.display = 'flex';
          kwGoToStep(1);
        };

        window.kwCloseCheckout = function() {
          document.getElementById('kwCheckoutModal').style.display = 'none';
        };

        window.kwGoToStep = function(step) {
          if (step === 2) {
            var name = document.getElementById('kwInputName').value.trim();
            var phone = document.getElementById('kwInputPhone').value.trim();
            var addr = document.getElementById('kwInputAddress').value.trim();
            var pin = document.getElementById('kwInputPin').value.trim();
            if (!name || !phone || !addr || !pin) {
              kwShowToast('Please fill out all address fields');
              return;
            }
          }

          document.getElementById('kwStep1').style.display = (step === 1) ? 'block' : 'none';
          document.getElementById('kwStep2').style.display = (step === 2) ? 'block' : 'none';
          document.getElementById('kwStep3').style.display = (step === 3) ? 'block' : 'none';

          for (var i = 1; i <= 3; i++) {
            var pill = document.getElementById('kwStepPill' + i);
            if (!pill) continue;
            pill.className = 'kw-step-item';
            if (i === step) pill.classList.add('active');
            else if (i < step) pill.classList.add('completed');
          }

          if (step === 3) {
            var nameVal = document.getElementById('kwInputName').value.trim();
            var phoneVal = document.getElementById('kwInputPhone').value.trim();
            var addrVal = document.getElementById('kwInputAddress').value.trim();
            var cityVal = document.getElementById('kwInputCity').value.trim();
            var pinVal = document.getElementById('kwInputPin').value.trim();
            
            document.getElementById('kwConfirmAddress').innerHTML = 
              '<strong>' + nameVal + '</strong> (' + phoneVal + ')<br>' +
              addrVal + ', ' + cityVal + ' - ' + pinVal;

            var subtotal = cart.reduce(function(acc, i) { return acc + (i.product.price * i.qty); }, 0);
            var discount = (selectedPayment === 'upi') ? 10 : 0;
            var grandTotal = Math.max(0, subtotal - discount);

            document.getElementById('kwConfirmItemCount').innerText = cart.reduce(function(acc, i) { return acc + i.qty; }, 0);
            document.getElementById('kwConfirmSubtotal').innerText = '₹' + subtotal;
            document.getElementById('kwConfirmUpiDiscountRow').style.display = (selectedPayment === 'upi') ? 'flex' : 'none';
            document.getElementById('kwConfirmGrandTotal').innerText = '₹' + grandTotal;

            var photoNotice = document.getElementById('kwConfirmPhotoNotice');
            if (photoNotice) photoNotice.style.display = customPhotoData ? 'block' : 'none';
          }
        };

        window.kwSelectPayment = function(type) {
          selectedPayment = type;
          document.getElementById('kwPayUpi').className = (type === 'upi') ? 'kw-pay-option selected' : 'kw-pay-option';
          document.getElementById('kwPayCod').className = (type === 'cod') ? 'kw-pay-option selected' : 'kw-pay-option';
        };

        window.kwSendWhatsAppOrder = function() {
          var name = document.getElementById('kwInputName').value.trim();
          var phone = document.getElementById('kwInputPhone').value.trim();
          var addr = document.getElementById('kwInputAddress').value.trim();
          var city = document.getElementById('kwInputCity').value.trim();
          var pin = document.getElementById('kwInputPin').value.trim();
          var orderId = 'KW' + Math.floor(10000 + Math.random() * 90000);

          var subtotal = cart.reduce(function(acc, i) { return acc + (i.product.price * i.qty); }, 0);
          var discount = (selectedPayment === 'upi') ? 10 : 0;
          var total = Math.max(0, subtotal - discount);

          var itemLines = cart.map(function(i) {
            return '• ' + i.product.name + ' (Qty: ' + i.qty + ') - ₹' + (i.product.price * i.qty);
          }).join('\n');

          var msg = '🛍️ *NEW ORDER: #' + orderId + '*\n' +
                    '*Store:* $title\n\n' +
                    '👤 *Customer:* ' + name + '\n' +
                    '📞 *Phone:* ' + phone + '\n' +
                    '📍 *Delivery Address:* ' + addr + ', ' + city + ' - ' + pin + '\n\n' +
                    '📦 *Items:*\n' + itemLines + '\n\n' +
                    (customPhotoData ? '📸 *Custom Photo Reference:* Attached by customer (Ready to send in chat)\n' : '') +
                    '💳 *Payment Method:* ' + (selectedPayment === 'upi' ? 'UPI (₹10 Discount Applied)' : 'Cash on Delivery') + '\n' +
                    '🚚 *Delivery:* FREE\n' +
                    '💰 *Total Amount:* ₹' + total + '\n\n' +
                    'Please confirm my order!';

          var url = 'https://wa.me/' + merchantPhone + '?text=' + encodeURIComponent(msg);
          window.open(url, '_blank');

          kwCloseCheckout();
          document.getElementById('kwSuccessOrderId').innerText = '#' + orderId;
          document.getElementById('kwSuccessModal').style.display = 'flex';
          cart = [];
          kwUpdateCartUI();
        };

        window.kwPlaceDirectOrder = function() {
          var orderId = 'KW' + Math.floor(10000 + Math.random() * 90000);
          kwCloseCheckout();
          document.getElementById('kwSuccessOrderId').innerText = '#' + orderId;
          document.getElementById('kwSuccessModal').style.display = 'flex';
          cart = [];
          kwUpdateCartUI();
        };

        window.kwCloseSuccessModal = function() {
          document.getElementById('kwSuccessModal').style.display = 'none';
        };

        window.kwShowToast = function(msg) {
          var toast = document.getElementById('kwToast');
          if (!toast) return;
          toast.innerText = msg;
          toast.style.display = 'block';
          clearTimeout(window._kwToastTimer);
          window._kwToastTimer = setTimeout(function() {
            toast.style.display = 'none';
          }, 2400);
        };

        // Initial setup
        kwRenderProducts('All', '');
        kwUpdateCartUI();
      })();
    </script>
  </section>
"""
    }

    private fun compileMultiStepWizard(
        block: WebBlockEntity,
        blockIdAttr: String,
        styleAttr: String
    ): String {
        val title = escapeHtml(block.title.ifBlank { "Multi-Step Booking & Quote Wizard" })
        val subtitle = escapeHtml(block.subtitle.ifBlank { "Customize your options, attach reference photos, and confirm instantly via WhatsApp" })
        val waPhone = block.buttonUrl.filter { it.isDigit() }.ifBlank { "919876543210" }
        val btnLabel = escapeHtml(block.buttonText.ifBlank { "Send Order to WhatsApp" })
        val heroImg = block.imageUrl

        // Parse options from block.content: "Name: Price: Description: Icon | ..."
        val rawOptions = block.content.split("|").map { it.trim() }.filter { it.isNotBlank() }
        val parsedOptions = if (rawOptions.isNotEmpty()) {
            rawOptions.mapIndexed { idx, raw ->
                val parts = raw.split(":")
                val name = parts.getOrNull(0)?.trim() ?: "Option ${idx + 1}"
                val price = parts.getOrNull(1)?.trim() ?: "₹999"
                val desc = parts.getOrNull(2)?.trim() ?: "Bespoke custom specification tailored to your needs"
                val icon = parts.getOrNull(3)?.trim() ?: "✨"
                mapOf("id" to "opt_$idx", "name" to name, "price" to price, "desc" to desc, "icon" to icon)
            }
        } else {
            listOf(
                mapOf("id" to "opt_0", "name" to "Petite Studio Edition", "price" to "₹799", "desc" to "Handcrafted minimal edition ideal for personal celebrations or compact needs", "icon" to "🍰"),
                mapOf("id" to "opt_1", "name" to "Signature Celebration", "price" to "₹1,899", "desc" to "Our most popular bespoke tier with 2 custom finishings & handcrafted details", "icon" to "🎂"),
                mapOf("id" to "opt_2", "name" to "Grand Luxe Masterpiece", "price" to "₹3,499", "desc" to "Centerpiece tier with master artisan detailing, custom monogramming & rush priority", "icon" to "👑"),
                mapOf("id" to "opt_3", "name" to "Curated Tasting Box", "price" to "₹599", "desc" to "Assorted flight of 6 artisan selections with custom wax-sealed gift packaging", "icon" to "🧁")
            )
        }

        val jsonOptions = parsedOptions.joinToString(",") { opt ->
            """{"id":"${escapeJson(opt["id"]!!)}","name":"${escapeJson(opt["name"]!!)}","price":"${escapeJson(opt["price"]!!)}","desc":"${escapeJson(opt["desc"]!!)}","icon":"${escapeJson(opt["icon"]!!)}"}"""
        }

        val heroBannerHtml = if (heroImg.isNotBlank()) {
            """
      <div class="msw-hero-card" style="background: linear-gradient(rgba(15,23,42,0.65), rgba(15,23,42,0.85)), url('${escapeHtml(heroImg)}') center/cover no-repeat;">
        <span class="msw-hero-badge">✦ Interactive Custom Builder</span>
        <h2 class="msw-hero-title">$title</h2>
        <p class="msw-hero-desc">$subtitle</p>
      </div>
            """.trimIndent()
        } else {
            """
      <div class="msw-header">
        <span class="msw-badge">✦ Interactive Custom Builder</span>
        <h2 class="msw-title">$title</h2>
        <p class="msw-desc">$subtitle</p>
      </div>
            """.trimIndent()
        }

        return """
  <section class="msw-wizard-section"$blockIdAttr$styleAttr>
    <style>
      .msw-wizard-section {
        padding: 5rem 1rem;
        background: var(--bg-primary, #0F172A);
        color: var(--text-primary, #F8FAFC);
        font-family: inherit;
      }
      .msw-container {
        max-width: 860px;
        margin: 0 auto;
      }
      .msw-header {
        text-align: center;
        margin-bottom: 2.5rem;
      }
      .msw-badge {
        display: inline-block;
        padding: 6px 14px;
        background: rgba(99, 102, 241, 0.15);
        color: #818CF8;
        border: 1px solid rgba(99, 102, 241, 0.3);
        border-radius: 9999px;
        font-size: 0.8rem;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        margin-bottom: 0.75rem;
      }
      .msw-title {
        font-size: 2.25rem;
        font-weight: 800;
        letter-spacing: -0.02em;
        margin-bottom: 0.75rem;
      }
      .msw-desc {
        font-size: 1.05rem;
        color: var(--text-secondary, #94A3B8);
        max-width: 620px;
        margin: 0 auto;
      }
      .msw-hero-card {
        border-radius: 1.5rem;
        padding: 3rem 2rem;
        text-align: center;
        margin-bottom: 2.5rem;
        border: 1px solid rgba(255, 255, 255, 0.15);
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
      }
      .msw-hero-badge {
        display: inline-block;
        padding: 4px 12px;
        background: rgba(255, 255, 255, 0.2);
        backdrop-filter: blur(8px);
        color: #fff;
        border-radius: 9999px;
        font-size: 0.75rem;
        font-weight: 700;
        margin-bottom: 0.75rem;
        text-transform: uppercase;
      }
      .msw-hero-title {
        font-size: 2.25rem;
        font-weight: 800;
        color: #fff;
        margin-bottom: 0.5rem;
      }
      .msw-hero-desc {
        color: rgba(255, 255, 255, 0.9);
        font-size: 1.05rem;
        max-width: 600px;
        margin: 0 auto;
      }
      /* Step Progress Bar */
      .msw-progress-card {
        background: var(--bg-card, #1E293B);
        border: 1px solid var(--border, #334155);
        border-radius: 1.25rem;
        padding: 1.25rem;
        margin-bottom: 1.75rem;
      }
      .msw-steps {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 0.5rem;
        position: relative;
      }
      .msw-step-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
        cursor: pointer;
        opacity: 0.5;
        transition: all 0.2s ease;
      }
      .msw-step-item.active {
        opacity: 1;
      }
      .msw-step-item.completed {
        opacity: 0.9;
      }
      .msw-step-circle {
        width: 34px;
        height: 34px;
        border-radius: 50%;
        background: var(--bg-primary, #0F172A);
        border: 2px solid var(--border, #334155);
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 800;
        font-size: 0.85rem;
        color: var(--text-secondary, #94A3B8);
        margin-bottom: 6px;
        transition: all 0.2s ease;
      }
      .msw-step-item.active .msw-step-circle {
        border-color: #6366F1;
        background: #6366F1;
        color: #fff;
        box-shadow: 0 0 12px rgba(99, 102, 241, 0.5);
      }
      .msw-step-item.completed .msw-step-circle {
        border-color: #10B981;
        background: #10B981;
        color: #fff;
      }
      .msw-step-label {
        font-size: 0.75rem;
        font-weight: 700;
        color: var(--text-secondary, #94A3B8);
      }
      .msw-step-item.active .msw-step-label {
        color: #fff;
      }
      /* Wizard Card Container */
      .msw-wizard-body {
        background: var(--bg-card, #1E293B);
        border: 1px solid var(--border, #334155);
        border-radius: 1.5rem;
        padding: 2rem;
        box-shadow: 0 16px 36px rgba(0, 0, 0, 0.15);
      }
      .msw-step-panel {
        display: none;
      }
      .msw-step-panel.active {
        display: block;
        animation: mswFadeIn 0.25s ease forwards;
      }
      @keyframes mswFadeIn {
        from { opacity: 0; transform: translateY(6px); }
        to { opacity: 1; transform: translateY(0); }
      }
      .msw-panel-title {
        font-size: 1.4rem;
        font-weight: 800;
        margin-bottom: 0.35rem;
      }
      .msw-panel-subtitle {
        font-size: 0.9rem;
        color: var(--text-secondary, #94A3B8);
        margin-bottom: 1.5rem;
      }
      /* Step 1 Grid */
      .msw-options-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
        gap: 1rem;
        margin-bottom: 2rem;
      }
      .msw-option-card {
        background: var(--bg-primary, #0F172A);
        border: 2px solid var(--border, #334155);
        border-radius: 1rem;
        padding: 1.25rem;
        cursor: pointer;
        transition: all 0.2s ease;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
      }
      .msw-option-card:hover {
        border-color: #6366F1;
        transform: translateY(-2px);
      }
      .msw-option-card.selected {
        border-color: #6366F1;
        background: rgba(99, 102, 241, 0.08);
        box-shadow: 0 0 16px rgba(99, 102, 241, 0.25);
      }
      .msw-opt-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 0.75rem;
      }
      .msw-opt-icon {
        font-size: 1.75rem;
      }
      .msw-opt-price {
        font-size: 1.15rem;
        font-weight: 800;
        color: #10B981;
      }
      .msw-opt-name {
        font-size: 1.05rem;
        font-weight: 700;
        margin-bottom: 0.35rem;
      }
      .msw-opt-desc {
        font-size: 0.85rem;
        color: var(--text-secondary, #94A3B8);
        line-height: 1.5;
      }
      /* Step 2 Form */
      .msw-addon-group {
        display: flex;
        flex-direction: column;
        gap: 0.85rem;
        margin-bottom: 1.5rem;
      }
      .msw-addon-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        background: var(--bg-primary, #0F172A);
        border: 1px solid var(--border, #334155);
        border-radius: 0.75rem;
        padding: 1rem 1.25rem;
        cursor: pointer;
        transition: border-color 0.2s;
      }
      .msw-addon-item:hover {
        border-color: #6366F1;
      }
      .msw-addon-left {
        display: flex;
        align-items: center;
        gap: 0.85rem;
      }
      .msw-addon-checkbox {
        width: 18px;
        height: 18px;
        accent-color: #6366F1;
      }
      .msw-addon-info strong {
        display: block;
        font-size: 0.95rem;
      }
      .msw-addon-info span {
        font-size: 0.8rem;
        color: var(--text-secondary, #94A3B8);
      }
      .msw-addon-price {
        font-size: 0.95rem;
        font-weight: 700;
        color: #10B981;
      }
      .msw-textarea {
        width: 100%;
        background: var(--bg-primary, #0F172A);
        border: 1px solid var(--border, #334155);
        border-radius: 0.75rem;
        padding: 0.85rem 1rem;
        color: #fff;
        font-family: inherit;
        font-size: 0.95rem;
        resize: vertical;
        min-height: 100px;
        box-sizing: border-box;
        margin-bottom: 1.5rem;
      }
      .msw-textarea:focus {
        outline: none;
        border-color: #6366F1;
      }
      /* Step 3 Photo Upload & Date */
      .msw-upload-zone {
        border: 2px dashed #6366F1;
        background: rgba(99, 102, 241, 0.05);
        border-radius: 1rem;
        padding: 2rem;
        text-align: center;
        cursor: pointer;
        transition: all 0.2s ease;
        margin-bottom: 1.5rem;
      }
      .msw-upload-zone:hover {
        background: rgba(99, 102, 241, 0.1);
      }
      .msw-upload-icon {
        font-size: 2.5rem;
        margin-bottom: 0.5rem;
      }
      .msw-upload-prompt {
        font-size: 1rem;
        font-weight: 700;
        margin-bottom: 0.25rem;
      }
      .msw-upload-hint {
        font-size: 0.8rem;
        color: var(--text-secondary, #94A3B8);
      }
      .msw-photo-preview-box {
        display: none;
        align-items: center;
        gap: 1rem;
        background: var(--bg-primary, #0F172A);
        border: 1px solid var(--border, #334155);
        border-radius: 0.75rem;
        padding: 0.75rem;
        margin-top: 1rem;
      }
      .msw-preview-img {
        width: 60px;
        height: 60px;
        border-radius: 0.5rem;
        object-fit: cover;
      }
      .msw-preview-details {
        flex: 1;
        text-align: left;
      }
      .msw-preview-name {
        font-size: 0.85rem;
        font-weight: 700;
        word-break: break-all;
      }
      .msw-preview-size {
        font-size: 0.75rem;
        color: #10B981;
      }
      .msw-remove-photo-btn {
        background: transparent;
        border: 1px solid #EF4444;
        color: #EF4444;
        padding: 4px 8px;
        border-radius: 6px;
        font-size: 0.75rem;
        cursor: pointer;
      }
      .msw-date-input {
        width: 100%;
        background: var(--bg-primary, #0F172A);
        border: 1px solid var(--border, #334155);
        border-radius: 0.75rem;
        padding: 0.85rem 1rem;
        color: #fff;
        font-family: inherit;
        font-size: 0.95rem;
        margin-bottom: 1.5rem;
        box-sizing: border-box;
      }
      .msw-date-input:focus {
        outline: none;
        border-color: #6366F1;
      }
      /* Step 4 Review & WhatsApp Form */
      .msw-review-card {
        background: var(--bg-primary, #0F172A);
        border: 1px solid var(--border, #334155);
        border-radius: 1rem;
        padding: 1.25rem;
        margin-bottom: 1.5rem;
      }
      .msw-review-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 6px 0;
        font-size: 0.9rem;
        border-bottom: 1px dashed rgba(255, 255, 255, 0.08);
      }
      .msw-review-row:last-child {
        border-bottom: none;
        padding-top: 10px;
        font-weight: 800;
        font-size: 1.1rem;
        color: #10B981;
      }
      .msw-input-group {
        display: flex;
        flex-direction: column;
        gap: 0.4rem;
        margin-bottom: 1rem;
      }
      .msw-input-group label {
        font-size: 0.85rem;
        font-weight: 700;
        color: var(--text-secondary, #94A3B8);
      }
      .msw-text-input {
        background: var(--bg-primary, #0F172A);
        border: 1px solid var(--border, #334155);
        border-radius: 0.75rem;
        padding: 0.85rem 1rem;
        color: #fff;
        font-size: 0.95rem;
        box-sizing: border-box;
        width: 100%;
      }
      .msw-text-input:focus {
        outline: none;
        border-color: #6366F1;
      }
      /* Action Buttons */
      .msw-actions {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 1rem;
        margin-top: 1.75rem;
      }
      .msw-btn-back {
        background: transparent;
        border: 1px solid var(--border, #334155);
        color: var(--text-secondary, #94A3B8);
        padding: 0.85rem 1.5rem;
        border-radius: 0.75rem;
        font-weight: 700;
        font-size: 0.95rem;
        cursor: pointer;
        transition: all 0.2s ease;
      }
      .msw-btn-back:hover {
        color: #fff;
        border-color: #fff;
      }
      .msw-btn-next {
        background: #6366F1;
        color: #fff;
        border: none;
        padding: 0.85rem 1.75rem;
        border-radius: 0.75rem;
        font-weight: 700;
        font-size: 0.95rem;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 6px;
        transition: all 0.2s ease;
      }
      .msw-btn-next:hover {
        background: #4F46E5;
        transform: translateY(-1px);
      }
      .msw-btn-whatsapp {
        background: #25D366;
        color: #fff;
        border: none;
        padding: 1rem 2rem;
        border-radius: 0.75rem;
        font-weight: 800;
        font-size: 1rem;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 8px;
        box-shadow: 0 10px 24px rgba(37, 211, 102, 0.35);
        transition: all 0.2s ease;
      }
      .msw-btn-whatsapp:hover {
        background: #20BA5A;
        transform: translateY(-2px);
      }
      /* Success Modal */
      .msw-modal-overlay {
        display: none;
        position: fixed;
        top: 0; left: 0; right: 0; bottom: 0;
        background: rgba(0, 0, 0, 0.75);
        backdrop-filter: blur(6px);
        z-index: 10000;
        align-items: center;
        justify-content: center;
        padding: 1rem;
      }
      .msw-modal-content {
        background: var(--bg-card, #1E293B);
        border: 1px solid var(--border, #334155);
        border-radius: 1.5rem;
        padding: 2.5rem 2rem;
        max-width: 480px;
        width: 100%;
        text-align: center;
        box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5);
      }
      @media (max-width: 640px) {
        .msw-steps {
          grid-template-columns: repeat(4, 1fr);
          gap: 2px;
        }
        .msw-step-label {
          font-size: 0.65rem;
        }
        .msw-step-circle {
          width: 28px;
          height: 28px;
          font-size: 0.75rem;
        }
        .msw-wizard-body {
          padding: 1.25rem;
        }
        .msw-actions {
          flex-direction: column-reverse;
          gap: 0.75rem;
        }
        .msw-btn-back, .msw-btn-next, .msw-btn-whatsapp {
          width: 100%;
          justify-content: center;
        }
      }
    </style>

    <div class="msw-container">
      $heroBannerHtml

      <!-- Progress Navigation -->
      <div class="msw-progress-card">
        <div class="msw-steps">
          <div class="msw-step-item active" id="mswStepTab0" onclick="mswGoToStep(0)">
            <div class="msw-step-circle">1</div>
            <span class="msw-step-label">Package</span>
          </div>
          <div class="msw-step-item" id="mswStepTab1" onclick="mswGoToStep(1)">
            <div class="msw-step-circle">2</div>
            <span class="msw-step-label">Customization</span>
          </div>
          <div class="msw-step-item" id="mswStepTab2" onclick="mswGoToStep(2)">
            <div class="msw-step-circle">3</div>
            <span class="msw-step-label">Photo & Date</span>
          </div>
          <div class="msw-step-item" id="mswStepTab3" onclick="mswGoToStep(3)">
            <div class="msw-step-circle">4</div>
            <span class="msw-step-label">Confirm</span>
          </div>
        </div>
      </div>

      <!-- Wizard Body -->
      <div class="msw-wizard-body">
        <!-- STEP 1: Package Selection -->
        <div class="msw-step-panel active" id="mswPanel0">
          <h3 class="msw-panel-title">1. Choose Your Preferred Package</h3>
          <p class="msw-panel-subtitle">Select the tier or baseline that best matches your celebration or project</p>
          
          <div class="msw-options-grid" id="mswOptionsGrid">
            <!-- Rendered by JS -->
          </div>

          <div class="msw-actions" style="justify-content: flex-end;">
            <button type="button" class="msw-btn-next" onclick="mswNextStep()">
              <span>Next: Custom Options</span> ➔
            </button>
          </div>
        </div>

        <!-- STEP 2: Customization & Preferences -->
        <div class="msw-step-panel" id="mswPanel1">
          <h3 class="msw-panel-title">2. Add-ons & Custom Preferences</h3>
          <p class="msw-panel-subtitle">Enhance your request with artisan finishings, priority service, and custom notes</p>
          
          <div class="msw-addon-group">
            <label class="msw-addon-item" onclick="mswToggleAddon('rush', 250)">
              <div class="msw-addon-left">
                <input type="checkbox" id="mswAddonRush" class="msw-addon-checkbox" onchange="mswUpdateTotal()">
                <div class="msw-addon-info">
                  <strong>⚡ Priority Rush Turnaround</strong>
                  <span>Fast-tracked production & dedicated priority scheduling</span>
                </div>
              </div>
              <span class="msw-addon-price">+₹250</span>
            </label>

            <label class="msw-addon-item" onclick="mswToggleAddon('gift', 150)">
              <div class="msw-addon-left">
                <input type="checkbox" id="mswAddonGift" class="msw-addon-checkbox" onchange="mswUpdateTotal()">
                <div class="msw-addon-info">
                  <strong>🎁 Luxury Gift Packaging & Wax Seal</strong>
                  <span>Embossed presentation box with silk ribbon & handwritten calligraphy card</span>
                </div>
              </div>
              <span class="msw-addon-price">+₹150</span>
            </label>

            <label class="msw-addon-item" onclick="mswToggleAddon('custom_craft', 200)">
              <div class="msw-addon-left">
                <input type="checkbox" id="mswAddonCraft" class="msw-addon-checkbox" onchange="mswUpdateTotal()">
                <div class="msw-addon-info">
                  <strong>🖋️ Bespoke Monogram / Custom Color Palette</strong>
                  <span>Personalized initials, custom piping colors, or specific brand styling</span>
                </div>
              </div>
              <span class="msw-addon-price">+₹200</span>
            </label>
          </div>

          <label style="display:block; font-size: 0.85rem; font-weight: 700; margin-bottom: 0.5rem; color: var(--text-secondary, #94A3B8);">
            Special Instructions / Dietary or Dimension Specifications:
          </label>
          <textarea id="mswNotes" class="msw-textarea" placeholder="e.g. Please use pastel lilac colors, sugar pearls, and write 'Happy 25th Anya' on the cake topper..."></textarea>

          <div class="msw-actions">
            <button type="button" class="msw-btn-back" onclick="mswPrevStep()">← Back</button>
            <button type="button" class="msw-btn-next" onclick="mswNextStep()">
              <span>Next: Photo & Date</span> ➔
            </button>
          </div>
        </div>

        <!-- STEP 3: Reference Photo & Delivery Date -->
        <div class="msw-step-panel" id="mswPanel2">
          <h3 class="msw-panel-title">3. Attach Reference Photo & Delivery Date</h3>
          <p class="msw-panel-subtitle">Upload inspiration photos, Pinterest boards, sketches, or room photos for our artisan team</p>

          <div class="msw-upload-zone" onclick="document.getElementById('mswFileInput').click()">
            <input type="file" id="mswFileInput" accept="image/*" style="display:none;" onchange="mswHandlePhotoSelect(this)">
            <div class="msw-upload-icon">📸</div>
            <div class="msw-upload-prompt">Click or Tap to Upload Inspiration Photo</div>
            <div class="msw-upload-hint">Supports JPG, PNG, WEBP from your camera roll or photo gallery</div>
          </div>

          <div class="msw-photo-preview-box" id="mswPhotoPreviewBox">
            <img id="mswPreviewImg" class="msw-preview-img" src="" alt="Reference Photo">
            <div class="msw-preview-details">
              <div class="msw-preview-name" id="mswPreviewName">custom_photo.jpg</div>
              <div class="msw-preview-size" id="mswPreviewSize">Ready to attach</div>
            </div>
            <button type="button" class="msw-remove-photo-btn" onclick="mswRemovePhoto()">✕ Remove</button>
          </div>

          <div style="margin-top: 1.5rem;">
            <label style="display:block; font-size: 0.85rem; font-weight: 700; margin-bottom: 0.5rem; color: var(--text-secondary, #94A3B8);">
              Target Event / Delivery Date:
            </label>
            <input type="date" id="mswTargetDate" class="msw-date-input">
          </div>

          <div class="msw-actions">
            <button type="button" class="msw-btn-back" onclick="mswPrevStep()">← Back</button>
            <button type="button" class="msw-btn-next" onclick="mswNextStep()">
              <span>Review Order Summary</span> ➔
            </button>
          </div>
        </div>

        <!-- STEP 4: Review & Instant WhatsApp Dispatch -->
        <div class="msw-step-panel" id="mswPanel3">
          <h3 class="msw-panel-title">4. Review & Confirm via WhatsApp</h3>
          <p class="msw-panel-subtitle">Review your custom package details and send your request straight to our WhatsApp</p>

          <div class="msw-review-card">
            <div class="msw-review-row">
              <span>Selected Package:</span>
              <strong id="mswRevPackage">Signature Celebration</strong>
            </div>
            <div class="msw-review-row">
              <span>Package Base Price:</span>
              <span id="mswRevPrice">₹1,899</span>
            </div>
            <div class="msw-review-row">
              <span>Custom Add-ons:</span>
              <span id="mswRevAddons">None</span>
            </div>
            <div class="msw-review-row">
              <span>Reference Photo:</span>
              <span id="mswRevPhoto" style="color:#10B981;">None attached</span>
            </div>
            <div class="msw-review-row">
              <span>Target Delivery Date:</span>
              <span id="mswRevDate">Flexible</span>
            </div>
            <div class="msw-review-row">
              <span>Total Estimated Investment:</span>
              <span id="mswRevTotal">₹1,899</span>
            </div>
          </div>

          <div class="msw-input-group">
            <label for="mswClientName">Your Full Name *</label>
            <input type="text" id="mswClientName" class="msw-text-input" placeholder="e.g. Priya Sharma">
          </div>

          <div class="msw-input-group">
            <label for="mswClientPhone">Your WhatsApp Number *</label>
            <input type="tel" id="mswClientPhone" class="msw-text-input" placeholder="e.g. +91 98765 43210">
          </div>

          <div class="msw-input-group">
            <label for="mswClientAddr">Delivery Address / Landmark (Optional)</label>
            <input type="text" id="mswClientAddr" class="msw-text-input" placeholder="e.g. Indiranagar, 100ft Road, Bangalore">
          </div>

          <div class="msw-actions">
            <button type="button" class="msw-btn-back" onclick="mswPrevStep()">← Back</button>
            <button type="button" class="msw-btn-whatsapp" onclick="mswSubmitToWhatsApp()">
              <span>$btnLabel</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Success Modal -->
    <div class="msw-modal-overlay" id="mswSuccessModal">
      <div class="msw-modal-content">
        <div style="font-size: 3.5rem; margin-bottom: 0.75rem;">🎉</div>
        <h3 style="font-size: 1.5rem; font-weight: 800; margin-bottom: 0.5rem;">Order Prepared for WhatsApp!</h3>
        <p style="color: var(--text-secondary, #94A3B8); font-size: 0.95rem; margin-bottom: 1.25rem;">
          Your custom order reference is <strong id="mswSuccessOrderId" style="color: #6366F1;">#MSW12345</strong>. WhatsApp will open with your complete specifications pre-filled.
        </p>
        <button type="button" class="msw-btn-next" style="width: 100%; justify-content: center;" onclick="document.getElementById('mswSuccessModal').style.display='none'">
          Done / Close
        </button>
      </div>
    </div>

    <script>
      (function() {
        var options = [$jsonOptions];
        var merchantPhone = '$waPhone';
        var storeTitle = '$title';
        var currentStep = 0;
        var selectedOptIndex = 0;
        var attachedPhotoData = null;
        var attachedPhotoName = '';

        window.mswRenderOptions = function() {
          var grid = document.getElementById('mswOptionsGrid');
          if (!grid) return;
          grid.innerHTML = options.map(function(opt, idx) {
            var isSel = (idx === selectedOptIndex);
            return '<div class="msw-option-card ' + (isSel ? 'selected' : '') + '" onclick="mswSelectOption(' + idx + ')">' +
                   '  <div class="msw-opt-header">' +
                   '    <span class="msw-opt-icon">' + opt.icon + '</span>' +
                   '    <span class="msw-opt-price">' + opt.price + '</span>' +
                   '  </div>' +
                   '  <div>' +
                   '    <h4 class="msw-opt-name">' + opt.name + '</h4>' +
                   '    <p class="msw-opt-desc">' + opt.desc + '</p>' +
                   '  </div>' +
                   '</div>';
          }).join('');
        };

        window.mswSelectOption = function(idx) {
          selectedOptIndex = idx;
          mswRenderOptions();
          mswUpdateTotal();
        };

        window.mswGoToStep = function(stepIdx) {
          if (stepIdx < 0 || stepIdx > 3) return;
          currentStep = stepIdx;
          for (var i = 0; i < 4; i++) {
            var tab = document.getElementById('mswStepTab' + i);
            var panel = document.getElementById('mswPanel' + i);
            if (tab) {
              tab.className = 'msw-step-item' + (i === currentStep ? ' active' : (i < currentStep ? ' completed' : ''));
            }
            if (panel) {
              panel.className = 'msw-step-panel' + (i === currentStep ? ' active' : '');
            }
          }
          if (currentStep === 3) {
            mswPopulateReview();
          }
        };

        window.mswNextStep = function() {
          mswGoToStep(currentStep + 1);
        };

        window.mswPrevStep = function() {
          mswGoToStep(currentStep - 1);
        };

        window.mswToggleAddon = function(addonId, cost) {
          mswUpdateTotal();
        };

        window.mswUpdateTotal = function() {
          var opt = options[selectedOptIndex] || options[0];
          var numPrice = parseInt((opt.price || '').replace(/[^0-9]/g, ''), 10) || 0;
          var currency = (opt.price && opt.price.indexOf('$') !== -1) ? '$' : '₹';

          var rush = document.getElementById('mswAddonRush');
          var gift = document.getElementById('mswAddonGift');
          var craft = document.getElementById('mswAddonCraft');

          var extra = 0;
          if (rush && rush.checked) extra += (currency === '$' ? 50 : 250);
          if (gift && gift.checked) extra += (currency === '$' ? 25 : 150);
          if (craft && craft.checked) extra += (currency === '$' ? 35 : 200);

          var total = numPrice + extra;
          var totalStr = currency + total.toLocaleString();

          var revTotal = document.getElementById('mswRevTotal');
          if (revTotal) revTotal.innerText = totalStr;
          return { total: totalStr, extra: extra, base: opt.price };
        };

        window.mswHandlePhotoSelect = function(input) {
          if (input.files && input.files[0]) {
            var file = input.files[0];
            attachedPhotoName = file.name;
            var reader = new FileReader();
            reader.onload = function(e) {
              attachedPhotoData = e.target.result;
              var box = document.getElementById('mswPhotoPreviewBox');
              var img = document.getElementById('mswPreviewImg');
              var name = document.getElementById('mswPreviewName');
              var size = document.getElementById('mswPreviewSize');
              if (box && img && name) {
                img.src = attachedPhotoData;
                name.innerText = file.name;
                if (size) size.innerText = (file.size / 1024).toFixed(1) + ' KB • Attached';
                box.style.display = 'flex';
              }
            };
            reader.readAsDataURL(file);
          }
        };

        window.mswRemovePhoto = function() {
          attachedPhotoData = null;
          attachedPhotoName = '';
          var box = document.getElementById('mswPhotoPreviewBox');
          var input = document.getElementById('mswFileInput');
          if (box) box.style.display = 'none';
          if (input) input.value = '';
        };

        window.mswPopulateReview = function() {
          var opt = options[selectedOptIndex] || options[0];
          var totals = mswUpdateTotal();

          var pName = document.getElementById('mswRevPackage');
          var pPrice = document.getElementById('mswRevPrice');
          var pAddons = document.getElementById('mswRevAddons');
          var pPhoto = document.getElementById('mswRevPhoto');
          var pDate = document.getElementById('mswRevDate');

          if (pName) pName.innerText = opt.name;
          if (pPrice) pPrice.innerText = opt.price;

          var addonsList = [];
          var rush = document.getElementById('mswAddonRush');
          var gift = document.getElementById('mswAddonGift');
          var craft = document.getElementById('mswAddonCraft');
          if (rush && rush.checked) addonsList.push('Priority Rush');
          if (gift && gift.checked) addonsList.push('Gift Box & Wax Seal');
          if (craft && craft.checked) addonsList.push('Custom Monogram');

          if (pAddons) pAddons.innerText = addonsList.length > 0 ? addonsList.join(', ') : 'None';

          if (pPhoto) {
            pPhoto.innerText = attachedPhotoData ? '✓ Attached (' + attachedPhotoName + ')' : 'None attached';
            pPhoto.style.color = attachedPhotoData ? '#10B981' : '#94A3B8';
          }

          var dateInput = document.getElementById('mswTargetDate');
          if (pDate) {
            pDate.innerText = (dateInput && dateInput.value) ? dateInput.value : 'Flexible';
          }
        };

        window.mswSubmitToWhatsApp = function() {
          var name = (document.getElementById('mswClientName').value || '').trim();
          var phone = (document.getElementById('mswClientPhone').value || '').trim();
          var addr = (document.getElementById('mswClientAddr').value || '').trim();
          var notes = (document.getElementById('mswNotes').value || '').trim();
          var date = (document.getElementById('mswTargetDate').value || '').trim();

          if (!name) {
            alert('Please enter your full name before continuing.');
            document.getElementById('mswClientName').focus();
            return;
          }
          if (!phone) {
            alert('Please enter your WhatsApp phone number.');
            document.getElementById('mswClientPhone').focus();
            return;
          }

          var opt = options[selectedOptIndex] || options[0];
          var totals = mswUpdateTotal();
          var orderRef = 'WIZ' + Math.floor(10000 + Math.random() * 90000);

          var addonsList = [];
          var rush = document.getElementById('mswAddonRush');
          var gift = document.getElementById('mswAddonGift');
          var craft = document.getElementById('mswAddonCraft');
          if (rush && rush.checked) addonsList.push('Priority Rush');
          if (gift && gift.checked) addonsList.push('Gift Box & Wax Seal');
          if (craft && craft.checked) addonsList.push('Custom Monogram');

          var msg = '🌸 *NEW BESPOKE ORDER REQUEST: #' + orderRef + '*\n' +
                    '*Service:* ' + storeTitle + '\n\n' +
                    '📦 *Selected Package:* ' + opt.name + ' (' + opt.price + ')\n' +
                    (addonsList.length > 0 ? ('✨ *Add-ons:* ' + addonsList.join(', ') + '\n') : '') +
                    (notes ? ('📝 *Custom Instructions:* ' + notes + '\n') : '') +
                    (date ? ('📅 *Target Date:* ' + date + '\n') : '') +
                    (attachedPhotoData ? '📸 *Reference Photo:* Attached by client (Ready to send in chat)\n' : '') +
                    '💰 *Estimated Total:* ' + totals.total + '\n\n' +
                    '👤 *Client Name:* ' + name + '\n' +
                    '📞 *Client WhatsApp:* ' + phone + '\n' +
                    (addr ? ('📍 *Address / Location:* ' + addr + '\n') : '') + '\n' +
                    'Please review and confirm my custom booking!';

          var url = 'https://wa.me/' + merchantPhone + '?text=' + encodeURIComponent(msg);
          window.open(url, '_blank');

          document.getElementById('mswSuccessOrderId').innerText = '#' + orderRef;
          document.getElementById('mswSuccessModal').style.display = 'flex';
        };

        // Initialize
        mswRenderOptions();
      })();
    </script>
  </section>
"""
    }

    data class ThemeVars(
        val bgPrimary: String,
        val bgSecondary: String,
        val bgCard: String,
        val textPrimary: String,
        val textSecondary: String,
        val accent: String,
        val accentHover: String,
        val accentRgb: String,
        val border: String,
        val glow: String
    )
}
