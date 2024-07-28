package org.smssecure.smssecure;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import org.smssecure.smssecure.crypto.IdentityKeyParcelable;
import org.smssecure.smssecure.crypto.MasterSecret;
import org.smssecure.smssecure.crypto.storage.SilenceSessionStore;
import org.smssecure.smssecure.database.DatabaseFactory;
import org.smssecure.smssecure.database.IdentityDatabase;
import org.smssecure.smssecure.database.MmsAddressDatabase;
import org.smssecure.smssecure.database.MmsDatabase;
import org.smssecure.smssecure.database.MmsSmsDatabase;
import org.smssecure.smssecure.database.SmsDatabase;
import org.smssecure.smssecure.database.documents.IdentityKeyMismatch;
import org.smssecure.smssecure.database.model.MessageRecord;
import org.smssecure.smssecure.jobs.SmsDecryptJob;
import org.smssecure.smssecure.recipients.Recipient;
import org.smssecure.smssecure.recipients.RecipientFactory;
import org.smssecure.smssecure.recipients.Recipients;
import org.smssecure.smssecure.sms.MessageSender;
import org.smssecure.smssecure.util.Base64;
import org.smssecure.smssecure.util.InvalidNumberException;
import org.smssecure.smssecure.util.Util;

import java.io.IOException;

public class ConfirmIdentityDialog extends AlertDialog {

  private static final String TAG = ConfirmIdentityDialog.class.getSimpleName();

  private OnClickListener callback;

  public ConfirmIdentityDialog(Context context,
                               MasterSecret masterSecret,
                               MessageRecord messageRecord,
                               IdentityKeyMismatch mismatch)
  {
    super(context);
    try {
      Recipient       recipient       = RecipientFactory.getRecipientForId(context, mismatch.getRecipientId(), false);
      String          name            = recipient.toShortString();
      String          number          = Util.canonicalizeNumber(context, recipient.getNumber());
      String          introduction    = String.format(context.getString(R.string.ConfirmIdentityDialog_the_signature_on_this_key_exchange_is_different), name, name);
      SpannableString spannableString = new SpannableString(introduction + " " +
                                                            context.getString(R.string.ConfirmIdentityDialog_you_may_wish_to_verify_this_contact));

      spannableString.setSpan(new VerifySpan(context, mismatch),
                              introduction.length()+1, spannableString.length(),
                              Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

      setTitle(name);
      setMessage(spannableString);

      setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.ConfirmIdentityDialog_accept), new AcceptListener(masterSecret, messageRecord, mismatch, number));
      setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(android.R.string.cancel),               new CancelListener());
    } catch (InvalidNumberException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public void show() {
    super.show();
    ((TextView)this.findViewById(android.R.id.message))
                   .setMovementMethod(LinkMovementMethod.getInstance());
  }

  public void setCallback(OnClickListener callback) {
    this.callback = callback;
  }

  private class AcceptListener implements OnClickListener {

    private final MasterSecret        masterSecret;
    private final MessageRecord       messageRecord;
    private final IdentityKeyMismatch mismatch;
    private final String              number;

    private AcceptListener(MasterSecret masterSecret, MessageRecord messageRecord, IdentityKeyMismatch mismatch, String number) {
      this.masterSecret  = masterSecret;
      this.messageRecord = messageRecord;
      this.mismatch      = mismatch;
      this.number        = number;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
      new AsyncTask<Void, Void, Void>()
      {
        @Override
        protected Void doInBackground(Void... params) {
          IdentityDatabase identityDatabase = DatabaseFactory.getIdentityDatabase(getContext());

          identityDatabase.saveIdentity(masterSecret,
                                        mismatch.getRecipientId(),
                                        mismatch.getIdentityKey());

          new SilenceSessionStore(getContext(), masterSecret, messageRecord.getSubscriptionId()).deleteAllSessions(number);

          processMessageRecord(messageRecord);

          return null;
        }

        private void processMessageRecord(MessageRecord messageRecord) {
          Context      context      = getContext();
          SmsDatabase  smsDatabase  = DatabaseFactory.getEncryptingSmsDatabase(context);

          smsDatabase.removeMismatchedIdentity(messageRecord.getId(),
                                                 mismatch.getRecipientId(),
                                                 mismatch.getIdentityKey());

          ApplicationContext.getInstance(context)
                            .getJobManager()
                            .add(new SmsDecryptJob(context, messageRecord.getId(), true, false));
        }

      }.execute();

      if (callback != null) callback.onClick(null, 0);
    }
  }

  private class CancelListener implements OnClickListener {
    @Override
    public void onClick(DialogInterface dialog, int which) {
      if (callback != null) callback.onClick(null, 0);
    }
  }

  private static class VerifySpan extends ClickableSpan {
    private final Context             context;
    private final IdentityKeyMismatch mismatch;

    private VerifySpan(Context context, IdentityKeyMismatch mismatch) {
      this.context  = context;
      this.mismatch = mismatch;
    }

    @Override
    public void onClick(View widget) {
      Intent intent = new Intent(context, VerifyIdentityActivity.class);
      intent.putExtra("recipient", mismatch.getRecipientId());
      intent.putExtra("remote_identity", new IdentityKeyParcelable(mismatch.getIdentityKey()));
      context.startActivity(intent);
    }
  }

}
