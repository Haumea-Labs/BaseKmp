package com.haumealabs.kmpbase

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.Typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.lexilabs.basic.ads.BasicAds
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import basekmp.composeapp.generated.resources.Res
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.haumealabs.kmpbase.ui.MainScreen
import com.haumealabs.aiphoto.ui.OnboardingScreen
import com.haumealabs.aiphoto.utils.StatusBarColorFactory
import com.haumealabs.kmpbase.base.PlatformContext
import com.haumealabs.kmpbase.base.domain.FileOperations
import com.haumealabs.kmpbase.base.domain.WebSocketManager
import com.haumealabs.kmpbase.base.domain.createHttpClient
import com.haumealabs.kmpbase.base.rating.RatingManager
import com.haumealabs.kmpbase.base.utils.ImagePicker
import com.haumealabs.kmpbase.base.utils.Singleton
import com.haumealabs.kmpbase.base.utils.Singleton.Companion.proUser
import com.haumealabs.kmpbase.base.utils.Singleton.Companion.storage
import com.haumealabs.kmpbase.base.utils.Singleton.Companion.userId
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppTypography(): Typography {
// Define FontFamily inside the composable scope using remember
    val interVariableFontFamily =
        FontFamily(
            Font(
                Res.font.Inter,
                weight = FontWeight.Normal,
                style = FontStyle.Normal
            ),
            Font(
                Res.font.InterItalic,
                weight = FontWeight.Normal,
                style = FontStyle.Italic
            )
        )

    return Typography(
        body1 = TextStyle(
            fontFamily = interVariableFontFamily,
            fontSize = 14.sp
        ),
    )
}

@OptIn(DependsOnGoogleMobileAds::class)
@Composable
fun App(
    fileOperations: FileOperations,
    imagePicker: ImagePicker,
    ratingManager: RatingManager
) {

    BasicAds.initialize(PlatformContext.getContext())

    Singleton.imagePicker = imagePicker
    Singleton.fileOperations = fileOperations
    Singleton.ratingManager = ratingManager

    PlatformContext.getRevenueCatApiKey()?.let { key ->
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(apiKey = key)

        Purchases.sharedInstance.getCustomerInfo(
            onError = { error -> /* Optional error handling */},
            onSuccess = { customerInfo ->
                userId = customerInfo.originalAppUserId
                if (customerInfo.entitlements["PRO User"]?.isActive == true) {
                    proUser = true
                }
            }
        )
    }

    // Create Json instance (still needed for WebSocketManager)
    val json = remember {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }

    // Use the factory to create the platform-specific HttpClient
    val webSocketClient = remember { createHttpClient() }
    val webSocketManager = remember { WebSocketManager(webSocketClient, json) }
    Singleton.webSocketManager = webSocketManager
    val currentVersion = 1
    val previousVersion = storage.loadInt("currentVersion") ?: 0

    if (previousVersion == 0 || previousVersion < currentVersion) {
        storage?.saveBoolean("showMotD", true)
    }

    storage?.saveInt("currentVersion", currentVersion)

    // Set status bar color
    val statusBarColor = StatusBarColorFactory.createStatusBarColor()

    LaunchedEffect(Unit) {
        statusBarColor.setStatusBarColor(Color(0xFFFFC107))
        // Clear cache on app start
        Singleton.fileOperations.clearCache()
        println("Cache cleared on startup.")
    }

    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primary = Color(0xFFFFC107),
            surface = MaterialTheme.colors.surface,
            background = MaterialTheme.colors.background,
        ),
        typography = AppTypography(),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            var navigator by remember { mutableStateOf<Navigator?>(null) }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(Res.string.app_name),
                                fontSize = 32.sp,
                                color = Color.White,
                                fontFamily = FontFamily(Font(Res.font.leckerli)),
                            )
                        },
                        navigationIcon = if (navigator?.canPop == true) {
                            {
                                IconButton(onClick = {
                                    navigator!!.pop()
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                            }
                        } else null,
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.onPrimary
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {

                    val showOnboarding = storage?.loadBoolean("showOnboarding", true) ?: true
                    if (showOnboarding) {
                        Navigator(OnboardingScreen()) { nav ->
                            navigator = nav
                            SlideTransition(nav)
                        }
                    } else {
                        Navigator(MainScreen()) { nav ->
                            navigator = nav
                            SlideTransition(nav)
                        }
                    }


                }
            }
        }
    }


}