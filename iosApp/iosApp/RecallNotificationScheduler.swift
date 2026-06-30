import UserNotifications

enum RecallNotificationScheduler {

    static func requestAuthorization(completion: @escaping (Bool) -> Void) {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound]) { granted, _ in
            DispatchQueue.main.async { completion(granted) }
        }
    }

    static func schedule(for recall: RecallData) {
        let center = UNUserNotificationCenter.current()
        for dayName in recall.days {
            guard let weekday = Self.weekday(for: dayName) else { continue }

            let content = UNMutableNotificationContent()
            content.title = recall.text
            if !recall.body.isEmpty { content.body = recall.body }
            content.sound = .default
            content.userInfo = ["recall_id": recall.id]
            content.interruptionLevel = .timeSensitive

            var components = DateComponents()
            components.weekday = weekday
            components.hour = recall.hour
            components.minute = recall.minute
            components.second = 0

            let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: true)
            let request = UNNotificationRequest(
                identifier: notificationId(recallId: recall.id, dayName: dayName),
                content: content,
                trigger: trigger
            )
            center.add(request)
        }
    }

    static func cancel(recallId: String) {
        let center = UNUserNotificationCenter.current()
        let allDays = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"]
        center.removePendingNotificationRequests(withIdentifiers: allDays.map { notificationId(recallId: recallId, dayName: $0) })
    }

    static func rescheduleAll(_ recalls: [RecallData]) {
        let center = UNUserNotificationCenter.current()
        center.removeAllPendingNotificationRequests()
        recalls.forEach { schedule(for: $0) }
    }

    private static func notificationId(recallId: String, dayName: String) -> String {
        "recall_\(recallId)_\(dayName)"
    }

    private static func weekday(for dayName: String) -> Int? {
        switch dayName {
        case "MONDAY": return 2
        case "TUESDAY": return 3
        case "WEDNESDAY": return 4
        case "THURSDAY": return 5
        case "FRIDAY": return 6
        case "SATURDAY": return 7
        case "SUNDAY": return 1
        default: return nil
        }
    }
}
