package ch.epfl.sdp.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.epfl.sdp.MainActivity;
import ch.epfl.sdp.R;
import ch.epfl.sdp.database.authentication.AuthenticationAPI;
import ch.epfl.sdp.database.firebase.api.CommonDatabaseAPI;
import ch.epfl.sdp.database.firebase.entityForFirebase.UserForFirebase;
import ch.epfl.sdp.dependencies.AppContainer;
import ch.epfl.sdp.dependencies.MyApplication;

public class RegisterFormActivity extends AppCompatActivity {
    private static final String REGEX = "^[A-Za-z0-9.]{1,20}@.{1,20}$";
    private static final int MINIMUM_PASSWORD_LENGTH = 8;

    private EditText txtUsername, txtEmail, txtPassword, txtPasswordConf;

    private AuthenticationAPI authenticationAPI;
    private CommonDatabaseAPI commonDatabaseAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerform);

        txtUsername = findViewById(R.id.username);
        txtEmail = findViewById(R.id.email);
        txtPassword = findViewById(R.id.txtRegisterPassword);
        txtPasswordConf = findViewById(R.id.passwordconf);

        AppContainer appContainer = ((MyApplication) getApplication()).appContainer;
        authenticationAPI = appContainer.authenticationAPI;
        commonDatabaseAPI = appContainer.commonDatabaseAPI;
    }

    public void registerBtn_OnClick(View view) {
        final String username = txtUsername.getText().toString();
        final String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        String passwordConf = txtPasswordConf.getText().toString().trim();

        switch (checkValidity(email, password, passwordConf)) {
            case 1:
                txtEmail.setError("Email is incorrect");
                return;
            case 2:
                txtPassword.setError("Password is incorrect");
                return;
        }

        register(username, email, password);
    }

    private void register(String username, String email, String password) {
        authenticationAPI.register(email, password, registerRes -> {
            if (!registerRes.isSuccessful()) {
                Toast.makeText(RegisterFormActivity.this, registerRes.getException().getMessage(), Toast.LENGTH_LONG).show();
            } else {
                UserForFirebase userForFirebase = new UserForFirebase(email, username, 0);
                commonDatabaseAPI.addUser(userForFirebase, addUserRes -> {
                    if (!addUserRes.isSuccessful()) {
                        Toast.makeText(RegisterFormActivity.this, addUserRes.getException().getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        RegisterFormActivity.this.startActivity(new Intent(RegisterFormActivity.this, MainActivity.class));
                        RegisterFormActivity.this.finish();
                    }
                });
            }
        });
    }

    public void backBtn_OnClick(View view) {
        startActivity(new Intent(RegisterFormActivity.this, LoginFormActivity.class));
        finish();
    }

    public int checkValidity(String email, String password, String passwordConf) {
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(email);
        if ((!matcher.matches())) {
            return 1;
        }
        if (password.length() < MINIMUM_PASSWORD_LENGTH || (!passwordConf.equals(password))) {
            return 2;
        }
        return 0;
    }
}
