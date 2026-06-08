# TaskApp

TaskApp is a comprehensive, modern Android application designed for managing tasks, notes, and high-priority reminders. It serves as a feature-rich replacement for standard note-taking apps, integrating deeply with Android's notification and alarm systems.

## Key Features

### 1. My Lists (Checklists & Notes)
*   **Dual Mode**: Create structured checklists or free-form text notes.
*   **Visual Hierarchy**: Support for subtasks via horizontal dragging for indentation.
*   **Customization**: Personalize lists with custom background colors.
*   **Organization**: Manual drag-and-drop reordering of lists on the home screen.
*   **Security**: Lock lists to prevent accidental edits.
*   **Lifecycle Management**: Archive completed lists or move them to Trash (auto-deleted after 30 days).

### 2. My Tasks (Standalone Reminders)
*   Quickly add individual tasks or reminders not tied to a specific list.
*   Simple click-to-edit interface.
*   Direct checkbox completion on the main tasks page.
*   Independent reminder scheduling for every task.

### 3. My Alarms
*   **Google Clock Experience**: Multiple concurrent alarms with custom labels.
*   **Recurring Schedules**: Set alarms for specific days of the week or every day.
*   **Persistent Alerts**: High-priority alarms that bypass "Silent" or "Mute" modes.
*   **Smart Snooze**: Configurable snooze duration (5, 10, 15, or 30 minutes).
*   **Sensory Options**: Choose from system ringtones or set a "Vibrate only" silent alarm.

### 4. Advanced Notifications
*   **Dynamic Updates**: Notifications in the status bar update in real-time as you edit tasks in the app.
*   **Flexible Frequency**: Support for Instant, Daily, Weekly, One-time, and Custom interval reminders.
*   **Heads-up Alerts**: Reminders "pop" onto the top of the screen for maximum visibility.

### 5. Personalization
*   **Theme Engine**: Switch between Light Mode, Dark Mode, or follow the OS System Default.
*   **Persistent Preferences**: All settings are saved using Jetpack DataStore.

---

## Technical Architecture

The app is built using modern Android development practices and the following stack:

*   **UI**: 100% **Jetpack Compose** for a declarative, reactive user interface.
*   **Navigation**: **Compose Navigation** with a shared Modal Navigation Drawer architecture.
*   **Database**: **Room Persistent Library** with a comprehensive migration history (Version 1-13).
*   **Preferences**: **Jetpack DataStore** for reliable, asynchronous storage of user settings.
*   **Asynchrony**: **Kotlin Coroutines and Flow** for thread-safe data streaming and background processing.
*   **Architecture Pattern**: **MVVM (Model-View-ViewModel)** with a clean Repository layer to separate concerns.

### Project Structure
- `ui/`: Contains Screen composables and ViewModels for each feature area.
- `domain/`: Pure Kotlin data models representing the core entities.
- `data/`: Room DAOs, Entities, and Repository implementations.
- `notification/`: Deep integration with `AlarmManager`, `NotificationManager`, and Foreground Services.

---

## Testing Suite

TaskApp includes a robust suite of **32 Unit Tests** verifying core business logic across:
*   **ViewModels**: Ensuring UI states and user interactions correctly trigger data changes.
*   **Repositories**: Verifying data filtering, reordering logic, and trash cleanup thresholds.
*   **Scheduling**: Testing the accuracy of alarm and notification time calculations.

The suite uses **MockK** for dependency injection and **Turbine** for testing asynchronous data flows.

---

## Setup & Installation

1.  Clone the repository.
2.  Open in **Android Studio Ladybug (or newer)**.
3.  Ensure you have **SDK 35** installed.
4.  Build and run on a device or emulator running **Android 8.0 (API 26)** or higher.
