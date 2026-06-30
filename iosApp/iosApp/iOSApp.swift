import SwiftUI

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var delegate
    @StateObject private var store = RecallStore()

    var body: some Scene {
        WindowGroup {
            RecallView(store: store)
                .onAppear {
                    delegate.onNotificationTap = { id in
                        Task { @MainActor in store.openRecall(id: id) }
                    }
                    delegate.flushPendingTap()
                    RecallNotificationScheduler.requestAuthorization { granted in
                        if granted { Task { @MainActor in store.scheduleAll() } }
                    }
                }
        }
    }
}
