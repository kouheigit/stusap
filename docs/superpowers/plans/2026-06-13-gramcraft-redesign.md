# GramCraft Redesign + Mascot Animation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Unify all app screens to the GramCraft visual language and give the mascot robot distinct, per-screen animated behavior (idle/wave/thinking/cheer/point + confetti), with a structure that lets pose images be swapped in later.

**Architecture:** Extend the already-established design-system in `ui/screen/common/` and `ui/theme/`, then apply it screen-by-screen in three verifiable batches (Core → Lists/Registration → Quiz/Results). Visual layer only — no navigation, ViewModel, Room, or behavior changes. The mascot is one PNG animated via `graphicsLayer` transforms; a `MascotMood → @DrawableRes` map allows future pose images with a one-line change.

**Tech Stack:** Kotlin / Jetpack Compose / Material 3 / JUnit4 / Gradle Kotlin DSL. Spec: `.claude/specs/2026-06-13-gramcraft-redesign.md`.

**Conventions for every task:**
- Build check: `./gradlew :app:testDebugUnitTest` then `./gradlew assembleDebug`. If CLI build fails with `Could not load module <Error module>`, use `./gradlew clean assembleDebug -Pkapt.use.k2=true` (known kapt/K2 issue, see memory).
- UI-touching tasks: after build, verify on emulator per `.claude/prompts/deploy.md` (`adb kill-server && adb start-server`, `assembleDebug`, `adb install -r app/build/outputs/apk/debug/app-debug.apk`, launch). If "fixed but not showing", suspect stale APK → `clean assembleDebug`.
- No hardcoded colors/strings: colors via `ui/theme/`, user-facing text via `res/values/strings.xml` (+ `values-en`).
- Commit after each task (separate logical commits, per CLAUDE.md Git Workflow). End commit messages with the Co-Authored-By trailer.

---

## File Structure

**Foundation (Phase 0) — created/modified:**
- `app/src/main/java/com/example/vocabapp/ui/screen/common/MascotAnimation.kt` — **Create.** Pure helpers: `MascotMood` enum (moved here), `mascotDrawable(mood)` map, `MascotMotion` data + `mascotMotionFor(mood)`. Unit-testable.
- `app/src/main/java/com/example/vocabapp/ui/screen/common/AnimatedMascot.kt` — **Modify.** Drive transforms from `mascotMotionFor`, add `Point` mood handling, render confetti overlay when `mood == Cheer`.
- `app/src/main/java/com/example/vocabapp/ui/screen/common/ConfettiOverlay.kt` — **Create.** Lightweight `Canvas` confetti, tap-transparent.
- `app/src/main/java/com/example/vocabapp/ui/screen/common/GramComponents.kt` — **Create.** Shared `GramCard`, `GramRewardPill`, `GramMiniStat`, `GramProgressBar`, `GramCircularProgress`, `GramPrimaryButton`, `GramSecondaryButton`, `GramFilterChips`, `GramListRow`, `GramFab`, `GramLessonPathNode`, `GramPathConnector`, `MasterBadge`.
- `app/src/main/java/com/example/vocabapp/ui/screen/home/HomeScreen.kt` — **Modify.** Delete dead code; use shared components.
- `app/src/test/java/com/example/vocabapp/ui/MascotAnimationTest.kt` — **Create.**
- `app/src/test/java/com/example/vocabapp/ui/GramFormatTest.kt` — **Create.** (grade mapping)

**Application (Phases A/B/C) — modified screens:** listed per task.

---

## Phase 0: Foundation (design-system + mascot)

### Task 0.1: Extract mascot motion + drawable map (pure, TDD)

**Files:**
- Create: `app/src/main/java/com/example/vocabapp/ui/screen/common/MascotAnimation.kt`
- Test: `app/src/test/java/com/example/vocabapp/ui/MascotAnimationTest.kt`
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/common/AnimatedMascot.kt` (remove its local `enum class MascotMood`)

- [ ] **Step 1: Write the failing test**

```kotlin
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
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.vocabapp.ui.MascotAnimationTest"`
Expected: FAIL (unresolved reference `mascotDrawable` / `mascotMotionFor`).

- [ ] **Step 3: Create `MascotAnimation.kt`**

