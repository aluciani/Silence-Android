package org.smssecure.smssecure.jobs;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.google.android.mms.pdu_alt.GenericPdu;
import com.google.android.mms.pdu_alt.NotificationInd;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;

import org.smssecure.smssecure.ApplicationContext;
import org.smssecure.smssecure.crypto.MasterSecret;
import org.smssecure.smssecure.database.DatabaseFactory;
import org.smssecure.smssecure.database.MmsDatabase;
import org.smssecure.smssecure.notifications.MessageNotifier;
import org.smssecure.smssecure.recipients.RecipientFactory;
import org.smssecure.smssecure.recipients.Recipients;
import org.smssecure.smssecure.service.KeyCachingService;
import org.smssecure.smssecure.util.dualsim.DualSimUtil;
import org.smssecure.smssecure.util.SilencePreferences;
import org.smssecure.smssecure.util.Util;
import org.whispersystems.jobqueue.JobParameters;

public class MmsReceiveJob extends ContextJob {

  private static final long serialVersionUID = 1L;

  private static final String TAG = MmsReceiveJob.class.getSimpleName();

  private final byte[] data;
  private final int    subscriptionId;

  public MmsReceiveJob(Context context, byte[] data, int subscriptionId) {
    super(context, JobParameters.newBuilder()
                                .withWakeLock(true)
                                .withPersistence().create());

    Log.w(TAG, "subscriptionId: " + subscriptionId);
    Log.w(TAG, "Found app subscription ID: " + DualSimUtil.getSubscriptionIdFromDeviceSubscriptionId(context, subscriptionId));

    this.data           = data;
    this.subscriptionId = DualSimUtil.getSubscriptionIdFromDeviceSubscriptionId(context, subscriptionId);
  }

  @Override
  public void onAdded() {

  }

  @Override
  public void onRun() {
    if (data == null) {
      Log.w(TAG, "Received NULL pdu, ignoring...");
      return;
    }

    PduParser  parser = new PduParser(data);
    GenericPdu pdu    = null;

    try {
      pdu = parser.parse();
    } catch (RuntimeException e) {
      Log.w(TAG, e);
    }

    if (isNotification(pdu) && !isBlocked(pdu)) {
      MmsDatabase      database           = DatabaseFactory.getMmsDatabase(context);
      Pair<Long, Long> messageAndThreadId = database.insertMessageInbox((NotificationInd)pdu, subscriptionId);
      MasterSecret     masterSecret       = KeyCachingService.getMasterSecret(context);

      Log.w(TAG, "Inserted received MMS notification...");

      database.markIncomingNotificationReceived(messageAndThreadId.second);

      if (!SilencePreferences.isMediaDownloadAllowed(context))
        MessageNotifier.updateNotification(context, masterSecret, messageAndThreadId.second);

      ApplicationContext.getInstance(context)
                        .getJobManager()
                        .add(new MmsDownloadJob(context,
                                                messageAndThreadId.first,
                                                messageAndThreadId.second,
                                                true));
    } else if (isNotification(pdu)) {
      Log.w(TAG, "*** Received blocked MMS, ignoring...");
    }
  }

  @Override
  public void onCanceled() {
    // TODO
  }

  @Override
  public boolean onShouldRetry(Exception exception) {
    return false;
  }

  private boolean isBlocked(GenericPdu pdu) {
    if (pdu.getFrom() != null && pdu.getFrom().getTextString() != null) {
      Recipients recipients = RecipientFactory.getRecipientsFromString(context, Util.toIsoString(pdu.getFrom().getTextString()), false);
      return recipients.isBlocked();
    }

    return false;
  }

  private boolean isNotification(GenericPdu pdu) {
    return pdu != null && pdu.getMessageType() == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;
  }
}
