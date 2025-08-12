package com.ducatti.badger.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ducatti.badger.navigation.popSlideInFromLeftAnimation
import com.ducatti.badger.navigation.popSlideOutToRightAnimation
import com.ducatti.badger.navigation.slideInFromRightAnimation
import com.ducatti.badger.navigation.slideOutToLeftAnimation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgerAppTopBar(
    topBarState: BadgerAppTopBarState.TopBarState,
    onBackClick: () -> Unit
) {
    MediumTopAppBar(
        title = {
            AnimatedContent(
                targetState = topBarState.title,
                transitionSpec = { getTitleAnimation(topBarState.isPoppingBack) }
            ) { newTitle ->
                Text(newTitle, modifier = Modifier.fillMaxWidth())

            }
        },
        navigationIcon = {
            AnimatedContent(
                targetState = topBarState.isInitialRoute,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { isInitialRoute ->
                if (!isInitialRoute) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            }
        },
        actions = {
            if (topBarState.hasActionButton) TopBarActions()
        }
    )
}

@Composable
private fun TopBarActions() {
//    var isMenuExpanded by remember { mutableStateOf(false) }
//    val deleteHandler = LocalDeleteHandlerRegistry.current

//    IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
//        Icon(
//            imageVector = Icons.Filled.MoreVert,
//            contentDescription = stringResource(R.string.more_button_label),
//        )
//    }
//    DropdownMenu(
//        expanded = isMenuExpanded,
//        onDismissRequest = { isMenuExpanded = false },
//    ) {
//        DropdownMenuItem(
//            text = {
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        imageVector = Icons.Filled.Delete,
//                        contentDescription = stringResource(R.string.delete_local_data_button_label),
//                    )
//                    Text(stringResource(R.string.delete_local_data_button_label))
//                }
//            },
//            onClick = {
////                deleteHandler?.trigger(DeleteType.LOCAL_DATA)
//                isMenuExpanded = !isMenuExpanded
//            },
//        )
//        DropdownMenuItem(
//            text = {
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        imageVector = Icons.Filled.Delete,
//                        contentDescription = stringResource(R.string.delete_all_data_button_label),
//                    )
//                    Text(stringResource(R.string.delete_all_data_button_label))
//                }
//            },
//            onClick = {
////                deleteHandler?.trigger(DeleteType.ALL_DATA)
//                isMenuExpanded = !isMenuExpanded
//            },
//        )
//    }
}

private fun getTitleAnimation(isPoppingBack: Boolean) =
    if (isPoppingBack) {
        popSlideInFromLeftAnimation() + fadeIn() togetherWith
                popSlideOutToRightAnimation() + fadeOut()
    } else {
        slideInFromRightAnimation() + fadeIn() togetherWith
                slideOutToLeftAnimation() + fadeOut()
    }
