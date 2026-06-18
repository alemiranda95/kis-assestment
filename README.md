# Context-Aware Focus Mode

A minimal Android app that helps you run a focus session while it passively watches for
distractions through the **microphone** (ambient noise) and **accelerometer** (movement). It
logs distraction events, shows live stats, sends a heads-up notification when a threshold is
crossed, persists sessions locally, and syncs them to a REST API.

Built for the **KIS Solutions — Senior Android Developer** technical assessment.

> **Application id:** `com.assestment.kis` · **min SDK 26 / target SDK 36** · Kotlin · Jetpack
> Compose · Material 3.

---

## What it does

- **Focus session** — start/stop; a circular timer shows elapsed time, and two live stat
  cards show the noise and movement distraction counts.
- **Distraction detection** — threshold-based on a microphone RMS amplitude and accelerometer
  magnitude. Edge-triggered with a debounce so a sustained noise counts as one event, not
  hundreds.
- **Ambient feedback** — the background eases toward red on each distraction (and the count
  bumps, so the signal is not color-only).
- **Notifications** — a heads-up, auto-dismissing notification states the reason (noise or
  movement), throttled per type, through a configured channel.
- **History** — a bottom sheet loads past sessions from the API and shows per-session event
  detail.

---

## Quick start

This project uses a **bleeding-edge AGP 9.2.1 / Gradle 9.4.1 / Kotlin 2.2.10** toolchain, which
needs a **JDK 17+**. Android Studio's bundled JBR (21) works out of the box.

- **Android Studio:** set *Settings → Build, Execution, Deployment → Build Tools → Gradle →
  Gradle JDK* to the embedded **JBR**, then Sync.
- **CLI:**
  ```bash
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  ./gradlew :app:assembleDebug      # build
  ./gradlew test                    # JVM unit tests (domain + data + ViewModel)
  ```

On first launch the app requests **microphone** and (Android 13+) **notification** permissions.
Make a sustained sound or move the device to trigger distractions.

> The runtime networking uses an in-process **fake** REST backend (see *Persistence & API*), so
> the app is fully self-contained — no server or internet needed.

---

## Architecture

Clean Architecture across **five flat modules**. The dependency rule points inward: everything
depends on `:domain`, and `:domain` depends on nothing Android.

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

| Module | Type | Responsibility |
|---|---|---|
| `:domain` | Kotlin/JVM library | Models, `Result`/error types, use cases, **detection policy** (evaluator, throttle, monitor), and the interfaces the outer layers implement. Zero Android. |
| `:data` | Android library | Room (source of truth), Retrofit + a fake remote (Option B), DTO/entity mappers, offline-first repository. |
| `:platform` | Android library | Android-framework isolation: `AudioRecord`/`SensorManager` sources, `NotificationManagerCompat` notifier, runtime permission checker. |
| `:presentation` | Android library | MVI `FocusViewModel`, Compose UI + design system (`ds/`), type-safe navigation. |
| `:app` | Android app | Wires the implementations to the domain interfaces via Koin and hosts the single screen. |

**Why 5 flat modules instead of `:core:*`/`:feature:*` + convention plugins?** The layer
separation that's graded is identical; flat modules give real, enforced module boundaries with
far less Gradle setup — the right trade-off for a single-feature app under a timebox. The full
matrix is the documented production scaling path (below).

**Presentation pattern — MVI.** Each screen has a `State` (immutable), `Action` (sealed user
intents), `Event` (one-shot effects), and a `ViewModel`. The composables split into a
`FocusRoot` (collects state, wires Koin + permission launchers) and a pure, previewable
`FocusScreen(state, onAction)`. The ViewModel depends **only on use cases** — never on a
repository, sensor, or notifier directly.

**DI — Koin**, one module per layer, assembled only in `:app`.

---

## Native resource handling

This is the part the app is built around, and the design goal was: **business logic must be
testable without touching Android.**

- **Sensors are behind domain interfaces.** `:domain` declares `NoiseSource` and `MotionSource`
  as `Flow<Float>` of raw magnitude. The Android implementations live in `:platform`
  (`AudioRecordNoiseSource`, `SensorManagerMotionSource`). Nothing in the domain or presentation
  layer imports `android.hardware` or `android.media`.
