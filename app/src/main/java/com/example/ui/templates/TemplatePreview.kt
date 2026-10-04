package com.example.ui.templates

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockType
import com.example.generator.TemplateDefinition
import com.example.ui.ViewportMode
import com.example.ui.theme.*

/**
 * TemplatePreview component that displays a larger visual representation of a selected template,
 * along with its prominent name, archetype badges, responsive multi-device preview,
 * key features list, design system specs, and direct editor action controls.
 */
@Composable
fun TemplatePreview(
    template: TemplateDefinition,
    onSelect: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sampleBlocks = remember(template) { template.createBlocks(0) }
    val accentColor = getTemplateAccent(template.id)
    val templateIcon = getTemplateIcon(template.id)
    val keyFeatures = remember(template.id) { getTemplateKeyFeatures(template) }
    var selectedViewport by remember { mutableStateOf(ViewportMode.DESKTOP) }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .testTag("template_preview_component"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // =========================================================================
            // 1. TOP HEADER: Name, Badges, and Dismiss Button
            // =========================================================================
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentColor.copy(alpha = 0.15f))
                                .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = templateIcon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = template.name,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("template_preview_title")
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = accentColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = template.badge.ifBlank { template.category },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = accentColor,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Category: ${template.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${sampleBlocks.size} Sections",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = accentColor
                                )
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Theme: ${template.themePreset}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .testTag("template_preview_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Preview",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // =========================================================================
            // 2. SCROLLABLE BODY: Larger Visual Preview + Key Features + Specs
            // =========================================================================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Description Callout
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("template_preview_description")
                )

                // -------------------------------------------------------------
                // A. LARGER VISUAL REPRESENTATION WITH MULTI-DEVICE VIEWPORT
                // -------------------------------------------------------------
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("template_large_visual_preview_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Viewport Switcher Bar & Browser Chrome
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Traffic lights + Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.height(22.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = BrandEmerald,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = "https://${template.id.replace('_', '-')}.dev",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Viewport Mode Buttons (Desktop / Tablet / Mobile)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ViewportMode.entries.forEach { mode ->
                                    val isCurrent = selectedViewport == mode
                                    val icon = when (mode) {
                                        ViewportMode.MOBILE -> Icons.Default.Smartphone
                                        ViewportMode.TABLET -> Icons.Default.Tablet
                                        ViewportMode.DESKTOP -> Icons.Default.Computer
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isCurrent) BrandIndigo else Color.Transparent,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { selectedViewport = mode }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = mode.label,
                                                tint = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = when (mode) {
                                                    ViewportMode.MOBILE -> "Mobile"
                                                    ViewportMode.TABLET -> "Tablet"
                                                    ViewportMode.DESKTOP -> "Desktop"
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Large Preview Canvas Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val containerWidthFraction = when (selectedViewport) {
                                ViewportMode.MOBILE -> 0.45f
                                ViewportMode.TABLET -> 0.72f
                                ViewportMode.DESKTOP -> 1f
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(containerWidthFraction)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        if (selectedViewport != ViewportMode.DESKTOP) 2.dp else 1.dp,
                                        if (selectedViewport != ViewportMode.DESKTOP) MaterialTheme.colorScheme.outline else accentColor.copy(alpha = 0.3f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .shadow(if (selectedViewport != ViewportMode.DESKTOP) 4.dp else 0.dp)
                            ) {
                                LargeTemplateVisualCanvas(
                                    template = template,
                                    accentColor = accentColor,
                                    viewport = selectedViewport
                                )
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // B. KEY FEATURES SECTION
                // -------------------------------------------------------------
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BrandIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "KEY FEATURES & CAPABILITIES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            keyFeatures.forEach { feature ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(BrandEmerald.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = BrandEmerald,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }

                                    Text(
                                        text = feature,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // C. DESIGN SYSTEM & SPECS MATRIX
                // -------------------------------------------------------------
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "DESIGN SYSTEM SPECIFICATIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Font Family Spec Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "TYPOGRAPHY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = template.fontFamily.split(",").firstOrNull()?.trim() ?: "Inter",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Google Fonts Pairing",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Color Preset Spec Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "PALETTE PRESET",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = template.themePreset,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(accentColor))
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(BrandIndigo))
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(BrandCyan))
                                }
                            }
                        }

                        // Sections Count Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "STRUCTURE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${sampleBlocks.size} Blocks",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = accentColor
                                )
                                Text(
                                    text = "Modular Sections",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // D. SECTION BLUEPRINT LIST
                // -------------------------------------------------------------
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "INCLUDED SECTION BLUEPRINTS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    sampleBlocks.forEachIndexed { index, block ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(accentColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = accentColor,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = block.type.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (block.title.isNotBlank()) {
                                        Text(
                                            text = block.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = block.type.name.lowercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // =========================================================================
            // 3. BOTTOM ACTIONS: Direct Editor Integration & Close
            // =========================================================================
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("close_template_preview_button")
                    ) {
                        Text("Back to Grid")
                    }

                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("use_template_preview_button")
                    ) {
                        Text("Use This Template & Edit", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-detail Large Visual Canvas demonstrating the actual layout,
 * colors, typography, hero banner, feature blocks, and footer.
 */
@Composable
private fun LargeTemplateVisualCanvas(
    template: TemplateDefinition,
    accentColor: Color,
    viewport: ViewportMode
) {
    val isDark = template.id.contains("saas") || template.id.contains("agency") || template.id.contains("resume")
    val bgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val subText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(accentColor))
                Text(
                    text = template.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (viewport != ViewportMode.MOBILE) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Features", fontSize = 9.sp, color = subText)
                    Text("About", fontSize = 9.sp, color = subText)
                    Text("Pricing", fontSize = 9.sp, color = subText)
                }
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = accentColor
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }

        // 2. Large Hero Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFEEF2F6)
            ),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "✨ ${template.category.uppercase()} LAUNCH",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "Crafted with ${template.name}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = template.description.take(80) + "...",
                    fontSize = 10.sp,
                    color = subText,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = accentColor
                    ) {
                        Text(
                            text = "Explore Now",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, subText.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Learn More",
                            fontSize = 9.sp,
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 3. Archetype-Tailored Content Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(if (viewport == ViewportMode.MOBILE) 2 else 3) { index ->
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accentColor))
                        }
                        Text(
                            text = when (index) {
                                0 -> "High Performance"
                                1 -> "Clean Layout"
                                else -> "Custom Theming"
                            },
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            maxLines = 1
                        )
                        Text(
                            text = "Pre-configured and ready to publish.",
                            fontSize = 8.sp,
                            color = subText,
                            maxLines = 2,
                            lineHeight = 11.sp
                        )
                    }
                }
            }
        }

        // 4. Footer Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "© 2026 ${template.name}. All rights reserved.",
                fontSize = 7.5.sp,
                color = subText
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(subText))
                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(subText))
                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(subText))
            }
        }
    }
}

