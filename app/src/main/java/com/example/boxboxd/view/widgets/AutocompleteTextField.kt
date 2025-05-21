package com.example.boxboxd.view.widgets

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import coil.compose.rememberAsyncImagePainter
import com.example.boxboxd.R
import com.example.boxboxd.model.DropdownItem
import com.example.boxboxd.model.TintedPainter

@Composable
fun AutocompleteTextField(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    setValue: (TextFieldValue) -> Unit,
    onDismissRequest: () -> Unit,
    dropDownExpanded: Boolean,
    list: List<DropdownItem>,
    label : String,
    onFocused: () -> Unit,
    areImagesTinted : Boolean = false,
) {

    Box(modifier) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        onDismissRequest()
                    } else {
                        onFocused()
                    }
                },
            value = value,
            label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
            onValueChange = setValue,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
        )
        DropdownMenu(
            expanded = dropDownExpanded,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = onDismissRequest,
            modifier = Modifier.fillMaxWidth(),
        ) {
            list.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            item.imageUrl?.let { url ->
                                Image(
                                    painter =
                                        if (areImagesTinted) {
                                            TintedPainter(
                                                painter = rememberAsyncImagePainter(url),
                                                tint = MaterialTheme.colorScheme.tertiary
                                            )
                                        } else {
                                            rememberAsyncImagePainter(url)
                                        }
                                    ,
                                    contentDescription = "${item.text} image",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .padding(end = 8.dp),
                                )
                            } ?: Spacer(modifier = Modifier.size(40.dp))
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    onClick = {
                        setValue(
                            TextFieldValue(
                                text = item.text,
                                selection = TextRange(item.text.length)
                            )
                        )
                        onDismissRequest()
                    }
                )
            }
        }
    }
}