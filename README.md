# Truesight

Privacy-focused link cleaner for Android and iOS, powered by a Kotlin Multiplatform (KMP) shared core.

Truesight extracts links from shared or pasted text, unwraps redirect links, removes tracking parameters, and gives quick actions to share/copy/open the cleaned URL.

## Table of Contents

- [Features](#features)
- [Project Structure](#project-structure)
- [Quickstart](#quickstart)
- [Development Workflow](#development-workflow)
- [Configuration Notes](#configuration-notes)
- [Contributing](#contributing)
- [License](#license)

## Features

- Extracts the first URL from arbitrary text.
- Unwraps common redirect wrappers (Google, Reddit, Facebook, Amazon, AMP cache variants).
- Follows selected remote redirects (Google Share, Reddit, `amzn.to`, `a.co`) before sanitization.
- Reddit-aware resolution preserves canonical discussion links:
  - follows Reddit short/share links (`/s/...`) to their comments permalinks
  - keeps text/self and internal gallery/media posts on the Reddit permalink
  - resolves to external destinations only for external link posts
- Removes common tracking query parameters (`utm_*`, click IDs, etc.).
- Domain-specific cleaning behavior, including:
  - Instagram share params
  - Medium tracking params (`source`, `sk`)
  - Amazon tracking/affiliate params (with optional affiliate-tag preservation)
  - Reddit share params (`share_id`, `rdt`)
  - Zillow and Redfin tracking params
- Optional Twitter/X to Nitter rewrite.
- Per-domain policy controls for:
  - redirect/unwrap behavior
  - parameter stripping behavior
- Global UTM stripping with a dedicated settings toggle:
  - removes `utm_*` params on all hosts when enabled (default)
  - preserves `utm_*` params when disabled
- Android share-overlay flow for fast cleaning from other apps.
- KMP shared cleaner core with iOS adapter scaffolding.

## Project Structure

- `app/` - Android app (Jetpack Compose UI + Android adapters)
- `shared/` - KMP shared cleaner engine and tests
- `iosApp/` - iOS integration adapters and setup notes
- `config/detekt/` - Detekt configuration
- `.github/workflows/ci.yml` - CI verification workflow

## Quickstart

- Jump to: [Prerequisites](#prerequisites) | [First-time setup](#first-time-setup-new-machine) | [Build Android debug APK](#build-android-debug-apk) | [Run tests](#run-tests) | [Run lint profile](#run-lint-profile)

### Prerequisites

- [Android Studio (latest stable)](https://developer.android.com/studio)
- [JDK 17 (Temurin recommended)](https://adoptium.net/temurin/releases/?version=17)
- Android SDK Platform 36 + Build-Tools (via Android Studio SDK Manager)

### First-time setup (new machine)

1. Verify local tooling:

```bash
java -version
./gradlew -v
adb version
```

2. Open this project in [Android Studio](https://developer.android.com/studio) and let Gradle sync/download dependencies.
3. If Android Studio does not auto-detect your SDK, set `sdk.dir` in `local.properties`:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

### Build Android debug APK

```bash
./gradlew :app:assembleDebug
```

### Install to connected device/emulator

```bash
./gradlew :app:installDebug
```

### Run tests

```bash
./gradlew testDebugUnitTest :shared:test
```

### Run lint profile

```bash
./gradlew lintAll
```

`lintAll` currently runs:

- `:app:ktlintCheck`
- `:shared:ktlintCheck`
- `:app:detekt`
- `:shared:detekt`
- `:app:lintDebug`

### Helpful links

- [Android Emulator setup](https://developer.android.com/studio/run/emulator)
- [ADB and device debugging](https://developer.android.com/tools/adb)
- [Kotlin Multiplatform docs](https://kotlinlang.org/docs/multiplatform.html)
- [iOS integration guide](iosApp/README.md)
- [CI workflow](.github/workflows/ci.yml)

## Development Workflow

- Format Kotlin files:

```bash
./gradlew :app:ktlintFormat :shared:ktlintFormat
```

- Full local verification before PR:

```bash
./gradlew lintAll testDebugUnitTest :shared:test
```

CI runs equivalent checks on pull requests.

## Configuration Notes

- Android entry points:
  - `MainActivity` for normal app flow
  - `ShareOverlayActivity` for share-sheet integration (`text/plain`)
- Shared cleaner pipeline and policy live in `shared/src/commonMain`.
- iOS integration instructions are in [iosApp/README.md](iosApp/README.md).

## Contributing

Contributions are welcome.

### How to contribute

1. Fork the repository.
2. Create a feature branch (`feat/...` or `fix/...`).
3. Make focused changes with tests where practical.
4. Run local verification:

```bash
./gradlew lintAll testDebugUnitTest :shared:test
```

5. Open a pull request with:
   - a clear problem statement
   - a concise change summary
   - test/verification notes
   - screenshots or screen recordings for UI changes

### Reporting bugs / requesting features

- Open an issue with:
  - expected behavior
  - actual behavior
  - reproduction steps
  - device/OS details when relevant

## License

This project is licensed under the Apache License 2.0. See [`LICENSE`](LICENSE).
