import Foundation
import SharedCleaner

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

    func loadPolicy() -> CleanerPolicy {
        service.loadPolicy()
    }

    func savePolicy(_ policy: CleanerPolicy) {
        service.savePolicy(policy: policy)
    }
}
