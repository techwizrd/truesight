import Foundation
import SharedCleaner

enum IOSCleanerStatus {
    case manualHint
    case noUrl
    case alreadyClean
    case cleaned

    var title: String {
        switch self {
        case .manualHint:
            return "Share from another app or paste text below."
        case .noUrl:
            return "No URL found in the text."
        case .alreadyClean:
            return "Link looks clean already."
        case .cleaned:
            return "Cleaned link is ready."
        }
    }
}

@MainActor
final class IOSCleanerViewModel: ObservableObject {
    @Published var inputText: String = ""
    @Published private(set) var status: IOSCleanerStatus = .manualHint
    @Published private(set) var originalUrl: String = ""
    @Published private(set) var cleanedUrl: String = ""
    @Published private(set) var paramsRemoved: Int = 0
    @Published private(set) var redirectsFollowed: Int = 0
    @Published private(set) var isCleaning: Bool = false
    @Published private(set) var policy: CleanerPolicy

    private let bridge: SharedCleanerBridge
    private var cleaningToken: UUID?

    init(bridge: SharedCleanerBridge = SharedCleanerBridge()) {
        self.bridge = bridge
        self.policy = bridge.loadPolicy()
    }

    func handleIncomingSharedText(_ text: String) {
        inputText = text
        cleanFromCurrentInput()
    }

    func cleanFromCurrentInput() {
        cleanFirstUrl(fromText: inputText)
    }

    func updatePolicy(_ transform: (CleanerPolicy) -> CleanerPolicy) {
        let updated = transform(policy)
        policy = updated
        bridge.savePolicy(updated)
    }

    private func cleanFirstUrl(fromText text: String) {
        let token = UUID()
        cleaningToken = token
        isCleaning = true
        originalUrl = ""
        cleanedUrl = ""
        paramsRemoved = 0
        redirectsFollowed = 0

        DispatchQueue.global(qos: .userInitiated).async { [bridge] in
            let result = bridge.cleanFirstUrlWithResult(fromText: text)

            DispatchQueue.main.async {
                guard self.cleaningToken == token else {
                    return
                }

                guard let result else {
                    self.originalUrl = ""
                    self.cleanedUrl = ""
                    self.status = .noUrl
                    self.paramsRemoved = 0
                    self.redirectsFollowed = 0
                    self.isCleaning = false
                    return
                }

                self.originalUrl = result.originalUrl
                self.cleanedUrl = result.cleanedUrl
                self.paramsRemoved = result.paramsRemoved
                self.redirectsFollowed = result.redirectsFollowed
                self.status = result.cleanedUrl == result.originalUrl ? .alreadyClean : .cleaned
                self.isCleaning = false
            }
        }
    }
}
