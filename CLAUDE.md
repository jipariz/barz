# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

A KMP sample (`adaptive-nav-suite`) demonstrating one specific thesis: **screen content is shared
Compose Multiplatform; navigation chrome is 100% native per platform.** Android wraps the shared
screen in Material 3 `NavigationSuiteScaffold`; iOS wraps it in SwiftUI `TabView(.sidebarAdaptable)`
+ `NavigationStack`. See README.md for the full rationale and the iPhone Duo argument.

The load-bearing constraint: **never replace the native nav containers with a Compose-drawn or
hand-rolled nav bar on iOS.** Apple's iPhone Duo guidance only grants adaptive placement to real
system containers. The whole point of the repo is lost if `RootView.swift`'s `TabView` becomes a
Compose surface.

## Commands

```sh
./gradlew :androidApp:assembleDebug            # build Android
./gradlew :androidApp:installDebug             # install on running emulator/device
./gradlew :shared:allTests                     # shared tests, all targets
./gradlew :shared:testAndroidHostTest          # shared tests, JVM host only (fast)
./gradlew :shared:iosSimulatorArm64Test        # shared tests, iOS sim target
```

Single test: `./gradlew :shared:testAndroidHostTest --tests "*AppDestinationTest.start*"`

iOS — the Xcode project has a "Compile Kotlin Framework" build phase that runs
`:shared:embedAndSignAppleFrameworkForXcode`, so no manual Gradle step first:

```sh
open iosApp/iosApp.xcodeproj
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build
```

No linter/formatter is configured. Gradle configuration cache and build cache are on
(`gradle.properties`); workers are capped at 2.

## Architecture

```
shared/     KMP module. commonMain: AppDestination (enum), DestinationContent,
            ui/DestinationScreen.kt (Compose Multiplatform). iosMain:
            MainViewController.kt exposes destinationViewController() via
            ComposeUIViewController. Built as static framework AdaptiveNavSuiteKit.
androidApp/ Compose app. AdaptiveNavApp.kt = NavigationSuiteScaffold + shared screen.
iosApp/     SwiftUI app. RootView.swift = TabView; DestinationDetailView.swift wraps
            the Compose controller in UIViewControllerRepresentable.
```

**Icon indirection.** `AppDestination` carries *string* icon keys (`materialIconName`,
`systemImageName`) rather than platform icon types, so `shared` stays framework-agnostic on icons.
`androidApp/.../ui/icons/DestinationIcons.kt` maps those keys to `ImageVector` via two maps and
**throws at runtime** on an unmapped key. Adding a destination therefore means touching three
places: the enum, `DestinationContent`'s two `when` blocks (exhaustive, so the compiler catches
those), and both maps in `DestinationIcons.kt` (the compiler does *not*). iOS needs no change —
SF Symbol names pass straight through.

**Swift bridging.** `allAppDestinations()` is a top-level function returning `List<AppDestination>`
because `EnumEntries` doesn't bridge cleanly; Swift sees `[AppDestination]`. Kotlin enums already
bridge as `Hashable` — adding a `Hashable` extension in Swift conflicts with the inherited
conformance. `Identifiable` is added retroactively using the shared `id: String`.

**Android nav-type override.** `AdaptiveNavApp` forces `NavigationSuiteType.NavigationDrawer` at
the expanded width breakpoint and defers to `NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo`
below it (bottom bar → rail). That override is deliberate, per Android's adaptive-navigation guide.

## Dependencies

All versions live in `gradle/libs.versions.toml`. `androidApp` pulls AndroidX Compose artifacts as
unversioned strings governed by the Compose BOM; `shared` uses the JetBrains Compose Multiplatform
artifacts (a separate version line, `composeMultiplatform` / `composeMultiplatformMaterial3`). Bump
the right one — they are not interchangeable.

Requires JDK 21, Xcode 16+ (iOS 18+ for `Tab` / `.sidebarAdaptable`), Android compileSdk/targetSdk 37,
minSdk 24.

## Note on README.md

README.md's Architecture section still claims `shared` has "no UI toolkit dependency — no Compose".
That is stale: `shared` depends on Compose Multiplatform and hosts `DestinationScreen`. The
no-Compose rule now applies only to *navigation chrome*, not to the module.
