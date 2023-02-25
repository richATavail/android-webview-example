package com.bitwisearts.example

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle.Event.*
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * The custom [WebView] for this application.
 *
 * @author Richard Arriaga
 *
 * @property model
 *   The active [WebViewModel].
 * @property navController
 *   The [NavController] used for screen navigation.
 */
@SuppressLint("ViewConstructor")
class CustomWebView(
	val model: WebViewModel,
	val navController: NavController, context: Context
) : WebView(context)
{
	/**
	 * The ExampleNativeAndroidAPI that contains the native Android API that can
	 * be called by Javascript running in this [CustomWebView]. These functions
	 * are evaluated on the Android side of the wall.
	 */
	val nativeAndroidAPI: ExampleNativeAndroidAPI = ExampleNativeAndroidAPI(
		CoroutineScope(Dispatchers.Main + SupervisorJob()),
		model,
		this,
		navController)

	/**
	 * Expose the [ExampleNativeAndroidAPI] to this [CustomWebView].
	 */
	internal fun exposeAndroidAPI ()
	{
		// This exposes all the public JavascriptInterface annotated functions
		// in ExampleNativeAndroidAPI to the WebView. The second argument
		// provides the name of the interface in Javascript that the functions
		// are callable through.
		addJavascriptInterface(
			nativeAndroidAPI, ExampleNativeAndroidAPI::class.java.simpleName)
	}
}

/**
 * Answer the configured [WebView] for this application.
 *
 * @param context
 *   The [Context] object used to access application assets.
 * @param url
 *   The url of file to load.
 * @param savedInstanceState
 *   The [Bundle] that is used to restore app state or `null` if no app
 *   state to restore.
 * @param model
 *   The [WebViewModel] that manages the state of the [WebView].
 * @param navController
 *   The [NavController] used for application navigation.
 * @return
 *   The constructed and configured [WebView].
 */
fun appWebView (
	context: Context,
	url: String,
	savedInstanceState: Bundle?,
	model: WebViewModel,
	navController: NavController
) = CustomWebView(model, navController, context).apply {
	layoutParams = ViewGroup.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.MATCH_PARENT)
	webViewClient = webViewClient()

	// The WebView's WebSettings, `WebView.settings`, manages the settings
	// for a WebView. See:
	// https://developer.android.com/reference/android/webkit/WebSettings

	// This tells the WebView to enable Javascript execution. Note it
	// can enable cross site scripting (xss) vulnerabilities
	@SuppressLint("SetJavaScriptEnabled")
	settings.javaScriptEnabled = true

	// This enables Javascript `window.localStorage` to exist and be usable
	// by the app.
	settings.domStorageEnabled = true

	// Establish the WebChromeClient; it enables custom actions on certain
	// events.
	webChromeClient = webChromeClient()

	// This exposes all the public JavascriptInterface annotated
	// functions in ExampleNativeAndroidAPI to the WebView. The second
	// argument provides the name of the interface in Javascript that
	// the functions are callable through.
	exposeAndroidAPI()

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
	if (savedInstanceState !== null)
	{
		restoreState(savedInstanceState)
	}
	else
	{
		loadUrl(url)
	}
}

/**
 * Create a custom [WebViewClient]. It is responsible for most of the
 * actions that occur inside a WebView. For example allows you to
 *  - intercept url requests for special handling
 *  - dictate where a URL is loaded (in WebView or the default browser)
 *  - What to do for certain events like onPageFinished, onReceivedSslError,
 *    etc
 */
