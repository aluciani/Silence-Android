package org.smssecure.smssecure.jobs;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.android.mms.dom.smil.parser.SmilXmlSerializer;
import com.google.android.mms.ContentType;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.pdu_alt.CharacterSets;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.PduBody;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduPart;
import com.google.android.mms.pdu_alt.SendConf;
import com.google.android.mms.pdu_alt.SendReq;
import com.google.android.mms.smil.SmilHelper;
import com.klinker.android.send_message.Utils;

import org.smssecure.smssecure.attachments.Attachment;
import org.smssecure.smssecure.crypto.MasterSecret;
import org.smssecure.smssecure.crypto.MmsCipher;
import org.smssecure.smssecure.crypto.storage.SilenceSignalProtocolStore;
import org.smssecure.smssecure.database.DatabaseFactory;
import org.smssecure.smssecure.database.MmsDatabase;
import org.smssecure.smssecure.database.NoSuchMessageException;
import org.smssecure.smssecure.jobs.requirements.MasterSecretRequirement;
import org.smssecure.smssecure.mms.CompatMmsConnection;
import org.smssecure.smssecure.mms.MediaConstraints;
import org.smssecure.smssecure.mms.MmsSendResult;
import org.smssecure.smssecure.mms.OutgoingMediaMessage;
import org.smssecure.smssecure.mms.PartAuthority;
import org.smssecure.smssecure.notifications.MessageNotifier;
import org.smssecure.smssecure.recipients.Recipient;
import org.smssecure.smssecure.recipients.Recipients;
import org.smssecure.smssecure.recipients.RecipientFormattingException;
import org.smssecure.smssecure.transport.UndeliverableMessageException;
import org.smssecure.smssecure.util.dualsim.DualSimUtil;
import org.smssecure.smssecure.util.Hex;
import org.smssecure.smssecure.util.NumberUtil;
import org.smssecure.smssecure.util.MediaUtil;
import org.smssecure.smssecure.util.Util;
import org.whispersystems.jobqueue.JobParameters;
import org.whispersystems.jobqueue.requirements.NetworkRequirement;
import org.whispersystems.jobqueue.util.Base64;
import org.whispersystems.libsignal.NoSessionException;
import org.whispersystems.libsignal.UntrustedIdentityException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.smssecure.smssecure.mms.MmsException;

public class MmsSendJob extends SendJob {

  private static final long serialVersionUID = 0L;

  private static final String TAG = MmsSendJob.class.getSimpleName();

  private final long messageId;

  public MmsSendJob(Context context, long messageId) {
    super(context, JobParameters.newBuilder()
                                .withGroupId("mms-operation")
                                .withRequirement(new NetworkRequirement(context))
                                .withRequirement(new MasterSecretRequirement(context))
                                .withPersistence()
                                .create());

    this.messageId = messageId;
  }

  @Override
  public void onAdded() {
//    MmsDatabase database = DatabaseFactory.getMmsDatabase(context);
//    database.markAsSending(messageId);
  }

  @Override
  public void onSend(MasterSecret masterSecret) throws MmsException, NoSuchMessageException, IOException {
    MmsDatabase          database = DatabaseFactory.getMmsDatabase(context);
    OutgoingMediaMessage message  = database.getOutgoingMessage(masterSecret, messageId);

    try {
      boolean upgradedSecure = false;

      SendReq pdu = constructSendPdu(masterSecret, message);

      if (message.isSecure()) {
        Log.w(TAG, "Encrypting MMS...");
        pdu = getEncryptedMessage(masterSecret, pdu, message.getSubscriptionId());
        upgradedSecure = true;
      }

      validateDestinations(message, pdu);

      final byte[]        pduBytes = getPduBytes(masterSecret, pdu);
      final SendConf      sendConf = new CompatMmsConnection(context).send(pduBytes, DualSimUtil.getSubscriptionIdFromAppSubscriptionId(context, message.getSubscriptionId()));
      final MmsSendResult result   = getSendResult(sendConf, pdu, upgradedSecure);

      database.markAsSent(messageId, result.isUpgradedSecure());
      markAttachmentsUploaded(messageId, message.getAttachments());
    } catch (UndeliverableMessageException | IOException e) {
      Log.w(TAG, e);
      database.markAsSentFailed(messageId);
      notifyMediaMessageDeliveryFailed(context, messageId);
    }
  }

