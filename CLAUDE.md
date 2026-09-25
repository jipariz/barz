# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

**Barz** — a published Compose Multiplatform SDK providing adaptive navigation chrome (bottom bar /
rail / drawer), with native `UITabBarController` chrome on iOS, plus a sample app.

```
:barz                 the published SDK. android, jvm, js, wasmJs, iosArm64, iosSimulatorArm64
:sample:shared        the demo — a Pokédex on PokéAPI, three tabs, all screens
:sample:androidApp    ) thin shells; each is ~one file that calls DemoApp()
:sample:desktopApp    )
:sample:webApp        )
sample/iosApp/        Xcode project; roots itself in barzTabBarController, not DemoApp()
```

Coordinates: `dev.parez.barz:barz`. Root project is `barz-sdk`, **not** `barz` — a root and a
module with the same name collide in type-safe project accessors.

## Commands

```sh
./gradlew :barz:allTests                    # jvm + androidHostTest + iosSimulatorArm64
./gradlew :barz:publishToMavenLocal         # verifies the whole publishing setup
./gradlew :barz:checkKotlinAbi              # fails if the public API changed
./gradlew :barz:updateKotlinAbi             # accept an intentional API change
./gradlew :sample:androidApp:installDebug
./gradlew :sample:desktopApp:run            # drag the window to see bar -> rail -> drawer
./gradlew :sample:webApp:wasmJsBrowserDevelopmentRun
```

Single test: `./gradlew :barz:jvmTest --tests "*NavigationModeResolverTest.landscape*"`

`./gradlew build` covers all of the above except formatting — KGP wires `check` to
`checkKotlinAbi`, so CI needs only `build ktfmtCheck`.

The ABI dump does **not** cover the Android target: KGP collects Android dumps from a
`KotlinAndroidTarget`, and AGP's `com.android.kotlin.multiplatform.library` gives a
`KotlinMultiplatformAndroidLibraryTarget`, which it skips. Hence `barz/api/jvm/` and no
`barz/api/android/`. There is no androidMain-only public API today; if you add some, it ships
unguarded.

iOS: `open sample/iosApp/iosApp.xcodeproj` — the "Compile Kotlin Framework" phase runs
`:sample:shared:embedAndSignAppleFrameworkForXcode`, so no manual Gradle step.

After a Kotlin or CMP bump the web toolchain's lockfiles go stale and *every* task fails with
"Lock file was changed": run `./gradlew kotlinUpgradeYarnLock kotlinWasmUpgradeYarnLock`.

## Architecture

**The SDK is deliberately thin.** Its whole substance is `rememberNavigationMode` in
`NavigationModeResolver.kt`, which maps window size to a `NavigationMode`. Everything else wraps it:
`AdaptiveNavigationScaffold` feeds it to `NavigationSuiteScaffold`, `AdaptiveNavigationBar` ignores
it and always draws a bottom bar.

`resolveNavigationMode` is a **pure function**, separate from the composable, so all its tests run
without a Compose runtime. Keep that split — it is why the test suite is cheap.

**Two non-obvious breakpoint rules, both load-bearing:**
- Drawer waits for 1200dp, not Material's 840dp "expanded". A permanent drawer takes ~40% of an
  unfolded foldable's width and starves multi-pane content.
- A minimum *height* is required for rail/drawer. A landscape phone is ~891dp wide but ~411dp tall;
  width alone would wrongly promote it.

**Barz builds the three navigation components itself**, delegating only *placement* to
`NavigationSuiteScaffoldLayout`. It used to hand items to `NavigationSuiteScaffold`, which drops two
things Material's own components offer: `NavigationRail`'s `header` slot is hardcoded to null, and
drawer items never receive `NavigationDrawerItemDefaults.ItemPadding`, so they sit flush against the
sheet. Both were visible defects. If you go back to the wrapper, both regress.

**Barz has no FAB, deliberately.** A floating action button is an Android idiom and the iOS
equivalents (an extra `UITabBarItem` intercepted in `shouldSelectViewController:`, or
`UITabBarController.bottomAccessory`) belong to the app, not to a navigation library. The slot and
`IosFabItem` existed briefly and were removed in `391b232`.

**Icons have two paths.** `NavigationItem.icon` (a `DrawableResource`) or an `icon` composable slot
on the container. Exactly one must be supplied; `NavigationItemIcon` throws a named error otherwise,
deliberately, because a missing icon would otherwise be an invisible tap target. The sample uses the
slot — see the AGP note below.

**iOS native chrome is Kotlin, not Swift.** `BarzTabBarController.kt` (iosMain) builds a real
`UITabBarController` through Kotlin/Native's UIKit bindings, so the SDK stays a single Gradle
dependency — SwiftUI could not have been shipped inside a Kotlin framework, UIKit can. That is what
makes Liquid Glass, `UITabBarControllerModeTabSidebar` and Duo-readiness real rather than imitated.
`IosChrome.ComposeGlass` is the opt-out for teams who want pure Compose.

