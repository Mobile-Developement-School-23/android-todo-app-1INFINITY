package ru.mirea.ivashechkinav.todo.presentation.fragments

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


val black = Color(0xFF000000)
val white = Color(0xFFFFFFFF)

//Light theme colors
val redLight = Color(0xFFFF3B30)
val greenLight = Color(0xFF34C759)
val blueLight = Color(0xFF007AFF)
val lightBlueLight = Color(0xFF4D007AFF)
val grayLightPalette = Color(0xFF8E8E93)
val grayLightLight = Color(0xFFD1D1D6)

val separatorLight = Color(0x33000000)
val overlayLight = Color(0x0F000000)
val colorPrimaryLight = black
val colorTertiaryLight = Color(0x4D000000)
val colorAccentLight = blueLight
val colorDisableLight = Color(0x26000000)
val colorBackgroundPrimaryLight = Color(0xFFF7F6F2)
val colorBackgroundSecondaryLight = white
val colorBackgroundElevatedLight = white

//Dark theme colors
val redDark = Color(0xFFFF453A)
val greenDark = Color(0xFF32D74B)
val blueDark = Color(0xFF0A84FF)
val lightBlueDark = Color(0xFF4D0A84FF)
val grayDark = Color(0xFF8E8E93)
val grayLightDark = Color(0xFF48484A)

val separatorDark = Color(0x33FFFFFF)
val overlayDark = Color(0x52000000)
val colorPrimaryDark = white
val colorTertiaryDark = Color(0x66FFFFFF)
val colorAccentDark = blueDark
val colorDisableDark = Color(0x26FFFFFF)
val colorBackgroundPrimaryDark = Color(0xFF161618)
val colorBackgroundSecondaryDark = Color(0xFF252528)
val colorBackgroundElevatedDark = Color(0xFF3C3C3F)

val Colors.red: Color get() = if (isLight) redLight else redDark
val Colors.green: Color get() = if (isLight) greenLight else greenDark
val Colors.blue: Color get() = if (isLight) blueLight else blueDark
val Colors.lightBlue: Color get() = if (isLight) lightBlueLight else lightBlueDark
val Colors.separator: Color get() = if (isLight) separatorLight else separatorDark
val Colors.gray: Color get() = if (isLight) grayLightPalette else grayDark
val Colors.grayLight: Color get() = if (isLight) grayLightLight else grayLightDark
val Colors.overlay: Color get() = if (isLight) overlayLight else overlayDark
val Colors.disabled: Color get() = if (isLight) colorDisableLight else colorDisableDark
val Colors.tertiary: Color get() = if (isLight) colorTertiaryLight else colorTertiaryDark
val Colors.elevated: Color get() = if (isLight) colorBackgroundElevatedLight else colorBackgroundElevatedDark

private val LightColors = lightColors(
    primary = colorPrimaryLight,
    primaryVariant = colorPrimaryLight,
    secondary = colorAccentLight,
    background = colorBackgroundPrimaryLight,
    surface = colorBackgroundSecondaryLight,
)

private val DarkColors = darkColors(
    primary = colorPrimaryDark,
    primaryVariant = colorPrimaryDark,
    secondary = colorAccentDark,
    background = colorBackgroundPrimaryDark,
    surface = colorBackgroundSecondaryDark,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable ()
    -> Unit,
) {
    val colors = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }
    CompositionLocalProvider() {
        MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}



val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    h1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp
    ),
    subtitle1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    h2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 32.sp
    ),
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
        lineHeight = 24.sp
    ),
)
val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp),
)