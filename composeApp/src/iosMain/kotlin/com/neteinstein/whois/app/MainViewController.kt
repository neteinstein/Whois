package com.neteinstein.whois.app

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** Entry point `iosApp`'s `ContentView.swift` wraps in a `UIViewControllerRepresentable`. */
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
