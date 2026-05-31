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

## Git Workflow & Rules for Agents

To maintain a clean and stable repository, agents must adhere to the following workflow:

1.  **Branching Strategy**:
    - **Never push directly to `main`**. It is a protected branch.
    - Always commit work on a **new branch**.
    - Branch names must use descriptive prefixes:
        - `feature/`: New features or significant UI updates.
        - `fix/`: Bug fixes.
        - `docs/`: Documentation updates (including `AGENTS.md`).
        - `ci/`: CI/CD configuration changes.
        - `chore/`: Maintenance tasks.

2.  **Commit Standards**:
    - Commit messages must start with a prefix matching the branch type: `docs:`, `fix:`, `feature:`, `ci:`, `chore:`, etc.
    - **Commit Message Bodies**: Must contain detailed, useful information regarding the changes. Avoid one-line commits for complex changes.

3.  **Safety & Communication**:
    - **Never blindly discard changes**.
    - When in doubt about a change or a deletion, **ask the user** for confirmation.
    - If unsure if specific changes are required, use `git stash` to preserve them for potential later use instead of deleting them.

4.  **Merging**:
    - Once changes are pushed to a feature/fix branch, notify the user so they can review and merge the Pull Request on GitHub.

## Tips for Agents
- **Adding Stats**: Update `PcStats.kt`, the `PcRepository` interface, and then update both `MockPcRepository` and the UI.
- **Adding Themes**: Add a new entry to `AppTheme` enum, define colors in `Color.kt`, create a `ColorScheme` in `Theme.kt`, and update the `when` branch in `PCDashboardTheme`.
- **Navigation**: Currently a single-screen app, but `MainActivity` is set up to host a `NavHost` if needed.
