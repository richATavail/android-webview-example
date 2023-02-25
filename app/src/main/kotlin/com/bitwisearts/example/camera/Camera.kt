package com.bitwisearts.example.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.FlipCameraAndroid
import androidx.compose.material.icons.sharp.Lens
import androidx.compose.material.icons.sharp.PhotoLibrary
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.bitwisearts.example.*
import com.bitwisearts.example.R
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * The different types of actions that can be performed on a camera view.
 */
sealed class CameraAction
{
	/** The shutter button has been clicked. */
	object ShutterAction : CameraAction()

	/**
	 * The button to switch to the camera on the opposite side of the device has
	 * been clicked.
	 */
	object SwitchCameraAction : CameraAction()

	/** The button to navigate to image gallery has been clicked. */
	object OpenGalleryAction : CameraAction()
}

/**
 * The [Composable] that draws the camera controls at the bottom of the camera
 * view.
 *
 * @param buttonIconSize
 *   The icon control button size in [Dp] for each control button.
 * @param drawShutterIcon
 *   A [Composable] function that draws the [button][IconButton] that takes a
 *   picture when tapped.
 * @param drawSwitchCameraIcon
 *   A [Composable] function that draws the [button][IconButton] that when
 *   tapped switches the active cameras.
 * @param drawOpenGalleryIcon
 *   A [Composable] function that draw an [button][IconButton] that opens the
 *   photo gallery when tapped.
 * @param cameraActionHandler
 *   The handler that accepts and processes the tapped button's relative
 *   [CameraAction].
 */
@Composable
fun CameraControls (
	buttonIconSize: Dp,
	drawShutterIcon: (@Composable () -> Unit)?,
	drawSwitchCameraIcon: (@Composable () -> Unit)?,
	drawOpenGalleryIcon: (@Composable () -> Unit)?,
	cameraActionHandler: (CameraAction) -> Unit)
{
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.background(Color.Black)
			.padding(14.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically)
	{

		drawSwitchCameraIcon?.let {
			ControlButton(
				buttonIconSize,
				drawIcon = drawSwitchCameraIcon,
				onClick =
				{
					cameraActionHandler(CameraAction.SwitchCameraAction)
				})
		}
		drawShutterIcon?.let {
			ControlButton(
				buttonIconSize,
				modifier = Modifier
					.padding(1.dp)
					.border(1.dp, Color.White, CircleShape),
				drawIcon = drawShutterIcon,
				onClick =
				{
					cameraActionHandler(CameraAction.ShutterAction)

				})
		}
		drawOpenGalleryIcon?.let {
			ControlButton(
				buttonIconSize,
				drawIcon = drawOpenGalleryIcon,
				onClick =
				{
					cameraActionHandler(CameraAction.OpenGalleryAction)
				})
		}
	}
}

/**
 * The [Composable] that draws an arbitrary camera control [button][IconButton].
 *
 * @param buttonIconSize
 *   The button icon size in [Dp].
 * @param modifier
 *   The composable [Modifier] for the outer [IconButton].
 * @param drawIcon
 *   A [Composable] function that draws an icon in the [IconButton].
 * @param onClick
 *   The [IconButton] `onClick` action to perform when the button is tapped.
 */
@Composable
fun ControlButton(
	buttonIconSize: Dp,
	modifier: Modifier = Modifier,
	drawIcon: @Composable () -> Unit,
	onClick: () -> Unit)
{
	IconButton(
		onClick = onClick,
		modifier = modifier.size(buttonIconSize))
	{
		drawIcon()
	}
}

/**
 * Encapsulates the state necessary to draw the camera controls in the camera
 * view.
 *
 * @author Richard Arriaga
 *
 * @property buttonIconSize
 *   The icon control button size in [Dp] for each control button.
 * @property drawShutterIcon
 *   A [Composable] function that draws the [button][IconButton] that takes a
 *   picture when tapped.
 * @property drawSwitchCameraIcon
 *   A [Composable] function that draws the [button][IconButton] that when
 *   tapped switches the active cameras.
 * @property drawOpenGalleryIcon
 *   A [Composable] function that draw an [button][IconButton] that opens the
 *   photo gallery when tapped.
 */
