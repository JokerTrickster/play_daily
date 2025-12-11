package com.dailymemo.presentation.theme

import androidx.compose.ui.graphics.Color

// Primary Brand Colors (Blue)
val Primary50 = Color(0xFFE3F2FD)
val Primary100 = Color(0xFFBBDEFB)
val Primary200 = Color(0xFF90CAF9)
val Primary300 = Color(0xFF64B5F6)
val Primary400 = Color(0xFF42A5F5)
val Primary500 = Color(0xFF2196F3) // Main Primary
val Primary600 = Color(0xFF1E88E5)
val Primary700 = Color(0xFF1976D2)
val Primary800 = Color(0xFF1565C0)
val Primary900 = Color(0xFF0D47A1)

// Secondary Colors (Teal/Mint - for accents)
val Secondary50 = Color(0xFFE0F2F1)
val Secondary100 = Color(0xFFB2DFDB)
val Secondary200 = Color(0xFF80CBC4)
val Secondary500 = Color(0xFF009688)
val Secondary700 = Color(0xFF00796B)
val Secondary900 = Color(0xFF004D40)

// Neutral Colors (Grays)
val Neutral50 = Color(0xFFFAFAFA)
val Neutral100 = Color(0xFFF5F5F5)
val Neutral200 = Color(0xFFEEEEEE)
val Neutral300 = Color(0xFFE0E0E0)
val Neutral400 = Color(0xFFBDBDBD)
val Neutral500 = Color(0xFF9E9E9E)
val Neutral600 = Color(0xFF757575)
val Neutral700 = Color(0xFF616161)
val Neutral800 = Color(0xFF424242)
val Neutral900 = Color(0xFF212121)

// Semantic Colors
val Error = Color(0xFFB3261E)
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFFC107)

// Legacy/Specific Colors (Keeping for compatibility if needed, but mapped to new palette where possible)
val MapPrimary = Primary700
val HighRating = Success
val MediumRating = Warning
val LowRating = Color(0xFFF44336)

// Theme Colors
val LightPrimary = Primary500
val LightOnPrimary = Color.White
val LightPrimaryContainer = Primary100
val LightOnPrimaryContainer = Primary900

val LightSecondary = Secondary500
val LightOnSecondary = Color.White
val LightSecondaryContainer = Secondary100
val LightOnSecondaryContainer = Secondary900

val LightBackground = Neutral50
val LightOnBackground = Neutral900
val LightSurface = Color.White
val LightOnSurface = Neutral900
val LightSurfaceVariant = Neutral100
val LightOnSurfaceVariant = Neutral700
val LightOutline = Neutral400
