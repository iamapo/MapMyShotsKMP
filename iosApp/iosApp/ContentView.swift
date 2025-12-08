import UIKit
import SwiftUI
import ComposeApp
import Photos

struct ComposeHost: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
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



