package com.example.ui.preview

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
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
import android.webkit.WebResourceRequest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tablet
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.CompiledSite
import com.example.ui.ViewportMode
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandIndigo

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PreviewScreen(
    compiledSite: CompiledSite?,
    viewportMode: ViewportMode,
    onSelectViewport: (ViewportMode) -> Unit,
    serverRunning: Boolean,
    serverPort: Int
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Assemble complete standalone HTML with inlined CSS & JS for 100% reliable local preview
    val fullHtml = remember(compiledSite) {
        if (compiledSite == null) {
            "<html><body style='display:flex;align-items:center;justify-content:center;height:100vh;font-family:sans-serif;'><h2>Generating preview...</h2></body></html>"
        } else {
            val html = compiledSite.html
            val css = "<style>\n${compiledSite.css}\n</style>"
            val js = "<script>\n${compiledSite.js}\n</script>"

            // Inject styles and script directly into HTML if links exist
            html.replace("<link rel=\"stylesheet\" href=\"styles.css\">", css)
                .replace("<script src=\"main.js\"></script>", js)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Viewport Switcher Toolbar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Responsive toggles
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ViewportToggleButton(
                        icon = Icons.Default.PhoneAndroid,
                        label = "Mobile",
                        selected = viewportMode == ViewportMode.MOBILE,
                        onClick = { onSelectViewport(ViewportMode.MOBILE) }
                    )
                    ViewportToggleButton(
                        icon = Icons.Default.Tablet,
                        label = "Tablet",
                        selected = viewportMode == ViewportMode.TABLET,
                        onClick = { onSelectViewport(ViewportMode.TABLET) }
                    )
                    ViewportToggleButton(
                        icon = Icons.Default.DesktopWindows,
                        label = "Desktop",
                        selected = viewportMode == ViewportMode.DESKTOP,
                        onClick = { onSelectViewport(ViewportMode.DESKTOP) }
                    )
                }

                // Quick Action buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            webViewInstance?.loadDataWithBaseURL(
                                "http://localhost:$serverPort/",
                                fullHtml,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload Preview",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val url = if (serverRunning) "http://localhost:$serverPort" else "data:text/html;charset=utf-8,${Uri.encode(fullHtml)}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Open in Browser",
                            tint = BrandIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Viewport Frame Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = 72.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            val frameModifier = when (viewportMode) {
                ViewportMode.MOBILE -> Modifier
                    .width(360.dp)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(3.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                ViewportMode.TABLET -> Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
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
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true

                            addJavascriptInterface(object {
                                @android.webkit.JavascriptInterface
                                fun onLinkTapped(href: String, text: String) {
                                    // Silent bridge handler
                                }
                            }, "AndroidBridge")

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    if (url.startsWith("mailto:")) {
                                        try {
                                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(url))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                        return true
                                    } else if (url.startsWith("tel:")) {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                        return true
                                    } else if (url.startsWith("http://") || url.startsWith("https://")) {
                                        if (!url.contains("localhost")) {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                            return true
                                        }
                                    }
                                    return false
                                }
                            }
                            loadDataWithBaseURL(
                                "http://localhost:$serverPort/",
                                fullHtml,
                                "text/html",
                                "UTF-8",
                                null
                            )
                            webViewInstance = this
                        }
                    },
                    update = { view ->
                        webViewInstance = view
                        view.loadDataWithBaseURL(
                            "http://localhost:$serverPort/",
                            fullHtml,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ViewportToggleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) BrandIndigo else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                ),
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
