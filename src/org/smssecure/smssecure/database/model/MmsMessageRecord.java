package org.smssecure.smssecure.database.model;


import android.content.Context;
import android.support.annotation.NonNull;

import org.smssecure.smssecure.database.documents.IdentityKeyMismatch;
import org.smssecure.smssecure.database.documents.NetworkFailure;
import org.smssecure.smssecure.mms.Slide;
import org.smssecure.smssecure.mms.SlideDeck;
import org.smssecure.smssecure.recipients.Recipient;
import org.smssecure.smssecure.recipients.Recipients;

import java.util.List;

public abstract class MmsMessageRecord extends MessageRecord {

  private final @NonNull SlideDeck slideDeck;

  MmsMessageRecord(Context context, long id, Body body, Recipients recipients,
                   Recipient individualRecipient, int recipientDeviceId, long dateSent,
                   long dateReceived, long threadId, int deliveryStatus, long dateDeliveryReceived,
                   long type, List<IdentityKeyMismatch> mismatches,
                   List<NetworkFailure> networkFailures, int subscriptionId, @NonNull SlideDeck slideDeck)
  {
    super(context, id, body, recipients, individualRecipient, recipientDeviceId, dateSent, dateReceived, threadId, deliveryStatus, dateDeliveryReceived, type, mismatches, networkFailures, subscriptionId);
    this.slideDeck = slideDeck;
  }

  @Override
  public boolean isMms() {
    return true;
  }

  @NonNull
  public SlideDeck getSlideDeck() {
    return slideDeck;
  }

  @Override
  public boolean isMediaPending() {
    for (Slide slide : getSlideDeck().getSlides()) {
      if (slide.isInProgress() || slide.isPendingDownload()) {
        return true;
      }
    }

    return false;
  }

  public boolean containsMediaSlide() {
    return slideDeck.containsMediaSlide();
  }


}
