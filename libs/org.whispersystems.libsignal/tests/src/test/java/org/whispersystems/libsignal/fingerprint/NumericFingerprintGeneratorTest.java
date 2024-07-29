package org.whispersystems.libsignal.fingerprint;

import android.util.Log;

import junit.framework.TestCase;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECKeyPair;
import org.whispersystems.libsignal.util.Hex;

import java.util.Arrays;

public class NumericFingerprintGeneratorTest extends TestCase {

  private static final String TAG = NumericFingerprintGeneratorTest.class.getSimpleName();

  private static final byte[] ALICE_IDENTITY              = {(byte) 0x05, (byte) 0x06, (byte) 0x86, (byte) 0x3b, (byte) 0xc6, (byte) 0x6d, (byte) 0x02, (byte) 0xb4, (byte) 0x0d, (byte) 0x27, (byte) 0xb8, (byte) 0xd4, (byte) 0x9c, (byte) 0xa7, (byte) 0xc0, (byte) 0x9e, (byte) 0x92, (byte) 0x39, (byte) 0x23, (byte) 0x6f, (byte) 0x9d, (byte) 0x7d, (byte) 0x25, (byte) 0xd6, (byte) 0xfc, (byte) 0xca, (byte) 0x5c, (byte) 0xe1, (byte) 0x3c, (byte) 0x70, (byte) 0x64, (byte) 0xd8, (byte) 0x68};
  private static final byte[] BOB_IDENTITY                = {(byte) 0x05, (byte) 0xf7, (byte) 0x81, (byte) 0xb6, (byte) 0xfb, (byte) 0x32, (byte) 0xfe, (byte) 0xd9, (byte) 0xba, (byte) 0x1c, (byte) 0xf2, (byte) 0xde, (byte) 0x97, (byte) 0x8d, (byte) 0x4d, (byte) 0x5d, (byte) 0xa2, (byte) 0x8d, (byte) 0xc3, (byte) 0x40, (byte) 0x46, (byte) 0xae, (byte) 0x81, (byte) 0x44, (byte) 0x02, (byte) 0xb5, (byte) 0xc0, (byte) 0xdb, (byte) 0xd9, (byte) 0x6f, (byte) 0xda, (byte) 0x90, (byte) 0x7b};
  private static final String DISPLAYABLE_FINGERPRINT     = "300354477692869396892869876765458257569162576843440918079131";
  private static final byte[] ALICE_SCANNABLE_FINGERPRINT = new byte[]{(byte)0x08, (byte)0x01, (byte)0x12, (byte)0x22, (byte)0x0a, (byte)0x20, (byte)0x1e, (byte)0x30, (byte)0x1a, (byte)0x03, (byte)0x53, (byte)0xdc, (byte)0xe3, (byte)0xdb, (byte)0xe7, (byte)0x68, (byte)0x4c, (byte)0xb8, (byte)0x33, (byte)0x6e, (byte)0x85, (byte)0x13, (byte)0x6c, (byte)0xdc, (byte)0x0e, (byte)0xe9, (byte)0x62, (byte)0x19, (byte)0x49, (byte)0x4a, (byte)0xda, (byte)0x30, (byte)0x5d, (byte)0x62, (byte)0xa7, (byte)0xbd, (byte)0x61, (byte)0xdf, (byte)0x1a, (byte)0x22, (byte)0x0a, (byte)0x20, (byte)0xd6, (byte)0x2c, (byte)0xbf, (byte)0x73, (byte)0xa1, (byte)0x15, (byte)0x92, (byte)0x01, (byte)0x5b, (byte)0x6b, (byte)0x9f, (byte)0x16, (byte)0x82, (byte)0xac, (byte)0x30, (byte)0x6f, (byte)0xea, (byte)0x3a, (byte)0xaf, (byte)0x38, (byte)0x85, (byte)0xb8, (byte)0x4d, (byte)0x12, (byte)0xbc, (byte)0xa6, (byte)0x31, (byte)0xe9, (byte)0xd4, (byte)0xfb, (byte)0x3a, (byte)0x4d};
  private static final byte[] BOB_SCANNABLE_FINGERPRINT   = new byte[]{(byte)0x08, (byte)0x01, (byte)0x12, (byte)0x22, (byte)0x0a, (byte)0x20, (byte)0xd6, (byte)0x2c, (byte)0xbf, (byte)0x73, (byte)0xa1, (byte)0x15, (byte)0x92, (byte)0x01, (byte)0x5b, (byte)0x6b, (byte)0x9f, (byte)0x16, (byte)0x82, (byte)0xac, (byte)0x30, (byte)0x6f, (byte)0xea, (byte)0x3a, (byte)0xaf, (byte)0x38, (byte)0x85, (byte)0xb8, (byte)0x4d, (byte)0x12, (byte)0xbc, (byte)0xa6, (byte)0x31, (byte)0xe9, (byte)0xd4, (byte)0xfb, (byte)0x3a, (byte)0x4d, (byte)0x1a, (byte)0x22, (byte)0x0a, (byte)0x20, (byte)0x1e, (byte)0x30, (byte)0x1a, (byte)0x03, (byte)0x53, (byte)0xdc, (byte)0xe3, (byte)0xdb, (byte)0xe7, (byte)0x68, (byte)0x4c, (byte)0xb8, (byte)0x33, (byte)0x6e, (byte)0x85, (byte)0x13, (byte)0x6c, (byte)0xdc, (byte)0x0e, (byte)0xe9, (byte)0x62, (byte)0x19, (byte)0x49, (byte)0x4a, (byte)0xda, (byte)0x30, (byte)0x5d, (byte)0x62, (byte)0xa7, (byte)0xbd, (byte)0x61, (byte)0xdf};

