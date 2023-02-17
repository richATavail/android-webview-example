package com.bitwisearts.example

import android.content.Context
import android.webkit.WebView
import android.widget.Toast

/**
 * The Javascript API exposed via the [WebView].
 *
 * All these functions are expected to either call:
 *  1. Globally eval capable Javascript; or
 *  2. Javascript functions defined in the web app loaded from the assets folder
 *
 * @author Richard Arriaga
 *
 * @property activity
 *   The owning [MainActivity]. This may not be necessary depending on what this
 *   interface is expected to do. It may be the case that a [Context] will do
 *   or it might not be necessary at all.
 */
class ExampleJavascriptAPI constructor(
	private val activity: MainActivity)
{
	/** The [WebView] that the web app is running in. */
	private val webView: WebView get() = activity.webView

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
	internal fun evaluateArbitraryJS ()
	{
		activity.runOnUiThread {
			webView.evaluateJavascript("document.body.style.color = 'red';")
			{
				activity.runOnUiThread {
					Toast.makeText(
						activity,
						"I ran JS code in the WebView",
						Toast.LENGTH_LONG
					).show()
				}
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
		// Note the Javascript API functions **must** be called on the UI thread.
		activity.runOnUiThread {
			webView.evaluateJavascript("stringConcat('${v1}', '${v2}');")
			{
				activity.runOnUiThread {
					Toast.makeText(
						activity,
						it,
						Toast.LENGTH_LONG
					).show()
				}
			}
		}
	}
}