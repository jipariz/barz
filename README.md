# Barz

Adaptive navigation chrome for Compose Multiplatform. One list of destinations; a bottom bar, a
navigation rail or a permanent drawer depending on the window — and a genuinely native `TabView` on
iOS rather than an imitation of one.

```kotlin
implementation("dev.parez.barz:barz:0.1.0")
```

| Target | Chrome |
|---|---|
| Android | Material 3 bar / rail / drawer |
| Desktop (JVM) | same, tracking the window as you resize it |
| Web (Wasm, JS) | same, tracking the browser viewport |
| iOS | SwiftUI `TabView` via the companion Swift package, or a Compose-drawn bar |

## Use it

```kotlin
val items = listOf(
    NavigationItem(title = "Home",      icon = Res.drawable.ic_home,     systemIcon = "house"),
    NavigationItem(title = "Favorites", icon = Res.drawable.ic_favorite, systemIcon = "heart", badge = "3"),
    NavigationItem(title = "Profile",   icon = Res.drawable.ic_person,   systemIcon = "person.crop.circle"),
)

var selected by rememberSaveable { mutableIntStateOf(0) }

AdaptiveNavigationScaffold(
    items = items,
    selectedIndex = selected,
    onItemSelected = { selected = it },
) {
    CurrentScreen(selected)
}
```

Want just a bottom bar, in your own `Scaffold`? Use `AdaptiveNavigationBar` instead — same
parameters, no adaptation. Want the decision without the container? `rememberNavigationMode(config)`
returns the `NavigationMode` and leaves the rendering to you.

If your icons are `ImageVector`s rather than Compose resources, `AdaptiveNavigationBar` has an
overload taking `icon: (index, selected) -> ImageVector`.

## Configure it

```kotlin
val config = AdaptiveNavigationBarDefaults.config(
    // Phones stay on a bottom bar no matter how the window is resized.
    android = AdaptiveNavigationConfig(allowedModes = setOf(NavigationMode.BottomBar)),
    // Desktop goes straight to a drawer.
    desktop = AdaptiveNavigationConfig(allowedModes = setOf(NavigationMode.Drawer)),
    // Everywhere else: default behaviour, but promote to a rail sooner.
    default = AdaptiveNavigationConfig(
        breakpoints = NavigationBreakpoints(railFromWidthDp = 520),
    ),
)
```

Only the override matching the running platform is consulted, so this is safe to write in
`commonMain` with no `expect`/`actual` of your own. `allowedModes` clamps rather than throws — a
config that permits only a drawer still renders a drawer on a phone.

### Why the drawer waits until 1200dp

The Material default promotes to a drawer at the 840dp "expanded" breakpoint. Barz waits for 1200dp,
because a permanent drawer takes roughly 40% of an unfolded foldable's width. That is affordable
when your content is one pane and ruinous when it has its own multi-pane layout. Move it with
`NavigationBreakpoints(drawerFromWidthDp = 840)` if you disagree.

There is also a height guard: a phone in landscape is ~891dp wide but only ~411dp tall, and a rail
or drawer there wastes the height that is left. Below `minHeightDp` you always get a bottom bar.

## iOS

Two options, chosen with `IosOptions.chrome`:

**`IosChrome.NativeTabView`** (default) — the companion Swift package renders a real SwiftUI
`TabView`. Liquid Glass, sidebar promotion on iPad and the placement iPhone Duo introduces are
genuine system behaviour, because they come from a real system container. Add it alongside the
Gradle dependency:

```swift
.package(url: "https://github.com/jipariz/barz", from: "0.1.0")
```

```swift
BarzTabView(items: items, selection: $selected) { item in
    NavigationStack { ComposeScreen(id: item.id) }
}
```

**`IosChrome.ComposeGlass`** — a bar drawn in Compose. One dependency, no Swift, but it only
*imitates* the system material and gets none of the automatic behaviours above.

> **On `liquidGlass`.** This flag is not a true system opt-out. The only real switch is the
> app-level `UIDesignRequiresCompatibility` key in `Info.plist`, which no library can set on your
> behalf. The flag chooses between the system material and an explicitly opaque bar background.

## Platform notes

Foldable posture (`isTabletop`) is reported on Android only — every other Compose Multiplatform
target returns a default `Posture()`. Window *size* is live everywhere, including desktop window
drags and browser resizes.

`iosX64` (the Intel-Mac simulator) is not published. Apple has wound Intel Macs down and several
Compose Multiplatform artifacts have already stopped shipping that variant.

## Building this repo

```sh
./gradlew :barz:allTests              # jvm, android host, iOS simulator
./gradlew :barz:publishToMavenLocal
./gradlew :sample:androidApp:installDebug
```

Web targets are compile-verified rather than tested: Karma needs a local Chrome, and Node cannot
host them either because Compose's web runtime loads Skiko's `.wasm` over XHR. The logic under test
is `commonMain` and is covered by the three runs above.

## Licence

Apache 2.0. See [LICENSE](LICENSE).
