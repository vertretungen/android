package de.niklaskorz.lgvertretungsplan;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by niklaskorz on 03.05.15.
 */
public class BaseActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppState.setApplicationContext(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.primaryDark));
            window.setNavigationBarColor(getResources().getColor(R.color.primaryDark));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppState.activityResumed(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppState.activityPaused();
    }
}
