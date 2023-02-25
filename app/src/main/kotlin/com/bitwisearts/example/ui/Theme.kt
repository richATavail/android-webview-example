package com.bitwisearts.example.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colors used by theme. These are provided as default on project creation.
val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)

/** [Shapes] used by theme. This is provided as default on project creation. */
val Shapes = Shapes(
	small = RoundedCornerShape(4.dp),
	medium = RoundedCornerShape(4.dp),
	large = RoundedCornerShape(0.dp))

/**
 * Color palette for dark mode.
 */
private val DarkColorPalette = darkColors(
	primary = Purple200,
	primaryVariant = Purple700,
	secondary = Teal200)

/**
 * Color palette for light mode.
 */
private val LightColorPalette = lightColors(
	primary = Purple500,
	primaryVariant = Purple700,
	secondary = Teal200)

/** Basic Set of Material [Typography] styles. */
val Typography = Typography(
	body1 = TextStyle(
		fontFamily = FontFamily.Default,
		fontWeight = FontWeight.Normal,
		fontSize = 16.sp),
	button = TextStyle(
		fontFamily = FontFamily.Default,
		fontWeight = FontWeight.W500,
		fontSize = 14.sp)
)

/**
 * The [Composable] theme for this application.
 *
 * @param darkTheme
 *   `true` indicates the app should be presented in dark mode; `false`
 *   indicates the app should be presented in light mode.
 * @param content
 *   The [Composable] function that draws content to the screen.
 */
@Composable
fun ExampleAppTheme (
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit)
{
	MaterialTheme(
		colors = if (darkTheme) DarkColorPalette else LightColorPalette,
		typography = Typography,
		shapes = Shapes,
		content = content)
}