package com.example.vocabapp.ui.screen.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocabapp.R
import com.example.vocabapp.ui.theme.DeepBlue
import com.example.vocabapp.ui.theme.TextMuted


@Composable
internal fun AnimatedMascot(
    modifier: Modifier = Modifier,
    mood: MascotMood = MascotMood.Idle,
    size: Dp = 92.dp,
    message: String? = null
) {
    val transition = rememberInfiniteTransition(label = "robota")
    val bob by transition.animateFloat(
        initialValue = -4f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (mood == MascotMood.Cheer) 520 else 1300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "robotaBob"
    )
    val rotate by transition.animateFloat(
        initialValue = when (mood) {
            MascotMood.Wave -> -6f
            MascotMood.Thinking -> -2f
            else -> -3f
        },
        targetValue = when (mood) {
            MascotMood.Wave -> 9f
            MascotMood.Thinking -> 4f
            MascotMood.Cheer -> 6f
            else -> 3f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (mood == MascotMood.Wave) 640 else 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "robotaRotate"
    )
    val scale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = if (mood == MascotMood.Cheer) 1.08f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (mood == MascotMood.Cheer) 520 else 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "robotaScale"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.robota_mascot),
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .graphicsLayer {
                        translationY = bob
                        rotationZ = rotate
                        scaleX = scale
                        scaleY = scale
                    },
                contentScale = ContentScale.Fit
            )
        }
        message?.let {
            Column(
                modifier = Modifier
                    .widthIn(max = 230.dp)
                    .shadow(2.dp, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("GramCraft", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(it, color = DeepBlue, fontSize = 15.sp, fontWeight = FontWeight.Black, lineHeight = 20.sp)
            }
        }
    }
}
