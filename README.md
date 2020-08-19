# 4Auth Phone Check for Android Example

## Quick Notes

- Requires the 4Auth Node Server to be running
- 4Auth Node Server needs to have a public URL. Use something like [Ngrok](https://ngrok.com/)
- The URL to the [4Auth Node Server](https://gitlab.com/4auth/devx/4auth-node-server) will need to be updated within the code (should be moved to config). Search for `LOCAL_ENDPOINT`.

## References

- [4Auth Node Server](https://gitlab.com/4auth/devx/4auth-node-server)
- [Fix Cleartext Traffic Error in Android 9 Pie](https://medium.com/@son.rommer/fix-cleartext-traffic-error-in-android-9-pie-2f4e9e2235e6) to allow HTTPS -> HTTP redirects
    - Quickfix is to add <application android:usesCleartextTraffic="true" ...`
    - But, this isn't secure. The better solutions is outlined in the link.
- [Coroutines and `viewModelScope`](https://developer.android.com/topic/libraries/architecture/coroutines#viewmodelscope) to execute network requests on a background thread
- [Moshi](https://github.com/square/moshi) for JSON serializing and deserializing
- [okhttp](https://square.github.io/okhttp/) for HTTP requests
- [Sending requests over Mobile data when WiFi is ON](https://stackoverflow.com/questions/25931334/send-request-over-mobile-data-when-wifi-is-on-android-l) 