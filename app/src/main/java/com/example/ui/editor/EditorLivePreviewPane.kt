package com.example.ui.editor

import android.annotation.SuppressLint
import android.view.View
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.WebBlockEntity
import com.example.data.model.WebsiteEntity
import com.example.generator.SiteCompiler
import com.example.ui.ViewportMode
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo

/**
 * In-editor live preview pane that renders the exact compiled website HTML/CSS
 * in real-time as users drag, drop, and edit UI elements.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EditorLivePreviewPane(
    website: WebsiteEntity?,
    blocks: List<WebBlockEntity>,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
    draggingItemIndex: Int? = null,
    isExpanded: Boolean = false,
    activePageSlug: String = "index",
    onSelectPage: ((String) -> Unit)? = null,
    onToggleExpand: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null
) {
    var viewportMode by remember { mutableStateOf(ViewportMode.MOBILE) }
    var reloadTrigger by remember { mutableStateOf(0) }
    var internalActivePageSlug by remember(activePageSlug) { mutableStateOf(activePageSlug) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val pages = remember(website?.pagesJson) {
        SiteCompiler.parsePages(website?.pagesJson ?: "")
    }

    // Pulsing animation for real-time live indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Recompile the site in real time using the exact compiler
    val compiledSite = remember(website, blocks) {
        if (website != null) {
            SiteCompiler.compile(website, blocks)
        } else null
    }

    // Assemble complete standalone HTML for the active page with inlined CSS & JS matching final export
    val fullHtml = remember(compiledSite, internalActivePageSlug, reloadTrigger) {
        if (compiledSite == null) {
            "<!DOCTYPE html><html><body style='display:flex;align-items:center;justify-content:center;height:100vh;font-family:sans-serif;background:#0d1117;color:#8b949e;text-align:center;'><div><h3>Preparing In-Editor Live Preview...</h3><p>Add sections or choose a template to begin</p></div></body></html>"
        } else {
            val html: String = if (internalActivePageSlug.isEmpty() || internalActivePageSlug == "index") {
                compiledSite.html
            } else {
                compiledSite.additionalPages[internalActivePageSlug] ?: compiledSite.html
            }
            val css = "<style>\n${compiledSite.css}\n</style>"
            val js = "<script>\n${compiledSite.js}\n</script>"
            html.replace("<link rel=\"stylesheet\" href=\"styles.css\">", css)
                .replace("<script src=\"main.js\"></script>", js)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("in_editor_live_preview_pane"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, BrandIndigo.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Live Preview Header Toolbar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Live Status Indicator & Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(BrandEmerald.copy(alpha = pulseAlpha))
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "LIVE PREVIEW PANE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BrandEmerald.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "100% Exact HTML",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = BrandEmerald
                                    )
                                }
                            }
                            Text(
                                text = "${blocks.count { it.isVisible }} visible sections • Real-time drag sync",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Viewport & Action Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Viewport Selector Pills
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewportMode = ViewportMode.MOBILE },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("preview_pane_viewport_mobile")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhoneAndroid,
                                        contentDescription = "Mobile Viewport",
                                        tint = if (viewportMode == ViewportMode.MOBILE) BrandIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewportMode = ViewportMode.TABLET },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("preview_pane_viewport_tablet")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tablet,
                                        contentDescription = "Tablet Viewport",
                                        tint = if (viewportMode == ViewportMode.TABLET) BrandIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewportMode = ViewportMode.DESKTOP },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("preview_pane_viewport_desktop")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DesktopWindows,
                                        contentDescription = "Desktop Viewport",
                                        tint = if (viewportMode == ViewportMode.DESKTOP) BrandIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Refresh button
                        IconButton(
                            onClick = {
                                reloadTrigger++
                                webViewRef?.reload()
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload Live Preview",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Expand / Collapse toggle
                        if (onToggleExpand != null) {
                            IconButton(
                                onClick = onToggleExpand,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = if (isExpanded) "Collapse Pane" else "Expand Pane",
                                    tint = BrandIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Close button if dismissible
                        if (onClose != null) {
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Preview Pane",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Real-time Drag Feedback Banner
            AnimatedVisibility(
                visible = isDragging,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = BrandIndigo,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, BrandCyan.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = null,
                            tint = BrandCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val pos = (draggingItemIndex ?: 0) + 1
                        Text(
                            text = "Live Sync: Reordering block to #$pos • Website HTML Updating Instantly",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            // Browser Address Chrome & Page Tabs
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = BrandEmerald,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val currentSlugPart = if (internalActivePageSlug == "index" || internalActivePageSlug.isEmpty()) "" else "/$internalActivePageSlug.html"
                                Text(
                                    text = "https://${website?.slug ?: "mysite"}.webcraft.app$currentSlugPart",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = viewportMode.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = BrandCyan
                        )
                    }

                    if (pages.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            pages.forEach { page ->
                                val isSelected = page.slug == internalActivePageSlug
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) BrandIndigo else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) BrandIndigo else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.clickable {
                                        internalActivePageSlug = page.slug
                                        onSelectPage?.invoke(page.slug)
                                    }
                                ) {
                                    Text(
                                        text = page.title.ifEmpty { page.slug },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Responsive Viewport Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                val frameModifier = when (viewportMode) {
                    ViewportMode.MOBILE -> Modifier
                        .width(340.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    ViewportMode.TABLET -> Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    ViewportMode.DESKTOP -> Modifier
                        .fillMaxSize()
                }

                Surface(
                    modifier = frameModifier,
                    color = Color.White
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                // Disable hardware acceleration on emulator to prevent Mesa rendernode crashes
                                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    cacheMode = WebSettings.LOAD_NO_CACHE
                                }
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        val url = request?.url?.toString() ?: ""
                                        if (url.startsWith("http://localhost") || url.startsWith("http://127.0.0.1")) {
                                            val path = request?.url?.path ?: ""
                                            val cleanSlug = path.removePrefix("/").removeSuffix(".html").substringBefore("?")
                                            val resolvedSlug = if (cleanSlug.isEmpty()) "index" else cleanSlug
                                            if (pages.any { it.slug == resolvedSlug }) {
                                                internalActivePageSlug = resolvedSlug
                                                onSelectPage?.invoke(resolvedSlug)
                                                return true
                                            }
                                        }
                                        return true // Confine navigation within preview
                                    }

                                    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                        // Prevents host application crash if renderer process terminates
                                        return true
                                    }
                                }

                                addJavascriptInterface(object {
                                    @android.webkit.JavascriptInterface
                                    fun onLinkTapped(href: String, text: String) {
                                        val cleanHref = href.trim()
                                        if (cleanHref.endsWith(".html") || !cleanHref.startsWith("#")) {
                                            val targetSlug = cleanHref.removeSuffix(".html").removePrefix("/").substringBefore("?")
                                            if (pages.any { it.slug == targetSlug || (targetSlug == "index" && it.slug == "index") }) {
                                                this@apply.post {
                                                    val resolved = if (targetSlug.isEmpty()) "index" else targetSlug
                                                    internalActivePageSlug = resolved
                                                    onSelectPage?.invoke(resolved)
                                                }
                                            }
                                        }
                                    }
                                }, "AndroidBridge")

                                tag = fullHtml
                                loadDataWithBaseURL(
                                    "http://localhost/",
                                    fullHtml,
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                                webViewRef = this
                            }
                        },
                        update = { webView ->
                            webViewRef = webView
                            if (webView.tag != fullHtml) {
                                webView.tag = fullHtml
                                webView.loadDataWithBaseURL(
                                    "http://localhost/",
                                    fullHtml,
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
