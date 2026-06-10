# Custom Passage Registration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build local custom long-form passage registration from a pasted plain-text problem format.

**Architecture:** Add a parser in the data/import layer, persist parsed passage sets in Room, expose them through a repository and ViewModel, then route saved sets into the existing passage practice UI. Keep the first version one-document-per-set and local-only.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, Room, Hilt, coroutines, JUnit.

---

### Task 1: Spec And Plan

**Files:**
- Create: `docs/superpowers/specs/2026-06-10-custom-passage-registration-design.md`
- Create: `docs/superpowers/plans/2026-06-10-custom-passage-registration.md`

- [ ] Commit the design and plan.

### Task 2: Parser Tests

**Files:**
- Create: `app/src/test/java/com/example/vocabapp/data/import/PassageTextImportParserTest.kt`

- [ ] Add tests for English labels, Japanese labels, missing body, invalid answer, and missing choices.
- [ ] Run the parser test and verify it fails because the parser does not exist.
- [ ] Commit the failing test.

### Task 3: Parser Implementation

**Files:**
- Create: `app/src/main/java/com/example/vocabapp/data/import/PassageTextImportParser.kt`

- [ ] Implement parsing into domain `PassageSet`.
- [ ] Validate one body, at least one question, 2 to 4 choices, and answer A-D within range.
- [ ] Run parser tests and verify they pass.
- [ ] Commit the parser.

### Task 4: Room Schema

**Files:**
- Create: `app/src/main/java/com/example/vocabapp/data/local/entity/CustomPassageSetEntity.kt`
- Create: `app/src/main/java/com/example/vocabapp/data/local/entity/CustomPassageQuestionEntity.kt`
- Modify: `app/src/main/java/com/example/vocabapp/data/local/dao/CustomContentDao.kt`
- Modify: `app/src/main/java/com/example/vocabapp/data/local/dao/AppDao.kt`
- Modify: `app/src/main/java/com/example/vocabapp/data/local/AppDatabase.kt`
- Modify: `app/src/main/java/com/example/vocabapp/di/AppModule.kt`

- [ ] Add entities, DAO methods, migration 13 to 14, and reset deletion.
- [ ] Run assembleDebug to verify Room schema generation.
- [ ] Commit the schema.

### Task 5: Repository Tests

**Files:**
- Create: `app/src/test/java/com/example/vocabapp/data/repository/CustomPassageRepositoryTest.kt`

- [ ] Add tests for saving a parsed set and mapping stored rows back into a passage set.
- [ ] Run the repository test and verify it fails because the repository does not exist.
- [ ] Commit the failing repository test.

### Task 6: Repository Implementation

**Files:**
- Create: `app/src/main/java/com/example/vocabapp/data/repository/CustomPassageRepository.kt`

- [ ] Implement save, observe summaries, and load by id.
- [ ] Run repository tests and verify they pass.
- [ ] Commit the repository.

### Task 7: ViewModel

**Files:**
- Create: `app/src/main/java/com/example/vocabapp/viewmodel/CustomPassageRegistrationViewModel.kt`
- Create: `app/src/main/java/com/example/vocabapp/viewmodel/CustomPassageListViewModel.kt`

- [ ] Add UI state for raw text, preview, validation errors, save status, list state, and selected-set loading.
- [ ] Run testDebugUnitTest.
- [ ] Commit ViewModels.

### Task 8: Registration UI

**Files:**
- Create: `app/src/main/java/com/example/vocabapp/ui/screen/passage/CustomPassageRegistrationScreen.kt`

- [ ] Build the paste, preview, save, and parsed preview UI.
- [ ] Run assembleDebug.
- [ ] Commit the registration screen.

### Task 9: List And Navigation

**Files:**
- Create: `app/src/main/java/com/example/vocabapp/ui/screen/passage/CustomPassageListScreen.kt`
- Modify: `app/src/main/java/com/example/vocabapp/ui/navigation/Route.kt`
- Modify: `app/src/main/java/com/example/vocabapp/MainActivity.kt`
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/home/HomeScreen.kt`

- [ ] Add routes, Home entries, list screen, and practice launch for saved sets.
- [ ] Run assembleDebug.
- [ ] Commit navigation and list UI.

### Task 10: Final Verification

**Files:**
- Modify only if verification finds a defect.

- [ ] Run `GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest`.
- [ ] Install and launch the debug APK.
- [ ] Verify the UI renders and the custom passage registration flow opens.
- [ ] Commit final fixes or verification notes if no code changes are needed.
