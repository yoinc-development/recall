import Foundation

@MainActor
class RecallStore: ObservableObject {
    @Published var recalls: [RecallData] = []
    @Published var pendingRecallId: String?

    init() {
        recalls = RecallPersistence.load()
    }

    func scheduleAll() {
        RecallNotificationScheduler.rescheduleAll(recalls)
    }

    func add(text: String, days: [String], hour: Int, minute: Int, description: String) {
        let data = RecallData(
            id: UUID().uuidString,
            text: text,
            body: description,
            days: days,
            hour: hour,
            minute: minute
        )
        recalls.append(data)
        RecallPersistence.save(recalls)
        RecallNotificationScheduler.schedule(for: data)
    }

    func delete(id: String) {
        recalls.removeAll { $0.id == id }
        RecallPersistence.save(recalls)
        RecallNotificationScheduler.cancel(recallId: id)
    }

    func openRecall(id: String) {
        pendingRecallId = id
    }
}
