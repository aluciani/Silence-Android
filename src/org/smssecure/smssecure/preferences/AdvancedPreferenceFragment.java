package org.smssecure.smssecure.preferences;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.support.annotation.Nullable;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;

import org.smssecure.smssecure.ApplicationPreferencesActivity;
import org.smssecure.smssecure.LogSubmitActivity;
import org.smssecure.smssecure.R;
import org.smssecure.smssecure.contacts.ContactAccessor;
import org.smssecure.smssecure.crypto.MasterSecret;
import org.smssecure.smssecure.util.dualsim.SubscriptionManagerCompat;
import org.smssecure.smssecure.util.SilencePreferences;

public class AdvancedPreferenceFragment extends ListSummaryPreferenceFragment {
  private static final String TAG = AdvancedPreferenceFragment.class.getSimpleName();

  private static final String SYSTEM_EMOJI_PREF     = SilencePreferences.SYSTEM_EMOJI_PREF;
  private static final String SUBMIT_DEBUG_LOG_PREF = "pref_submit_debug_logs";

  @Override
  public void onCreate(Bundle paramBundle) {
    super.onCreate(paramBundle);

    this.findPreference(SUBMIT_DEBUG_LOG_PREF)
      .setOnPreferenceClickListener(new SubmitDebugLogListener());

    this.findPreference(SilencePreferences.ENTER_KEY_TYPE).setOnPreferenceChangeListener(new ListSummaryListener());
    initializeListSummary((ListPreference) this.findPreference(SilencePreferences.ENTER_KEY_TYPE));

    if (SubscriptionManagerCompat.from(getActivity()).getActiveSubscriptionInfoList().size() <= 1) {
      this.findPreference(SilencePreferences.ASK_FOR_SIM_CARD).setVisible(false);
    }
  }

  @Override
  public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.preferences_advanced);
  }

  @Override
  public void onResume() {
    super.onResume();
    ((ApplicationPreferencesActivity) getActivity()).getSupportActionBar().setTitle(R.string.preferences__advanced);
  }

  private class SubmitDebugLogListener implements Preference.OnPreferenceClickListener {
    @Override
    public boolean onPreferenceClick(Preference preference) {
      final Intent intent = new Intent(getActivity(), LogSubmitActivity.class);
      startActivity(intent);
      return true;
    }
  }
}
