package org.smssecure.smssecure.jobs;

import android.content.Context;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.util.Pair;

import com.google.android.mms.pdu_alt.CharacterSets;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.MultimediaMessagePdu;
import com.google.android.mms.pdu_alt.PduBody;
import com.google.android.mms.pdu_alt.PduPart;
import com.google.android.mms.pdu_alt.RetrieveConf;

import org.smssecure.smssecure.attachments.Attachment;
import org.smssecure.smssecure.attachments.UriAttachment;
import org.smssecure.smssecure.crypto.MasterSecret;
import org.smssecure.smssecure.crypto.MmsCipher;
import org.smssecure.smssecure.crypto.storage.SilenceSignalProtocolStore;
import org.smssecure.smssecure.database.AttachmentDatabase;
import org.smssecure.smssecure.database.DatabaseFactory;
import org.smssecure.smssecure.database.MmsDatabase;
import org.smssecure.smssecure.jobs.requirements.MasterSecretRequirement;
import org.smssecure.smssecure.jobs.requirements.MediaNetworkRequirement;
import org.smssecure.smssecure.mms.ApnUnavailableException;
import org.smssecure.smssecure.mms.CompatMmsConnection;
import org.smssecure.smssecure.mms.IncomingLollipopMmsConnection;
import org.smssecure.smssecure.mms.IncomingMediaMessage;
import org.smssecure.smssecure.mms.IncomingLegacyMmsConnection;
import org.smssecure.smssecure.mms.IncomingMmsConnection;
import org.smssecure.smssecure.mms.MmsRadioException;
import org.smssecure.smssecure.mms.PartParser;
import org.smssecure.smssecure.notifications.MessageNotifier;
import org.smssecure.smssecure.protocol.WirePrefix;
import org.smssecure.smssecure.providers.SingleUseBlobProvider;
import org.smssecure.smssecure.service.KeyCachingService;
import org.smssecure.smssecure.util.dualsim.DualSimUtil;
import org.smssecure.smssecure.util.Hex;
import org.smssecure.smssecure.util.Util;
import org.whispersystems.jobqueue.JobParameters;
import org.whispersystems.jobqueue.requirements.NetworkRequirement;
import org.whispersystems.jobqueue.util.Base64;
import org.whispersystems.libsignal.DuplicateMessageException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.LegacyMessageException;
import org.whispersystems.libsignal.NoSessionException;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.util.guava.Optional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.smssecure.smssecure.mms.MmsException;

public class MmsDownloadJob extends MasterSecretJob {

  private static final String TAG = MmsDownloadJob.class.getSimpleName();

  private final long    messageId;
  private final long    threadId;
  private final boolean automatic;

  public MmsDownloadJob(Context context, long messageId, long threadId, boolean automatic) {
    super(context, JobParameters.newBuilder()
                                .withPersistence()
                                .withRequirement(new MasterSecretRequirement(context))
                                .withRequirement(new NetworkRequirement(context))
                                .withRequirement(new MediaNetworkRequirement(context, messageId, automatic))
                                .withGroupId("mms-operation")
                                .withWakeLock(true, 30, TimeUnit.SECONDS)
                                .create());

    this.messageId = messageId;
    this.threadId  = threadId;
    this.automatic = automatic;
  }

  @Override
  public void onAdded() {
    if (automatic && KeyCachingService.getMasterSecret(context) == null) {
      DatabaseFactory.getMmsDatabase(context).markIncomingNotificationReceived(threadId);
      MessageNotifier.updateNotification(context, null);
    }
  }

