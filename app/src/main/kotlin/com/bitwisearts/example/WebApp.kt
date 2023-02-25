package com.bitwisearts.example

import android.app.Application
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import java.util.*

/**
 * The backing [Application] for this example Android app.
 *
 * @author Richard Arriaga
 */
class WebApp: Application()
{
	/**
	 * There is no hook for [Application] termination, so we can't clean up
	 * data in the `localStorage` of the [WebView], so we use this flag to
	 * indicate the app is starting up for the first time so any app data stored
	 * in `localStorage` can be removed on app startup. Using this flag we can
	 * ensure this is only done once.
	 */
	var freshStart = true

	/**
	 * This is a mechanism for passing asynchronous [APIResponse]s captured
	 * when the [WebView] is not the visible screen, such as when scanning a
	 * barcode. Doing it this way isn't particularly great, but it is good
	 * enough for this example. A better solution would be to keep both the
	 * [WebView] and the other views launched to process a request 
	 * (*barcode scan, etc*) under the same composable root, so they can share
	 * a common data object that is remembered or [ViewModel], so that the
	 * screen transition is a recomposition, not a reconfiguration.
	 */
	val responseQueue = LinkedList<APIResponse>()

	override fun onCreate()
	{
		super.onCreate()
		app = this
	}

	companion object
	{
		/**
		 * The running application.
		 */
		lateinit var app: WebApp
			private set
	}
}