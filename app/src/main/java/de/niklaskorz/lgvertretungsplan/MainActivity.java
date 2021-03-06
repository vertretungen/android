package de.niklaskorz.lgvertretungsplan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.piwik.sdk.Tracker;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends BaseActivity implements PlanClient.ResponseHandler {
    Toolbar toolbar;
    PlanClient planClient;
    LoginManager loginManager;
    SwipeRefreshLayout swipeRefreshLayout;
    PlanClient.Type lastPlanType = PlanClient.Type.TODAY;

    RecyclerView recyclerView;
    PlanAdapter adapter;
    LinearLayoutManager layoutManager;

    String selectedClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Application.get().getTracker().trackScreenView("/main");

        checkVersionChange();
        checkUpdates();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPlan(lastPlanType);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.red, R.color.green, R.color.blue);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .marginResId(R.dimen.horizontal_divider_left_margin, R.dimen.horizontal_divider_right_margin)
                        .build());
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        planClient = new PlanClient(this);

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

        selectedClass = PreferenceManager.getDefaultSharedPreferences(this).getString("class", "0");

        loginManager = new LoginManager(this, planClient);
        loginManager.login(lastPlanType, this);
    }

    private void checkVersionChange() {
        int currentVersion = 0;
        String versionName = "";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersion = packageInfo.versionCode;
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ex) {
            ex.printStackTrace();
            finish();
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int lastVersion = settings.getInt("version_code", 0);

        if (lastVersion != currentVersion) {
            Application.get().getTracker().trackEvent("update", "installed", versionName, currentVersion);

            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("version_code", currentVersion);
            try {
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), 0);
                File appFile = new File(appInfo.sourceDir);
                editor.putLong("last_update", appFile.lastModified());
            } catch (PackageManager.NameNotFoundException ex) {
                ex.printStackTrace();
                Application.get().getTracker().trackException(ex, ex.getMessage(), false);
            }
            editor.apply();
        }
    }

    private void checkUpdates() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        long lastUpdateSearch = settings.getLong("last_update_search", 0);
        long now = new Date().getTime();

        if (now - lastUpdateSearch > 2 * 24 * 60 * 60 * 1000) {
            VersionManager.getInstance().check(this, null);
        }
    }

    private void loadPlan(final PlanClient.Type type) {
        lastPlanType = type;
        swipeRefreshLayout.setRefreshing(true);
        planClient.get(type, this);
    }

    @Override
    public void onSuccess(Plan p) {
        swipeRefreshLayout.setRefreshing(false);
        adapter = new PlanAdapter(this, layoutManager, p, selectedClass);
        recyclerView.setAdapter(adapter);
        SnackbarManager.show(Snackbar
                .with(this)
                .text(p.lastUpdateString)
                .duration(Snackbar.SnackbarDuration.LENGTH_LONG));

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd.MM.yyyy", Locale.getDefault());
        setTitle(dateFormat.format(p.date));
    }

    @Override
    public void onFailure(final Throwable error) {
        final Context context = this;

        swipeRefreshLayout.setRefreshing(false);
        error.printStackTrace();
        SnackbarManager.show(Snackbar
                .with(this)
                .text("Vertretungsplan konnte nicht geladen werden")
                .actionLabel("Info")
                .actionListener(new ActionClickListener() {
                    @Override
                    public void onActionClicked(Snackbar snackbar) {
                        new MaterialDialog.Builder(context)
                                .title("Something went wrong")
                                .content(error.getLocalizedMessage())
                                .positiveText("Close")
                                .show();
                    }
                })
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            selectedClass = PreferenceManager.getDefaultSharedPreferences(this).getString("class", "0");
            if (adapter != null) {
                adapter.setClassFilter(selectedClass);
            }

            return true;
        } else if (id == R.id.action_signout) {
            loginManager.logout();
            recyclerView.setAdapter(null);
            loginManager.login(lastPlanType, this);
        } else if (id == R.id.action_today) {
            loadPlan(PlanClient.Type.TODAY);
        } else if (id == R.id.action_tomorrow) {
            loadPlan(PlanClient.Type.TOMORROW);
        } else if (id == R.id.action_classes) {
            new MaterialDialog.Builder(this)
                    .title(R.string.action_classes)
                    .items(R.array.pref_class_titles)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                            String[] classValues = getResources().getStringArray(R.array.pref_class_values);
                            selectedClass = classValues[i];
                            if (adapter != null) {
                                adapter.setClassFilter(selectedClass);
                            }

                            Application.get().getTracker().trackEvent("class", "select", selectedClass);
                        }
                    })
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    
}
