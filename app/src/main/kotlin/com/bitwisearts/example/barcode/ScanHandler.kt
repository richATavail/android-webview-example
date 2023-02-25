package com.bitwisearts.example.barcode

import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Handles the result of an attempt to scan a barcode.
 *
 * @author Richard Arriaga
 */
interface ScanHandler
{
	/**
	 * Process a scanned [Barcode].
	 *
	 * @param barcode
	 *   The successfully scanned [Barcode].
	 */
	fun onScanned (barcode: Barcode)

	/**
	 * Process an error that was encountered during a [Barcode] scan attempt.
	 *
	 * @param exception
	 *   The [Throwable] caught during the scan.
	 */
	fun onError (exception: Throwable)
}