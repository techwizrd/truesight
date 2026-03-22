# F-Droid Submission Notes

This directory stores metadata and submission notes for publishing Truesight to F-Droid.

## Files

- `org.sarkhel.truesight.yml`: metadata file to submit in `fdroiddata/metadata/`.

## Local pre-submission checklist

1. Confirm release tag and app version values match:
   - `app/build.gradle.kts` (`versionName`, `versionCode`)
   - `fdroid/org.sarkhel.truesight.yml` (`CurrentVersion`, `CurrentVersionCode`, latest `Builds` entry)
2. Verify Android build from source:
   - `./gradlew :app:assembleRelease`
3. Verify tests and lint:
   - `./gradlew lintAll testDebugUnitTest :shared:test`
4. Ensure release tag exists and points to the exact build commit:
   - `git tag --list 'v*'`

## F-Droid merge request checklist

1. Fork `https://gitlab.com/fdroid/fdroiddata`.
2. Create `metadata/org.sarkhel.truesight.yml` using this repository copy.
3. Run in your `fdroiddata` clone:
   - `fdroid readmeta`
   - `fdroid lint org.sarkhel.truesight`
   - `fdroid build -v -l org.sarkhel.truesight`
4. Open merge request to `fdroiddata` with build logs.
