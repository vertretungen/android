package de.niklaskorz.lgvertretungsplan;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.impl.cookie.BasicClientCookie;

/**
 * Created by niklaskorz on 16.04.15.
 */
public class PlanClient {
    static final String BASE_URL = "https://vertretungsplan.leiningergymnasium.de";
    static final String INDEX_URL = BASE_URL + "/index";
    static final String TOMORROW_URL = BASE_URL + "/morgen";

    private AsyncHttpClient client;
    private PersistentCookieStore cookieStore;
    private String auth;
    private String sessionId;

    public enum Type {
        TODAY, TOMORROW
    }

    public interface LoginResponseHandler {
    }

    public PlanClient(Context c) {
        client = new AsyncHttpClient();

        cookieStore = new PersistentCookieStore(c);
        client.setCookieStore(cookieStore);

        /*BasicClientCookie cookie = new BasicClientCookie("PHPSESSID", "4d7f22d9625d919f14f9ccfedf4176e5");
        cookie.setVersion(1);
        cookie.setDomain("vertretungsplan.leiningergymnasium.de");
        cookie.setPath("/");
        cookieStore.addCookie(cookie);*/
    }

    public void login(String username, String password, AsyncHttpResponseHandler responseHandler) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("username", username);
        requestParams.put("password", password);
        client.post(INDEX_URL, requestParams, responseHandler);
    }

    public void logout() {
        cookieStore.clear();
    }

    public void get(Type type, final AsyncHttpResponseHandler responseHandler) {
        AsyncHttpResponseHandler rh = new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                responseHandler.onSuccess(statusCode, headers, responseBody);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                responseHandler.onFailure(statusCode, headers, responseBody, error);
            }
        };

        if (type == Type.TODAY) {
            client.get(INDEX_URL, rh);
        } else if (type == Type.TOMORROW) {
            client.get(TOMORROW_URL, rh);
        }
    }
}
