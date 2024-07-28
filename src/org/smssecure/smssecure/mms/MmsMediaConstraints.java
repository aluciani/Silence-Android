package org.smssecure.smssecure.mms;

import android.content.Context;
import android.os.Bundle;

import com.android.mms.service_alt.MmsConfig;

class MmsMediaConstraints extends MediaConstraints {

  private static final int DEFAULT_MAX_IMAGE_DIMEN  = 1024;
  private static final int DEFAULT_MAX_MESSAGE_SIZE = 280 * 1024;

  private static final double RATIO_IF_ENCRYPTED = 0.75; // Reduce by 25% max size for encrypted MMS mesages

  private final int subscriptionId;
  private final boolean isSecure;

  MmsMediaConstraints(int subscriptionId, boolean isSecure) {
    this.subscriptionId = subscriptionId;
    this.isSecure = isSecure;
  }

  @Override
  public int getImageMaxWidth(Context context) {
    MmsConfig mmsConfig = MmsConfigManager.getMmsConfig(context, subscriptionId);

    if (mmsConfig != null) {
      MmsConfig.Overridden overridden = new MmsConfig.Overridden(mmsConfig, new Bundle());
      return overridden.getMaxImageWidth();
    }

    return DEFAULT_MAX_IMAGE_DIMEN;
  }

  @Override
  public int getImageMaxHeight(Context context) {
    MmsConfig mmsConfig = MmsConfigManager.getMmsConfig(context, subscriptionId);

    if (mmsConfig != null) {
      MmsConfig.Overridden overridden = new MmsConfig.Overridden(mmsConfig, new Bundle());
      return overridden.getMaxImageHeight();
    }

    return DEFAULT_MAX_IMAGE_DIMEN;
  }

  @Override
  public int getImageMaxSize(Context context) {
    return getMaxMessageSize(context);
  }

  @Override
  public int getGifMaxSize(Context context) {
    return getMaxMessageSize(context);
  }

  @Override
  public int getVideoMaxSize(Context context) {
    return getMaxMessageSize(context);
  }

  @Override
  public int getAudioMaxSize(Context context) {
    return getMaxMessageSize(context);
  }

  private int getMaxMessageSize(Context context) {
    MmsConfig mmsConfig = MmsConfigManager.getMmsConfig(context, subscriptionId);

    if (mmsConfig != null) {
      MmsConfig.Overridden overridden = new MmsConfig.Overridden(mmsConfig, new Bundle());
      int size;
      double plaintextMaxSize = (double) overridden.getMaxMessageSize();
      if (isSecure) {
        size = (int)(plaintextMaxSize * RATIO_IF_ENCRYPTED);
      } else {
        size = (int)plaintextMaxSize;
      }
      return size;
    }

    return DEFAULT_MAX_MESSAGE_SIZE;
  }
}
