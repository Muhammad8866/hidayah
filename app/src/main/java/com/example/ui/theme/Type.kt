package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

/** Bundled IndoPak Nastaleeq face for Arabic/Urdu headings and labels. */
val ArabicFontFamily = FontFamily(Font(R.font.indopak_nastaleeq))

private fun style(size: Int, line: Int, weight: FontWeight, tracking: Double = 0.0) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp
)

// Slightly generous line heights keep Devanagari, Bengali and Urdu scripts from clipping.
val Typography = Typography(
    displayLarge = style(52, 60, FontWeight.Bold, -0.5),
    displayMedium = style(42, 50, FontWeight.Bold, -0.25),
    displaySmall = style(34, 42, FontWeight.Bold),
    headlineLarge = style(30, 38, FontWeight.Bold),
    headlineMedium = style(26, 34, FontWeight.Bold),
    headlineSmall = style(23, 31, FontWeight.Bold),
    titleLarge = style(21, 29, FontWeight.SemiBold),
    titleMedium = style(17, 25, FontWeight.SemiBold, 0.1),
    titleSmall = style(15, 22, FontWeight.SemiBold, 0.1),
    bodyLarge = style(16, 25, FontWeight.Normal, 0.2),
    bodyMedium = style(14, 22, FontWeight.Normal, 0.2),
    bodySmall = style(12, 18, FontWeight.Normal, 0.3),
    labelLarge = style(14, 20, FontWeight.SemiBold, 0.1),
    labelMedium = style(12, 17, FontWeight.Medium, 0.4),
    labelSmall = style(11, 16, FontWeight.Medium, 0.4)
)
