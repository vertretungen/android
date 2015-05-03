package de.niklaskorz.lgvertretungsplan;

import android.app.Activity;
import android.content.Context;

/**
 * Created by niklaskorz on 03.05.15.
 */
public class AppState {
    private static Context applicationContext = null;
    private static Activity activeActivity = null;
    private static int activityCounter = 0;

    public static Context getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(Context c) {
        if (applicationContext != null) {
            applicationContext = c;
        }
    }

    public static Activity getActiveActivity() {
        return activeActivity;
    }

    public static void activityPaused() {
        activityCounter -= 1;
    }

    public static void activityResumed(Activity a) {
        activityCounter += 1;
        activeActivity = a;
    }

    public static boolean isActive() {
        return activityCounter > 0;
    }
}
