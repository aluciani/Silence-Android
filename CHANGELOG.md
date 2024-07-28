# Silence Changelog

### [0.15.16] - 2019-08-28
- Improved local encryption
- Fixed duplicated MMS settings
- Added notifications channels
- Updated translations

### [0.15.15] - 2019-08-23
- Fixed infinite loop when sending MMS messages
- Fixed crash when receiving encrypted MMS message
- Updated translations

### [0.15.14] - 2019-08-17
- Improved MMS support and quality of scaled images
- Added Android Auto support
- Added UI fixes on failed messages
- Added selective permissions
- Improved welcome screen
- Added option to always ask for the SIM card to use to send a message
- Updated translations

### [0.15.13] - 2018-03-23
- Fixed logs sending
- Added option to share key fingerprint
- Minor UI fixes
- Updated translations

### [0.15.12] - 2018-02-19
- Added option to hide "new message" separator
- Updated APNs with fix for Free Mobile (FR)
- Switched to a new way to receive encrypted messages (started in v0.15.9)
- Updated translations

### [0.15.11] - 2018-01-04
- Fixed persistent crash when downloading some MMS messages
- Updated translations

### [0.15.10] - 2017-11-30
- Fixed crash on some devices when exporting data
- Added fallback to ROM's emojis
- Updated translations

### [0.15.9] - 2017-11-20
- Switched to a new way to send encrypted messages
- Updated translations

### [0.15.8] - 2017-10-05
- Updated emojis to Android O
- Improved build reproducibility
- Fixed crash on malformed messages
- Enabled incognito keyboard mode by default
- Removed cleartext password that may leak
- Enabled Czech and Portuguese (Brazil) languages
- Updated translations

### [0.15.7] - 2017-04-21
- Fixed crash on malformed encrypted messages
- Updated translations

### [0.15.6] - 2017-03-29
- Moved from Axolotl to Signal protocol
- Fixed crash when opening a conversation
- Added fallback to legacy MMS API on failure
- Added minor UI improvements
- Fixed minor bugs
- Removed dead code
- Updated translations

### [0.15.5] - 2017-03-16
- Fixed glitch with "Secure session ended" items
- Updated translations

### [0.15.4] - 2017-03-14
- Fixed build
- Updated translations

### [0.15.3] - 2017-03-14
- Added link to our Privacy policy in Settings
- Added "New messages" divider
- Linkified geo: and xmpp: URIs
- Fixed occasional crash on Android 7 quick reply
- Updated translations

### [0.15.2] - 2017-02-22
- Fixed button to download MMS messages
- Updated translations

### [0.15.1] - 2017-02-19
- Fixed build

### [0.15.0] - 2017-02-18
- Added missing emojis flags
- Added widget with unread messages count
- Added support for image keyboard
- Added Android N bundled notifications and quick reply
- Added support for direct share targets
- Added sticky date headers
- Added scroll-to-bottom button
- Updated translations
- Minor bugs fixes

### [0.14.8] - 2016-12-30
- Fixed emoji's support with Android <= 6.1
- Updated emojis, added flags
- Updated translations

### [0.14.7] - 2016-12-28
- Fixed issues with vibration of notifications when Silence is unlocked
- Added button to send drafts from main screen
- Updated emojis
- Updated translations

### [0.14.6] - 2016-12-06
- Fixed notifications when Silence is locked

### [0.14.5] - 2016-12-04
- Fixed notification lights with encrypted messages
- Updated translations

### [0.14.4] - 2016-11-29
- Fixed notifications with Android N
- Prepared for upcoming XMPP transport
- Added reminder to enable delivery reports
- Fixed privacy settings with Android < 5
- Fixed SMS characters calculation
- Updated APN database
- Updated translations
- Fixed lots of minor bugs

### [0.14.3] - 2016-05-14
- Fixed blocked MMS download

### [0.14.2] - 2016-05-13
- Fixed MMS issues with Android 4.x
- Updated translations

### [0.14.1] - 2016-04-25
- Fixed APN issues
- Updated translations

### [0.14.0] - 2016-04-23
- SMSSecure is now Silence
- Added new emojis
- Added support for multi-SIM phones
- Added encrypted backups
- Fixed strings
- Updated APN database
- Updated translations

### [0.13.2] - 2016-02-07
- Fixed keyboard/focus regressions
- Fixed debug logs
- Fixed click interception on failed messages
- Fixed silent in-thread notification
- Fixed notification LED and sound
- Fixed contact sorting
- Fixed crash on invalid image
- Fixed database migration from 0.12.3 to 0.13.1
- Updated APN database
- Added Esperanto translation
- Updated translations

