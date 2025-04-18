package com.example.boxboxd.core.inner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class StateFlowDelegate<T>(private val state: State<T>) : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return state.value
    }
}

@Composable
fun <T> StateFlow<T>.collectAsStateDelegate(): StateFlowDelegate<T> {
    val state = collectAsState()
    return StateFlowDelegate(state)
}