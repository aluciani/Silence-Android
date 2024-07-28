package org.smssecure.smssecure.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import org.smssecure.smssecure.BuildConfig;
import org.smssecure.smssecure.R;

import java.util.Arrays;

public class NotificationChannels {

  public static String MESSAGES      = "messages";
  public static String FAILURES      = "failures";
  public static String LOCKED_STATUS = "locked_status";
  public static String OTHER         = "other";

  /**
   * Ensures all of the notification channels are created. No harm in repeat calls. Call is safely
   * ignored for API < 26.
   */
  public static void create(@NonNull Context context) {
    if (Build.VERSION.SDK_INT < 26) {
      return;
    }

    NotificationChannel messages     = new NotificationChannel(MESSAGES, context.getString(R.string.NotificationChannel_messages), NotificationManager.IMPORTANCE_HIGH);
    NotificationChannel failures     = new NotificationChannel(FAILURES, context.getString(R.string.NotificationChannel_failures), NotificationManager.IMPORTANCE_HIGH);
    NotificationChannel lockedStatus = new NotificationChannel(LOCKED_STATUS, context.getString(R.string.NotificationChannel_locked_status), NotificationManager.IMPORTANCE_LOW);
    NotificationChannel other        = new NotificationChannel(OTHER, context.getString(R.string.NotificationChannel_other), NotificationManager.IMPORTANCE_LOW);

    NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannels(Arrays.asList(messages, failures, lockedStatus, other));
  }
}
