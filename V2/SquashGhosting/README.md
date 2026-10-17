# Squash Ghosting Trainer (Android)

Native Kotlin port of the Kivy `main.py` app. Open the `SquashGhosting` folder in
Android Studio Narwhal, let Gradle sync, and run the `app` configuration.

## Toolchain
| | Version |
|---|---|
| Gradle wrapper | 8.14.3 |
| Android Gradle Plugin | 8.11.1 |
| Kotlin | 2.2.0 |
| compileSdk / targetSdk | 36 |
| minSdk | 26 (adaptive icon only, no PNG mipmaps needed) |
| JDK | 17 (Android Studio's bundled JBR is fine) |

## Structure
```
app/src/main/java/com/squashghosting/trainer/
  core/   CourtPosition, Pattern, SequenceGenerator, VoiceGender/VoiceNames  (pure Kotlin)
  audio/  BeepPlayer (in-memory sine tones), CoachSpeaker (TextToSpeech + voice ranking)
  ui/     TrainingViewModel (session state machine), MainActivity (views)
app/src/test/  JUnit tests for sequence rules and voice-gender detection
```

## Python → Android mapping
| Python (Kivy) | Android |
|---|---|
| `Clock.schedule_once` + `_events` set | `Handler.postAtTime(..., token, ...)`, cancelled with `removeCallbacksAndMessages(token)` |
| `make_tone` WAV + `SoundLoader` | `BeepPlayer`: same 660 Hz/150 ms and 1000 Hz/400 ms tones, static `AudioTrack` |
| pyjnius `TextToSpeech` + `InitListener` | `CoachSpeaker` (same voice ranking, pitch/rate profiles, audio attributes) |
| `FLAG_KEEP_SCREEN_ON` + partial wake lock (2 h timeout) | Activity window flag + `PowerManager` wake lock in the ViewModel |
| `on_pause` returning `True` | Session lives in a `ViewModel` on the main looper — survives rotation and backgrounding |
| `on_stop` | `TrainingViewModel.onCleared()` (stops TTS, releases lock, cancels events) |
| Desktop TTS branches (PowerShell / `say` / espeak) | Dropped — not applicable on Android |

Behaviour preserved: 5 patterns, 3-second countdown beeps, high beep per set,
no T calls, first call of a set never repeats the previous set's last call,
random mirroring, voice can be changed live (previews when idle), instruction
delay adjustable during training, pattern / sets / set-delay locked while training.

`AndroidManifest.xml` declares `WAKE_LOCK` and a `<queries>` entry for
`TTS_SERVICE` (required on API 30+ to bind to the TTS engine).
