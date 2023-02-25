package com.bitwisearts.example

import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * The Javascript API exposed via the [WebView].
 *
 * All these functions are expected to either call:
 *  1. Globally eval capable Javascript; or
 *  2. Javascript functions defined in the web app loaded from the assets folder
 *
 * @author Richard Arriaga
 *
 * @property uiScope
 *   The UI [CoroutineScope] that enables running tasks on the UI thread.
 * @property webView
 *   The [WebView] that the web app is running in.
 */
class ExampleJavascriptAPI constructor(
	private val uiScope: CoroutineScope =
		CoroutineScope(Dispatchers.Main + SupervisorJob()),
	private val webView: WebView)
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
	 * An example of asynchronously evaluating Javascript code in the [webView].
	 *
	 * This just runs generic, globally available Javascript, and changes the
	 * color of the text to red.
	 *
	 * Note the Javascript API functions **must** be called on the UI thread.
	 * see: [WebView.evaluateJavascript]
	 */
	@Suppress("unused")
	internal fun evaluateArbitraryJavascriptExample ()
	{
		runOnUiThread {
			webView.evaluateJavascript("document.body.style.color = 'red';")
			{
				runOnUiThread {
					Toast.makeText(
						WebApp.app,
						"I ran JS code in the WebView",
						Toast.LENGTH_LONG
					).show()
				}
			}
		}
	}

	/**
	 * Helper function used to evaluate arbitrary Javascript on the main UI
	 * thread.
	 *
	 * Note the Javascript API functions **must** be called on the UI thread.
	 * see: [WebView.evaluateJavascript]
	 *
	 * @param script
	 *   The String that contains the arbitrary Javascript code.
	 * @param then
	 *   A callback to be invoked when the script execution completes with the
	 *   result of the execution (if any). May be {@code null} if no
	 *   notification of the result is required.
	 */
	private fun evaluateJS (script: String, then: (String) -> Unit = {})
	{
		runOnUiThread {
			webView.evaluateJavascript(script)
			{
				Log.d(
					TAG,
					"Executed JS${it?.let { s -> "($s)"  } ?: ""}:\n" +
						script)
				then(it)
			}
		}
	}

	/**
	 * Instruct the [webView] to save its state to `window.localStorage`.
	 */
	internal fun saveState ()
	{
		evaluateJS("saveState();")
		{
			Log.d("SaveState", "Saving State...")
			runOnUiThread {
				Toast.makeText(
					WebApp.app,
					"Saving WebView state",
					Toast.LENGTH_LONG
				).show()
			}
		}
	}

	/**
	 * Call the Javascript `stringConcat` function defined in
	 * src/main/assets/js/app.js.
	 *
	 * Note the Javascript API functions **must** be called on the UI thread.
	 *
	 * see: [WebView.evaluateJavascript]
	 *
	 * @param v1
	 *   The string to be concatenated on.
	 * @param v2
	 *   The string to append to `v1`.
	 */
	internal fun jsConcat(v1: String, v2: String)
	{
		evaluateJS("stringConcat('${v1}', '${v2}');")
		{
			runOnUiThread {
				Toast.makeText(
					WebApp.app,
					it,
					Toast.LENGTH_LONG
				).show()
			}
		}
	}

	companion object
	{
		/**
		 * The logging tag for [ExampleJavascriptAPI].
		 */
		private val TAG = ExampleJavascriptAPI::class.java.simpleName
	}
}