package com.haumealabs.kmpbase.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.haumealabs.kmpbase.viewmodel.MainViewModel
import com.haumealabs.kmpbase.viewmodel.MainViewModelEffects
import kotlinx.coroutines.launch

class MainScreen : Screen {
    @Composable
    override fun Content() {
        Main()
    }

    @OptIn(DependsOnGoogleMobileAds::class)
    @Composable
    fun Main() {
        val viewModel = rememberScreenModel { MainViewModel() }
        val navigator = LocalNavigator.currentOrThrow
        val scaffoldState = rememberScaffoldState()
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is MainViewModelEffects.ShowError -> {
                        coroutineScope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(
                                message = effect.message,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                    is MainViewModelEffects.ShowPaywall -> {
                        //TODO: PAYWALL
                    }

                    MainViewModelEffects.ShowRewarded -> {
                        //TODO: SHOW AD
                    }
                }
            }
        }

        val state = viewModel.uiState.collectAsState().value
        Scaffold(
            scaffoldState = scaffoldState,
            snackbarHost = {
                SnackbarHost(it) { data ->
                    Snackbar(
                        snackbarData = data,
                        backgroundColor = Color.Red,
                        contentColor = Color.White
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Text("HELLO")
            }
        }
    }
}