Two Kotlin/Native interop gotchas in that file: ObjC *category* members (`tabBarItem`) are
extensions needing an explicit import (`platform.UIKit.setTabBarItem`), and NS_ENUM constants are
top-level (`UITabBarControllerModeTabSidebar`), not nested under the enum type. Also, UIKit holds
`delegate` weakly, so the delegate is a field of the controller subclass — drop that and tab
selection silently stops firing.

`liquidGlass` is *not* a true system opt-out — the only real switch is the app-level
`UIDesignRequiresCompatibility` Info.plist key, which no library can set.

## Gotchas

**Dependency versions are pinned for target-availability reasons, not taste.** Compose Multiplatform
artifacts periodically drop target variants. Before bumping anything, check the target list in
`~/.gradle/caches/modules-2/metadata-2.107/descriptors/<group>/<artifact>/<version>/*/descriptor.bin`
— it is more reliable than the downloaded filenames in `files-2.1`.

**Barz is consumable from the Kotlin Toolchain (ex-Amper) as published, with no extra artifact.**
Verified against CLI 0.12.2: a `module.yaml` with `dependencies: [dev.parez.barz:barz:…]` compiles
on jvm, android, iosArm64, iosSimulatorArm64, js and wasmJs. Its resolver reads Gradle Module
Metadata, so the root coordinate fans out to the per-platform variants the way Gradle does — there
is nothing Gradle-specific to publish alongside. The toolchain is still Alpha and cannot host
`:barz` itself (no ABI-validation equivalent to `abiValidation {}`, which is a Kotlin *Gradle*
Plugin feature), but that is a separate question from consuming it.

**`iosX64` is intentionally absent.** Intel simulators are end-of-life and several artifacts no
longer publish a `uikitX64` variant.

**Web targets are compile-verified, not tested.** Karma needs a local Chrome, and Node cannot host
them because Compose's web runtime loads Skiko's `.wasm` over XHR. Both browser test tasks are
explicitly disabled. The logic under test is `commonMain` and covered by the other three runs.

**AGP's `com.android.kotlin.multiplatform.library` does not merge a KMP library's Compose resources
into a consuming app's assets.** The sample crashed with `MissingResourceException` and zero
`composeResources` entries in the APK; that is why it uses the icon slot rather than `Res.drawable`.
The SDK's `painterResource` path itself is correct.

**The SDK reads `LocalWindowInfo.containerSize` directly** rather than going through
`currentWindowAdaptiveInfo`. That is what makes configurable breakpoints possible — the window size
class quantises to fixed buckets, so a custom `railFromWidthDp` could not be honoured through it.

**Signing must use in-memory keys.** `org.gradle.configuration-cache=true` is on and
`signing.useGpgCmd()` is not compatible with it. Publishing is gated on `signingInMemoryKey` being
present, so local builds work without credentials.

**The demo is a port, not original.** It came from the `sidekick` repo's Pokédex, with the debug
overlay, its Gradle plugin, the KSP preferences store and the Room cache all stripped. The cache is
now in-memory inside `PokemonRepository`; Team and Settings are new, written against the Figma
design (`lLE628Ir8musp8JD101dxo`). The design has two tabs and a settings *sheet*; the demo has
three tabs, deliberately — Barz is what is being shown off, so it gets a third destination.

**Docs screenshots are captured by hand, from real devices.** `docs/images/*.png` are window
captures (bezel included) of the Simulator, the Android emulator, the desktop window and a browser
— `screencapture -x -o -l <windowId>`, with the window id resolved from
`CGWindowListCopyWindowInfo`. Three traps worth knowing before you regenerate them: the demo's
`ThemeMode.SYSTEM` follows the host, so force `ThemeMode.LIGHT` for the run; the iPhone Duo exposes
its two displays as two same-titled windows, of which only the lit one is the current posture; and
the Android emulator window renders the *inner* display even when folded, so the cover-display shot
is `adb shell screencap` composited into the folded bezel.

**The demo frosts its own screens.** `DemoApp` wraps `TabContent` in
`Modifier.hazeBlur(input = HazeInput.Content, …)`, which blurs that subtree only — the chrome is
drawn by the scaffold *outside* `content`, so it stays sharp and becomes the only thing in focus. Haze (`dev.chrisbanes.haze`) is a **sample** dependency; `:barz` does not depend on it and
should not. It makes the demo deliberately unreadable, so Settings has a **Blur Content** switch
that turns it off — reach for that rather than editing the modifier.

**The demo's screens inset themselves.** Both hosts draw edge to edge, so each screen takes
`WindowInsets.safeDrawing` as `contentPadding` rather than having the container clip it. This is not
cosmetic on iPhone Duo: the native tab bar moves to the *side* strip there, which arrives as a
horizontal inset. A screen that only honours the vertical ones renders under the bar.
