package com.example.itweetapp.Activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.itweetapp.Fragments.ExploreFragment;
import com.example.itweetapp.Fragments.ProfileFragment;
import com.example.itweetapp.Fragments.SettingsFragment;
import com.example.itweetapp.Models.PostModel;
import com.example.itweetapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    //private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private Toolbar toolbar;

    private Dialog addPostDialog;
    public static ImageView userImage;
    private ImageView addPostImage ,addPostBtn;
    private Uri pickedImage = null;

    public static ImageView navUserPhoto;
    private NavigationView navigationView;
    private View headerView;

    private TextView addPostTitle, addPostDesc;
    private ProgressBar addPostLoadingBar;

    public static TextView navUsername;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    public static ArrayList<String> postsSegments = new ArrayList<>(); //for SettingsFragment

    private MenuItem searchItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Explore");
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();


        final FloatingActionButton addPostIcon = findViewById(R.id.addPostIcon);
        addPostIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPostDialog();
                choosePostImage();

            }
        });



        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true); // Home/Explore is default checked
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id == R.id.nav_explore){
                    addPostIcon.setVisibility(View.VISIBLE);
                    toolbar.setTitle("Explore");
                    searchItem.setVisible(true);
                    getSupportFragmentManager().beginTransaction().replace(R.id.home_container, new ExploreFragment()).commit();
                }else if(id == R.id.nav_profile){
                    toolbar.setTitle("Profile");
                    searchItem.setVisible(false);
                    addPostIcon.setVisibility(View.GONE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.home_container, new ProfileFragment()).commit();
                }else if(id == R.id.nav_settings){
                    toolbar.setTitle("Settings");
                    searchItem.setVisible(false);
                    addPostIcon.setVisibility(View.GONE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.home_container, new SettingsFragment()).commit();
                }else if (id == R.id.nav_logout){
                    firebaseAuth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }

                drawer.closeDrawers(); // close drawer when item is tapped
                return true;
            }
        });


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_tools, R.id.nav_share, R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();

         */

        updateNavHeader();

        getSupportFragmentManager().beginTransaction().replace(R.id.home_container, new ExploreFragment()).commit(); //Home Fragment is the default fragment
    }

    private void choosePostImage() {
        addPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.home, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);

        searchItem = menu.findItem(R.id.search_post);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ExploreFragment.postAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }

    public void updateNavHeader(){
        navigationView = findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0); //<-- Section 1
        navUsername = headerView.findViewById(R.id.nav_username);
        TextView navUserEmail = headerView.findViewById(R.id.nav_user_email);
        navUserPhoto = headerView.findViewById(R.id.nav_user_photo);

        navUserEmail.setText(currentUser.getEmail());
        navUsername.setText(currentUser.getDisplayName());
        Glide.with(headerView.getContext()).load(currentUser.getPhotoUrl()).apply(new RequestOptions().circleCropTransform()).into(navUserPhoto);


    }

    private void startPostDialog(){
        addPostDialog = new Dialog(this);
        addPostDialog.setContentView(R.layout.add_post_dialog_layout);
        userImage = addPostDialog.findViewById(R.id.add_post_user_photo);
        addPostImage = addPostDialog.findViewById(R.id.add_post_image);
        addPostBtn = addPostDialog.findViewById(R.id.add_post_btn);
        addPostTitle = addPostDialog.findViewById(R.id.add_post_title);
        addPostDesc = addPostDialog.findViewById(R.id.add_post_desc);
        addPostLoadingBar = addPostDialog.findViewById(R.id.add_post_loading_bar);

        Glide.with(MainActivity.this).load(currentUser.getPhotoUrl()).apply(new RequestOptions().circleCrop()).into(userImage);
        //////////////////////////////////////// ADD POST ////////////////////////////////////////

        addPostLoadingBar.setVisibility(View.INVISIBLE);

        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPostLoadingBar.setVisibility(View.VISIBLE);
                addPostBtn.setVisibility(View.INVISIBLE);

                final String POSTTITLE = addPostTitle.getText().toString().trim();
                final String POSTDESC = addPostDesc.getText().toString().trim();

                if(POSTTITLE.isEmpty() && POSTDESC.isEmpty() && pickedImage == null){
                    addPostLoadingBar.setVisibility(View.INVISIBLE);
                    addPostBtn.setVisibility(View.VISIBLE);

                    showMessage("Empty Fields");
                }else if(POSTTITLE.isEmpty()){
                    addPostLoadingBar.setVisibility(View.INVISIBLE);
                    addPostBtn.setVisibility(View.VISIBLE);

                    addPostTitle.setFocusable(true);
                    addPostTitle.setError("Empty Title");
                }else if(POSTDESC.isEmpty()){
                    addPostLoadingBar.setVisibility(View.INVISIBLE);
                    addPostBtn.setVisibility(View.VISIBLE);

                    addPostDesc.setError("Empty Description");
                    addPostDesc.setFocusable(true);
                }else if(pickedImage == null){
                    addPostLoadingBar.setVisibility(View.INVISIBLE);
                    addPostBtn.setVisibility(View.VISIBLE);

                    showMessage("No image picked, yet.");
                }else{
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Post_images");
                    final StorageReference imageFilePath = storageReference.child(pickedImage.getLastPathSegment()+"("+currentUser.getEmail()+")");
                    postsSegments.add(pickedImage.getLastPathSegment());
                    imageFilePath.putFile(pickedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String postImageURL = uri.toString();
                                    String userEmail = currentUser.getEmail(); //<-- Unique Email (identifier)
                                    String userPhoto = currentUser.getPhotoUrl().toString();
                                    PostModel myPost = new PostModel(POSTTITLE,POSTDESC,postImageURL,userEmail,userPhoto);
                                    addPost(myPost); // to DB
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showMessage("Exception: "+e.getMessage());
                                    addPostBtn.setVisibility(View.VISIBLE);
                                    addPostLoadingBar.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    });
                }

            }
        });

        //////////////////////////////////////// ADD POST ////////////////////////////////////////
        addPostDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        addPostDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT,Toolbar.LayoutParams.WRAP_CONTENT);
        addPostDialog.getWindow().getAttributes().gravity = Gravity.TOP;
        addPostDialog.show();
    }

    private void addPost(PostModel myPost) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Posts").push();
        String ID = myRef.getKey(); //<-- unique Post ID (identifier)
        myPost.setPostID(ID); //save as a variable

        myRef.setValue(myPost).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showMessage("Post Added Successfully");
                    addPostBtn.setVisibility(View.VISIBLE);
                    addPostLoadingBar.setVisibility(View.INVISIBLE);
                    addPostDialog.dismiss();
                }else{
                    showMessage("Exception: "+task.getException().getMessage());
                }
            }
        });
    }

    private void openGallery() {
        Intent toGallery = new Intent(Intent.ACTION_GET_CONTENT);
        toGallery.setType("image/*"); // <-- accept any type of images
        startActivityForResult(toGallery,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data !=null && requestCode == 1){
            pickedImage = data.getData();
            addPostImage.setImageURI(pickedImage);
        }
    }

    private void showMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }

}
