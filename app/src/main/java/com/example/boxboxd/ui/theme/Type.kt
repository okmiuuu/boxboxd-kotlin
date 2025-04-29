package com.example.boxboxd.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.boxboxd.R

val Formula1FontFamily = FontFamily(
    Font(R.font.formula1regular, FontWeight.Normal),
    Font(R.font.formula1bold, FontWeight.Bold),
    Font(R.font.formula1wide, FontWeight.ExtraBold),
)

fun createTypography(isDarkTheme: Boolean): Typography {
    return Typography(
        bodySmall = TextStyle(
            fontFamily = Formula1FontFamily,
            fontWeight = FontWeight.Normal,
            color = textColor(isDarkTheme),
            fontSize = 14.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = Formula1FontFamily,
            fontWeight = FontWeight.Normal,
            color = textColor(isDarkTheme),
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = Formula1FontFamily,
            fontWeight = FontWeight.Normal,
            color = textColor(isDarkTheme),
            fontSize = 20.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        titleSmall = TextStyle(
            fontFamily = Formula1FontFamily,
            fontWeight = FontWeight.Bold,
            color = textColor(isDarkTheme),
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.5.sp
        ),
        titleMedium = TextStyle(
            fontFamily = Formula1FontFamily,
            fontWeight = FontWeight.Bold,
            color = textColor(isDarkTheme),
            fontSize = 20.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        titleLarge = TextStyle(
            fontFamily = Formula1FontFamily,
            fontWeight = FontWeight.ExtraBold,
            color = textColor(isDarkTheme),
            fontSize = 20.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
    )
}