  @Override
  public boolean onShouldRetryThrowable(Exception exception) {
    return false;
  }

  @Override
  public void onCanceled() {
    DatabaseFactory.getMmsDatabase(context).markAsSentFailed(messageId);
    notifyMediaMessageDeliveryFailed(context, messageId);
  }

  private byte[] getPduBytes(MasterSecret masterSecret, SendReq message)
      throws IOException, UndeliverableMessageException
  {
    byte[] pduBytes = new PduComposer(context, message).make();

    if (pduBytes == null) {
      throw new UndeliverableMessageException("PDU composition failed, null payload");
    }

    return pduBytes;
  }

  private MmsSendResult getSendResult(SendConf conf, SendReq message, boolean upgradedSecure)
      throws UndeliverableMessageException
  {
    if (conf == null) {
      throw new UndeliverableMessageException("No M-Send.conf received in response to send.");
    } else if (conf.getResponseStatus() != PduHeaders.RESPONSE_STATUS_OK) {
      throw new UndeliverableMessageException("Got bad response: " + conf.getResponseStatus());
    } else if (isInconsistentResponse(message, conf)) {
      throw new UndeliverableMessageException("Mismatched response!");
    } else {
      return new MmsSendResult(conf.getMessageId(), conf.getResponseStatus(), upgradedSecure, false);
    }
  }

  private SendReq getEncryptedMessage(MasterSecret masterSecret, SendReq pdu, int subscriptionId)
      throws UndeliverableMessageException
  {
    try {
      MmsCipher cipher = new MmsCipher(new SilenceSignalProtocolStore(context, masterSecret, subscriptionId));
      return cipher.encrypt(context, pdu);
    } catch (UntrustedIdentityException | NoSessionException e) {
      throw new UndeliverableMessageException(e);
    } catch (RecipientFormattingException e) {
      throw new AssertionError(e);
    }
  }

  private boolean isInconsistentResponse(SendReq message, SendConf response) {
    Log.w(TAG, "Comparing: " + Hex.toString(message.getTransactionId()));
    Log.w(TAG, "With:      " + Hex.toString(response.getTransactionId()));
    return !Arrays.equals(message.getTransactionId(), response.getTransactionId());
  }

  private void validateDestinations(EncodedStringValue[] destinations) throws UndeliverableMessageException {
    if (destinations == null) return;

    for (EncodedStringValue destination : destinations) {
      if (destination == null || !NumberUtil.isValidSmsOrEmail(destination.getString())) {
        throw new UndeliverableMessageException("Invalid destination: " +
                                                (destination == null ? null : destination.getString()));
      }
    }
  }

  private void validateDestinations(OutgoingMediaMessage media, SendReq message) throws UndeliverableMessageException {
    validateDestinations(message.getTo());
    validateDestinations(message.getCc());
    validateDestinations(message.getBcc());

    if (message.getTo() == null && message.getCc() == null && message.getBcc() == null) {
      throw new UndeliverableMessageException("No to, cc, or bcc specified!");
    }
  }

