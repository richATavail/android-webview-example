package com.bitwisearts.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.bitwisearts.example.ui.ExampleAppTheme

/**
 * The singular [ComponentActivity] that is used to display the web app inside
 * the [WebView].
 */
class MainActivity : ComponentActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		// Enables WebView debugging in Chrome. To debug web app:
		//  1. Connect Android device to computer
		//  2. Open Chrome web browser
		//  3. navigate to `chrome://inspect`
		//  4. Click "inspect" below your listed device.
		WebView.setWebContentsDebuggingEnabled(true)

		// Setup the view using Jetpack Compose.
		setContent {
			// Check to see if we have the appropriate permissions.
			ConditionallyRequestPermission(Manifest.permission.CAMERA)
			{
				if (!it)
				{
					Toast.makeText(
						WebApp.app,
						"Did not grant permission to use camera.",
						Toast.LENGTH_SHORT
					).show()
				}
			}
			ExampleAppTheme {
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colors.background)
				{
					AppNavHost()
				}
			}
		}
	}
}

/**
 * Conditionally request an app [permission][Manifest.permission].
 *
 * @param permission
 *   The String representation of the permission to request.
 * @param resultHandler
 *   Accepts `true` if the permission is granted; `false` if not.
 */
@Composable
fun ConditionallyRequestPermission (
	permission: String,
	resultHandler: (Boolean) -> Unit)
{
	val permissionFlag =
		ContextCompat.checkSelfPermission(WebApp.app, permission)
	if (permissionFlag != PackageManager.PERMISSION_GRANTED)
	{
		val launcher = rememberLauncherForActivityResult(
			ActivityResultContracts.RequestPermission(), resultHandler)
		SideEffect {
			launcher.launch(permission)
		}
	}
	else
	{
		resultHandler(true)
	}
}