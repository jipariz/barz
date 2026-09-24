// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "Barz",
    platforms: [.iOS(.v18)],
    products: [
        .library(name: "Barz", targets: ["Barz"]),
    ],
    targets: [
        // Deliberately no dependency on the Kotlin framework. Barz's SwiftUI chrome is driven by
        // plain Swift values so this package builds and previews on its own; the app maps its
        // Kotlin NavigationItems into BarzItem at the call site. That keeps the Swift package
        // usable without a Gradle build and avoids pinning it to one framework layout.
        .target(name: "Barz"),
    ],
)
