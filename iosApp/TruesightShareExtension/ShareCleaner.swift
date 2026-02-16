import Foundation
import SharedCleaner

final class ShareCleaner {
    private let service: SharedCleanerService

    init(service: SharedCleanerService = CleanerFactory.makeService()) {
        self.service = service
    }

    func clean(sharedText: String?) -> String? {
        guard let sharedText, !sharedText.isEmpty else {
            return nil
        }
        return service.cleanFirstUrlFromText(text: sharedText)
    }
}
