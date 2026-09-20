import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    init() {
        // Starts the shared Koin graph once for the process. See :composeApp's InitKoinIos.kt
        // for why this is `doInitKoin` and not `initKoin`. Kotlin/Native's Objective-C exporter
        // puts every file's top-level declarations on a per-file facade class named
        // `<FileName>Kt` (same convention ContentView.swift already relies on for
        // `MainViewControllerKt.MainViewController()`), so Swift must call this one as
        // `InitKoinIosKt.doInitKoin()` rather than as a bare global function.
        InitKoinIosKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