  private SendReq constructSendPdu(MasterSecret masterSecret, OutgoingMediaMessage message)
      throws UndeliverableMessageException
  {
    SendReq          req               = new SendReq();
    String           lineNumber        = Utils.getMyPhoneNumber(context);
    List<String>     numbers           = message.getRecipients().toNumberStringList(true);
    MediaConstraints mediaConstraints  = MediaConstraints.getMmsMediaConstraints(message.getSubscriptionId(), message.isSecure());
    List<Attachment> scaledAttachments = scaleAttachments(masterSecret, mediaConstraints, message.getAttachments());

    if (!TextUtils.isEmpty(lineNumber)) {
      req.setFrom(new EncodedStringValue(lineNumber));
    }

    for (String recipient : numbers) {
      req.addTo(new EncodedStringValue(recipient));
    }

    req.setDate(System.currentTimeMillis() / 1000);

    PduBody body = new PduBody();
    int     size = 0;

    if (!TextUtils.isEmpty(message.getBody())) {
      PduPart part = new PduPart();
      String name = String.valueOf(System.currentTimeMillis());
      part.setData(Util.toUtf8Bytes(message.getBody()));
      part.setCharset(CharacterSets.UTF_8);
      part.setContentType(ContentType.TEXT_PLAIN.getBytes());
      part.setContentId(name.getBytes());
      part.setContentLocation((name + ".txt").getBytes());
      part.setName((name + ".txt").getBytes());

      body.addPart(part);
      size += getPartSize(part);
    }

    for (Attachment attachment : scaledAttachments) {
      try {
        if (attachment.getDataUri() == null) throw new IOException("Assertion failed, attachment for outgoing MMS has no data!");

        PduPart part = new PduPart();

        String fileName      = String.valueOf(Math.abs(Util.getSecureRandom().nextLong()));
        String fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(attachment.getContentType());

        if (fileExtension != null) fileName = fileName + "." + fileExtension;

        if (attachment.getContentType().startsWith("text")) {
          part.setCharset(CharacterSets.UTF_8);
        }

        part.setContentType(attachment.getContentType().getBytes());
        part.setContentLocation(fileName.getBytes());
        part.setName(fileName.getBytes());

        int index = fileName.lastIndexOf(".");
        String contentId = (index == -1) ? fileName : fileName.substring(0, index);
        part.setContentId(contentId.getBytes());
        part.setData(Util.readFully(PartAuthority.getAttachmentStream(context, masterSecret, attachment.getDataUri())));

        body.addPart(part);
        size += getPartSize(part);

        if (message.isSecure()) {
          if (attachment.getDigest() != null) {
            byte[]  base64Digest    = Base64.encode(attachment.getDigest(), Base64.NO_WRAP);
            PduPart digestPart      = new PduPart();
            String  name            = fileName + ".digest";
            String  digestContentId = contentId + "-digest";

            digestPart.setData(base64Digest);
            digestPart.setCharset(CharacterSets.UTF_8);
            digestPart.setContentType(MediaUtil.CONTENT_TYPE_OCTET_STREAM.getBytes());
            digestPart.setContentId(digestContentId.getBytes());
            digestPart.setContentLocation((name + ".txt").getBytes());
            digestPart.setName(name.getBytes());

            Log.w(TAG, "Inserting digest for file " + fileName);

            body.addPart(digestPart);
            size += getPartSize(digestPart);
          } else {
            Log.w(TAG, "Digest is null!");
          }
        }
      } catch (IOException e) {
        Log.w(TAG, e);
      }
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    SmilXmlSerializer.serialize(SmilHelper.createSmilDocument(body), out);
    PduPart smilPart = new PduPart();
    smilPart.setContentId("smil".getBytes());
    smilPart.setContentLocation("smil.xml".getBytes());
    smilPart.setContentType(ContentType.APP_SMIL.getBytes());
    smilPart.setData(out.toByteArray());
    body.addPart(0, smilPart);

    req.setBody(body);
    req.setMessageSize(size);
    req.setMessageClass(PduHeaders.MESSAGE_CLASS_PERSONAL_STR.getBytes());
    req.setExpiry(7 * 24 * 60 * 60);

    try {
      req.setPriority(PduHeaders.PRIORITY_NORMAL);
      req.setDeliveryReport(PduHeaders.VALUE_NO);
      req.setReadReport(PduHeaders.VALUE_NO);
    } catch (InvalidHeaderValueException e) {}

    return req;
  }

  private long getPartSize(PduPart part) {
    return part.getName().length + part.getContentLocation().length +
        part.getContentType().length + part.getData().length +
        part.getContentId().length;
  }

  private void notifyMediaMessageDeliveryFailed(Context context, long messageId) {
    long       threadId   = DatabaseFactory.getMmsDatabase(context).getThreadIdForMessage(messageId);
    Recipients recipients = DatabaseFactory.getThreadDatabase(context).getRecipientsForThreadId(threadId);

    if (recipients != null) {
      MessageNotifier.notifyMessageDeliveryFailed(context, recipients, threadId);
    }
  }
}
