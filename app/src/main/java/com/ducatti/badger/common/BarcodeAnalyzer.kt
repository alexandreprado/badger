package com.ducatti.badger.common

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val onBarcodeScanned: (String) -> Unit,
    private val scanIntervalMillis: Long = 2000L
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()
    private var lastScanTime = 0L

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastScanTime < scanIntervalMillis) return@addOnSuccessListener
                for (barcode in barcodes) {
                    if (barcode.valueType == Barcode.TYPE_TEXT || barcode.rawValue != null) {
                        lastScanTime = currentTime
                        barcode.rawValue?.let { onBarcodeScanned(it) }
                        break // Stop after first read
                    }
                }
            }
            .addOnFailureListener {
                Log.e("BarcodeAnalyzer", "Scan failed", it)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
