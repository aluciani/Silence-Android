/** 
 * Copyright (C) 2011 Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.smssecure.smssecure.crypto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

/**
 * A class for streaming an encrypted MMS "part" to disk.
 *
 * @author Moxie Marlinspike
 */

public class EncryptingPartOutputStream extends FileOutputStream {

  private static final String TAG = EncryptingPartOutputStream.class.getSimpleName();

  private Cipher        cipher;
  private Mac           mac;
  private MessageDigest messageDigest;

  private boolean closed;

  private byte[] digest;

  public EncryptingPartOutputStream(File file, MasterSecret masterSecret) throws FileNotFoundException {
    super(file);

    try {
      this.cipher        = initializeCipher();
      this.mac           = initializeMac();
      this.messageDigest = MessageDigest.getInstance("SHA256");

      this.cipher.init(Cipher.ENCRYPT_MODE, masterSecret.getEncryptionKey());
      this.mac.init(masterSecret.getMacKey());

      mac.update(cipher.getIV());
      messageDigest.update(cipher.getIV());

      super.write(cipher.getIV(), 0, cipher.getIV().length);

      closed = false;
    } catch (IOException ioe) {
      Log.w(TAG, ioe);
      throw new FileNotFoundException("Couldn't write IV");
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public void write(byte[] buffer) throws IOException {
    this.write(buffer, 0, buffer.length);
  }

  @Override
  public void write(byte[] buffer, int offset, int length) throws IOException {
    byte[] encryptedBuffer = cipher.update(buffer, offset, length);

    if (encryptedBuffer != null) {
      mac.update(encryptedBuffer);
      messageDigest.update(encryptedBuffer);
      super.write(encryptedBuffer, 0, encryptedBuffer.length);
    }
  }

  @Override
  public void close() throws IOException {
    try {
      if (!closed) {
        byte[] encryptedRemainder = cipher.doFinal();
        mac.update(encryptedRemainder);

        byte[] macBytes = mac.doFinal();

        messageDigest.update(encryptedRemainder);
        this.digest = messageDigest.digest(macBytes);

        super.write(encryptedRemainder, 0, encryptedRemainder.length);
        super.write(macBytes, 0, macBytes.length);

        closed = true;
      }

      super.close();
    } catch (BadPaddingException bpe) {
      throw new AssertionError(bpe);
    } catch (IllegalBlockSizeException e) {
      throw new AssertionError(e);
    }
  }

  public byte[] getAttachmentDigest() {
    return digest;
  }

  private Mac initializeMac() {
    try {
      return Mac.getInstance("HmacSHA1");
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }
  }

  private Cipher initializeCipher() {
    try {
      return Cipher.getInstance("AES/CBC/PKCS5Padding");
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new AssertionError(e);
    }
  }

}
