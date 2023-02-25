package com.bitwisearts.example

import android.webkit.WebView
import com.bitwisearts.example.barcode.Format
import com.bitwisearts.example.barcode.Type
import org.availlang.json.JSONFriendly
import org.availlang.json.JSONWriter
import org.availlang.json.jsonPrettyPrintWriter

/**
 * The types of messages that represent the requests that can be made of the
 * native Android API from the web app running in the [WebView].
 */
enum class MessageType
{
	/**
	 * A response acknowledging the action that needs no special action.
	 */
	Acknowledgement,

	/**
	 * A response indicating an error occurred while handling the request.
	 */
	Error,

	/**
	 * A response to the web view checking if the app is freshly started or
	 * if the app is just redrawing the web view.
	 */
	CheckFreshStart,

	/**
	 * A response to getting a file from disk.
	 */
	GetFile,

	/**
	 * A response to listing the files on disk.
	 */
	GetFileList,

	/**
	 * A response to a request to scan a barcode.
	 */
	GetBarcode
}

/**
 * A [JSONFriendly] that is a response to a native Android API request.
 *
 * @author Richard Arriaga
 */
abstract class APIResponse: JSONFriendly
{
	/**
	 * The positive integer that represents this instance of [APIResponse].
	 */
	abstract val conversationId: Int

	/**
	 * The [MessageType] of this [APIResponse].
	 */
	abstract val messageType: MessageType

	/**
	 * `true` indicates the response was successful; `false` otherwise.
	 */
	abstract val succeeded: Boolean

	/**
	 * Write this [APIResponse]'s custom payload data to the provided
	 * [JSONWriter].
	 *
	 * @param writer
	 *   The [JSONWriter] to write to.
	 */
	abstract fun writePayload (writer: JSONWriter)

	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			at(::conversationId.name) { write(conversationId) }
			at(::messageType.name) { write(messageType.name) }
			at(::succeeded.name) { write(succeeded) }
			at("payload") { writePayload(this) }
		}
	}

	/**
	 * This [APIResponse] as a JSON string.
	 */
	val json: String get() = jsonPrettyPrintWriter {
		this@APIResponse.writeTo(this)
	}.toString()
}

/**
 * The [MessageType.Acknowledgement] [APIResponse].
 */
@Suppress("unused")
class AcknowledgementResponse constructor(
	override val conversationId: Int
): APIResponse()
{
	override val messageType: MessageType = MessageType.Acknowledgement
	override val succeeded: Boolean = true
	override fun writePayload(writer: JSONWriter)
	{
		// do nothing
	}
}

/**
 * The [MessageType.Error] [APIResponse].
 *
 * @property originatingMessageType
 *   The message type name that represents the original request type
 * @property errorMessage
 *   The error message that describes how the request failed.
 */
class ErrorResponse constructor(
	override val conversationId: Int,
	private val originatingMessageType: String,
	private val errorMessage: String
): APIResponse()
{
	override val messageType: MessageType = MessageType.Error
	override val succeeded: Boolean = false
	override fun writePayload(writer: JSONWriter)
	{
		writer.writeObject {
			at(::originatingMessageType.name) { write(originatingMessageType) }
			at(::errorMessage.name) { write(errorMessage) }
		}
	}
}

/**
 * The [MessageType.CheckFreshStart] [APIResponse].
 *
 * @property freshStart
 *   `true` indicates this is a new load of the app; `false` is a redraw of the
 *   screen.
 */
class CheckFreshStartResponse constructor(
	override val conversationId: Int,
	private val freshStart: Boolean
): APIResponse()
{
	override val messageType: MessageType = MessageType.CheckFreshStart
	override val succeeded: Boolean = true
	override fun writePayload(writer: JSONWriter)
	{
		writer.writeObject {
			at(::freshStart.name) { write(freshStart) }
		}
	}
}

/**
 * The [MessageType.GetFile] [APIResponse].
 *
 * @property fileText
 *   The text of the retrieved file
 */
class GetFileResponse constructor(
	override val conversationId: Int,
	private val fileText: String
): APIResponse()
{
	override val messageType: MessageType = MessageType.GetFile
	override val succeeded: Boolean = true
	override fun writePayload(writer: JSONWriter)
	{
		writer.writeObject {
			at(::fileText.name) { write(fileText) }
		}
	}
}

/**
 * The [MessageType.GetFile] [APIResponse].
 *
 * @property files
 *   The list of [pairs][Pair] of file relative path - boolean, if
 *   `true` indicates the file is a directory, `false` indicates it not a
 *   directory.
 */
class GetFileListResponse constructor(
	override val conversationId: Int,
	private val files: List<Pair<String, Boolean>>
): APIResponse()
{
	override val messageType: MessageType = MessageType.GetFileList
	override val succeeded: Boolean = true
	override fun writePayload(writer: JSONWriter)
	{
		writer.writeObject {
			at(::files.name) {
				writeArray {
					files.forEach {
						writeArray {
							write(it.first)
							write(it.second)
						}
					}
				}
			}
		}
	}
}

/**
 * The [MessageType.GetBarcode] [APIResponse].
 *
 * @property barcode
 *   The scanned content of the barcode.
 * @property format
 *   The barcode [Format].
 * @property type
 *   The barcode [Type].
 */
class GetBarcodeResponse constructor(
	override val conversationId: Int,
	private val barcode: String,
	private val format: String,
	private val type: String
): APIResponse()
{
	override val messageType: MessageType = MessageType.GetBarcode
	override val succeeded: Boolean = true
	override fun writePayload(writer: JSONWriter)
	{
		writer.writeObject {
			at(::barcode.name) { write(barcode) }
			at(::format.name) { write(format) }
			at(::type.name) { write(type) }
		}
	}
}