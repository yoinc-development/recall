import Foundation

enum RecallPersistence {
    private static var fileURL: URL {
        FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("recalls.json")
    }

    static func load() -> [RecallData] {
        guard
            let data = try? Data(contentsOf: fileURL),
            let recalls = try? JSONDecoder().decode([RecallData].self, from: data)
        else { return [] }
        return recalls
    }

    static func save(_ recalls: [RecallData]) {
        guard let data = try? JSONEncoder().encode(recalls) else { return }
        let dir = fileURL.deletingLastPathComponent()
        try? FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        try? data.write(to: fileURL, options: .atomic)
    }
}
