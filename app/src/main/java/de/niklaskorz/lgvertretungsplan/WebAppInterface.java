package de.niklaskorz.lgvertretungsplan;

import android.content.Context;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

/**
 * Created by niklaskorz on 14.04.15.
 */
public class WebAppInterface {
    Context context;
    WebView webView;
    Handler mainHandler;
    Plan plan;
    boolean isReady;

    WebAppInterface(Context c, WebView view) {
        context = c;
        webView = view;
        mainHandler = new Handler(context.getMainLooper());
        isReady = false;
    }

    @JavascriptInterface
    public void snack(String text) {
        SnackbarManager.show(Snackbar.with(context).text(text));
    }

    @JavascriptInterface
    public void ready() {
        isReady = true;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (plan != null) {
                    webView.loadUrl("javascript:app.onDataChanged()");
                }
            }
        });
    }

    @JavascriptInterface
    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan p) {
        plan = p;
        if (isReady) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:app.onDataChanged()");
                }
            });
        }
    }
}
