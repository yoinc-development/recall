import Foundation

struct RecallData: Codable, Identifiable {
    let id: String
    let text: String
    let body: String
    let days: [String]   // e.g. ["MONDAY", "WEDNESDAY"]
    let hour: Int
    let minute: Int
}
