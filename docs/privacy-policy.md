# Privacy Policy for Chronos

**Effective Date:** November 10, 2026  
**Last Updated:** November 10, 2026  
**Application Name:** Chronos  
**Package ID:** `com.aistudio.chronos.vktw`

---

## 1. Introduction

Welcome to **Chronos** ("we," "our," or "us"). Chronos is a countdown timer application for Android that allows you to track upcoming events across years, months, days, hours, minutes, and seconds, with customizable notifications, categories, multi-timezone support, and home/lock screen widgets.

We are committed to protecting your privacy. This Privacy Policy explains how Chronos handles your information when you use our mobile application. In short: **Chronos operates offline and stores all of your countdown events and preferences locally on your device. We do not collect, transmit, rent, or sell any personal data.**

---

## 2. Information We Collect and How It Is Stored

### 2.1 User-Created Content (Stored Locally on Your Device)
When you create, edit, or organize countdown timers in Chronos, the information you enter is stored strictly on your device using a local SQLite database (via Android Jetpack Room) and local Android SharedPreferences:
- **Countdown Event Details:** Event titles, target dates and times, selected time zones, categories, accent colors, display ordering, and optional notes/details you enter.
- **Notification Preferences:** Custom reminder offsets and alert settings configured for your countdowns.
- **Widget Configuration:** Mappings between placed home/lock screen widgets (`3×1`, `4×1`, `5×1`, and `2×2` widgets) and the specific countdown timer each widget displays.

**None of this information leaves your device or is transmitted to us or any third-party server.**

### 2.2 Personal Information
Chronos does **not** require you to create an account, sign in, or provide any personally identifiable information (such as your name, email address, phone number, or physical location).

### 2.3 Analytics, Tracking, and Advertising
Chronos does **not** integrate any third-party analytics trackers, crash-reporting services that harvest personal data, or advertising SDKs. We do not track your usage behavior across apps or websites.

---

## 3. Device Permissions We Use

To provide core countdown, alarm, and widget functionality, Chronos requests only the minimum required Android system permissions:

| Permission | Purpose |
| :--- | :--- |
| **`POST_NOTIFICATIONS`** | Allows Chronos to display timely reminder notifications when your countdown events reach their scheduled alert times or target dates. You can grant or revoke this permission at any time in your Android system settings. |
| **`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`** | Enables Chronos to trigger countdown completion alerts, custom advance reminders, and minute-boundary widget updates at the exact scheduled time. |
| **`RECEIVE_BOOT_COMPLETED`** | Allows Chronos to automatically reschedule your active countdown alarms and refresh your home screen widgets after your device restarts. |
| **`VIBRATE`** | Provides haptic feedback and vibration alerts when a countdown notification is triggered. |

---

## 4. Data Retention, Backup, and Deletion

- **Local Control:** All countdown timers, notes, and widget preferences remain on your device for as long as you keep them in the app.
- **Deleting Data:** You can edit or permanently delete individual countdown timers at any time directly within the Chronos app. You can also clear all app data via **Android Settings → Apps → Chronos → Storage & cache → Clear storage**, or by uninstalling the application.
- **Android System Backup:** Depending on your device settings, Android's built-in system backup (`allowBackup`) may back up your local app data to your personal Google Drive account so your timers can be restored if you switch or reset devices. This backup is governed by your personal Google Account settings and Google's Privacy Policy; we do not have access to your backups.

---

## 5. Data Sharing and Disclosure

Because we do not collect or transmit your data to external servers, **we do not share, sell, trade, or disclose your information to any third parties.**

---

## 6. Data Security

Your countdown data is stored within the application's private, sandboxed storage directory on your Android device, which is protected by the Android operating system's security model and device-level encryption.

---

## 7. Children's Privacy

Chronos does not knowingly collect any personal information from anyone, including children under the age of 13 (or the applicable age of consent in your jurisdiction). Because all data stays locally on the user's device, the app complies with the Children's Online Privacy Protection Act (COPPA) and similar international regulations.

---

## 8. Changes to This Privacy Policy

We may update this Privacy Policy from time to time to reflect changes in app features or legal requirements. Any updates will be posted on this page with an updated "Last Updated" date. We encourage you to review this Privacy Policy periodically.

---

## 9. Contact Us

If you have any questions, concerns, or feedback regarding this Privacy Policy or the Chronos app, please contact us at:

- **Email:** [teohpix@gmail.com](mailto:teohpix@gmail.com)
