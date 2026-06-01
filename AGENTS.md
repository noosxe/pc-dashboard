# AGENTS.md - Technical Context for LLM Agents

This document provides a high-level technical overview of the PCDashboard project to assist LLM agents in understanding the codebase, architecture, and design patterns.

## Project Goal
PCDashboard is a real-time PC statistics monitor for Android, optimized for use as a dedicated secondary display (immersive mode, "always-on" screen).

## Architecture & Data Flow
The project follows **MVVM** with a **Flow-based** unidirectional data flow and a foreground service for persistent connectivity.

1.  **Data Source**: `PcRepository` interface defines `getPcStatsFlow(): Flow<PcStats>`, `getNotificationsFlow()`, `getSessionLockFlow()`, `getMediaStateFlow()`, and `sendMediaCommand()`.
2.  **Implementation**: 
    - `MockPcRepository`: Generates random stats for testing/UI development.
    - `WebSocketPcRepository`: Connects to a server via WebSockets (using OkHttp) to receive real-time telemetry, media info, notifications, and lock state.
3.  **Service**: `PcStatsService` is a foreground service that keeps the repository flows active even when the activity is in the background or the screen is off.
4.  **ViewModel**: `DashboardViewModel` consumes various repository flows and exposes them as `StateFlow`s for the UI. It also manages theme state and media commands.
5.  **UI**: Jetpack Compose observes `uiState` and other flows. It uses `NavHost` for navigation between the Dashboard and Settings screens.

## Key Components

### Entry Point & System UI
- **`MainActivity.kt`**: 
    - Handles `WindowInsetsController` to enable immersive mode (transient system bars).
    - Manages `FLAG_KEEP_SCREEN_ON` and screen brightness (dimming to 1% when the host PC is locked).
    - Starts `PcStatsService` and requests necessary permissions (e.g., `POST_NOTIFICATIONS`).
    - Hosts the `NavHost` and manages top-level app state.

### State Management
- **`DashboardViewModel.kt`**:
    - Manages `uiState` (`PcStats`).
    - Manages `isLocked` state (dims screen when true).
    - Manages `mediaState` (list of active players and their metadata).
    - Exposes a `notifications` flow for displaying toasts.
    - Handles theme switching and media control commands.

### Data Layer
- **`PcStats.kt`**: Contains domain data classes (`PcStats`, `MediaState`, `PcNotification`) and DTOs for `kotlinx.serialization` to handle polymorphic WebSocket messages.
- **`SettingsRepository.kt`**: Simple repository for app settings like themes.

### Theming System
- **`Theme.kt`**: Defines `AppTheme` enum (Tokyo Night and Catppuccin variants) and `PCDashboardTheme` composable.
- **`Color.kt`**: Contains color palettes for all theme variants.
- **`Type.kt`**: Defines Material 3 typography.

## Design Decisions
- **Immersive Mode**: Designed for "always-on" usage as a dashboard.
- **Foreground Service**: Ensures the WebSocket connection remains active, allowing the device to react immediately to PC events (like locking or notifications).
- **Session Locking**: Automatically dims the Android screen when the PC is locked to save power and reduce burn-in.
- **Media Controls**: Integrated media controls using a `HorizontalPager` to switch between multiple active media players on the PC.

## Git Workflow & Rules for Agents

To maintain a clean and stable repository, agents must adhere to the following workflow:

1.  **Branching Strategy**:
    - **Never push directly to `main`**. It is a protected branch.
    - Always commit work on a **new branch**.
    - Branch names must use descriptive prefixes: `feature/`, `fix/`, `docs/`, `ci/`, `chore/`.

2.  **Commit Standards**:
    - Commit messages must start with a prefix: `docs:`, `fix:`, `feature:`, `ci:`, `chore:`, etc.
    - **Commit Message Bodies**: Must contain detailed, useful information regarding the changes.

3.  **Safety & Communication**:
    - **Never blindly discard changes**. Ask the user if unsure.
    - Use `git stash` to preserve potential changes instead of deleting them.

4.  **Merging**:
    - Notify the user once changes are pushed to a branch for review and merging.

## Tips for Agents
- **Adding Stats**: Update DTOs in `PcStats.kt`, the `PcRepository` interface, then update `MockPcRepository`, `WebSocketPcRepository`, and finally the UI components (`StatCard`, `MemoryCard`).
- **Adding Themes**: Add a new entry to `AppTheme` enum, define colors in `Color.kt`, create a `ColorScheme` in `Theme.kt`, and update the `when` branch in `PCDashboardTheme`.
- **WebSocket Messages**: New message types should be added to the `ServerMessage` sealed class in `PcStats.kt` and handled in `ServerMessageSerializer`.

