# AGENTS.md - Technical Context for LLM Agents

This document provides a high-level technical overview of the PCDashboard project to assist LLM agents in understanding the codebase, architecture, and design patterns.

## Project Goal
PCDashboard is a real-time PC statistics monitor for Android, optimized for use as a dedicated secondary display (immersive mode, "always-on" screen).

## Architecture & Data Flow
The project follows **MVVM** with a **Flow-based** unidirectional data flow.

1.  **Data Source**: `PcRepository` interface defines `getPcStatsFlow(): Flow<PcStats>`.
2.  **Implementation**: `MockPcRepository` currently generates random stats for testing/UI development.
3.  **ViewModel**: `DashboardViewModel` consumes the Flow and exposes it as a `StateFlow<PcStats>` via `stateIn`.
4.  **UI**: Jetpack Compose observes `uiState` and updates the dashboard widgets.

## Key Components

### Entry Point & System UI
- **`MainActivity.kt`**: Handles `WindowInsetsController` to enable immersive mode (hiding system bars) and sets `FLAG_KEEP_SCREEN_ON`.

### State Management
- **`DashboardViewModel.kt`**:
    - Manages `uiState` (`PcStats`).
    - Manages `theme` (`AppTheme`) from `SettingsRepository`.
    - Handles theme switching via `setTheme(AppTheme)`.

### Data Layer
- **`PcStats.kt`**: Data class containing CPU/GPU usage, temperatures, and RAM/VRAM info.
- **`SettingsRepository.kt`**: Simple in-memory (for now) repository for app settings like themes.

### Theming System
- **`Theme.kt`**: Defines `AppTheme` enum and `PCDashboardTheme` composable.
- **`Color.kt`**: Contains the hex values for Tokyo Night and Catppuccin palettes.
- **`Type.kt`**: Defines Material 3 typography.

## Design Decisions
- **Immersive Mode**: The app is designed to be "glanceable" and distraction-free.
- **Mocking**: The `MockPcRepository` allows for rapid UI iteration without needing a live PC connection.
- **Theming**: Heavy emphasis on aesthetic themes (Tokyo Night, Catppuccin) using custom `ColorScheme` definitions.

## Tips for Agents
- **Adding Stats**: Update `PcStats.kt`, the `PcRepository` interface, and then update both `MockPcRepository` and the UI.
- **Adding Themes**: Add a new entry to `AppTheme` enum, define colors in `Color.kt`, create a `ColorScheme` in `Theme.kt`, and update the `when` branch in `PCDashboardTheme`.
- **Navigation**: Currently a single-screen app, but `MainActivity` is set up to host a `NavHost` if needed.
