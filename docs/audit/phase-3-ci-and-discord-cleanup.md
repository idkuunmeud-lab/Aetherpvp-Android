# Phase 3: CI and Discord Cleanup Review

Date: 2026-11-20

## Scope

This phase continues the static audit after removal of the private Discord SDK. It checks the remaining Discord-related settings/automation and reviews the current Android CI build gates. This is a source review only; no Gradle build, APK installation, or device test was performed.

## Changes made

- Removed the obsolete Rich Presence preference from `FeatureSettings`.
- Removed the disabled Rich Presence switch and its listener from `SettingsActivity`.
- Removed the corresponding settings row from `view_settings_sections.xml`.
- Removed the release-time Discord webhook notification step from `.github/workflows/android.yml`.
- Kept the launcher’s community Discord link in the About section. It is a user-facing community link, not an SDK, background service, webhook, or build dependency.
- Kept the `DiscordPresence` no-op compatibility shim for any remaining lifecycle call sites. It does not load a Discord SDK or transmit presence.

## CI findings

The workflow checks out submodules recursively and installs JDK 21, Android NDK r28c, Xmake 2.9.9, and Build Tools 35.0.0.

Important limitations found:

1. When Firebase secrets are absent, CI creates a placeholder `google-services.json` but only runs `:app:compileDebugKotlin` and `:app:compileDebugJavaWithJavac`. Those tasks do not prove that the complete APK can be assembled or that the native CMake/Xmake pipeline works.
2. The workflow does not explicitly install Android SDK Platform 36, even though the app uses `compileSdkVersion 36`. It installs Build Tools 35.0.0. The SDK package setup should be made explicit before treating CI as a reliable full-build gate.
3. No successful or failed Actions run is available for this branch at the time of this review.

## CI hardening applied after review

The workflow was updated in a follow-up commit:

- Explicitly installs Android SDK Platform 36, Build Tools 36.0.0, and CMake 3.22.1.
- When Firebase secrets are absent, CI now runs `:app:assembleDebug` rather than stopping after Java/Kotlin compilation.
- Debug APK artifact upload is no longer restricted to the Firebase-secrets path.

This makes the no-secrets path attempt a complete debug APK build, including native build tasks. It is still unverified until GitHub Actions actually runs successfully.

## Required next validation

Before adding gameplay modules:

1. Make CI assemble a complete debug APK even when Firebase production secrets are absent, using the existing CI-only placeholder configuration.
2. Explicitly install the SDK platform/build-tools packages required by the app and verify CMake/Xmake native dependencies.
3. Run the workflow and inspect its logs; do not treat source inspection or Java/Kotlin compilation as a successful APK build.
4. Only after a clean baseline APK build, add the minimal native diagnostic module and test loading, Mod Menu registration, configuration, enable/disable, and unload on the target Android device with the exact installed Minecraft build.

## Current status

- Discord private SDK dependency: removed in earlier commits.
- Discord Rich Presence setting and release webhook: removed in this phase.
- Community Discord link: intentionally retained.
- Native module pipeline: reviewed in Phase 1, not yet validated by a built APK.
- AetherPvP gameplay modules: not implemented yet.
- APK build / device test: not performed.

