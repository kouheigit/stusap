# Custom Passage Registration Verification

Date: 2026-06-10

## Commands

```bash
GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest
```

Result: `BUILD SUCCESSFUL`

```bash
ls -la app/build/outputs/apk/debug/app-debug.apk
```

Result: APK exists at `app/build/outputs/apk/debug/app-debug.apk`.

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Result: succeeded on the third attempt after the emulator finished initializing package services.

```bash
adb shell am start -n com.example.vocabapp/.MainActivity
```

Result: start command succeeded.

## UI Check

`dumpsys window windows` showed `com.example.vocabapp/com.example.vocabapp.MainActivity` with `mHasSurface=true`, `isReadyForDisplay()=true`, `mViewVisibility=0x0`, and `isVisible=true`.

Visual interaction was blocked by an emulator-side `System UI isn't responding` ANR dialog. Screenshots were captured to `/tmp/stusap-screen.png`, `/tmp/stusap-screen-2.png`, and `/tmp/stusap-screen-3.png`; all showed the SystemUI ANR overlay rather than the app content. Logcat output did not show a `com.example.vocabapp` fatal exception in the captured tail.

## Notes

The final Android runtime check confirms install, launch intent, and a visible app window, but not manual navigation through the new Home entries because the emulator SystemUI overlay blocked interaction.
