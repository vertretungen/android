package de.niklaskorz.lgvertretungsplan;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by niklaskorz on 21.04.15.
 */
public class LoginManager implements View.OnClickListener {
    private static final String PREFS_NAME = "auth";

    private Context context;
    private SharedPreferences settings;
    private PlanClient planClient;

    private MaterialDialog dialog;
    @InjectView(R.id.sign_in_button) Button signInButton;
    @InjectView(R.id.username) AutoCompleteTextView usernameInput;
    @InjectView(R.id.password) EditText passwordInput;

    public LoginManager(Context c, PlanClient p) {
        context = c;
        settings = context.getSharedPreferences(PREFS_NAME, 0);
        planClient = p;
    }

    public void showDialog() {
        if (dialog != null) {
            return;
        }

        dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_login, true)
                .build();

        ButterKnife.inject(this, dialog.getCustomView());
        signInButton.setOnClickListener(this);

        dialog.show();
    }

    public void closeDialog() {
        dialog.dismiss();
        dialog = null;
        signInButton = null;
        usernameInput = null;
        passwordInput = null;
    }

    @Override
    public void onClick(View v) {
        String name = usernameInput.getText().toString();
        String pw = passwordInput.getText().toString();
        closeDialog();
        SnackbarManager.show(Snackbar
                .with(context)
                .text(String.format("Name: %s, PW: %s", name, pw)));
    }
}
