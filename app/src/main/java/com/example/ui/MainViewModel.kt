package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.BlockType
import com.example.data.model.CompiledSite
import com.example.data.model.DeploymentEntity
import com.example.data.model.WebBlockEntity
import com.example.data.model.WebsiteEntity
import com.example.data.repository.WebsiteRepository
import com.example.deploy.DeployProgressState
import com.example.deploy.NetlifyDeployer
import com.example.deploy.NetlifyUser
import com.example.generator.SiteCompiler
import com.example.generator.TemplateDefinition
import com.example.generator.WebsiteTemplates
import com.example.server.ServerLogEntry
import com.example.server.WebsiteLocalServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WebsiteRepository
    private val localServer = WebsiteLocalServer()
    private val deployer = NetlifyDeployer()
    private val prefs = application.getSharedPreferences("web_builder_prefs", Context.MODE_PRIVATE)

    val serverRunning: StateFlow<Boolean> = localServer.isRunning
    val serverPort: StateFlow<Int> = localServer.port
    val serverLocalIp: StateFlow<String> = localServer.localIp
    val serverLogs: StateFlow<List<ServerLogEntry>> = localServer.logs

    private val _selectedTab = MutableStateFlow(NavigationTab.EDITOR)
    val selectedTab: StateFlow<NavigationTab> = _selectedTab.asStateFlow()

    private val _viewportMode = MutableStateFlow(ViewportMode.MOBILE)
    val viewportMode: StateFlow<ViewportMode> = _viewportMode.asStateFlow()

    private val _currentWebsite = MutableStateFlow<WebsiteEntity?>(null)
    val currentWebsite: StateFlow<WebsiteEntity?> = _currentWebsite.asStateFlow()

    private val _blocks = MutableStateFlow<List<WebBlockEntity>>(emptyList())
    val blocks: StateFlow<List<WebBlockEntity>> = _blocks.asStateFlow()

    private val _compiledSite = MutableStateFlow<CompiledSite?>(null)
    val compiledSite: StateFlow<CompiledSite?> = _compiledSite.asStateFlow()

    private val _editingBlock = MutableStateFlow<WebBlockEntity?>(null)
    val editingBlock: StateFlow<WebBlockEntity?> = _editingBlock.asStateFlow()

    private val _isAddBlockSheetOpen = MutableStateFlow(false)
    val isAddBlockSheetOpen: StateFlow<Boolean> = _isAddBlockSheetOpen.asStateFlow()

    private val _isSiteSettingsOpen = MutableStateFlow(false)
    val isSiteSettingsOpen: StateFlow<Boolean> = _isSiteSettingsOpen.asStateFlow()

    private val _netlifyToken = MutableStateFlow(prefs.getString("netlify_token", "") ?: "")
    val netlifyToken: StateFlow<String> = _netlifyToken.asStateFlow()

    private val _netlifyUser = MutableStateFlow<NetlifyUser?>(null)
    val netlifyUser: StateFlow<NetlifyUser?> = _netlifyUser.asStateFlow()

    private val _customSiteName = MutableStateFlow("")
    val customSiteName: StateFlow<String> = _customSiteName.asStateFlow()

    private val _deployProgress = MutableStateFlow<DeployProgressState>(DeployProgressState.Idle)
    val deployProgress: StateFlow<DeployProgressState> = _deployProgress.asStateFlow()

    private val _deployments = MutableStateFlow<List<DeploymentEntity>>(emptyList())
    val deployments: StateFlow<List<DeploymentEntity>> = _deployments.asStateFlow()

    private val _distZip = MutableStateFlow<File?>(null)
    val distZip: StateFlow<File?> = _distZip.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Template selection screen active before entering editor
    private val _isTemplateSelectionActive = MutableStateFlow(true)
    val isTemplateSelectionActive: StateFlow<Boolean> = _isTemplateSelectionActive.asStateFlow()

    init {
        val database = AppDatabase.getInstance(application)
        repository = WebsiteRepository(database.websiteDao())

        viewModelScope.launch {
            initInitialSiteIfNeeded()
        }

        // Auto-verify saved Netlify token if present
        val savedToken = prefs.getString("netlify_token", "") ?: ""
        if (savedToken.isNotBlank()) {
            verifyNetlifyToken()
        }

        // Keep local server updated with latest compiled output
        viewModelScope.launch {
            _compiledSite.collectLatest { site ->
                if (site != null) {
                    localServer.updateSite(site)
                }
            }
        }
    }

    private suspend fun initInitialSiteIfNeeded() {
        val allSites = repository.getWebsiteSync(1)
        if (allSites == null) {
            // Seed with default SaaS template
            val defaultTemplate = WebsiteTemplates.getDefaultTemplate()
            val initialSite = defaultTemplate.createWebsite().copy(id = 1)
            val siteId = repository.createWebsite(initialSite)
            val initialBlocks = defaultTemplate.createBlocks(siteId)
            repository.insertBlocks(initialBlocks)
            _currentWebsite.value = initialSite
            _blocks.value = initialBlocks
            if (_customSiteName.value.isBlank()) {
                _customSiteName.value = initialSite.slug
            }
            recompileSite(initialSite, initialBlocks)
        } else {
            _currentWebsite.value = allSites
            val loadedBlocks = repository.getBlocksSync(allSites.id)
            _blocks.value = loadedBlocks
            if (_customSiteName.value.isBlank()) {
                _customSiteName.value = allSites.slug
            }
            recompileSite(allSites, loadedBlocks)
        }

        // Observe blocks updates from Room
        _currentWebsite.value?.let { site ->
            repository.getBlocks(site.id).collectLatest { updatedBlocks ->
                _blocks.value = updatedBlocks
                _currentWebsite.value?.let { current ->
                    recompileSite(current, updatedBlocks)
                }
            }
        }
    }

    private fun recompileSite(website: WebsiteEntity, blocksList: List<WebBlockEntity>) {
        viewModelScope.launch(Dispatchers.Default) {
            val compiled = SiteCompiler.compile(website, blocksList)
            _compiledSite.value = compiled

            // Auto-prepare dist zip in background cache
            val cacheDir = getApplication<Application>().cacheDir
            val zipFile = File(cacheDir, "dist-${website.slug.ifBlank { "site" }}.zip")
            SiteCompiler.compileToZip(compiled, zipFile)
            _distZip.value = zipFile
        }
    }

    fun selectTab(tab: NavigationTab) {
        _selectedTab.value = tab
    }

    fun setViewport(mode: ViewportMode) {
        _viewportMode.value = mode
    }

    fun openAddBlockSheet() {
        _isAddBlockSheetOpen.value = true
    }

    fun closeAddBlockSheet() {
        _isAddBlockSheetOpen.value = false
    }

    fun openSiteSettings() {
        _isSiteSettingsOpen.value = true
    }

    fun closeSiteSettings() {
        _isSiteSettingsOpen.value = false
    }

    fun editBlock(block: WebBlockEntity) {
        _editingBlock.value = block
    }

    fun closeBlockEditor() {
        _editingBlock.value = null
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun addBlock(type: BlockType, insertIndex: Int = -1) {
        val site = _currentWebsite.value ?: return
        viewModelScope.launch {
            val currentList = _blocks.value.toMutableList()
            val newIndex = if (insertIndex in currentList.indices) insertIndex + 1 else currentList.size
            val newBlock = WebBlockEntity(
                id = UUID.randomUUID().toString(),
                websiteId = site.id,
                orderIndex = newIndex,
                type = type,
                title = when (type) {
                    BlockType.NAVBAR -> site.title
                    BlockType.HERO -> "Welcome to ${site.title}"
                    BlockType.FEATURES -> "Why Choose Us"
                    BlockType.ABOUT -> "Our Story & Vision"
                    BlockType.SERVICES -> "What We Offer"
                    BlockType.TESTIMONIALS -> "What People Say"
                    BlockType.PRICING -> "Simple Plans"
                    BlockType.GALLERY -> "Visual Showcase"
                    BlockType.CTA -> "Take The Next Step Today"
                    BlockType.CONTACT -> "Get In Touch With Us"
                    BlockType.FAQ -> "Frequently Asked Questions"
                    BlockType.STATS -> "Proven Results & Scale"
                    BlockType.TIMELINE -> "How It Works"
                    BlockType.TEAM -> "Meet Our Leadership"
                    BlockType.LOGOS -> "Trusted By Leading Innovators"
                    BlockType.NEWSLETTER -> "Stay In The Loop"
                    BlockType.CUSTOM_HTML -> "Custom Component"
                    BlockType.FOOTER -> site.title
                },
                subtitle = when (type) {
                    BlockType.HERO -> "Fast, responsive and designed to impress."
                    BlockType.FEATURES -> "Designed for speed, built for reliability."
                    BlockType.TESTIMONIALS -> "Verified Customer"
                    BlockType.CONTACT -> "We'd love to hear from you."
                    BlockType.STATS -> "Key milestones and numbers demonstrating reliability at scale."
                    BlockType.TIMELINE -> "A seamless, step-by-step workflow designed for rapid execution."
                    BlockType.TEAM -> "Experienced engineers, designers, and creative leaders."
                    BlockType.LOGOS -> "Powering modern teams and forward-thinking enterprises."
                    else -> ""
                },
                content = when (type) {
                    BlockType.NAVBAR -> "Home|Features|Pricing|Contact"
                    BlockType.HERO -> "Empowering creators to design stunning websites directly from their phones."
                    BlockType.FEATURES -> "🚀 Ultra-Fast: Sub-second load times worldwide.|📱 Fully Responsive: Looks incredible on every screen.|🎨 Modular Design: Drag and drop customization."
                    BlockType.ABOUT -> "We build open-source tools that empower millions of independent creators and small businesses."
                    BlockType.SERVICES -> "Web Design: Modern, accessible layouts.|Custom Systems: High scale architecture.|Consulting: Practical advice for growth."
                    BlockType.TESTIMONIALS -> "\"This website builder made launching our landing page faster than ever.\""
                    BlockType.PRICING -> "Basic ($9/mo): Essential Features, Standard Support|Pro ($29/mo): Unlimited Traffic, Custom Domain, Priority Support|Team ($79/mo): Multi-User, Dedicated Account Manager"
                    BlockType.FAQ -> "Can I export my code?: Yes! Dist maker provides complete HTML, CSS, and JS.|Can I host anywhere?: Absolutely. Host on Netlify, GitHub Pages, or any server.|Is it free?: 100% free and open-source."
                    BlockType.STATS -> "99.99%: Edge Availability SLA | < 1ms: Global TTFB | 10k+: Active Teams | 250M+: Daily Requests"
                    BlockType.TIMELINE -> "01 Connect & Configure: Link your domain and choose your custom layout blocks.|02 Customize & Style: Fine-tune colors, copy, typography, and interactive components.|03 One-Click Deploy: Launch instantly to global edge CDN with automated SSL."
                    BlockType.TEAM -> "Sarah Chen: Chief Executive Officer: Leading product vision and distributed systems engineering.|Marcus Vance: Head of Design: Award-winning art director specializing in tactile typography.|Alex Rivera: Lead Developer Advocate: Full-stack educator and open-source systems contributor."
                    BlockType.LOGOS -> "Google Cloud | Stripe | Vercel | Supabase | GitHub | Docker | Cloudflare"
                    BlockType.CUSTOM_HTML -> "<div style=\"padding: 2rem; background: rgba(99,102,241,0.1); border-radius: 12px; text-align: center;\"><p>Custom HTML / Embed element</p></div>"
                    BlockType.FOOTER -> "© 2026 ${site.title}. Powered by Web Builder."
                    else -> ""
                },
                buttonText = when (type) {
                    BlockType.HERO, BlockType.CTA -> "Get Started"
                    BlockType.NAVBAR -> "Contact"
                    BlockType.PRICING -> "Choose Plan"
                    BlockType.ABOUT -> "Our Story"
                    BlockType.SERVICES -> "Get a Quote"
                    BlockType.FEATURES -> "Explore Features"
                    BlockType.CONTACT -> "Send Message"
                    BlockType.TIMELINE -> "Start Now"
                    BlockType.TEAM -> "Join Our Team"
                    BlockType.STATS -> "View Metrics"
                    BlockType.NEWSLETTER -> "Subscribe"
                    else -> ""
                },
                buttonUrl = when (type) {
                    BlockType.HERO -> "#pricing"
                    BlockType.CTA -> "#contact"
                    BlockType.NAVBAR -> "#contact"
                    BlockType.PRICING -> "#contact"
                    BlockType.ABOUT -> "#services"
                    BlockType.SERVICES -> "#contact"
                    BlockType.FEATURES -> "#pricing"
                    BlockType.CONTACT -> "#contact"
                    BlockType.TIMELINE -> "#pricing"
                    BlockType.STATS -> "#pricing"
                    BlockType.TEAM -> "#contact"
                    BlockType.NEWSLETTER -> "#newsletter"
                    else -> "#contact"
                },
                secondaryButtonText = when (type) {
                    BlockType.HERO -> "Learn More"
                    else -> ""
                },
                secondaryButtonUrl = when (type) {
                    BlockType.HERO -> "#features"
                    else -> ""
                }
            )

            currentList.add(newIndex, newBlock)
            repository.reorderBlocks(currentList)
            _blocks.value = currentList
            _userMessage.value = "Added ${type.displayName}"
        }
    }

    fun saveEditedBlock(updated: WebBlockEntity) {
        viewModelScope.launch {
            repository.updateBlock(updated)
            val updatedList = _blocks.value.map { if (it.id == updated.id) updated else it }
            _blocks.value = updatedList
            _currentWebsite.value?.let { recompileSite(it, updatedList) }
            _editingBlock.value = null
            _userMessage.value = "Updated ${updated.type.displayName}"
        }
    }

    fun toggleBlockVisibility(block: WebBlockEntity) {
        val updated = block.copy(isVisible = !block.isVisible)
        saveEditedBlock(updated)
    }

    fun duplicateBlock(block: WebBlockEntity) {
        viewModelScope.launch {
            repository.duplicateBlock(block)
            _userMessage.value = "Duplicated ${block.type.displayName}"
        }
    }

    fun deleteBlock(block: WebBlockEntity) {
        viewModelScope.launch {
            repository.deleteBlock(block)
            val remaining = _blocks.value.filter { it.id != block.id }
            repository.reorderBlocks(remaining)
            _blocks.value = remaining
            _userMessage.value = "Deleted ${block.type.displayName}"
        }
    }

    fun moveBlockUp(block: WebBlockEntity) {
        val site = _currentWebsite.value ?: return
        viewModelScope.launch {
            repository.moveBlock(site.id, block.id, -1)
        }
    }

    fun moveBlockDown(block: WebBlockEntity) {
        val site = _currentWebsite.value ?: return
        viewModelScope.launch {
            repository.moveBlock(site.id, block.id, 1)
        }
    }

    fun reorderBlocks(newOrder: List<WebBlockEntity>) {
        viewModelScope.launch {
            _blocks.value = newOrder
            repository.reorderBlocks(newOrder)
            _currentWebsite.value?.let { recompileSite(it, newOrder) }
        }
    }

    fun updateSiteSettings(
        title: String,
        slug: String,
        description: String,
        themePreset: String,
        fontFamily: String,
        customCss: String
    ) {
        val site = _currentWebsite.value ?: return
        val updated = site.copy(
            title = title.ifBlank { "My Website" },
            slug = slug.ifBlank { "my-website" },
            description = description,
            themePreset = themePreset,
            fontFamily = fontFamily,
            customCss = customCss,
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.updateWebsite(updated)
            _currentWebsite.value = updated
            recompileSite(updated, _blocks.value)
            _isSiteSettingsOpen.value = false
            _userMessage.value = "Saved website settings"
        }
    }

    fun applyTemplate(template: TemplateDefinition) {
        viewModelScope.launch {
            val newSite = template.createWebsite().copy(id = 1)
            repository.updateWebsite(newSite)
            _currentWebsite.value = newSite

            val newBlocks = template.createBlocks(newSite.id)
            repository.replaceAllBlocks(newSite.id, newBlocks)
            _blocks.value = newBlocks
            recompileSite(newSite, newBlocks)

            _selectedTab.value = NavigationTab.EDITOR
            _userMessage.value = "Loaded ${template.name} template"
        }
    }

    fun showTemplateSelection() {
        _isTemplateSelectionActive.value = true
    }

    fun dismissTemplateSelection() {
        _isTemplateSelectionActive.value = false
    }

    fun selectTemplateAndEnterEditor(template: TemplateDefinition) {
        applyTemplate(template)
        _isTemplateSelectionActive.value = false
    }

    fun toggleServer() {
        viewModelScope.launch {
            if (serverRunning.value) {
                localServer.stop()
                _userMessage.value = "Localhost server stopped"
            } else {
                _compiledSite.value?.let { localServer.updateSite(it) }
                val started = localServer.start(8080)
                if (started) {
                    _userMessage.value = "Localhost server active on http://${localServer.localIp.value}:8080"
                } else {
                    _userMessage.value = "Failed to start localhost server"
                }
            }
        }
    }

    fun clearServerLogs() {
        localServer.clearLogs()
    }

    fun setNetlifyToken(token: String) {
        val trimmed = token.trim()
        _netlifyToken.value = trimmed
        prefs.edit().putString("netlify_token", trimmed).apply()
        if (trimmed.isNotBlank()) {
            verifyNetlifyToken()
        } else {
            _netlifyUser.value = null
        }
    }

    fun deleteNetlifyToken() {
        _netlifyToken.value = ""
        _netlifyUser.value = null
        prefs.edit().remove("netlify_token").apply()
        _userMessage.value = "Netlify token removed successfully."
    }

    fun setCustomSiteName(name: String) {
        val sanitized = name.lowercase().replace(Regex("[^a-z0-9-]"), "-").trim('-')
        _customSiteName.value = sanitized
    }

    fun verifyNetlifyToken() {
        val token = _netlifyToken.value
        if (token.isBlank()) {
            _userMessage.value = "Please enter your Netlify Personal Access Token"
            return
        }
        viewModelScope.launch {
            _userMessage.value = "Verifying Netlify token..."
            val result = deployer.verifyToken(token)
            result.onSuccess { user ->
                _netlifyUser.value = user
                _userMessage.value = "Connected as ${user.fullName} (${user.email})"
            }.onFailure { err ->
                _netlifyUser.value = null
                _userMessage.value = "Token verification failed: ${err.message}"
            }
        }
    }

    fun deployToNetlify(customName: String? = null, useSimulationIfNoToken: Boolean = true) {
        val site = _currentWebsite.value ?: return
        val requestedName = (customName ?: _customSiteName.value).ifBlank { site.slug }
        val safeName = requestedName.lowercase().replace(Regex("[^a-z0-9-]"), "-").trim('-').ifBlank { "site-${site.id}" }

        // Sync slug with site entity if it was changed
        if (site.slug != safeName) {
            val updatedSite = site.copy(slug = safeName)
            _currentWebsite.value = updatedSite
            viewModelScope.launch {
                repository.updateWebsite(updatedSite)
            }
        }

        val zip = _distZip.value
        if (zip == null || !zip.exists()) {
            _userMessage.value = "Building dist bundle..."
            _compiledSite.value?.let {
                val f = File(getApplication<Application>().cacheDir, "dist-$safeName.zip")
                SiteCompiler.compileToZip(it, f)
                _distZip.value = f
            }
        }

        val targetZip = _distZip.value
        if (targetZip == null || !targetZip.exists()) {
            _deployProgress.value = DeployProgressState.Error("Could not generate ZIP bundle")
            return
        }

        val token = _netlifyToken.value.trim()
        viewModelScope.launch {
            if (token.isNotBlank()) {
                val result = deployer.deployReal(token, safeName, targetZip) { progress ->
                    _deployProgress.value = progress
                }
                result.onSuccess { deployRes ->
                    val deployment = DeploymentEntity(
                        websiteId = site.id,
                        siteId = deployRes.siteId,
                        siteName = deployRes.siteName,
                        deployUrl = deployRes.liveUrl,
                        adminUrl = deployRes.adminUrl,
                        status = "LIVE"
                    )
                    repository.recordDeployment(deployment)
                    _deployments.value = listOf(deployment) + _deployments.value
                    _userMessage.value = "Live on Netlify! ${deployRes.liveUrl}"
                }
            } else if (useSimulationIfNoToken) {
                val result = deployer.deploySimulation(safeName, targetZip) { progress ->
                    _deployProgress.value = progress
                }
                result.onSuccess { deployRes ->
                    val deployment = DeploymentEntity(
                        websiteId = site.id,
                        siteId = deployRes.siteId,
                        siteName = deployRes.siteName,
                        deployUrl = deployRes.liveUrl,
                        adminUrl = deployRes.adminUrl,
                        status = "LIVE"
                    )
                    repository.recordDeployment(deployment)
                    _deployments.value = listOf(deployment) + _deployments.value
                    _userMessage.value = "Site deployed (Demo Mode)! ${deployRes.liveUrl}"
                }
            } else {
                _userMessage.value = "Netlify Access Token is required for production deploy."
            }
        }
    }

    fun resetDeployState() {
        _deployProgress.value = DeployProgressState.Idle
    }

    fun exportSiteAsJson(): String {
        val site = _currentWebsite.value ?: return "{}"
        val blocksList = _blocks.value
        val root = JSONObject()
        root.put("title", site.title)
        root.put("slug", site.slug)
        root.put("description", site.description)
        root.put("themePreset", site.themePreset)
        root.put("fontFamily", site.fontFamily)
        root.put("customCss", site.customCss)

        val arr = JSONArray()
        for (b in blocksList) {
            val obj = JSONObject()
            obj.put("type", b.type.name)
            obj.put("title", b.title)
            obj.put("subtitle", b.subtitle)
            obj.put("content", b.content)
            obj.put("buttonText", b.buttonText)
            obj.put("buttonUrl", b.buttonUrl)
            obj.put("secondaryButtonText", b.secondaryButtonText)
            obj.put("secondaryButtonUrl", b.secondaryButtonUrl)
            obj.put("imageUrl", b.imageUrl)
            obj.put("backgroundColorHex", b.backgroundColorHex)
            obj.put("textColorHex", b.textColorHex)
            obj.put("alignment", b.alignment)
            arr.put(obj)
        }
        root.put("blocks", arr)
        return root.toString(2)
    }

    fun importSiteFromJson(jsonStr: String): Boolean {
        return try {
            val json = JSONObject(jsonStr)
            val current = _currentWebsite.value ?: return false
            val updatedSite = current.copy(
                title = json.optString("title", current.title),
                slug = json.optString("slug", current.slug),
                description = json.optString("description", current.description),
                themePreset = json.optString("themePreset", current.themePreset),
                fontFamily = json.optString("fontFamily", current.fontFamily),
                customCss = json.optString("customCss", current.customCss)
            )

            val blocksArr = json.optJSONArray("blocks") ?: JSONArray()
            val newBlocks = mutableListOf<WebBlockEntity>()
            for (i in 0 until blocksArr.length()) {
                val obj = blocksArr.getJSONObject(i)
                val type = try {
                    BlockType.valueOf(obj.optString("type"))
                } catch (e: Exception) {
                    BlockType.CUSTOM_HTML
                }
                newBlocks.add(
                    WebBlockEntity(
                        websiteId = updatedSite.id,
                        orderIndex = i,
                        type = type,
                        title = obj.optString("title", ""),
                        subtitle = obj.optString("subtitle", ""),
                        content = obj.optString("content", ""),
                        buttonText = obj.optString("buttonText", ""),
                        buttonUrl = obj.optString("buttonUrl", ""),
                        secondaryButtonText = obj.optString("secondaryButtonText", ""),
                        secondaryButtonUrl = obj.optString("secondaryButtonUrl", ""),
                        imageUrl = obj.optString("imageUrl", ""),
                        backgroundColorHex = obj.optString("backgroundColorHex", ""),
                        textColorHex = obj.optString("textColorHex", ""),
                        alignment = obj.optString("alignment", "center")
                    )
                )
            }

            viewModelScope.launch {
                repository.updateWebsite(updatedSite)
                repository.replaceAllBlocks(updatedSite.id, newBlocks)
                _currentWebsite.value = updatedSite
                _blocks.value = newBlocks
                recompileSite(updatedSite, newBlocks)
                _userMessage.value = "Imported ${updatedSite.title} successfully!"
            }
            true
        } catch (e: Exception) {
            _userMessage.value = "Failed to parse JSON: ${e.message}"
            false
        }
    }
}
