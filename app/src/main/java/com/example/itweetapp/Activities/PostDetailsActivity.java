package com.example.itweetapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.itweetapp.Adapters.CommentAdapter;
import com.example.itweetapp.Models.CommentModel;
import com.example.itweetapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class PostDetailsActivity extends AppCompatActivity {

    private ImageView postImage, currentUserPhoto, ownerPhoto, deletePostBtn;
    private TextView postTitle, postDate, postDesc;
    private EditText comment;
    private Button addCommentBtn;
    private String postID ,userEmail;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private RecyclerView commentSectionRV;
    private CommentAdapter commentAdapter;
    public static ArrayList<CommentModel> commentList = new ArrayList<>();
    private static String COMMENTS = "Comments";
    private CommentModel myComment = new CommentModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getSupportActionBar().hide();

        postImage = findViewById(R.id.post_detail_img); // Owner Post
        ownerPhoto = findViewById(R.id.post_detail_ower_photo); // Owner User Profile Photo
        currentUserPhoto = findViewById(R.id.post_detail_currentuser_photo); // Another User
        postTitle = findViewById(R.id.post_detail_title);
        postDate = findViewById(R.id.post_detail_date);
        postDesc = findViewById(R.id.post_detail_desc);
        comment = findViewById(R.id.post_detail_comment);
        addCommentBtn = findViewById(R.id.post_detail_add_comment_btn);
        deletePostBtn = findViewById(R.id.delete_post_btn);
        deletePostBtn.setVisibility(View.INVISIBLE);
        commentSectionRV = findViewById(R.id.comment_section_RV);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        String title = getIntent().getStringExtra("title");
        postTitle.setText(title);

        String desc = getIntent().getStringExtra("desc");
        postDesc.setText(desc);

        String postImageURL = getIntent().getStringExtra("postImage");
        Glide.with(this).load(postImageURL).into(postImage);

        String ownerPostPhotoURL = getIntent().getStringExtra("userPhoto");
        Glide.with(this).load(ownerPostPhotoURL).apply(new RequestOptions().circleCrop()).into(ownerPhoto);

        Glide.with(this).load(currentUser.getPhotoUrl()).apply(new RequestOptions().circleCrop()).into(currentUserPhoto);

        long postTimeStamp = getIntent().getExtras().getLong("postTimeStamp");
        postDate.setText(timeStampToString(postTimeStamp));

        postID = getIntent().getStringExtra("postID");

        addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCommentBtn.setVisibility(View.INVISIBLE);

                DatabaseReference myRef = database.getReference(COMMENTS).child(postID).push();
                String ID = myRef.getKey(); //<-- unique Post ID (identifier)


                final String COMMENTCONTENT = comment.getText().toString().trim();
                final String USEREMAIL = currentUser.getEmail();
                final String USERNAME = currentUser.getDisplayName();
                final String USERPHOTO = currentUser.getPhotoUrl().toString();

                myComment = new CommentModel(COMMENTCONTENT, USEREMAIL, USERNAME, USERPHOTO);
                myComment.setCommentID(ID); //save as a variable

                myRef.setValue(myComment).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            showMessage("Added Successfully");
                            comment.setText("");
                            addCommentBtn.setVisibility(View.VISIBLE);
                        }else{
                            showMessage("Exception: "+task.getException().getMessage());
                        }
                    }
                });

            }
        });


        myRef = database.getReference(COMMENTS).child(postID);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList = new ArrayList<>();
                for(DataSnapshot commentSnapshot : dataSnapshot.getChildren()){
                    myComment = commentSnapshot.getValue(CommentModel.class);
                    commentList.add(myComment);
                }

                commentAdapter = new CommentAdapter(getApplicationContext(),commentList, postID );
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
                linearLayoutManager.setStackFromEnd(true);
                linearLayoutManager.setReverseLayout(true);
                commentSectionRV.setLayoutManager(linearLayoutManager);
                commentSectionRV.setAdapter(commentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        setDeleteBtn();
    }

    private void setDeleteBtn() {

        DatabaseReference postRef = database.getReference("Posts");
        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    userEmail = postSnapshot.child("userEmail").getValue(String.class);

                    if(postSnapshot.getKey().equals(postID)) {
                        if (userEmail.equals(currentUser.getEmail())) {
                            deletePostBtn.setVisibility(View.VISIBLE);

                            deletePostBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteDialog();
                                }
                            });

                        } else {
                            deletePostBtn.setVisibility(View.INVISIBLE);
                        }
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    private void deleteDialog() {

        final Dialog alertDialog = new Dialog(this);
        alertDialog.setContentView(R.layout.delete_dialog_message);
        alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);

        Button cancelBtn = alertDialog.findViewById(R.id.delete_cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        Button confirmBtn = alertDialog.findViewById(R.id.delete_confirm_btn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePostFromDB(postID);
                alertDialog.dismiss();
                onBackPressed();
            }
        });



        alertDialog.show();

    }

    private void deletePostFromDB(String postID) {

        DatabaseReference postReference = database.getReference("Posts").child(postID);
        DatabaseReference commentReference = database.getReference("Comments").child(postID);

        postReference.removeValue();
        commentReference.removeValue();

        showMessage("Deleted");

    }

    private String timeStampToString(long timeStamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timeStamp);
        String date = DateFormat.format("dd-MM-yyyy",calendar).toString();
        return date;
    }

    private void showMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }


}
