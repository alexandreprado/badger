package com.ducatti.badger.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

val defaultScrollRange = ScrollRange(0.dp, 64.dp)

@Composable
fun rememberAppBarScrollPosition(
    state: LazyListState,
    scrollRange: ScrollRange = defaultScrollRange,
): State<Dp> {
    val scrollPosition = remember { mutableStateOf(0.dp) }
    AppBarScrollPosition(
        state = state,
        scrollRange = scrollRange
    ) { position ->
        println("FOCUS_HELL: position = $position")
        scrollPosition.value = position
    }
    return scrollPosition
}

@Composable
private fun AppBarScrollPosition(
    state: LazyListState,
    scrollRange: ScrollRange = defaultScrollRange,
    positionProvider: (Dp) -> Unit,
) {
    val density = LocalDensity.current
    val currentItemIndex by remember(state) { derivedStateOf { state.firstVisibleItemIndex } }
    val currentItemOffset by remember(state) { derivedStateOf { state.firstVisibleItemScrollOffset } }
    val lastItemIndex = rememberPrevious(state, currentItemIndex)
    val lastItemOffset = rememberPrevious(state, currentItemOffset)
    val currentOffset = rememberSaveable(state) { mutableIntStateOf(0) }

    LaunchedEffect(currentItemIndex, currentItemOffset) {
        when {
            // Initial state, no need to calculate, we just take the current offset into account
            currentItemIndex == 0 -> currentOffset.intValue = currentItemOffset

            // Still same item, but the offset might have changed, we need to add the difference
            lastItemIndex == currentItemIndex ->
                currentOffset.intValue += currentItemOffset - lastItemOffset

            // Scrolling up, we need to remove the difference
            lastItemIndex > currentItemIndex ->
                currentOffset.intValue -= lastItemOffset

            // Scrolling down, new index, we just add the new offset since it's naturally reset
            else -> currentOffset.intValue += currentItemOffset
        }

        positionProvider(
            with(density) { currentOffset.intValue.toDp() }
                .coerceIn(scrollRange.start, scrollRange.end)
        )
    }
}

@Composable
private fun rememberPrevious(state: LazyListState, current: Int): Int {
    val previous = rememberSaveable(state) { mutableIntStateOf(0) }
    // Runs it in a SideEffect so the difference is calculated and updated after the composition
    SideEffect {
        if (previous.intValue != current) {
            previous.intValue = current
        }
    }
    return previous.intValue
}

internal fun getBackgroundAlphaScroll(scrollValue: Dp, scrollRange: ScrollRange) =
    scrollValue.percentageIn(scrollRange)

/**
 * [percentageIn] returns a percentage 0-1 of the value position within the range of values.
 */
private fun Dp.percentageIn(scrollRange: ScrollRange): Float =
    ((this - scrollRange.start) / (scrollRange.end - scrollRange.start)).coerceIn(0f, 1f)

data class ScrollRange(val start: Dp, val end: Dp)
