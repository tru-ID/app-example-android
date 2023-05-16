
# tru.ID Android PhoneCheck Example

[![License][license-image]][license-url]

This repository is an example integration and usage of [tru.ID's SDK](https://github.com/tru-ID/tru-sdk-android) in an Android mobile application. In order to verify first that tru.ID has coverage of the mobile network operator being used, and that the phone number and SIM card match, this application uses two tru.ID APIs:

- tru.ID [Coverage API](https://developer.tru.id/docs/reference/utils#tag/coverage/operation/get-coverage-by-device-ip)
- tru.ID [PhoneCheck API](https://developer.tru.id/docs/phone-check/integration)

The Coverage API determines whether the mobile network that a device is connected to is supported with tru.ID, and that the Products are available as well. This check is all carried out based on the sender's device IP Address. This API should be called first, because if the MNO is not supported, then you would be able to fallback to whatever other method of verification for the phone number that you use.

The PhoneCheck API confirms the ownership of a mobile phone number by verifying the possession of an active SIM card with the same number. Using a mobile data session, a PhoneCheck is created with a unique Check URL. tru.ID then resolves a match between the phone number that the mobile network operator identifies as the owner of the mobile data session and the phone number.

## Before you begin

You will need:

- Android capable IDE e.g. [Android Studio](https://developer.android.com/studio)
- A [tru.ID Account](https://tru.id)
- The Node.js installed and the latest version of the [tru.ID example dev server](https://github.com/tru-ID/dev-server) running locally
    - Follow the instructions within the tru.ID example node.js server README
    - A local tunnel solution such as [ngrok](https://ngrok.com/) The default `dev-server` port is 8080.
        - `ngrok http 8080`
- An Android phone with a SIM card and mobile data connection

## Getting Started

- Clone this repository into a directory: `git clone git@github.com:tru-ID/app-example-android.git`
- Open the project with your Android Capable IDE
- Once you have your server up and running make a copy of the `app/tru.properties.example` file `cp app/tru.properties.example app/tru.properties` and update the configuration value to be the URL of your example server.
    - `tru.properties`:
        ```
        EXAMPLE_SERVER_BASE_URL="<YOUR_NGROK_URL>"
        ```
- Connect your phone to your computer so it's used for running the PhoneCheckExample application
- Pair your phone to your computer, an active data connection on the phone is required for this application
- Run the application from your IDE
- Enter the phone number for the mobile device in the UI in the format +{country_code}{number} e.g. `+447900123456`
- Press the done keyboard key or touch the "Verify my phone number" button
- You will see the result of the Phone Check
- Get in touch: please email feedback@tru.id with any questions

## How can I use tru.ID in my Android application?

tru.ID have an [Android SDK](https://github.com/tru-ID/tru-sdk-android) that you can install into your Android application to make the implementation easier. This SDK also provides functionality to force a cellular data connection through native Android APIs.

To begin with, add the public Maven repository to your Android IDE:

- `https://gitlab.com/api/v4/projects/22035475/packages/maven`

Then in your `build.gradle` add the following dependency:

- `implementation 'id.tru.sdk:tru-sdk-android:x.y.z'`

> **Note** Replace `x.y.z` with the latest version, which can be found on the [GitHub repository](https://github.com/tru-ID/tru-sdk-android/tags).

Usage examples can be found also within the Android SDK [GitHub Repository](https://github.com/tru-ID/tru-sdk-android#usage-example).

## References

- [tru.ID example node.js dev server]((https://github.com/tru-ID/dev-server)
- [tru.ID Android SDK](https://github.com/tru-ID/tru-sdk-android)

## Meta

Distributed under the MIT license. See [LICENSE][license-url] for more information.

[https://github.com/tru-ID](https://github.com/tru-ID)

[license-image]: https://img.shields.io/badge/License-MIT-blue.svg
[license-url]: LICENSE.md
