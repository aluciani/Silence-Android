package org.smssecure.smssecure.preferences.widgets;


import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import org.smssecure.smssecure.R;
import org.smssecure.smssecure.util.ViewUtil;

public class SilenceListPreference extends ListPreference {

  private TextView rightSummary;
  private CharSequence summary;

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public SilenceListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize();
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public SilenceListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize();
  }

  public SilenceListPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }

  public SilenceListPreference(Context context) {
    super(context);
    initialize();
  }

  private void initialize() {
    setWidgetLayoutResource(R.layout.preference_right_summary_widget);
  }

  @Override
  public void onBindViewHolder(PreferenceViewHolder view) {
    super.onBindViewHolder(view);

    this.rightSummary = (TextView)view.findViewById(R.id.right_summary);
    setSummary(this.summary);
  }

  @Override
  public void setSummary(CharSequence summary) {
    super.setSummary(null);

    this.summary = summary;

    if (this.rightSummary != null) {
      this.rightSummary.setText(summary);
    }
  }
}
