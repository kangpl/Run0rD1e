package ch.epfl.sdp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import ch.epfl.sdp.game.DatabaseHelper;

public class LoginFormActivity extends AppCompatActivity {
    public AuthenticationController authenticationController;
    private EditText lemail, lpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginform);

        lemail = findViewById(R.id.emaillog);
        lpassword = findViewById(R.id.passwordlog);

        authenticationController = new FirebaseAuthentication(new FirestoreUserData());
        DatabaseHelper.UserData loggedUser = new DatabaseHelper(this).getLoggedUser();
        if(loggedUser != null)  {
            MainActivity.startGame();
            authenticationController.signIn(LoginFormActivity.this, loggedUser.email, loggedUser.password);
        }
    }

   public void createAccountBtn_OnClick(View view) {
       startActivity(new Intent(LoginFormActivity.this, RegisterFormActivity.class));
       finish();
    }

    public void loginBtn_OnClick(View view) {
        // temporarily: should be called when login is successfull
        MainActivity.startGame();

        String email = lemail.getText().toString().trim();
        String password = lpassword.getText().toString().trim();

        if (email.isEmpty()) {
            lemail.setError("Email can't be empty");
            return;
        }
        if (password.isEmpty()) {
            lpassword.setError("Password can't be empty");
            return;
        }
        authenticationController.signIn(LoginFormActivity.this, email, password);
    }
}
