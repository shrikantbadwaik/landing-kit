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

/** Field journal pigments. Fixed accents always pair with Ink; surfaces use semantic colors. */
object LandingKitColors {
    val Ink = Color(0xFF263329)
    val Forest = Color(0xFF293F35)
    val Cream = Color(0xFFF5EFDF)
    val Paper = Color(0xFFFFF9ED)
    val Coral = Color(0xFFEBA15B)
    val Sun = Color(0xFFF0BB54)
    val Seafoam = Color(0xFFD0D8BA)
    val Sky = Color(0xFFE3DBBF)
    val Lavender = Color(0xFFE3D3B8)
    val Moss = Color(0xFF63735B)
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
        fontSize = 44.sp,
        lineHeight = 46.sp,
        letterSpacing = (-1.2).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.8).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
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
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 25.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
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
    secondary = Color(0xFF8B451E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF7DFC0),
    onSecondaryContainer = Color(0xFF49260D),
    tertiary = Color(0xFF546447),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDFE7CD),
    onTertiaryContainer = Color(0xFF26351D),
    background = LandingKitColors.Cream,
    onBackground = LandingKitColors.Ink,
    surface = LandingKitColors.Paper,
    onSurface = LandingKitColors.Ink,
    surfaceVariant = Color(0xFFECE4D3),
    onSurfaceVariant = Color(0xFF5F6558),
    outline = Color(0xFF777C6C),
    outlineVariant = Color(0xFFD4CDBB),
    surfaceDim = Color(0xFFE6DFCE),
    surfaceBright = LandingKitColors.Paper,
    surfaceContainerLowest = Color(0xFFFFFCF5),
    surfaceContainerLow = Color(0xFFF8F2E5),
    surfaceContainer = Color(0xFFF1EADB),
    surfaceContainerHigh = Color(0xFFECE4D3),
    surfaceContainerHighest = Color(0xFFE6DFCE),
    surfaceTint = LandingKitColors.Forest,
    inverseSurface = LandingKitColors.Ink,
    inverseOnSurface = LandingKitColors.Paper,
    inversePrimary = LandingKitColors.Seafoam,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD0D8BA),
    onPrimary = Color(0xFF24351F),
    primaryContainer = Color(0xFF354C3D),
    onPrimaryContainer = Color(0xFFE0EAD1),
    secondary = Color(0xFFF1B978),
    onSecondary = Color(0xFF472606),
    secondaryContainer = Color(0xFF624020),
    onSecondaryContainer = Color(0xFFFFDFC0),
    tertiary = Color(0xFFC3CEAC),
    onTertiary = Color(0xFF28351D),
    tertiaryContainer = Color(0xFF3E4C31),
    onTertiaryContainer = Color(0xFFDFE7CD),
    background = Color(0xFF171E19),
    onBackground = Color(0xFFF0E9D9),
    surface = Color(0xFF202A23),
    onSurface = Color(0xFFF0E9D9),
    surfaceVariant = Color(0xFF343D32),
    onSurfaceVariant = Color(0xFFCACDBE),
    outline = Color(0xFF969C8A),
    outlineVariant = Color(0xFF4A5143),
    surfaceDim = Color(0xFF171E19),
    surfaceBright = Color(0xFF394239),
    surfaceContainerLowest = Color(0xFF111812),
    surfaceContainerLow = Color(0xFF1C261F),
    surfaceContainer = Color(0xFF202A23),
    surfaceContainerHigh = Color(0xFF2A342C),
    surfaceContainerHighest = Color(0xFF343D32),
    surfaceTint = LandingKitColors.Seafoam,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = LandingKitColors.Paper,
    inverseOnSurface = LandingKitColors.Ink,
    inversePrimary = LandingKitColors.Forest,
    scrim = Color.Black,
)

private val LandingKitShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
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
