import Foundation
import UIKit
import UniformTypeIdentifiers

final class ShareExtensionRequestHandler {
    private let cleaner: ShareCleaner

    init(cleaner: ShareCleaner = ShareCleaner()) {
        self.cleaner = cleaner
    }

    func handle(extensionContext: NSExtensionContext?) {
        extractSharedText(from: extensionContext) { [weak self] sharedText in
            guard let self else {
                extensionContext?.completeRequest(returningItems: nil, completionHandler: nil)
                return
            }

            if let cleaned = self.cleaner.clean(sharedText: sharedText) {
                UIPasteboard.general.string = cleaned
            }

            extensionContext?.completeRequest(returningItems: nil, completionHandler: nil)
        }
    }

    private func extractSharedText(from context: NSExtensionContext?, completion: @escaping (String?) -> Void) {
        guard
            let inputItem = context?.inputItems.first as? NSExtensionItem,
            let attachments = inputItem.attachments,
            !attachments.isEmpty
        else {
            completion(nil)
            return
        }

        let plainTextType = UTType.plainText.identifier
        let urlType = UTType.url.identifier

        let group = DispatchGroup()
        var result: String?

        for provider in attachments where result == nil {
            if provider.hasItemConformingToTypeIdentifier(plainTextType) {
                group.enter()
                provider.loadItem(forTypeIdentifier: plainTextType, options: nil) { item, _ in
                    if result == nil {
                        if let text = item as? String {
                            result = text
                        } else if let data = item as? Data, let text = String(data: data, encoding: .utf8) {
                            result = text
                        }
                    }
                    group.leave()
                }
            } else if provider.hasItemConformingToTypeIdentifier(urlType) {
                group.enter()
                provider.loadItem(forTypeIdentifier: urlType, options: nil) { item, _ in
                    if result == nil {
                        if let url = item as? URL {
                            result = url.absoluteString
                        } else if let text = item as? String {
                            result = text
                        }
                    }
                    group.leave()
                }
            }
        }

        group.notify(queue: .main) {
            completion(result)
        }
    }
}