- **The *decision* is pure.** `DistractionEvaluator` (in `:domain`) takes a magnitude + threshold
  and returns whether it's a distraction — edge-triggered with a debounce re-arm window. It's a
  plain Kotlin class, unit-tested by feeding it numbers.
- **Lifecycle = collection.** Both sensor sources are cold `callbackFlow`s: they register on
  collection and release in `awaitClose`. The ViewModel only collects while a session is active, so
  the mic and accelerometer are acquired on **Start** and released on **Stop** — no manual
  bookkeeping, no leaks.
- **Backgrounding ends the session.** A `ProcessLifecycleOwner` observer in `FocusRoot` stops the
  session when the app goes to background (`ON_STOP`), which releases the sensors, saves the session,
  and best-effort syncs it. Config changes (rotation) are debounced by `ProcessLifecycleOwner`, so
  they don't end a session. This keeps detection strictly foreground and is *why* no foreground
  service is needed.
- **Permissions.** Requested on app entry and at session start via the Activity Result API in
  `FocusRoot`; status is queried through a `PermissionChecker` interface. If the mic is denied the
  session still runs (movement-only) and the UI says so.
- **Battery awareness (implemented):** modest accelerometer rate (`SENSOR_DELAY_NORMAL`); the mic
  is sampled mono at 16 kHz for loudness only (not fidelity), with the first buffers discarded to
  skip the startup transient; sensors run only while a session is foreground; network is batched to
  a single POST at stop.

**Foreground service — intentionally not used.** Detection only runs while the app is foreground
(backgrounding stops the session, above), so a foreground service is unnecessary for the MVP (and
avoids its own battery cost and notification). True always-on/background monitoring *would* require a
foreground service with the `microphone` type — documented as future work, not built.

---

## Notifications

Modern APIs: a configured `NotificationChannel` (`IMPORTANCE_HIGH` for heads-up), built with
`NotificationCompat`, posted via `NotificationManagerCompat`, gated on the `POST_NOTIFICATIONS`
runtime permission (Android 13+). Each notification clearly states the reason, auto-dismisses
(`setTimeoutAfter`), and is throttled to once per type per cooldown so it never spams. The
*throttle decision* lives in `:domain` (`NotificationThrottle`) and is unit-tested; only the act
of showing a notification is in `:platform`.

---

## Persistence & API (offline-first, "Option B")

- **Room is the source of truth.** Sessions and their distraction events persist locally and
  survive process death.
- **Sync is best-effort.** On stop, the session is saved to Room, then `POST /sessions` is
  attempted; failure leaves it stored and marked unsynced with a non-blocking message — the stop
  action never fails.
- **Reads are offline-first:** `GET /sessions` → cache into Room → return from Room, falling back
  to the cache on network error. `GET /session/{id}` for detail.
- **The mock API is in-process.** A `FakeRemoteSessionDataSource` is the runtime default, so the
  app is self-contained. The **real `RetrofitRemoteSessionDataSource`** implements the same
  interface and is verified against **MockWebServer** in tests (paths, JSON, 404/500/timeout
  mapping). Swapping fake → real is a one-line DI change. *Trade-off:* the live demo doesn't make a
  network round-trip; in exchange the build runs anywhere, deterministically.

Networking uses **Retrofit** ("or equivalent" per the brief). Models are separated three ways —
`SessionDto` (network) ↔ `FocusSession` (domain) ↔ `SessionEntity` (Room) — with mappers in `:data`.

---

## Testability

Testability was treated as a first-class concern; the architecture exists largely to enable it.

- **`:domain` is pure JVM**, so its tests run fast with no device:
  - `DistractionEvaluator` — threshold crossing, debounce re-arm, per-type independence.
  - `NotificationThrottle` — cooldown gating.
  - `DistractionMonitor` / `ObserveDistractionsUseCase` — fake sources → expected events + notify policy.
- **ViewModel** (`FocusViewModelTest`) — state transitions, distraction counting + notify, sync-failure
  event, history load success/failure, permission flows — with fakes, Turbine, and a test dispatcher.
