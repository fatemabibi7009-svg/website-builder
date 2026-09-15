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

    fun compile(website: WebsiteEntity, blocks: List<WebBlockEntity>): CompiledSite {
        val themeVars = getThemeVariables(website.themePreset)
        val fontImport = getFontImport(website.fontFamily)
        val fontCssFamily = website.fontFamily

        val htmlContent = buildHtml(website, blocks, fontImport, fontCssFamily)
        val cssContent = buildCss(website, themeVars, fontCssFamily)
        val jsContent = buildJs()
        val manifestContent = buildManifest(website, themeVars)
        val readmeContent = buildReadme(website)

        return CompiledSite(
            html = htmlContent,
            css = cssContent,
            js = jsContent,
            manifestJson = manifestContent,
            readme = readmeContent
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
        fontFamily: String
    ): String {
        val visibleBlocks = blocks.filter { it.isVisible }
        val sb = StringBuilder()

        sb.appendLine("<!DOCTYPE html>")
        sb.appendLine("<html lang=\"en\">")
        sb.appendLine("<head>")
        sb.appendLine("  <meta charset=\"UTF-8\">")
        sb.appendLine("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
        sb.appendLine("  <title>${escapeHtml(website.title)}</title>")
        sb.appendLine("  <meta name=\"description\" content=\"${escapeHtml(website.description)}\">")
        sb.appendLine("  <meta name=\"generator\" content=\"Open-Source Mobile Web Builder\">")
        sb.appendLine("  <link rel=\"manifest\" href=\"manifest.json\">")
        sb.appendLine("  <link rel=\"stylesheet\" href=\"styles.css\">")
        if (fontImport.isNotBlank()) {
            sb.appendLine("  $fontImport")
        }
        sb.appendLine("</head>")
        sb.appendLine("<body>")

        // Render sections
        for (block in visibleBlocks) {
            sb.appendLine(renderBlockHtml(block))
        }

        // Floating Back-to-Top Button
        sb.appendLine("  <button id=\"backToTop\" class=\"back-to-top\" aria-label=\"Back to top\" title=\"Back to top\">↑</button>")
        sb.appendLine("  <script src=\"main.js\"></script>")
        sb.appendLine("</body>")
        sb.appendLine("</html>")

        return sb.toString()
    }

    fun resolveSmartButtonUrl(rawUrl: String, buttonText: String, blockType: BlockType): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isNotBlank() && trimmed != "#") {
            return trimmed
        }
        val lower = buttonText.lowercase()
        return when {
            lower.contains("contact") || lower.contains("message") || lower.contains("talk") || lower.contains("reach") || lower.contains("inquir") -> "#contact"
            lower.contains("subscri") || lower.contains("news") || lower.contains("digest") || blockType == BlockType.NEWSLETTER -> "#newsletter"
            lower.contains("direction") || lower.contains("map") -> "https://maps.google.com"
            lower.contains("pric") || lower.contains("plan") || lower.contains("buy") || lower.contains("tier") || lower.contains("start") -> "#pricing"
            lower.contains("feature") || lower.contains("explore") || lower.contains("discover") || lower.contains("more") || lower.contains("work") -> "#features"
            lower.contains("about") || lower.contains("story") || lower.contains("team") -> "#about"
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

    private fun renderBlockHtml(block: WebBlockEntity): String {
        val customStyle = buildString {
            if (block.backgroundColorHex.isNotBlank()) append("background-color: ${block.backgroundColorHex}; ")
            if (block.textColorHex.isNotBlank()) append("color: ${block.textColorHex}; ")
            if (block.alignment.isNotBlank()) append("text-align: ${block.alignment}; ")
        }
        val styleAttr = if (customStyle.isNotBlank()) " style=\"$customStyle\"" else ""
        val blockIdAttr = " id=\"${block.type.name.lowercase()}\" data-block-id=\"${block.id}\" data-block-type=\"${block.type.name}\""

        return when (block.type) {
            BlockType.NAVBAR -> {
                val links = block.content.split("|").filter { it.isNotBlank() }
                buildString {
                    appendLine("  <header class=\"navbar\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container nav-container\">")
                    appendLine("      <a href=\"#\" class=\"nav-brand\">")
                    appendLine("        <span class=\"brand-logo-mark\">✦</span>")
                    appendLine("        <span>${escapeHtml(block.title)}</span>")
                    appendLine("      </a>")
                    appendLine("      <nav class=\"nav-links\" id=\"navLinks\">")
                    for (link in links) {
                        val anchor = link.trim().lowercase().replace(" ", "-")
                        appendLine("        <a href=\"#$anchor\" class=\"nav-link\" data-redirect=\"#$anchor\">${escapeHtml(link.trim())}</a>")
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
                buildString {
                    appendLine("  <section class=\"hero-section\"$blockIdAttr$styleAttr>")
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
                    appendLine("      <div class=\"hero-proof-bar\">")
                    appendLine("        <div class=\"proof-item\"><span class=\"proof-number\">99.99%</span><span class=\"proof-label\">Edge Availability</span></div>")
                    appendLine("        <div class=\"proof-sep\">•</div>")
                    appendLine("        <div class=\"proof-item\"><span class=\"proof-number\">&lt; 1ms</span><span class=\"proof-label\">Global Latency</span></div>")
                    appendLine("        <div class=\"proof-sep\">•</div>")
                    appendLine("        <div class=\"proof-item\"><span class=\"proof-number\">10k+</span><span class=\"proof-label\">Active Builders</span></div>")
                    appendLine("      </div>")
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.FEATURES -> {
                val items = block.content.split("|").filter { it.isNotBlank() }
                buildString {
                    appendLine("  <section class=\"features-section\"$blockIdAttr$styleAttr>")
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
                    appendLine("  <section class=\"about-section\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container about-container\">")
                    if (block.title.isNotBlank()) appendLine("      <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
                    if (block.subtitle.isNotBlank()) appendLine("      <p class=\"section-subtitle\">${escapeHtml(block.subtitle)}</p>")
                    if (block.content.isNotBlank()) appendLine("      <p class=\"about-text\">${escapeHtml(block.content)}</p>")
                    if (block.buttonText.isNotBlank()) {
                        val url = resolveSmartButtonUrl(block.buttonUrl, block.buttonText, block.type)
                        appendLine("      ${renderButtonLink(url, block.buttonText, "btn btn-primary", "#contact")}")
                    }
                    appendLine("    </div>")
                    appendLine("  </section>")
                }
            }

            BlockType.SERVICES -> {
                val items = block.content.split("|").filter { it.isNotBlank() }
                buildString {
                    appendLine("  <section class=\"services-section\"$blockIdAttr$styleAttr>")
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
                    appendLine("  <section class=\"testimonial-section\"$blockIdAttr$styleAttr>")
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
                    appendLine("  <section class=\"pricing-section\"$blockIdAttr$styleAttr>")
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
                    appendLine("  <section class=\"gallery-section\"$blockIdAttr$styleAttr>")
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
                    appendLine("  <section class=\"cta-section\"$blockIdAttr$styleAttr>")
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
                    appendLine("  <section class=\"contact-section\"$blockIdAttr$styleAttr>")
                    appendLine("    <div class=\"container contact-container\">")
                    if (block.title.isNotBlank()) appendLine("      <h2 class=\"section-title\">${escapeHtml(block.title)}</h2>")
                    if (block.subtitle.isNotBlank()) appendLine("      <p class=\"section-subtitle\">${escapeHtml(block.subtitle)}</p>")

                    appendLine("      <div class=\"contact-grid\">")
                    appendLine("        <div class=\"contact-info-panel\">")
                    if (block.content.isNotBlank()) {
                        appendLine("          <p class=\"contact-desc\">${escapeHtml(block.content)}</p>")
                    }
                    appendLine("          <div class=\"contact-details\">")
                    appendLine("            <div class=\"contact-item\">")
                    appendLine("              <span class=\"contact-icon\">✉️</span>")
                    appendLine("              <div><strong>Email</strong><br><a href=\"mailto:hello@mysite.com\">hello@mysite.com</a></div>")
                    appendLine("            </div>")
                    appendLine("            <div class=\"contact-item\">")
                    appendLine("              <span class=\"contact-icon\">📞</span>")
                    appendLine("              <div><strong>Call</strong><br><a href=\"tel:+18005550199\">+1 (800) 555-0199</a></div>")
                    appendLine("            </div>")
                    appendLine("            <div class=\"contact-item\">")
                    appendLine("              <span class=\"contact-icon\">📍</span>")
                    appendLine("              <div><strong>Location</strong><br><span>Global Edge &bull; San Francisco, CA</span></div>")
                    appendLine("            </div>")
                    appendLine("          </div>")
                    appendLine("        </div>")

                    appendLine("        <form id=\"contactForm\" class=\"contact-form\">")
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
                    appendLine("  <section class=\"faq-section\"$blockIdAttr$styleAttr>")
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
                    appendLine("  <section class=\"stats-section\"$blockIdAttr$styleAttr>")
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
                                lower.contains("contact") -> "#contact"
                                lower.contains("docs") -> "#features"
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

    private fun buildCss(website: WebsiteEntity, theme: ThemeVars, fontFamily: String): String {
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
  border-radius: 9999px;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  border: 1px solid transparent;
  outline: none;
}

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

/* Custom CSS from user */
${website.customCss}
""".trimIndent()
    }

    private fun buildJs(): String {
        return """
// High-End Responsive Web Engine with Zero Debug Clutter
document.addEventListener('DOMContentLoaded', () => {
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

  // Contact Form Submission with Realistic In-Card Feedback
  const contactForm = document.getElementById('contactForm');
  if (contactForm) {
    contactForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const submitBtn = contactForm.querySelector('button[type="submit"]');
      const originalText = submitBtn ? submitBtn.innerHTML : 'Submit Message';
      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = 'Sending message...';
      }
      setTimeout(() => {
        if (submitBtn) {
          submitBtn.disabled = false;
          submitBtn.innerHTML = '✓ Message Sent!';
          submitBtn.style.backgroundColor = '#10B981';
          submitBtn.style.borderColor = '#10B981';
        }
        showFormFeedback(contactForm, 'success', 'Thank you! Your message has been received. Our team will get back to you within 24 hours.');
        contactForm.reset();
        setTimeout(() => {
          if (submitBtn) {
            submitBtn.innerHTML = originalText;
            submitBtn.style.backgroundColor = '';
            submitBtn.style.borderColor = '';
          }
        }, 3500);
      }, 500);
    });
  }

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

    private fun getFontImport(fontFamily: String): String {
        return when {
            fontFamily.contains("Playfair", ignoreCase = true) ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,700;1,400&display=swap\" rel=\"stylesheet\">"
            fontFamily.contains("JetBrains", ignoreCase = true) ->
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin><link href=\"https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600;800&display=swap\" rel=\"stylesheet\">"
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
