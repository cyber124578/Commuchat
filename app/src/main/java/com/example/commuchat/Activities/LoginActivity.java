package com.example.commuchat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.commuchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText usermail,userpassword;
    private Button btnLogin;
    private ProgressBar  loginProgress;
    private FirebaseAuth mAuth;
    private Intent HomeActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usermail =findViewById(R.id.login_mail);
        userpassword =findViewById(R.id.login_password);
        btnLogin = findViewById(R.id.login_btn);
        loginProgress = findViewById(R.id.login_progress);
        mAuth =FirebaseAuth.getInstance();
        HomeActivity = new Intent(this, com.example.commuchat.Activities.Home.class);
        ImageView loginPhoto = findViewById(R.id.login_photo);
        loginPhoto.setOnClickListener(view -> {
            Intent registerActivity = new Intent(getApplicationContext(),RegisterActivity.class);
            startActivity(registerActivity);
            finish();
        });
        loginProgress.setVisibility(View.INVISIBLE);
        btnLogin.setOnClickListener(view -> {
            loginProgress.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.INVISIBLE);

            final String mail =usermail.getText().toString();
            final String password = userpassword.getText().toString();

            if (mail.isEmpty() || password.isEmpty()) {
                showMessage("Please verify the fields");
                btnLogin.setVisibility(View.VISIBLE);
                loginProgress.setVisibility(View.INVISIBLE);
            }
            else {
                SignIn(mail,password);
            }
        });


    }

    private void SignIn(String mail, String password) {
        mAuth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                loginProgress.setVisibility(View.INVISIBLE);
                btnLogin.setVisibility(View.VISIBLE);
                updateUI();
            }
            else
            {
                showMessage(Objects.requireNonNull(task.getException()).getMessage());
                btnLogin.setVisibility(View.VISIBLE);
                loginProgress.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void updateUI() {
        startActivity(HomeActivity);
        finish();
    }

    private void showMessage(String text) {
        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user =  mAuth.getCurrentUser();
        if (user!=null) {
            updateUI();
        }
    }
}

