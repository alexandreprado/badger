package com.ducatti.badger.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ducatti.badger.R

@Composable
fun CameraButton(onNavigateToCamera: () -> Unit) {
    ExtendedFloatingActionButton(onNavigateToCamera) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Ler QR code")
            Icon(
                painter = painterResource(R.drawable.ic_qr_code),
                contentDescription = null,
            )
        }
    }
}
