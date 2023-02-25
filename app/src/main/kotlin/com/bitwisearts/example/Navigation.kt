package com.bitwisearts.example

import android.webkit.WebView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bitwisearts.example.barcode.Scanner
import com.bitwisearts.example.camera.Camera
import com.bitwisearts.example.camera.CameraView

/**
 * The enumeration of the different [Composable]s that can be navigated to in
 * this example application.
 */
@Suppress("EnumEntryName")
enum class NavRoutes
{
	/** Navigate to the main [WebView]. */
	webView,

	/** Navigate to the [CameraView]. */
	camera,

	/** Navigate to the [Scanner]. */
	scanner
}

/**
 * The [Composable] [NavHost] that manages navigation.
 * 
 * @param modifier
 *   The [Modifier] to be applied to the layout.
 * @param navController
 *   The [NavHostController] for the [NavHost].
 * @param startDestination
 *   The route for the staring [Composable] destination.
 */
@Composable
fun AppNavHost(
	modifier: Modifier = Modifier,
	navController: NavHostController = rememberNavController(),
	startDestination: String = NavRoutes.webView.name
) {
	NavHost(
		modifier = modifier,
		navController = navController,
		startDestination = startDestination
	) {
		composable(NavRoutes.webView.name)
		{
			val backHandlerEnabled =
				remember { mutableStateOf(true) }
			WebViewContent(
				url = "file:///android_asset/index.html",
				navController = navController,
				backHandlerEnabled = backHandlerEnabled)
		}
		composable(NavRoutes.camera.name)
		{
			Camera(navController)
		}

		composable(
			"${NavRoutes.scanner.name}/{callbackId}",
			arguments = listOf(navArgument("callbackId") {
				type = NavType.IntType
			})
		)
		{
			Scanner(navController, it.arguments!!.getInt("callbackId"))
		}
	}
}
