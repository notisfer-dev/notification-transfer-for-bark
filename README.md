English | [日本語](README.ja.md)

# Notification Transfer for Bark

Android app that forwards notifications, SMS, and call events to Bark on your iPhone with AES-256-GCM encrypted payloads.

## Screenshots

<table>
  <tr>
    <td align="center">
      <img src="docs/assets/app-setup.png" alt="Initial setup" width="220"><br>
      <sub>Initial setup</sub>
    </td>
    <td align="center">
      <img src="docs/assets/app-bark-settings.png" alt="Bark settings" width="220"><br>
      <sub>Bark settings</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="docs/assets/app-transfer-settings.png" alt="Transfer settings" width="220"><br>
      <sub>Transfer settings</sub>
    </td>
    <td align="center">
      <img src="docs/assets/app-excluded-apps.png" alt="Excluded apps" width="220"><br>
      <sub>Excluded apps</sub>
    </td>
  </tr>
</table>

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

## Build on GitHub

If you want to build the APK on GitHub instead of your local machine:

1. Fork this repository to your own GitHub account.
2. Open the `Actions` tab in your fork and enable workflows if GitHub asks.
3. Open the `Android CI` workflow and click `Run workflow`.
4. When the run finishes, download the `app-debug-apk` artifact.
5. Install the APK from that artifact on your Android device.

`app-debug-apk` is the main installable artifact for fork-based builds. `app-release-unsigned-apk` is also uploaded, but it is only an unsigned reference build and will not install until you add your own signing config.

## Install

- Install `app/build/outputs/apk/debug/app-debug.apk` for local testing.
- If you build on GitHub Actions, download and install the `app-debug-apk` artifact from your workflow run.
- `app-release-unsigned.apk` is intentionally unsigned and will not install until you add your own signing config.

## Notification Access Note for Sideloaded APKs

If you installed the app from an external APK, Android may block notification access until you manually allow restricted settings first.

1. Open the notification access prompt from inside the app.
2. In the Settings app, open the `Notification Transfer` app details page.
3. Open the top-right three-dot menu and allow restricted settings.
4. Go back and enable notification access for the app.

The wording can differ a little depending on your Android version or device vendor, but the flow is the same: allow restricted settings first, then turn on notification access.

## Bark Setup

Configure the Bark iPhone app with:

- Algorithm: `AES256`
- Mode: `GCM`
- Padding: `noPadding`
- Key: same 32-character key entered in the Android app
- IV: same 12-character IV entered in the Android app

<p>
  <table>
    <tr>
      <td align="center" valign="top">
        <a href="https://apps.apple.com/app/bark-custom-notifications/id1403753865">
          <img src="https://is1-ssl.mzstatic.com/image/thumb/PurpleSource211/v4/27/b3/7f/27b37f52-a029-c5ad-8675-fa0cfb935801/Placeholder.mill/400x400bb-75.jpg" alt="Bark app icon from the App Store listing" width="88">
        </a>
        <br>
        <strong>Bark</strong>
        <br>
        <a href="https://apps.apple.com/app/bark-custom-notifications/id1403753865">
          <img src="https://developer.apple.com/assets/elements/badges/download-on-the-app-store.svg" alt="Download on the App Store" height="40">
        </a>
        <br>
        <sub>Bark GitHub repository</sub>
        <br>
        <a href="https://github.com/Finb/Bark">https://github.com/Finb/Bark</a>
      </td>
      <td align="center" valign="top">
        <a href="https://apps.apple.com/app/bark-custom-notifications/id1403753865">
          <img src="docs/assets/bark-app-store-qr.svg" alt="QR code that opens Bark on the App Store" width="140">
        </a>
        <br>
        <sub>Scan the QR code to open Bark on the App Store.</sub>
      </td>
    </tr>
  </table>
</p>

The Android app sends `ciphertext` and `iv` in the same format as Bark's Node.js GCM example. You can paste `https://api.day.app/<your-key>` directly into the Android app, or paste the key by itself. You can also scan the QR code above to open Bark on the App Store.

If you are looking for the encryption settings screen in Bark, tap `Encryption Settings` as shown below.

<img src="docs/assets/bark-encryption-settings-arrow.png" alt="Bark encryption settings entry point with arrow" width="300">

Bark is a separate iOS app by Finb. Special thanks to Bark and its developer Finb for building and sharing the iOS app that makes this workflow possible.

## Icons and Images

- Bark `icon` and `image` require public URLs.
- This app tries to resolve notification icons from the Play Store Web first.
- If Play resolution fails, you can set a manual icon URL per app.
- Notification images are only forwarded when the original notification already exposes a public `http` or `https` URL.
- If nothing public is available, the push is still sent without an icon.
- This repository does not bundle Bark UI screenshots or app artwork.

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

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=notisfer-dev/notification-transfer-for-bark&type=Date)](https://www.star-history.com/#notisfer-dev/notification-transfer-for-bark&Date)

## License

This project is released under the MIT License. See [LICENSE](LICENSE).
