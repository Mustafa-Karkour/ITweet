package com.example.itweetapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.itweetapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Button loginBtn;
    private ProgressBar loginLoadingBar;
    private TextView signUpBtn;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_pass);
        loginBtn = findViewById(R.id.login_btn);
        loginLoadingBar = findViewById(R.id.login_loading_bar);
        loginLoadingBar.setVisibility(View.INVISIBLE);
        signUpBtn = findViewById(R.id.tv_sign_up);
        firebaseAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginBtn.setVisibility(View.INVISIBLE);
                loginLoadingBar.setVisibility(View.VISIBLE);

                final String EMAIL = loginEmail.getText().toString().trim();
                final String PASSWORD = loginPassword.getText().toString();

                if(EMAIL.isEmpty() && PASSWORD.isEmpty()){
                    showMessage("Empty Fields");

                    loginBtn.setVisibility(View.VISIBLE);
                    loginLoadingBar.setVisibility(View.INVISIBLE);
                }else if(EMAIL.isEmpty() || !(Patterns.EMAIL_ADDRESS.matcher(EMAIL).matches())){
                    loginBtn.setVisibility(View.VISIBLE);
                    loginLoadingBar.setVisibility(View.INVISIBLE);
                    if(EMAIL.isEmpty()) {
                        loginEmail.setFocusable(true);
                        loginEmail.setError("Empty Field");
                    }else {
                        loginEmail.setFocusable(true);
                        loginEmail.setError("Invalid Email");
                    }
                }else if(PASSWORD.isEmpty()){
                    loginPassword.setError("Empty Fields");
                    loginPassword.setFocusable(true);

                    loginBtn.setVisibility(View.VISIBLE);
                    loginLoadingBar.setVisibility(View.INVISIBLE);
                }else{
                    login(EMAIL, PASSWORD);
                }
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,SignUpActivity.class));
                finish();
            }
        });

    }

    private void login(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                        updateUI();
                }else{
                    loginBtn.setVisibility(View.VISIBLE);
                    loginLoadingBar.setVisibility(View.VISIBLE);
                    showMessage("Registration Failed: "+task.getException().getMessage());
                }
            }
        });
    }

    private void showMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }

    private void updateUI() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onStart() { //<-- to remember a registered User So, when a user exit the app he doesn;t need to login again
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null){
            // User is already registered
            updateUI();
        }
    }
}
