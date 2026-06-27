import SharedCleaner
import SwiftUI
import UIKit

struct CleanerRootView: View {
    @StateObject private var viewModel = IOSCleanerViewModel()
    @State private var isShowingSettings = false

    var body: some View {
        NavigationStack {
            CleanerView(viewModel: viewModel)
                .navigationTitle("Truesight")
                .toolbar {
                    ToolbarItem(placement: .topBarTrailing) {
                        Button {
                            isShowingSettings = true
                        } label: {
                            Image(systemName: "gearshape")
                        }
                        .accessibilityLabel("Open settings")
                    }
                }
                .sheet(isPresented: $isShowingSettings) {
                    NavigationStack {
                        SettingsView(viewModel: viewModel)
                    }
                }
        }
    }
}

private struct CleanerView: View {
    @ObservedObject var viewModel: IOSCleanerViewModel
    @Environment(\.openURL) private var openURL

    var body: some View {
        Form {
            Section("Status") {
                Text(viewModel.status.title)
                if viewModel.isCleaning {
                    ProgressView("Resolving and cleaning link...")
                }
            }

            Section("Input") {
                TextEditor(text: $viewModel.inputText)
                    .frame(minHeight: 110)
                Button("Paste From Clipboard") {
                    if let clipboard = UIPasteboard.general.string, !clipboard.isEmpty {
                        viewModel.inputText = clipboard
                    }
                }
                Button("Clean Link") {
                    viewModel.cleanFromCurrentInput()
                }
                .disabled(viewModel.inputText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || viewModel.isCleaning)
            }

            if !viewModel.cleanedUrl.isEmpty {
                Section("Summary") {
                    Text("Removed \(viewModel.paramsRemoved) parameters and followed \(viewModel.redirectsFollowed) redirects")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }

                Section("Original URL") {
                    Text(viewModel.originalUrl)
                        .textSelection(.enabled)
                        .font(.footnote)
                }

                Section("Cleaned URL") {
                    Text(viewModel.cleanedUrl)
                        .textSelection(.enabled)
                }

                Section("Actions") {
                    ShareLink(item: viewModel.cleanedUrl) {
                        Label("Share", systemImage: "square.and.arrow.up")
                    }
                    Button {
                        UIPasteboard.general.string = viewModel.cleanedUrl
                    } label: {
                        Label("Copy", systemImage: "doc.on.doc")
                    }
                    Button {
                        guard let url = URL(string: viewModel.cleanedUrl) else {
                            return
                        }
                        openURL(url)
                    } label: {
                        Label("Open", systemImage: "safari")
                    }
                }
            }
        }
    }
}

