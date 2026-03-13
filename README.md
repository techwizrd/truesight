# Truesight

[![CI](https://img.shields.io/github/actions/workflow/status/techwizrd/truesight/ci.yml?branch=main&label=CI)](https://github.com/techwizrd/truesight/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/techwizrd/truesight)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![Android](https://img.shields.io/badge/Android-Jetpack%20Compose-3DDC84?logo=android)](https://developer.android.com/jetpack/compose)

Privacy-focused link cleaner for Android and iOS, powered by a Kotlin Multiplatform shared core.

Truesight takes messy shared links, unwraps redirect layers, strips tracking parameters, and gives fast actions to copy/share/open the clean URL.

## Why Truesight

- Reduce link tracking clutter before sharing links with others.
- Keep links readable and easier to trust.
- Use one cleaner engine across Android and iOS.
- Customize behavior by domain and tracking source.

## Example

Input:

```text
https://www.google.com/url?q=https%3A%2F%2Fexample.com%2Farticle%3Futm_source%3Dnewsletter%26id%3D42&sa=D
```

Output:

```text
https://example.com/article?id=42
```

## Features

- Extracts the first URL from pasted/shared text.
- Unwraps common redirect wrappers (Google, Reddit, Facebook, Amazon, AMP cache variants).
- Follows selected remote redirects (`share.google.com`, Reddit, `amzn.to`, `a.co`) before sanitization.
- Removes tracking query params (`utm_*`, click IDs, ad campaign params, host-specific trackers).
- Reddit-aware behavior:
  - resolves short/share links to canonical comment permalinks
  - preserves Reddit permalinks for self/internal media posts
  - resolves to destination URLs only for external link posts
- Domain-aware stripping for Amazon, Instagram, Medium, Reddit, Zillow, Redfin, and more.
- Optional Twitter/X to Nitter rewrite.
- Android share-overlay for one-step cleaning from other apps.
- Per-domain and per-vendor policy controls in Settings.

## New Here? Start in 2 Minutes

### Prerequisites

- [Android Studio (latest stable)](https://developer.android.com/studio)
- [JDK 17](https://adoptium.net/temurin/releases/?version=17)
- Android SDK Platform 36 + Build-Tools

### First-time setup

```bash
java -version
./gradlew -v
adb version
```

Open this project in Android Studio and let Gradle sync.

If needed, set SDK path in `local.properties`:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

### Build and run

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

### Verify locally before PR

```bash
./gradlew lintAll testDebugUnitTest :shared:test
```

## Project Structure

- `app/` - Android app (Jetpack Compose UI and platform adapters)
- `shared/` - Kotlin Multiplatform cleaner core + tests
- `iosApp/` - iOS bridge/adapters and setup notes
- `config/detekt/` - detekt rules
- `.github/workflows/ci.yml` - CI pipeline

## Architecture at a Glance

- `UrlCleanerCore` in `shared/` owns clean-link business logic.
- Android and iOS call the same shared core and supply platform-specific redirect/policy adapters.
- UI screens (`MainActivity`, `ShareOverlayActivity`) are thin layers over ViewModels + shared cleaner behavior.

## Contributing

Contributions are welcome and appreciated.

### Good first contributions

- add/expand cleaner test cases for real-world URL patterns
- improve settings UX copy and help text
- improve iOS integration ergonomics and docs
- performance and battery optimizations in redirect handling

### How to contribute

1. Fork and create a branch (`feat/...` or `fix/...`).
2. Make focused changes with tests when practical.
3. Run:

```bash
./gradlew lintAll testDebugUnitTest :shared:test
```

4. Open a PR with problem, solution, and verification notes.

## Helpful Links

- [iOS integration guide](iosApp/README.md)
- [CI workflow](.github/workflows/ci.yml)
- [Kotlin Multiplatform docs](https://kotlinlang.org/docs/multiplatform.html)
- [Android Emulator setup](https://developer.android.com/studio/run/emulator)

## License

Licensed under Apache License 2.0. See [`LICENSE`](LICENSE).
