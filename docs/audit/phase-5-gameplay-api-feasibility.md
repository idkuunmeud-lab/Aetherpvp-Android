# AetherPvP Phase 5 — Gameplay API Feasibility Audit

Date: 2026-11-21
Repository branch: `main`
User-reported Minecraft Bedrock version: `1.26.62.1`

## Purpose

Establish what the current LeviLauncher/Preloader integration can safely support before implementing substantial AetherPvP gameplay modules. This is a source-level audit, not a claim of in-game verification.

## Target build assessment

The checked-in Preloader signature rules include a first rule with minimum version `1.26.60.00` and no maximum version. The Preloader's version comparator parses numeric components, so the reported `1.26.62.1` satisfies that minimum and will select that rule.

Important limitation: this only means the rule is selected by version comparison. It does **not** prove that its signatures resolve correctly against the user's exact `1.26.62.1` `libminecraftpe.so` binary. That still requires runtime validation on the exact build. The currently bundled signatures cover pause-menu/HUD screen lifecycle and menu visibility, not player or inventory state.

## Confirmed integration points

- Native modules are loaded through the Preloader's `preload-native` manifest and C++ lifecycle registration (`PL_REGISTER_MOD`).
- Native modules can register module toggles, typed configuration entries, and custom HUD buttons through `pl::modmenu`.
- The Preloader exposes a generic native detour API (`pl::memory::hook`) and signature resolution against `libminecraftpe.so`.
- The current bundled signature rules in `resources/preloader/preloader_signature_rules_source.json` cover pause-menu/HUD screen lifecycle and menu visibility. They do not provide player, inventory, item durability, potion effect, or entity-state accessors.
- Java's `ExternalModBridge` transports registered module metadata, config, buttons, and HUD draw commands. It does not itself provide Minecraft player or entity state.

## Not confirmed

The repository does not currently contain a documented, version-stable interface for reading:

- Local player inventory/equipment and armor durability
- Active potion/status effects
- Nearby entity/player information
- Target/aim information
- Combat events or authoritative server state

The existence of generic hooks and signature scanning does not establish that any of these data sources are available or safe to call. No guessed offsets, unverified signatures, or fabricated HUD values should be shipped as working features.

## Implementation direction

Prioritize substantial, legitimate client-side PvP functionality, with each module gated on a verified data source:

1. **Equipment/armor status** — first candidate once local-player inventory and item durability access are verified.
2. **Status-effect tracker** — requires a verified active-effect source.
3. **Combat feedback** — use only reliable local events and avoid automating attacks or modifying reach/damage.
4. **Training utilities** — may be implemented independently when they do not require private game-state access.

Do not implement automated attacks against other players, reach/damage manipulation, anti-cheat evasion, or similar public-server cheating behavior.

## Required verification before a gameplay module is marked complete

- Confirm exact Minecraft build and ABI.
- Validate the selected Preloader signatures against the exact `1.26.62.1` binary.
- Identify the actual class/function/data source from matching symbols or verified signatures.
- Confirm lifecycle/thread requirements and null/disconnect behavior.
- Add version-gated handling and fail closed when unsupported.
- Build ARM64 APK and test in a local/offline world before claiming support.
- Clearly distinguish source implementation, successful CI build, and device-tested behavior.

## Current result

The existing native integration is a viable extension point. The current version rules select a menu/HUD lifecycle signature set for `1.26.62.1`, but exact-build signature resolution is not yet verified. The checked-in API and signature rules remain insufficient to truthfully claim working armor, potion, target, or entity tracking. Further reverse-engineering/verification is required before those modules can be implemented responsibly.
