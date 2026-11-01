# Blackjack AI Analyzer (free starter APK project)

## Build
This project uses Kotlin, Android ML Kit on-device text recognition, and local SharedPreferences history.

1. Upload this folder's contents to a GitHub repository.
2. Open Actions and run "Build Blackjack APK" (or push to main).
3. Download the `BlackjackAIAnalyzer-debug` artifact from the completed workflow.
4. Extract `app-debug.apk` and install it on Android (allow installation from that browser/file manager if Android asks).

## Notes
- OCR is on-device; no OCR API key is needed.
- OCR extracts visible rank-like text. It cannot reliably identify which hand belongs to Player vs Dealer, so verify/edit both fields before calculating.
- The included Hit/Stand logic is a simplified educational heuristic, not a full mathematically optimal strategy. Results depend on rules and number of decks.
- This is a debug APK for testing, not a Play Store release.
