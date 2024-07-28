## Translations

Please do not submit issues or pull requests for translation fixes. Anyone can update the translations in [Weblate](https://translate.silence.dev).
Please submit your corrections there.


## Submitting bug reports

1. Search our issues first to make sure this is not a duplicate.
2. (Optional) Search [Signal's issues](https://github.com/WhisperSystems/Signal-Android/issues).
3. Open an issue and follow the template carefully. If you can't get a debug log from Settings, you can use ADB to grab it: `adb logcat | grep $(adb shell ps | grep org.smssecure.smssecure | tr -s " " | cut -d " " -f2)`.

## Submitting merge requests

All useful MRs are accepted. Please respect [our template](https://git.silence.dev/Silence/Silence-Android/blob/master/.gitlab/merge_request_templates/Merge Request.md) and ask to merge your commits in `unstable` (PRs in `master` will be closed).
