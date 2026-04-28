# AGENTS.md

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

Do not convert this project into Flutter, React Native, or a web app.

---

## Main App Concept

Build an English vocabulary learning app similar to TEPPEN-style word training.

The app should help users study English words efficiently using:

- Word list
- Word detail
- Favorite words
- Learned status
- Quiz mode
- Multiple-choice questions
- Result screen
- Progress tracking
- Local persistence

The app should work locally on the user's Android phone.

---

## Core Features

Implement the following features:

1. Home screen
   - Show learning progress
   - Start quiz button
   - Navigate to word list
   - Navigate to settings

2. Word list screen
   - Display English words
   - Display Japanese meanings
   - Search words
   - Filter by favorite / learned / unlearned
   - Tap a word to open detail screen

3. Word detail screen
   - English word
   - Japanese meaning
   - Example sentence
   - Japanese translation of example sentence
   - Favorite toggle
   - Learned toggle

4. Quiz screen
   - Multiple-choice quiz
   - Show one English word or Japanese meaning
   - Provide 4 answer choices
   - Judge correct / incorrect
   - Move to next question

5. Result screen
   - Show score
   - Show number of correct answers
   - Show number of wrong answers
   - Allow retry
   - Return to home

6. Settings screen
   - Reset progress
   - Display app information
   - Simple learning settings if needed

---

## Data Rules

Use local storage first.

Preferred options:

- Room database for word data and learning progress
- DataStore for simple settings
- JSON seed data for initial vocabulary list

Do not require a backend server unless explicitly requested.

---

## Architecture Rules

Use a simple MVVM structure.

Recommended structure:

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
