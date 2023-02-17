package com.bitwisearts.example

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog

/**
 * The singular [AppCompatActivity] that is used to display the web app inside
 * the [WebView].
 */
class MainActivity : AppCompatActivity()
{
	/**
	 * The [WebView] with id `my_web_view` from `layout/activity_main.xml`.
	 */
	internal lateinit var webView: WebView

	/**
	 * The [ExampleNativeAndroidAPI] that contains the native Android API that
	 * can be called by Javascript running in the [webView]. These functions
	 * are evaluated on the Android side of the wall.
	 */
	private val nativeAndroidAPI = ExampleNativeAndroidAPI(this)

	/**
	 * The [ExampleJavascriptAPI] that exposes Javascript API functions that are
	 * evaluated in the [webView] that are callable from the Android side of
	 * the wall.
	 */
	internal lateinit var jsAPI: ExampleJavascriptAPI
		private set

	/**
	 * The [MainViewModel] for this [MainActivity].
	 */
	internal lateinit var model: MainViewModel
		private set

	/**
	 * This is the custom handler for when the back button is pressed.
	 */
	private val onBackPressedCallback = object: OnBackPressedCallback(true)
	{
		override fun handleOnBackPressed()
		{
			// If the WebView is capable of going back, it will
			if (webView.canGoBack()) webView.goBack()
			// Otherwise the app does nothing which is the same behavior as a
			// web browser that has no more pages in its back stack. Some
			// applications attempt to "close" the application calling `finish()`
			// on the Activity, but all this does is "destroys" the activity but
			// doesn't actually close down the application completely. There is
			// no way to fully close an Android application programmatically. We
			// we prevent any further back navigation when the stack is empty.
			// It is up to the user to use the standard Android UI process of
			// closing the app.
		}
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		webView = findViewById(R.id.my_web_view)
		webView.rootView

		val m: MainViewModel by viewModels()
		model = m

		// Enables WebView debugging in Chrome. To debug web app:
		//  1. Connect Android device to computer
		//  2. Open Chrome web browser
		//  3. navigate to `chrome://inspect`
		//  4. Click "inspect" below your listed device.
		WebView.setWebContentsDebuggingEnabled(true)

		// WebViewClient is responsible for most of the actions that occur
		// inside a WebView. For example allows you to
		//  - intercept url requests for special handling
		//  - dictate where a URL is loaded (in WebView or the default browser)
		//  - What to do for certain events like onPageFinished,
		//    onReceivedSslError, etc
		webView.webViewClient = object: WebViewClient()
		{
			// By overriding this with a blanket return of false we are making
			// all web navigation occur in our WebView. If we want some web
			// traffic to open in the default browser we have to add logic to
			// check whether or not we want to open the URL in the default
			// browser by returning true.
			// Consider overriding shouldInterceptRequest to determine how
			// requests should be handled.
			override fun shouldOverrideUrlLoading(
				view: WebView,
				request: WebResourceRequest
			): Boolean
			{
				// we only allow opening the page in the default browser if it
				// is navigating to Google.
				val isGoogle = request.url.toString().startsWith(
					"https://www.google.com")
				if (isGoogle)
				{
					// Open Google in the default browser instead
					val intent = Intent(Intent.ACTION_VIEW, request.url)
					if (intent.resolveActivity(packageManager) != null)
					{
						startActivity(intent)
					}
				}
				return isGoogle
			}

			override fun onRenderProcessGone(
				view: WebView,
				detail: RenderProcessGoneDetail
			): Boolean
			{
				if (!detail.didCrash())
				{
					// Renderer was killed because the system ran out of memory.
					// The app can recover gracefully by creating a new WebView
					// instance in the foreground.
					Log.e("WebViewExample",
						"System killed the WebView rendering process " +
						"to reclaim memory. Recreating...")

					val webViewContainer: ViewGroup =
						findViewById(R.id.web_parent_view)
					webViewContainer.removeView(view)
					view.destroy()

					// By this point, the instance variable "mWebView" is guaranteed
					// to be null, so it's safe to reinitialize it.

					return true // The app continues executing.
				}
				// Renderer crashed because of an internal error.
				Log.e(
					"WebViewExample",
					"The WebView rendering process crashed!")

				// In this example, the app itself crashes after detecting that
				// the renderer crashed. If you choose to handle the crash more
				// gracefully and allow your app to continue executing, you
				// should:
				//   1) destroy the current WebView instance
				//   2) specify logic for how the app can continue executing
				//   3) return "true" instead
				return false

			}
		}

		// This will load the website from the provided URL. This can either
		// be a URL to a webpage available from the internet:
		//   webView.loadUrl("https://www.google.com/")
		// or a file placed in the
		//   - app/src/main/assets folder using:
		//       webView.loadUrl("file:///android_asset/<target-file>")
		//   - app/src/main/res folder using:
		//       webView.loadUrl("file:///android_res/<target-file>").
		// A page can also be created dynamically from a string:
		//   webView.loadDataWithBaseURL(
		//			"file:///android_asset/",
		//			"<html><body>A custom page!</body></html>",
		//			"text/html",
		//			"UTF-8",
		//			null)
		webView.loadUrl("file:///android_asset/index.html")


		// The WebView's WebSettings `webView.settings` manages the settings
		// for a WebView. See:
		// https://developer.android.com/reference/android/webkit/WebSettings

		// This tells the WebView to enable Javascript execution. Note it can
		// enables cross site scripting (xss) vulnerabilities
		@SuppressLint("SetJavaScriptEnabled")
		webView.settings.javaScriptEnabled = true

		// Add the back pressed callback to the dispatcher
		onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

		// Add the Native Android API to the webView
		exposeAndroidAPItoWebView()

		jsAPI = ExampleJavascriptAPI(this)

		// This captures logging to the that would be written to the browser
		// console and logs it using Android's Log API.
		webView.webChromeClient = object: WebChromeClient()
		{
			override fun onConsoleMessage(
				consoleMessage: ConsoleMessage
			): Boolean
			{
				val messageLevel = consoleMessage.messageLevel()
				val logMessage = buildString {
					append("(")
					append(consoleMessage.sourceId())
					append(": ")
					append(messageLevel)
					append(") Line number: ")
					append(consoleMessage.lineNumber())
					append("\n")
					append(consoleMessage.message())
				}
				if (messageLevel == null)
				{
					Log.i("WebViewConsole", logMessage)
					return false
				}
				when (messageLevel)
				{
					ConsoleMessage.MessageLevel.TIP,
					ConsoleMessage.MessageLevel.LOG ->
						Log.i("WebViewConsole", logMessage)
					ConsoleMessage.MessageLevel.WARNING ->
						Log.w("WebViewConsole", logMessage)
					ConsoleMessage.MessageLevel.ERROR ->
						Log.e("WebViewConsole", logMessage)
					ConsoleMessage.MessageLevel.DEBUG ->
						Log.d("WebViewConsole", logMessage)
				}
				return false
			}
		}
	}
	override fun onDestroy()
	{
		// We want to disable/cleanup the Android Native API.
		webView.removeJavascriptInterface(
			ExampleNativeAndroidAPI::class.java.simpleName)
		super.onDestroy()
	}

