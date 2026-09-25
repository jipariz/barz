# Integration guide

[← back to the README](../README.md)

## Install

One dependency, on every platform. It goes in your **shared** KMP module, not in the per-platform
shells — the iOS chrome rides along inside the Kotlin framework.

It is on Maven Central, so `mavenCentral()` is the only repository you need.

```kotlin
// shared/build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("dev.parez.navbarz:navbarz:0.1.0")
        }
    }
}
```

With a version catalog:

```toml
# gradle/libs.versions.toml
[versions]
navbarz = "0.1.0"

[libraries]
navbarz = { module = "dev.parez.navbarz:navbarz", version.ref = "navbarz" }
```

There is no Swift package and no SPM step. See the [iOS guide](ios.md) for why.

### From the Kotlin Toolchain (ex-Amper)

NavBarz's Gradle publication carries Gradle Module Metadata, so the Kotlin Toolchain resolves the
root coordinate into the right per-platform variant exactly as Gradle does. Verified against CLI
0.12.2 on jvm, android, iosArm64, iosSimulatorArm64, js and wasmJs:

```yaml
# module.yaml
product:
  type: lib
  platforms: [jvm, android, iosArm64, iosSimulatorArm64, js, wasmJs]

dependencies:
  - dev.parez.navbarz:navbarz:0.1.0

settings:
  compose: enabled
```

## Which entry point

NavBarz has four, in decreasing order of how much it does for you.

| Use | When |
|---|---|
| [`AdaptiveNavigationScaffold`](#adaptivenavigationscaffold) | You want chrome that changes shape with the window. This is the one. |
| [`AdaptiveNavigationBar`](#adaptivenavigationbar) | You want a bottom bar and nothing else, inside your own `Scaffold`. |
| [`rememberNavigationMode`](#remembernavigationmode) | You want the decision but not the container. |
| [`navBarzTabBarController`](ios.md) | iOS, and you want the system to own the bar. |

### AdaptiveNavigationScaffold

```kotlin
AdaptiveNavigationScaffold(
    items = items,
    selectedIndex = selected,
    onItemSelected = { selected = it },
    modifier = Modifier,
    config = AdaptiveNavigationConfig(),
    icon = { index, isSelected -> Icon(iconFor(index, isSelected), null) },
    header = { Text("My app") },                  // rail + drawer only
) {
    CurrentScreen(selected)
}
```

It wraps `NavigationSuiteScaffoldLayout` for placement but builds the three Material components
itself, because the stock wrapper drops `NavigationRail`'s `header` slot and never applies
`NavigationDrawerItemDefaults.ItemPadding` to drawer items.

On iOS this renders the **Compose** container, not a `UITabBarController`.

### AdaptiveNavigationBar

Always a bottom bar. Drop it into a `Scaffold`:

```kotlin
Scaffold(
    bottomBar = {
        AdaptiveNavigationBar(
            items = items,
            selectedIndex = selected,
            onItemSelected = { selected = it },
            icon = { index, isSelected -> Icon(iconFor(index, isSelected), null) },
        )
    },
) { padding -> Content(padding) }
```

Unlike the scaffold this one takes `colors: AdaptiveNavigationBarColors`, and on iOS it can render
the platform's own bar — see the [iOS guide](ios.md).

There is a second overload for apps whose icons are `ImageVector`s, taking a **non-composable**
`icon: (index, selected) -> ImageVector`. It has no `config` parameter, so it is always a plain
Compose bar even on iOS.

### rememberNavigationMode

```kotlin
when (rememberNavigationMode()) {
    NavigationMode.BottomBar -> MyOwnBar()
    NavigationMode.Rail -> MyOwnRail()
    NavigationMode.Drawer -> MyOwnDrawer()
}
```

State-backed, so desktop window drags and browser resizes recompose live — but only at the two
thresholds. The window size behind it changes every frame of a drag; the mode is wrapped in
`derivedStateOf` so your content is not invalidated sixty times a second on the way past 600dp.

## Icons

Two paths, and **exactly one** must be supplied. An item with neither throws a named error rather
than rendering an invisible tap target.

```kotlin
// 1. Compose resources, on the item
NavigationItem(
    title = "Home",
    icon = Res.drawable.ic_home,
    selectedIcon = Res.drawable.ic_home_filled,   // optional; falls back to `icon`
    systemIcon = "house",
)

// 2. an icon slot on the container — use this for ImageVectors
AdaptiveNavigationScaffold(
    items = items,
    icon = { index, selected -> Icon(vectors[index], null) },
    …
)
```

> **AGP caveat.** `com.android.kotlin.multiplatform.library` does not merge a KMP library's Compose
> resources into a consuming app's assets — the app crashes with `MissingResourceException`. If you
> hit that, use the icon slot. The sample does.

## Configuration

```kotlin
AdaptiveNavigationConfig(
    allowedModes = NavigationMode.entries.toSet(),
    breakpoints = NavigationBreakpoints(
        railFromWidthDp = 600,
        drawerFromWidthDp = 1200,
        minHeightDp = 480,
    ),
    ios = IosBarOptions(),
)
```

### How a mode is chosen

```
height < minHeightDp        -> BottomBar     // a short window wins over a wide one
width  >= drawerFromWidthDp -> Drawer
width  >= railFromWidthDp   -> Rail
otherwise                   -> BottomBar
```

…then clamped into `allowedModes`: the widest allowed mode narrower than the one wanted, or the
narrowest allowed if there is none. It never throws — `allowedModes = setOf(Drawer)` really does
give a drawer on a 360×640 phone.

Two of the defaults are load-bearing:

- **The drawer waits for 1200dp**, not Material's 840dp. A permanent drawer takes roughly 40% of an
  unfolded foldable's width and starves the content that the extra width was for.
- **A minimum height is required** for rail and drawer. A landscape phone is ~891dp wide but only
  ~411dp tall; width alone would wrongly promote it.

### Per platform

```kotlin
val config = AdaptiveNavigationBarDefaults.config(
    default = AdaptiveNavigationConfig(),
    android = AdaptiveNavigationConfig(allowedModes = setOf(NavigationMode.BottomBar)),
    desktop = AdaptiveNavigationConfig(allowedModes = setOf(NavigationMode.Drawer)),
)
```

Only the override matching the running platform is consulted, so this is safe to call from
`commonMain` with no `expect`/`actual` of your own. It is not a composable.

## Header

`header` is a slot on `AdaptiveNavigationScaffold`. It renders above the destinations in the **rail
and drawer**; a bottom bar has nowhere to put it, so it is ignored there.

Supplying one also gives the rail its top spacing — Material reserves only 4dp above the first item
and expects a header to do the rest, so NavBarz stands in with 8dp when there is none.

NavBarz has no FAB slot, deliberately. A primary action is the app's business, and the idiom differs
too much per platform to wrap honestly: use `Scaffold`'s `floatingActionButton` on Android, Desktop
and Web, and on iOS either an extra tab item you intercept in the delegate or
`UITabBarController.bottomAccessory`.

## Platform notes

`currentPlatform` is a plain `val`, not a `CompositionLocal` — it cannot be faked in tests, which
matters if you try to unit-test the iOS-gated paths.
