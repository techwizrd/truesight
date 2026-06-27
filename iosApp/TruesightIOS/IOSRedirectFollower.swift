import Foundation
import SharedCleaner

final class IOSRedirectFollower: NSObject, RedirectFollower, RedirectFollowerWithStats, RedirectLocationFetcher, RedirectBodyFetcher {
    private let timeout: TimeInterval = 3.5
    private let maxBodyChars = 256_000
    private let resolver = SharedRedirectResolver()

    func follow(url: String, policy: CleanerPolicy) -> String {
        followWithResult(url: url, policy: policy).resolvedUrl
    }

    func followWithResult(url: String, policy: CleanerPolicy) -> RedirectFollowResult {
        resolver.followWithResult(
            url: url,
            policy: policy,
            locationFetcher: self,
            bodyFetcher: self
        )
    }

    func fetchRedirectLocation(url: String) -> String? {
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
        var statusCode = 0
        var location: String?

        let redirectDelegate = RedirectProbeDelegate()
        let redirectSession = URLSession(
            configuration: URLSessionConfiguration.default,
            delegate: redirectDelegate,
            delegateQueue: nil
        )

        let task = redirectSession.dataTask(with: request) { _, response, _ in
            if let http = response as? HTTPURLResponse {
                statusCode = http.statusCode
                location = redirectDelegate.redirectLocation
            }
            semaphore.signal()
        }

        task.resume()
        _ = semaphore.wait(timeout: .now() + timeout)
        redirectSession.invalidateAndCancel()

        guard (300..<400).contains(statusCode) else {
            return nil
        }

        return location
    }

    func fetchBody(url: String) -> String? {
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
        var body: String?

        let task = URLSession.shared.dataTask(with: request) { data, response, _ in
            if let http = response as? HTTPURLResponse,
               (200..<300).contains(http.statusCode),
               let data,
               let text = String(data: data, encoding: .utf8) {
                body = String(text.prefix(self.maxBodyChars))
            }
            semaphore.signal()
        }

        task.resume()
        _ = semaphore.wait(timeout: .now() + timeout)
        return body
    }
}

private final class RedirectProbeDelegate: NSObject, URLSessionTaskDelegate {
    var redirectLocation: String?

    func urlSession(
        _ session: URLSession,
        task: URLSessionTask,
        willPerformHTTPRedirection response: HTTPURLResponse,
        newRequest request: URLRequest,
        completionHandler: @escaping (URLRequest?) -> Void
    ) {
        redirectLocation = response.value(forHTTPHeaderField: "Location")
        completionHandler(nil)
    }
}
