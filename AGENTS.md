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
- **`androidApp` does NOT apply `org.jetbrains.kotlin.android`.** CI's second real failure was
  `Cannot add extension with name 'kotlin', as there is an extension already registered with that
  name` while applying the classic Kotlin Android plugin. AGP 9's `com.android.application` now
  registers its own `kotlin` extension (built-in Kotlin support), so applying the separate KGP
  Android plugin on top double-registers it. `androidApp/build.gradle.kts` applies only
  `com.android.application` + `org.jetbrains.kotlin.plugin.compose` (the Compose compiler plugin,
  which still hooks into whatever Kotlin compilation AGP drives). Don't add `kotlinAndroid` back
  without checking whether AGP has changed this again.
- **Dependency versions in this repo were pinned by guesswork at authoring time, then corrected
  against live Maven Central metadata once CI exposed the gap.** The third real CI failure was
  `NoSuchMethodError: KotlinMultiplatformAndroidComponentsExtension.onVariant(...)` while
  configuring `:androidApp` — a binary-incompatible pairing of Compose Multiplatform 1.8.2 with
  AGP 9.0.0's newer `com.android.kotlin.multiplatform.library` API shape. Checking
  `https://repo1.maven.org/maven2/.../maven-metadata.xml` for Kotlin, Compose Multiplatform,
  Koin, coroutines, and Lifecycle showed each was several minor/major versions behind what's
  actually published; `gradle/libs.versions.toml` was updated to the current stable release of
  each at that time. **Maven Central's metadata is reachable even from a sandbox that blocks
  `dl.google.com`** (it blocks Google's Maven specifically, not Maven Central), so checking a
  library's actual latest version there is possible even when a full build isn't — do that before
  guessing a version number, and re-check it if a future CI failure looks like a cross-library
  binary-compatibility mismatch rather than a straightforward code bug. `androidx.*` artifacts
  (core-ktx, activity-compose, core-splashscreen, androidx.test.*) are Google-Maven-only, so they
  can't be checked this way from such a sandbox — CI is the only signal for those.
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
- **AGP 9.0.0 requires Gradle 9.1.0+.** The first real CI run failed all four `ubuntu-latest`
  jobs with `Minimum supported Gradle version is 9.1.0. Current version is 8.14.3` — confirmed
  by an actual build, not a guess. The wrapper (`gradle/wrapper/gradle-wrapper.properties`) is
  pinned to Gradle 9.1.0 for this reason; if you ever bump the AGP version, check its minimum
  Gradle requirement and bump the wrapper alongside it in the same change.
- **Kover must be applied in every module it collects coverage from, not just the root.** CI
  failed `koverXmlReport` with `No matching variant of project :core:ui was found ... attribute
  'org.gradle.usage' with value 'kover'` for every module the root's `dependencies { kover(project(...)) }`
  block referenced. Kover's multi-module aggregation pattern needs the plugin applied in each
  covered module (so it exposes a `kover`-usage variant) *and* in the root (which then merges
  them); only applying it at the root, as this repo initially did, leaves the covered modules
  with no such variant to select. Every module in the root's `kover(project(...))` list now also
  applies `alias(libs.plugins.kover)` in its own `plugins { }` block — keep the two lists in sync
  if you add or remove a module. Kover's compatibility with the new
  `com.android.kotlin.multiplatform.library` Android target is still a comparatively fresh
  combination even with the plugin applied correctly, so a further Kover-specific failure after
  this fix wouldn't be surprising.
- **ktlint style rules to keep in mind** (all found by CI, not obvious from reading typical
  Kotlin style guides): a class whose body opens with a blank line before the first member is
  flagged ("Class body should not start with blank line") — no blank line right after the opening
  `{`. A `val x = someCall { ... }` where the RHS spans multiple lines wants the RHS moved to its
  own line (`val x =` then `someCall { ... }` indented below) rather than `{` trailing on the
  declaration line. A chained call like `libs.versions.foo.get().toInt()` wants the trailing
  `.get()`/`.toInt()` calls each on their own indented line once ktlint's Android profile
  (`android.set(true)` in the root `build.gradle.kts`) is in effect. `MainViewController()` in
  `composeApp`'s iosMain is a deliberate PascalCase top-level function (Swift-interop
  convention); it carries `@Suppress("ktlint:standard:function-naming")` rather than being
  renamed, since ktlint's factory-method exception doesn't recognize it (the function name
  doesn't match its return type's name, `UIViewController`).
- **In-app language switching doesn't use Android resource files or `Locale`/NSLocale
  machinery.** `core/ui/strings/Strings.kt` is a plain Kotlin data class with two hand-written
  translations (EN/PT), swapped instantly via a `CompositionLocal` driven by
  `SettingsRepository.language`. This was a deliberate simplification to get instant, symmetric
  Android+iOS language switching without per-platform resource-locale plumbing — not an
  oversight. If the app needs many more languages, moving to Compose Multiplatform resources
  (`compose.components.resources`) would probably be worth revisiting this.