  public void testVectors() throws Exception {
    IdentityKey aliceIdentityKey = new IdentityKey(ALICE_IDENTITY, 0);
    IdentityKey bobIdentityKey   = new IdentityKey(BOB_IDENTITY, 0);

    NumericFingerprintGenerator generator        = new NumericFingerprintGenerator(5200);
    Fingerprint                 aliceFingerprint = generator.createFor("+14152222222", aliceIdentityKey,
                                                                       "+14153333333", bobIdentityKey);

    Fingerprint bobFingerprint = generator.createFor("+14153333333", bobIdentityKey,
                                                     "+14152222222", aliceIdentityKey);

    assertEquals(aliceFingerprint.getDisplayableFingerprint().getDisplayText(), DISPLAYABLE_FINGERPRINT);
    assertEquals(bobFingerprint.getDisplayableFingerprint().getDisplayText(), DISPLAYABLE_FINGERPRINT);

    assertTrue(Arrays.equals(aliceFingerprint.getScannableFingerprint().getSerialized(), ALICE_SCANNABLE_FINGERPRINT));
    assertTrue(Arrays.equals(bobFingerprint.getScannableFingerprint().getSerialized(), BOB_SCANNABLE_FINGERPRINT));
  }

  public void testMatchingFingerprints() throws FingerprintVersionMismatchException, FingerprintIdentifierMismatchException, FingerprintParsingException {
    ECKeyPair aliceKeyPair = Curve.generateKeyPair();
    ECKeyPair bobKeyPair   = Curve.generateKeyPair();

    IdentityKey aliceIdentityKey = new IdentityKey(aliceKeyPair.getPublicKey());
    IdentityKey bobIdentityKey   = new IdentityKey(bobKeyPair.getPublicKey());

    NumericFingerprintGenerator generator        = new NumericFingerprintGenerator(1024);
    Fingerprint                 aliceFingerprint = generator.createFor("+14152222222", aliceIdentityKey,
                                                                       "+14153333333", bobIdentityKey);

    Fingerprint bobFingerprint = generator.createFor("+14153333333", bobIdentityKey,
                                                     "+14152222222", aliceIdentityKey);

    assertEquals(aliceFingerprint.getDisplayableFingerprint().getDisplayText(),
                 bobFingerprint.getDisplayableFingerprint().getDisplayText());

    assertTrue(aliceFingerprint.getScannableFingerprint().compareTo(bobFingerprint.getScannableFingerprint().getSerialized()));
    assertTrue(bobFingerprint.getScannableFingerprint().compareTo(aliceFingerprint.getScannableFingerprint().getSerialized()));

    assertEquals(aliceFingerprint.getDisplayableFingerprint().getDisplayText().length(), 60);
  }

  public void testMismatchingFingerprints() throws FingerprintVersionMismatchException, FingerprintIdentifierMismatchException, FingerprintParsingException {
    ECKeyPair aliceKeyPair = Curve.generateKeyPair();
    ECKeyPair bobKeyPair   = Curve.generateKeyPair();
    ECKeyPair mitmKeyPair  = Curve.generateKeyPair();

    IdentityKey aliceIdentityKey = new IdentityKey(aliceKeyPair.getPublicKey());
    IdentityKey bobIdentityKey   = new IdentityKey(bobKeyPair.getPublicKey());
    IdentityKey mitmIdentityKey  = new IdentityKey(mitmKeyPair.getPublicKey());

    NumericFingerprintGenerator generator        = new NumericFingerprintGenerator(1024);
    Fingerprint                 aliceFingerprint = generator.createFor("+14152222222", aliceIdentityKey,
                                                                       "+14153333333", mitmIdentityKey);

    Fingerprint bobFingerprint = generator.createFor("+14153333333", bobIdentityKey,
                                                     "+14152222222", aliceIdentityKey);

    assertNotSame(aliceFingerprint.getDisplayableFingerprint().getDisplayText(),
                  bobFingerprint.getDisplayableFingerprint().getDisplayText());

    assertFalse(aliceFingerprint.getScannableFingerprint().compareTo(bobFingerprint.getScannableFingerprint().getSerialized()));
    assertFalse(bobFingerprint.getScannableFingerprint().compareTo(aliceFingerprint.getScannableFingerprint().getSerialized()));
  }

  public void testMismatchingIdentifiers() throws FingerprintVersionMismatchException, FingerprintParsingException {
    ECKeyPair aliceKeyPair = Curve.generateKeyPair();
    ECKeyPair bobKeyPair   = Curve.generateKeyPair();

    IdentityKey aliceIdentityKey = new IdentityKey(aliceKeyPair.getPublicKey());
    IdentityKey bobIdentityKey   = new IdentityKey(bobKeyPair.getPublicKey());

    NumericFingerprintGenerator generator        = new NumericFingerprintGenerator(1024);
    Fingerprint                 aliceFingerprint = generator.createFor("+141512222222", aliceIdentityKey,
                                                                       "+14153333333", bobIdentityKey);

    Fingerprint bobFingerprint = generator.createFor("+14153333333", bobIdentityKey,
                                                     "+14152222222", aliceIdentityKey);

    assertNotSame(aliceFingerprint.getDisplayableFingerprint().getDisplayText(),
                  bobFingerprint.getDisplayableFingerprint().getDisplayText());

    assertFalse(aliceFingerprint.getScannableFingerprint().compareTo(bobFingerprint.getScannableFingerprint().getSerialized()));
    assertFalse(bobFingerprint.getScannableFingerprint().compareTo(aliceFingerprint.getScannableFingerprint().getSerialized()));
  }

}
