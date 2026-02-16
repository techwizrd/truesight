# iOS Integration (SwiftUI + Share Extension)

This directory contains iOS adapters for the shared KMP cleaner core.

## Files to add to targets

### iOS app target (`TruesightIOS`)

- `TruesightIOS/IOSCleanerPolicyStore.swift`
- `TruesightIOS/IOSRedirectFollower.swift`
- `TruesightIOS/CleanerFactory.swift`
- `TruesightIOS/SharedCleanerBridge.swift`

### Share extension target (`TruesightShareExtension`)

- `TruesightShareExtension/ShareCleaner.swift`
- `TruesightShareExtension/ShareExtensionRequestHandler.swift`

## Xcode linkage steps

1. Build KMP Apple framework from macOS:

   ```bash
   ./gradlew :shared:assembleXCFramework
   ```

2. In Xcode, add `SharedCleaner.xcframework` to both app and extension targets.
3. Ensure **Framework Search Paths** include the framework output folder.
4. Confirm `import SharedCleaner` resolves in all Swift files above.

## Usage in app target

```swift
let bridge = SharedCleanerBridge()
let cleaned = bridge.cleanFirstUrl(fromText: "Check this https://x.com/user/status/123?utm_source=share")
```

## Usage in share extension

```swift
let handler = ShareExtensionRequestHandler()
handler.handle(extensionContext: extensionContext)
```

## Notes

- Framework/module name is expected to be `SharedCleaner` (from KMP `baseName`).
- `SharedCleanerBridge` wraps KMP interop calls so the rest of your Swift code stays simple.
- This Linux environment cannot compile iOS targets, so validate final wiring on macOS/Xcode.
