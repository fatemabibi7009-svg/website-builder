package com.example.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockType
import com.example.data.model.WebBlockEntity
import com.example.data.model.WebsiteEntity
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandRose
import com.example.ui.theme.BrandViolet

/**
 * Live visual representation of a website section rendered directly in Jetpack Compose.
 * Used for both the inline Live Visual Editor and the real-time editing sheet preview.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveBlockVisualPreview(
    block: WebBlockEntity,
    website: WebsiteEntity? = null,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = false,
    onEditSection: (() -> Unit)? = null
) {
    val themePreset = website?.themePreset ?: "modern-dark"
    val isDark = themePreset.contains("dark") || themePreset.contains("cyber") || themePreset.contains("terminal") || themePreset.contains("neon")

    // Theme color palette
    val defaultBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textPrimary = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    val customBg = parseHexColor(block.backgroundColorHex)
    val customText = parseHexColor(block.textColorHex)

    val resolvedBg = customBg ?: defaultBg
    val resolvedText = customText ?: textPrimary
    val accentColor = when (themePreset) {
        "cyber-neon" -> BrandCyan
        "emerald-clean" -> BrandEmerald
        "minimal-light" -> BrandIndigo
        "warm-editorial" -> BrandAmber
        "sunset-gradient" -> BrandRose
        "anime-4bit" -> Color(0xFFFF2A85)
        "retro-arcade-4bit" -> Color(0xFF39FF14)
        "gameboy-4bit" -> Color(0xFF306230)
        else -> BrandIndigo
    }

    val textAlign = when (block.alignment.lowercase()) {
        "left" -> TextAlign.Left
        "right" -> TextAlign.Right
        else -> TextAlign.Center
    }

    val horizontalAlign = when (block.alignment.lowercase()) {
        "left" -> Alignment.Start
        "right" -> Alignment.End
        else -> Alignment.CenterHorizontally
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onEditSection != null) Modifier.clickable { onEditSection() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = resolvedBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = horizontalAlign
        ) {
            // Interactive Edit Header Banner if clickable in Live Editor
            if (onEditSection != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(accentColor))
                            Text(
                                text = block.type.displayName.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = accentColor
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { onEditSection() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = accentColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Edit Section",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            when (block.type) {
                BlockType.NAVBAR -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(accentColor))
                            Text(
                                text = block.title.ifBlank { "Brand" },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = resolvedText
                            )
                        }

                        val links = block.content.split("|").map { it.trim() }.filter { it.isNotBlank() }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            links.take(3).forEach { link ->
                                Text(
                                    text = link,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = textSecondary
                                )
                            }
                            if (block.buttonText.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = accentColor
                                ) {
                                    Text(
                                        text = block.buttonText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                BlockType.HERO -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = horizontalAlign
                    ) {
                        if (block.subtitle.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = accentColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(accentColor))
                                    Text(
                                        text = block.subtitle,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                        color = accentColor
                                    )
                                }
                            }
                        }

                        Text(
                            text = block.title.ifBlank { "Hero Heading Goes Here" },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                lineHeight = 28.sp
                            ),
                            color = resolvedText,
                            textAlign = textAlign,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (block.content.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = block.content,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                                color = textSecondary,
                                textAlign = textAlign,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (block.buttonText.isNotBlank() || block.secondaryButtonText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (block.buttonText.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = accentColor
                                    ) {
                                        Text(
                                            text = block.buttonText,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                                if (block.secondaryButtonText.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = cardBg,
                                        border = BorderStroke(1.dp, borderColor)
                                    ) {
                                        Text(
                                            text = block.secondaryButtonText,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = resolvedText,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                BlockType.FEATURES, BlockType.SERVICES -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = horizontalAlign
                    ) {
                        Text(
                            text = block.title.ifBlank { if (block.type == BlockType.FEATURES) "Key Features" else "Our Services" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = resolvedText,
                            textAlign = textAlign
                        )
                        if (block.subtitle.isNotBlank()) {
                            Text(
                                text = block.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary,
                                textAlign = textAlign
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        val items = block.content.split("|").map { it.trim() }.filter { it.isNotBlank() }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items.take(4).forEachIndexed { idx, item ->
                                val parts = item.split(":", limit = 2)
                                val itemTitle = parts.firstOrNull()?.trim() ?: "Item ${idx + 1}"
                                val itemDesc = if (parts.size > 1) parts[1].trim() else ""

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = cardBg,
                                    border = BorderStroke(1.dp, borderColor),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(accentColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when (idx % 4) {
                                                    0 -> "⚡"
                                                    1 -> "🛡️"
                                                    2 -> "📊"
                                                    else -> "🚀"
                                                },
                                                fontSize = 12.sp
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = itemTitle,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = resolvedText
                                            )
                                            if (itemDesc.isNotBlank()) {
                                                Text(
                                                    text = itemDesc,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                    color = textSecondary,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                BlockType.PRICING -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = horizontalAlign
                    ) {
                        Text(
                            text = block.title.ifBlank { "Simple Transparent Pricing" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = resolvedText,
                            textAlign = textAlign
                        )
                        if (block.subtitle.isNotBlank()) {
                            Text(
                                text = block.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary,
                                textAlign = textAlign
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        val tiers = block.content.split("|").map { it.trim() }.filter { it.isNotBlank() }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tiers.take(2).forEachIndexed { idx, tier ->
                                val parts = tier.split(":", limit = 2)
                                val planName = parts.firstOrNull()?.trim() ?: "Plan ${idx + 1}"
                                val features = if (parts.size > 1) parts[1].trim() else ""
                                val isFeatured = idx == 1 || tier.contains("Pro", ignoreCase = true)

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isFeatured) accentColor.copy(alpha = 0.1f) else cardBg,
                                    border = BorderStroke(if (isFeatured) 2.dp else 1.dp, if (isFeatured) accentColor else borderColor)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        if (isFeatured) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = accentColor,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            ) {
                                                Text(
                                                    text = "MOST POPULAR",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = planName,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = resolvedText
                                        )
                                        Text(
                                            text = if (idx == 0) "$0/mo" else "$29/mo",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = accentColor
                                        )
                                        if (features.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = features,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = textSecondary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                BlockType.TESTIMONIALS -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = cardBg,
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = horizontalAlign
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = BrandAmber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = block.content.ifBlank { "\"This platform transformed our entire design workflow within minutes!\"" },
                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic, fontSize = 13.sp),
                                color = resolvedText,
                                textAlign = textAlign
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(accentColor))
                                Text(
                                    text = block.subtitle.ifBlank { "Verified Customer" },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }

                BlockType.STATS -> {
                    val stats = block.content.split("|").map { it.trim() }.filter { it.isNotBlank() }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = horizontalAlign
                    ) {
                        if (block.title.isNotBlank()) {
                            Text(
                                text = block.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = resolvedText,
                                textAlign = textAlign
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            stats.take(3).forEach { item ->
                                val parts = item.split(":", limit = 2)
                                val num = parts.firstOrNull()?.trim() ?: item
                                val label = if (parts.size > 1) parts[1].trim() else ""

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = cardBg,
                                    border = BorderStroke(1.dp, borderColor)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = num,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = accentColor
                                        )
                                        if (label.isNotBlank()) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = textSecondary,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                BlockType.LOGOS -> {
                    val partners = block.content.split("|").map { it.trim() }.filter { it.isNotBlank() }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = horizontalAlign
                    ) {
                        Text(
                            text = block.title.ifBlank { "TRUSTED BY INNOVATORS" },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = textSecondary,
                            textAlign = textAlign
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            partners.forEach { partner ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = cardBg,
                                    border = BorderStroke(1.dp, borderColor)
                                ) {
                                    Text(
                                        text = partner,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                                        color = resolvedText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                BlockType.FAQ -> {
                    val faqs = block.content.split("|").map { it.trim() }.filter { it.isNotBlank() }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = horizontalAlign
                    ) {
                        Text(
                            text = block.title.ifBlank { "Frequently Asked Questions" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = resolvedText,
                            textAlign = textAlign
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            faqs.take(3).forEach { item ->
                                val parts = item.split(":", limit = 2)
                                val q = parts.firstOrNull()?.trim() ?: "Question"
                                val a = if (parts.size > 1) parts[1].trim() else "Answer goes here."

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = cardBg,
                                    border = BorderStroke(1.dp, borderColor)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = q,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = resolvedText,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = textSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = a,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                BlockType.CTA -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = accentColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = block.title.ifBlank { "Ready to build your next project?" },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = resolvedText,
                                textAlign = TextAlign.Center
                            )
                            if (block.subtitle.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = block.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (block.buttonText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = accentColor
                                ) {
                                    Text(
                                        text = block.buttonText,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                BlockType.FOOTER -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "© 2026 ${block.title.ifBlank { "Website" }}. All rights reserved.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = textSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Privacy",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = accentColor
                            )
                            Text(
                                text = "Terms",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = accentColor
                            )
                        }
                    }
                }

                BlockType.WHATSAPP_SHOP -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Store Header Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(cardBg)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(accentColor)
                                )
                                Text(
                                    text = block.title.ifBlank { "Knot & Weave" },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = resolvedText
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BrandEmerald.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🛍️ Cart (1)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BrandEmerald)
                                }
                            }
                        }

                        // Promo Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = block.subtitle.ifBlank { "Artisan Festival • Up to 50% off select handcrafted bags" },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Handcrafted organic merino wool • Direct WhatsApp dispatch",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        // Product Preview Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(55.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFE9D5FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👜 Tote", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B21A8))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Lavender Breeze", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = resolvedText)
                                    Text("₹1,249", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold), color = BrandEmerald)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(55.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFECDD3)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👛 Crossbody", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF9F1239))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Crimson Night", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = resolvedText)
                                    Text("₹899", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold), color = BrandEmerald)
                                }
                            }
                        }

                        // Photo Upload Option & Multi-Step Wizard Indicator
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = BrandAmber.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, BrandAmber.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📸 Custom Photo Upload Feature", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BrandAmber)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Customers can attach reference design photos, custom initials, and notes.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = textSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "1. Address ➔ 2. Payment ➔ 3. Summary",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = resolvedText
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BrandEmerald
                                    ) {
                                        Text(
                                            text = "📲 WhatsApp Order",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                BlockType.MULTISTEP_WIZARD -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = BrandIndigo.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, BrandIndigo.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "✦ MULTI-STEP WIZARD",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = BrandIndigo,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = block.title.ifBlank { "Multi-Step Booking & Quote Wizard" },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = resolvedText,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = block.subtitle.ifBlank { "Customize your options, attach reference photos, and confirm via WhatsApp" },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Stepper indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(cardBg)
                                .padding(vertical = 6.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(BrandIndigo), contentAlignment = Alignment.Center) {
                                    Text("1", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("Tier", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = BrandIndigo)
                            }
                            Text("➔", color = textSecondary, fontSize = 9.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                    Text("2", color = resolvedText, fontSize = 9.sp)
                                }
                                Text("Add-ons", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = textSecondary)
                            }
                            Text("➔", color = textSecondary, fontSize = 9.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                    Text("3", color = resolvedText, fontSize = 9.sp)
                                }
                                Text("Photo & Date", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = textSecondary)
                            }
                            Text("➔", color = textSecondary, fontSize = 9.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(BrandEmerald), contentAlignment = Alignment.Center) {
                                    Text("4", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("WhatsApp", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = BrandEmerald)
                            }
                        }

                        // Sample Tier Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = BrandIndigo.copy(alpha = 0.08f),
                                border = BorderStroke(1.5.dp, BrandIndigo)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("🎂 Signature Tier", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BrandIndigo)
                                    Text("₹1,899", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold), color = BrandEmerald)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = cardBg,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("👑 Grand Luxe", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = resolvedText)
                                    Text("₹3,499", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold), color = BrandEmerald)
                                }
                            }
                        }

                        // Photo Upload + WhatsApp CTA row
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = BrandEmerald.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("📸 Reference Photo Upload Included", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = resolvedText)
                                Surface(shape = RoundedCornerShape(4.dp), color = BrandEmerald) {
                                    Text("📲 WhatsApp", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                }
                            }
                        }
                    }
                }

                BlockType.COUNTDOWN_TIMER -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = block.title.ifBlank { "🚀 Launching Soon" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = resolvedText,
                            textAlign = TextAlign.Center
                        )
                        if (block.subtitle.isNotBlank()) {
                            Text(
                                text = block.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Countdown boxes preview
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("14" to "DAYS", "08" to "HOURS", "45" to "MINS", "30" to "SECS").forEach { (num, lbl) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = cardBg,
                                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = num,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = accentColor
                                        )
                                        Text(
                                            text = lbl,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.SemiBold),
                                            color = textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        if (block.buttonText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = accentColor
                            ) {
                                Text(
                                    text = block.buttonText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                BlockType.IMAGE_CAROUSEL -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = block.title.ifBlank { "Featured Highlights" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = resolvedText,
                            textAlign = TextAlign.Center
                        )
                        if (block.subtitle.isNotBlank()) {
                            Text(
                                text = block.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(accentColor.copy(alpha = 0.12f))
                                .border(1.dp, borderColor, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.4f), modifier = Modifier.size(24.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Text("‹", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🖼️ Interactive Carousel Gallery", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = resolvedText)
                                    Text("Swipe & Autoplay Ready", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = textSecondary)
                                }
                                Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.4f), modifier = Modifier.size(24.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Text("›", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                                }
                            }

                            // Carousel dots
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(16.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(accentColor))
                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.5f)))
                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.5f)))
                            }
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = horizontalAlign
                    ) {
                        Text(
                            text = block.title.ifBlank { block.type.displayName },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = resolvedText,
                            textAlign = textAlign
                        )
                        if (block.subtitle.isNotBlank()) {
                            Text(
                                text = block.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary,
                                textAlign = textAlign
                            )
                        }
                        if (block.content.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = block.content,
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary,
                                textAlign = textAlign
                            )
                        }
                        if (block.buttonText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = accentColor
                            ) {
                                Text(
                                    text = block.buttonText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseHexColor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = when (clean.length) {
            6 -> (0xFF000000 or clean.toLong(16)).toInt()
            8 -> clean.toLong(16).toInt()
            else -> return null
        }
        Color(colorInt)
    } catch (e: Exception) {
        null
    }
}
