# Phase 2 — Remove the private Discord SDK dependency

## Decision

AetherPvP does not need Discord Rich Presence. It does not contribute to Minecraft launch, native module loading, Mod Menu functionality, overlays, performance, or PvP features. The fork will not require the upstream private Discord Social SDK repository or its Actions token.

## Changes made

- Removed `implementation files('libs/discord_partner_sdk.aar')` from the app dependencies.
- Removed the Discord application ID and large-image BuildConfig fields and the now-unused `discord.properties` file.
- Removed the GitHub Actions step that downloaded and checksum-verified `discord_partner_sdk.aar`.
- Replaced the Discord presence implementation with a small no-op compatibility shim. Existing lifecycle/settings call sites remain source-compatible, but no Discord SDK class is loaded and no Rich Presence service is started.
- Deleted the unused `DiscordPresenceNative` JNI declaration class.

## Preserved

- LeviLauncher app and Minecraft modules.
- Existing native Preloader/mod loading pipeline.
- Mod Menu, configuration, overlay and button APIs.
- Firebase and other unrelated launcher dependencies.
- The existing Discord community link is separate from the private SDK and was not changed.

## Static verification

- Re-fetched the edited Gradle file and confirmed there is no remaining SDK AAR dependency or Discord BuildConfig field.
- Re-fetched the workflow and confirmed the private SDK download step and secret/variable references are gone.
- Confirmed the settings screen's legacy Rich Presence toggle is disabled because the compatibility shim reports the feature as unconfigured.
- No SDK binary, token, or secret was added to the repository.

## Limitations / next gate

This is a source-level change only. No Gradle build or GitHub Actions run has been executed yet, so compilation is not verified. Next, run the fork's Android CI on this branch and fix any newly exposed compile/configuration issues before attempting the untouched baseline APK build. Do not treat this report as proof that an APK builds or runs.
