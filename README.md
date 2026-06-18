# Context-Aware Focus Mode

A small Android app that helps you run a focus session. While a session runs, the app checks for
distractions with the **microphone** (noise) and the **accelerometer** (movement). It saves each
distraction, shows live stats, sends a notification when noise or movement is too high, stores the
sessions on the device, and syncs them to a REST API.

Made for the **KIS Solutions — Senior Android Developer** technical assessment.

> **Application id:** `com.assestment.kis` · **min SDK 26 / target SDK 36** · Kotlin · Jetpack
> Compose · Material 3.

---

## What it does

- **Focus session** — start and stop a session. A round timer shows the time, and two cards show
  how many noise and movement distractions happened.
- **Distraction detection** — it uses simple thresholds on the microphone level and the
  accelerometer value. One distraction is counted once, even if the noise lasts a while (not
  hundreds of times).
- **Visual feedback** — the background turns red for a moment on each distraction. The counter also
  goes up, so the meaning does not depend on color alone.
- **Notifications** — a pop-up notification says the reason (noise or movement). It closes by
  itself, and it is limited so it does not repeat too often. It uses a proper notification channel.
- **History** — a bottom sheet loads past sessions from the API and shows the events of each session.

---

## Quick start

**Just want to try it?** A ready-to-install debug APK is in `apk/focus-mode.apk`. Install it
with `adb install apk/focus-mode.apk`, or copy it to a device and open it. To build from
source instead, read on.

To build the app you need a recent **JDK (17 or newer)**. The easiest way is to open the project in
**Android Studio**, which already includes a compatible JDK.

- **Android Studio:** set *Settings → Build, Execution, Deployment → Build Tools → Gradle →
  Gradle JDK* to the included JDK, then Sync and Run.
- **CLI** (set `JAVA_HOME` to a JDK 17+, for example the one inside Android Studio):
  ```bash
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  ./gradlew :app:assembleDebug      # build
  ./gradlew test                    # JVM unit tests (domain + data + ViewModel)
  ```

On the first launch, the app asks for the **microphone** and (on Android 13+) the **notification**
permission. Make a loud sound or move the phone to create distractions.

> The app uses a **fake** REST backend that runs inside the app (see *Persistence & API*). So it
> works on its own, with no server and no internet.

---

## Architecture

The app uses Clean Architecture with **five modules**. Every module depends on `:domain`, and
`:domain` does not depend on Android.

```
            ┌─────────────────────────── :app ───────────────────────────┐
            │  Application, MainActivity, DI assembly (Koin startKoin)     │
            └───────┬───────────────┬───────────────┬───────────────┬─────┘
                    │               │               │               │
            ┌───────▼──────┐ ┌──────▼──────┐ ┌──────▼───────┐ ┌─────▼──────┐
            │ :presentation│ │   :data     │ │  :platform   │ │  (wires    │
            │  MVI + Compose│ │ Room + API  │ │ sensors,     │ │   them)    │
            │              │ │ offline-first│ │ notif, perms │ │            │
            └───────┬──────┘ └──────┬──────┘ └──────┬───────┘ └────────────┘
                    └───────────────┼───────────────┘
                            ┌───────▼────────┐
                            │    :domain     │   pure Kotlin/JVM — no Android
                            │ models, use    │
                            │ cases, policy  │
                            └────────────────┘
```

| Module | Type | What it holds |
|---|---|---|
| `:domain` | Kotlin/JVM library | Models, `Result`/error types, use cases, and the detection logic (evaluator, throttle, monitor). It also defines the interfaces that the other layers implement. No Android code. |
| `:data` | Android library | Room (the main data store), Retrofit and a fake remote that runs in the app, DTO/entity mappers, and an offline-first repository. |
| `:platform` | Android library | The Android-only code: `AudioRecord`/`SensorManager` sources, the `NotificationManagerCompat` notifier, and the permission checker. |
| `:presentation` | Android library | The MVI `FocusViewModel`, the Compose UI, the design system (`ds/`), and type-safe navigation. |
| `:app` | Android app | Connects the implementations to the domain interfaces with Koin, and shows the single screen. |

**Why five simple modules and not a bigger `:core:*`/`:feature:*` setup?** The layer separation is
the same, but simple modules need much less build setup. This is a good choice for a one-feature app
with limited time. The bigger setup is described below as the way to grow the project.

**Presentation pattern — MVI.** Each screen has a `State` (immutable), an `Action` (user actions),
an `Event` (one-time effects), and a `ViewModel`. The UI is split into `FocusRoot` (reads the state
and connects Koin and the permission launchers) and `FocusScreen(state, onAction)` (a simple
function that is easy to preview). The ViewModel uses **only use cases**. It never calls a
repository, a sensor, or the notifier directly.

**DI — Koin**, one module per layer, all connected in `:app`.

---

## Native resource handling

This is the core part of the app. The main goal was simple: **the business logic must be testable
without Android.**

- **Sensors are behind interfaces.** `:domain` defines `NoiseSource` and `MotionSource` as a
  `Flow<Float>` of raw values. The Android versions are in `:platform` (`AudioRecordNoiseSource`,
  `SensorManagerMotionSource`). The domain and presentation layers never import `android.hardware`
  or `android.media`.
