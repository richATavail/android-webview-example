package com.bitwisearts.example.camera

import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

// This file contains Image conversion utilities.

/**
 * Conditionally convert this [Image] to an [ImageBitmap] or answer `null` if
 * the source image format is not supported.
 *
 * @return
 *   The [ImageBitmap] or the `null` if the source image format is not
 *   supported.
 */
@Suppress("unused")
fun Image.asImageBitmap(): ImageBitmap?
{
	val formatType = when (format)
	{
		ImageFormat.JPEG -> return jpegToBitMap()
		ImageFormat.YUV_420_888 -> return yuv420888ToBitmap()
		ImageFormat.DEPTH16 -> "ImageFormat.DEPTH16"
		ImageFormat.DEPTH_JPEG -> "ImageFormat."
		ImageFormat.DEPTH_POINT_CLOUD -> "ImageFormat."
		ImageFormat.FLEX_RGBA_8888 -> "ImageFormat."
		ImageFormat.FLEX_RGB_888 -> "ImageFormat."
		ImageFormat.HEIC -> "ImageFormat."
		ImageFormat.NV16 -> "ImageFormat."
		ImageFormat.NV21 -> "ImageFormat."
		ImageFormat.PRIVATE -> "ImageFormat."
		ImageFormat.RAW10 -> "ImageFormat."
		ImageFormat.RAW12 -> "ImageFormat."
		ImageFormat.RAW_PRIVATE -> "ImageFormat."
		ImageFormat.RAW_SENSOR -> "ImageFormat."
		ImageFormat.RGB_565 -> "ImageFormat."
		ImageFormat.UNKNOWN -> "ImageFormat."
		ImageFormat.Y8 -> "ImageFormat."
		ImageFormat.YCBCR_P010 -> "ImageFormat."
		ImageFormat.YUV_422_888 -> "ImageFormat."
		ImageFormat.YUV_444_888 -> "ImageFormat."
		ImageFormat.YUY2 -> "ImageFormat."
		ImageFormat.YV12 -> "ImageFormat."
		else -> "ImageFormat not found"
	}
	val msg = "Unsupported image format: $formatType (code: $format)"
	Log.e("Image.asImageBitmap", msg, UnsupportedOperationException(msg))
	return null
}

/**
 * Answer an [ImageBitmap] from this [ImageFormat.YUV_420_888] source [Image].
 */
fun Image.yuv420888ToBitmap(): ImageBitmap
{
	if (format != ImageFormat.YUV_420_888)
	{
		throw IllegalStateException(
			"Expected ImageFormat YUV_420_888()${ImageFormat.YUV_420_888}," +
				" but received $format")
	}
	val yBuffer = planes[0].buffer
	val vuBuffer = planes[2].buffer
	val ySize = yBuffer.remaining()
	val vuSize = vuBuffer.remaining()
	val nv21 = ByteArray(ySize + vuSize)
	yBuffer.get(nv21, 0, ySize)
	vuBuffer.get(nv21, ySize, vuSize)
	val yuvImage = YuvImage(
		nv21,
		ImageFormat.NV21,
		this.width,
		this.height,
		null)
	val outStream = ByteArrayOutputStream()
	yuvImage.compressToJpeg(
		Rect(0, 0, yuvImage.width, yuvImage.height),
		50,
		outStream)
	val imageBytes = outStream.toByteArray()
	return BitmapFactory
		.decodeByteArray(imageBytes, 0, imageBytes.size)
		.asImageBitmap()
}

/**
 * Answer an [ImageBitmap] from this [ImageFormat.JPEG] source [Image].
 */
fun Image.jpegToBitMap(): ImageBitmap
{
	if (format != ImageFormat.JPEG)
	{
		throw IllegalStateException(
			"Expected ImageFormat YUV_420_888()${ImageFormat.JPEG}, but " +
				"received $format")
	}
	val planesBuffer: ByteBuffer = planes[0].buffer
	val target = ByteArray(planesBuffer.capacity())
	planesBuffer.get(target)
	return BitmapFactory
		.decodeByteArray(target, 0, target.size, null)
		.asImageBitmap()
}