	/**
	 * An example of asynchronously evaluating Javascript code in the [webView].
	 */
	@Suppress("unused")
	private fun evaluateJS ()
	{
		webView.evaluateJavascript(
			"document.body.style.color = 'red';")
		{
			runOnUiThread {
				Toast.makeText(
					this,
					"I ran JS code in the WebView",
					Toast.LENGTH_LONG
				).show()
			}
		}
	}

	/**
	 * Add the [nativeAndroidAPI] to the [webView] to expose all the public
	 * [JavascriptInterface] annotated functions in [ExampleNativeAndroidAPI]
	 * to the [webView] as an interface that can be used to call the native
	 * Android API.
	 */
	private fun exposeAndroidAPItoWebView ()
	{
		// This exposes all the public JavascriptInterface annotated functions
		// in ExampleNativeAndroidAPI to the WebView. The second argument
		// provides the name of the interface in Javascript that the functions
		// are callable through.
		webView.addJavascriptInterface(
			nativeAndroidAPI, ExampleNativeAndroidAPI::class.java.simpleName)
	}

	/**
	 * This displays an [AlertDialog] confirming the request to send some data
	 *
	 */
	private fun showCloseAppDialog ()
	{
		AlertDialog.Builder(this)
			.setMessage(getString(R.string.close_app_message))
			.setCancelable(false)
			.setPositiveButton(getString(R.string.yes))
			{ _, _ -> finish() }
			.setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
			.create()
			.apply { setTitle(getString(R.string.close_app)) }
			.show()
	}
}