package com.bitwisearts.example.camera

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect

/**
 * Access the Android photo gallery.
 *
 * @param onImageUri
 *   Accepts a `nullable` Uri to an image that has been selected from
 *   the photo gallery or `null` if no image was selected.
 */
@Composable
fun PhotoGallery (onImageUri: (Uri?) -> Unit)
{
	val activityLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.GetContent(),
		onResult = onImageUri)

	SideEffect {
		activityLauncher.launch("image/*")
	}
}