- **Data** — `RetrofitRemoteSessionDataSource` against MockWebServer, and the offline-first repository
  with in-memory fakes.
- **UI** — a Robolectric Compose test for the screen (start/stop rendering + click), runnable on the JVM.

Stack: JUnit5 (domain), JUnit4 + Turbine + AssertK + `kotlinx-coroutines-test` (Android modules),
MockWebServer, Robolectric. Fakes are preferred over mocks.

---

## Accessibility

A pragmatic inclusive-design pass (not a full WCAG audit):

- **Content descriptions** on the history FAB and the sync badge; stat cards expose a single merged
  description (`"Noise: 3"`) for screen readers; decorative icons are `null`.
- **Not color-only** — a distraction shows as a rising count and an icon/label, not just the red flash.
- **Touch targets** ≥ 48dp (the primary button is 60dp; the small history FAB uses
  `minimumInteractiveComponentSize()`).
- **Dynamic type** — Material 3 typography scales with the user's font size.
- **Contrast** — light text on the deep-indigo gradient clears 7:1.

---

## Design system

UI values live in `:presentation/ds/` so there are **no magic numbers in composables**:
`Color.kt` (gradient, accent, translucent overlays), `Values.kt` (`Dimens` sizes/spacing, `Motion`
durations, `FocusLayout` weights/angles), `Theme.kt`, `Type.kt`. The main screen is an intentional
always-dark focus ambiance; the Material 3 light/dark scheme drives the history sheet and dialogs.

---

## Trade-offs & what was intentionally deprioritized

- **Multi-module convention plugins / `build-logic`** — flat modules instead (see Architecture).
- **Foreground service** — not needed for foreground-only detection (documented decision).
- **Live HTTP at runtime** — Option B fake; real client is test-covered instead.
- **Adaptive battery** (`PowerManager.isPowerSaveMode`, battery-level sampling) and **WorkManager**
  sync retry/backoff — designed-for but not built.
- **Process-death restoration** — the active session restores elapsed-time continuity via
  `SavedStateHandle`, but in-flight events are not restored (a known MVP limitation).
- **Full WCAG audit** — basics only.

## What I'd improve with more time / how this scales

- **Modularize per feature** (`:core:*` + `:feature:*` with convention plugins) as more features
  land, to keep build times and boundaries healthy.
- **Adaptive detection & battery** — calibrate thresholds, duty-cycle the mic under power-save,
  optionally a foreground service for background sessions.
- **Real sync** — swap the fake remote for the Retrofit client + a backend, add WorkManager retry,
  conflict resolution, and auth/token handling (the `DataError` types already cover it).
- **Observability** — structured logging/metrics on detection accuracy to tune thresholds in the field.
- **Deeper tests** — instrumented Room/DAO tests, more Compose UI coverage, screenshot tests.

---

## Tooling & methodology

**AI usage (approved by the recruiter).** Built with **Claude Code** following a **Spec-Driven
Development** workflow: `brainstorm → spec → plan → implement → review`, with a human checkpoint
between every stage. Each stage produced a document (kept in a local `_docs/`, not part of the
deliverable) that the next stage built on. The AI generated the Kotlin/Gradle code; every
architectural decision was made and reviewed deliberately — including pushing back on and
restructuring its output (e.g. introducing use cases, reorganizing packages, redesigning the UI).

**Time spent** — derived from git commit timestamps (the source of truth), not estimated:

| | |
|---|---|
| **Core timebox** | **~3h** — planning + Phases 1–4: full 5-module architecture, pure tested domain, MVI UI, Room + offline-first API. App running end-to-end on simulated sensors. |
| **Extra time** | **~2h** — real `AudioRecord`/`SensorManager` sensors + notifications + permissions, on-device bug fixes, the UI redesign, and this README. |
| **Total** | **≈ 5h** |

The extra time (explicitly permitted if documented) turned the architecturally-complete app into
a polished, on-device-verified one.

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

Build commands and module conventions are in the project's build files; all modules build with
`./gradlew build`.
