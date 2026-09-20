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
- Tests live in `commonTest` using `kotlin.test`, and must pass with `./gradlew allTests`
  (Kotlin Multiplatform's own aggregate task; runs `commonTest` + the android target's
  JVM-executed tests — no emulator, no iOS simulator needed — see the `allTests` vs.
  `testDebugUnitTest` gotcha below). New domain/data code should arrive with a test in the same
  PR.
- A ViewModel that touches `viewModelScope` in a test needs `Dispatchers.setMain(...)` /
  `resetMain()` around it (see `SplashViewModelTest`) — otherwise `viewModelScope` throws because
  no `Main` dispatcher is registered on a plain JVM unit test.

## Build / verify commands

```
./gradlew allTests                 # all commonTest + androidUnitTest, no SDK emulator needed
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
- **`doInitKoin()`, not `initKoin()` - and Swift must call it as `InitKoinIosKt.doInitKoin()`,
  never bare.** Kotlin/Native's Objective-C exporter treats a top-level function named like an
  initializer (`initXyz`) as an init-style selector and mangles it, so Swift can't call
  `InitKoinIosKt.initKoin()` directly. The iOS entry point in
  `composeApp/src/iosMain/.../InitKoinIos.kt` is named `doInitKoin()` specifically to avoid this.
  Separately - and this is the bug that actually shipped and broke the `ios-build` CI job -
  Kotlin/Native's ObjC/Swift export puts every *file's* top-level declarations on a per-file
  facade class named `<FileName>Kt` (the same JVM-style convention `ContentView.swift` already
  relies on for `MainViewControllerKt.MainViewController()`), not a single framework-wide facade
  and not a bare global function. `iOSApp.swift` originally called `doInitKoin()` unqualified,
  which failed to compile with `error: cannot find 'doInitKoin' in scope`; the fix is
  `InitKoinIosKt.doInitKoin()` (the file is `InitKoinIos.kt`, so the facade is `InitKoinIosKt`,
  *not* `InitKoinKt`). This was only caught in an actual `xcodebuild` run - a Linux sandbox can't
  compile Swift at all, so this class of Swift/Kotlin symbol-naming mismatch is invisible until
  CI's `ios-build` job runs. Don't rename it back to `initKoin` or drop the `InitKoinIosKt.`
  qualifier without testing the actual Swift-side symbol against a real build.
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
- **AGP has a minimum Gradle version, and it moves with the AGP version.** The first real CI run
  failed all four `ubuntu-latest` jobs with `Minimum supported Gradle version is 9.1.0. Current
  version is 8.14.3` against AGP 9.0.0 — confirmed by an actual build, not a guess. See the next
  two bullets for how this cascaded further when AGP itself had to move to 9.1.0. If you ever
  bump the AGP version again, check its minimum Gradle requirement and bump the wrapper
  (`gradle/wrapper/gradle-wrapper.properties`) alongside it in the same change.
- **`compileSdk`/AGP/Compose Multiplatform versions are a linked triple - bumping one can break
  the others.** After the Gradle-version fix, `:androidApp:checkDebugAarMetadata` failed with 20
  AAR metadata errors: Compose Multiplatform 1.12.0's Android artifacts (`androidx.compose.ui:ui-android`,
  `foundation-android`, etc.) require **AGP 9.1.0+** and **`compileSdk` 37+** — stricter than the
  Gradle-version bump alone accounted for (AGP was still at 9.0.0, `compileSdk`/`targetSdk` at
  36). `agp`, `androidCompileSdk`, and `androidTargetSdk` in `libs.versions.toml` are now 9.1.0
  and 37/37. If you bump `composeMultiplatform` again, expect its Android artifacts' own AGP/SDK
  floor to have moved too - `checkDebugAarMetadata`'s error message states the exact floor when
  it hasn't.
- **AGP 9.1.0 in turn requires Gradle 9.3.1+** (`Minimum supported Gradle version is 9.3.1.
  Current version is 9.1.0` — a *second* Gradle-version bump in the same cascade as the AGP/
  compileSdk fix above). Rather than bump the wrapper to exactly 9.3.1 and risk a third
  round-trip, it's pinned to whatever `https://services.gradle.org/versions/current` reported as
  the latest stable release at the time (9.7.1) for headroom. AGP, Gradle, and Compose
  Multiplatform version floors keep escalating together; when one of the three needs a bump,
  check whether Gradle already has a much newer stable release than the bare minimum before
  picking a value, so this doesn't become a fourth commit.
- **Kover must be applied in every module it collects coverage from, not just the root.** CI
  failed `koverXmlReport` with `No matching variant of project :core:ui was found ... attribute
  'org.gradle.usage' with value 'kover'` for every module the root's `dependencies { kover(project(...)) }`
  block referenced. Kover's multi-module aggregation pattern needs the plugin applied in each
  covered module (so it exposes a `kover`-usage variant) *and* in the root (which then merges
  them); only applying it at the root, as this repo initially did, leaves the covered modules
  with no such variant to select. Every module in the root's `kover(project(...))` list now also
  applies `alias(libs.plugins.kover)` in its own `plugins { }` block — keep the two lists in sync
  if you add or remove a module.
