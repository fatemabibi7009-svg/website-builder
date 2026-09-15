package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WebsiteEntity
import com.example.ui.theme.BrandIndigo

@Composable
fun SiteSettingsDialog(
    website: WebsiteEntity,
    onDismiss: () -> Unit,
    onSave: (title: String, slug: String, description: String, themePreset: String, fontFamily: String, customCss: String) -> Unit
) {
    var title by remember { mutableStateOf(website.title) }
    var slug by remember { mutableStateOf(website.slug) }
    var description by remember { mutableStateOf(website.description) }
    var themePreset by remember { mutableStateOf(website.themePreset) }
    var fontFamily by remember { mutableStateOf(website.fontFamily) }
    var customCss by remember { mutableStateOf(website.customCss) }

    val themes = listOf(
        "modern-dark" to "Modern Dark (Indigo/Slate)",
        "clean-light" to "Clean Light (Snow/Indigo)",
        "sunset-warm" to "Sunset Warm (Amber/Warm)",
        "cyber-neon" to "Cyber Neon (Purple/Night)",
        "emerald-minimal" to "Emerald Minimal (Mint/Dark)"
    )

    val fonts = listOf(
        "Inter, sans-serif" to "Inter (Modern Sans)",
        "Playfair Display, serif" to "Playfair Display (Editorial Serif)",
        "JetBrains Mono, monospace" to "JetBrains Mono (Code Monospace)",
        "Georgia, serif" to "Georgia (Classic Serif)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = BrandIndigo,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Website Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Website Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_site_title"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = slug,
                    onValueChange = { slug = it.lowercase().replace(" ", "-") },
                    label = { Text("Project Slug (URL subdomain)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_site_slug"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("SEO Meta Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp)
                )

                // Theme Presets
                Text(
                    text = "Color Theme Preset",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for ((presetKey, label) in themes) {
                        val isSelected = themePreset == presetKey
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { themePreset = presetKey },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) BrandIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, BrandIndigo) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) BrandIndigo else Color.Transparent)
                                        .border(1.5.dp, if (isSelected) BrandIndigo else MaterialTheme.colorScheme.outline, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) BrandIndigo else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Typography Presets
                Text(
                    text = "Primary Typography",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for ((fontKey, fontLabel) in fonts) {
                        val isSelected = fontFamily == fontKey
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { fontFamily = fontKey },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) BrandIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, BrandIndigo) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) BrandIndigo else Color.Transparent)
                                        .border(1.5.dp, if (isSelected) BrandIndigo else MaterialTheme.colorScheme.outline, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = fontLabel,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) BrandIndigo else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Custom CSS
                OutlinedTextField(
                    value = customCss,
                    onValueChange = { customCss = it },
                    label = { Text("Custom CSS Overrides") },
                    placeholder = { Text("/* e.g. .hero-title { letter-spacing: 2px; } */") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(title, slug, description, themePreset, fontFamily, customCss)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_site_settings_button")
            ) {
                Text("Save Settings", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
