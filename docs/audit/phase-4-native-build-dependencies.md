# Phase 4: Native Build Dependency Audit

Date: 2026-11-21

## Scope and method

Static inspection of the fork's Gradle configuration, Android CI workflow, CMake project, and pinned native submodules. This is not a compiler run. No APK has been produced or installed.

## Pinned native components

The fork pins these Git submodules:

- Android Preloader: `cc08b40a31471d3e2dedb267f5f17b28da2b0604`
- Microsoft libHttpClient: `f8ae1d9cf931d9d55542865eee395adca5bdbe41`

The root `.gitmodules` declares both URLs, and the fork's git tree records both as gitlinks at those exact commits. The CI checkout uses `submodules: recursive`, which is necessary because libHttpClient itself has nested dependencies.

The pinned libHttpClient commit contains nested submodule pointers for Asio, OpenSSL, WebSocket++, and zlib. Its Android CMake target references those source directories. The generated Android OpenSSL headers are tracked in the libHttpClient repository. Recursive checkout is therefore an important build requirement, not an optional convenience.

## Native build layout

The app CMake project adds:

1. `preloader` — Android-only C++20 shared library.
2. `libHttpClient/Build/libHttpClient.Android` — Microsoft Android HTTP client target.
3. `worldtools` — C++23 JNI shared library with vendored Snappy, Zstandard, LevelDB, NBT, and BinaryStream sources.

The Preloader CMake project requires the Android toolchain and checks for its ARM64 GlossHook static library. The pinned commit contains the ARM64 library at `lib/ARM64/libGlossHook.a`.

The app's ABI filter is `arm64-v8a`; the CI NDK setup requests r28c, matching the app's configured NDK version `28.2.13676358`. The CI SDK package list now explicitly includes Android Platform 36, Build Tools 35.0.0 and 36.0.0, and CMake 3.22.1. Build Tools 35.0.0 remains installed because the release-signing step explicitly selects it.

## CI status and limitation

The workflow now attempts `:app:assembleDebug` when Firebase secrets are absent and uploads the resulting debug APK. The PR was opened as a draft solely to make the branch's CI validation visible without merging it into `main`.

At the time of this report, GitHub reports zero workflow runs and zero check runs for the branch/PR head. This means there is still no build result. The repository's Actions configuration may require the owner to enable Actions for the fork or manually start the workflow; this has not been confirmed from the available read-only API.

## Remaining build risks

- Recursive submodule checkout must succeed for all nested libHttpClient dependencies.
- Preloader's CMake FetchContent dependencies (nlohmann/json, Boost.PFR, magic_enum, fmt, and lunasvg) must be downloadable in the runner environment.
- The full native CMake configure/compile/link must pass for ARM64.
- The Firebase placeholder path must be accepted by the Google Services Gradle plugin in a real run.
- The CI run must reach the artifact upload step and produce exactly one debug APK.

## Next gate

Enable/run the workflow and inspect its first failure, if any. Fix only evidenced failures. Do not add gameplay hooks or version-specific game offsets until the baseline APK assembles and the diagnostic module is validated on-device.


## CI follow-up

The Android SDK setup input is space-delimited by `android-actions/setup-android@v3`; the workflow now supplies the SDK package identifiers as a single space-delimited value. A fresh push-triggered run is required to test commit `9a66b537d740be357ec75b9344ae9cddab614407`; retrying the earlier run reuses its original workflow commit.
