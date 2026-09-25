# API reference

[← back to the README](../README.md) · everything in package `dev.parez.navbarz`

## Composables

### `AdaptiveNavigationScaffold`

```kotlin
@Composable
fun AdaptiveNavigationScaffold(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    config: AdaptiveNavigationConfig = AdaptiveNavigationConfig(),
    icon: (@Composable (index: Int, selected: Boolean) -> Unit)? = null,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable () -> Unit,
)
```

Chrome that changes shape with the window: bottom bar → rail → permanent drawer.

- `header` is ignored in bottom-bar mode.
- Only `config.breakpoints` and `config.allowedModes` are consulted. **`config.ios` has no effect
  here** — this is the Compose container on every platform, iOS included.
- There is **no `colors` parameter**; `AdaptiveNavigationBarColors` applies to
  `AdaptiveNavigationBar` only. Restyle via `MaterialTheme`.
- The rail and the drawer scroll vertically, so more destinations than fit the window stay
  reachable. A bottom bar does not — Material caps it at five.
- `NavigationDrawerItem` has no `enabled` parameter, so in **drawer** mode `enabled = false` is
  approximated: the item is inert but not visually dimmed. Bar and rail honour it properly.
  `showLabel` and badges are honoured in all three, but the drawer puts the badge in the row's own
  end slot rather than over the icon.

### `AdaptiveNavigationBar`

```kotlin
@Composable
fun AdaptiveNavigationBar(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    config: AdaptiveNavigationConfig = AdaptiveNavigationConfig(),
    icon: (@Composable (index: Int, selected: Boolean) -> Unit)? = null,
    colors: AdaptiveNavigationBarColors = AdaptiveNavigationBarDefaults.colors(),
)
```

Always a bottom bar. Three mutually exclusive render paths, chosen in order:

1. **native `UITabBar`** — iOS with `config.ios.chrome == NativeTabBar` (the default). Returns
   early, so the `icon` slot is unused on this path.
2. **Compose glass** — iOS with `ComposeGlass` and `liquidGlass = true`. Rounded translucent
   surface at 72% alpha.
3. **plain `NavigationBar`** — everything else, including every non-iOS platform.

### `AdaptiveNavigationBar` (ImageVector overload)

```kotlin
@Composable
fun AdaptiveNavigationBar(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    icon: (index: Int, selected: Boolean) -> ImageVector,     // NOT @Composable
    modifier: Modifier = Modifier,
    colors: AdaptiveNavigationBarColors = AdaptiveNavigationBarDefaults.colors(),
)
```

`NavigationItem.icon` is ignored in favour of `icon`. No `config` parameter, so this is always a
plain Compose bar — it can never take the native or glass path, even on iOS.

### `rememberNavigationMode`

```kotlin
@Composable
fun rememberNavigationMode(config: AdaptiveNavigationConfig = AdaptiveNavigationConfig()): NavigationMode
```

Reads `LocalWindowInfo.containerSize` directly rather than `currentWindowAdaptiveInfo()`, because
the window size class quantises to fixed buckets that would defeat custom breakpoints.

`containerSize` changes every frame of a window drag; the result is wrapped in `derivedStateOf`, so
callers recompose only at the two thresholds where the mode actually changes.

### `navBarzTabBarController` — iOS only, not a composable

```kotlin
fun navBarzTabBarController(
    items: List<NavigationItem>,
    options: IosControllerOptions = IosControllerOptions(),
    onSelect: (Int) -> Unit = {},
    content: @Composable (index: Int) -> Unit,
): UIViewController
```

See the [iOS guide](ios.md).

## Types

### `NavigationItem`

```kotlin
@Immutable
data class NavigationItem(
    val title: String,
    val icon: DrawableResource? = null,
    val selectedIcon: DrawableResource? = null,
    val systemIcon: String,                    // required — and it sits after two defaulted params
    val selectedSystemIcon: String? = null,
    val showLabel: Boolean = true,
    val badge: String? = null,
    val showBadgeDot: Boolean = false,
    val enabled: Boolean = true,
    val contentDescription: String? = null,
)
```

Exactly one of `icon` or the container's `icon` slot must be supplied — supplying neither throws a
named error rather than rendering an invisible tap target. A text `badge` wins over `showBadgeDot`.

`contentDescription` is only applied when `showLabel = false`. The Material item composables merge
descendant semantics and already announce the title, so describing the icon as well makes a screen
reader read every destination twice.

### `AdaptiveNavigationConfig`

```kotlin
@Immutable
data class AdaptiveNavigationConfig(
    val allowedModes: Set<NavigationMode> = NavigationMode.entries.toSet(),
    val breakpoints: NavigationBreakpoints = NavigationBreakpoints(),
    val ios: IosBarOptions = IosBarOptions(),
)
```

An empty `allowedModes` throws at construction. The resolver itself clamps rather than failing.

### `NavigationBreakpoints`

```kotlin
@Immutable
data class NavigationBreakpoints(
    val railFromWidthDp: Int = 600,
    val drawerFromWidthDp: Int = 1200,
    val minHeightDp: Int = 480,
)
```

`railFromWidthDp > drawerFromWidthDp` throws at construction: the resolver tests the drawer first,
so a swapped pair would make `Rail` unreachable rather than fail.

### `IosBarOptions` / `IosControllerOptions`

The two iOS entry points share exactly one setting, so they take separate option types rather than
one fused type in which each API carried knobs that silently did nothing.

```kotlin
// commonMain — for AdaptiveNavigationBar, via AdaptiveNavigationConfig.ios
@Immutable
data class IosBarOptions(
    val chrome: IosChrome = IosChrome.NativeTabBar,
    val liquidGlass: Boolean = true,
    val nativeBarHeight: Dp = 56.dp,
)

// iOS source set only — for navBarzTabBarController
@Immutable
data class IosControllerOptions(
    val liquidGlass: Boolean = true,
    val sidebarAdaptable: Boolean = true,
)
```

`nativeBarHeight` exists because a UIKit view cannot report its size back through Compose interop;
it is meaningless to a real `UITabBarController`, which the system lays out itself.
`IosControllerOptions` lives in the iOS source set so a `js` or `jvm` consumer never sees a type it
cannot use.

### `AdaptiveNavigationBarColors` / `AdaptiveNavigationBarDefaults`

```kotlin
@Immutable
data class AdaptiveNavigationBarColors(
    val containerColor: Color,
    val indicatorColor: Color,
    val selectedIconColor: Color,
    val selectedTextColor: Color,
    val unselectedIconColor: Color,
    val unselectedTextColor: Color,
    val badgeContainerColor: Color,
    val badgeContentColor: Color,
)

object AdaptiveNavigationBarDefaults {
    @Composable fun colors(…): AdaptiveNavigationBarColors     // Material defaults
    fun config(default, android, desktop, web, ios): AdaptiveNavigationConfig
}
```

`config` is not a composable.

### Enums

| Type | Values |
|---|---|
| `NavigationMode` | `BottomBar`, `Rail`, `Drawer` |
| `IosChrome` | `NativeTabBar` (default), `ComposeGlass` |
| `NavBarzPlatform` | `Android`, `Desktop`, `Web`, `Ios` |

### `currentPlatform`

```kotlin
expect val currentPlatform: NavBarzPlatform
```

A plain top-level `val`, not a `CompositionLocal` — not overridable at runtime or in tests.
