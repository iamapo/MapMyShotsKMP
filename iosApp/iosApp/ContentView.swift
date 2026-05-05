import UIKit
import SwiftUI
import ComposeApp
import Photos

private let appBackgroundColor = Color(
    red: 248.0 / 255.0,
    green: 250.0 / 255.0,
    blue: 253.0 / 255.0
)

struct ComposeHost: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let viewController = MainViewControllerKt.MainViewController()
        viewController.view.backgroundColor = UIColor.clear
        return viewController
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @State private var authStatus: PHAuthorizationStatus = PHPhotoLibrary.authorizationStatus(for: .readWrite)

    var body: some View {
        Group {
            switch authStatus {
            case .authorized, .limited:
                ComposeHost()
                    .ignoresSafeArea()
            case .notDetermined:
                ProgressView("Zugriff auf Fotos wird angefragt…")
                    .progressViewStyle(.circular)
            case .denied, .restricted:
                VStack(spacing: 12) {
                    Text("Kein Zugriff auf deine Fotos")
                        .font(.headline)
                    Text("Bitte erlaube den Fotozugriff in den iOS-Einstellungen.")
                        .font(.subheadline)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 24)
                    Button("Einstellungen öffnen") {
                        if let url = URL(string: UIApplication.openSettingsURLString) {
                            UIApplication.shared.open(url)
                        }
                    }
                }
                .padding()
            @unknown default:
                Text("Unbekannter Berechtigungsstatus")
            }
        }
        .onAppear {
            refreshAuthorization()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(appBackgroundColor.ignoresSafeArea())
    }

    private func refreshAuthorization() {
        let status = PHPhotoLibrary.authorizationStatus(for: .readWrite)
        authStatus = status
        guard status == .notDetermined else { return }

        PHPhotoLibrary.requestAuthorization(for: .readWrite) { newStatus in
            DispatchQueue.main.async {
                authStatus = newStatus
            }
        }
    }
}
