import SwiftUI

struct RecallView: View {
    @ObservedObject var store: RecallStore

    @State private var showAdd = false
    @State private var selectedRecall: RecallData?

    var body: some View {
        NavigationStack {
            Group {
                if store.recalls.isEmpty {
                    emptyState
                } else {
                    List {
                        ForEach(store.recalls) { recall in
                            RecallRow(recall: recall)
                                .contentShape(Rectangle())
                                .onTapGesture { selectedRecall = recall }
                                .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                                    Button(role: .destructive) {
                                        store.delete(id: recall.id)
                                    } label: {
                                        Label("Delete", systemImage: "trash")
                                    }
                                }
                        }
                    }
                    .listStyle(.insetGrouped)
                }
            }
            .navigationTitle("Your Recalls")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button { showAdd = true } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showAdd) {
                AddRecallSheet { text, days, hour, minute, description in
                    store.add(text: text, days: days, hour: hour, minute: minute, description: description)
                }
            }
            .sheet(item: $selectedRecall) { recall in
                RecallDetailSheet(recall: recall)
            }
            .onChange(of: store.pendingRecallId) { _, id in
                guard let id, let recall = store.recalls.first(where: { $0.id == id }) else { return }
                selectedRecall = recall
                store.pendingRecallId = nil
            }
        }
    }

    private var emptyState: some View {
        VStack(spacing: 12) {
            Image(systemName: "bell.slash")
                .font(.system(size: 48))
                .foregroundStyle(.secondary)
            Text("No Recalls Yet")
                .font(.headline)
            Text("Tap + to add something you want to be reminded of.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
        }
    }
}

// MARK: – Row

private struct RecallRow: View {
    let recall: RecallData

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(recall.text)
                .font(.body)
            let subtitle = recallSubtitle
            if !subtitle.isEmpty {
                Text(subtitle)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.vertical, 2)
    }

    private var recallSubtitle: String {
        let dayPart = recall.days
            .compactMap { RecallDay(rawValue: $0)?.label }
            .joined(separator: " ")
        let timePart = String(format: "%02d:%02d", recall.hour, recall.minute)
        return [dayPart, timePart].filter { !$0.isEmpty }.joined(separator: "  ")
    }
}

// MARK: – Add sheet

private struct AddRecallSheet: View {
    let onAdd: (String, [String], Int, Int, String) -> Void
    @Environment(\.dismiss) private var dismiss

    @State private var text = ""
    @State private var selectedDays: Set<String> = []
    @State private var time = Calendar.current.date(bySettingHour: 9, minute: 0, second: 0, of: .now) ?? .now
    @State private var description = ""

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    TextField("What do you want to recall?", text: $text)
                }
                Section("Days") {
                    DayPicker(selectedDays: $selectedDays)
                        .padding(.vertical, 4)
                }
                Section("Time") {
                    DatePicker("Reminder time", selection: $time, displayedComponents: .hourAndMinute)
                        .datePickerStyle(.wheel)
                        .labelsHidden()
                        .frame(maxWidth: .infinity)
                }
                Section("Description") {
                    TextField("Optional notes", text: $description, axis: .vertical)
                        .lineLimit(3...6)
                }
            }
            .navigationTitle("New Recall")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Add") {
                        let comps = Calendar.current.dateComponents([.hour, .minute], from: time)
                        onAdd(
                            text.trimmingCharacters(in: .whitespaces),
                            Array(selectedDays),
                            comps.hour ?? 9,
                            comps.minute ?? 0,
                            description.trimmingCharacters(in: .whitespaces)
                        )
                        dismiss()
                    }
                    .disabled(text.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
    }
}

// MARK: – Detail sheet

private struct RecallDetailSheet: View {
    let recall: RecallData
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Section("Days") {
                    DayDisplay(activeDays: recall.days)
                        .padding(.vertical, 4)
                }
                Section("Time") {
                    Text(String(format: "%02d:%02d", recall.hour, recall.minute))
                }
                if !recall.body.isEmpty {
                    Section("Description") {
                        Text(recall.body)
                    }
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle(recall.text)
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }
}

// MARK: – Day widgets

private struct DayPicker: View {
    @Binding var selectedDays: Set<String>

    var body: some View {
        HStack(spacing: 0) {
            ForEach(RecallDay.allCases) { day in
                let selected = selectedDays.contains(day.rawValue)
                Button {
                    if selected { selectedDays.remove(day.rawValue) }
                    else { selectedDays.insert(day.rawValue) }
                } label: {
                    Text(day.label)
                        .font(.caption)
                        .fontWeight(.medium)
                        .frame(width: 36, height: 36)
                        .background(selected ? Color.accentColor : Color(.systemGray5))
                        .foregroundStyle(selected ? Color.white : Color.primary)
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)
                .frame(maxWidth: .infinity)
            }
        }
    }
}

private struct DayDisplay: View {
    let activeDays: [String]

    var body: some View {
        HStack(spacing: 0) {
            ForEach(RecallDay.allCases) { day in
                let active = activeDays.contains(day.rawValue)
                Text(day.label)
                    .font(.caption)
                    .fontWeight(.medium)
                    .frame(width: 36, height: 36)
                    .background(active ? Color.accentColor : Color(.systemGray5))
                    .foregroundStyle(active ? Color.white : Color(.systemGray2))
                    .clipShape(Circle())
                    .frame(maxWidth: .infinity)
            }
        }
    }
}

// MARK: – Day model

private enum RecallDay: String, CaseIterable, Identifiable {
    case monday = "MONDAY", tuesday = "TUESDAY", wednesday = "WEDNESDAY"
    case thursday = "THURSDAY", friday = "FRIDAY", saturday = "SATURDAY", sunday = "SUNDAY"

    var id: String { rawValue }

    var label: String {
        switch self {
        case .monday:    "Mo"
        case .tuesday:   "Tu"
        case .wednesday: "We"
        case .thursday:  "Th"
        case .friday:    "Fr"
        case .saturday:  "Sa"
        case .sunday:    "Su"
        }
    }
}
