package com.example.ui.templates

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator.TemplateDefinition
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandRose
import com.example.ui.theme.BrandViolet

/**
 * Thumbnail grid component for the template selection screen that shows
 * small, high-fidelity visual previews of available website templates.
 */
@Composable
fun TemplateThumbnailGrid(
    templates: List<TemplateDefinition>,
    onSelectTemplate: (TemplateDefinition) -> Unit,
    onPreviewTemplate: (TemplateDefinition) -> Unit,
    modifier: Modifier = Modifier,
    headerContent: (@Composable () -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("template_thumbnail_grid"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (headerContent != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                headerContent()
            }
        }

        if (templates.isEmpty() && emptyContent != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                emptyContent()
            }
        } else {
            items(templates, key = { it.id }) { template ->
                TemplateThumbnailCard(
                    template = template,
                    onSelect = { onSelectTemplate(template) },
                    onPreview = { onPreviewTemplate(template) }
                )
            }
        }

        // Bottom spacer for comfortable scrolling
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Individual Card in the Thumbnail Grid featuring a small visual preview,
 * theme color indicators, metadata tags, and quick actions.
 */
@Composable
fun TemplateThumbnailCard(
    template: TemplateDefinition,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = getTemplateAccent(template.id)
    val sampleBlocks = remember(template) { template.createBlocks(0) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onPreview)
            .testTag("template_thumbnail_card_${template.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Small Visual Preview (Thumbnail Frame)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(152.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                TemplateVisualThumbnail(
                    template = template,
                    accentColor = accentColor
                )

                // Top Floating Badges: Category & Theme Color Dots
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.72f),
                        contentColor = Color.White
                    ) {
                        Text(
                            text = template.badge.ifBlank { template.category },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Theme Color Palette Indicator Dots
                    TemplateColorDots(template = template)
                }

                // Bottom Floating Name Banner with Quick Preview Button
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.75f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(onClick = onPreview)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("preview_thumb_btn_${template.id}")
                        ) {
                            Text(
                                text = "Preview",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandCyan
                            )
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Quick Preview ${template.name}",
                                tint = BrandCyan,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            // 2. Template Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = template.category,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${sampleBlocks.size} sections",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = accentColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onPreview,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("btn_inspect_${template.id}"),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Inspect",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = onSelect,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(34.dp)
                            .testTag("select_template_thumb_${template.id}"),
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo)
                    ) {
                        Text(
                            text = "Use",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders small visual preview of the template layout within a mock browser frame.
 */
@Composable
fun TemplateVisualThumbnail(
    template: TemplateDefinition,
    accentColor: Color
) {
    val isDark = template.themePreset.contains("dark") ||
        template.themePreset.contains("neon") ||
        template.themePreset.contains("agency") ||
        template.themePreset.contains("terminal")

    val bg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardFill = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val subText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        // Browser Window Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.5.dp)) {
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF10B981)))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF))
                    .padding(horizontal = 3.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "🌐 ${template.name}",
                    fontSize = 6.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Distinct Visual Layout Tailored per Template
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            when {
                template.id.contains("saas") -> SaasThumbnailLayout(template, accentColor, cardFill, subText, isDark)
                template.id.contains("agency") -> AgencyThumbnailLayout(template, accentColor, cardFill, subText)
                template.id.contains("store") -> EcommerceThumbnailLayout(template, accentColor, cardFill, subText)
                template.id.contains("portfolio") -> PortfolioThumbnailLayout(template, accentColor, cardFill, subText)
                template.id.contains("blog") -> BlogThumbnailLayout(template, accentColor, cardFill, subText)
                template.id.contains("cafe") -> CafeThumbnailLayout(template, accentColor, cardFill, subText)
                template.id.contains("link") -> LinkBioThumbnailLayout(template, accentColor, cardFill, subText)
                template.id.contains("resume") -> ResumeThumbnailLayout(template, accentColor, cardFill)
                template.id.contains("blank") -> BlankCanvasThumbnailLayout(template, accentColor)
                else -> GenericThumbnailLayout(template, accentColor, cardFill, subText)
            }
        }
    }
}

// -------------------------------------------------------------
// Specialized Visual Preview Layouts for each Template Archetype
// -------------------------------------------------------------