private struct SettingsView: View {
    @ObservedObject var viewModel: IOSCleanerViewModel
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        Form {
            Section("Google Share") {
                Toggle("Follow redirects", isOn: policyBinding(
                    get: { $0.googleShareRedirectEnabled },
                    set: { PolicyDraft.cleaner(from: $0, googleShareRedirectEnabled: $1) }
                ))
                Toggle("Strip parameters", isOn: policyBinding(
                    get: { $0.googleShareStripEnabled },
                    set: { PolicyDraft.cleaner(from: $0, googleShareStripEnabled: $1) }
                ))
            }

            Section("Reddit") {
                Toggle("Follow redirects", isOn: policyBinding(
                    get: { $0.redditRedirectEnabled },
                    set: { PolicyDraft.cleaner(from: $0, redditRedirectEnabled: $1) }
                ))
                Toggle("Strip parameters", isOn: policyBinding(
                    get: { $0.redditStripEnabled },
                    set: { PolicyDraft.cleaner(from: $0, redditStripEnabled: $1) }
                ))
            }

            Section("Amazon") {
                Toggle("Follow redirects", isOn: policyBinding(
                    get: { $0.amazonRedirectEnabled },
                    set: { PolicyDraft.cleaner(from: $0, amazonRedirectEnabled: $1) }
                ))
                Toggle("Strip parameters", isOn: policyBinding(
                    get: { $0.amazonStripEnabled },
                    set: { PolicyDraft.cleaner(from: $0, amazonStripEnabled: $1) }
                ))
                Toggle("Remove Amazon affiliate tag", isOn: policyBinding(
                    get: { $0.amazonRemoveAffiliateTagEnabled },
                    set: { PolicyDraft.cleaner(from: $0, amazonRemoveAffiliateTagEnabled: $1) }
                ))
            }

            Section("Instagram") {
                Toggle("Follow redirects", isOn: policyBinding(
                    get: { $0.instagramRedirectEnabled },
                    set: { PolicyDraft.cleaner(from: $0, instagramRedirectEnabled: $1) }
                ))
                Toggle("Strip parameters", isOn: policyBinding(
                    get: { $0.instagramStripEnabled },
                    set: { PolicyDraft.cleaner(from: $0, instagramStripEnabled: $1) }
                ))
            }

            Section("AMP Cache") {
                Toggle("Follow redirects", isOn: policyBinding(
                    get: { $0.ampCacheRedirectEnabled },
                    set: { PolicyDraft.cleaner(from: $0, ampCacheRedirectEnabled: $1) }
                ))
                Toggle("Strip parameters", isOn: policyBinding(
                    get: { $0.ampCacheStripEnabled },
                    set: { PolicyDraft.cleaner(from: $0, ampCacheStripEnabled: $1) }
                ))
            }

            Section("General Tracking") {
                Toggle("Rewrite Twitter/X to Nitter", isOn: policyBinding(
                    get: { $0.twitterToNitterEnabled },
                    set: { PolicyDraft.cleaner(from: $0, twitterToNitterEnabled: $1) }
                ))
                Toggle("Strip UTM parameters (utm_*)", isOn: policyBinding(
                    get: { $0.utmTrackingStripEnabled },
                    set: { PolicyDraft.cleaner(from: $0, utmTrackingStripEnabled: $1) }
                ))
            }

            Section("Ad Tracking Vendors") {
                Toggle("Strip Google Ads parameters", isOn: adTrackingBinding(
                    get: { $0.googleEnabled },
                    set: { PolicyDraft.adTracking(from: $0, googleEnabled: $1) }
                ))
                Toggle("Aggressive Google Ads stripping (gad_*)", isOn: adTrackingBinding(
                    get: { $0.googleAggressiveEnabled },
                    set: { PolicyDraft.adTracking(from: $0, googleAggressiveEnabled: $1) }
                ))
                .disabled(!viewModel.policy.adTracking.googleEnabled)
                Toggle("Strip Meta Ads parameters", isOn: adTrackingBinding(
                    get: { $0.metaEnabled },
                    set: { PolicyDraft.adTracking(from: $0, metaEnabled: $1) }
                ))
                Toggle("Strip Microsoft Ads parameters", isOn: adTrackingBinding(
                    get: { $0.microsoftEnabled },
                    set: { PolicyDraft.adTracking(from: $0, microsoftEnabled: $1) }
                ))
                Toggle("Strip TikTok Ads parameters", isOn: adTrackingBinding(
                    get: { $0.tiktokEnabled },
                    set: { PolicyDraft.adTracking(from: $0, tiktokEnabled: $1) }
                ))
                Toggle("Strip Twitter/X Ads parameters", isOn: adTrackingBinding(
                    get: { $0.twitterEnabled },
                    set: { PolicyDraft.adTracking(from: $0, twitterEnabled: $1) }
                ))
                Toggle("Strip LinkedIn Ads parameters", isOn: adTrackingBinding(
                    get: { $0.linkedInEnabled },
                    set: { PolicyDraft.adTracking(from: $0, linkedInEnabled: $1) }
                ))
                Toggle("Strip Pinterest Ads parameters", isOn: adTrackingBinding(
                    get: { $0.pinterestEnabled },
                    set: { PolicyDraft.adTracking(from: $0, pinterestEnabled: $1) }
                ))
                Toggle("Strip Snapchat Ads parameters", isOn: adTrackingBinding(
                    get: { $0.snapchatEnabled },
                    set: { PolicyDraft.adTracking(from: $0, snapchatEnabled: $1) }
                ))
            }
        }
        .navigationTitle("Cleaning Preferences")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button("Done") {
                    dismiss()
                }
            }
        }
    }

    private func policyBinding(
        get: @escaping (CleanerPolicy) -> Bool,
        set: @escaping (CleanerPolicy, Bool) -> CleanerPolicy
    ) -> Binding<Bool> {
        Binding(
            get: {
                get(viewModel.policy)
            },
            set: { isOn in
                viewModel.updatePolicy { current in
                    set(current, isOn)
                }
            }
        )
    }

    private func adTrackingBinding(
        get: @escaping (AdTrackingPolicy) -> Bool,
        set: @escaping (AdTrackingPolicy, Bool) -> AdTrackingPolicy
    ) -> Binding<Bool> {
        Binding(
            get: {
                get(viewModel.policy.adTracking)
            },
            set: { isOn in
                viewModel.updatePolicy { current in
                    let updatedAdTracking = set(current.adTracking, isOn)
                    return PolicyDraft.cleaner(from: current, adTracking: updatedAdTracking)
                }
            }
        )
    }
}
