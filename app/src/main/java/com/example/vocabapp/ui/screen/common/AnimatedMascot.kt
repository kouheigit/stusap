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
import com.example.vocabapp.ui.theme.DeepBlue
import com.example.vocabapp.ui.theme.TextMuted


@Composable
internal fun AnimatedMascot(
    modifier: Modifier = Modifier,
    mood: MascotMood = MascotMood.Idle,
    size: Dp = 92.dp,
    message: String? = null
) {
    val motion = mascotMotionFor(mood)
    val transition = rememberInfiniteTransition(label = "robota")
    val bob by transition.animateFloat(
        initialValue = motion.bobFrom, targetValue = motion.bobTo,
        animationSpec = infiniteRepeatable(tween(motion.bobDurationMillis), RepeatMode.Reverse),
        label = "robotaBob"
    )
    val rotate by transition.animateFloat(
        initialValue = motion.rotateFrom, targetValue = motion.rotateTo,
        animationSpec = infiniteRepeatable(tween(motion.rotateDurationMillis), RepeatMode.Reverse),
        label = "robotaRotate"
    )
    val scale by transition.animateFloat(
        initialValue = motion.minScale, targetValue = motion.maxScale,
        animationSpec = infiniteRepeatable(tween(motion.scaleDurationMillis), RepeatMode.Reverse),
        label = "robotaScale"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (motion.showConfetti) {
                ConfettiOverlay(modifier = Modifier.matchParentSize())
            }
            Image(
                painter = painterResource(mascotDrawable(mood)),
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
