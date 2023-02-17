package com.bitwisearts.example

import java.io.File

/**
 * A class that provides simple wrappers for device file management.
 *
 * @author Richard Arriaga
 *
 * @constructor
 * Construct the [FileManager]
 */
class FileManager constructor(app: WebApp)
{
	/**
	 * The root file directory where application files can be stored.
	 */
	private val appRootDir = app.filesDir.apply { mkdirs() }

	init
	{
		val sf = File(appRootDir, sampleFileName)
		if (!sf.exists())
		{
			sf.writeText(sampleFileText)
		}
	}

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
	suspend fun listAppDirFiles (
		then: (List<Pair<String, Boolean>>) -> Unit,
		failure: (Throwable) -> Unit)
	{
		try
		{
			then(appRootDir.listFiles()?.map {
				Pair(it.toRelativeString(appRootDir), it.isDirectory)
			} ?: emptyList())
		}
		catch (e: Throwable)
		{
			failure(e)
		}
	}

	/**
	 * Asynchronously read the target file from disk and provide its contents.
	 *
	 * @param target
	 *   The [appRootDir] relative path to the file to retrieve.
	 * @param then
	 *   Accepts the raw [bytes][ByteArray] of the target file.
	 * @param failure
	 *   Accepts a [Throwable] in the event there is a failure.
	 */
	suspend fun getFileContent (
		target: String,
		then: (ByteArray) -> Unit,
		failure: (Throwable) -> Unit)
	{
		try
		{
			then(File(appRootDir, target).readBytes())
		}
		catch (e: Throwable)
		{
			failure(e)
		}
	}

	/**
	 * Save the content to a file relative to the [appRootDir].
	 *
	 * @param target
	 *   The [appRootDir] relative path to the file to retrieve.
	 * @param content
	 *   The file [contents][ByteArray] to write to disk.
	 * @param then
	 *   Called after the file is written to disk.
	 * @param failure
	 *   Accepts a [Throwable] in the event there is a failure.
	 */
	suspend fun saveFile (
		target: String,
		content: ByteArray,
		then: () -> Unit,
		failure: (Throwable) -> Unit)
	{
		try
		{
			File(appRootDir, target).writeBytes(content)
			then()
		}
		catch (e: Throwable)
		{
			failure(e)
		}
	}

	companion object
	{
		/**
		 * The name of the sample file we will write to disk.
		 */
		const val sampleFileName = "sample-file.txt"

		/**
		 * Just some random text to put in a the [sampleFileName].
		 */
		const val sampleFileText =
			"Here is some text for our text file stored on disk."
	}
}