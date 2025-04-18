package com.example.boxboxd.view.widgets

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.jolpica.Race
import com.example.boxboxd.viewmodel.AccountViewModel
import com.example.boxboxd.viewmodel.RacesViewModel

@Composable
fun ChooseListMenu(
    racesViewModel: RacesViewModel,
    accountViewModel: AccountViewModel,
    modifier: Modifier = Modifier,
    isListScreen : Boolean = true,
    race : Race? = null,
    onDismiss : () -> Unit
) {
    val isCreateListMenuOpened = remember { mutableStateOf(false) }
    val showConfirmDialog = remember { mutableStateOf(false) }
    val showDuplicateDialog = remember { mutableStateOf(false) }
    val userListsState = accountViewModel.userLists.collectAsState()
    val userLists = userListsState.value ?: emptyList()
    val context = LocalContext.current
    val toastText = stringResource(R.string.race_added)

    Box(modifier = modifier) {
        Column(modifier = Modifier
            .fillMaxSize()
            .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (userLists.isNotEmpty()) {
                MainButton(
                    buttonText = stringResource(R.string.create_new_list),
                    onClick = {
                        isCreateListMenuOpened.value = true
                    }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (userLists.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_lists_found),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(R.string.create_your_first_list),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            AddButton(
                                modifier = Modifier
                                    .width(100.dp)
                                    .padding(top = 8.dp)
                            ) {
                                isCreateListMenuOpened.value = true
                            }
                        }
                    }
                } else {

                    items(userLists) { list ->
                        ListCard(
                            customList = list,
                            isMainVersion = isListScreen,
                            accountViewModel = accountViewModel,
                            racesViewModel = racesViewModel,
                            onEditList = {

                            },
                            onClick = {
                                if (!isListScreen && race != null) {

                                    accountViewModel.addRaceToTheList(
                                        list = list,
                                        race = race,
                                        onSuccess = {
                                            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        },
                                        onDuplicate = {
                                            Log.i("aa", "aa")
                                            showDuplicateDialog.value = true

                                            Log.i("showDuplicateDialog outside", showDuplicateDialog.value.toString())
                                        }
                                    )


                                }
                            }
                        )
                    }
                }
            }
        }

        if (isCreateListMenuOpened.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        onClick = { showConfirmDialog.value = true },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(
                        onClick = {  },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .padding(16.dp)
            ) {
                CreateListMenu (
                    accountViewModel = accountViewModel,
                    onLogSubmitted = {
                        isCreateListMenuOpened.value = false
                    }
                )
            }
        }
    }

    if (showDuplicateDialog.value) {
        Log.i("showDuplicateDialog inside", showDuplicateDialog.value.toString())

        AlertDialog(
            onDismissRequest = { showDuplicateDialog.value = false },
            text = { Text(
                text = stringResource(R.string.on_duplicate_dialog),
                style = MaterialTheme.typography.bodyMedium
            ) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDuplicateDialog.value = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.confirm),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        )
    }


    if (showConfirmDialog.value) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog.value = false },
            text = { Text(stringResource(R.string.confirm_dialog)) },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog.value = false }) {
                    Text(
                        text = stringResource(R.string.no),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.i("ChooseListMenu", "Confirm dialog confirmed, calling onDismiss")
                        showConfirmDialog.value = false
                        isCreateListMenuOpened.value = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.yes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        )
    }
}