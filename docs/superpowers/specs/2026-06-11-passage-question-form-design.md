# Passage Question Form Design

## Goal

Extend the custom passage registration screen with a structured question setup flow. Users should be able to add one question at a time without writing the `Q1/A/B/ANSWER` text format by hand.

## Flow

For each question:

1. Enter one question stem in a text field.
2. Select how many choices the question has.
3. Show exactly that many choice text fields.
4. Select the correct choice.
5. Show two actions:
   - `設題を増やす`: store the current question and reset the form so the same flow starts again.
   - `問題設定を完了`: store the current question and complete the question setup.

Completing the question setup builds a `PassageSet` preview from the manual title/type/time/body fields and the stored questions. Saving uses the existing custom passage repository.

## Scope

Keep the existing paste-format import. The new structured form is additive and lives on the same screen.

Manual form fields:

- title
- document type
- time limit
- body
- current question stem
- choice count
- choice text fields
- correct answer
- explanation
- completed question list
- question setup completed flag

Validation:

- body must not be blank
- each question stem must not be blank
- all visible choice fields must not be blank
- at least one question is required before completion
- correct answer must be within the selected choice count

## Verification

Add ViewModel unit tests for question-count selection, dynamic choice fields, adding questions, completing setup, and validation errors. Run:

```bash
GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest
```

## Implemented State

The manual registration flow now supports the requested loop:

1. The screen shows one question stem field.
2. The user chooses 2, 3, or 4 choices from a selector.
3. The screen renders the same number of choice input fields.
4. The user chooses the correct answer and can enter an explanation.
5. `設題を増やす` saves the current question and resets the same setup flow.
6. `問題設定を完了` finalizes the question set and builds the preview used for saving.

The screen also shows registered-question summary rows, completion status, and a save-status hint so the user can tell whether the manual question setup has been finalized.

Latest verification on 2026-06-11:

```bash
GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest
```

Result: `BUILD SUCCESSFUL`.
