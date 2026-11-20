# AetherPvP Android — Phase 1 Native Module Pipeline Review

Date: 2026-11-20  
Reviewed baseline: `d544df8cafab731ba7686dfb88951602f13293ef`  
Review type: static source review only; no APK or runtime test.

## Confirmed integration path

LeviLauncher already provides most of the Android-side plumbing AetherPvP needs:

1. The Mods manager discovers native packages in the selected Minecraft version's mods directory.
2. A package uses `manifest.json` with `type: preload-native`, an `entry` path to a `.so`, and optional `minecraft_versions`.
3. The native loader stages the package into its app-private cache, calls `System.load()`, then initializes it through the Preloader JNI bridge.
4. Preloader owns the native module lifecycle. The pinned SDK exposes `PLGetModRegistration` and C++ `pl::mod` lifecycle callbacks.
5. The existing External Mod Bridge exposes module enumeration, enable/disable, config schema, and overlay-button events to the Java UI.
6. ExternalModuleProvider adapts those native modules into the existing unified Mod Menu UI.

**Decision:** build AetherPvP as a small set of Preloader native modules that use this existing bridge and menu. Do not build a parallel loader or a replacement mod menu.

## Existing built-in features worth reusing

The launcher already has built-in native modules and overlay integrations, including FPS, Auto Sprint, Gyro, Hotbar Slot, Zoom, Snaplook, and More Buttons. These are useful examples of the launcher-to-native-to-overlay contract. They are not proof that arbitrary third-party modules or every Minecraft version works.

## Package import and path handling

The native package importer extracts ZIP entries into a private staging directory and checks canonical destination paths before writing entries. This reduces the likelihood of ZIP-slip traversal in the normal archive import path.

The Java ModManager sanitizes manifest entry paths against absolute paths and dot/dot-dot components. The Preloader's manifest parser independently applies lexical relative-path checks. Neither check alone is a substitute for runtime compatibility validation. Before accepting third-party packages as trusted, add explicit tests for malformed paths, symlink behavior, invalid manifests, and failed native loads. The ZIP importer currently writes regular files/directories from ZipInputStream rather than intentionally creating filesystem symlinks.

## Important behavior to verify at runtime

- Confirm when `loadEnabledSoMods`, `nativeEnableLoadedMods`, and `nativeDisableAndUnloadLoadedMods` run relative to Minecraft process/activity startup and shutdown.
- Confirm failed `System.load` or Preloader initialization does not leave a misleading enabled state or a partially initialized module.
- Confirm repeated launches do not load duplicate native modules or leave stale native state.
- Confirm module config persistence and schema changes work through the External Mod Bridge.
- Confirm the overlay input/render callbacks are safe on the actual device and Minecraft version.
- Confirm the package's `minecraft_versions` gate is strict enough for native ABI/signature compatibility. An empty list currently means no version restriction; AetherPvP's diagnostic module should declare only the exact tested version.

## Build gate remains blocked

The upstream workflow requires a private Discord Social SDK AAR and private repository access via Actions secrets. The fork has no Actions runs yet, and no build result exists. Do not remove or stub proprietary Discord integration just to force a green build. Resolve authorized SDK access or agree on a documented optional-Discord fork configuration before attempting a clean baseline build.

## Phase 1 outcome

The launcher has a credible existing extension architecture for AetherPvP. The next implementation artifact should be a minimal diagnostic module with one harmless Mod Menu toggle and lifecycle logging, but it must not be presented as tested until the launcher and module are built and exercised on the target tablet with the exact installed Minecraft build.