### [0.13.1] - 2016-01-20
- Fixed crash on Gingerbread
- Fixed some issues with RTL languages
- Updated translations

### [0.13.0] - 2015-12-23
- Added support for archive actions
- Improved UI
- Improved MMS support for Android 5+
- Fixed lots of bugs

### [0.12.3] - 2015-10-08
- Improved settings
- Fixed bugs
- Updated translations

### [0.12.1] - 2015-10-07
- Added MMS download controls
- Improved MMS requests
- Fixed bugs
- Updated translations

### [0.11.3] - 2015-09-23
- Fixed bugs
- Updated translations

### [0.11.0] - 2015-09-15
- Added GIF support
- Added new options
- Fixed bugs
- Updated translations

### [0.10.1] - 2015-07-16
- Added a new, more "material" UI
- Added support for per-contact options
- Fixed bugs
- Updated translations

### [0.9.0] - 2015-06-02
- Added support for direct photo capture
- Fixed ANR on certain devices
- Updated emoji set
- Fixed bugs
- Updated translations

### [0.8.1] - 2015-05-13
- Fixed manual key exchange completion
- Fixed occasional generated avatar mis-sizing in conversation
- Updated translations

### [0.8.0] - 2015-05-12
- Added a generated avatar for contacts without pictures
- Fixed emoji drawer bugs
- Fixed crash in AppCompat

### [0.7.0] - 2015-05-07
- Added support for Android Wear
- Added support for Lollipop-style notifications
- Improved encrypted message detection (for using as non-default SMS app)
- Fixed bugs

### [0.6.0] - 2015-04-14
- Fixed key exchanges being blue while pending
- Fixed crash when username was null in MMS auth
- Improved handling of requests to end non-existing sessions
- Updated translations

### [0.5.4] - 2015-04-09
- Removed `READ_CALL_LOG` permission
- Fixed Norwegian localization issues
- Fixed problem with upgrading the database

### [0.5.3] - 2015-04-07
- Fixed crash on upgrading

### [0.5.2] - 2015-04-05
- Fixed bugs

### [0.5.1] - 2015-04-02
- Removed more push-related code
- Fixed MMS crash

### [0.4.2] - 2015-03-31
- Added ability to import TextSecure backups

### [0.4.1] - 2015-03-31
- Added new icon

### [0.4.0] - 2015-03-30
- Fixed bugs
- Removed TextSecure push-related code and strings

### [0.3.3] - 2015-03-22
- Renamed project to SMSSecure at the request of Moxie (TextSecure dev)

### [0.3.2] - 2015-03-21
- Fixed crash

### [0.3.1] - 2015-03-20
- Added the ability to install SecuredText alongside TextSecure

### [0.3.0] - 2015-03-19
- Fixed bugs

