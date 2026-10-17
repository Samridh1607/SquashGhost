# Squash Ghosting Trainer — V2 (Android)

> **Version 2.0** · native Kotlin rewrite of the V1 Python/Kivy app (`../../V1/main.py`).
> For the project overview and the V1 vs V2 comparison, see the [main README](../../README.md).

Open this `SquashGhosting` folder in Android Studio Narwhal (or newer), let Gradle sync,
and run the `app` configuration on a device or emulator.

## Toolchain
| | Version |
|---|---|
| Gradle wrapper | 8.14.3 |
| Android Gradle Plugin | 8.11.1 |
| Kotlin | 2.2.0 |
| compileSdk / targetSdk | 36 |
| minSdk | 26 (Android 8.0; adaptive icon only, no PNG mipmaps needed) |
| JDK | 17 (Android Studio's bundled JBR is fine) |

## Build & test
```
gradlew assembleDebug        # debug APK -> app/build/outputs/apk/debug/
gradlew testDebugUnitTest    # JUnit tests for sequences and voice detection
```
Use `./gradlew` on macOS/Linux.

## Structure
```
app/src/main/java/com/squashghosting/trainer/
  core/   CourtPosition, Pattern, SequenceGenerator, VoiceGender/VoiceNames  (pure Kotlin)
  audio/  BeepPlayer (in-memory sine tones), CoachSpeaker (TextToSpeech + voice ranking)
  ui/     TrainingViewModel (session state machine), MainActivity (views)
app/src/main/res/
  layout/activity_main.xml       single training screen
  values/dimens.xml              text sizes, spacing, button size
  values/themes.xml              theme + FitText / SliderLabel / ActionButton styles
  drawable/ic_launcher_*.xml     adaptive icon (background, foreground, monochrome)
app/src/test/                    JUnit tests
```

## Carried over from V1
| V1 (Python / Kivy) | V2 (Android) |
|---|---|
| 6 court positions, sets of shuffled calls | `CourtPosition`; the V1 behaviour is now the **Star** pattern |
| Sets 5–15, set delay 10–30 s, instruction delay 3–5 s sliders | Same ranges and defaults (Material sliders) |
| `Clock.schedule_once` + `scheduled_events` list | `Handler.postAtTime(..., token, ...)`, cancelled with `removeCallbacksAndMessages(token)` |
| New `TextToSpeech` created for every call (pyjnius) | One `CoachSpeaker` instance, speaks only after the engine reports ready |
| Desktop TTS branches (PowerShell / `say` / espeak) | Dropped — V2 is Android only |
| Start / End buttons, status and progress lines | Same controls and messages |
| `on_stop` | `TrainingViewModel.onCleared()` (stops speech, cancels pending calls) |

## New in V2
- **5 patterns:** Front Two Corners, Backcourt / Service Box, Diagonal & Half-Moon, Random / Rally, Star.
  Sequences are randomly mirrored, and a set never starts on the previous set's last call.
- **Female / male voice:** picks the best installed offline English voice of that gender (device
  accent first), with a small pitch/rate touch-up. Changeable mid-session; previews when idle.
- **Audio cues:** 3-second countdown (660 Hz beeps) and a 1000 Hz beep at the start of each set.
- **Uninterrupted sessions:** screen kept on while training; a partial wake lock (2 h safety
  timeout) keeps calls going if the screen turns off; the session lives in a `ViewModel`, so
  rotation doesn't interrupt it.
- **Live adjustment:** voice and instruction delay can change during a session; pattern, number of
  sets and set delay are locked while training.
- **UI:** dark Material 3 theme, text that auto-shrinks to fit narrow screens, compact 50 dp
  buttons, and a new futuristic adaptive icon (`icon_preview.png`).

## Customising the UI
All sizes live in `res/values/dimens.xml` (text maximums, the `text_min` auto-shrink floor,
spacing and `button_height`). Change them there rather than in the layout.

## Permissions
`AndroidManifest.xml` declares `WAKE_LOCK` and a `<queries>` entry for `TTS_SERVICE`
(required on Android 11+ for the app to reach the text-to-speech engine).

## Changelog
- **2.0** — Native Kotlin rewrite of V1, training patterns, voice selection, beeps,
  wake lock, new icon, fit-to-screen layout with smaller buttons.
