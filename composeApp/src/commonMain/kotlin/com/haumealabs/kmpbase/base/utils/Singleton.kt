package com.haumealabs.kmpbase.base.utils

import com.haumealabs.kmpbase.base.domain.FileOperations
import com.haumealabs.kmpbase.base.domain.WebSocketManager
import com.haumealabs.kmpbase.base.analytics.AnalyticsHelper
import com.haumealabs.kmpbase.base.rating.RatingManager
import com.haumealabs.kmpbase.base.storage.Storage

class Singleton {

    companion object {
        lateinit var imagePicker: ImagePicker
        lateinit var fileOperations: FileOperations
        val shareUtil = ShareUtil()
        val storage = Storage()
        var ratingManager: RatingManager? = null
        var webSocketManager: WebSocketManager? = null
        var userId: String = "default"
        var proUser: Boolean = false
        val analyticsHelper = AnalyticsHelper()
    }
}