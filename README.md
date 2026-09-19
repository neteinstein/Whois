# Whois

A Kotlin Multiplatform (Android + iOS) app to quickly search the public web for information
about a person or company, using whatever details you already have on hand — a name, phone
number, company, or address.

## What it does

- **Splash** — an animated magnifying-glass-over-a-silhouette intro.
- **Search** (main screen) — enter a name, phone number, company and/or address, then tap
  **Search** to open Brave Search (preferring the Brave app if it's installed) with a
  natural-language "who is this?" prompt built from whatever fields you filled in.
- **Share a contact into the app** — on Android, sharing a contact from the Contacts/People app
  (`ACTION_SEND` with a vCard) prefills the search fields and immediately triggers the search.
  The vCard's `NOTE` field is never read, on purpose (see `AGENTS.md`).
- **Settings** — switch the in-app language between English and Portuguese, and an About section
  linking to [neteinstein/loopgain](https://github.com/neteinstein/loopgain).

## Stack

Kotlin Multiplatform + Compose Multiplatform, MVVM, Koin, coroutines/`StateFlow`, clean
architecture per feature module. See `AGENTS.md` for the full breakdown of modules, conventions,
and known gotchas, and `CI_CD.md` for how the GitHub Actions pipeline is wired up.

## Building

```
./gradlew :androidApp:assembleDebug
./gradlew testDebugUnitTest
```

iOS: open `iosApp/iosApp.xcodeproj` in Xcode (or run the `xcodebuild` command in `CI_CD.md`).

> Building this project needs a full Android SDK + network access to Google's Maven repository,
> and the iOS build needs a Mac with Xcode. Neither was available in the sandbox this project was
> authored in — see the "Sandboxed/cloud agent containers" section of `AGENTS.md` before assuming
> a build failure locally means the code is wrong; check the PR's CI run first.
