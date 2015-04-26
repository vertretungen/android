package de.niklaskorz.lgvertretungsplan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.os.Build;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.ActionClickListener;

import org.apache.http.Header;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements PlanClient.ResponseHandler {
    Toolbar toolbar;
    ProfileDrawerItem profileDrawerItem;
    AccountHeader.Result accountHeader;
    Drawer.Result drawer;
    PlanClient planClient;
    LoginManager loginManager;
    SwipeRefreshLayout swipeRefreshLayout;
    PlanClient.Type lastPlanType = PlanClient.Type.TODAY;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    LinearLayoutManager layoutManager;

    public static final int ACTION_TODAY = 0;
    public static final int ACTION_TOMORROW = 1;
    public static final int ACTION_SIGNOUT = 2;
    public static final int ACTION_SETTINGS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.primaryDark));
            window.setNavigationBarColor(getResources().getColor(R.color.primaryDark));
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        profileDrawerItem = new ProfileDrawerItem();

        accountHeader = new AccountHeader()
                .withActivity(this)
                .withHeaderBackground(R.drawable.books)
                .withProfileImagesVisible(false)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(profileDrawerItem)
                .withSavedInstance(savedInstanceState)
                .build();

        final PlanClient.ResponseHandler rh = this;
        drawer = new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggleAnimated(true)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(getResources().getString(R.string.action_today)).withCheckable(false).withIdentifier(ACTION_TODAY),
                        new PrimaryDrawerItem().withName(getResources().getString(R.string.action_tomorrow)).withCheckable(false).withIdentifier(ACTION_TOMORROW),
                        new DividerDrawerItem(),
                        //new SecondaryDrawerItem().withName("Einstellungen"),
                        new SecondaryDrawerItem().withName(getResources().getString(R.string.action_signout)).withCheckable(false).withIdentifier(ACTION_SIGNOUT)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l, IDrawerItem iDrawerItem) {
                        if (iDrawerItem.getIdentifier() == ACTION_TODAY) {
                            loadPlan(PlanClient.Type.TODAY);
                        } else if (iDrawerItem.getIdentifier() == ACTION_TOMORROW) {
                            loadPlan(PlanClient.Type.TOMORROW);
                        } else if (iDrawerItem.getIdentifier() == ACTION_SIGNOUT) {
                            loginManager.logout();
                            recyclerView.setAdapter(null);
                            loginManager.login(lastPlanType, rh);
                        }

                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPlan(lastPlanType);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.red, R.color.green, R.color.blue);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        planClient = new PlanClient(this);

        swipeRefreshLayout.setRefreshing(true);
        loginManager = new LoginManager(this, planClient);
        loginManager.login(lastPlanType, this);
    }

    public void loadPlan(final PlanClient.Type type) {
        lastPlanType = type;
        swipeRefreshLayout.setRefreshing(true);
        planClient.get(type, this);
    }

    @Override
    public void onSuccess(Plan p) {
        profileDrawerItem.setEmail(loginManager.getUsername());
        profileDrawerItem.setName(loginManager.getUserFullname());
        accountHeader.removeProfile(0);
        accountHeader.addProfile(profileDrawerItem, 0);

        swipeRefreshLayout.setRefreshing(false);
        adapter = new PlanAdapter(layoutManager, p);
        recyclerView.setAdapter(adapter);
        SnackbarManager.show(Snackbar
                .with(this)
                .text(p.dateString)
                .duration(Snackbar.SnackbarDuration.LENGTH_LONG));

        if (lastPlanType == PlanClient.Type.TODAY) {
            drawer.setSelectionByIdentifier(ACTION_TODAY, false);
        } else if (lastPlanType == PlanClient.Type.TOMORROW) {
            drawer.setSelectionByIdentifier(ACTION_TOMORROW, false);
        }
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

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState = drawer.saveInstanceState(outState);
        outState = drawer.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
}
