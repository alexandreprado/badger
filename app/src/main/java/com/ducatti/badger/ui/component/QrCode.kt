package com.ducatti.badger.ui.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.ducatti.badger.utils.generateQrCode
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun QrCode(
    content: String,
    modifier: Modifier = Modifier,
    quietZoneModules: Int = 2,
    errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.M,
    contentDescription: String? = "QR code"
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.TopStart) {
        val px = with(LocalDensity.current) {
            this@BoxWithConstraints.maxWidth.coerceAtMost(300.dp).roundToPx()
        }

        val bitmap by produceState<Bitmap?>(
            initialValue = null,
            content,
            px,
            quietZoneModules,
            errorCorrection
        ) {
            value = withContext(Dispatchers.Default) {
                generateQrCode(
                    content = content,
                    size = px,
                    quietZoneModules = quietZoneModules,
                    errorCorrection = errorCorrection
                )
            }
        }

        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit
            )
        } ?: CircularProgressIndicator()
    }
}
