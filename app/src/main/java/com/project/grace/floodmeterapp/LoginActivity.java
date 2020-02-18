package com.project.grace.floodmeterapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.project.grace.floodmeterapp.helpers.EmailValidator;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private TextView txtRegister;
    private EditText txtPassword;
    private EditText txtEmail;
    private Button btnLogin;
    private FirebaseAuth firebaseAuth;
    private EmailValidator emailValidator;
    private ProgressBar progressBar;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        updateUI(firebaseAuth.getCurrentUser());


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.

        emailValidator = new EmailValidator();
        txtRegister = findViewById(R.id.register_new);
        txtPassword = findViewById(R.id.password);
        txtEmail = findViewById(R.id.email);
        btnLogin = findViewById(R.id.email_sign_in_button);
        progressBar = findViewById(R.id.login_progress);

        txtRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });
        btnLogin.setOnClickListener(v -> {
            String email = txtEmail.getText().toString().trim();
            String password = txtPassword.getText().toString();
            if (email.equals("")) {
                txtEmail.setError("Please enter an email.");
                return;
            }
            if (email.equals("")) {
                txtEmail.setError("Please enter a password.");
                return;
            }
            if (!emailValidator.validateEmail(email)) {
                txtEmail.setError("Please enter a valid email.");
                return;
            }
            showLoading();
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        hideLoading();
                        if (task.isSuccessful()) {
                            updateUI(firebaseAuth.getCurrentUser());
                        } else {
                            Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.GONE);
        txtRegister.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnLogin.setVisibility(View.VISIBLE);
        txtRegister.setVisibility(View.VISIBLE);
    }
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
    }
}

