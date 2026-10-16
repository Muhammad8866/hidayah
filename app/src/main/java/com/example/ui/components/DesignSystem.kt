package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HeroGradientBottom
import com.example.ui.theme.HeroGradientMiddle
import com.example.ui.theme.HeroGradientTop
import kotlin.math.cos
import kotlin.math.sin

/*
 * Design system v2: the building blocks every screen shares, so cards, tiles, rows and hero surfaces
 * look and behave the same everywhere. Prefer these over hand-rolled Card/Box combinations.
 */

/** One spacing scale for the whole app. Screens use [screen] as their horizontal gutter. */
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val screen = 16.dp
    /** Gap between stacked cards and sections in a scrolling page. */
    val section = 20.dp
}

/** Mid-tone accents are lifted toward white on dark surfaces so icons keep their contrast. */
@Composable
fun accentTint(accent: Color): Color =
    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) lerp(accent, Color.White, 0.3f) else accent

/** Gently shrinks whatever it decorates while [interactionSource] is pressed. */
fun Modifier.pressScale(interactionSource: MutableInteractionSource, pressedScale: Float = 0.97f): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "pressScale"
    )
    graphicsLayer { scaleX = scale; scaleY = scale }
}

/** Card corners for row [index] of [count] stacked rows: the group reads as one rounded block. */
fun groupedShape(index: Int, count: Int, outer: Dp = 20.dp, inner: Dp = 6.dp): Shape {
    val top = if (index == 0) outer else inner
    val bottom = if (index == count - 1) outer else inner
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

/**
 * The standard content card: surface colour, hairline outline, large corners. With [onClick] it
 * becomes a button with a soft press animation.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    outlined: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(Spacing.lg),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val border = if (outlined) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)) else null
    val clickModifier = if (onClick != null) Modifier.pressScale(interaction) else Modifier
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.then(clickModifier),
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            interactionSource = interaction
        ) { Column(Modifier.padding(contentPadding), verticalArrangement = verticalArrangement, content = content) }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            border = border
        ) { Column(Modifier.padding(contentPadding), verticalArrangement = verticalArrangement, content = content) }
    }
}

/** Tinted rounded-square icon holder used by tiles, rows and section headers. */
@Composable
fun IconBadge(
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 24.dp,
    contentDescription: String? = null,
    filled: Boolean = false
) {
    val tint = accentTint(accent)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(if (filled) tint else tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (filled) MaterialTheme.colorScheme.surface else tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Compact launcher tile for grids of features: badge on top, title (and optional subtitle) below.
 * [title] and [subtitle] are English source strings localized here.
 */
@Composable
fun FeatureTile(
    title: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    centered: Boolean = true,
    selected: Boolean = false
) {
    val tint = accentTint(accent)
    AppCard(
        modifier = modifier,
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        containerColor = if (selected) tint.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.md)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            IconBadge(icon = icon, accent = accent, size = 44.dp, filled = selected)
            Column(horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start) {
                LocalizedText(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) LocalizedText(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * A full-width tappable row: badge, title, optional subtitle and trailing content (a chevron by
 * default). Stack several with [groupedShape] for a settings-style group.
 */
@Composable
fun ListRowItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.large,
    localizeTitle: Boolean = true,
    localizeSubtitle: Boolean = true,
    trailing: (@Composable RowScope.() -> Unit)? = if (onClick != null) {
        { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
    } else null
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = shape,
        outlined = false,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                IconBadge(icon = icon, accent = accent, size = 40.dp, iconSize = 22.dp)
                Spacer(Modifier.width(14.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (localizeTitle) localizedUiText(title) else title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) Text(
                    text = if (localizeSubtitle) localizedUiText(subtitle) else subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (trailing != null) {
                Spacer(Modifier.width(Spacing.sm))
                trailing()
            }
        }
    }
}

/** Small rounded label: counts, statuses and tags. [text] is shown as given (localize before passing). */
@Composable
fun TonalPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = contentColor, maxLines = 1)
    }
}

/**
 * Faint eight-pointed-star lattice, the classic Islamic geometric motif. Drawn, not an image, so
 * it stays crisp at any density. Use a low-alpha [color] behind hero content.
 */
fun Modifier.islamicPattern(color: Color, cell: Dp = 36.dp, strokeWidth: Dp = 1.dp): Modifier = drawBehind {
    val step = cell.toPx()
    val radius = step * 0.36f
    val stroke = Stroke(width = strokeWidth.toPx())
    val star = Path()
    var y = 0f
    var row = 0
    while (y < size.height + step) {
        var x = if (row % 2 == 0) 0f else step / 2f
        while (x < size.width + step) {
            star.reset()
            // Two overlapping squares, one turned 45 degrees, make the eight-pointed star.
            for (i in 0 until 8) {
                val angle = Math.toRadians(45.0 * i + 22.5)
                val r = if (i % 2 == 0) radius else radius * 0.72f
                val point = Offset(x + (r * cos(angle)).toFloat(), y + (r * sin(angle)).toFloat())
                if (i == 0) star.moveTo(point.x, point.y) else star.lineTo(point.x, point.y)
            }
            star.close()
            drawPath(star, color, style = stroke)
            x += step
        }
        y += step * 0.86f
        row++
    }
}

/**
 * The deep-emerald hero surface (prayer card, Ramadan, reading resume). Stays dark in both themes
 * so white and gold text read well; carries the geometric pattern and a soft gold glow.
 */
@Composable
fun HeroSurface(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    contentPadding: PaddingValues = PaddingValues(horizontal = Spacing.xl, vertical = 18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val base = modifier
        .clip(shape)
        .then(if (onClick != null) Modifier.pressScale(interaction).clickable(interaction, null, role = Role.Button, onClick = onClick) else Modifier)
        .background(Brush.linearGradient(listOf(HeroGradientTop, HeroGradientMiddle, HeroGradientBottom)))
        .islamicPattern(Color.White.copy(alpha = 0.045f))
        .drawBehind {
            drawCircle(
                Brush.radialGradient(
                    listOf(GoldAccent.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width * 0.95f, 0f),
                    radius = size.height * 0.9f
                ),
                radius = size.height * 0.9f,
                center = Offset(size.width * 0.95f, 0f)
            )
        }
        .padding(contentPadding)
    Column(base, content = content)
}

/** Heading for a block of content inside a page, with an optional trailing text action. */
@Composable
fun PageSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) = SectionHeader(
    title = title,
    actionText = actionText,
    onActionClick = onAction,
    modifier = modifier
)
