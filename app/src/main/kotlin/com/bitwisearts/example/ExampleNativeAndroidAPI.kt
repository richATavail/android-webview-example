package com.bitwisearts.example

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast

/**
 * Contains the native Android API callable from a [WebView].
 *
 * @author Richard Arriaga
 *
 * @property activity
 *   The owning [MainActivity]. This may not be necessary depending on what this
 *   interface is expected to do. It may be the case that a [Context] will do
 *   or it might not be necessary at all.
 */
class ExampleNativeAndroidAPI constructor(private val activity: MainActivity)
{
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
		activity.runOnUiThread {
			Toast.makeText(activity, toast, Toast.LENGTH_LONG).show()
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
		activity.jsAPI.jsConcat(v1, v2)
	}

	/**
	 * Retrieve the list of files stored on disk.
	 *
	 * @param conversationId
	 *   The [APIJsonResponse.conversationId] that uniquely identifies this
	 *   request.
	 */
	@JavascriptInterface
	@Suppress("unused")
	fun getFileList (conversationId: Int)
	{
		activity.model.listAppDirFiles(
			{
				val resp = GetFileListResponse(conversationId, it)
				activity.runOnUiThread {
					activity.webView.evaluateJavascript(
						"resolveConversation('${conversationId}', '${resp.json}');")
					{
						// Do nothing
					}
				}
			}
		){
			reportError(conversationId, it)
		}
	}

	/**
	 * Retrieve the target file stored on disk.
	 *
	 * @param conversationId
	 *   The [APIJsonResponse.conversationId] that uniquely identifies this
	 *   request.
	 */
	@JavascriptInterface
	@Suppress("unused")
	fun getFile (conversationId: Int, target: String)
	{
		activity.model.getFileContent(
			target,
			{
				val text = String(it)
				val resp = GetFileResponse(conversationId, text)
				activity.runOnUiThread {
					val request =
						"resolveConversation('${conversationId}', ${resp.json});"
					activity.webView.evaluateJavascript(request)
					{
						// Do nothing
					}
				}
			}
		){
			reportError(conversationId, it)
		}
	}

	/**
	 * Respond to the API request with an [ErrorResponse].
	 *
	 * @param conversationId
	 *   The [APIJsonResponse.conversationId] that uniquely identifies this
	 *   request.
	 * @param e
	 *   The [Throwable] exception.
	 */
	private fun reportError (conversationId: Int, e: Throwable)
	{
		val resp = ErrorResponse(conversationId, e.stackTraceToString())
		activity.runOnUiThread {
			activity.webView.evaluateJavascript(
				"resolveConversation('${conversationId}', '${resp.json}');")
			{
				// Do nothing
			}
		}
	}
}