package de.niklaskorz.lgvertretungsplan;

import android.app.*;
import android.content.Context;

import org.piwik.sdk.Tracker;

/**
 * Created by niklaskorz on 03.05.15.
 */
public class AppState {
    private static Activity activeActivity = null;
    private static int activityCounter = 0;
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