- **The decision is pure.** `DistractionEvaluator` (in `:domain`) takes a value and a threshold and
  decides if it is a distraction. It fires only when the value crosses the threshold, and it waits
  before it can fire again. It is a plain Kotlin class, tested by giving it numbers.
- **Lifecycle = collection.** Both sensor sources are cold `callbackFlow`s. They start when
  collection starts and stop in `awaitClose`. The ViewModel collects only while a session runs, so
  the mic and accelerometer turn on at **Start** and turn off at **Stop**. There is no extra cleanup
  code and no leaks.
- **Going to the background ends the session.** A `ProcessLifecycleOwner` observer in `FocusRoot`
  stops the session when the app goes to the background (`ON_STOP`). This releases the sensors, saves
  the session, and syncs it if possible. Rotation does not stop the session, because
  `ProcessLifecycleOwner` ignores short config changes. This keeps detection in the foreground only,
  which is why no foreground service is needed.
- **Permissions.** They are asked on app start and on session start with the Activity Result API in
  `FocusRoot`. The current status is read through a `PermissionChecker` interface. If the microphone
  is denied, the session still runs with movement only, and the UI shows this.
- **Battery.** The accelerometer uses a normal rate (`SENSOR_DELAY_NORMAL`). The mic records in mono
  at 16 kHz, only to measure loudness (not quality), and the first buffers are skipped to avoid a
  start noise. Sensors run only in the foreground. The app sends one POST at stop instead of many
  calls.

**Foreground service — not used on purpose.** Detection runs only in the foreground (the background
stops the session, see above). So the MVP does not need a foreground service, which would add its
own battery cost and notification. Real background monitoring would need a foreground service with
the `microphone` type. This is listed as future work and is not built.

---

## Notifications

The app uses modern APIs: a `NotificationChannel` with `IMPORTANCE_HIGH` (pop-up), built with
`NotificationCompat`, shown with `NotificationManagerCompat`, and protected by the
`POST_NOTIFICATIONS` permission (Android 13+). Each notification says the reason, closes by itself
(`setTimeoutAfter`), and is limited to one per type per cooldown, so it does not repeat too often.
The rule that limits notifications is in `:domain` (`NotificationThrottle`) and is unit-tested. Only
the act of showing the notification is in `:platform`.

---

## Persistence & API (offline-first)

- **Room is the main data store.** Sessions and their events are saved on the device and survive
  process death.
- **Sync is best-effort.** At stop, the session is saved to Room first, then the app tries
  `POST /sessions`. If it fails, the session stays saved and is marked as not synced, and the user
  sees a short message. The stop action never fails.
- **Reads are offline-first.** `GET /sessions` loads from the API, saves to Room, and returns the
  Room data. If the network fails, it returns the saved data. `GET /session/{id}` loads the details.
- **The mock API runs inside the app.** `FakeRemoteSessionDataSource` is used at runtime, so the app
  works on its own. The real `RetrofitRemoteSessionDataSource` uses the same interface and is tested
  with **MockWebServer** (paths, JSON, and 404/500/timeout handling). Changing from the fake to the
  real one is a one-line DI change. *Trade-off:* the demo does not make a real network call, but in
  return the build works anywhere and gives the same result every time.

Networking uses **Retrofit** ("or equivalent" in the brief). The models are kept separate in three
forms: `SessionDto` (network), `FocusSession` (domain), and `SessionEntity` (Room), with mappers in
`:data`.

---

## Testability

Testing was a main goal, and the architecture is built to make it easy.

- **`:domain` is pure JVM**, so its tests are fast and need no device:
  - `DistractionEvaluator` — crossing the threshold, the wait before firing again, and each type
    counted on its own.
  - `NotificationThrottle` — the cooldown.
  - `DistractionMonitor` / `ObserveDistractionsUseCase` — fake sources produce the expected events
    and notifications.
- **ViewModel** (`FocusViewModelTest`) — state changes, counting and notifying, the sync-failure
  message, history load (success and failure), and the permission flows. It uses fakes, Turbine, and
  a test dispatcher.
- **Data** — `RetrofitRemoteSessionDataSource` with MockWebServer, and the offline-first repository
  with in-memory fakes.
- **UI** — a Robolectric Compose test for the screen (it checks start/stop and a click) that runs on
  the JVM.

Tools: JUnit5 (domain), JUnit4 + Turbine + AssertK + `kotlinx-coroutines-test` (Android modules),
MockWebServer, and Robolectric. The tests use fakes instead of mocks.

---

## Accessibility

A basic inclusive-design pass (not a full WCAG audit):

- **Content descriptions** on the history button and the sync badge. The stat cards give one
  combined description (`"Noise: 3"`) for screen readers, and decorative icons are `null`.
- **Not color only** — a distraction shows as a higher number and an icon/label, not only the red
  flash.
- **Touch targets** are at least 48dp (the main button is 60dp; the small history button uses
  `minimumInteractiveComponentSize()`).
