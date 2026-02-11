# YT Skipper

An Android accessibility service that automatically taps on-screen elements matching user-defined text. Useful for skipping YouTube ads and other automated interactions.



## Overview

YT Skipper is a lightweight Android utility that uses the Accessibility Service API to monitor screen content and automatically click on UI elements containing specific text. Originally designed for skipping YouTube ads, it can be configured to interact with any app based on visible text triggers.

## Features

- **Auto-Click Matching Text**: Automatically taps on UI elements containing your configured target text
- **Customizable Target**: Set any text string to search for (default: "Skip Ad")
- **Accessibility Service**: Runs in the background using Android's accessibility API
- **Simple UI**: Clean Jetpack Compose interface with Material3 design
- **Service Status Indicator**: Visual indicator showing whether the service is active
- **Lightweight**: Minimal resource usage and battery impact

## Tech Stack

- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern declarative UI toolkit
- **Material3**: Latest Material Design components
- **AccessibilityService**: Android API for interacting with UI elements
- **Gradle**: Build automation with Kotlin DSL

## Prerequisites

- **Android Studio** (latest stable version recommended)
- **Android SDK** with API level 34 or higher
- **JDK** 11 or higher
- An Android device or emulator running **Android 14+** (API 34)

## Installation

### Build from Source

1. **Clone the repository:**

   ```bash
   git clone https://github.com/kanataidarov/ytskipper.git
   cd ytskipper
   ```

2. **Open in Android Studio:**

   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory and open it

3. **Build the project:**

   ```bash
   ./gradlew assembleDebug
   ```

   Or use Android Studio: **Build > Make Project** (Ctrl+F9 / Cmd+F9)

4. **Install on device:**

   ```bash
   ./gradlew installDebug
   ```

   Or use Android Studio: **Run > Run 'app'** (Shift+F10 / Ctrl+R)

### Install APK Directly

Download the latest APK from [Releases](../../releases) and install it on your Android device.

> **Note**: You may need to enable "Install from unknown sources" in your device settings.

## Usage

1. **Launch the app** - Open YT Skipper from your app drawer

2. **Configure target text** - Enter the text you want the app to auto-click (e.g., "Skip Ad", "Skip")

3. **Enable Accessibility Service** - Tap the button to open Accessibility Settings and enable "YT Skipper" service

4. **Grant permissions** - Allow the app to observe and interact with screen content

5. **Use other apps** - The service will now automatically click matching text when it appears on screen

## Configuration

| Setting | Description | Default |
|---------|-------------|---------|
| Target Text | The exact text to search for and click | "Google" |

Change the target text at any time from the app's main screen.

## Project Structure

```
ytskipper/
├── app/
│   ├── src/main/java/com/github/kanataidarov/ytskipper/
│   │   ├── MainActivity.kt          # Main UI with Compose
│   │   ├── ui/theme/                # Material3 theme components
│   │   │   ├── Color.kt
│   │   │   ├── Theme.kt
│   │   │   └── Type.kt
│   │   └── service/
│   │       └── ScreenClickerService.kt  # Accessibility service
│   ├── build.gradle.kts             # App module build config
│   └── src/main/AndroidManifest.xml # Service declarations
├── build.gradle.kts                 # Project build config
├── gradle/libs.versions.toml        # Version catalog
└── settings.gradle.kts              # Project settings
```

## Important Notes

- **Accessibility Permission**: This app requires the Accessibility Service permission to function. This is a system-level permission that allows the app to observe and interact with UI elements.

- **Battery Optimization**: The service may be affected by battery optimization settings. Consider disabling battery optimization for this app if you need consistent performance.

- **Security**: Only grant accessibility permissions to apps you trust. This permission allows extensive interaction with your device.

## Limitations

- Requires Android 14 (API 34) or higher
- Text matching is exact (case-sensitive)
- Cannot interact with secure windows (banking apps, payment screens)
- Some apps may block accessibility services

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -m 'Add my feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Open a Pull Request

## Issues

If you encounter any issues:

- Check that the accessibility service is enabled in Settings
- Ensure the target text exactly matches what's displayed on screen
- Verify your device meets the minimum Android version requirement
- Report bugs via [GitHub Issues](../../issues)

## License

Distributed under the MIT License. See [LICENSE](./LICENSE) for details.

---

**Disclaimer**: This app is for educational and convenience purposes. Use responsibly and in accordance with the terms of service of apps you interact with.
