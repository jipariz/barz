# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

**Barz** — a published Compose Multiplatform SDK providing adaptive navigation chrome (bottom bar /
rail / drawer), plus a Swift companion package for native iOS chrome, plus a sample app.

```
:barz                 the published SDK. android, jvm, js, wasmJs, iosArm64, iosSimulatorArm64
swift/Barz/           Swift Package — SwiftUI TabView for iOS
:sample:shared        demo content (shapes UI, list-detail screens)
:sample:androidApp    demo app, a consumer of :barz
iosApp/               demo Xcode project
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
```

Single test: `./gradlew :barz:jvmTest --tests "*NavigationModeResolverTest.landscape*"`

iOS: `open iosApp/iosApp.xcodeproj` — the "Compile Kotlin Framework" phase runs
`:sample:shared:embedAndSignAppleFrameworkForXcode`, so no manual Gradle step.

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

**Icons have two paths.** `NavigationItem.icon` (a `DrawableResource`) or an `icon` composable slot
on the container. Exactly one must be supplied; `NavigationItemIcon` throws a named error otherwise,
deliberately, because a missing icon would otherwise be an invisible tap target. The sample uses the
slot — see the AGP note below.

**iOS has two chrome options** via `IosOptions.chrome`: `NativeTabView` (the Swift package, real
Liquid Glass and Duo-readiness) or `ComposeGlass` (Compose-drawn, imitation). `liquidGlass` is *not*
a true system opt-out — the only real switch is the app-level `UIDesignRequiresCompatibility`
Info.plist key, which no library can set.

## Gotchas

**Dependency versions are pinned for target-availability reasons, not taste.** Compose Multiplatform
artifacts periodically drop target variants. `adaptive` stays on stable 1.2.0 — the 1.3.0 line is
beta-only on the JetBrains side, and an SDK should not ship a beta transitive. Before bumping
anything, check the target list in
`~/.gradle/caches/modules-2/metadata-2.107/descriptors/<group>/<artifact>/<version>/*/descriptor.bin`
— it is more reliable than the downloaded filenames in `files-2.1`.

**`iosX64` is intentionally absent.** Intel simulators are end-of-life and several artifacts no
longer publish a `uikitX64` variant.

**Web targets are compile-verified, not tested.** Karma needs a local Chrome, and Node cannot host
them because Compose's web runtime loads Skiko's `.wasm` over XHR. Both browser test tasks are
explicitly disabled. The logic under test is `commonMain` and covered by the other three runs.

**AGP's `com.android.kotlin.multiplatform.library` does not merge a KMP library's Compose resources
into a consuming app's assets.** The sample crashed with `MissingResourceException` and zero
`composeResources` entries in the APK; that is why it uses the icon slot rather than `Res.drawable`.
The SDK's `painterResource` path itself is correct.

**`currentWindowAdaptiveInfoV2` does not exist in adaptive 1.2.0** (it arrived in 1.3.0). The SDK
reads `LocalWindowInfo.containerSize` directly instead — which is also what makes configurable
breakpoints possible, since the window size class quantises to fixed buckets.

**Signing must use in-memory keys.** `org.gradle.configuration-cache=true` is on and
`signing.useGpgCmd()` is not compatible with it. Publishing is gated on `signingInMemoryKey` being
present, so local builds work without credentials.
