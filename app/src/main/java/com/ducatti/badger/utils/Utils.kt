package com.ducatti.badger.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import android.widget.Toast
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.net.toUri
import com.ducatti.badger.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.text.Normalizer

fun Context.goToSettings(revokedPermissions: List<String> = emptyList()) {
    if (revokedPermissions.isNotEmpty()) {
        Toast.makeText(
            this,
            this.getString(
                R.string.provide_permissions_via_settings,
                revokedPermissions.joinToString(
                    transform = { it.replace("android.permission.", "") },
                    separator = ", "
                )
            ),
            Toast.LENGTH_LONG
        ).show()
    }

    startActivity(
        Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = "package:$packageName".toUri()
        }
    )
}

fun String.clearLowercase(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    return normalized
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .lowercase()
}

fun generateQrCode(
    content: String,
    size: Int = 512,
    quietZoneModules: Int,
    errorCorrection: ErrorCorrectionLevel
): Bitmap {
    val hints = mapOf(
        EncodeHintType.CHARACTER_SET to "UTF-8",
        EncodeHintType.MARGIN to quietZoneModules,
        EncodeHintType.ERROR_CORRECTION to errorCorrection
    )
    val matrix = MultiFormatWriter().encode(
        content,
        BarcodeFormat.QR_CODE,
        size,
        size,
        hints
    )
    val bitmap = createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap[x, y] =
                if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
    }
    return bitmap
}
