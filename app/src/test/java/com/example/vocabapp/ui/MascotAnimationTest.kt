package com.example.vocabapp.ui

import com.example.vocabapp.R
import com.example.vocabapp.ui.screen.common.MascotMood
import com.example.vocabapp.ui.screen.common.mascotDrawable
import com.example.vocabapp.ui.screen.common.mascotMotionFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MascotAnimationTest {
    @Test fun everyMoodMapsToRobotaForNow() {
        MascotMood.values().forEach { mood ->
            assertEquals(R.drawable.robota_mascot, mascotDrawable(mood))
        }
    }

    @Test fun cheerIsFasterAndBigger_thanIdle() {
        val idle = mascotMotionFor(MascotMood.Idle)
        val cheer = mascotMotionFor(MascotMood.Cheer)
        assertTrue(cheer.bobDurationMillis < idle.bobDurationMillis)
        assertTrue(cheer.maxScale > idle.maxScale)
        assertTrue(cheer.showConfetti)
    }

    @Test fun onlyCheerShowsConfetti() {
        MascotMood.values().forEach { mood ->
            assertEquals(mood == MascotMood.Cheer, mascotMotionFor(mood).showConfetti)
        }
    }
}
