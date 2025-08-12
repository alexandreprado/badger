package com.ducatti.badger.ui.page.camera

import android.content.Context
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.SurfaceRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ducatti.badger.common.BarcodeAnalyzer
import com.ducatti.badger.ui.component.CameraPermissionWrapper
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable
data object QRScannerRoute

@Composable
fun CameraScreen(
    onNavigateToDetails: (String) -> Unit,
    viewModel: CameraViewModel = hiltViewModel<CameraViewModel>(),

) {
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    CameraScreen(
        surfaceRequest,
        viewModel::bindToCamera,
        viewModel::onCodeScanned,
        onNavigateToDetails
    )
}

@Composable
private fun CameraScreen(
    surfaceRequest: SurfaceRequest?,
    bindToCamera: BindToCamera,
    onCodeScanned: (String) -> CameraViewModel.ScannedCode,
    onNavigateToDetails: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var scannedCode by remember { mutableStateOf<String?>(null) }
    var invalidCode by remember { mutableStateOf<String?>(null) }
    var isCameraPreviewVisible by remember { mutableStateOf(false) }

    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    BarcodeAnalyzer({ scannedCode = it })
                )
            }
    }

    LaunchedEffect(scannedCode) {
        scannedCode?.let { code ->
            invalidCode = when (onCodeScanned(code)) {
                CameraViewModel.ScannedCode.Invalid -> code
                CameraViewModel.ScannedCode.Valid -> {
                    onNavigateToDetails(code)
                    null
                }
            }
        }
    }

    LaunchedEffect(invalidCode) {
        if (invalidCode != null) {
            delay(5_000)
            invalidCode == null
        }
    }

    CameraPermissionWrapper {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {

            LaunchedEffect(lifecycleOwner) {
                bindToCamera(context.applicationContext, lifecycleOwner, imageAnalysis)
            }

            val density = LocalDensity.current
            var height by remember { mutableStateOf(1.dp) }

            if (!isCameraPreviewVisible) {
                CircularProgressIndicator()
            }

            surfaceRequest?.let { request ->
                isCameraPreviewVisible = true
                CameraXViewfinder(surfaceRequest = request)
            }

            if (isCameraPreviewVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height)
                        .padding(36.dp)
                        .border(width = 8.dp, color = Color.Green)
                        .onGloballyPositioned { coords ->
                            height = with(density) { coords.size.width.toDp() }
                        }
                )
            }

            invalidCode?.let { invalidCode ->
                InvalidCodeMessage(invalidCode)
            }
        }
    }
}

@Composable
private fun InvalidCodeMessage(scannedCode: String) {
    Box(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color.Red)
    ) {
        Text(
            text = "Código inválido: $scannedCode",
            style = TextStyle(color = Color.White)
        )
    }
}

typealias BindToCamera = suspend (
    appContext: Context,
    lifecycleOwner: LifecycleOwner,
    imageAnalysis: ImageAnalysis
) -> Unit
