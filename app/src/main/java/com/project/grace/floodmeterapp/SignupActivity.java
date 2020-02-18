package com.project.grace.floodmeterapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.project.grace.floodmeterapp.helpers.EmailValidator;

import org.w3c.dom.Text;

public class SignupActivity extends AppCompatActivity {

    Button btnCreateAccount;
    TextView txtLogin;
    EditText txtEmail;
    EditText txtPassword;
    EditText txtPasswordConfirm;
    FirebaseAuth firebaseAuth;
    EmailValidator emailValidator;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        emailValidator = new EmailValidator();
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.login_progress);
        btnCreateAccount = findViewById(R.id.email_sign_in_button);
        txtLogin = findViewById(R.id.login);
        txtEmail = findViewById(R.id.email);
        txtPassword = findViewById(R.id.password);
        txtPasswordConfirm = findViewById(R.id.password_confirm);

        btnCreateAccount.setOnClickListener(v -> {
            String email = txtEmail.getText().toString().trim(),
                    password = txtPassword.getText().toString().trim(),
                    passwordConfirm = txtPasswordConfirm.getText().toString().trim();

            if (email.equals("")) {
                txtEmail.setError("Please enter an email address.");
                return;
            }
            if (!emailValidator.validateEmail(email)) {
                txtEmail.setError("Please enter a valid email address.");
                return;
            }
            if (password.equals("")) {
                txtPassword.setError("Please enter a password.");
                return;
            }
            if (password.length() < 6) {
                txtPassword.setError("You password should be greater than 6 characters.");
                return;
            }
            if (!password.equals(passwordConfirm)) {
                txtPasswordConfirm.setError("Passwords does not match.");
                return;
            }
            showLoading();
            //create account in firebase
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            // updateUI(user);
                            hideLoading();
                            gotoLogin();
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            hideLoading();
                            // updateUI(null);
                        }
                    });
        });
        txtLogin.setOnClickListener(v -> {
            gotoLogin();
        });
    }

    private void gotoLogin() {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        btnCreateAccount.setVisibility(View.GONE);
        txtLogin.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnCreateAccount.setVisibility(View.VISIBLE);
        txtLogin.setVisibility(View.VISIBLE);
    }
}
