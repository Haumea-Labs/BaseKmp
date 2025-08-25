package com.haumealabs.kmpbase.base.analytics

import com.haumealabs.kmpbase.base.PlatformContext
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics

class AnalyticsHelper {

    private val firebaseAnalytics = Firebase.analytics

    fun trackEvent(event: String, params: List<Pair<String, String>>?) {
        if (!PlatformContext.isDev()) {
            val map = params?.toMap()
            firebaseAnalytics.logEvent(event, map)
        }
    }

}