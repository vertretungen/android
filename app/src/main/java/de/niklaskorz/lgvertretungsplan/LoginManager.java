package de.niklaskorz.lgvertretungsplan;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by niklaskorz on 21.04.15.
 */
public class LoginManager implements View.OnClickListener, PlanClient.ResponseHandler {
    private Context context;
    private SharedPreferences settings;
    private PlanClient planClient;

    private MaterialDialog dialog;
    @InjectView(R.id.login_progress) ProgressBar loginProgress;
    @InjectView(R.id.login_form) ViewGroup loginForm;
    @InjectView(R.id.login_error) TextView loginError;
    @InjectView(R.id.sign_in_button) Button signInButton;
    @InjectView(R.id.username) AutoCompleteTextView usernameInput;
    @InjectView(R.id.password) EditText passwordInput;

    private PlanClient.ResponseHandler responseHandler;
    private PlanClient.Type planType;
    private boolean loginInProgress = false;

    private String userFullname = "";

    public static class LoginInProgressException extends Exception {}

    public LoginManager(Context c, PlanClient p) {
        context = c;
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        planClient = p;
    }

    public String getUsername() {
        return settings.getString("username", "");
    }

    public String getUserFullname() {
        return userFullname;
    }

    public void login(PlanClient.Type t, PlanClient.ResponseHandler rh) {
        if (loginInProgress) {
            rh.onFailure(new LoginInProgressException());
        }
        loginInProgress = true;

        responseHandler = rh;
        planType = t;

        String username = settings.getString("username", null);
        String password = settings.getString("password", null);
        if (username == null || password == null) {
            showDialog();
            return;
        }

        planClient.setLoginCredentials(username, password);
        planClient.get(planType, this);
    }

    public void logout() {
        planClient.setLoginCredentials(null, null);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.remove("username");
        settingsEditor.remove("password");
        settingsEditor.apply();
        userFullname = "";
    }

    private void showDialog() {
        if (dialog != null) {
            passwordInput.setText("");
            passwordInput.requestFocus();
            loginForm.setVisibility(View.VISIBLE);
            loginProgress.setVisibility(View.GONE);
            return;
        }

        dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_login, true)
                .cancelable(false)
                .autoDismiss(false)
                .build();

        ButterKnife.inject(this, dialog.getCustomView());
        signInButton.setOnClickListener(this);
        usernameInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        usernameInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    return passwordInput.requestFocus();
                }
                return false;
            }
        });
        passwordInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onClick(v);
                    return true;
                }
                return false;
            }
        });

        dialog.show();
    }

    private void closeDialog() {
        if (dialog == null) {
            return;
        }
        dialog.dismiss();
        dialog = null;
        loginProgress = null;
        loginForm = null;
        loginError = null;
        signInButton = null;
        usernameInput = null;
        passwordInput = null;
    }

    @Override
    public void onClick(View v) {
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();

        loginForm.setVisibility(View.GONE);
        loginProgress.setVisibility(View.VISIBLE);

        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putString("username", username);
        settingsEditor.putString("password", password);
        settingsEditor.apply();

        planClient.setLoginCredentials(username, password);
        planClient.get(planType, this);
    }

    @Override
    public void onSuccess(Plan p) {
        closeDialog();
        loginInProgress = false;
        userFullname = p.userFullname;
        responseHandler.onSuccess(p);
    }

    @Override
    public void onFailure(Throwable error) {
        if (error instanceof Plan.ParserException) {
            if (loginError != null) {
                loginError.setVisibility(View.VISIBLE);
            }
            showDialog();
            return;
        }
        closeDialog();
        loginInProgress = false;
        responseHandler.onFailure(error);
        responseHandler = null;
        planType = null;
    }
}
