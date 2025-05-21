package com.example.boxboxd.view.widgets

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.example.boxboxd.R
import com.example.boxboxd.core.inner.objects.MapObjects
import com.example.boxboxd.model.DropdownItem

@Composable
fun WidgetForAutocompleteField (
    valuesForSearch : List<DropdownItem>,
    onChangeValue : (TextFieldValue) -> Unit,
    label : String,
    valuesLimit : Int,
    areImagesTinted : Boolean = false,
) {
    val dropDownOptions = remember { mutableStateOf(listOf<DropdownItem>()) }
    val textFieldValue = remember { mutableStateOf(TextFieldValue()) }
    val dropDownExpanded = remember { mutableStateOf(false) }

    fun filterDropdownOptions(text : String) {
        Log.i("FIELD dropDownOptions1", dropDownOptions.value.size.toString())

        Log.i("FIELD valuesForSearch", valuesForSearch.size.toString())
        Log.i("FIELD textSearch", text)

        dropDownOptions.value = valuesForSearch
            .filter { it.text.contains(text, ignoreCase = true) && it.text != text }
            .take(valuesLimit)

        Log.i("FIELD dropDownOptions2", dropDownOptions.value.size.toString())
    }

    fun onDropdownDismissRequest() {
        dropDownExpanded.value = false
    }

    fun onValueChanged(value: TextFieldValue) {
        dropDownExpanded.value = true
        textFieldValue.value = value

        filterDropdownOptions(value.text)

        onChangeValue(textFieldValue.value)
    }

    AutocompleteTextField (
        modifier = Modifier.fillMaxWidth(),
        value = textFieldValue.value,
        setValue = ::onValueChanged,
        onDismissRequest = ::onDropdownDismissRequest,
        dropDownExpanded = dropDownExpanded.value,
        list = dropDownOptions.value,
        label = label,
        onFocused = {
            dropDownExpanded.value = true
            filterDropdownOptions("")
        },
        areImagesTinted = areImagesTinted,
    )
}