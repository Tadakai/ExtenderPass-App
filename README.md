# ExtenderPass App

<p align="center">
  <img src="https://img.shields.io/badge/platform-Android-green?style=flat-square&logo=android" />
  <img src="https://img.shields.io/badge/license-GNU%20GPL%20v3-blue?style=flat-square" />
</p>

  <b>Turn any short password into a strong, unique one — right from your pocket.</b><br/>
  Same input · Same output · Every device · Every time
</p>

---

## What is ExtenderPass App?

ExtenderPass App is the Android companion to [ExtenderPass](https://github.com/Tadakai/ExtenderPass). It stretches a simple master password into a long, high-entropy string using **PBKDF2-HMAC-SHA512** with 300,000 iterations — all offline, all on-device, nothing stored.

> **Cross-platform compatible:** The app produces byte-for-byte identical output to the [Java CLI](https://github.com/Tadakai/ExtenderPass)

---

## Features

- **Deterministic** — same input always → same output, on any Android device
- **Material You** — adapts to your system wallpaper colors (Android 12+)
- **4 charset modes** — All / Alphanumeric / Letters / Digits
- **No network** — works 100% offline, no permissions required
- **No storage** — the app is fully stateless, nothing is ever saved
- **Fast** — PBKDF2 runs off the main thread, UI stays responsive

---

## Screenshots

> *(coming soon)*

---

## Download

### Build from source

```bash
# Clone the repository
git clone https://github.com/Tadakai/ExtenderPass-App.git
cd ExtenderPass-App

# Build a debug APK
./gradlew assembleDebug

# Install on a connected device
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Requirements

| Tool | Version |
|------|---------|
| Android Studio | Hedgehog (2023.1.1) or newer |
| Gradle | 8.6 |
| Kotlin | 1.9.22 |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 34 (Android 14) |
| Java | 17 |

---

## Algorithm

```
DK = PBKDF2-HMAC-SHA512(
    password   = UTF-8(seed),
    salt       = "extenderpass_fixed_salt_v5"  (UTF-8, fixed forever),
    iterations = 300,000,
    dkLen      = estimated_bytes
)

output = rejection_sampled(DK, charset)
```

### Why is it always the same output?

The app implements PBKDF2-HMAC-SHA512 **manually** following RFC 2898 §6.2, instead of relying on `PBEKeySpec` (which had encoding bugs in older Android versions). This means the output is guaranteed to be identical across:

- Any Android version (API 26+)
- Any device manufacturer
- Any future version of this app
- The [Java CLI tool](https://github.com/Tadakai/ExtenderPass)

The salt, iteration count, charset order, and rejection threshold are all **hardcoded constants** that will never change.

---

## Character Sets

| Mode | Characters |
|------|-----------|
| **All** *(default)* | `A-Z a-z 0-9 !@#$%^&*()-_=+[]{};:,.<>?` |
| **A-Z 0-9** | `A-Z a-z 0-9` |
| **A-Z** | `A-Z a-z` |
| **0-9** | `0-9` |

---

## Privacy

- No internet permission
- No data stored on disk
- No analytics or tracking
- No backup (explicitly disabled in manifest)
- Everything happens locally on your device

---

## CLI Version

Prefer the terminal The same algorithm is available as a single-file Java CLI tool:

**[ExtenderPass](https://github.com/Tadakai/ExtenderPass)** — runs on Linux, macOS and Windows with no dependencies.