  @Override
  public void onRun(MasterSecret masterSecret) {
    MmsDatabase                               database     = DatabaseFactory.getMmsDatabase(context);
    Optional<MmsDatabase.MmsNotificationInfo> notification = database.getNotification(messageId);

    if (!notification.isPresent()) {
      Log.w(TAG, "No notification for ID: " + messageId);
      return;
    }

    try {
      if (notification.get().getContentLocation() == null) {
        throw new MmsException("Notification content location was null.");
      }

      database.markDownloadState(messageId, MmsDatabase.Status.DOWNLOAD_CONNECTING);

      String contentLocation = notification.get().getContentLocation();
      byte[] transactionId   = new byte[0];

      try {
        transactionId = notification.get().getTransactionId().getBytes(CharacterSets.MIMENAME_ISO_8859_1);
      } catch (UnsupportedEncodingException e) {
        Log.w(TAG, e);
      }

      try {
        URI mmsUri = URI.create(contentLocation);
        Log.w(TAG, "Downloading mms at " + mmsUri.getHost());
      } catch (Exception e) {
        throw new MmsException("Invalid content location: "+contentLocation);
      }

      int deviceSubbscriptionId = DualSimUtil.getSubscriptionIdFromDeviceSubscriptionId(context, notification.get().getSubscriptionId());

      RetrieveConf retrieveConf = new CompatMmsConnection(context).retrieve(contentLocation, transactionId, deviceSubbscriptionId);

      if (retrieveConf == null) {
        throw new MmsException("RetrieveConf was null");
      }

      if (retrieveConf.getSubject() != null && WirePrefix.isEncryptedMmsSubject(retrieveConf.getSubject().getString())) {
        MmsCipher            mmsCipher    = new MmsCipher(new SilenceSignalProtocolStore(context, masterSecret, notification.get().getSubscriptionId()));
        MultimediaMessagePdu plaintextPdu = (MultimediaMessagePdu) mmsCipher.decrypt(context, retrieveConf);

        storeRetrievedMms(masterSecret, contentLocation, messageId, threadId, retrieveConf.getFrom(), retrieveConf.getTo(), retrieveConf.getCc(),
                          plaintextPdu.getBody(), retrieveConf.getDate(), true, notification.get().getSubscriptionId());
      } else {
        storeRetrievedMms(masterSecret, contentLocation, messageId, threadId, retrieveConf.getFrom(), retrieveConf.getTo(), retrieveConf.getCc(),
                          retrieveConf.getBody(), retrieveConf.getDate(), false, notification.get().getSubscriptionId());
      }
    } catch (ApnUnavailableException e) {
      Log.w(TAG, e);
      handleDownloadError(masterSecret, messageId, threadId, MmsDatabase.Status.DOWNLOAD_APN_UNAVAILABLE,
                          automatic);
    } catch (FileNotFoundException | MmsException e) {
      Log.w(TAG, e);
      handleDownloadError(masterSecret, messageId, threadId,
                          MmsDatabase.Status.DOWNLOAD_HARD_FAILURE,
                          automatic);
    } catch (MmsRadioException | IOException e) {
      Log.w(TAG, e);
      handleDownloadError(masterSecret, messageId, threadId,
                          MmsDatabase.Status.DOWNLOAD_SOFT_FAILURE,
                          automatic);
    } catch (DuplicateMessageException e) {
      Log.w(TAG, e);
      database.markAsDecryptDuplicate(messageId, threadId);
    } catch (LegacyMessageException e) {
      Log.w(TAG, e);
      database.markAsLegacyVersion(messageId, threadId);
    } catch (NoSessionException | UntrustedIdentityException e) {
      Log.w(TAG, e);
      database.markAsNoSession(messageId, threadId);
    } catch (InvalidMessageException e) {
      Log.w(TAG, e);
      database.markAsDecryptFailed(messageId, threadId);
    }
  }

  @Override
  public void onCanceled() {
    MmsDatabase database = DatabaseFactory.getMmsDatabase(context);
    database.markDownloadState(messageId, MmsDatabase.Status.DOWNLOAD_SOFT_FAILURE);

    if (automatic) {
      database.markIncomingNotificationReceived(threadId);
      MessageNotifier.updateNotification(context, null, threadId);
    }
  }

