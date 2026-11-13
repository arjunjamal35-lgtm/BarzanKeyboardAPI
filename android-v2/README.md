# Barzan Keyboard — بەرزان کیبۆرد

Native Android IME starter, Kotlin, min Android 8.0. Offline Kurdish Sorani keyboard with numeric row, emoji panel, light/dark mode, and HTTPS AI hooks.

## Build
Open this directory in Android Studio (JDK 17), let Gradle sync, then Build > Build APK(s). No APK is included; this environment does not contain an Android SDK/Gradle wrapper.

## Enable
Open Barzan Keyboard launcher app -> Enable keyboard -> Select Barzan Keyboard. Android warns when enabling third-party keyboards; review keyboard trust before enabling.

## AI API
Configure an HTTPS endpoint in the launcher settings. Select text in an editor, then press correction or translation. Only selected text is sent on button press. The endpoint must accept POST JSON:
`{"action":"correct"|"translate","text":"...","targetLanguage":"en"}`
and return `{"result":"..."}`. Implement authentication, rate limits and secure backend AI processing on your own server. Do not embed provider secrets in the app.

## Limitations
- This is a first functional source-code prototype, not a production-ready keyboard.
- No online AI server is bundled; translation and correction need a configured endpoint.
- Emoji appearance depends on Android device fonts. Kurdistan flag is represented by a sun motif; it is not guaranteed as a standard Unicode emoji.
- Layout approximates the supplied screenshot and should be device-tested and tuned.
- Advanced IME features (composing, autocorrect dictionary, predictive suggestions, secure input-type handling, accessibility, full keyboard testing) require further work.
