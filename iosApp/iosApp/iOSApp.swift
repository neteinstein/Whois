import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    init() {
        // Starts the shared Koin graph once for the process. See :composeApp's
        // InitKoinIos.kt for why this is `doInitKoin` and not `initKoin`.
        doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
