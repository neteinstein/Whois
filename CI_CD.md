# CI/CD

## Topology

`.github/workflows/pr.yml` runs on every PR into `main` (and on push to `main`). Five jobs run in
parallel — none `needs:` another:

| Job          | Runner         | What it does                                                        |
|--------------|----------------|----------------------------------------------------------------------|
| `build`      | `ubuntu-latest`| `./gradlew :androidApp:assembleDebug` — compiles every module (all KMP targets get built as dependencies) and assembles the debug APK. |
| `unit-tests` | `ubuntu-latest`| `./gradlew allTests` — Kotlin Multiplatform's aggregate task; runs `commonTest` + the android target's JVM-executed tests for every module (see the `allTests` vs. `testDebugUnitTest` gotcha in `AGENTS.md` — the latter silently runs nothing on these modules). Uploads `**/build/reports/tests/` as an artifact. |
| `lint`       | `ubuntu-latest`| `ktlintCheck` (style, every module) + `:androidApp:lintDebug` (Android Lint). Uploads reports. |
| `coverage`   | `ubuntu-latest`| `koverXmlReport` + `koverVerify` (Kover, minimum line coverage 40% — see `kover { }` in the root `build.gradle.kts`). Uploads the XML/HTML report. |
| `ios-build`  | `macos-15`     | `xcodebuild ... build` against `iosApp/iosApp.xcodeproj`, targeting the iOS Simulator. This is what actually exercises the hand-authored Xcode project and the `embedAndSignAppleFrameworkForXcode` Gradle task — a Linux dev/agent box has no way to verify either. |

The four `ubuntu-latest` jobs share setup steps via the composite action
`.github/actions/setup-build-env/action.yml` (JDK 21, Gradle, and pinning the Android SDK
platform/build-tools versions explicitly with `sdkmanager` rather than trusting whatever happens
to be preinstalled on the runner image).

## Secrets

None are currently required. The app makes no server calls of its own (it only launches an
external browser to a public search URL) and has no Firebase/Google Services integration.

If a Google Services config (`google-services.json`) is added later: it must **not** be
committed (see `.gitignore`). Commit a `androidApp/google-services.json.ci` placeholder for
build validation instead — `setup-build-env` already copies it into place if it exists — and add
the real file's contents as a repository secret that a release/signing workflow writes out at
build time.

## Signing status

- **Android**: unsigned. `androidApp`'s `release` build type enables `isMinifyEnabled` /
  `isShrinkResources` (R8) but no signing config is wired up — there's no keystore, and none is
  committed (see `.gitignore`: `*.jks`, `*.keystore`). A real release pipeline needs its own
  workflow that decodes a keystore from a secret and applies a `signingConfig`; that does not
  exist yet, since there is no distribution channel configured for this app.
- **iOS**: `CODE_SIGN_STYLE = Automatic` with no team set, and CI passes
  `CODE_SIGNING_ALLOWED=NO` explicitly for the simulator build (no signing identity exists on
  the CI runner and none is needed to build for the simulator). Shipping to a device or
  TestFlight needs provisioning profiles/certificates wired up separately.

## Conventional Commits

Commit messages should follow Conventional Commits (`feat(scope): ...`, `fix(scope): ...`,
`docs(scope): ...`, etc.) so history stays scannable and a changelog could be generated later
if needed.
