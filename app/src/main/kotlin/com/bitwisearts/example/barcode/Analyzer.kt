package com.bitwisearts.example.barcode

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * An [ImageAnalysis.Analyzer] used to analyze [Barcode]s.
 *
 * @author Richard Arriaga
 *
 * @property options
 *   The [BarcodeScannerOptions] if specified; `null` otherwise.
 * @property handler
 *   The [ScanHandler] that process the result of a [Barcode] scan attempt.
 */
class BarcodeAnalyzer constructor (
	private val options: BarcodeScannerOptions?,
	private val handler: ScanHandler
): ImageAnalysis.Analyzer
{
	@SuppressLint("UnsafeOptInUsageError")
	override fun analyze(image: ImageProxy)
	{
		image.image?.let { rawImage ->
			val input = InputImage.fromMediaImage(
				rawImage, image.imageInfo.rotationDegrees)
			val scanner =
				if (options != null)
				{
					BarcodeScanning.getClient(options)
				}
				else
				{
					BarcodeScanning.getClient()
				}
			scanner.process(input)
				.addOnSuccessListener { barcodes ->
					barcodes.forEach { bc ->
						bc?.let {
							handler.onScanned(it)
						}
					}
				}
				.addOnFailureListener {
					handler.onError(it)
				}.addOnCompleteListener {
					image.close()
				}
		} ?: image.close()
	}
}

/**
 * The abstract [Barcode] format.
 *
 * @author Richard Arriaga
 *
 * @property id
 *   Uniquely identifies the format.
 */
sealed class Format constructor (val id: Int)
{
	/** The type is not recognized.*/
	object Unknown: Format(Barcode.FORMAT_UNKNOWN)

	/** The [Barcode.FORMAT_ALL_FORMATS]. */
	object AllFormats: Format(Barcode.FORMAT_ALL_FORMATS)

	/**
	 * A high density linear [barcode format][Format].
	 *
	 * see: [https://en.wikipedia.org/wiki/Code_128]
	 */
	object Code128: Format(Barcode.FORMAT_CODE_128)

	/**
	 * Code 39 (also known as Alpha39, Code 3 of 9, Code 3/9, Type 39,
	 * USS Code 39, or USD-3) is a variable length, discrete
	 * [barcode][Format] symbology.
	 *
	 * see: [https://en.wikipedia.org/wiki/Code_39]
	 */
	object Code39: Format(Barcode.FORMAT_CODE_39)

	/**
	 * Code 93 is a [barcode][Format] symbology that provides a higher
	 * density and data security enhancement to [Code 39][Code39].
	 *
	 * see: [https://en.wikipedia.org/wiki/Code_93]
	 */
	object Code93: Format(Barcode.FORMAT_CODE_93)

	/**
	 * Codabar is a linear [barcode][Format] symbology.
	 *
	 * see: [https://en.wikipedia.org/wiki/Codabar]
	 */
	object Codabar: Format(Barcode.FORMAT_CODABAR)

	/**
	 * A Data Matrix is a two-dimensional code consisting of black and white
	 * "cells" or dots arranged in either a square or rectangular pattern,
	 * also known as a matrix.
	 *
	 * see: [https://en.wikipedia.org/wiki/Data_Matrix]
	 */
	object DataMatrix: Format(Barcode.FORMAT_DATA_MATRIX)

	/**
	 * The International Article Number (also known as European Article Number
	 * or EAN) is a standard describing a barcode symbology and numbering system
	 * used in global trade to identify a specific retail product type, in a
	 * specific packaging configuration, from a specific manufacturer.
	 *
	 * The most commonly used EAN standard is the thirteen-digit EAN-13, a
	 * superset of the original 12-digit Universal Product Code (UPC-A) standard.
	 *
	 * see: [https://en.wikipedia.org/wiki/International_Article_Number]
	 */
	object Ean13: Format(Barcode.FORMAT_EAN_13)

	/**
	 * The International Article Number (also known as European Article Number
	 * or EAN) is a standard describing a barcode symbology and numbering system
	 * used in global trade to identify a specific retail product type, in a
	 * specific packaging configuration, from a specific manufacturer.
	 *
	 * The less commonly used 8-digit EAN-8 barcode was introduced for use on
	 * small packages, where [EAN-13][Ean13] would be too large
	 *
	 * see: [https://en.wikipedia.org/wiki/International_Article_Number]
	 */
	object Ean8: Format(Barcode.FORMAT_EAN_8)

	/**
	 * ITF-14 is the GS1 implementation of an Interleaved 2 of 5 (ITF) bar code
	 * to encode a Global Trade Item Number.
	 *
	 * see: [https://en.wikipedia.org/wiki/ITF-14]
	 */
	object Itf: Format(Barcode.FORMAT_ITF)

