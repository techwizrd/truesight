import Foundation
import SharedCleaner

struct BridgeCleanFirstUrlResult {
    let originalUrl: String
    let cleanedUrl: String
    let paramsRemoved: Int
    let redirectsFollowed: Int
}

final class SharedCleanerBridge {
    private let service: SharedCleanerService

    init(service: SharedCleanerService = CleanerFactory.makeService()) {
        self.service = service
    }

    func cleanUrl(_ url: String) -> String {
        service.cleanUrl(url: url)
    }

    func cleanFirstUrl(fromText text: String) -> String? {
        service.cleanFirstUrlFromText(text: text)
    }

    func cleanFirstUrlWithResult(fromText text: String) -> BridgeCleanFirstUrlResult? {
        guard let result = service.cleanFirstUrlFromTextWithResult(text: text) else {
            return nil
        }
        return BridgeCleanFirstUrlResult(
            originalUrl: result.originalUrl,
            cleanedUrl: result.cleanedUrl,
            paramsRemoved: Int(result.paramsRemoved),
            redirectsFollowed: Int(result.redirectsFollowed)
        )
    }

    func loadPolicy() -> CleanerPolicy {
        service.loadPolicy()
    }

    func savePolicy(_ policy: CleanerPolicy) {
        service.savePolicy(policy: policy)
    }
}
