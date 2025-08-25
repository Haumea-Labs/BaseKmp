package com.haumealabs.kmpbase.architecture

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<S, F>(initialState: S) : ScreenModel {

    private val _uiState: MutableStateFlow<S> by lazy { MutableStateFlow(initialState) }
    val uiState = _uiState.asStateFlow()

    protected fun setState(reducer: S.() -> S) {
        _uiState.value = _uiState.value.reducer()
    }

    private val _effect = MutableSharedFlow<F>(
        replay = 0,                // Side effects are consumed only once.
        extraBufferCapacity = 1,   // Minimal buffer to ensure emission.
        onBufferOverflow = BufferOverflow.DROP_OLDEST // Drop oldest values if needed.
    )

    val effect: SharedFlow<F> = _effect.asSharedFlow()

    protected fun setEffect(builder: () -> F) {
        val effectValue = builder()
        screenModelScope.launch { _effect.emit(effectValue) }
    }

}