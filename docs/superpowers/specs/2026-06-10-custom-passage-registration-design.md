# Custom Passage Registration Design

## Goal

Add a "長文問題登録" entry that lets users paste a plain-text long-form reading problem, preview the parsed result, save it locally, and solve it through the existing passage practice UI.

## Input Format

The first version supports one passage document per import.

```text
TITLE: Email about schedule change
TYPE: email
TIME_LIMIT: 300

本文:
Dear ...

Q1: What is the main purpose of this email?
A. ...
B. ...
C. ...
D. ...
ANSWER: B
EXPLANATION: ...
```

Japanese labels are also accepted for the same fields: `タイトル`, `種類`, `制限時間`, `正解`, and `解説`. `本文:` starts the body. Questions use `Q1:` style markers and choices use `A.` through `D.`.

## Behavior

- The user opens "長文問題登録" from Home.
- The screen shows a large paste area, a sample format, a preview button, and save action.
- Preview validates and parses the pasted text into a passage set.
- Validation errors are shown in Japanese and point to the missing or invalid section.
- Saving stores the parsed passage in Room.
- The custom passage list shows saved items newest first.
- Tapping a saved item starts the existing `PassagePracticeScreen` with that saved set.

## Data

Use local Room persistence. A passage set is stored in one table and its questions are stored in a child table. Choices are stored as newline-separated text in the question row to keep the schema simple for this first version.

Stored fields:

- set: title, document type, body, instruction, time limit, created time
- question: number, stem, options, answer index, optional explanation, display order

## Scope

Included:

- Plain-text parser
- Room tables and migration
- Repository and ViewModel
- Registration screen
- Custom passage list screen
- Navigation from Home and into passage practice
- Unit tests for parsing and repository mapping

Excluded for this version:

- AI question generation
- Multiple documents in one import
- Email header fields
- CSV/XLSX import for passage problems
- Editing existing saved passage problems

## Verification

Run:

```bash
GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest
```

For UI changes, install and launch the debug APK, then verify the Home entry, registration screen, list screen, and practice launch render without crashes.