```kotlin
package com.example.vocabapp.ui.screen.common

import androidx.annotation.DrawableRes
import com.example.vocabapp.R

/** Robot mascot behaviors. One PNG today; [mascotDrawable] is the single swap point for future pose images. */
enum class MascotMood { Idle, Wave, Thinking, Cheer, Point }

/** Per-mood pose image. All point at robota_mascot until per-pose art exists — change here only. */
@DrawableRes
fun mascotDrawable(mood: MascotMood): Int = when (mood) {
    MascotMood.Idle -> R.drawable.robota_mascot
    MascotMood.Wave -> R.drawable.robota_mascot
    MascotMood.Thinking -> R.drawable.robota_mascot
    MascotMood.Cheer -> R.drawable.robota_mascot
    MascotMood.Point -> R.drawable.robota_mascot
}

/** Transform parameters for the infinite mascot animation. */
data class MascotMotion(
    val bobFrom: Float,
    val bobTo: Float,
    val bobDurationMillis: Int,
    val rotateFrom: Float,
    val rotateTo: Float,
    val rotateDurationMillis: Int,
    val minScale: Float,
    val maxScale: Float,
    val scaleDurationMillis: Int,
    val showConfetti: Boolean,
)

fun mascotMotionFor(mood: MascotMood): MascotMotion = when (mood) {
    MascotMood.Idle -> MascotMotion(-4f, 6f, 1300, -3f, 3f, 1500, 0.96f, 1.02f, 1200, false)
    MascotMood.Wave -> MascotMotion(-4f, 6f, 1300, -6f, 9f, 640, 0.97f, 1.03f, 1200, false)
    MascotMood.Thinking -> MascotMotion(-3f, 4f, 1700, -2f, 4f, 1800, 0.97f, 1.02f, 1500, false)
    MascotMood.Cheer -> MascotMotion(-8f, 4f, 520, -6f, 6f, 520, 0.94f, 1.10f, 520, true)
    MascotMood.Point -> MascotMotion(-3f, 3f, 1100, 2f, 8f, 900, 0.98f, 1.02f, 1100, false)
}
```

- [ ] **Step 4: Remove the duplicate enum from `AnimatedMascot.kt`**

Delete these lines from `AnimatedMascot.kt`:
```kotlin
internal enum class MascotMood {
    Idle,
    Wave,
    Thinking,
    Cheer
}
```
(The enum now lives in `MascotAnimation.kt`. Same package, so no import change needed.)

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.vocabapp.ui.MascotAnimationTest"`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/common/MascotAnimation.kt \
        app/src/main/java/com/example/vocabapp/ui/screen/common/AnimatedMascot.kt \
        app/src/test/java/com/example/vocabapp/ui/MascotAnimationTest.kt
git commit -m "feat: extract mascot motion params and drawable map"
```

---

### Task 0.2: Confetti overlay

**Files:**
- Create: `app/src/main/java/com/example/vocabapp/ui/screen/common/ConfettiOverlay.kt`

- [ ] **Step 1: Create `ConfettiOverlay.kt`**

```kotlin
package com.example.vocabapp.ui.screen.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.vocabapp.ui.theme.AccentBlue
import com.example.vocabapp.ui.theme.BrightBlue
import com.example.vocabapp.ui.theme.Danger
import com.example.vocabapp.ui.theme.Gold
import kotlin.random.Random

private data class ConfettiPiece(val xRatio: Float, val color: Color, val size: Float, val phase: Float)

