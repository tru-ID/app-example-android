# sample-app-android

[![License][license-image]][license-url]


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

## References

- [tru.ID example node.js dev server]((https://github.com/tru-ID/dev-server)

## Meta

Distributed under the MIT license. See [LICENSE][license-url] for more information.

[https://github.com/tru-ID](https://github.com/tru-ID)

[license-image]: https://img.shields.io/badge/License-MIT-blue.svg
[license-url]: LICENSE.md
