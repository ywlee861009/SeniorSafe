package com.kero.anbu.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge  = TextStyle(fontSize = 60.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
    headlineMedium= TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    bodyLarge     = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Normal),
    bodyMedium    = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodySmall     = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    labelSmall    = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
)