@Composable
private fun SaasThumbnailLayout(template: TemplateDefinition, accent: Color, cardFill: Color, subText: Color, isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Navbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(accent))
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent)
            )
        }

        // Hero Gradient Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = 0.35f), accent.copy(alpha = 0.08f))
                    )
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Template Name Title
                Text(
                    text = template.name,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Subtitle line
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(subText)
                )
                // CTA Button
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(accent)
                )
            }
        }

        // 3 Feature Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(3) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(cardFill)
                        .padding(2.dp),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(accent.copy(alpha = 0.7f)))
                    Box(modifier = Modifier.width(16.dp).height(2.5.dp).clip(RoundedCornerShape(1.dp)).background(subText))
                }
            }
        }
    }
}

@Composable
private fun AgencyThumbnailLayout(template: TemplateDefinition, accent: Color, cardFill: Color, subText: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Monogram Navbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(template.name.take(9).uppercase(), fontSize = 6.5.sp, fontWeight = FontWeight.Black, color = Color.White)
            Box(modifier = Modifier.width(14.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(accent))
        }

        // Bold Typography Hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF131826))
                .border(0.5.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .padding(4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(modifier = Modifier.width(18.dp).height(3.dp).clip(RoundedCornerShape(1.dp)).background(accent))
                Text(
                    text = template.name,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(modifier = Modifier.width(50.dp).height(3.dp).clip(RoundedCornerShape(1.dp)).background(subText))
            }
        }

        // 2 Portfolio Showcase Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(BrandViolet.copy(alpha = 0.35f))
                    .border(0.5.dp, BrandViolet.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(BrandCyan.copy(alpha = 0.35f))
                    .border(0.5.dp, BrandCyan.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
private fun EcommerceThumbnailLayout(template: TemplateDefinition, accent: Color, cardFill: Color, subText: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Top Announcement Strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(accent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(template.name.take(16), fontSize = 5.sp, fontWeight = FontWeight.Bold, color = accent)
        }

        // Hero Shop Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE6F4EA))
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = template.name,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F5132),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(modifier = Modifier.width(20.dp).height(5.dp).clip(RoundedCornerShape(2.dp)).background(accent))
                }
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(accent.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        // 2x2 Products Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(2) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(cardFill)
                        .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(3.dp))
                        .padding(2.5.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFF1F5F9))
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(16.dp).height(3.dp).background(subText))
                        Box(modifier = Modifier.width(10.dp).height(4.dp).clip(RoundedCornerShape(1.dp)).background(accent))
                    }
                }
            }
        }
    }
}

@Composable
private fun PortfolioThumbnailLayout(template: TemplateDefinition, accent: Color, cardFill: Color, subText: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Centered Creator Avatar & Bio
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.3f))
                    .border(1.dp, accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(accent))
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(1.5.dp)) {
                Text(
                    text = template.name,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(modifier = Modifier.width(22.dp).height(2.5.dp).clip(RoundedCornerShape(1.dp)).background(accent))
            }
        }

        // Masonry Portfolio Grid (staggered cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(BrandViolet.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(BrandRose.copy(alpha = 0.25f))
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(BrandCyan.copy(alpha = 0.25f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(BrandAmber.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
private fun BlogThumbnailLayout(template: TemplateDefinition, accent: Color, cardFill: Color, subText: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Editorial Masthead
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(template.name.uppercase(), fontSize = 6.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
        }

        // Featured Story Hero with Image preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(cardFill)
                .border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(3.dp))
                .padding(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(2.dp))
                        .background(accent.copy(alpha = 0.3f))
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(modifier = Modifier.width(14.dp).height(3.dp).clip(RoundedCornerShape(1.dp)).background(accent))
                    Text(
                        text = template.name,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(modifier = Modifier.width(28.dp).height(2.5.dp).clip(RoundedCornerShape(1.dp)).background(subText))
                }
            }
        }

        // 2 Article Snippets
        repeat(2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.width(55.dp).height(3.dp).background(Color(0xFF334155)))
                Box(modifier = Modifier.width(12.dp).height(3.dp).background(accent.copy(alpha = 0.6f)))
            }
        }
    }
}

