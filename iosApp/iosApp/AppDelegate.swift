import UIKit
import UserNotifications

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    // Set by iOSApp once the scene is ready; pending taps are buffered until then.
    var onNotificationTap: ((String) -> Void)?
    private var pendingRecallId: String?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        return true
    }

    // Called when the user taps a notification (both cold-launch and foreground).
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        if let recallId = response.notification.request.content.userInfo["recall_id"] as? String {
            if let handler = onNotificationTap {
                handler(recallId)
            } else {
                pendingRecallId = recallId
            }
        }
        completionHandler()
    }

    // Show notification banner even when the app is in the foreground.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound])
    }

    // Called by iOSApp once the scene is ready so that any cold-launch tap is delivered.
    func flushPendingTap() {
        guard let id = pendingRecallId, let handler = onNotificationTap else { return }
        pendingRecallId = nil
        handler(id)
    }
}
