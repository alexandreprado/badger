package com.ducatti.badger.ui.page.camera

import android.content.Context
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.SurfaceRequest
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.takeOrElse
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ducatti.badger.common.BarcodeAnalyzer
import com.ducatti.badger.ui.component.CameraPermissionWrapper
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data object QRScannerRoute

@Composable
fun CameraScreen(
    onNavigateToConfirmation: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel<CameraViewModel>(),

    ) {
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    CameraScreen(
        surfaceRequest,
        viewModel::bindToCamera,
        viewModel::onCodeScanned,
        viewModel::tapToFocus,
        onNavigateToConfirmation,
        onNavigateBack
    )
}

@Composable
private fun CameraScreen(
    surfaceRequest: SurfaceRequest?,
    bindToCamera: BindToCamera,
    onCodeScanned: (String) -> CameraViewModel.ScannedCode,
    onTapToFocus: (Offset) -> Unit = {},
    onNavigateToConfirmation: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var scannedCode by remember { mutableStateOf<String?>(null) }
    var invalidCode by remember { mutableStateOf<String?>(null) }
    var isCameraPreviewVisible by remember { mutableStateOf(false) }

    var autofocusRequest by remember { mutableStateOf(UUID.randomUUID() to Offset.Unspecified) }
    val autofocusRequestId = autofocusRequest.first
    val showAutofocusIndicator = autofocusRequest.second.isSpecified
    val autofocusCoords = remember(autofocusRequestId) { autofocusRequest.second }

    if (showAutofocusIndicator) {
        LaunchedEffect(autofocusRequestId) {
            delay(1_000)
            autofocusRequest = autofocusRequestId to Offset.Unspecified
        }
    }

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
                    onNavigateToConfirmation(code)
                    null
                }
            }
        }
    }

    LaunchedEffect(invalidCode) {
        if (invalidCode != null) {
            delay(3_000)
            invalidCode = null
            scannedCode = null
        }
    }

    LaunchedEffect(lifecycleOwner) {
        bindToCamera(context.applicationContext, lifecycleOwner, imageAnalysis)
    }

    CameraPermissionWrapper {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            surfaceRequest?.let { request ->
                isCameraPreviewVisible = true
                val coordinateTransformer = remember { MutableCoordinateTransformer() }
                CameraXViewfinder(
                    surfaceRequest = request,
                    coordinateTransformer = coordinateTransformer,
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures { tapCoords ->
                            with(coordinateTransformer) {
                                onTapToFocus(tapCoords.transform())
                            }
                            autofocusRequest = UUID.randomUUID() to tapCoords
                        }
                    }
                )

                AnimatedVisibility(
                    visible = showAutofocusIndicator,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .offset { autofocusCoords.takeOrElse { Offset.Zero }.round() }
                        .offset((-24).dp, (-24).dp)
                ) {
                    Spacer(
                        Modifier
                            .border(2.dp, Color.White, CircleShape)
                            .size(48.dp)
                    )
                }
            }

            if (!isCameraPreviewVisible) {
                CircularProgressIndicator()
            }

            if (isCameraPreviewVisible) {
                BoxWithConstraints(Modifier.padding(36.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(this.maxWidth)
                            .padding(36.dp)
                            .border(width = 8.dp, color = Color.Green)
                    )
                }
            }

            Column(Modifier.fillMaxSize()) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back button",
                        tint = Color.White
                    )
                }

                Spacer(Modifier.weight(1f))

                invalidCode?.let { invalidCode ->
                    InvalidCodeMessage(invalidCode)
                }
            }
        }
    }
}

@Composable
private fun InvalidCodeMessage(scannedCode: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .background(Color.Red)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Código inválido",
                color = Color.White,
                fontSize = 14.sp,
            )
            Text(
                text = scannedCode,
                color = Color.White,
                fontSize = 18.sp,
            )
        }
    }
}

typealias BindToCamera = suspend (
    appContext: Context,
    lifecycleOwner: LifecycleOwner,
    imageAnalysis: ImageAnalysis
) -> Unit
