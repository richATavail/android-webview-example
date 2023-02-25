package com.bitwisearts.example

import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.bitwisearts.example.camera.Camera
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Contains the native Android API callable from a [WebView].
 *
 * @author Richard Arriaga
 *
 * @property uiScope
 *   The UI [CoroutineScope] that enables running tasks on the UI thread.
 * @property model
 *   The [WebViewModel] for the [MainActivity].
 * @property webView
 *   The [WebView] that the web app is running in.
 * @property navController
 *   The [NavController] used for application navigation.
 */
class ExampleNativeAndroidAPI constructor(
	private val uiScope: CoroutineScope =
		CoroutineScope(Dispatchers.Main + SupervisorJob()),
	private val model: WebViewModel,
	private val webView: WebView,
	private val navController: NavController)
{
	/**
	 * Run a process on the main UI thread.
	 *
	 * @param action
	 *   The action to run on the UI thread.
	 */
	private fun runOnUiThread(action: suspend () -> Unit) =
		uiScope.launch { action() }

	/**
	 * The [ExampleJavascriptAPI] that exposes Javascript API functions that are
	 * evaluated in the [webView] that are callable from the Android side of
	 * the wall.
	 */
	internal val jsAPI: ExampleJavascriptAPI =
		ExampleJavascriptAPI(uiScope, webView)

	/**
	 * Send the [APIResponse] to the [webView].
	 *
	 * @param resp
	 *   The [APIResponse] to send.
	 */
	private fun sendResponse (resp: APIResponse)
	{
		runOnUiThread {
			webView.evaluateJavascript(
				"resolveConversation(${resp.json});")
			{
				// Do nothing
			}
		}
	}

	/**
	 * Allows the [webView] know that it is ready to receive any delayed
	 * responses to async requests that were added to [WebApp.responseQueue]
	 * while the [webView] was unavailable.
	 */
	@JavascriptInterface
	@Suppress("unused")
	fun getAsyncResponses ()
	{
		var next = WebApp.app.responseQueue.poll()
		while (next !== null)
		{
			sendResponse(next)
			next = WebApp.app.responseQueue.poll()
		}
	}

	/**
	 * Process the request to see whether the app is
	 * [freshly started][CheckFreshStartResponse.freshStart].
	 *
	 * @param conversationId
	 *   The [APIResponse.conversationId] that uniquely identifies this
	 *   request.
	 */
	@JavascriptInterface
	@Suppress("unused")
	fun checkFreshStart (conversationId: Int)
	{
		val freshStart =
			if (WebApp.app.freshStart)
			{
				WebApp.app.freshStart = false
				true
			}
			else
			{
				false
			}

		sendResponse(CheckFreshStartResponse(conversationId, freshStart))
	}

	/**
	 * A [JavascriptInterface] annotated function that is exposed as an API that
	 * is callable from a [WebView].
	 *
	 * This API function launches a [Toast] using Android's native toasting
	 * mechanism.
	 *
	 * @param toast
	 *   The message to toast.
	 */
	// This annotation makes this function callable from JS running in a WebView
	// iff it is added to the WebView using WebView.addJavascriptInterface.
	@JavascriptInterface
	@Suppress("unused")
	fun displayToast (toast: String)
	{
		runOnUiThread {
			Toast.makeText(WebApp.app, toast, Toast.LENGTH_LONG).show()
		}
	}

	/**
	 * Calls an API defined in the Javascript, [ExampleJavascriptAPI.jsConcat].
	 *
	 * This demonstrates how a native Android code can call a function defined
	 * in the Javascript in the web app running in the [WebView].
	 *
	 * @param v1
	 *   The string to be concatenated on.
	 * @param v2
	 *   The string to append to `v1`.
	 */
	@JavascriptInterface
	@Suppress("unused")
	fun roundTripConcat (v1: String, v2: String)
	{
		jsAPI.jsConcat(v1, v2)
	}

	/**
	 * Retrieve the list of files stored on disk.
	 *
	 * @param conversationId
	 *   The [APIResponse.conversationId] that uniquely identifies this
	 *   request.
	 */
	@JavascriptInterface
	@Suppress("unused")
	fun getFileList (conversationId: Int)
	{
		model.listAppDirFiles(
			{
				sendResponse(GetFileListResponse(conversationId, it))
			}
		){
			reportError(conversationId, MessageType.GetFileList, it)
		}
	}

	/**
	 * Retrieve the target file stored on disk.
	 *
	 * @param conversationId
	 *   The [APIResponse.conversationId] that uniquely identifies this
	 *   request.
	 */
	@JavascriptInterface
	@Suppress("unused")
	fun getFile (conversationId: Int, target: String)
	{
		model.getFileContent(
			target,
			{
				sendResponse(GetFileResponse(conversationId, String(it)))
			}
		){
			reportError(conversationId, MessageType.GetFile, it)
		}
	}

	/**
	 * Open the [Composable] [Camera] view, navigating away from the [webView].
	 */
	@JavascriptInterface
	@Suppress("unused")
	fun openCameraView ()
	{
		runOnUiThread {
			navController.navigate(NavRoutes.camera.name)
		}
	}

	/**
	 * Open the [Composable] [Camera] view, navigating away from the [webView].
	 */
	@JavascriptInterface
	@Suppress("unused")
	fun openScanner (conversationId: Int)
	{
		runOnUiThread {
			navController.navigate(
				"${NavRoutes.scanner.name}/${conversationId}")
		}
	}

	/**
	 * Respond to the API request with an [ErrorResponse].
	 *
	 * @param conversationId
	 *   The [APIResponse.conversationId] that uniquely identifies this
	 *   request.
	 * @param messageType
	 *   The originating [MessageType] that resulted in the [MessageType.Error].
	 * @param e
	 *   The [Throwable] exception.
	 */
	private fun reportError(
		conversationId: Int,
		messageType: MessageType,
		e: Throwable)
	{
		sendResponse(
			ErrorResponse(
				conversationId, e.stackTraceToString(), messageType.name))
	}
}