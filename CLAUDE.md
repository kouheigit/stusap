# CLAUDE.md

## Project Overview

This is a native Android application project.

The goal is to build a TEPPEN-style English vocabulary learning app for Android.

Use:

- Kotlin
- Android Studio
- Jetpack Compose
- Material 3
- MVVM architecture
- Gradle Kotlin DSL

Do not use Flutter, React Native, or web technologies unless explicitly requested.

---

## App Concept

Build an English vocabulary learning app inspired by TEPPEN-style word training.

The app should allow users to study English words using:

- Word list
- Word detail screen
- Favorite words
- Learned status
- Quiz mode
- Multiple-choice questions
- Result screen
- Progress tracking
- Local data storage

The app should be installable and usable on a local Android smartphone.

---

## Required Screens

Create and maintain these screens:

1. Home screen
   - Learning progress
   - Start quiz button
   - Word list navigation
   - Settings navigation

2. Word list screen
   - English word list
   - Japanese meanings
   - Search function
   - Favorite filter
   - Learned / unlearned filter
   - Detail screen navigation

3. Word detail screen
   - English word
   - Japanese meaning
   - Example sentence
   - Japanese translation
   - Favorite toggle
   - Learned toggle

4. Quiz screen
   - Multiple-choice question
   - 4 answer choices
   - Correct / incorrect judgment
   - Next question button

5. Result screen
   - Score
   - Correct count
   - Wrong count
   - Retry button
   - Back to home button

6. Settings screen
   - Reset progress
   - App information
   - Simple settings if needed

---

## Data Design

Use local persistence first.

Preferred data choices:

- Room database for vocabulary and progress
- DataStore for settings
- JSON seed data for initial word data

Do not add a backend API unless explicitly requested.

Example word data should include:

- id
- english word
- Japanese meaning
- example sentence
- Japanese translation
- category
- level
- favorite flag
- learned flag

---

## Architecture

Use MVVM.

Recommended directory structure:

```text
app/src/main/java/com/example/teppenenglish/
├── MainActivity.kt
├── ui/
│   ├── screens/
│   ├── components/
│   └── theme/
├── viewmodel/
├── data/
│   ├── local/
│   ├── repository/
│   └── model/
└── domain/
```

---

# Workflow Rules

## Git Workflow

- When the user specifies a number of commits/pushes, follow that exact number.
- Do not reduce or consolidate commit counts unless explicitly requested.
- Always verify before commit.
- Never commit unverified UI changes.
- Keep commits logically separated whenever possible.

---

## UI Verification Rules

- After any UI/CSS/layout change, always rebuild the project.
- Verify the UI visually before committing.
- Do not assume UI changes are correct without checking rendering.
- Do not modify unrelated UI components.
- Make only the minimum necessary UI change.

---

## Android Workflow

Before Android install/deploy:

1. Run:
   adb kill-server && adb start-server

2. Build project:
   ./gradlew assembleDebug

3. Install APK:
   adb install -r app/build/outputs/apk/debug/app-debug.apk

4. Verify installation succeeded.

5. Launch app after install.

If ADB fails, retry up to 3 times before stopping.

---

## Bug Investigation Rules

- If the same bug is not fixed after 2 attempts:
  - stop making surface-level fixes
  - investigate root cause
  - list assumptions
  - verify assumptions before editing more code

- Avoid repeated trial-and-error fixes without diagnosis.

---

## Change Scope Rules

- Do not introduce behavior changes not requested by the user.
- Do not remove or hide UI elements unless explicitly requested.
- Avoid unnecessary refactors during bug fixes.
- Preserve existing behavior whenever possible.

---

## Verification Rules

Before completing a task:

- Build must succeed
- UI must render correctly
- Emulator/browser verification should be completed
- Errors must be checked before commit/push

Never mark work as complete before verification.