- **Font scaling** — Material 3 text scales with the user's font size.
- **Contrast** — light text on the dark-blue background is easy to read.

---

## Design system

The UI values are in `:presentation/ds/`, so there are **no magic numbers in the composables**:
`Color.kt` (gradient, accent, see-through overlays), `Values.kt` (`Dimens` for sizes and spacing,
`Motion` for durations, `FocusLayout` for weights and angles), `Theme.kt`, and `Type.kt`. The main
screen is always dark on purpose, to feel calm. The Material 3 light/dark theme is used for the
history sheet and dialogs.

---

## What I prioritized (and why)

The time was limited, so the order of work was a deliberate choice:

- **Architecture, the domain, and testability first.** These are the parts the assessment values
  most, and getting them right early makes every later step safer and faster. After this step, the
  project is already in a good state, even if something later has to be cut.
- **A working app early, on fake sensors.** This gave a full start → detect → stop → history flow to
  show, without depending on device-only code.
- **Persistence and the API before the real sensors.** These are standard and lower risk, so they
  were done before the harder, device-specific work.
- **Real sensors last.** They depend on the device and need the most tuning (mic levels,
  permissions). Because they sit behind interfaces, the rest of the app did not have to wait for them.
- **The visual design and on-device testing at the very end.** During the main build the UI was kept
  simple and functional. The full design (the gradient screen, the round timer, the animations, and
  the theming) and the testing and fixing on a real device came last, in the extra time, once the app
  worked and the structure was solid.

## Trade-offs & what was left out on purpose

- **A bigger module setup** (`:core:*` + `:feature:*` with convention plugins) — simple modules were
  used instead (see Architecture).
- **Foreground service** — not needed for foreground-only detection (a documented decision).
- **Real network calls at runtime** — the app uses a fake backend inside the app; the real Retrofit
  client is covered by tests (MockWebServer) instead of the demo.
- **Adaptive battery** (`PowerManager.isPowerSaveMode`, battery level) and **WorkManager** retry —
  planned but not built.
- **Process-death restoration** — the session keeps its time after process death with
  `SavedStateHandle`, but the events during the session are not restored (a known MVP limit).
- **Full WCAG audit** — only the basics.

## What I would improve with more time / how it scales

- **Split by feature** (`:core:*` + `:feature:*` with convention plugins) when more features are
  added, to keep build times and boundaries clean.
- **Adaptive detection and battery** — tune the thresholds, slow the mic in power-save mode, and add
  a foreground service for background sessions if needed.
- **Real sync** — replace the fake remote with the Retrofit client and a backend, add WorkManager
  retry, handle conflicts, and add auth/token support (the `DataError` types already support this).
- **Monitoring** — add logs and metrics about detection accuracy to tune the thresholds in real use.
- **More tests** — instrumented Room/DAO tests, more Compose UI tests, and screenshot tests.

---

## Tooling & methodology

**AI usage (approved by the recruiter).** The app was built with **Claude Code** using a
**Spec-Driven Development** process: `brainstorm → spec → plan → implement → review`, with a human
check between each step. Each step created a document (kept in a local `_docs/` folder, not part of
the deliverable) that the next step used. The AI wrote the Kotlin/Gradle code, but every design
choice was made and reviewed on purpose. This included changing the AI's output when needed (for
example, adding use cases, reorganizing packages, and redesigning the UI).

**Time spent** — from git commit times (the real source), not guessed. Rough blocks:

| Block | ~Time | What was done |
|---|---|---|
| Planning | ~1h | Brainstorm, spec, and plan: architecture, scope, and the main decisions |
| Foundation + domain | ~40m | The five-module project and build setup; the pure-Kotlin domain and detection logic, with unit tests |
| Presentation | ~55m | The MVI ViewModel, the Compose screen, and the history sheet (on fakes); then the change to use cases |
| Data & API | ~10m | Room as the main store, the offline-first repository, Retrofit, and MockWebServer |
| *— core subtotal —* | *~3h* | *the app is complete in structure and runs end-to-end with simulated sensors* |
| Platform | ~1h | The real `AudioRecord`/`SensorManager`, notifications, and permissions; plus on-device fixes |
| UI redesign | ~35m | The gradient screen, round timer, animated stats, themed history sheet, and design values |
| Polish + review | ~45m | Bug fixes, test fixes, this README, and the review with the background-stop fix |
| **Total** | **≈ 5h** | **~3h core + ~2h extra** (extra time is allowed if it is documented) |

The extra time made the complete app more polished and tested it on a real device.

---

## Project structure

```
domain/       pure Kotlin — util (Result/errors), session (models, repo iface, use cases),
              detection (evaluator, monitor, config, sources), notification, permission
data/         session/{local (Room), remote (Retrofit + fake), mapper, repository}, network
platform/     sensor (AudioRecord, SensorManager), notification, permission
presentation/ focus (MVI + screen + history), ds (design system), ui (UiText, helpers)
app/          FocusApp (Koin), MainActivity, NavHost
```

To build, see the build files in each module. All modules build with `./gradlew build`.
