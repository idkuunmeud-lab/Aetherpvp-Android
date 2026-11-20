# AetherPvP Android — Phase 0 Fork Baseline

Date: 2026-11-20  
Repository: `idkuunmeud-lab/Aetherpvp-Android`  
Upstream: `LiteLDev/LeviLauncher-Android`  
Audited fork baseline commit: `d544df8cafab731ba7686dfb88951602f13293ef`

## Scope and status

This is a static source/configuration review of the newly created fork. No source files have been changed in this phase. No APK has been built, installed, or tested on a device. GitHub Actions currently reports zero workflow runs for the fork, so there is no CI build result to treat as a pass or failure.

The fork's `main` HEAD exactly matches the upstream `main` HEAD at the time of this check. The repository metadata identifies it as a public fork whose parent is `LiteLDev/LeviLauncher-Android`. Keep the original `AetherPvP-v3.1` repository separate.

## Verified project baseline

- Android app module plus `:minecraft` and `:pojav_controls` Gradle modules.
- Gradle wrapper is present, including `gradle-wrapper.jar`; wrapper distribution is Gradle 8.13.
- Version catalog pins Android Gradle Plugin 8.13.2 and Kotlin 2.3.0.
- App uses Java 21, compile SDK 36, target SDK 35, min SDK 28, NDK 28.2.13676358, and ARM64-v8a ABI filter.
- Native CMake build includes the Android Preloader, Microsoft libHttpClient Android build, and worldtools.
- The two Git submodules are pinned to exact gitlink commits:
  - Preloader Android: `cc08b40a31471d3e2dedb267f5f17b28da2b0604`
  - Microsoft libHttpClient: `f8ae1d9cf931d9d55542865eee395adca5bdbe41`
- README states Android 9/API 28+, ARM64, Minecraft Bedrock 1.21.80+, and a licensed Google Play Minecraft installation are required. Verify these requirements against current docs and the user's exact game build before device testing.

## Native module integration findings

The launcher already contains a native module pipeline; a separate host/loader architecture is not needed for the initial integration.

- `ModManager` discovers package directories with `manifest.json` and type `preload-native`.
- The manifest entry path is normalized and rejects absolute paths, empty components, `.`, and `..`; the entry must be a regular `.so` file.
- `ModNativeLoader` prepares a cache copy, loads the native library, and asks the Preloader to initialize it.
- The Java bridge already exposes external-module enumeration, enable/disable, config-schema, and overlay/button APIs.
- The pinned Preloader source exposes a C++ lifecycle registration interface in `pl::mod`, including load/enable/disable/unload callbacks and `PLGetModRegistration`; its CMake target requires the Android toolchain and C++20.
- This confirms a plausible integration route, not a verified working AetherPvP module. A minimal diagnostic module and on-device test remain mandatory before feature work.

## Build/CI blocker

The checked-in GitHub Actions workflow unconditionally downloads `discord_partner_sdk.aar` from a separately configured private GitHub repository using the `DISCORD_SDK_TOKEN` secret and `DISCORD_SDK_REPOSITORY` variable. A new fork does not inherit the upstream repository's private Actions secrets. The app Gradle file also declares that AAR as a dependency and imports its SDK class, so simply skipping the download step will not provide a valid compile.

The workflow can create a Firebase placeholder for compile checks when Firebase secrets are absent, but that does not solve the private Discord AAR dependency. Before a baseline build, choose a clean approach: provide authorized access to the required SDK artifact/secrets, or isolate/remove the optional Discord integration in a documented fork-specific way. Do not commit secrets or private SDK binaries.

## Phase 0 decision

**Do not begin branding or gameplay-feature implementation yet.** First make the fork buildable in a reproducible way, run the untouched upstream baseline build, inspect the exact Preloader headers/template against the pinned submodule commit, then build and test a harmless diagnostic module on the target device and exact supported Minecraft version.

## Next audit tasks

1. Resolve the private Discord SDK dependency without exposing credentials or proprietary binaries.
2. Verify submodule checkout and complete native build on a clean environment.
3. Trace the full module package/import/enable/disable/unload lifecycle, including failure handling and version gating.
4. Review permissions, storage isolation, native library loading, and external module trust boundaries.
5. Build a minimal lifecycle-log module with one harmless Mod Menu setting; test it before branding or feature development.