	/**
	 * A QR code (an initialism for quick response code) is a type of matrix
	 * barcode (or two-dimensional barcode).
	 *
	 * see: [https://en.wikipedia.org/wiki/QR_code]
	 */
	object QRCode: Format(Barcode.FORMAT_QR_CODE)

	/**
	 * The Universal Product Code (UPC or UPC code) is a barcode symbology that
	 * is widely used worldwide for tracking trade items in stores.
	 *
	 * see: [https://en.wikipedia.org/wiki/Universal_Product_Code]
	 */
	object UpcA: Format(Barcode.FORMAT_UPC_A)

	/**
	 * To allow the use of UPC barcodes on smaller packages, where a full
	 * 12-digit barcode may not fit, a zero-suppressed version of UPC was
	 * developed, called UPC-E, in which the number system digit, all trailing
	 * zeros in the manufacturer code, and all leading zeros in the product code,
	 * are suppressed (omitted).
	 *
	 * see: [https://en.wikipedia.org/wiki/Universal_Product_Code#UPC-E]
	 */
	object UpcE: Format(Barcode.FORMAT_UPC_E)

	/**
	 * PDF417 is a stacked linear barcode format used in a variety of
	 * applications such as transport, identification cards, and inventory
	 * management. "PDF" stands for Portable Data File.
	 *
	 * see: [https://en.wikipedia.org/wiki/PDF417]
	 */
	object Pdf417: Format(Barcode.FORMAT_PDF417)

	/**
	 * Aztec Code is a type of 2D barcode that has the potential to use less
	 * space than other matrix barcodes because it does not require a
	 * surrounding blank "quiet zone".
	 *
	 * see: [https://en.wikipedia.org/wiki/Aztec_Code]
	 */
	object Aztec: Format(Barcode.FORMAT_AZTEC)

	companion object
	{
		/**
		 * Provide the [Format] for the provided [Format.id].
		 *
		 * @param id
		 *   The [Format.id] to look up.
		 * @return
		 *   The associated [Format] or [Format.Unknown] if not
		 *   recognized.
		 */
		operator fun get (id: Int): Format =
			when (id)
			{
				Barcode.FORMAT_UNKNOWN -> Unknown
				Barcode.FORMAT_ALL_FORMATS -> AllFormats
				Barcode.FORMAT_CODE_128 -> Code128
				Barcode.FORMAT_CODE_39 -> Code39
				Barcode.FORMAT_CODE_93 -> Code93
				Barcode.FORMAT_CODABAR -> Codabar
				Barcode.FORMAT_DATA_MATRIX -> DataMatrix
				Barcode.FORMAT_EAN_13 -> Ean13
				Barcode.FORMAT_EAN_8 -> Ean8
				Barcode.FORMAT_ITF -> Itf
				Barcode.FORMAT_QR_CODE -> QRCode
				Barcode.FORMAT_UPC_A -> UpcA
				Barcode.FORMAT_UPC_E -> UpcE
				Barcode.FORMAT_PDF417 -> Pdf417
				Barcode.FORMAT_AZTEC -> Aztec
				else -> Unknown
			}
	}
}

/**
 * The abstract information type of the [Barcode].
 *
 * @author Richard Arriaga
 *
 * @property id
 *   The id that uniquely identifies the [Type].
 */
sealed class Type constructor (val id: Int)
{
	/**
	 * The canonical [Type] representation of an unrecognized barcode.
	 */
	object Unknown: Type(Barcode.TYPE_UNKNOWN)

	/**
	 * A barcode containing [contact information][Barcode.ContactInfo].
	 */
	object ContactInfo: Type(Barcode.TYPE_CONTACT_INFO)
	{
		/**
		 * Extract the [Barcode.ContactInfo] from the [Barcode].
		 *
		 * @param barcode
		 *   The barcode to extract data from.
		 */
		@Suppress("unused")
		fun info (barcode: Barcode): Barcode.ContactInfo =
			barcode.contactInfo!!
	}

	/**
	 * The [Type] containing [email information][Barcode.Email].
	 */
	object Email: Type(Barcode.TYPE_EMAIL)
	{
		/**
		 * Get the [Barcode.Email] from the [Barcode].
		 *
		 * @param barcode
		 *   The barcode to extract data from.
		 */
		@Suppress("unused")
		fun email (barcode: Barcode): Barcode.Email =
			barcode.email!!
	}

	/**
	 * The BarcodeType containing [ISBN information][Barcode.TYPE_ISBN].
	 */
	object Isbn: Type(Barcode.TYPE_ISBN)

