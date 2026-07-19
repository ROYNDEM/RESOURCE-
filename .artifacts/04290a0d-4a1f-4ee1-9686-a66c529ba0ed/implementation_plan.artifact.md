# Implementation Plan - Fix Google Sign-In Error 10 in Signed APK

Google Sign-In "Error 10" (DEVELOPER_ERROR) when using a signed APK is almost always caused by a missing SHA-1 fingerprint in the Firebase Console for the production keystore. When you build a debug version, Android uses a default debug keystore; when you generate a signed APK, you use your own production keystore, which has a different digital signature.

## User Review Required

> [!IMPORTANT]
> This fix requires you to perform actions in the **Firebase Console**. I cannot do this for you. I will provide the steps to get your release SHA-1 and where to add it.

## Proposed Steps

### Step 1: Get your Release SHA-1 Fingerprint
You need the SHA-1 of the keystore you used to sign the APK.

1.  Open the **Terminal** in Android Studio (at the bottom).
2.  Run the following command (replace path and alias with your actual keystore details):
    ```bash
    keytool -list -v -keystore "PATH_TO_YOUR_KEYSTORE" -alias YOUR_KEYSTORE_ALIAS
    ```
3.  When prompted, enter your keystore password.
4.  Locate the **SHA1** line and copy the long string of hex characters (e.g., `5E:8F:..:..`).

### Step 2: Add SHA-1 to Firebase Console
1.  Go to the [Firebase Console](https://console.firebase.google.com/).
2.  Select your project: **kids-2a3df**.
3.  Click the gear icon (Project Settings) -> **General** tab.
4.  Scroll down to **Your apps** and select the Android app (`com.roy.ngong`).
5.  Click **Add fingerprint**.
6.  Paste your **Release SHA-1** here and click **Save**.

### Step 3: Update google-services.json
1.  After saving the fingerprint, download the new `google-services.json` file from the same settings page.
2.  Replace the existing `app/google-services.json` in your project with this new one.

### Step 4: Add SHA-256 (Optional but Recommended)
For some features (like Smart Lock or phone auth), you also need the SHA-256 fingerprint. Follow the same steps as above but copy the **SHA256** line instead.

## Verification Plan

### Manual Verification
1.  Generate a **new signed APK** after updating `google-services.json`.
2.  Install it on a device.
3.  Attempt to sign in with Google. It should now work without Error 10.
