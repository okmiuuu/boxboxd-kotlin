package com.example.boxboxd.view.widgets

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.CustomList
import com.example.boxboxd.viewmodel.AccountViewModel

@Composable
fun EditListMenu(
    listToEdit : CustomList,
    accountViewModel: AccountViewModel,
    onLogSubmitted: () -> Unit
) {
    val originalPictureUri = listToEdit.picture?.let { uriString ->
        try {
            Uri.parse(uriString).also {
                println("Parsed Uri: $it")
            }
        } catch (e: Exception) {
            println("Uri parsing failed: ${e.message}")
            null
        }
    }

    val context = LocalContext.current

    val name = remember { mutableStateOf(listToEdit.name ?: "") }
    val description = remember { mutableStateOf(listToEdit.description ?: "") }
    val selectedImageUri = remember { mutableStateOf(originalPictureUri) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.create_list),
            style = MaterialTheme.typography.bodyMedium
        )

        Row {

            ImagePicker(
                firstImageToShow = originalPictureUri,
                onImageSelected = { uri ->
                    selectedImageUri.value = uri
                }
            )

            Column {
                TextField(
                    value = name.value,
                    onValueChange = {
                        name.value = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    label = {
                        Text (
                            text = stringResource(R.string.name),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true,
                )

                TextField(
                    value = description.value,
                    onValueChange = {
                        description.value = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    label = {
                        Text (
                            text = stringResource(R.string.description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = false,
                )
            }
        }

        Button(
            modifier = Modifier
                .height(30.dp)
                .height(100.dp),
            onClick = {
                val newCustomList = CustomList (
                    user = accountViewModel.userObject.value,
                    name = name.value,
                    description  = description.value,
                    picture = selectedImageUri.value?.toString().also {
                        selectedImageUri.value?.let { uri -> accountViewModel.persistUriPermission(context, uri) }
                    }
                )

                accountViewModel.changeListToNew(
                    id = listToEdit.id,
                    newList = newCustomList
                )

                onLogSubmitted()
            },
            shape = RoundedCornerShape(10.dp),
            enabled = (name.value.isNotEmpty())
        ) {
            Text(
                text = stringResource(R.string.save),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }

}