	/**
	 * The [Type] containing [telephone information][Barcode.TYPE_PHONE].
	 */
	object Phone: Type(Barcode.TYPE_PHONE)
	{
		/**
		 * Get the [Barcode.Phone] from the [Barcode].
		 *
		 * @param barcode
		 *   The barcode to extract data from.
		 */
		@Suppress("unused")
		fun phone (barcode: Barcode): Barcode.Phone =
			barcode.phone!!
	}

	/**
	 * The barcode contains [product information][Barcode.TYPE_PRODUCT].
	 */
	object Product: Type(Barcode.TYPE_PRODUCT)

	/**
	 * The barcode contains [sms information][Barcode.TYPE_SMS].
	 */
	object Sms: Type(Barcode.TYPE_SMS)
	{
		/**
		 * Get the [Barcode.Sms] from the [Barcode].
		 *
		 * @param barcode
		 *   The barcode to extract data from.
		 */
		@Suppress("unused")
		fun sms (barcode: Barcode): Barcode.Sms =
			barcode.sms!!
	}

	/**
	 * The barcode contains [text][Barcode.TYPE_TEXT].
	 */
	object Text: Type(Barcode.TYPE_TEXT)

	/**
	 * The barcode contains a [url][Barcode.TYPE_URL].
	 */
	object Url: Type(Barcode.TYPE_URL)
	{
		/**
		 * Get the [Barcode.UrlBookmark] from the [Barcode].
		 *
		 * @param barcode
		 *   The barcode to extract data from.
		 */
		@Suppress("unused")
		fun url (barcode: Barcode): String = barcode.url!!.url!!
	}

	/**
	 * The barcode contains [WIFI information][Barcode.TYPE_WIFI].
	 */
	object WiFi: Type(Barcode.TYPE_WIFI)
	{
		/**
		 * Get the [Barcode.WiFi] from the [Barcode].
		 *
		 * @param barcode
		 *   The barcode to extract data from.
		 */
		@Suppress("unused")
		fun wifi (barcode: Barcode): Barcode.WiFi =
			barcode.wifi!!
	}

	/**
	 * The barcode contains [coordinate data][Barcode.GeoPoint].
	 */
	object Geo: Type(Barcode.TYPE_GEO)
	{
		/**
		 * Get the [Barcode.GeoPoint] from the [Barcode].
		 *
		 * @param barcode
		 *   The barcode to extract data from.
		 */
		@Suppress("unused")
		fun geoPoint (barcode: Barcode): Barcode.GeoPoint =
			barcode.geoPoint!!
	}

	/** The type is for a calendar event.*/
	object CalendarEvent: Type(Barcode.TYPE_CALENDAR_EVENT)
	{
		/**
		 * Get the [Barcode.CalendarEvent] from the [Barcode].
		 *
		 * @param barcode
		 *   The barcode to extract data from.
		 */
		@Suppress("unused")
		fun event (barcode: Barcode): Barcode.CalendarEvent =
			barcode.calendarEvent!!
	}

	/** The type is for drivers license.*/
	object DriversLicense: Type(Barcode.TYPE_DRIVER_LICENSE)
	{
		/**
		 * Get the [Barcode.DriverLicense] from the [Barcode].
		 *
		 * @param barcode
		 *   The barcode to extract data from.
		 */
		@Suppress("unused")
		fun driverLicense (barcode: Barcode): Barcode.DriverLicense =
			barcode.driverLicense!!
	}

	companion object
	{
		/**
		 * Provide the [Type] for the given [Type.id].
		 *
		 * @param id
		 *   The [Type.id].
		 * @return
		 *   The associated type.
		 */
		operator fun get (id: Int): Type =
			when (id)
			{
				Barcode.TYPE_UNKNOWN -> Unknown
				Barcode.TYPE_CONTACT_INFO -> ContactInfo
				Barcode.TYPE_EMAIL -> Email
				Barcode.TYPE_ISBN -> Isbn
				Barcode.TYPE_PHONE -> Phone
				Barcode.TYPE_PRODUCT -> Product
				Barcode.TYPE_SMS -> Sms
				Barcode.TYPE_TEXT -> Text
				Barcode.TYPE_URL -> Url
				Barcode.TYPE_WIFI -> WiFi
				Barcode.TYPE_GEO -> Geo
				Barcode.TYPE_CALENDAR_EVENT -> CalendarEvent
				Barcode.TYPE_DRIVER_LICENSE -> DriversLicense
				else -> Unknown
			}
	}
}

/** This [Barcode]'s [Type]. */
val Barcode.type: Type get() = Type[valueType]

/** The [Barcode]'s [Format]. */
val Barcode.imageFormat: Format get() = Format[format]