private fun webViewClient() = object : WebViewClient()
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
			"https://www.google.com"
		)
		if (isGoogle)
		{
			// Open Google in the default browser instead
			val intent = Intent(Intent.ACTION_VIEW, request.url)
			intent.flags = FLAG_ACTIVITY_NEW_TASK
			if (intent.resolveActivity(WebApp.app.packageManager) != null)
			{
				WebApp.app.startActivity(intent)
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
			Log.e(
				"WebViewExample",
				"System killed the WebView rendering process " +
					"to reclaim memory. Recreating...")
			view.destroy()
			// By this point, the instance variable "webView" is guaranteed
			// to be null, so it's safe to reinitialize it.
			return true
		}
		// Renderer crashed because of an internal error.
		Log.e(
			"WebViewExample",
			"The WebView rendering process crashed!"
		)
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

/**
 * Create a [WebChromeClient]. It captures logging to the that would be
 * written to the browser console and logs it using Android's Log API.
 *
 * It is also capable of performing custom actions when certain events
 * happen.
 */
private fun webChromeClient() = object : WebChromeClient()
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

/**
 * The main [WebView] content wrapped in an [WebViewContent].
 *
 * @param model
 *   The [WebViewModel] used by this application.
 * @param url
 *   The URL to load in the [WebView].
 * @param backHandlerEnabled
 *   The [MutableState] of Boolean that, when `true` indicates the
 */
@Composable
fun WebViewContent (
	model: WebViewModel = viewModel(),
	url: String,
	backHandlerEnabled: MutableState<Boolean>,
	navController: NavController)
{
	// HACK
	// The following is a hack to preserve the WebView state upon a
	// configuration change. At the time of building this app there is no
	// native way available in Compose that preserves WebView state. This
	// workaround was sourced from the conversation in the open issue;
	// https://github.com/google/accompanist/issues/1178.
	var webView: CustomWebView? by remember {
		mutableStateOf(null)
	}
	var savedBundle: Bundle? by rememberSaveable {
		mutableStateOf(null)
	}
	val lifecycle = LocalLifecycleOwner.current.lifecycle
	DisposableEffect(lifecycle) {
		val saveState: (() -> Unit) -> Unit = { then ->
			val bundle = Bundle()
			// If the WebView exists, save its state
			webView?.apply {
				// Save web app state to `window.localStorage`.
				nativeAndroidAPI.jsAPI.saveState()

				// Write the WebView's state to the bundle.
				saveState(bundle)

				// Arbitrary operation to run after data is saved.
				then()
			}
			savedBundle = bundle
		}
		// Add a lifecycle observer to react accordingly to lifecycle events.
		val statePreservingObserver = LifecycleEventObserver { _, event ->
			Log.d("DisposableEffect", "Event occurring: ${event.name}")
			when (event)
			{
				// Need to ensure the state is saved so it can be reloaded
				// ON_RESUME/ON_START
				ON_PAUSE, ON_STOP -> saveState {}

				// Want to save the state as we are not sure why the Activity
				// has been destroyed; it could just be in the background and
				// destroyed by Android to free up resources, but it may come
				// back.
				ON_DESTROY -> saveState {
					// We want to disable/cleanup the Android Native API.
					webView?.removeJavascriptInterface(
						ExampleNativeAndroidAPI::class.java.simpleName)
				}
				// Nothing needed on these events
				ON_CREATE, ON_RESUME, ON_START, ON_ANY -> {}
			}
		}

		lifecycle.addObserver(statePreservingObserver)

		onDispose {
			backHandlerEnabled.value = false
			lifecycle.removeObserver(statePreservingObserver)
		}
	}
	// END HACK

	// While the WebView is active, we want to intercept the back button presses
	// to navigate back on the WebView stack. If there is no where else to go
	// we want to navigate
	BackHandler(backHandlerEnabled.value)
	{
		val wv = webView
		if (wv?.canGoBack() == true)
		{
			wv.goBack()
		}
		else
		{
			// This actually has to run twice before navigating away from the
			// WebView when the WebView back stack is exhausted. Not sure why.
			navController.popBackStack()
		}
	}

	AndroidView(
		factory = {
			appWebView(it, url, savedBundle, model, navController).apply {
				webView = this
			}
		},
		update = {
			savedBundle?.let { bundle ->
				// This is a restore of the WebView so don't load the URL
				it.restoreState(bundle)
			} ?: it.loadUrl(url) // First load of the web app, so need to load URL
		})
}