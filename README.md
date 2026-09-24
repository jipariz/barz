# Adaptive Nav Suite

A small Kotlin Multiplatform sample that shows what "adaptive navigation"
looks like when each platform uses its **own native, officially-recommended**
adaptive container — instead of one custom-drawn navigation bar reused as-is
on every OS.

It's inspired by
[narendraanjana09/adaptive-navigation-bar](https://github.com/narendraanjana09/adaptive-navigation-bar),
which shares a single Compose Multiplatform UI (including the nav bar itself)
between Android and iOS. This project takes the opposite approach for the
navigation chrome specifically:

- **Android** uses Material 3's
  [`NavigationSuiteScaffold`](https://developer.android.com/develop/adaptive-apps/guides/build-adaptive-navigation),
  which switches between a bottom `NavigationBar`, a `NavigationRail`, and a
  permanent `NavigationDrawer` as the window size class changes — the pattern
  Android's own adaptive-navigation guide recommends.
- **iOS / iPadOS** uses SwiftUI's system `TabView` with
  `.tabViewStyle(.sidebarAdaptable)`, which shows a bottom tab bar on iPhone
  and promotes the same tabs to a top bar / sidebar on iPad, per Apple's
  [Human Interface Guidelines for tab bars](https://developer.apple.com/documentation/human-interface-guidelines/tab-bars).
  Using a real system container (rather than a hand-rolled one) is also what
  keeps this app ready for **iPhone Duo** — see
  [below](#iphone-duo-readiness).

Only the *domain model and content* are shared in Kotlin; each platform's
navigation UI is 100% native to that platform.

## Architecture

```
adaptive-nav-suite/
├── shared/      Pure-Kotlin KMP module (no Compose dependency).
│                Domain model (AppDestination) + demo content
│                (DestinationContent). Consumed natively by both apps.
├── androidApp/  Jetpack Compose app. NavigationSuiteScaffold +
│                Material 3 theming.
└── iosApp/      SwiftUI app (Xcode project). TabView(.sidebarAdaptable) +
                 NavigationStack, consuming `shared` as an
                 Objective-C/Swift framework (AdaptiveNavSuiteKit).
```

`shared` intentionally has **no UI toolkit dependency** — no Compose, no
SwiftUI. It only exposes:

- `AppDestination`: an enum of the four demo destinations (Home, Favorites,
  Shopping, Profile), each carrying a Material icon name pair (outline /
  filled), an SF Symbols name for iOS, and a content-description string.
- `DestinationContent`: headline/body copy per destination (used to prove the
  same shared code path drives both platforms' content, not just their nav
  chrome).
- `allAppDestinations()`: a Swift-friendly top-level function returning
  `List<AppDestination>` (Kotlin `List` bridges directly to a Swift `[T]`).

Each platform's app then builds its **own native navigation container**
around that shared model.

### Android: `NavigationSuiteScaffold`

`androidApp/src/main/kotlin/.../ui/AdaptiveNavApp.kt` wraps content in a
`NavigationSuiteScaffold`, iterating `AppDestination.entries` for nav items.
It also overrides the suite type so a permanent `NavigationDrawer` (with
labels) is forced at the "expanded" width breakpoint, per the customization
snippet in Android's own adaptive-navigation guide — the default calculated
type is used below that breakpoint (bottom bar on compact, rail on medium).

Verified live on a `Pixel_10_Pro_Fold` emulator: folded/compact width shows a
bottom `NavigationBar`; unfolded/expanded width shows a permanent
`NavigationDrawer`. Tapping a destination swaps the Material icon to its
filled variant and updates the body text sourced from `DestinationContent`.

### iOS: `TabView(.sidebarAdaptable)` + `NavigationStack`

`iosApp/iosApp/RootView.swift` drives a `TabView(selection:)` over
`AppDestinationKt.allAppDestinations()`, using the iOS 18+ `Tab(title,
systemImage:, value:)` API and `.tabViewStyle(.sidebarAdaptable)`. Each tab
hosts a `NavigationStack` wrapping `DestinationDetailView`, which reads
`DestinationContent.shared.headline(destination:)` /
`.body(destination:)` — the same shared object the Android app uses.

Verified by building with `xcodebuild` and running on both an iPhone 17
simulator (bottom tab bar) and an iPad Pro 11" (M5) simulator (top tab bar
with a sidebar-toggle button) — confirming the same `TabView` adapts its
presentation per idiom automatically, with no per-device code.

#### iPhone Duo readiness

Apple's [Preparing your app for iPhone Duo](https://developer.apple.com/documentation/technologyoverviews/preparing-your-app-for-iphone-duo)
guidance describes a new `.axisBehavior(_:)` toolbar modifier for controlling
how system containers lay out across the two Duo displays. As of this
project's Xcode 26.6 / iOS 26.5 SDK, that API does not exist yet (it's
documented as shipping in a future SDK), so no code references it today —
doing so would fail to compile.

The guidance is explicit that **only real system containers pick up Duo
behavior automatically**; custom, hand-drawn navigation bars do not. That's
why this project deliberately avoids a Compose-drawn / hand-rolled nav bar on
iOS (unlike the reference project, which hosts a shared Compose
`UIViewController` for its entire UI) and instead builds on `TabView` +
`NavigationStack` — the same containers Apple's guidance names as
Duo-adaptive. Once building against an SDK that ships `.axisBehavior(_:)`,
adopting Duo-specific layout should only require adding that modifier to
`RootView`'s existing `TabView` — no structural rework.

## Requirements

- JDK 21
- Android SDK (`compileSdk`/`targetSdk` 37, `minSdk` 24) — see
  `local.properties` for the `sdk.dir` used locally
- Xcode 16+ (iOS 18+ deployment target, required for the `Tab`/
  `.sidebarAdaptable` APIs)
- Gradle 9.6+ (the wrapper handles this automatically)

## Building & running

### Android

```sh
./gradlew :androidApp:assembleDebug
# or, with a running emulator/device:
./gradlew :androidApp:installDebug
```

### iOS

The Xcode project's "Compile Kotlin Framework" build phase runs
`./gradlew :shared:embedAndSignAppleFrameworkForXcode` automatically, so no
manual Gradle step is needed first — just open and build/run from Xcode:

```sh
open iosApp/iosApp.xcodeproj
# or from the command line:
cd iosApp
xcodebuild -project iosApp.xcodeproj -scheme iosApp \
  -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build
```

### Shared module tests

```sh
./gradlew :shared:build
```

Runs `AppDestinationTest` on both the Android host-test target and the iOS
simulator test target.
