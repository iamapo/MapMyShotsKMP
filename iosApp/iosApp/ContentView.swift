import UIKit
import SwiftUI
import ComposeApp

struct ComposeHost: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        Main_iosKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeHost()
            .onAppear {
                PHPhotoLibrary.requestAuthorization(for: .readWrite) { _ in
                }
            }
    }
}



