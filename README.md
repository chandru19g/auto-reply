# Auto-Reply

Android app that automates replies to **calls**, **SMS**, and **WhatsApp** when you turn on one of three modes: **Sleeping**, **Working**, or **Driving**.

## Features

- **Modes:** Off, Sleeping, Working, Driving (one active at a time).
- **Calls:** When a mode is on, incoming calls can be rejected and an SMS is sent to the caller with your custom message.
- **SMS:** Incoming SMS get an automatic reply with your custom message for the active mode.
- **WhatsApp:** Incoming WhatsApp messages get an automatic reply using the notification reply action (no official WhatsApp API).

All reply text is configurable per mode in Settings. No data is sent to any server; everything runs on-device.

## Requirements

- Android 7.0 (API 24) or higher.
- For WhatsApp auto-reply: enable **Notification access** for this app in system Settings, and ensure WhatsApp notifications include the reply action.

## How to run

1. Open the project in Android Studio (File → Open → select the project folder).
2. Let Android Studio sync Gradle (it will create the Gradle wrapper if needed).
3. Connect a device or start an emulator, then run the `app` configuration.

## Project structure

- `app/src/main/java/com/autoreply/app/`
  - `domain/` – Mode enum.
  - `data/` – Preferences, app state, repository.
  - `ui/` – Compose screens (main, settings), ViewModel, theme.
  - `service/` – CallReceiver, SmsReceiver, WhatsAppReplyService (NotificationListenerService), rate limiting.
  - `util/` – Notification listener helper.

## Play Store approval

For steps and checklists to submit the app to the Google Play Store (permissions declaration, video, store listing, privacy policy), see **[PLAYSTORE_APPROVAL.md](PLAYSTORE_APPROVAL.md)**.

## License

Private / unlicensed unless stated otherwise.
