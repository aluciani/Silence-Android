# emoji-extractor

This bunch of scripts allows you to:

 * Extract emojis from `NotoColorEmoji.ttf` (can be found in Android Source Code)
 * Remove useless margins of extracted emojis
 * Generate sprites from `emoji-categories.xml` with extracted emojis

Note: `gen-sprite.py` will generate a list of emojis included in sprites. To get escaped strings for Java, run `native2ascii -encoding utf8 input.txt output.java.txt`