/**
 * Returns a list of curated key features for each template archetype.
 */
fun getTemplateKeyFeatures(template: TemplateDefinition): List<String> {
    return when {
        template.id.contains("4bit") || template.category.contains("4-Bit") || template.category.contains("Anime") -> listOf(
            "Authentic 4-bit / 8-bit retro arcade aesthetic with pixel typography ('Press Start 2P', 'DotGothic16')",
            "Synthesized 8-bit Web Audio chiptune engine & retro square-wave sound effects",
            "CRT scanline effect simulation with authentic phosphor glow and jitter toggles",
            "High-contrast retro color palette (Game Boy DMG, Akiba Cyber Neon, or Phosphor Green)",
            "Integrated multi-step wizard, interactive cartridge catalog, or direct WhatsApp dispatch"
        )
        template.id.contains("whatsapp") -> listOf(
            "Interactive product catalog with category filters and live instant search",
            "Cart drawer with live subtotal calculation and quantity adjustment",
            "Multi-step checkout wizard (Delivery Address, Payment Method, Order Summary)",
            "Direct WhatsApp order dispatch with auto-formatted customer receipt",
            "Photo upload tool for custom artisan piece orders and reference designs"
        )
        template.id.contains("saas") -> listOf(
            "Full-bleed hero banner with high-converting dual Call-to-Action buttons",
            "Interactive feature grid highlighting product modules and benefits",
            "Tiered pricing matrix with popular badge and FAQ accordion",
            "Customer social proof section with testimonial cards & trust logos",
            "Dynamic dark aesthetic paired with high-contrast vibrant accents"
        )
        template.id.contains("agency") -> listOf(
            "Monochrome dark canvas with high-impact typography pairing",
            "Masonry portfolio gallery showcasing creative case studies",
            "Services breakdown matrix with custom hover accents",
            "Client testimonial carousel & enterprise brand partners strip",
            "Direct inquiry & consultation form section"
        )
        template.id.contains("store") -> listOf(
            "Product showcase grid with pricing, discount badges & quick-add",
            "Top promotional banner for seasonal offers and free shipping alerts",
            "Customer reviews rating cards with star indicators",
            "Trust badges including Money Back Guarantee and Secure Checkout",
            "Mobile-optimized shopping cart and checkout triggers"
        )
        template.id.contains("portfolio") -> listOf(
            "Personal branding hero with creator avatar and narrative bio",
            "Multi-category project gallery with live links and tech tags",
            "Interactive skills matrix with proficiency badges",
            "Integrated contact card and social network links",
            "Warm creative visual theme with responsive grid foundation"
        )
        template.id.contains("blog") -> listOf(
            "Editorial magazine layout featuring lead featured story banner",
            "Categorized article list with reading time and author metadata",
            "Newsletter subscription signup component with instant trigger",
            "Clean serif / sans typography pairing tuned for reading comfort",
            "Social share links and author bio box"
        )
        template.id.contains("cafe") -> listOf(
            "Warm culinary aesthetic with rich golden & espresso color palette",
            "Structured food and beverage menu with pricing and dietary tags",
            "Operating hours, Google Maps directions, and table reservation CTA",
            "Chef's special recommendations showcase cards",
            "Instagram feed gallery teaser for culinary photos"
        )
        template.id.contains("link") -> listOf(
            "Minimalist mobile-first link in bio hub optimized for all screens",
            "Rounded link buttons with custom icons and click feedback",
            "Profile avatar header with bio handle and verification badge",
            "Social media icon row connecting Instagram, TikTok, and X",
            "Ultra-lightweight fast-loading page footprint"
        )
        template.id.contains("resume") -> listOf(
            "Terminal-inspired developer aesthetic with dark syntax highlights",
            "Chronological work experience and educational timeline",
            "Technical skill tags for languages, frameworks, and cloud tools",
            "One-click PDF resume download & GitHub profile connection",
            "Compact project highlights with live demo badges"
        )
        template.id.contains("blank") -> listOf(
            "Pure empty canvas with zero pre-baked layout assumptions",
            "Complete freedom to arrange any of the 12+ modular content blocks",
            "Clean slate starting point with default responsive container",
            "Full styling customization through the live property inspector",
            "Instant preview and one-click code generation"
        )
        else -> listOf(
            "Responsive multi-section layout pre-configured for modern browsers",
            "Curated typography and cohesive color palette presets",
            "Modular block components that can be reordered or edited easily",
            "SEO-friendly semantic HTML structure",
            "Instant one-click integration into the drag-and-drop editor"
        )
    }
}
