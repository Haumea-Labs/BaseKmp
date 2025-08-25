package com.haumealabs.kmpbase.viewmodel

import com.haumealabs.kmpbase.architecture.BaseViewModel
import com.haumealabs.kmpbase.viewmodel.MainViewModelEffects.ShowError

class MainViewModel : BaseViewModel<MainViewModelState, MainViewModelEffects>(MainViewModelState()) {

    fun onAdFailed() {
        setEffect { ShowError("AD FAILED") }
        setState { copy(status = MainViewModelStatus.INCOMPLETE) }
    }

    fun onAdWatched() {
        //TODO: DO SOMETHING
    }

    override fun onDispose() {
        super.onDispose()
    }

}

enum class MainViewModelStatus {
    INCOMPLETE,
}

data class MainViewModelState(
    val status: MainViewModelStatus = MainViewModelStatus.INCOMPLETE,
)

sealed class MainViewModelEffects {
    data object ShowRewarded : MainViewModelEffects()
    data class ShowError(val message: String) : MainViewModelEffects()
    data object ShowPaywall : MainViewModelEffects()
}