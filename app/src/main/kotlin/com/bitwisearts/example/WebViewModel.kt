package com.bitwisearts.example

import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The [ViewModel] for web app being presented in the [WebView].
 *
 * @author Richard Arriaga
 */
class WebViewModel: ViewModel()
{
	/**
	 * The [FileManager] used to manage files for this application.
	 */
	private val fileManager = FileManager(WebApp.app)

	/**
	 * Asynchronously gets the files stored in this application's private file
	 * directory.
	 *
	 * @param then
	 *   Accepts the list of [pairs][Pair] of file relative path - boolean, if
	 *   `true` indicates the file is a directory, `false` indicates it not a
	 *   directory.
	 * @param failure
	 *   Accepts a [Throwable] in the event there is a failure.
	 */
	fun listAppDirFiles (
		then: (List<Pair<String, Boolean>>) -> Unit,
		failure: (Throwable) -> Unit)
	{
		viewModelScope.launch(Dispatchers.IO)
		{
			fileManager.listAppDirFiles(then, failure)
		}
	}

	/**
	 * Relative to the application's root file directory, asynchronously read
	 * the target file from disk and provide its contents.
	 *
	 * @param target
	 *   The relative path to the file to retrieve.
	 * @param then
	 *   Accepts the raw [bytes][ByteArray] of the target file.
	 * @param failure
	 *   Accepts a [Throwable] in the event there is a failure.
	 */
	fun getFileContent (
		target: String,
		then: (ByteArray) -> Unit,
		failure: (Throwable) -> Unit)
	{
		viewModelScope.launch(Dispatchers.IO)
		{
			fileManager.getFileContent(target, then, failure )
		}
	}

	/**
	 * Save the content to a file relative to the application's root file
	 * directory.
	 *
	 * @param target
	 *   The relative path to the file to retrieve.
	 * @param content
	 *   The file [contents][ByteArray] to write to disk.
	 * @param then
	 *   Called after the file is written to disk.
	 * @param failure
	 *   Accepts a [Throwable] in the event there is a failure.
	 */
	@Suppress("unused")
	fun saveFile (
		target: String,
		content: ByteArray,
		then: () -> Unit,
		failure: (Throwable) -> Unit)
	{
		viewModelScope.launch(Dispatchers.IO)
		{
			fileManager.saveFile(target, content, then, failure)
		}
	}
}