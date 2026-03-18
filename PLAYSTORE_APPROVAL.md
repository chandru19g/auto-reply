# Play Store Approval Steps

This document outlines steps and checklists to maximize the chance of **Google Play Store approval** for the Auto-Reply app, which uses sensitive permissions (SMS, Call Log, Notification Listener).

---

## Production code checklist (before building release APK)

- [ ] **Re-enable 30-minute rate limit**  
  In `app/src/main/java/com/autoreply/app/service/RateLimitHelper.kt`, change:
  ```kotlin
  private const val COOLDOWN_MS = 0L   // testing — replace this
  ```
  to:
  ```kotlin
  private const val COOLDOWN_MS = 30 * 60 * 1000L   // 30 minutes
  ```
  This prevents the app from spamming auto-replies to the same contact more than once per 30 minutes.

---

## Pre-submission checklist

- [ ] **Store listing and in-app copy**  
  The app clearly promotes **automation based on user-set modes** (Sleeping, Working, Driving) in:
  - Play Store title and short/long description  
  - In-app text (e.g. “Choose a mode to auto-reply to calls, SMS, and WhatsApp”)

- [ ] **Privacy Policy**  
  - Publish a Privacy Policy (hosted at a stable URL).  
  - State that the app **does not upload** SMS, call log, or notification content to any server; all processing is on-device.  
  - Link the Privacy Policy from the Play Console store listing and from inside the app (e.g. Settings or About).

- [ ] **In-app disclosure**  
  - Before requesting each sensitive permission, show a short explanation (e.g. “We need SMS access to send automatic replies when your mode is on”).  
  - Before the user enables Notification access, explain that it is used to detect WhatsApp (and similar) messages and send the user’s chosen auto-reply when a mode is active.

---

## Permissions Declaration Form (Play Console)

When submitting the app, complete the **Permissions Declaration Form** in Play Console with the following.

### SMS and Call Log permissions

- **Use case:** **Device automation**
- **Explanation (suggested text):**  
  “User selects a mode (Sleeping, Working, or Driving). The app automatically replies to incoming calls and SMS: it can reject calls and send an SMS reply to the caller, and reply to incoming SMS with the user’s chosen message. All actions are triggered only when the user has turned on one of these modes and configured the reply text.”

- **Eligible permissions:**  
  `READ_PHONE_STATE`, `READ_CALL_LOG`, `CALL_PHONE`, `SEND_SMS`, `RECEIVE_SMS`, `READ_SMS` (or the subset your app actually uses).

### Notification listener

- **Explanation (suggested text):**  
  “Used to detect incoming WhatsApp (and similar) messages and send the user’s chosen auto-reply when a mode (Sleeping, Working, or Driving) is active. The user enables notification access in system settings; the app only uses it to trigger the reply action on the notification.”

- Attach a **short video** that shows:
  1. Enabling a mode (e.g. Driving).  
  2. Receiving a call → call is rejected and an SMS is sent to the caller.  
  3. Receiving an SMS → automatic reply is sent.  
  4. Receiving a WhatsApp message → automatic reply is sent via the notification reply action.

- Provide **test account / instructions** if needed (e.g. how to trigger each flow for reviewers).

---

## Store listing

- **Title / short description:**  
  Mention “auto-reply when you’re sleeping, working, or driving” and “automate replies to calls, SMS, and WhatsApp.”

- **Avoid:**  
  Wording that suggests the app is only for “notification enhancement” or “alerts.” Keep the focus on **user-defined automation** (user sets the mode and the reply text; the app performs the actions).

---

## Post-submission

- If the app is **rejected**, note the reason (permission declaration, video, policy, or other). Update the declaration and/or app (e.g. clearer in-app explanations, video, or privacy policy) and resubmit.

- **Plan B:** If the full app (with SMS/Call Log) is repeatedly rejected, consider a **WhatsApp-only** build (no SMS/call permissions) to get on the store and iterate.

---

## Ongoing

- If you add **new permissions** or **new use cases**, resubmit the Permissions Declaration Form with updated information and update this document.
