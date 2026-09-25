# iOS guide

[← back to the README](../README.md)

## One Gradle dependency. No Swift package, no SPM.

`NavBarzTabBarController.kt` builds a real `UITabBarController` through Kotlin/Native's UIKit
bindings, so the native chrome ships **inside your Kotlin framework**. SwiftUI could not have been
distributed that way; UIKit is fully bound by Kotlin/Native, so it can.

That choice is what makes Liquid Glass, the iPad sidebar and the iPhone Duo side strip real rather
than imitated: the system grants those to genuine containers and to nothing else.

## Three tiers of iOS chrome

Pick deliberately — they are not interchangeable.

| | Liquid Glass | Repositions on iPad / Duo |
|---|---|---|
| `IosChrome.ComposeGlass` | imitated in Compose | no |
| `IosChrome.NativeTabBar` (default) | real — an embedded `UITabBar` | no |
| `navBarzTabBarController` | real | **yes** |

The first two are properties of `AdaptiveNavigationBar`. The third is a different shape of API
entirely: it replaces your root view controller, which is why it is not an `IosChrome` value.

## Rooting in the native controller

```kotlin
// shared/src/iosMain/kotlin/…/MainViewController.kt
import dev.parez.navbarz.IosOptions
import dev.parez.navbarz.navBarzTabBarController
import platform.UIKit.UIViewController

fun rootViewController(): UIViewController =
    navBarzTabBarController(
        items = navItems,
        options = IosOptions(sidebarAdaptable = true, liquidGlass = true),
        onSelect = { index -> println("selected $index") },
        content = { index -> AppTab(index) },
    )
```

Then hand it to SwiftUI:

```swift
import SwiftUI
import ComposeApp

struct NavBarzRoot: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.rootViewController()
    }
    func updateUIViewController(_ vc: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View { NavBarzRoot().ignoresSafeArea() }
}
```

**Each tab is its own `ComposeUIViewController`, so each runs as a separate composition.** Anything
shared across tabs — selected state, a cart, a settings store — has to live outside composition
(a DI singleton, a `StateFlow`). Anything you `remember` in one tab is invisible to the others.

## Item mapping

`NavigationItem.systemIcon` is an SF Symbol name, `selectedSystemIcon` its filled variant, `badge`
becomes `UITabBarItem.badgeValue`, `showBadgeDot` an empty badge, and `showLabel = false` drops the
title. `contentDescription` falls back to `title`.

The native path resolves icons through `UIImage.systemImageNamed` only — there is no asset-catalog
fallback there, so a name that is not an SF Symbol renders nothing.

## Safe areas, and why the Duo needs you to care

The native bar is outside your Compose content, so it arrives as a window inset. On iPhone that is
a bottom inset and ignoring it merely looks sloppy. **On iPhone Duo the bar moves to the side
strip, which arrives as a horizontal inset** — a screen that only honours the vertical ones renders
underneath it.

Take `WindowInsets.safeDrawing` as `contentPadding` on your scrolling content rather than letting
the container clip it:

```kotlin
val insets = WindowInsets.safeDrawing.asPaddingValues()
LazyVerticalGrid(contentPadding = insets, …) { … }
```

## Liquid Glass, honestly

`IosOptions.liquidGlass` is **not a true system opt-out.** The only real switch is the app-level
`UIDesignRequiresCompatibility` key in `Info.plist`, which no library can set on your behalf. What
the flag does:

- `true` — leave the bar on the system material, whatever the OS decides that is.
- `false` — install an explicitly opaque `UITabBarAppearance` on both `standardAppearance` and
  `scrollEdgeAppearance`.

So `false` gets you an opaque bar; it does not turn the design language off.

## Xcode wiring

The sample's Xcode project has a **Compile Kotlin Framework** build phase that runs

```sh
cd "$SRCROOT/../.."
./gradlew :sample:shared:embedAndSignAppleFrameworkForXcode
```

so there is no manual Gradle step before building in Xcode. Copy that phase into your own project
and point it at your shared module.

## Known limitations

- **NavBarz has no FAB.** A floating action button is an Android idiom, and iOS has better native
  answers that belong to your app rather than to a navigation library: add an extra `UITabBarItem`
  and intercept it in `tabBarController:shouldSelectViewController:` to run an action instead of
  switching tabs, or use `UITabBarController.bottomAccessory` (iOS 26) for a Now-Playing-style
  strip above the bar. Both are reachable from Kotlin/Native.
- `iosX64` is not published. Intel simulators are end-of-life and several Compose Multiplatform
  artifacts no longer ship a `uikitX64` variant.
