package com.ducatti.badger.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ducatti.badger.data.model.User

@Composable
fun UserMetadata(
    user: User,
    modifier: Modifier = Modifier,
    shouldCenterItems: Boolean = false,
    isExpanded: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (shouldCenterItems) {
            Alignment.CenterHorizontally
        } else {
            Alignment.Start
        }
    ) {
        Text(user.name, fontSize = 24.sp)
        if (isExpanded) {
            Text("+", fontSize = 24.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(user.guests.toString())
            if (user.guests > 1) {
                Text("convidados", fontSize = 18.sp)
            } else if (user.guests == 0) {
                Text("Nenhum convidado", fontSize = 18.sp)
            } else {
                Text("convidado", fontSize = 18.sp)
            }
        }
    }
}
