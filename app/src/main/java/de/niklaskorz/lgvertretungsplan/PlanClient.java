package de.niklaskorz.lgvertretungsplan;

import android.content.Context;

import com.flurry.android.FlurryAgent;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

/**
 * Created by niklaskorz on 16.04.15.
 */
public class PlanClient {
    static final String BASE_URL = "https://vertretungsplan.leiningergymnasium.de";
    static final String INDEX_URL = BASE_URL + "/index";
    static final String TOMORROW_URL = BASE_URL + "/morgen";

    private AsyncHttpClient client;
    private PersistentCookieStore cookieStore;
    private String sessionId;
    private String username;
    private String password;

    public enum Type {
        TODAY, TOMORROW
    }

    public interface ResponseHandler {
        public void onSuccess(Plan p);
        public void onFailure(Throwable error);
    }

    public PlanClient(Context c) {
        client = new AsyncHttpClient();

        cookieStore = new PersistentCookieStore(c);
        client.setCookieStore(cookieStore);
    }

    // Login not needed, instead deliver login information with every request
    /*public void login(String username, String password, AsyncHttpResponseHandler responseHandler) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("username", username);
        requestParams.put("password", password);
        client.post(INDEX_URL, requestParams, responseHandler);
    }*/

    public void setLoginCredentials(String u, String p) {
        cookieStore.clear();
        username = u;
        password = p;

        FlurryAgent.setUserId(u);
    }

    public void get(final Type type, final ResponseHandler rh) {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("Type", type.name());
        FlurryAgent.logEvent("Plan_load", eventParams);

        AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    rh.onSuccess(new Plan(new String(responseBody, "UTF-8")));

                    Map<String, String> eventParams = new HashMap<String, String>();
                    eventParams.put("Type", type.name());
                    FlurryAgent.logEvent("Plan_loaded", eventParams);
                } catch (Throwable error) {
                    rh.onFailure(error);
                    FlurryAgent.onError("PLAN_GET_FAILED", error.getLocalizedMessage(), error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                rh.onFailure(error);
            }
        };

        RequestParams requestParams = new RequestParams();
        requestParams.put("username", username);
        requestParams.put("password", password);

        if (type == Type.TODAY) {
            client.post(INDEX_URL, requestParams, responseHandler);
        } else if (type == Type.TOMORROW) {
            client.post(TOMORROW_URL, requestParams, responseHandler);
        }
    }
}
