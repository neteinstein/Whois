# AGENTS.md

Guidance for any agent (human or AI) working in this repo. This file documents the stack,
why the modules are split the way they are, conventions to follow, and — most importantly —
the gotchas a fresh agent would otherwise trip over.

## Origin of the app content

This app (Whois) was built directly from a plain-text app-creation prompt given in chat, not
derived from a spreadsheet/PDF/design doc. There is no `docs/` folder of canonical source
material to defer to — the screens, copy, and behavior described below **are** the spec.
Do not invent new screens or fields beyond what's described here without checking with whoever
is driving the change.

## Stack

- Kotlin Multiplatform (Android + iOS: `iosArm64` + `iosSimulatorArm64` only, no `iosX64`)
- Compose Multiplatform for UI, MVVM, `kotlinx.coroutines` + `StateFlow`
- Koin for DI
- Gradle Kotlin DSL, every dependency version centralized in `gradle/libs.versions.toml`
- `com.russhwolf:multiplatform-settings` for persisted key/value settings (language choice)
- Kover for coverage, ktlint + Android Lint for lint, GitHub Actions for CI (see `CI_CD.md`)

## Module layout

```
core/common   - DispatcherProvider, UseCase/NoParamUseCase base types, AppLanguage, UrlOpener
core/ui       - theme (light/dark Material3), typography, shared components, in-memory
                localized Strings (EN/PT), Destination + Navigator
feature/splash    - animated splash screen (magnifying glass over a person silhouette)
feature/search    - the main screen: name/phone/company/address fields, Brave search
feature/settings  - language switch + About section
composeApp    - aggregator KMP library: App(), Koin module wiring, platform UrlOpener impls,
                the iOS `MainViewController()` entry point
androidApp    - the actual Android application (manifest, Application, MainActivity)
iosApp        - hand-authored Xcode project (see "Hand-authored Xcode project" below)
```

**Why `composeApp` and `androidApp` are separate modules:** this project targets AGP 9+, and
AGP 9 does not allow a module to apply both `org.jetbrains.kotlin.multiplatform` and
`com.android.application` at once. So the shared UI/logic lives in `:composeApp` (a KMP library,
via `com.android.kotlin.multiplatform.library`), and the real Android app entry point
(manifest, `MainActivity`, `Application`) lives in the separate, Android-only `:androidApp`
module, which depends on `:composeApp` as a regular Gradle project dependency.

**Why feature modules aren't split further into data/domain/ui submodules:** each feature module
*internally* uses `domain/`, `data/`, and top-level `ui` (screen + ViewModel) packages — that's
where the clean-architecture layering lives. The Gradle module boundary is per-feature, not
per-layer, to avoid a combinatorial explosion of tiny KMP modules (each of which pays a real
Gradle configuration-time and AGP-target cost).

**Why there's no `org.jetbrains.androidx.navigation` dependency:** with exactly three screens and
no deep-linking needs, `core/ui/navigation/Navigator.kt` is a ~30-line hand-rolled backstack
(`StateFlow<List<Destination>>`) driven by `AnimatedContent` in `composeApp/App.kt`. This avoids
pulling in a comparatively young multiplatform-navigation artifact whose API was still moving
between releases, for no real benefit at this screen count. If the app grows past a handful of
screens or needs deep links, revisit this.

## MVVM / Koin / testing conventions

- Composables are stateless: a `XyzScreen()` pulls a ViewModel via `koinViewModel()`; anything
  reusable takes state + callbacks as parameters (see `core/ui/components/WhoisComponents.kt`).
- ViewModels expose one immutable `StateFlow<XyzUiState>` (or several focused `StateFlow`s, as in
  `SettingsViewModel`); UI events are plain method calls on the ViewModel, never a callback into
  a "controller" or shared mutable state.
- Every feature module has its own `FeatureModule.kt` (`val searchModule = module { ... }`);
  `composeApp/di/AppModule.kt#sharedModules()` is the single place that lists all of them. Add a
  new feature's module to that list or it will silently not be wired up.
- `DispatcherProvider` (in `core:common`) is injected everywhere instead of using
  `Dispatchers.IO`/`Default` directly, so tests can substitute a `TestDispatcher`.
- Tests live in `commonTest` using `kotlin.test`, and must pass with `./gradlew testDebugUnitTest`
  (that task runs `commonTest` + `androidUnitTest` on the JVM — no emulator, no iOS simulator
  needed). New domain/data code should arrive with a test in the same PR.
- A ViewModel that touches `viewModelScope` in a test needs `Dispatchers.setMain(...)` /
  `resetMain()` around it (see `SplashViewModelTest`) — otherwise `viewModelScope` throws because
  no `Main` dispatcher is registered on a plain JVM unit test.

## Build / verify commands

```
./gradlew testDebugUnitTest        # all commonTest + androidUnitTest, no SDK emulator needed
./gradlew ktlintCheck              # style, all modules
./gradlew :androidApp:lintDebug    # Android Lint
./gradlew koverXmlReport koverVerify
./gradlew :androidApp:assembleDebug
# iOS (macOS + Xcode only):
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build
```

### Sandboxed/cloud agent containers likely cannot build this project

This repo was authored in a sandboxed Linux container with **no Android SDK installed and no
network access to `dl.google.com`** (the proxy in that environment explicitly blocks it, and
`google()` is the only source for the Android Gradle Plugin, `androidx.*` and `com.android.*`
artifacts). That means:

- `./gradlew` could not be run against the real project in that environment at all — not even
  `./gradlew tasks` — because resolving the AGP plugin itself requires `google()`.
- Nothing in this repo (module wiring, the AGP 9 `androidLibrary { }` DSL, dependency versions)
  was verified by an actual build in that environment.
- **CI on the PR is the real build signal.** If you're an agent in a similarly locked-down
  sandbox, say so explicitly rather than claiming a build/test "passed" — check what you can
  offline (reading the Kotlin source for obvious syntax/type errors, `ktlint` if it's reachable
  without `google()`, since ktlint itself doesn't need the Android SDK) and defer everything
  else to the `pr.yml` workflow.
- If you *do* have a working Android SDK + network access, the very first thing worth doing is
  `./gradlew :androidApp:assembleDebug --stacktrace` to shake out any AGP 9 DSL drift (see next
  section) before touching feature code.

## Gotchas

- **`com.android.kotlin.multiplatform.library` DSL is new and still settling.** Every KMP
  module's `build.gradle.kts` uses:
  ```kotlin
  kotlin {
      androidLibrary {
          namespace = "..."
          compileSdk = libs.versions.androidCompileSdk.get().toInt()
          minSdk = libs.versions.androidMinSdk.get().toInt()
      }
      iosArm64()
      iosSimulatorArm64()
  }
  ```
  instead of a separate top-level `android { }` block. This is AGP 9's replacement for applying
  `com.android.library` inside a KMP module. If AGP renames/reshapes this extension by the time
  you're reading this, every `core/*`, `feature/*`, and `composeApp` build file needs the same
  mechanical update — it's not a per-module design choice, just repeated boilerplate.
- **`doInitKoin()`, not `initKoin()`.** Kotlin/Native's Objective-C exporter treats a top-level
  function named like an initializer (`initXyz`) as an init-style selector and mangles it, so
  Swift can't call `InitKoinKt.initKoin()` directly. The iOS entry point in
  `composeApp/src/iosMain/.../InitKoinIos.kt` is named `doInitKoin()` specifically to avoid this;
  don't rename it back to `initKoin` without testing the actual Swift-side symbol.
- **The iOS project is hand-authored, not exported from Xcode.** `iosApp/iosApp.xcodeproj/project.pbxproj`
  was written by hand (no CocoaPods, no `xcodegen`). If you add a new Swift file, you must add
  matching `PBXFileReference`/`PBXBuildFile` entries yourself — Xcode will do this for you once
  you open the project in the GUI and add files through it, which is the easiest path for a
  human; an agent without Xcode should edit the `.pbxproj` very carefully and get a human to open
  it in Xcode once to confirm it's still valid.
- **The `Assets.xcassets/AppIcon.appiconset` has no actual image**, only a `Contents.json`
  declaring a universal 1024×1024 slot. This is a code-only repo; real app icon artwork (both
  Android's adaptive-icon PNGs/vectors — the current launcher icon is a hand-drawn vector, fine
  for now — and a real iOS `.png`) should come from whoever owns visual design before shipping.
- **No iOS Share Extension.** The "accept a shared contact" requirement is implemented as an
  Android `ACTION_SEND` intent filter only (`androidApp/.../MainActivity.kt`). Wiring the
  equivalent on iOS needs a genuinely separate Xcode target (a Share Extension) with its own
  entitlements/App Group to hand data back to the host app — that's materially more iOS-project
  surface than this pass covers, and is called out here rather than silently skipped.
- **"Brave webview" was implemented as "launch a URL, preferring the Brave app."** `UrlOpener`
  (Android: explicit `Intent` at `com.brave.browser`, falling back to the default browser; iOS:
  the `brave://open-url?url=` scheme, falling back to `https://`) opens Brave Search
  (`search.brave.com`) with a natural-language whois-style prompt in the query string — it does
  **not** embed an in-app `WebView`/`WKWebView`. If a literal embedded browser view was intended,
  that's a different (bigger) piece of platform-specific UI to build.
- **The vCard parser in `feature/search` never reads the `NOTE` property, on purpose** (privacy:
  contact notes routinely hold sensitive info the user wrote for themselves). Don't "helpfully"
  add it back when extending the parser.
- **Every dependency version lives in `gradle/libs.versions.toml`.** Never hardcode a version
  string in a module's `build.gradle.kts`.
- **Kover coverage on top of the new `com.android.kotlin.multiplatform.library` target is an
  untested combination.** Kover's Android-target support was built against the older
  `com.android.library` plugin; if `koverXmlReport`/`koverVerify` fail specifically (as opposed
  to `testDebugUnitTest` passing on its own), that's the most likely reason, not a real coverage
  regression.
- **In-app language switching doesn't use Android resource files or `Locale`/NSLocale
  machinery.** `core/ui/strings/Strings.kt` is a plain Kotlin data class with two hand-written
  translations (EN/PT), swapped instantly via a `CompositionLocal` driven by
  `SettingsRepository.language`. This was a deliberate simplification to get instant, symmetric
  Android+iOS language switching without per-platform resource-locale plumbing — not an
  oversight. If the app needs many more languages, moving to Compose Multiplatform resources
  (`compose.components.resources`) would probably be worth revisiting this.
