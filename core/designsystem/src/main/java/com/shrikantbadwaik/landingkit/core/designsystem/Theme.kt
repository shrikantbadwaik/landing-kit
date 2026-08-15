package com.shrikantbadwaik.landingkit.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Brand accents used for illustrated moments outside Material's semantic colors. */
object LandingKitColors {
    val Ink = Color(0xFF183331)
    val Forest = Color(0xFF164A45)
    val Cream = Color(0xFFFFF9F0)
    val Paper = Color(0xFFFFFDF8)
    val Coral = Color(0xFFF27D59)
    val Sun = Color(0xFFF4C95D)
    val Seafoam = Color(0xFFA9D7CA)
    val Sky = Color(0xFFB8DDE2)
    val Lavender = Color(0xFFD8CFE5)
    val Moss = Color(0xFF78947C)
}

private val Fraunces = FontFamily(
    Font(R.font.fraunces_variable, FontWeight.Normal),
    Font(R.font.fraunces_variable, FontWeight.Medium),
    Font(R.font.fraunces_variable, FontWeight.SemiBold),
    Font(R.font.fraunces_variable, FontWeight.Bold),
)

private val Manrope = FontFamily(
    Font(R.font.manrope_variable, FontWeight.Normal),
    Font(R.font.manrope_variable, FontWeight.Medium),
    Font(R.font.manrope_variable, FontWeight.SemiBold),
    Font(R.font.manrope_variable, FontWeight.Bold),
)

private val LandingKitTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 56.sp,
        lineHeight = 54.sp,
        letterSpacing = (-1.6).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 42.sp,
        lineHeight = 42.sp,
        letterSpacing = (-1).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.6).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 30.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.2.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.3.sp,
    ),
)

private val LightColors = lightColorScheme(
    primary = LandingKitColors.Forest,
    onPrimary = Color.White,
    primaryContainer = LandingKitColors.Seafoam,
    onPrimaryContainer = LandingKitColors.Ink,
    secondary = Color(0xFFA84E35),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCCD),
    onSecondaryContainer = Color(0xFF482016),
    tertiary = Color(0xFF536E58),
    tertiaryContainer = Color(0xFFD6E7D5),
    background = LandingKitColors.Cream,
    onBackground = LandingKitColors.Ink,
    surface = LandingKitColors.Paper,
    onSurface = LandingKitColors.Ink,
    surfaceVariant = Color(0xFFF1E9DE),
    onSurfaceVariant = Color(0xFF64706D),
    outline = Color(0xFFABB4B0),
    outlineVariant = Color(0xFFE1D8CC),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA9D9CD),
    onPrimary = Color(0xFF073733),
    primaryContainer = Color(0xFF174B46),
    onPrimaryContainer = Color(0xFFC5F2E7),
    secondary = Color(0xFFFFB59E),
    onSecondary = Color(0xFF5C1A0B),
    secondaryContainer = Color(0xFF74341F),
    onSecondaryContainer = Color(0xFFFFDBD0),
    tertiary = Color(0xFFB7CFB7),
    onTertiary = Color(0xFF233426),
    tertiaryContainer = Color(0xFF394B3B),
    onTertiaryContainer = Color(0xFFD3EBD2),
    background = Color(0xFF101917),
    onBackground = Color(0xFFE1EAE6),
    surface = Color(0xFF17211F),
    onSurface = Color(0xFFE1EAE6),
    surfaceVariant = Color(0xFF25302E),
    onSurfaceVariant = Color(0xFFBFC9C5),
    outline = Color(0xFF89938F),
    outlineVariant = Color(0xFF3F4946),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE1EAE6),
    inverseOnSurface = Color(0xFF2E3734),
    inversePrimary = LandingKitColors.Forest,
    scrim = Color.Black,
)

private val LandingKitShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(30.dp),
)

@Composable
fun LandingKitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = LandingKitTypography,
        shapes = LandingKitShapes,
        content = content,
    )
}
