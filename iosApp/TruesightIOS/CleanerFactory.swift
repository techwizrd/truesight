import Foundation
import SharedCleaner

enum CleanerFactory {
    static func makeService(defaults: UserDefaults = .standard) -> SharedCleanerService {
        let policyStore = IOSCleanerPolicyStore(defaults: defaults)
        let redirectFollower = IOSRedirectFollower()
        return SharedCleanerService(
            policyStore: policyStore,
            redirectFollower: redirectFollower
        )
    }
}
