package com.ducatti.badger.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import com.ducatti.badger.utils.generateQrCode
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

@Composable
fun QrCode(
    content: String,
    modifier: Modifier = Modifier,
    quietZoneModules: Int = 2,
    errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.M,
    contentDescription: String? = "QR code"
) {
    BoxWithConstraints(modifier = modifier) {
        val px = with(LocalDensity.current) {
            this@BoxWithConstraints.maxWidth.coerceAtMost(maxHeight).roundToPx()
        }
        // Regenerate bitmap only when inputs change
        val bitmap = remember(content, px, quietZoneModules, errorCorrection) {
            generateQrCode(
                content = content,
                size = px,
                quietZoneModules = quietZoneModules,
                errorCorrection = errorCorrection
            )
        }

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = Modifier,
            contentScale = ContentScale.Fit
        )
    }
}