class Controls constructor (
	private val buttonIconSize: Dp,
	private val drawShutterIcon: (@Composable () -> Unit)? =
		{
			Icon(
				Icons.Sharp.Lens,
				contentDescription =
					stringResource(id = R.string.take_pic_desc),
				tint = Color.White)
		},
	private val drawSwitchCameraIcon: (@Composable () -> Unit)? =
		{
			Icon(
				Icons.Sharp.FlipCameraAndroid,
				contentDescription =
				stringResource(id = R.string.switch_camera_desc),
				tint = Color.White)
		},
	private val drawOpenGalleryIcon: (@Composable () -> Unit)? =
		{
			Icon(
				Icons.Sharp.PhotoLibrary,
				contentDescription =
					stringResource(id = R.string.nav_gallery_desc),
				tint = Color.White)
		})
{
	/**
	 * Draw the camera controls to the camera view.
	 *
	 * @param action
	 *   The handler that accepts and processes the tapped button's relative
	 *   [CameraAction].
	 */
	@Composable
	fun DrawControls (action: (CameraAction) -> Unit)
	{
		CameraControls(
			buttonIconSize,
			drawShutterIcon,
			drawSwitchCameraIcon,
			drawOpenGalleryIcon,
			action)
	}
}

/**
 * Utility function on [Context] that retrieves the [ProcessCameraProvider].
 */
suspend fun Context.getProcessCameraProvider (): ProcessCameraProvider =
	suspendCoroutine { continuation ->
		ProcessCameraProvider.getInstance(this).also { provider ->
			provider.addListener(
				{
					@Suppress("BlockingMethodInNonBlockingContext")
					continuation.resume(provider.get())
				},
				ContextCompat.getMainExecutor(this))
		}
	}

/**
 * Draw the camera preview screen.
 *
 * @param imageCapture
 *   The captured [image][ImageCapture].
 * @param activeCameraLens
 *   The integer that represents active camera lens.
 * @param drawControls
 *   A [Composable] function that draws the camera [Controls].
 */
@Composable
private fun CameraPreviewView(
	imageCapture: ImageCapture,
	activeCameraLens: Int = LENS_FACING_BACK,
	drawControls: @Composable () -> Unit)
{
	val localContext = LocalContext.current
	val lifecycleOwner = LocalLifecycleOwner.current

	val photoPreview = Preview.Builder().build()
	val selector = CameraSelector.Builder()
		.requireLensFacing(activeCameraLens)
		.build()

	val preview = remember { PreviewView(localContext) }
	LaunchedEffect(activeCameraLens) {
		val provider = localContext.getProcessCameraProvider()
		provider.unbindAll()
		provider.bindToLifecycle(
			lifecycleOwner,
			selector,
			photoPreview,
			imageCapture
		)
		photoPreview.setSurfaceProvider(preview.surfaceProvider)
	}

	Box(modifier = Modifier.fillMaxSize())
	{
		AndroidView(
			factory = { preview },
			modifier = Modifier.fillMaxSize())
		Column(
			modifier = Modifier.align(Alignment.BottomCenter),
			verticalArrangement = Arrangement.Bottom)
		{
			drawControls()
		}
	}
}

/**
 * Draw a camera view.
 *
 * @param controls
 *   The [Controls] used to control the camera in this view.
 * @param onImageCaptureCallBack
 *   The [ImageCapture.OnImageCapturedCallback] that processes an image.
 * @param onImageLoaded
 *   The retrieved image handler that accepts a [Uri] to the chosen image from
 *   the photo gallery.
 */
@Composable
fun CameraView(
	controls: Controls,
	onImageCaptureCallBack: ImageCapture.OnImageCapturedCallback,
	onImageLoaded: (Uri?) -> Unit)
{
	val localContext = LocalContext.current
	var openGallerySelected by remember { mutableStateOf(false) }
	var activeCameraLens by remember {
		mutableStateOf(LENS_FACING_BACK)
	}
	val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }

	if (openGallerySelected)
	{
		PhotoGallery(
			onImageUri = { uri ->
				openGallerySelected = false
				onImageLoaded(uri)
			})
	}
	else
	{
		CameraPreviewView(imageCapture, activeCameraLens)
		{
			controls.DrawControls { action ->
				when (action)
				{
					is CameraAction.ShutterAction ->
					{
						imageCapture.takePicture(
							ContextCompat.getMainExecutor(localContext),
							onImageCaptureCallBack
						)
					}
					is CameraAction.SwitchCameraAction ->
					{
						activeCameraLens =
							if (activeCameraLens == LENS_FACING_BACK)
							{
								LENS_FACING_FRONT
							}
							else
							{
								LENS_FACING_BACK
							}
					}
					is CameraAction.OpenGalleryAction ->
					{
						openGallerySelected = true
					}
				}
			}
		}
	}
}

