package com.haumealabs.kmpbase.architecture.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class SlideData(val title: String, val subtitle: String, val boldPart: String, val boldSubtitlePart: String?, val image: DrawableResource)

@Composable
fun SlideContent(
    slideData: SlideData,
    textColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = buildAnnotatedString {
                val startIndex = slideData.title.indexOf(slideData.boldPart)
                if (startIndex == -1) {
                    append(slideData.title)
                } else {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
                        append(slideData.title.substring(0, startIndex))
                    }

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Black)) {
                        append(slideData.boldPart)
                    }

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
                        append(slideData.title.substring(startIndex + slideData.boldPart.length))
                    }
                }
            },
            fontSize = 40.sp,
            textAlign = TextAlign.Center,
            color = textColor,
            modifier = Modifier.padding(top = 16.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(slideData.image),
            contentDescription = slideData.title
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = buildAnnotatedString {
                if (slideData.boldSubtitlePart == null) {
                    append(slideData.subtitle)
                } else {
                    val startIndex = slideData.subtitle.indexOf(slideData.boldSubtitlePart)
                    if (startIndex == -1) {
                        append(slideData.subtitle)
                    } else {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
                            append(slideData.subtitle.substring(0, startIndex))
                        }

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(slideData.boldSubtitlePart)
                        }

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
                            append(slideData.subtitle.substring(startIndex + slideData.boldSubtitlePart.length))
                        }

                    }
                }
            },
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            color = textColor,
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Onboarding(
    slides: List<SlideData>,
    buttonNextMessage: String,
    buttonEndMessage: String,
    backgroundColor: Color,
    textColor: Color,
    onFinish: () -> Unit) {
    var currentSlideIndex by remember { mutableStateOf(0) }
    val isLastSlide = currentSlideIndex == slides.size - 1

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedContent(
                targetState = currentSlideIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(durationMillis = 300)) + fadeIn())
                            .togetherWith(slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }, animationSpec = tween(durationMillis = 300)) + fadeOut())
                    } else {
                        (slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }, animationSpec = tween(durationMillis = 300)) + fadeIn())
                            .togetherWith(slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(durationMillis = 300)) + fadeOut())
                    }
                }
            ) { targetIndex ->
                SlideContent(
                    slideData = slides[targetIndex],
                    textColor = textColor
                )
            }
        }

        // Improved Button
        Button(
            onClick = {
                if (isLastSlide) {
                    onFinish()
                } else {
                    currentSlideIndex++
                }
            },
            modifier = Modifier
                .fillMaxWidth() // Make button wider
                .padding(horizontal = 32.dp, vertical = 16.dp), // Add horizontal and vertical padding
            shape = RoundedCornerShape(percent = 50), // Fully rounded corners (pill shape)
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.White, // White background
                contentColor = MaterialTheme.colors.primary // Primary color for text
            ),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            Text(
                text = if (isLastSlide) buttonEndMessage else buttonNextMessage,
                fontWeight = FontWeight.Bold, // Make text bold
                modifier = Modifier.padding(vertical = 8.dp) // Add vertical padding inside button
            )
        }
    }
}