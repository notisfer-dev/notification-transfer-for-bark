English | [日本語](README.ja.md)

# Bark Forwarder

Android app that forwards notifications, SMS, and call events to Bark with AES-256-GCM encrypted payloads.

## Features

- Bark-only forwarding to `https://api.day.app/push`
- AES-256-GCM payload encryption using Bark-compatible `ciphertext` and fixed `iv`
- Notification listener forwarding with per-app exclusion rules
- Duplicate notification timeout configurable from 1 to 120 seconds, default 5
- SMS and call forwarding with notification fallback when direct permissions are unavailable
- Automatic Play Store icon resolution from package name with cached results
- Optional manual icon URL override per app
- Public image URL forwarding to Bark `image`

## Build

This repository is set up for GitHub Actions and local Gradle builds.

```bash
gradle testDebugUnitTest
gradle assembleDebug
```

## Install

- Install `app/build/outputs/apk/debug/app-debug.apk` for local testing.
- `app-release-unsigned.apk` is intentionally unsigned and will not install until you add your own signing config.

## Bark Setup

Configure the Bark iPhone app with:

- Algorithm: `AES256`
- Mode: `GCM`
- Padding: `noPadding`
- Key: same 32-character key entered in the Android app
- IV: same 12-character IV entered in the Android app

<img src="docs/assets/bark-encryption-settings.png" alt="Bark encryption setup overview" width="590">

The Android app sends `ciphertext` and `iv` in the same format as Bark's Node.js GCM example. You can paste `https://api.day.app/<your-key>` directly into the Android app, or paste the key by itself.

## Icons and Images

- Bark `icon` and `image` require public URLs.
- This app tries to resolve notification icons from the Play Store Web first.
- If Play resolution fails, you can set a manual icon URL per app.
- Notification images are only forwarded when the original notification already exposes a public `http` or `https` URL.
- If nothing public is available, the push is still sent without an icon.

## Duplicate Notification Timeout

- Duplicate filtering applies only to app notifications.
- The timeout compares normalized notification content instead of `subText` or category noise.
- Grouped apps such as LINE reuse extra content-aware keys so summary and child notifications can collapse into one forward when they describe the same message.
- SMS and call events still use direct delivery, plus a small fallback dedupe window only when the event came from notification fallback.

## Permissions

The app requests:

- Notification access for app notifications
- SMS permissions for direct SMS reads
- Phone state and call log access for direct call events
- Boot completed for app list refresh after restart

Some SMS and call permissions are restricted on certain devices or installers. When that happens, the app falls back to forwarding notifications from the default SMS or phone app instead.
