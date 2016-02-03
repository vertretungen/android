package de.niklaskorz.lgvertretungsplan;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import cz.msebera.android.httpclient.Header;
import org.json.JSONException;
import org.json.JSONObject;
import org.piwik.sdk.Tracker;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by niklaskorz on 01.05.15.
 */
public class VersionManager {
    public static interface CompletionHandler {
        public void complete(boolean updateAvailable);
        public void error(Throwable error);
    }

    public static class VersionInfo {
        int code;
        String name;
        String description;
    }

    private static VersionManager instance;

    private AsyncHttpClient client = new AsyncHttpClient();

    private VersionManager() {}

    public static VersionManager getInstance() {
        if (instance != null) {
            return instance;
        }

        return instance = new VersionManager();
    }

    public void check(final Context c, final CompletionHandler ch) {
        client.get("http://dev.niklaskorz.de/lgv/version.txt", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
                SharedPreferences.Editor settingsEditor = settings.edit();
                settingsEditor.putLong("last_update_search", new Date().getTime());
                settingsEditor.apply();

                final int latest = Integer.parseInt(new String(responseBody));
                int installed = 0;
                try {
                    installed = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionCode;
                } catch (PackageManager.NameNotFoundException ex) {
                    ex.printStackTrace();
                }

                boolean hasUpdate = latest > installed;

                if (!hasUpdate && ch != null) {
                    ch.complete(false);
                }

                if (hasUpdate) {
                    client.get("http://dev.niklaskorz.de/lgv/versions/" + latest + ".json", new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            VersionInfo versionInfo = new VersionInfo();
                            versionInfo.code = latest;
                            try {
                                versionInfo.name = response.getString("version");
                                versionInfo.description = response.getString("description");
                            } catch (JSONException ex) {
                                ex.printStackTrace();

                                if (ch != null) {
                                    ch.complete(false);
                                }
                                return;
                            }
                            showAvailable(c, versionInfo);


                            Application.get().getTracker().trackEvent("update", "received", versionInfo.name, versionInfo.code);

                            if (ch != null) {
                                ch.complete(true);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] hedaers, Throwable error, JSONObject errorResponse) {
                            if (ch != null) {
                                ch.error(error);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (ch != null) {
                    ch.error(error);
                }
            }
        });
    }

    public void loadUpdate(Context c, int version) {
        NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotificationIds.UPDATE_AVAILABLE_OR_INSTALLABLE);

        Uri uri = Uri.parse("http://dev.niklaskorz.de/lgv/versions/" + version + ".apk");
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();

        final DownloadManager downloadManager = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = downloadManager.enqueue(new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setTitle(c.getString(R.string.app_name))
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "lgv.apk"));

        c.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Uri fileUri = downloadManager.getUriForDownloadedFile(downloadId);
                if (fileUri == null) {
                    return;
                }

                context.unregisterReceiver(this);
                showInstallable(context, fileUri);
            }
        }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void showAvailable(Context c, VersionInfo info) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c)
                .setSmallIcon(c.getApplicationInfo().icon)
                .setContentTitle(c.getString(R.string.app_name))
                .setContentText(c.getString(R.string.updates_available));

        Intent updateIntent = new Intent(c, UpdateActivity.class);
        updateIntent.putExtra("versionCode", info.code);
        updateIntent.putExtra("versionName", info.name);
        updateIntent.putExtra("versionDescription", info.description);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(c);
        stackBuilder.addParentStack(UpdateActivity.class);
        stackBuilder.addNextIntent(updateIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NotificationIds.UPDATE_AVAILABLE_OR_INSTALLABLE, builder.build());

        if (AppState.isActive()) {
            c.startActivity(updateIntent);
        }
    }

    private void showInstallable(Context c, Uri fileUri) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c)
                .setSmallIcon(c.getApplicationInfo().icon)
                .setContentTitle(c.getString(R.string.app_name))
                .setContentText(c.getString(R.string.update_loaded))
                .setAutoCancel(true);

        final Intent installerIntent = new Intent(Intent.ACTION_VIEW);
        installerIntent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        installerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(c);
        //stackBuilder.addParentStack(UpdateActivity.class);
        stackBuilder.addNextIntent(installerIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        final NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification notification = builder.build();
        notificationManager.notify(NotificationIds.UPDATE_AVAILABLE_OR_INSTALLABLE, notification);

        final Context activeContext = AppState.getActiveActivity();
        new MaterialDialog.Builder(activeContext)
                .title(R.string.app_name)
                .content(R.string.update_loaded)
                .cancelable(false)
                .positiveText(R.string.install_update)
                .negativeText(R.string.postpone_update)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        activeContext.startActivity(installerIntent);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        materialDialog.dismiss();
                        notificationManager.notify(NotificationIds.UPDATE_AVAILABLE_OR_INSTALLABLE, notification);
                    }
                })
                .show();
    }
}
