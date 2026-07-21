# UCall Widget Lite

UCall Widget Lite is a lightweight Android home-screen widget for seeing the latest phone call and calling back with one tap.

The project was created for OpenAI Build Week by a non-technical product creator with Codex assistance. Codex helped turn product requirements, testing feedback, and device-specific observations into a working Android project.

## Overview

UCall Widget Lite focuses on one practical job: keep the latest call visible on the home screen and make callback fast. It is a simplified widget-only version of the broader UCall idea, with overlay, foreground service, usage access, watchdog, heartbeat, game mode, notification, and special-access flows removed.

The app is intentionally small and privacy-conscious. It reads local phone/call/contact data only on the device to render the widget.

## Main Features

- Latest-call home-screen widget
- Compact default widget variant, sized for approximately 1x4 launcher cells
- Optional wide widget variant, sized for approximately 1x5 launcher cells
- One-tap callback from the widget
- Incoming, outgoing, and missed call type indicators
- Localized English and Ukrainian UI text
- Permission onboarding for the Lite permissions only
- Automatic widget refresh after real call-end events
- Additional safe refresh triggers for launcher/widget lifecycle events

## Privacy

UCall Widget Lite does not require an account and does not send call data to a server.

The app uses Android permissions to read the local call log, phone state, and contacts so it can display the latest call and format the widget correctly. Call data is stored locally on the device through the app's local storage helpers. The project does not include analytics, network tracking, cloud sync, advertising SDKs, or remote APIs.

No signing credentials, API keys, personal keystores, APK/AAB build artifacts, local SDK paths, or ZIP archives are intended to be committed to this repository.

## Technical Stack

- Kotlin
- Android SDK
- AppWidgetProvider / RemoteViews
- WorkManager
- Android call log and phone-state APIs
- Gradle Kotlin DSL
- AndroidX and Material components

## Required Permissions

The Lite app requests only the permissions needed for widget operation:

- `READ_PHONE_STATE` - detect real call-state transitions
- `READ_CALL_LOG` - read the latest call after a call ends
- `READ_CONTACTS` - display contact names when available
- `CALL_PHONE` - support one-tap callback from the widget
- `RECEIVE_BOOT_COMPLETED` - refresh/recover widgets after reboot

The app does not use overlay permission, usage access, foreground service permission, notification permission, wake locks, or network-state permission.

## Build and Run

1. Clone the repository.
2. Open the project in Android Studio.
3. Let Gradle sync complete.
4. Build a debug APK:

```bash
./gradlew assembleDebug
```

On Windows, the equivalent command is:

```bash
./gradlew.bat assembleDebug
```

5. Install and run the debug build on an Android device or emulator with phone/call-log capability.

## How to Add and Test the Widget

Concise judging flow:

1. Install and launch UCall Widget Lite.
2. Grant the requested Android runtime permissions.
3. Continue through onboarding.
4. When prompted, add the widget to the home screen.
5. Add either the default widget or the optional wide widget from the launcher widget picker.
6. Make or receive a real phone call, then end the call.
7. Confirm the widget updates to show the latest call.
8. Tap the call button on the widget to verify callback behavior.
9. Reboot the device or resize the widget to confirm the widget refreshes cleanly.

The final Android launcher widget-pinning confirmation is controlled by the system launcher, not by this app.

## How Codex Was Used

UCall Widget Lite was created and refined with Codex. The Codex desktop model used for direct repository work was GPT-5.5.

Codex was used throughout development to:

- audit and remove non-Lite functionality;
- rebrand the project as UCall Widget Lite;
- separate the widget-only update flow from removed full-app features;
- implement and review the Android code;
- fix Ukrainian localization and encoding issues;
- improve onboarding and add-widget UI behavior;
- add compact and wide widget variants;
- improve call-end refresh reliability;
- run Gradle builds;
- reduce debug logging and temporary diagnostics before testing;
- prepare Git history;
- publish the GitHub repository;
- update documentation.

During the final OpenAI Build Week phase, GPT-5.6 Thinking in ChatGPT was used for product and architecture work: refining the product logic, reviewing the widget update flow, making privacy and customization decisions, and shaping the final demo and submission narrative.

GPT-5.6 was used in ChatGPT for this final reasoning and product-shaping phase. It was not selected inside Codex for direct repository work.

## Current Project Status

The project is in release-candidate testing state for OpenAI Build Week submission.

Current state:

- Debug build passes with `./gradlew assembleDebug`
- Current public repository history is cleaned to a safe source snapshot
- Widget-only Lite package is `com.ucall.widget.lite`
- Temporary refresh Toast diagnostics are disabled by default
- No overlay, foreground service, usage access, watchdog, heartbeat, polling service, or notification logic is part of the Lite app

## Known Limitations

- Widget refresh behavior can vary by Android version, launcher, and OEM background restrictions.
- Launcher widget sizing is not fully standardized; default and wide variants are configured for common launcher cell sizes, but users may still resize manually.
- Call-log availability can be delayed briefly after a call ends, so the app uses short retry timing.
- Devices without phone/call-log capability are not the primary target.
- The project has not yet completed broad external device testing.

## Future Plans

- Test across Samsung One UI, Pixel Launcher, MIUI, and Microsoft Launcher.
- Improve onboarding copy based on tester feedback.
- Add more device-specific reliability notes if needed.
- Prepare release signing and Play Console metadata outside the public source repository.
- Keep the Lite app focused on the widget-only last-call experience.
