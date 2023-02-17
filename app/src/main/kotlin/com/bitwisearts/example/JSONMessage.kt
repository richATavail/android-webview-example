package com.bitwisearts.example

import org.availlang.json.JSONFriendly
import org.availlang.json.JSONWriter
import org.availlang.json.jsonPrettyPrintWriter

enum class MessageType
{
	/**
	 * A response acknowledging the action that needs no special action.
	 */
	AcknowledgementResponse,

	/**
	 * A response indicating an error occurred while handling the request.
	 */
	ErrorResponse,

	/**
	 * A response to getting a file from disk.
	 */
	GetFileResponse,

	/**
	 * A response to listing the files on disk.
	 */
	GetFileListResponse
}

/**
 * A [JSONFriendly] that is a response to a native Android API request.
 *
 * @author Richard Arriaga
 */
abstract class APIJsonResponse: JSONFriendly
{
	/**
	 * The positive integer that represents this instance of [APIJsonResponse].
	 */
	abstract val conversationId: Int

	/**
	 * The [MessageType] of this [APIJsonResponse].
	 */
	abstract val messageType: MessageType

	/**
	 * `true` indicates the response was successful; `false` otherwise.
	 */
	abstract val succeeded: Boolean

	/**
	 * Write this [APIJsonResponse]'s custom payload data to the provided
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
	 * This [APIJsonResponse] as a JSON string.
	 */
	val json: String get() = jsonPrettyPrintWriter {
		this@APIJsonResponse.writeTo(this)
	}.toString()
}

/**
 * The [MessageType.AcknowledgementResponse] [APIJsonResponse].
 */
class AcknowledgementResponse constructor(
	override val conversationId: Int
): APIJsonResponse()
{
	override val messageType: MessageType = MessageType.AcknowledgementResponse
	override val succeeded: Boolean = true
	override fun writePayload(writer: JSONWriter)
	{
		// do nothing
	}
}

/**
 * The [MessageType.ErrorResponse] [APIJsonResponse].
 *
 * @property errorMessage
 *   The error message that describes how the request failed.
 */
class ErrorResponse constructor(
	override val conversationId: Int,
	private val errorMessage: String
): APIJsonResponse()
{
	override val messageType: MessageType = MessageType.ErrorResponse
	override val succeeded: Boolean = false
	override fun writePayload(writer: JSONWriter)
	{
		writer.writeObject {
			at(::errorMessage.name) { write(errorMessage) }
		}
	}
}

/**
 * The [MessageType.GetFileResponse] [APIJsonResponse].
 *
 * @property fileText
 *   The text of the retrieved file
 */
class GetFileResponse constructor(
	override val conversationId: Int,
	private val fileText: String
): APIJsonResponse()
{
	override val messageType: MessageType = MessageType.GetFileResponse
	override val succeeded: Boolean = true
	override fun writePayload(writer: JSONWriter)
	{
		writer.writeObject {
			at(::fileText.name) { write(fileText) }
		}
	}
}

/**
 * The [MessageType.GetFileResponse] [APIJsonResponse].
 *
 * @property files
 *   The list of [pairs][Pair] of file relative path - boolean, if
 *   `true` indicates the file is a directory, `false` indicates it not a
 *   directory.
 */
class GetFileListResponse constructor(
	override val conversationId: Int,
	private val files: List<Pair<String, Boolean>>
): APIJsonResponse()
{
	override val messageType: MessageType = MessageType.GetFileListResponse
	override val succeeded: Boolean = true
	override fun writePayload(writer: JSONWriter)
	{
		writer.writeObject {
			at(::files.name) {
				writeArray {
					files.forEach {
						write(it.first)
						write(it.second)
					}
				}
			}
		}
	}
}