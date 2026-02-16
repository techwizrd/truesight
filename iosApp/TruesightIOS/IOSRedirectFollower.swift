import Foundation
import SharedCleaner

final class IOSRedirectFollower: NSObject, RedirectFollower {
    private let session: URLSession
    private let timeout: TimeInterval = 3.5

    override init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 3.5
        config.timeoutIntervalForResource = 3.5
        self.session = URLSession(configuration: config)
        super.init()
    }

    func follow(url: String, policy: CleanerPolicy) -> String {
        guard shouldFollow(url: url, policy: policy) else {
            return url
        }

        let followed = resolveFinalURL(url: url) ?? url
        let redditResolved = resolveRedditDestinationIfNeeded(url: followed)
        return redditResolved ?? followed
    }

    private func shouldFollow(url: String, policy: CleanerPolicy) -> Bool {
        guard let host = URL(string: url)?.host?.lowercased() else {
            return false
        }

        if !policy.isRedirectEnabledForHost(host: host) {
            return false
        }

        return host == "share.google.com" ||
            host == "share.google" ||
            host == "amzn.to" ||
            host == "a.co" ||
            host == "reddit.com" ||
            host == "redd.it" ||
            host.hasSuffix(".reddit.com")
    }

    private func resolveFinalURL(url: String) -> String? {
        guard let target = URL(string: url) else {
            return nil
        }

        var request = URLRequest(url: target)
        request.httpMethod = "GET"
        request.timeoutInterval = timeout
        request.setValue(
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile",
            forHTTPHeaderField: "User-Agent"
        )

        let semaphore = DispatchSemaphore(value: 0)
        var resolved: String?

        let task = session.dataTask(with: request) { _, response, _ in
            if let finalURL = response?.url?.absoluteString {
                resolved = finalURL
            }
            semaphore.signal()
        }

        task.resume()
        _ = semaphore.wait(timeout: .now() + timeout)
        return resolved
    }

    private func resolveRedditDestinationIfNeeded(url: String) -> String? {
        guard let parsed = URL(string: url), let host = parsed.host?.lowercased() else {
            return nil
        }

        let isReddit = host == "reddit.com" || host == "redd.it" || host.hasSuffix(".reddit.com")
        guard isReddit, parsed.path.contains("/comments/") else {
            return nil
        }

        guard let jsonURL = buildRedditJsonURL(from: parsed),
              let data = fetchData(url: jsonURL),
              let root = try? JSONSerialization.jsonObject(with: data) as? [[String: Any]],
              let listing = root.first,
              let dataNode = listing["data"] as? [String: Any],
              let children = dataNode["children"] as? [[String: Any]],
              let child = children.first,
              let childData = child["data"] as? [String: Any]
        else {
            return nil
        }

        if let overridden = childData["url_overridden_by_dest"] as? String,
           overridden.hasPrefix("http://") || overridden.hasPrefix("https://") {
            return overridden
        }

        if let fallback = childData["url"] as? String,
           fallback.hasPrefix("http://") || fallback.hasPrefix("https://") {
            return fallback
        }

        return nil
    }

    private func buildRedditJsonURL(from url: URL) -> URL? {
        var components = URLComponents(url: url, resolvingAgainstBaseURL: false)
        var path = components?.path ?? ""
        if !path.hasSuffix(".json") {
            path = path.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
            path = "/\(path).json"
        }
        components?.path = path

        var items = components?.queryItems ?? []
        items.append(URLQueryItem(name: "raw_json", value: "1"))
        components?.queryItems = items
        return components?.url
    }

    private func fetchData(url: URL) -> Data? {
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.timeoutInterval = timeout
        request.setValue(
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile",
            forHTTPHeaderField: "User-Agent"
        )

        let semaphore = DispatchSemaphore(value: 0)
        var body: Data?

        let task = session.dataTask(with: request) { data, response, _ in
            if let http = response as? HTTPURLResponse, (200..<300).contains(http.statusCode) {
                body = data
            }
            semaphore.signal()
        }

        task.resume()
        _ = semaphore.wait(timeout: .now() + timeout)
        return body
    }
}
