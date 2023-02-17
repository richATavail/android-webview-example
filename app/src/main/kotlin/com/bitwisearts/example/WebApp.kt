package com.bitwisearts.example

import android.app.Application

/**
 * The backing [Application] for this example Android app.
 *
 * @author Richard Arriaga
 */
class WebApp: Application()
{
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