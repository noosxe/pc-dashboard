# PCDashboard

PCDashboard is an Android application designed to provide real-time monitoring of PC statistics. It features a clean, immersive UI with support for popular color schemes like Tokyo Night and Catppuccin.

## Features

- **Real-time Monitoring**: Track CPU and GPU usage and temperatures.
- **Memory Stats**: Monitor RAM and VRAM usage and total capacity.
- **Immersive Mode**: Full-screen dashboard with system bars hidden for a focused experience.
- **Stay Awake**: Keeps the screen on while the app is active, perfect for use as a dedicated secondary display.
- **Theme Support**: Includes multiple variations of **Tokyo Night** and **Catppuccin** themes.

## Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Design System**: [Material 3](https://m3.material.io/)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Reactive Programming**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) and [Flow](https://kotlinlang.org/docs/flow.html)
- **Navigation**: [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)

## Project Structure

- `data/`: Contains data models (`PcStats`), repository interfaces, and implementations (including a `MockPcRepository` for testing).
- `ui/`:
    - `dashboard/`: Contains the `DashboardViewModel` which manages the UI state.
    - `theme/`: Defines the color schemes, typography, and the main `PCDashboardTheme` wrapper.
- `MainActivity.kt`: The entry point of the application, handling navigation and immersive mode setup.

## Themes

PCDashboard supports the following themes:

- **Tokyo Night**: Night, Storm, Moon, and Day variations.
- **Catppuccin**: Mocha, Macchiato, Frappe, and Latte variations.

## Getting Started

1. Clone the repository.
2. Open the project in Android Studio (Ladybug or newer recommended).
3. Build and run the `app` module on an Android device or emulator.

Currently, the app uses a `MockPcRepository` to simulate real-time data. Future versions will integrate with a PC-side agent to fetch actual system statistics.
