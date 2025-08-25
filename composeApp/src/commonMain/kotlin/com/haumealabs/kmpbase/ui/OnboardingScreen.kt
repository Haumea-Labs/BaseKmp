package com.haumealabs.aiphoto.ui

import animaite.composeapp.generated.resources.Res
import animaite.composeapp.generated.resources.get_started
import animaite.composeapp.generated.resources.next
import animaite.composeapp.generated.resources.onboarding1
import animaite.composeapp.generated.resources.onboarding2
import animaite.composeapp.generated.resources.onboarding3
import animaite.composeapp.generated.resources.onboarding_slide1_subtitle
import animaite.composeapp.generated.resources.onboarding_slide1_subtitle_bold
import animaite.composeapp.generated.resources.onboarding_slide1_title
import animaite.composeapp.generated.resources.onboarding_slide1_title_bold
import animaite.composeapp.generated.resources.onboarding_slide2_subtitle
import animaite.composeapp.generated.resources.onboarding_slide2_subtitle_bold
import animaite.composeapp.generated.resources.onboarding_slide2_title
import animaite.composeapp.generated.resources.onboarding_slide2_title_bold
import animaite.composeapp.generated.resources.onboarding_slide3_subtitle
import animaite.composeapp.generated.resources.onboarding_slide3_subtitle_bold
import animaite.composeapp.generated.resources.onboarding_slide3_title
import animaite.composeapp.generated.resources.onboarding_slide3_title_bold
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.haumealabs.aiphoto.architecture.onboarding.Onboarding
import com.haumealabs.aiphoto.architecture.onboarding.SlideData
import com.haumealabs.aiphoto.utils.Singleton.Companion.analyticsHelper
import com.haumealabs.aiphoto.utils.Singleton.Companion.storage
import com.haumealabs.kmpbase.ui.MainScreen
import org.jetbrains.compose.resources.stringResource

class OnboardingScreen() : Screen {
    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    override fun Content() {
        val navigator: Navigator = LocalNavigator.currentOrThrow
        val slidesData = listOf(
            SlideData(
                title = stringResource(Res.string.onboarding_slide1_title),
                subtitle = stringResource(Res.string.onboarding_slide1_subtitle),
                boldPart = stringResource(Res.string.onboarding_slide1_title_bold),
                boldSubtitlePart = stringResource(Res.string.onboarding_slide1_subtitle_bold),
                image = Res.drawable.onboarding1
            ),
            SlideData(
                title = stringResource(Res.string.onboarding_slide2_title),
                subtitle = stringResource(Res.string.onboarding_slide2_subtitle),
                boldPart = stringResource(Res.string.onboarding_slide2_title_bold),
                boldSubtitlePart = stringResource(Res.string.onboarding_slide2_subtitle_bold),
                image = Res.drawable.onboarding2
            ),
            SlideData(
                title = stringResource(Res.string.onboarding_slide3_title),
                subtitle = stringResource(Res.string.onboarding_slide3_subtitle),
                boldPart = stringResource(Res.string.onboarding_slide3_title_bold),
                boldSubtitlePart = stringResource(Res.string.onboarding_slide3_subtitle_bold),
                image = Res.drawable.onboarding3
            )
        )

        val buttonNextText = stringResource(Res.string.next)
        val buttonEndText = stringResource(Res.string.get_started)

        Onboarding(
            slides = slidesData,
            buttonNextMessage = buttonNextText,
            buttonEndMessage = buttonEndText,
            backgroundColor = MaterialTheme.colors.primary,
            textColor = Color.White
        ) {
            analyticsHelper?.trackEvent(
                event = "onboarding_finished",
                params = null
            )
            storage?.saveBoolean("showOnboarding", false)
            navigator.replace(MainScreen())
        }
    }
}

