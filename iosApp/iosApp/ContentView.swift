import SwiftUI

struct ContentView: View {
    let recalls: [String] = []

    var body: some View {
        RecallList(recalls: recalls)
    }
}

struct RecallList: View {
    let recalls: [String]

    var body: some View {
        if recalls.isEmpty {
            VStack(spacing: 8) {
                Text("No recalls added. Look at you, remembering everything.")
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
                Text("Add a Recall")
                    .foregroundColor(.blue)
                    .underline()
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
