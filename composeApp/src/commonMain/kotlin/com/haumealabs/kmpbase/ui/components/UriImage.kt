package com.haumealabs.kmpbase.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage

@Composable
fun UriImage(
    modifier: Modifier,
    uri: String
) {

    Box(modifier = modifier) {
        // Background image
        AsyncImage(
            model = uri,
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )

        AsyncImage(
            model = uri,
            contentDescription = "Selected Photo",
            Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun CancellableUriImage(
    modifier: Modifier,
    uri: String,
    onCancel: () -> Unit
) {
    val squareSize = remember { mutableIntStateOf(0) }
    Box(modifier = modifier) {

        // Main image
        AsyncImage(
            model = uri,
            contentDescription = "Selected Photo",
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .onGloballyPositioned { coordinates ->
                    squareSize.intValue = coordinates.size.width
                },
            contentScale = ContentScale.Fit
        )

        // Cancel button overlay
        Box(
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.TopEnd)
                .offset(
                    x = (-16).dp,
                    y = 16.dp
                )
                .zIndex(1f)
                .background(
                    color = Color.Red,
                    shape = CircleShape
                )
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "×",
                color = Color.White,
                style = MaterialTheme.typography.h5,
            )
        }
    }
}