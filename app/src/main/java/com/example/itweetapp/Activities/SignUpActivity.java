package com.example.itweetapp.Activities;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.itweetapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class SignUpActivity extends AppCompatActivity  {

    private ImageView userImage;
    private Uri pickedImage;
    private EditText userName, userEmail, userPass1, userPass2;
    private Button signUpBtn;
    private ProgressBar loadingBar;
    private TextView loginBtn;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    //private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        //database = FirebaseDatabase.getInstance();

        userImage = findViewById(R.id.reg_user_photo);
        userName = findViewById(R.id.reg_username);
        userEmail = findViewById(R.id.reg_user_email);
        userPass1 = findViewById(R.id.reg_user_pass1);
        userPass2 = findViewById(R.id.reg_user_pass2);
        signUpBtn = findViewById(R.id.sign_up_btn);
        loginBtn = findViewById(R.id.tv_login);

        loadingBar = findViewById(R.id.reg_loading_bar);
        loadingBar.setVisibility(View.INVISIBLE);

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpBtn.setVisibility(View.INVISIBLE);
                loadingBar.setVisibility(View.VISIBLE);

                final String NAME = userName.getText().toString().trim();
                final String EMAIL = userEmail.getText().toString().trim();
                final String PASS1 = userPass1.getText().toString();
                final String PASS2 = userPass2.getText().toString();

                if(NAME.isEmpty() && EMAIL.isEmpty() && PASS1.isEmpty() && PASS2.isEmpty()){
                    showMessage("Empty Fields");

                    signUpBtn.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.INVISIBLE);
                }else if(NAME.isEmpty()){
                    userName.setFocusable(true);
                    userName.setError("Empty Field");

                    signUpBtn.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.INVISIBLE);
                }else if(EMAIL.isEmpty() || !(Patterns.EMAIL_ADDRESS.matcher(EMAIL).matches())){
                    signUpBtn.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.INVISIBLE);

                    if(EMAIL.isEmpty()){
                        userEmail.setError("Empty Field");
                        userEmail.setFocusable(true);
                    }else{
                        userEmail.setError("Invalid Email");
                        userEmail.setFocusable(true);
                    }
                }else if(PASS1.isEmpty() || PASS1.length() < 6){
                    signUpBtn.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.INVISIBLE);

                    if(PASS1.isEmpty()){
                        userPass1.setFocusable(true);
                        userPass1.setError("Empty Field");
                    }else{
                        userPass1.setFocusable(true);
                        userPass1.setError("Password is less than 6 char");
                    }
                }else if(PASS2.isEmpty()){
                    signUpBtn.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.INVISIBLE);

                    userPass2.setError("Empty Field");
                    userPass2.setFocusable(true);
                }else if(!(PASS1.equals(PASS2))){
                    signUpBtn.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.INVISIBLE);

                    userPass1.setFocusable(true);
                    userPass2.setFocusable(true);
                    showMessage("Passwords don't match-up");
                }else if(pickedImage == null){
                    signUpBtn.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.INVISIBLE);

                    showMessage("No image picked, yet");
                }else if(currentUser !=null){

                    showMessage("Email is already registered");
                }
                else
                {
                    createUserAccount(NAME, EMAIL, PASS1);
                }
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this,LoginActivity.class));
            }
        });
    }

    private void createUserAccount(final String userName, String userEmail, String userPassword) {
        firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    updateUserInfo(userName, pickedImage, firebaseAuth.getCurrentUser());
                }else{
                    showMessage("Registration Failed: "+task.getException().getMessage());
                }
            }
        });
    }

    private void updateUserInfo(final String userName, Uri pickedImage, final FirebaseUser currentUser) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Users_photo");
        final StorageReference imageFilePath = storageReference.child(currentUser.getEmail());
        imageFilePath.putFile(pickedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(userName)
                                .setPhotoUri(uri)
                                .build();

                        currentUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    showMessage("Welcome "+userName);
                                    userImage.setImageURI(currentUser.getPhotoUrl());
                                    updateUI();
                                }
                            }
                        });
                    }
                });
            }
        });
    }


    private void updateUI() {
        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
        finish();
    }

    private void openGallery() {
        Intent toGallery = new Intent(Intent.ACTION_GET_CONTENT);
        toGallery.setType("image/*"); // <-- accept any type of images
        startActivityForResult(toGallery,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data !=null && requestCode ==0){
            pickedImage = data.getData();
            userImage.setImageURI(pickedImage);
        }
    }

    private void showMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }
}
