package com.ducatti.badger.ui.component

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ducatti.badger.utils.goToSettings

@Composable
fun CameraPermissionWrapper(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var isCameraAccessGranted by remember {
        mutableStateOf(
            context.checkSelfPermission(
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val showRationale = remember {
        activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
        } ?: false
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isCameraAccessGranted = granted
    }

    LaunchedEffect(Unit) {
        if (!isCameraAccessGranted) {
            launcher.launch(Manifest.permission.CAMERA)

            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    when {
        isCameraAccessGranted -> content()
        else -> AskForPermission {
            if (showRationale) {
                launcher.launch(Manifest.permission.CAMERA)
            } else {
                context.goToSettings(listOf(Manifest.permission.CAMERA))
            }
        }
    }
}

@Composable
private fun AskForPermission(onPermissionRequested: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onPermissionRequested) {
            Text("Pedir permissão")
        }
    }
}
