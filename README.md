# sample-app-android

[![License][license-image]][license-url]


## Before you being

You will need:

- Android capable IDE e.g. [Android Studio](https://developer.android.com/studio)
- A [tru.ID Account](https://tru.id)
- The [Server Example](https://github.com/tru-ID/server-example-node) & Node.JS installed
    - Follow the instructions within the 4Auth Node Server README
    - A local tunnel solution such as [ngrok](https://ngrok.com/) when using a real mobile device
    - The URL to the [Server Example](https://github.com/tru-ID/server-example-node) is configured via a variable named `AUTH_ENDPOINT`. It defaults to `http://10.0.2.2:4040`. Update to the ngrok URL.
- An Android phone with a SIM card and mobile data connection

## Getting Started

- Clone or unzip the PhoneCheckExample into a directory.
- Open the project with your Android Capable IDE
- Connect your phone to your computer so it's used for running the PhoneCheckExample application
- Turn off WiFi on your mobile device
- Run the application from your IDE
- Enter the phone number for the mobile device in the UI
- Press the done keyboard key or touch the "Verify my phone number" button
- You will see the result of the Phone Check
- Get in touch: please email feedback@tru.id with any questions

## Notes


## References

- [4Auth Node Server](https://gitlab.com/4auth/devx/4auth-node-server)
- [Fix Cleartext Traffic Error in Android 9 Pie](https://medium.com/@son.rommer/fix-cleartext-traffic-error-in-android-9-pie-2f4e9e2235e6) to allow HTTPS -> HTTP redirects
    - Quickfix is to add <application android:usesCleartextTraffic="true" ...`
    - But, this isn't secure. The better solutions is outlined in the link.
- [Coroutines and `viewModelScope`](https://developer.android.com/topic/libraries/architecture/coroutines#viewmodelscope) to execute network requests on a background thread
- [Moshi](https://github.com/square/moshi) for JSON serializing and deserializing
- [okhttp](https://square.github.io/okhttp/) for HTTP requests
- [Sending requests over Mobile data when WiFi is ON](https://stackoverflow.com/questions/25931334/send-request-over-mobile-data-when-wifi-is-on-android-l) 

## Meta

Distributed under the MIT license. See ``LICENSE`` for more information.

[https://github.com/tru-ID](https://github.com/tru-ID)

[license-image]: https://img.shields.io/badge/License-MIT-blue.svg
[license-url]: LICENSE