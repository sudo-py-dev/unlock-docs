# 🔓 Unlock-Docs

Unlock-Docs is a high-performance, cross-platform (Desktop & Android) application designed to recover passwords for encrypted documents and archives. Built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, it provides a clean, native UI and utilizes Kotlin Coroutines for blazing-fast, concurrent password cracking.

## ✨ Features

* **Multiplatform Support**: Runs seamlessly on Android and Desktop (Windows, macOS, Linux).
* **Modern UI**: Built with Jetpack Compose / Compose Multiplatform, featuring theming (Light/Dark/System) and internationalization (i18n).
* **Concurrent Recovery**: Utilizes `kotlinx.coroutines` with configurable concurrency to test passwords efficiently without blocking the UI.
* **Format Support**: Supports standard encrypted archives and Microsoft Office documents (powered by `zip4j` and `poi-ooxml`).
* **Wordlist Manager**: Easily import and manage custom dictionary files for dictionary attacks.

## 🛠️ Technology Stack

* **Kotlin Multiplatform (KMP)** - Core application logic sharing
* **Compose Multiplatform** - Declarative UI framework
* **Kotlin Coroutines** - Asynchronous and concurrent tasks
* **Apache POI** - Document format parsing (`poi-ooxml`)
* **Zip4j** - Zip archive encryption/decryption

## 🚀 Getting Started

### Prerequisites
* JDK 17
* Android Studio (for Android builds) or IntelliJ IDEA

### Building the App

**For Desktop (macOS / Windows / Linux):**
To run the app locally:
```bash
./gradlew run
```

To create a standalone distributable (e.g., `.dmg`, `.msi`, `.deb`):
```bash
./gradlew createDistributable
```
Your desktop app distribution will be ready at `app/build/compose/binaries/main/app`.

**For Android:**
To build and install the debug APK on a connected device/emulator:
```bash
./gradlew installDebug
```

## 🔒 Security & Usage Warning
This tool is intended for **educational purposes and legitimate password recovery only**. You should only attempt to unlock documents and archives that you own or have explicit authorization to access.

## 🤝 Contributing
Contributions are welcome! Please open an issue or submit a pull request for any bug fixes, optimizations, or new format support.
