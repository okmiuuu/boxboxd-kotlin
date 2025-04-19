package com.example.boxboxd.view.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp

@Composable
fun AutocompleteTextField(
    suggestions: List<String>,
    label: String,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var filteredSuggestions by remember { mutableStateOf(suggestions) }

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            // Filter suggestions based on input (case-insensitive)
            filteredSuggestions = suggestions.filter {
                it.contains(newText, ignoreCase = true)
            }
            isDropdownExpanded = newText.isNotEmpty() || filteredSuggestions.isNotEmpty()
        },
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                isDropdownExpanded = focusState.isFocused && filteredSuggestions.isNotEmpty()
                if (focusState.isFocused && text.isEmpty()) {
                    filteredSuggestions = suggestions
                }
            }
    )

    DropdownMenu(
        expanded = isDropdownExpanded,
        onDismissRequest = { isDropdownExpanded = false },
        modifier = Modifier.fillMaxWidth()
    ) {
        filteredSuggestions.forEach { suggestion ->
            DropdownMenuItem(
                text = { Text(suggestion) },
                onClick = {
                    text = suggestion
                    isDropdownExpanded = false
                    filteredSuggestions = suggestions
                }
            )
        }
    }
}