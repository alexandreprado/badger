package com.ducatti.badger.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun SearchField(
    focusRequester: FocusRequester,
    searchQuery: String,
    onSearch: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = searchQuery,
        label = { Text("Pesquisar") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search icon"
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Clear search button",
                    modifier = Modifier.clickable {
                        onSearch("")
                        focusManager.clearFocus()
                    }
                )
            }
        },
        onValueChange = { onSearch(it) },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
    )
}