@Composable
private fun CafeThumbnailLayout(template: TemplateDefinition, accent: Color, cardFill: Color, subText: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Cafe Warm Hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF2C1E18))
                .border(0.5.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Coffee,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    text = template.name,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFDE68A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accent)
                )
            }
        }

        // Menu Rows with Golden Price tags
        repeat(2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF241914))
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.width(36.dp).height(3.dp).background(Color(0xFFE5E7EB)))
                Box(modifier = Modifier.width(12.dp).height(4.dp).clip(RoundedCornerShape(1.dp)).background(accent))
            }
        }
    }
}

@Composable
private fun LinkBioThumbnailLayout(template: TemplateDefinition, accent: Color, cardFill: Color, subText: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Avatar + Handle
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(listOf(BrandRose, BrandAmber, BrandViolet, BrandRose))
                )
                .padding(1.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
        Text(
            text = template.name,
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // 3 Stacked Rounded Link Pills
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(11.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        if (index == 0) accent else cardFill
                    )
                    .border(
                        0.5.dp,
                        if (index == 0) accent else Color(0xFFCBD5E1),
                        RoundedCornerShape(5.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(35.dp)
                        .height(2.5.dp)
                        .background(if (index == 0) Color.White else Color(0xFF334155))
                )
            }
        }

        // Social Icons Row
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) {
                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(accent.copy(alpha = 0.5f)))
            }
        }
    }
}

@Composable
private fun ResumeThumbnailLayout(template: TemplateDefinition, accent: Color, cardFill: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF111827))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Terminal Prompt
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(">", fontSize = 7.sp, color = BrandEmerald, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(2.dp))
            Text(template.name.lowercase().replace(" ", "-"), fontSize = 6.5.sp, color = Color(0xFF94A3B8), maxLines = 1)
        }

        // Code Syntax Lines
        Box(modifier = Modifier.width(55.dp).height(3.dp).background(BrandCyan))
        Box(modifier = Modifier.width(42.dp).height(3.dp).background(BrandAmber))
        Box(modifier = Modifier.width(62.dp).height(3.dp).background(BrandEmerald))

        Spacer(modifier = Modifier.height(2.dp))

        // Skills Matrix
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF1F2937))
                        .border(0.5.dp, BrandEmerald.copy(alpha = 0.4f), RoundedCornerShape(2.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.width(12.dp).height(2.dp).background(BrandEmerald))
                }
            }
        }
    }
}

@Composable
private fun BlankCanvasThumbnailLayout(template: TemplateDefinition, accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFF1F5F9))
            .border(
                BorderStroke(1.dp, Brush.linearGradient(listOf(Color(0xFF94A3B8), Color(0xFFCBD5E1)))),
                RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = template.name,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GenericThumbnailLayout(template: TemplateDefinition, accent: Color, cardFill: Color, subText: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(accent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = template.name,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Box(modifier = Modifier.weight(1f).height(24.dp).clip(RoundedCornerShape(2.dp)).background(cardFill))
            Box(modifier = Modifier.weight(1f).height(24.dp).clip(RoundedCornerShape(2.dp)).background(cardFill))
        }
    }
}

/**
 * Renders 3 mini palette dots corresponding to the template's color scheme.
 */
@Composable
fun TemplateColorDots(template: TemplateDefinition) {
    val colors = when {
        template.id.contains("saas") -> listOf(Color(0xFF0F172A), BrandIndigo, Color(0xFF94A3B8))
        template.id.contains("agency") -> listOf(Color(0xFF090A0F), BrandViolet, BrandCyan)
        template.id.contains("store") -> listOf(Color(0xFFFFFFFF), BrandEmerald, Color(0xFF065F46))
        template.id.contains("portfolio") -> listOf(Color(0xFFFAF8FF), BrandViolet, BrandRose)
        template.id.contains("blog") -> listOf(Color(0xFFFFFDF9), BrandCyan, Color(0xFF1E293B))
        template.id.contains("cafe") -> listOf(Color(0xFF1C1410), BrandAmber, Color(0xFFFDE68A))
        template.id.contains("link") -> listOf(Color(0xFFF8FAFC), BrandIndigo, BrandRose)
        template.id.contains("resume") -> listOf(Color(0xFF181824), BrandCyan, BrandEmerald)
        else -> listOf(Color(0xFFF8FAFC), BrandIndigo, Color(0xFF64748B))
    }

    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.6f),
        modifier = Modifier.padding(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            colors.forEach { col ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(col)
                        .border(0.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                )
            }
        }
    }
}