  @Override
  public boolean onShouldRetryThrowable(Exception exception) {
    return false;
  }

  private void storeRetrievedMms(MasterSecret masterSecret, String contentLocation,
                                 long messageId, long threadId, EncodedStringValue pduFrom, 
                                 EncodedStringValue[] pduTo, EncodedStringValue[] pduCc, PduBody pduBody,
                                 long date, boolean isSecure, int subscriptionId)
      throws MmsException, NoSessionException, DuplicateMessageException, InvalidMessageException,
             LegacyMessageException
  {
    MmsDatabase           database    = DatabaseFactory.getMmsDatabase(context);
    SingleUseBlobProvider provider    = SingleUseBlobProvider.getInstance();
    String                from        = null;
    List<String>          to          = new LinkedList<>();
    List<String>          cc          = new LinkedList<>();
    String                body        = null;
    List<Attachment>      attachments = new LinkedList<>();

    if (pduFrom != null) {
      from = Util.toIsoString(pduFrom.getTextString());
    }

    if (pduTo != null) {
      for (EncodedStringValue toValue : pduTo) {
        to.add(Util.toIsoString(toValue.getTextString()));
      }
    }

    if (pduCc != null) {
      for (EncodedStringValue ccValue : pduCc) {
        cc.add(Util.toIsoString(ccValue.getTextString()));
      }
    }

    if (pduBody != null) {
      body = PartParser.getMessageText(pduBody);
      PduBody media = PartParser.getSupportedMediaParts(pduBody);

      for (int i=0;i<media.getPartsNum();i++) {
        PduPart part = media.getPart(i);

        if (part.getData() != null) {
          byte[]  decodedDigest = null;

          if (isSecure) {
            PduPart digestPart  = pduBody.getPartByName(Util.toIsoString(part.getName()) + ".digest");
            byte[]  digestBytes = null;

            if (digestPart != null) {
              digestBytes = digestPart.getData();
            }

            if (digestBytes != null) {
              decodedDigest = Base64.decode(digestBytes, Base64.NO_WRAP);
            }

            if (decodedDigest != null) {
              Log.w(TAG, "Available digest for part name " + Util.toIsoString(part.getName()) + " (content id " + Util.toIsoString(part.getContentId()) + "): " + Hex.toString(decodedDigest));
            } else {
              Log.w(TAG, "No available digest for part name " + Util.toIsoString(part.getName()) + " (content id " + Util.toIsoString(part.getContentId()) + ")");
            }
          }

          Uri uri = provider.createUri(part.getData());
          attachments.add(new UriAttachment(uri, Util.toIsoString(part.getContentType()),
                                            AttachmentDatabase.TRANSFER_PROGRESS_DONE,
                                            part.getData().length, decodedDigest));
        }
      }
    }

    IncomingMediaMessage message = new IncomingMediaMessage(from, to, cc, body, date * 1000L, attachments, subscriptionId);

    Pair<Long, Long> messageAndThreadId;

    if (isSecure) {
      messageAndThreadId = database.insertSecureDecryptedMessageInbox(masterSecret, message,
                                                                      threadId);
    } else {
      messageAndThreadId = database.insertMessageInbox(masterSecret, message,
                                                       contentLocation, threadId);
    }

    database.delete(messageId);
    MessageNotifier.updateNotification(context, masterSecret, message.getSubscriptionId());
  }

  private void handleDownloadError(MasterSecret masterSecret, long messageId, long threadId,
                                   int downloadStatus, boolean automatic)
  {
    MmsDatabase db = DatabaseFactory.getMmsDatabase(context);

    db.markDownloadState(messageId, downloadStatus);

    if (automatic) {
      db.markIncomingNotificationReceived(threadId);
      MessageNotifier.updateNotification(context, masterSecret, threadId);
    }
//
//    toastHandler.makeToast(error);
  }
}
