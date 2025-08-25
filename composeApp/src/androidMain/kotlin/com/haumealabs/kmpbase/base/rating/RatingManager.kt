package com.haumealabs.kmpbase.base.rating

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

actual class RatingManager(
    private val activity: Activity,
    private val coroutineScope: CoroutineScope
) {

    actual fun requestInAppReview() {
        if (!BuildConfig.DEBUG) {
            coroutineScope.launch {
                try {
                    val reviewManager = ReviewManagerFactory.create(activity)
                    val request = reviewManager.requestReviewFlow()
                    request.addOnCompleteListener {
                        if (it.isSuccessful) {
                            val reviewInfo = it.result
                            val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                            flow.addOnCompleteListener {
                                //Continue your application process
                                println("flow completed")
                            }
                        } else {
                            println("error")
                            //Handle the error here
                        }
                    }
                } catch (e: Exception) {

                }
            }
        }
    }

}