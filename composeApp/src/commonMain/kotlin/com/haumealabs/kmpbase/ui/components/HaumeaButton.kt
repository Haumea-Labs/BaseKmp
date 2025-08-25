package com.haumealabs.kmpbase.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import kotlin.text.uppercase

@Composable
fun HaumeaButton(
    modifier: Modifier = Modifier,
    text: String,
    animatedCircleColor: Color = Color(0xFFFFC107),
    width: Dp? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit) {

    var fontSize = when {
        text.length <= 8 -> 20.sp
        text.length <= 12 -> 16.sp
        text.length <= 16 -> 14.sp
        else -> 12.sp
    }

    if (icon != null) {
        fontSize = (fontSize.value - 2).sp
    }

    val  newModifier = if (width == null) {
        modifier
            .height(48.dp)
            .fillMaxWidth()

    } else {
        modifier
            .height(48.dp)
            .width(width)

    }


    Button(
        modifier = newModifier,
        colors = ButtonDefaults.buttonColors(backgroundColor = animatedCircleColor),
        enabled = enabled,
        onClick = {
            onClick()
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (icon != null) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    imageVector = icon,
                    contentDescription = "Arrow Icon",
                    tint = Color.White
                )
            }
            Text(
                modifier = Modifier.weight(1f),
                text = text.uppercase(),
                color = Color.White,
                fontFamily = FontFamily(Font(Res.font.montserrat_black)),
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                textAlign = TextAlign.Center
            )
        }

    }
}