- **`testDebugUnitTest` does not exist on these KMP modules at all — use `allTests` instead.**
  `koverVerify` kept failing with `lines covered percentage is 0.000000, but expected minimum is
  40` even after explicitly running `testDebugUnitTest` before it (first suspected as a
  Kover/AGP dependency-wiring gap). The real cause: under
  `com.android.kotlin.multiplatform.library`, `testDebugUnitTest` is a task name that only
  exists on `:androidApp` (a plain `com.android.application` module with no test sources of its
  own - its `testDebugUnitTest` runs and reports `NO-SOURCE`). None of `core:common`, `core:ui`,
  or the `feature:*` modules register a task by that name; Gradle silently skips a bare task name
  in any subproject where it doesn't exist rather than erroring, so **the "Unit tests" CI job had
  been passing on every prior commit without ever running a single one of this repo's actual
  tests** - confirmed by grepping that job's own log for `compileAndroidTest`/`testDebugUnitTest`
  per module and finding only `:androidApp:testDebugUnitTest NO-SOURCE`. Both the "Unit tests"
  job and the coverage job's Gradle command now use `allTests` - Kotlin Multiplatform's own
  aggregate task, which runs whatever test tasks each target actually registers (`commonTest`,
  the android target's JVM-executed tests, etc.) without needing to know AGP's naming for them.
  If you ever see `NO-SOURCE` next to a module you expected to have tests run, that's the same
  failure mode recurring - check the actual task name, don't assume `testDebugUnitTest` reaches
  it.
- **Switching to `allTests` still wasn't enough - Android host-side unit tests are opt-in under
  `com.android.kotlin.multiplatform.library`.** Coverage still failed with `lines covered
  percentage is 0.000000` after the `allTests` fix above, and `compileTestKotlinIosSimulatorArm64`
  was the only test-compile task that ran per module (visible in the job log as `> Task
  :feature:splash:compileTestKotlinIosSimulatorArm64` immediately followed by `> Task
  :feature:splash:allTests NO-SOURCE`, with no `androidHostTest`/JVM test-compile task anywhere in
  the log). The root cause: under this plugin, **both** host-side (JVM) and instrumented Android
  unit tests are disabled by default for build-speed reasons - `androidLibrary { }` alone gives
  you a compile-only Android target, not a runnable one. `commonTest` was therefore only ever
  getting compiled for `iosSimulatorArm64`, which a Linux CI runner can't execute either, so
  `allTests` had nothing left it could actually run. Every module with real tests (`core:common`,
  `core:ui`, `feature:splash`, `feature:search`, `feature:settings`) plus `composeApp` (for
  consistency with the rest of the `kover(project(...))` list, even though it has no test sources
  yet) now calls `withHostTestBuilder {}.configure {}` inside their `androidLibrary { }` block to
  opt into the JVM-executed "host test" compilation (its dependencies, if ever needed beyond what
  `commonTest.dependencies` already provides, would go in an `androidHostTest` source set). This
  was diagnosed from the Android Kotlin Multiplatform plugin docs
  (https://developer.android.com/kotlin/multiplatform/plugin), not verified by an actual local
  build (see the sandboxed-container note above) - CI on the next push is the real signal.
- **The first real Kotlin *compiler* error didn't surface until CI got past all of the above.**
  Every earlier CI failure happened at Gradle configuration or an AGP verification task, before
  any `compileAndroidMain`/`compileKotlin` task ever ran - so a genuine bug in this repo's own
  Kotlin source (three `UseCase` subclasses missing the `()` constructor call in their supertype
  list: `: UseCase<P, R>` instead of `: UseCase<P, R>()`, since `UseCase` is an abstract class,
  not an interface) went undetected through five earlier fix-and-push rounds. If you can't run a
  full Gradle build locally, a standalone `ktlint` pass (see the style-rules bullet below) checks
  formatting but **not** whether the code actually compiles - don't mistake a clean `ktlint` run
  for a clean build.
- **`Dispatchers.IO` is `internal` on Kotlin/Native, unlike the JVM/Android artifact.** The iOS
  build failed compiling `core:common`'s `iosMain` with `Cannot access 'val IO: CoroutineDispatcher':
  it is internal in 'kotlinx.coroutines.Dispatchers'`. Kotlin/Native has no equivalent
  blocking-IO thread pool convention, so `IosDispatcherProvider.io` maps to `Dispatchers.Default`
  instead - the accepted substitute, not a workaround to revisit later.
- **The Xcode project only builds for `arm64` simulators, not `x86_64`, on purpose.** The repo
  targets `iosArm64` + `iosSimulatorArm64` only (see the "no `iosX64`" note in Stack, above), but
  Xcode's default `VALID_ARCHS` for the simulator SDK includes `x86_64` too, so a plain
  `-destination "generic/platform=iOS Simulator"` build asks Gradle for an `ios_x64` framework
  slice that doesn't exist - `validateArchitecturesForEmbedAndSignAppleFrameworkForXcode` fails
  with "Xcode requested target architectures that are not configured in your Gradle build:
  ios_x64", and Compose Multiplatform's resource sync task fails the same way ("Unknown iOS
  simulator arch: 'x86_64'"). Both build configurations in `project.pbxproj` set
  `EXCLUDED_ARCHS[sdk=iphonesimulator*] = x86_64` for this reason - don't remove it without also
  adding `iosX64()` back to every module's `kotlin { }` block (which the "no iosX64" convention
  explicitly avoids).
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
