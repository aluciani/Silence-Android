package org.smssecure.smssecure.crypto.storage;

import android.content.Context;

import org.smssecure.smssecure.crypto.IdentityKeyUtil;
import org.smssecure.smssecure.crypto.MasterSecret;
import org.smssecure.smssecure.crypto.SessionUtil;
import org.smssecure.smssecure.database.DatabaseFactory;
import org.smssecure.smssecure.recipients.RecipientFactory;
import org.smssecure.smssecure.util.SilencePreferences;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.IdentityKeyStore;

public class SilenceIdentityKeyStore implements IdentityKeyStore {

  private static final Object LOCK = new Object();

  private final Context      context;
  private final MasterSecret masterSecret;
  private final int          subscriptionId;

  public SilenceIdentityKeyStore(Context context, MasterSecret masterSecret, int subscriptionId) {
    this.context        = context;
    this.masterSecret   = masterSecret;
    this.subscriptionId = subscriptionId;
  }

  @Override
  public IdentityKeyPair getIdentityKeyPair() {
    return IdentityKeyUtil.getIdentityKeyPair(context, masterSecret, subscriptionId);
  }

  @Override
  public int getLocalRegistrationId() {
    return SilencePreferences.getLocalRegistrationId(context);
  }

  @Override
  public boolean saveIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
    synchronized (LOCK) {
      long recipientId = RecipientFactory.getRecipientsFromString(context, address.getName(), true).getPrimaryRecipient().getRecipientId();
      DatabaseFactory.getIdentityDatabase(context).saveIdentity(masterSecret, recipientId, identityKey);
      return true;
    }
  }

  @Override
  public boolean isTrustedIdentity(SignalProtocolAddress address, IdentityKey identityKey, Direction direction) {
    synchronized (LOCK) {
      switch (direction) {
        case SENDING:   return isTrustedIdentity(address, identityKey);
        case RECEIVING: return true;
        default:        throw new AssertionError("Unknown direction: " + direction);
      }
    }
  }

  public boolean isTrustedIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
    long recipientId = RecipientFactory.getRecipientsFromString(context, address.getName(), true).getPrimaryRecipient().getRecipientId();

    boolean trusted = DatabaseFactory.getIdentityDatabase(context)
                                     .isValidIdentity(masterSecret, recipientId, identityKey);

    if (!trusted) {
      new SilenceSessionStore(context, masterSecret, subscriptionId).deleteAllSessions(address.getName());
    }

    return trusted;
  }

  @Override
  public IdentityKey getIdentity(SignalProtocolAddress address) {
    return null;
  }
}
