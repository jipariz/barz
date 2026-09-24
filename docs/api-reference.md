# API reference

[← back to the README](../README.md) · everything in package `dev.parez.barz`

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
    fab: (@Composable () -> Unit)? = null,
    fabPlacement: FabPlacement = FabPlacement.Top,
    content: @Composable () -> Unit,
)
```

Chrome that changes shape with the window: bottom bar → rail → permanent drawer.

- `header` is ignored in bottom-bar mode.
- `fabPlacement` is ignored in bottom-bar mode; the FAB always floats bottom-end there.
- Only `config.breakpoints` and `config.allowedModes` are consulted. **`config.ios` has no effect
  here** — this is the Compose container on every platform, iOS included.
- There is **no `colors` parameter**; `AdaptiveNavigationBarColors` applies to
  `AdaptiveNavigationBar` only. Restyle via `MaterialTheme`.
- In **drawer** mode Material's `NavigationDrawerItem` shape means `showLabel` and `enabled` are
  not applied; labels always show and items are always enabled. Both are honoured in bar and rail.

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
    iosFab: IosFabItem? = null,
    onIosFabClick: () -> Unit = {},
    fabIcon: (@Composable () -> Unit)? = null,
    colors: AdaptiveNavigationBarColors = AdaptiveNavigationBarDefaults.colors(),
)
```

Always a bottom bar. Three mutually exclusive render paths, chosen in order:

1. **native `UITabBar`** — iOS with `config.ios.chrome == NativeTabBar` (the default). Returns
   early, so `iosFab`, `fabIcon` and `icon` are all unused on this path.
2. **Compose glass** — iOS with `ComposeGlass` and `liquidGlass = true`. Rounded translucent
   surface at 72% alpha. The only path that renders `iosFab`.
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

### `barzTabBarController` — iOS only, not a composable

```kotlin
fun barzTabBarController(
    items: List<NavigationItem>,
    options: IosOptions = IosOptions(),
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

Exactly one of `icon` or the container's `icon` slot must be supplied; neither throws a named
error. A text `badge` wins over `showBadgeDot`. `contentDescription` falls back to `title`.

### `AdaptiveNavigationConfig`

```kotlin
@Immutable
data class AdaptiveNavigationConfig(
    val allowedModes: Set<NavigationMode> = NavigationMode.entries.toSet(),
    val breakpoints: NavigationBreakpoints = NavigationBreakpoints(),
    val ios: IosOptions = IosOptions(),
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

### `IosOptions`

```kotlin
@Immutable
data class IosOptions(
    val chrome: IosChrome = IosChrome.NativeTabBar,
    val liquidGlass: Boolean = true,
    val sidebarAdaptable: Boolean = true,
    val nativeBarHeight: Dp = 56.dp,
)
```

`sidebarAdaptable` only affects `barzTabBarController`. `nativeBarHeight` exists because a UIKit
view cannot report its size back through Compose interop.

### `IosFabItem`

```kotlin
@Immutable
data class IosFabItem(
    val systemIcon: String,
    val icon: DrawableResource? = null,
    val containerColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
    val title: String = "",
    val showLabel: Boolean = false,
    val contentDescription: String? = null,
)
```

Renders only with `IosChrome.ComposeGlass` on iOS. `systemIcon`, `title` and `showLabel` are
currently unread on that path.

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
| `FabPlacement` | `Top` (Material's convention, default), `Bottom` |
| `IosChrome` | `NativeTabBar` (default), `ComposeGlass` |
| `BarzPlatform` | `Android`, `Desktop`, `Web`, `Ios` |

### `currentPlatform`

```kotlin
expect val currentPlatform: BarzPlatform
```

A plain top-level `val`, not a `CompositionLocal` — not overridable at runtime or in tests.