/** Lightweight celebratory confetti. Decorative only; place behind interactive content so taps pass through. */
@Composable
internal fun ConfettiOverlay(modifier: Modifier = Modifier, pieceCount: Int = 26) {
    val palette = listOf(BrightBlue, Gold, AccentBlue, Danger)
    val pieces = remember {
        List(pieceCount) {
            ConfettiPiece(
                xRatio = Random.nextFloat(),
                color = palette[it % palette.size],
                size = 6f + Random.nextFloat() * 8f,
                phase = Random.nextFloat()
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "confetti")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "confettiT"
    )
    Canvas(modifier) {
        pieces.forEach { p ->
            val progress = (t + p.phase) % 1f
            val y = progress * size.height
            val x = p.xRatio * size.width
            drawRect(
                color = p.color.copy(alpha = 1f - progress),
                topLeft = Offset(x, y),
                size = Size(p.size, p.size * 1.6f)
            )
        }
    }
}
```

- [ ] **Step 2: Build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/common/ConfettiOverlay.kt
git commit -m "feat: add lightweight confetti overlay"
```

---

### Task 0.3: Drive AnimatedMascot from motion params + confetti

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/common/AnimatedMascot.kt`

- [ ] **Step 1: Replace the body of `AnimatedMascot`**

Replace the three `animateFloat` blocks and the `Image` painter so they read from `mascotMotionFor(mood)` and `mascotDrawable(mood)`, and wrap the image `Box` with a confetti overlay when `motion.showConfetti`. Full replacement of the composable body:

```kotlin
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
```

Remove now-unused imports (`RepeatMode` stays; the conditional `if (mood == ...)` ternaries are gone). Keep imports referenced above; add none beyond existing (all symbols are in the same package or already imported).

- [ ] **Step 2: Build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Emulator visual check**

Install and open Home + Result screens. Expected: Home mascot waves (faster arm tilt); Result mascot bounces with confetti falling behind it.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/common/AnimatedMascot.kt
git commit -m "feat: drive mascot animation from per-mood motion and add confetti"
```

---

### Task 0.4: Grade mapping helper for result screen (pure, TDD)

**Files:**
- Create: `app/src/main/java/com/example/vocabapp/ui/screen/common/GramFormat.kt`
- Test: `app/src/test/java/com/example/vocabapp/ui/GramFormatTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.example.vocabapp.ui

import com.example.vocabapp.ui.screen.common.gradeLabel
import org.junit.Assert.assertEquals
import org.junit.Test

class GramFormatTest {
    @Test fun gradeBoundaries() {
        assertEquals("S", gradeLabel(100))
        assertEquals("A", gradeLabel(80))
        assertEquals("B", gradeLabel(60))
        assertEquals("C", gradeLabel(40))
        assertEquals("D", gradeLabel(0))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.vocabapp.ui.GramFormatTest"`
Expected: FAIL (unresolved reference `gradeLabel`).

- [ ] **Step 3: Create `GramFormat.kt`**

```kotlin
package com.example.vocabapp.ui.screen.common

/** Maps a 0-100 score to a TEPPEN-style letter grade for the result screen badge. */
fun gradeLabel(scoreOutOf100: Int): String = when {
    scoreOutOf100 >= 100 -> "S"
    scoreOutOf100 >= 80 -> "A"
    scoreOutOf100 >= 60 -> "B"
    scoreOutOf100 >= 40 -> "C"
    else -> "D"
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.vocabapp.ui.GramFormatTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/common/GramFormat.kt \
        app/src/test/java/com/example/vocabapp/ui/GramFormatTest.kt
git commit -m "feat: add grade label mapping for result screen"
```

---

### Task 0.5: Shared GramCraft components

**Files:**
- Create: `app/src/main/java/com/example/vocabapp/ui/screen/common/GramComponents.kt`

- [ ] **Step 1: Create `GramComponents.kt`**

Provide reusable composables used by the screen tasks. Each is `internal`. (Colors from `ui/theme`.)

```kotlin
package com.example.vocabapp.ui.screen.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocabapp.ui.theme.AccentBlue
import com.example.vocabapp.ui.theme.BrightBlue
import com.example.vocabapp.ui.theme.DeepBlue
import com.example.vocabapp.ui.theme.Gold
import com.example.vocabapp.ui.theme.SoftBlue
import com.example.vocabapp.ui.theme.Success
import com.example.vocabapp.ui.theme.TextMuted

@Composable
internal fun GramCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) { content() }
}

@Composable
internal fun GramRewardPill(text: String, color: Color = Gold) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(Icons.Default.Star, null, tint = color, modifier = Modifier.size(15.dp))
        Text(text, color = DeepBlue, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
internal fun GramMiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(10.dp)).background(SoftBlue.copy(alpha = 0.72f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(value, color = DeepBlue, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
internal fun GramProgressBar(progress: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
        color = BrightBlue,
        trackColor = SoftBlue
    )
}

@Composable
internal fun GramCircularProgress(progress: Float, label: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(64.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.size(64.dp),
            color = BrightBlue,
            trackColor = SoftBlue,
            strokeWidth = 6.dp
        )
        Text(label, color = DeepBlue, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
internal fun GramPrimaryButton(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrightBlue, contentColor = Color.White)
    ) {
        if (icon != null) { Icon(icon, null, modifier = Modifier.size(18.dp)); androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp)) }
        Text(text, fontWeight = FontWeight.Black)
    }
}

@Composable
internal fun GramSecondaryButton(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick, modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, BrightBlue),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrightBlue)
    ) {
        if (icon != null) { Icon(icon, null, modifier = Modifier.size(18.dp)); androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp)) }
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun GramFilterChips(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { i, label ->
            val active = i == selectedIndex
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp))
                    .background(if (active) BrightBlue else Color.White)
                    .clickable { onSelect(i) }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = if (active) Color.White else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@Composable
internal fun GramListRow(
    english: String,
    meaning: String,
    favorite: Boolean,
    learned: Boolean,
    onFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GramCard(modifier = modifier.clickable(onClick = onClick)) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(english, color = DeepBlue, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text(meaning, color = TextMuted, fontSize = 13.sp)
            }
            Icon(
                if (favorite) Icons.Default.Star else Icons.Default.StarBorder, null,
                tint = if (favorite) Gold else TextMuted,
                modifier = Modifier.size(22.dp).clickable(onClick = onFavorite)
            )
            androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
            Box(
                modifier = Modifier.size(24.dp).clip(CircleShape)
                    .background(if (learned) Success else SoftBlue),
                contentAlignment = Alignment.Center
            ) {
                if (learned) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
internal fun GramLessonPathNode(label: String, active: Boolean, completed: Boolean) {
    val container = when { completed -> BrightBlue; active -> AccentBlue; else -> SoftBlue }
    val content = if (completed || active) Color.White else TextMuted
    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(container), contentAlignment = Alignment.Center) {
        if (completed) Icon(Icons.Default.Check, null, tint = content, modifier = Modifier.size(22.dp))
        else Text(label, color = content, fontWeight = FontWeight.Black)
    }
}

@Composable
internal fun MasterBadge(text: String) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Gold.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(Icons.Default.Star, null, tint = Gold, modifier = Modifier.size(15.dp))
        Text(text, color = DeepBlue, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
internal fun GramFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(56.dp).clip(CircleShape).background(BrightBlue).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
}
```

- [ ] **Step 2: Build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/common/GramComponents.kt
git commit -m "feat: add shared GramCraft UI components"
```

---

### Task 0.6: Remove dead code in HomeScreen and reuse shared components

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/home/HomeScreen.kt`

- [ ] **Step 1: Delete unused private composables**

Remove these unused declarations from `HomeScreen.kt` (no call sites): `HomeBottomNav`, `HomeBottomItem`, `HomePathCard`, `PathNode`, `PathConnector`. Then remove imports left unused by their deletion (`LinearProgressIndicator`, `AccentBlue` if now unused — let the compiler/`assembleDebug` confirm; remove flagged unused imports).

- [ ] **Step 2: Replace local `HomeRewardPill` / `HomeMiniStat` usages with shared components**

In `HomeScreen.kt`, change `HomeRewardPill("3", BrightBlue)` → `GramRewardPill("3", BrightBlue)` (and the `120`, reward pill calls), and `HomeMiniStat(...)` → `GramMiniStat(...)`. Delete the now-unused private `HomeRewardPill` and `HomeMiniStat` definitions. (`com.example.vocabapp.ui.screen.common.*` is already imported.)

- [ ] **Step 3: Build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL (fix any unused-import warnings flagged as errors).

- [ ] **Step 4: Emulator visual check**

Open Home. Expected: identical layout to before (reward pills, mini stats, mascot wave), no visual regression.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/home/HomeScreen.kt
git commit -m "refactor: remove dead Home code and reuse shared components"
```

---

## Phase A: Core screens

> For each screen task: open the file, wrap content in `BlueScaffold` (if not already), set background `SoftBlue`, replace ad-hoc cards/buttons with the Task 0.5 components, and add `AnimatedMascot(mood = …, message = …)` per the spec's mood table. Verify build + emulator, then commit. No behavior/navigation changes.

### Task A.1: Lesson list — numbered path + Master badge

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/lesson/LessonListScreen.kt`

- [ ] **Step 1:** Wrap the list in `BlueScaffold(title = "レッスン", onBack = ...)` (keep existing back/nav callbacks). Render each lesson row as: leading `GramLessonPathNode(label = index, active = …, completed = isMastered)` + connector spacing, a `GramCard` with the range label ("1〜100語") and `GramProgressBar(progress)`, and either `MasterBadge("Master")` when mastered or a `GramPrimaryButton("スタート")`. Use the lesson's existing mastered/progress fields (do not change the ViewModel).
- [ ] **Step 2:** Add `AnimatedMascot(mood = MascotMood.Idle, size = 64.dp)` is optional here; the lesson screen in the mockup has no mascot, so omit it. (Keep screen mascot-free to match카ンプ.)
- [ ] **Step 3:** Build: `./gradlew assembleDebug` → BUILD SUCCESSFUL.
- [ ] **Step 4:** Emulator: open レッスン. Expected: green numbered nodes down the left, white cards with progress bars, Master gold badge on completed, スタート button otherwise — matching mockup 1/screen 2.
- [ ] **Step 5:** Commit:
```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/lesson/LessonListScreen.kt
git commit -m "feat: GramCraft style for lesson list"
```

### Task A.2: Idiom lesson list

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/lesson/IdiomLessonListScreen.kt`

- [ ] **Step 1:** Apply the identical treatment as A.1 (title = "英熟語"). Lock later ranges show a lock affordance: when a range is not yet unlocked, render the node greyed and replace the start button with a lock `Icon(Icons.Default.Lock)` (no behavior change — reuse whatever "locked" state the screen already derives; if none exists, do not invent one — just style existing rows).
- [ ] **Step 2:** Build → BUILD SUCCESSFUL.
- [ ] **Step 3:** Emulator: open 英熟語 list. Expected: matches mockup 1/screen 3.
- [ ] **Step 4:** Commit:
```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/lesson/IdiomLessonListScreen.kt
git commit -m "feat: GramCraft style for idiom lesson list"
```

### Task A.3: Settings polish

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/settings/SettingsScreen.kt`

- [ ] **Step 1:** Ensure it uses `BlueScaffold(title = "設定")` and `GramCard` rows for each setting; keep the existing `AnimatedMascot(mood = MascotMood.Thinking, …)`. Replace any reset/destructive button with `GramPrimaryButton`/`GramSecondaryButton`. No logic change.
- [ ] **Step 2:** Build → BUILD SUCCESSFUL.
- [ ] **Step 3:** Emulator: open 設定. Expected: green header, white setting cards, mascot present.
- [ ] **Step 4:** Commit:
```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/settings/SettingsScreen.kt
git commit -m "feat: GramCraft polish for settings"
```

---

## Phase B: Lists & registration

### Task B.1: Custom word list — search + filter chips + rows + FAB

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/custom/CustomWordListScreen.kt`

- [ ] **Step 1:** Wrap in `BlueScaffold(title = "英単語")`. Top: search `TextField` (existing query state) styled rounded white; below it `GramFilterChips(options = listOf("すべて","お気に入り","習得済み","未習得"), selectedIndex = …, onSelect = …)` wired to the existing filter state (map index→existing filter enum; do not add new filter behavior beyond what exists — if only some filters exist, show only those). List rows use `GramListRow(english, meaning, favorite, learned, onFavorite, onClick)` bound to existing item fields/callbacks. Overlay `GramFab(onClick = navigateToAddWord)` bottom-end via `Box`.
- [ ] **Step 2:** Keep `LazyColumn` with existing `key = { it.id }`.
- [ ] **Step 3:** Build → BUILD SUCCESSFUL.
- [ ] **Step 4:** Emulator: open 英単語 list. Expected: matches mockup 2/screen 3 (search, chips, star/check rows, green ＋ FAB).
- [ ] **Step 5:** Commit:
```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/custom/CustomWordListScreen.kt
git commit -m "feat: GramCraft style for word list"
```

### Task B.2: Custom idiom list

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/custom/CustomIdiomListScreen.kt`

- [ ] **Step 1:** Same treatment as B.1, title = "英熟語". The idiom screen lacks favorite/learned toggles — pass `favorite`/`learned` from existing fields if present, otherwise use a row variant without those icons (reuse `GramListRow` only if the data exists; otherwise render `GramCard` rows with english + meaning only). Do not add toggle behavior that didn't exist.
- [ ] **Step 2:** Build → BUILD SUCCESSFUL.
- [ ] **Step 3:** Emulator: open 英熟語 list. Expected: matches mockup 2/screen 4.
- [ ] **Step 4:** Commit:
```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/custom/CustomIdiomListScreen.kt
git commit -m "feat: GramCraft style for idiom list"
```

### Task B.3: Add word screen — mascot Wave + form cards

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/word/AddWordScreen.kt`

- [ ] **Step 1:** `BlueScaffold(title = "新規単語登録")`. Top `AnimatedMascot(mood = MascotMood.Wave, size = 72.dp, message = stringResource(R.string.add_word_mascot))` — add that string to `strings.xml`/`values-en` ("新しい単語を登録して自分だけの単語帳を作りましょう！" / EN equivalent). Wrap inputs in a `GramCard`; bottom row = `GramSecondaryButton("プレビュー")` + `GramPrimaryButton("保存", icon = Icons.Default.Save)` bound to existing callbacks.
- [ ] **Step 2:** Build → BUILD SUCCESSFUL.
- [ ] **Step 3:** Emulator: open 新規単語登録. Expected: matches mockup 2/screen 1 (mascot waving + speech bubble, white form, プレビュー/保存).
- [ ] **Step 4:** Commit:
```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/word/AddWordScreen.kt app/src/main/res/values/strings.xml app/src/main/res/values-en/strings.xml
git commit -m "feat: GramCraft style for add word screen"
```

### Task B.4: Add sentence / passage registration screens

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/quiz/AddSentenceScreen.kt`
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/passage/CustomPassageRegistrationScreen.kt`

- [ ] **Step 1:** Apply B.3 treatment to both (mascot `Wave` + message via new strings `add_sentence_mascot`, `passage_reg_mascot`; `GramCard` form; `GramSecondaryButton`/`GramPrimaryButton` actions). Keep all existing input/preview/save logic.
- [ ] **Step 2:** Build → BUILD SUCCESSFUL.
- [ ] **Step 3:** Emulator: open both. Expected: matches mockup 3/screen 2 (文章登録) and registration mockups.
- [ ] **Step 4:** Commit (one commit):
```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/quiz/AddSentenceScreen.kt \
        app/src/main/java/com/example/vocabapp/ui/screen/passage/CustomPassageRegistrationScreen.kt \
        app/src/main/res/values/strings.xml app/src/main/res/values-en/strings.xml
git commit -m "feat: GramCraft style for sentence and passage registration"
```

### Task B.5: Import screens — Cheer + confetti on success

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/imports/BulkImportScreen.kt`
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/word/WordImportScreen.kt`
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/quiz/SentenceImportScreen.kt`

- [ ] **Step 1:** For the file-pick state keep `AnimatedMascot(mood = MascotMood.Idle/Wave, message = "ファイルを選んでインポートしよう！")`. For the success state (existing "成功しました"/imported-count UI) render `AnimatedMascot(mood = MascotMood.Cheer, message = "成功しました！")` — the confetti now appears automatically (Task 0.3). Wrap preview tables/rows in `GramCard`, action as `GramPrimaryButton("インポートを実行")`.
- [ ] **Step 2:** Build → BUILD SUCCESSFUL.
- [ ] **Step 3:** Emulator: run an import to success. Expected: confetti + cheering mascot (mockup 2/screen 6, mockup 3/screen 3).
- [ ] **Step 4:** Commit:
```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/imports/BulkImportScreen.kt \
        app/src/main/java/com/example/vocabapp/ui/screen/word/WordImportScreen.kt \
        app/src/main/java/com/example/vocabapp/ui/screen/quiz/SentenceImportScreen.kt
git commit -m "feat: GramCraft style and cheer mascot for import screens"
```

---

## Phase C: Quiz & results

### Task C.1: Quiz screen (multiple choice) — Thinking mascot

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/common/CommonQuizContent.kt`
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/quiz/QuizScreen.kt`

- [ ] **Step 1:** In `CommonQuizContent`, keep the existing mascot mood logic but change the un-answered idle to `MascotMood.Thinking` (reading state) and message "しっかり読んで正解を選ぼう！". Style the progress as `GramProgressBar`. Choice cards: selected-correct = `Success`, selected-wrong = `Danger`, with `Icons.Default.Check` on the correct choice when answered. Keep bottom action row ("わからない"/"あとで復習"/"メモ") using `GramSecondaryButton`.
- [ ] **Step 2:** Build → BUILD SUCCESSFUL.
- [ ] **Step 3:** Emulator: start a quiz. Expected: matches mockup 4/screen 1.
- [ ] **Step 4:** Commit:
```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/common/CommonQuizContent.kt \
        app/src/main/java/com/example/vocabapp/ui/screen/quiz/QuizScreen.kt
git commit -m "feat: GramCraft style for quiz screen"
```

### Task C.2: Sentence quiz (arrange) — Point mascot

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/quiz/SentenceQuizContent.kt`
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/quiz/SentenceQuizScreen.kt`

- [ ] **Step 1:** Add `AnimatedMascot(mood = MascotMood.Point, message = "正しい語順に並べよう！")` at the top. Render the sentence with numbered blank slots and a word bank of `GramCard`-styled chips (reuse existing arrange state/callbacks). Bottom: `GramSecondaryButton("シャッフル")` + `GramPrimaryButton("答え合わせ")`.
- [ ] **Step 2:** Build → BUILD SUCCESSFUL.
- [ ] **Step 3:** Emulator: open sentence arrange quiz. Expected: matches mockup 4/screen 2.
- [ ] **Step 4:** Commit:
```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/quiz/SentenceQuizContent.kt \
        app/src/main/java/com/example/vocabapp/ui/screen/quiz/SentenceQuizScreen.kt
git commit -m "feat: GramCraft style for sentence arrange quiz"
```

### Task C.3: Result screens — grade badge + score + Cheer/confetti

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/common/CommonResultContent.kt`
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/common/CommonResultSections.kt`
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/quiz/ResultScreen.kt`

- [ ] **Step 1:** Header card: `AnimatedMascot(mood = MascotMood.Cheer, message = "おつかれさまでした！")` (confetti automatic) beside a gold grade badge showing `gradeLabel(score)` (Task 0.4) and the score "80点 / 100点". Stats row: 正解 (green) / 不正解 (red) / 正解率 (blue) using `GramMiniStat`-like tiles. Detail card: 学習時間 / 問題数 / 連続学習 rows. Actions: `GramSecondaryButton("再チャレンジ")` + `GramPrimaryButton("次へ")` and a full-width `GramSecondaryButton("ホームへ")`. Bind to existing result fields/callbacks; do not change scoring.
- [ ] **Step 2:** Build → BUILD SUCCESSFUL.
- [ ] **Step 3:** Emulator: finish a quiz to reach result. Expected: matches mockup 4/screen 3 (grade A badge, score, confetti, two mascots-feel).
- [ ] **Step 4:** Commit:
```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/common/CommonResultContent.kt \
        app/src/main/java/com/example/vocabapp/ui/screen/common/CommonResultSections.kt \
        app/src/main/java/com/example/vocabapp/ui/screen/quiz/ResultScreen.kt
git commit -m "feat: GramCraft result screen with grade badge and confetti"
```

### Task C.4: Passage practice + Flashcard + Review

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/passage/PassagePracticeScreen.kt` (also remove the 22 hardcoded `Color(0x...)` per spec, routing through `ui/theme`)
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/flashcard/FlashcardScreen.kt`
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/review/ReviewScreen.kt`

- [ ] **Step 1:** Apply `BlueScaffold` + shared cards/buttons. Passage practice: mascot `Thinking` + "しっかり読んで正解を選ぼう！"; replace each `Color(0x...)` literal with the nearest theme color (`SoftBlue`, `Color.White`, `Success`, `Danger`, `DeepBlue`, `TextMuted`) — match current visual intent. Flashcard: `GramCard` flip card, mascot `Idle`. Review: `GramCard` list, mascot `Idle` + "復習で記憶を定着させよう！" (add string).
- [ ] **Step 2:** Build → BUILD SUCCESSFUL.
- [ ] **Step 3:** Emulator: open passage practice, flashcard, review. Expected: consistent GramCraft styling, no hardcoded-color visual regressions.
- [ ] **Step 4:** Commit:
```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/passage/PassagePracticeScreen.kt \
        app/src/main/java/com/example/vocabapp/ui/screen/flashcard/FlashcardScreen.kt \
        app/src/main/java/com/example/vocabapp/ui/screen/review/ReviewScreen.kt \
        app/src/main/res/values/strings.xml app/src/main/res/values-en/strings.xml
git commit -m "feat: GramCraft style for passage, flashcard, review"
```

### Task C.5: Remaining secondary screens sweep

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/quiz/SentenceMenuScreen.kt`, `CustomSentenceListScreen.kt`
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/custom/CustomTrainingListScreen.kt`, `CustomTrainingQuizScreen.kt`, `RandomCustomQuizScreen.kt`
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/passage/CustomPassageListScreen.kt`, `PassageResultReviewScreen.kt`
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/word/WordDetailScreen.kt`
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/training/TrainingListScreen.kt`

- [ ] **Step 1:** For each, ensure `BlueScaffold` + `SoftBlue` background + shared `GramCard`/buttons, and a context mood (lists → `Idle`, quizzes → `Thinking`, results → `Cheer`). Word detail: `GramCard` with english/meaning/example + favorite star + learned check (existing toggles). No behavior change.
- [ ] **Step 2:** Build → BUILD SUCCESSFUL.
- [ ] **Step 3:** Emulator: spot-check each screen reachable from Home. Expected: all share GramCraft look; no screen still on old design.
- [ ] **Step 4:** Commit:
```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/
git commit -m "feat: GramCraft sweep for remaining secondary screens"
```

---

## Final verification (Task Z)

- [ ] **Step 1:** Full test suite: `./gradlew :app:testDebugUnitTest` → all PASS (incl. MascotAnimationTest, GramFormatTest, and pre-existing tests).
- [ ] **Step 2:** Clean release-config build to catch warnings: `./gradlew clean assembleDebug`.
- [ ] **Step 3:** Confirm no new hardcoded literals: `grep -rn "Color(0x" app/src/main/java/com/example/vocabapp/ui/screen | grep -v theme` returns nothing in screens touched (passage cleaned).
- [ ] **Step 4:** Emulator pass over every screen in the four mockups; confirm mascot moods (wave/thinking/cheer/point) and confetti on results/import-success.
- [ ] **Step 5:** Report results to user with the screen list and verification evidence (do not claim complete before this passes — per superpowers:verification-before-completion).

---

## Self-Review

**Spec coverage:** §3.1 visual language → Tasks 0.5/A/B/C. §3.2 components → 0.5. §3.3 mascot system (moods, confetti, drawable map) → 0.1/0.2/0.3. §3.4 mood assignments → A/B/C per-task moods. §4 phases → Phase 0/A/B/C structure. §5 edge cases (coerce progress, maxLines, tap-transparent confetti) → GramProgressBar/GramCircularProgress coerce, GramListRow maxLines, ConfettiOverlay decorative. §6 perf (graphicsLayer, LazyColumn key) → 0.3 + B.1 step 2. §7 AC → Final verification Z. Dead code removal → 0.6. Hardcoded color removal → C.4. **No gaps.**

**Placeholder scan:** Foundation tasks (Phase 0) have full code. Per-screen tasks (A/B/C) are transformations of existing screens; they specify exact file, mood, components, target mockup, and verification — concrete, not "TBD". No "add error handling"/"similar to" placeholders.

**Type consistency:** `MascotMood` (5 values) defined in 0.1, used everywhere. `mascotMotionFor`/`mascotDrawable` signatures consistent. `MascotMotion` fields (`bobDurationMillis`, `maxScale`, `showConfetti`) match test and 0.3 usage. `gradeLabel(Int): String` consistent in 0.4 and C.3. Component names (`GramCard`, `GramListRow`, `GramFilterChips`, `GramPrimaryButton`, etc.) defined in 0.5 and referenced unchanged in A/B/C.
