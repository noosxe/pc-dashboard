# PCDashboard

PCDashboard is an Android application designed to provide real-time monitoring and control of your PC. It features a clean, immersive UI with support for popular color schemes like Tokyo Night and Catppuccin, optimized for use as a dedicated secondary display.

## Features

- **Real-time Telemetry**:
    - **CPU**: Monitor usage percentage and temperature.
    - **GPU**: Monitor usage percentage and temperature.
    - **Memory**: Track RAM and VRAM usage vs total capacity.
- **Media Control & Monitoring**:
    - View currently playing track, artist, and album art.
    - Full playback controls: Play/Pause, Skip Previous/Next.
    - Progress tracking with a visual seek bar.
    - Support for multiple active media players with a swipeable pager.
- **Remote Notifications**: View PC system and app notifications directly on your Android device.
- **Session Awareness**: Automatically detects when the PC session is locked, dimming the screen and showing a minimal clock to save power and reduce distraction.
- **Immersive Mode**: Full-screen dashboard with system bars hidden for a focused, distraction-free experience.
- **Always-On Screen**: Keeps the screen active while the app is in focus, perfect for dedicated dashboard tablets or phones.
- **Digital Clock**: Always-visible real-time clock on the dashboard.
- **Theme Support**: Extensively themed with multiple variations of **Tokyo Night** (Night, Storm, Moon, Day) and **Catppuccin** (Mocha, Macchiato, Frappe, Latte).

## Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jackpack/compose)
- **Design System**: [Material 3](https://m3.material.io/)
- **Architecture**: MVVM (Model-View-ViewModel) with Unidirectional Data Flow.
- **Reactive Programming**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) and [Flow](https://kotlinlang.org/docs/flow.html).
- **Networking**: [Ktor](https://ktor.io/) (used in the background service to connect to the PC agent).
- **Navigation**: [Compose Navigation](https://developer.android.com/jetpack/compose/navigation).
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/) for album art and icons.

## Project Structure

- `data/`: Domain models (`PcStats`, `MediaState`, `PcNotification`), repository interfaces, and implementations.
- `service/`: `PcStatsService` - a foreground service that manages the persistent connection to the PC host.
- `ui/`:
    - `dashboard/`: `DashboardViewModel` managing UI state and business logic.
    - `theme/`: Custom Material 3 color schemes and typography for Tokyo Night and Catppuccin.
- `MainActivity.kt`: Main entry point, handles system UI (immersive mode), window flags, and navigation.

## Themes

PCDashboard supports a wide range of aesthetic themes:

- **Tokyo Night**: 
    - 🌙 Night
    - ⛈️ Storm
    - 🌛 Moon
    - ☀️ Day
- **Catppuccin**:
    - 🌿 Latte
    - 🌅 Frappé
    - 🍱 Macchiato
    -  mocha Mocha

## Getting Started

1. Clone the repository.
2. Open the project in Android Studio (Ladybug or newer recommended).
3. Build and run the `app` module on an Android device.

*Note: Currently, the app can be configured to use a `MockPcRepository` for UI testing. To connect to a real PC, a corresponding agent must be running on the host machine.*

## Roadmap

- **Interactive Notifications**: Support for notification actions (e.g., clicking 'Reply' or 'Dismiss' from the Android device).
- **Customizable Layouts**: Allow users to reorder or hide specific dashboard widgets.
- **Historical Data**: Graphs for telemetry over time.