/**
 * The [Composable] view that can be navigated to.
 *
 * @param navController
 *   The [NavHostController] used for navigation.
 */
@Composable
fun Camera (navController: NavHostController)
{
	val localContext = LocalContext.current
	var cameraIsOpen by remember { mutableStateOf(false) }
	var cameraAccessGranted by remember { mutableStateOf(false) }
	var lastImage by remember { mutableStateOf<ImageBitmap?>(null) }
	var error by remember { mutableStateOf("") }
	val controls = Controls(64.dp)
	val handler = object : ImageCapture.OnImageCapturedCallback()
	{
		override fun onError(exception: ImageCaptureException)
		{
			cameraIsOpen = false
			error = exception.message ?: "A nondescript error occurred!"
		}

		@SuppressLint("UnsafeOptInUsageError")
		override fun onCaptureSuccess(image: ImageProxy)
		{
			error = ""
			image.image?.let {
				lastImage = it.asImageBitmap()
				error =
					if (lastImage == null)
						"The image format is not supported"
					else ""
			}
			cameraIsOpen = false
		}
	}
	ConditionallyRequestPermission(Manifest.permission.CAMERA)
	{
		cameraAccessGranted = it
	}
	if (cameraIsOpen)
	{
		CameraView(controls, handler)
		{
			if (it != null)
			{
				val source = ImageDecoder
					.createSource(localContext.contentResolver, it)
				lastImage = ImageDecoder.decodeBitmap(source).asImageBitmap()
			}
			else
			{
				error = "The image was not loaded"
			}
			cameraIsOpen = false
		}
	}
	else
	{
		Column(modifier = Modifier.fillMaxSize())
		{
			ConstraintLayout(Modifier.fillMaxSize())
			{
				val (desc, btn, btn2, output, err) = createRefs()
				Text(
					text = stringResource(id = R.string.view_explain),
					modifier = Modifier.constrainAs(desc)
					{
						top.linkTo(parent.top)
						end.linkTo(parent.end)
						start.linkTo(parent.start)
					}
						.padding(8.dp),)
				Button(
					modifier = Modifier.constrainAs(btn)
						{
							top.linkTo(desc.bottom)
							end.linkTo(parent.end)
							start.linkTo(parent.start)
						}
						.padding(8.dp),
					enabled = cameraAccessGranted,
					onClick = {
						if (cameraAccessGranted)
						{
							cameraIsOpen = true
						}
					})
				{
					Text(text = stringResource(R.string.camera))
				}
				Button(
					modifier = Modifier.constrainAs(btn2)
					{
						top.linkTo(btn.bottom)
						end.linkTo(parent.end)
						start.linkTo(parent.start)
					}
						.padding(8.dp),
					onClick = { navController.navigate(NavRoutes.webView.name) })
				{
					Text(text = stringResource(R.string.web_view))
				}
				if (lastImage != null)
				{
					Image(
						bitmap = lastImage!!,
						contentDescription = "",
						modifier = Modifier.size(500.dp)
							.constrainAs(output)
							{
								top.linkTo(btn2.bottom)
								start.linkTo(parent.start)
								end.linkTo(parent.end)
							}
							.padding(15.dp))
				}
				if (error.isNotEmpty())
				{
					Text(
						text = error,
						color = Color.Red,
						textAlign = TextAlign.Center,
						fontSize = 20.sp,
						modifier = Modifier
							.constrainAs(err)
							{
								top.linkTo(btn2.bottom)
								start.linkTo(parent.start)
							}
							.padding(15.dp))
				}
			}
		}
	}
}