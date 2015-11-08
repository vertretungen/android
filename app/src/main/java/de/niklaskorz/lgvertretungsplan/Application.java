package de.niklaskorz.lgvertretungsplan;

import com.flurry.android.FlurryAgent;

/**
 * Created by niklaskorz on 08.11.2015.
 */
public class Application extends android.app.Application {
    final String FLURRY_API_KEY = "";

    @Override
    public void onCreate() {
        super.onCreate();

        FlurryAgent.setLogEnabled(true);
        FlurryAgent.init(this, FLURRY_API_KEY);
    }
}
