package com.bitwisearts.example.barcode

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.FlipCameraAndroid
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.bitwisearts.example.*
import com.bitwisearts.example.camera.CameraAction
import com.bitwisearts.example.camera.ControlButton
import com.bitwisearts.example.camera.getProcessCameraProvider
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Draw a view used to scan a [Barcode].
 *
 * @param buttonSize
 *   The size in [Dp] of the button.
 * @param controlBarColor
 *   The of the control bar's background [Color].
 * @param options
 *   [BarcodeScannerOptions] used to restrict scanner if specified; `null`
 *   otherwise.
 * @param handler
 *   The [ScanHandler] that processes the [Barcode] scan attempt.
 * @param drawIcon
 *   A [Composable] function that draws the button icon.
 */
@Composable
@Suppress("unused")
fun ScannerView (
	buttonSize: Dp = 64.dp,
	controlBarColor: Color = Color.Black,
	options: BarcodeScannerOptions? = null,
	handler: ScanHandler,
	drawIcon: @Composable () -> Unit = {
		Icon(
			Icons.Sharp.FlipCameraAndroid,
			contentDescription = "",
			tint = Color.White)
	})
{
	var cameraAccessGranted by remember { mutableStateOf(false) }
	ConditionallyRequestPermission(Manifest.permission.CAMERA)
	{
		cameraAccessGranted = it
	}
	var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
	ScannerPreviewView(
		handler,
		controlBarColor,
		buttonSize,
		lensFacing,
		options,
		drawIcon)
	{
		when (it)
		{
			is CameraAction.SwitchCameraAction ->
			{
				lensFacing =
					if (lensFacing == CameraSelector.LENS_FACING_BACK)
					{
						CameraSelector.LENS_FACING_FRONT
					}
					else
					{
						CameraSelector.LENS_FACING_BACK
					}
			}
			else -> {}
		}
	}
}

/**
 * Draws the preview screen of the active scanner.
 *
 * @param handler
 *   The [ScanHandler] that handles the result of the barcode scan attempt.
 * @param controlBarColor
 *   The control bar's background [Color].
 * @param buttonSize
 *   The size in [Dp] of the button.
 * @param lensFacing
 *   The camera lens that is currently active.
 * @param options
 *   [BarcodeScannerOptions] used to restrict scanner if specified; `null`
 *   otherwise.
 * @param drawIcon
 *   A [Composable] function used to draw the button icon.
 * @param action
 *   The [CameraAction] to execute when the button is pressed.
 */
@Composable
fun ScannerPreviewView (
	handler: ScanHandler,
	controlBarColor: Color,
	buttonSize: Dp,
	lensFacing: Int = CameraSelector.LENS_FACING_BACK,
	options: BarcodeScannerOptions?,
	drawIcon: @Composable () -> Unit,
	action: (CameraAction) -> Unit)
{
	val localContext = LocalContext.current
	val lifecycleOwner = LocalLifecycleOwner.current
	val scanPreview = Preview.Builder().build()
	val selector = CameraSelector.Builder()
		.requireLensFacing(lensFacing)
		.build()
	val preview = remember { PreviewView(localContext) }
	LaunchedEffect(lensFacing) {
		val imageAnalysis = ImageAnalysis.Builder()
			.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
			.build()
		val provider = localContext.getProcessCameraProvider()
		provider.unbindAll()
		imageAnalysis.setAnalyzer(
			ContextCompat.getMainExecutor(localContext),
			BarcodeAnalyzer(
				options,
				object: ScanHandler
				{
					override fun onScanned(barcode: Barcode)
					{
						provider.unbindAll()
						handler.onScanned(barcode)
					}

					override fun onError(exception: Throwable)
					{
						handler.onError(exception)
					}
				})
		)
		provider.bindToLifecycle(
			lifecycleOwner,
			selector,
			scanPreview,
			imageAnalysis)
		scanPreview.setSurfaceProvider(preview.surfaceProvider)
	}

	Box(modifier = Modifier.fillMaxSize()) {
		AndroidView({ preview }, modifier = Modifier.fillMaxSize()) {

		}
		Column(
			modifier = Modifier.align(Alignment.BottomCenter),
			verticalArrangement = Arrangement.Bottom
		) {
			ScannerControls(
				controlBarColor,
				buttonSize,
				drawIcon,
				action)
		}

	}
}

/**
 * Draws control bar for the scanner.
 *
 * @param controlBarColor
 *   The control bar's background [Color].
 * @param buttonSize
 *   The size in [Dp] of the scan button.
 * @param drawIcon
 *   A [Composable] function that draws the icon to perform the scan.
 * @param scanAction
 *   The [CameraAction] to execute when the scan icon button is tapped.
 */
@Composable
fun ScannerControls(
	controlBarColor: Color,
	buttonSize: Dp,
	drawIcon: @Composable () -> Unit,
	scanAction: (CameraAction) -> Unit)
{
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.background(controlBarColor)
			.padding(16.dp),
		horizontalArrangement = Arrangement.Start,
		verticalAlignment = Alignment.CenterVertically)
	{
		ControlButton(
			buttonSize,
			modifier= Modifier
				.size(64.dp)
				.padding(1.dp)
				.border(1.dp, Color.White, CircleShape),
			drawIcon,
			onClick = { scanAction(CameraAction.ShutterAction) })
	}
}

/**
 * The [Composable] view that can be navigated to.
 *
 * @param navController
 *   The [NavHostController] used for navigation.
 * @param conversationId
 *   The callbackId of the request that triggered this screen.
 */
@Composable
fun Scanner(navController: NavHostController, conversationId: Int)
{
	var error by remember { mutableStateOf("") }
	var format by remember { mutableStateOf(" - ") }
	var type by remember { mutableStateOf(" - ") }
	var cameraIsOpen by remember { mutableStateOf(false) }
	var lastScanValue by remember { mutableStateOf(" - ") }
	val handler = object : ScanHandler
	{
		override fun onScanned(barcode: Barcode)
		{
			error = ""
			barcode.rawValue?.let { lastScanValue = it }
			format = barcode.imageFormat.javaClass.simpleName
			type = barcode.type.javaClass.simpleName
			cameraIsOpen = false
			WebApp.app.responseQueue.add(
				GetBarcodeResponse(
				conversationId,
					lastScanValue,
					format,
					type))
			navController.popBackStack()
		}

		override fun onError(exception: Throwable)
		{
			cameraIsOpen = false
			error = exception.message ?: "Error!"
			WebApp.app.responseQueue.add(ErrorResponse(
				conversationId, MessageType.GetBarcode.name, error))
			navController.popBackStack()
		}
	}
	ScannerView(handler = handler)
}