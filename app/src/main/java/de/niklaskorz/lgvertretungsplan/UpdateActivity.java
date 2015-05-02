package de.niklaskorz.lgvertretungsplan;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import de.niklaskorz.lgvertretungsplan.R;

public class UpdateActivity extends ActionBarActivity {
    String versionName;
    int versionCode;
    String versionDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.primaryDark));
            window.setNavigationBarColor(getResources().getColor(R.color.primaryDark));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        versionName = intent.getStringExtra("versionName");
        versionCode = intent.getIntExtra("versionCode", 0);
        versionDescription = intent.getStringExtra("versionDescription");

        setTitle(getIntent().getStringExtra("versionName"));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        TextView descriptionView = (TextView) findViewById(R.id.update_description);
        descriptionView.setText(versionDescription);
    }

    public void actionWait(View v) {
        finish();
    }

    public void actionDownload(View v) {
        VersionManager.getInstance().loadUpdate(getApplicationContext(), versionCode);
        finish();
    }
}
