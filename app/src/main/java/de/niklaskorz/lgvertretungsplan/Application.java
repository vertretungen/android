package de.niklaskorz.lgvertretungsplan;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.piwik.sdk.PiwikApplication;
import org.piwik.sdk.Tracker;

/**
 * Created by niklaskorz on 08.11.2015.
 */
public class Application extends PiwikApplication {
    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        getPiwik().setDebug(BuildConfig.DEBUG);
        getPiwik().setDryRun(BuildConfig.DEBUG);
        getTracker().trackAppDownload();
    }

    public static Application get() {
        return instance;
    }

    @Override
    public String getTrackerUrl() {
        return "http://dev.niklaskorz.de/lgv/piwik/piwik.php";
    }

    @Override
    public Integer getSiteId() {
        return 1;
    }
}