### [0.2.0] - 2015-03-19
- Initial fork
- Changed app name
- Removed non-free libraries

 [0.15.16]: https://git.silence.dev/Silence/Silence-Android/compare/v0.15.15...v0.15.16
 [0.15.15]: https://git.silence.dev/Silence/Silence-Android/compare/v0.15.14...v0.15.15
 [0.15.14]: https://git.silence.dev/Silence/Silence-Android/compare/v0.15.13...v0.15.14
 [0.15.13]: https://git.silence.dev/Silence/Silence-Android/compare/v0.15.12...v0.15.13
 [0.15.12]: https://git.silence.dev/Silence/Silence-Android/compare/v0.15.11...v0.15.12
 [0.15.11]: https://git.silence.dev/Silence/Silence-Android/compare/v0.15.10...v0.15.11
 [0.15.10]: https://git.silence.dev/Silence/Silence-Android/compare/v0.15.9...v0.15.10
 [0.15.9]: https://git.silence.dev/Silence/Silence-Android/compare/v0.15.8...v0.15.9
 [0.15.8]: https://git.silence.dev/Silence/Silence-Android/compare/v0.15.7...v0.15.8
 [0.15.7]: https://git.silence.dev/Silence/Silence-Android/compare/v0.15.6...v0.15.7
 [0.15.6]: https://git.silence.dev/Silence/Silence-Android/compare/v0.15.5...v0.15.6
 [0.15.5]: https://git.silence.dev/Silence/Silence-Android/compare/v0.15.4...v0.15.5
 [0.15.4]: https://git.silence.dev/Silence/Silence-Android/compare/v0.15.3...v0.15.4
 [0.15.3]: https://git.silence.dev/Silence/Silence-Android/compare/v0.15.2...v0.15.3
 [0.15.2]: https://git.silence.dev/Silence/Silence-Android/compare/v0.15.1...v0.15.2
 [0.15.1]: https://git.silence.dev/Silence/Silence-Android/compare/v0.15.0...v0.15.1
 [0.15.0]: https://git.silence.dev/Silence/Silence-Android/compare/v0.14.8...v0.15.0
 [0.14.8]: https://git.silence.dev/Silence/Silence-Android/compare/v0.14.7...v0.14.8
 [0.14.7]: https://git.silence.dev/Silence/Silence-Android/compare/v0.14.6...v0.14.7
 [0.14.6]: https://git.silence.dev/Silence/Silence-Android/compare/v0.14.5...v0.14.6
 [0.14.5]: https://git.silence.dev/Silence/Silence-Android/compare/v0.14.4...v0.14.5
 [0.14.4]: https://git.silence.dev/Silence/Silence-Android/compare/v0.14.3...v0.14.4
 [0.14.3]: https://git.silence.dev/Silence/Silence-Android/compare/v0.14.2...v0.14.3
 [0.14.2]: https://git.silence.dev/Silence/Silence-Android/compare/v0.14.1...v0.14.2
 [0.14.1]: https://git.silence.dev/Silence/Silence-Android/compare/v0.14.0...v0.14.1
 [0.14.0]: https://git.silence.dev/Silence/Silence-Android/compare/v0.13.2...v0.14.0
 [0.13.2]: https://git.silence.dev/Silence/Silence-Android/compare/v0.13.1...v0.13.2
 [0.13.1]: https://git.silence.dev/Silence/Silence-Android/compare/v0.13.0...v0.13.1
 [0.13.0]: https://git.silence.dev/Silence/Silence-Android/compare/v0.12.3...v0.13.0
 [0.12.3]: https://git.silence.dev/Silence/Silence-Android/compare/v0.12.1...v0.12.3
 [0.12.1]: https://git.silence.dev/Silence/Silence-Android/compare/v0.11.3...v0.12.1
 [0.11.3]: https://git.silence.dev/Silence/Silence-Android/compare/v0.11.1...v0.11.3
 [0.11.0]: https://git.silence.dev/Silence/Silence-Android/compare/v0.10.1...v0.11.1
 [0.10.1]: https://git.silence.dev/Silence/Silence-Android/compare/v0.9.0...v0.10.1
 [0.9.0]: https://git.silence.dev/Silence/Silence-Android/compare/v0.8.1...v0.9.0
 [0.8.1]: https://git.silence.dev/Silence/Silence-Android/compare/v0.8.0...v0.8.1
 [0.8.0]: https://git.silence.dev/Silence/Silence-Android/compare/v0.7.0...v0.8.0
 [0.7.0]: https://git.silence.dev/Silence/Silence-Android/compare/v0.6.0...v0.7.0
 [0.6.0]: https://git.silence.dev/Silence/Silence-Android/compare/v0.5.4...v0.6.0
 [0.5.4]: https://git.silence.dev/Silence/Silence-Android/compare/v0.5.3...v0.5.4
 [0.5.3]: https://git.silence.dev/Silence/Silence-Android/compare/v0.5.2...v0.5.3
 [0.5.2]: https://git.silence.dev/Silence/Silence-Android/compare/v0.5.1...v0.5.2
 [0.5.1]: https://git.silence.dev/Silence/Silence-Android/compare/v0.4.2...v0.5.1
 [0.4.2]: https://git.silence.dev/Silence/Silence-Android/compare/v0.4.1...v0.4.2
 [0.4.1]: https://git.silence.dev/Silence/Silence-Android/compare/v0.4.0...v0.4.1
 [0.4.0]: https://git.silence.dev/Silence/Silence-Android/compare/v0.3.3...v0.4.0
 [0.3.3]: https://git.silence.dev/Silence/Silence-Android/compare/v0.3.2...v0.3.3
 [0.3.2]: https://git.silence.dev/Silence/Silence-Android/compare/v0.3.1...v0.3.2
 [0.3.1]: https://git.silence.dev/Silence/Silence-Android/compare/v0.3.0...v0.3.1
 [0.3.0]: https://git.silence.dev/Silence/Silence-Android/compare/v0.2.0...v0.3.0
 [0.2.0]: https://git.silence.dev/Silence/Silence-Android/compare/ac92fa6f5e1f86da833b38aa5955b685e1959846...v0.2.0
