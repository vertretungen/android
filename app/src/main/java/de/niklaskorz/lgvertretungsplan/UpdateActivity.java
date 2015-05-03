package de.niklaskorz.lgvertretungsplan;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class UpdateActivity extends BaseActivity {
    String versionName;
    int versionCode;
    String versionDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

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
