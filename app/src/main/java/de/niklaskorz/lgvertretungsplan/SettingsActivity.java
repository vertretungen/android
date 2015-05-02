package de.niklaskorz.lgvertretungsplan;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.support.v7.widget.Toolbar;


import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

import java.util.Date;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends ActionBarActivity {
    private String username;
    private String userFullname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.primaryDark));
            window.setNavigationBarColor(getResources().getColor(R.color.primaryDark));
        }

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        userFullname = intent.getStringExtra("fullname");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        GeneralPreferenceFragment preferenceFragment = (GeneralPreferenceFragment) getFragmentManager().findFragmentById(R.id.preference_fragment);
        preferenceFragment.setUser(username, userFullname);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        Preference searchUpdatesPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            //bindPreferenceSummaryToValue(findPreference("courses"));
            bindPreferenceSummaryToValue(findPreference("class"));

            updateLastUpdateSearch();
            updateLastUpdate();

            Preference versionPref = (Preference) findPreference("version");
            try {
                String version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
                versionPref.setSummary(version);

                versionPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("version", preference.getSummary());
                        clipboard.setPrimaryClip(clip);

                        SnackbarManager.show(Snackbar
                                .with(getActivity())
                                .text(getResources().getString(R.string.snack_version_copied)));

                        return true;
                    }
                });
            } catch (PackageManager.NameNotFoundException ex) {
                ex.printStackTrace();
            }

            searchUpdatesPreference = findPreference("search_updates");
            searchUpdatesPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    searchUpdatesPreference.setSummary(R.string.searching_for_updates);
                    VersionManager.getInstance().check(getActivity(), new VersionManager.CompletionHandler() {
                        @Override
                        public void complete(final boolean updateAvailable) {
                            if (updateAvailable) {
                                searchUpdatesPreference.setSummary(R.string.updates_available);
                            } else {
                                searchUpdatesPreference.setSummary(R.string.no_updates_available);
                            }
                            updateLastUpdateSearch();
                        }

                        @Override
                        public void error(Throwable error) {
                            searchUpdatesPreference.setSummary(R.string.no_updates_available);
                            SnackbarManager.show(Snackbar
                                    .with(getActivity())
                                    .text(error.getLocalizedMessage())
                                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE));
                        }
                    });
                    return true;
                }
            });
        }

        private void updateLastUpdateSearch() {
            long lastUpdateSearchTimestamp = getPreferenceManager().getSharedPreferences().getLong("last_update_search", 0);
            Preference lastUpdateSearchPreference = findPreference("last_update_search");
            if (lastUpdateSearchTimestamp != 0) {
                lastUpdateSearchPreference.setSummary(new Date(lastUpdateSearchTimestamp).toString());
            }
        }

        private void updateLastUpdate() {
            long lastUpdateTimestamp = getPreferenceManager().getSharedPreferences().getLong("last_update", 0);
            Preference lastUpdatePreference = findPreference("last_update");
            if (lastUpdateTimestamp != 0) {
                lastUpdatePreference.setSummary(new Date(lastUpdateTimestamp).toString());
            }
        }

        public void setUser(String name, String fullname) {
            Preference username = (Preference) findPreference("username");
            username.setTitle(fullname);
            username.setSummary(name);
        }
    }
}
