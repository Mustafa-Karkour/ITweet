package com.example.itweetapp.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.itweetapp.Models.CommentModel;
import com.example.itweetapp.R;
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

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private FirebaseUser currentUser;

    private Context mContext;
    private ArrayList<CommentModel> commentList;
    private String postID;

    public CommentAdapter(Context mContext, ArrayList<CommentModel> commentList, String postID) {
        this.mContext = mContext;
        this.commentList = commentList;
        this.postID = postID;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View commentItem = LayoutInflater.from(mContext).inflate(R.layout.comment_row_layout,parent,false);

        return new ViewHolder(commentItem);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentAdapter.ViewHolder holder, final int position) {

        String userPhoto = commentList.get(position).getUserPhoto();
        Glide.with(mContext).load(userPhoto).apply(new RequestOptions().circleCrop()).into(holder.userPhoto);

        String userName = commentList.get(position).getUserName();
        holder.userName.setText(userName);

        String userComment = commentList.get(position).getComment();
        holder.userComment.setText(userComment);

        String commentDate = timeStampToString((long) commentList.get(position).getTimeStamp());
        holder.commentDate.setText(commentDate);

        holder.deleteCommentBtn.setVisibility(View.INVISIBLE);
        holder.deleteCommentBtn.setImageResource(R.mipmap.bin);

        if(commentList.get(position).getUserEmail().equals(currentUser.getEmail())){

            holder.deleteCommentBtn.setVisibility(View.VISIBLE);

            holder.deleteCommentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteComment(position);
                }
            });

        }

        /*
        holder.itemView.setOnClickListener(new View.OnClickListener() { //<-- commentItem selected
            @Override
            public void onClick(View v) {

                deleteComment(position);  // my comment
            }
        });

         */

        //deleteComment(position);  // my comment

    }



    public void deleteComment(final int commentIndex) {

        final String commentUserEmail = commentList.get(commentIndex).getUserEmail();
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Comments").child(postID);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot commentSnapshot : dataSnapshot.getChildren()){


                    String currentUserEmail = currentUser.getEmail();

                    if(commentUserEmail.equals(currentUserEmail)){

                        if(commentSnapshot.getKey().equals(commentList.get(commentIndex).getCommentID())) {
                            myRef.child(commentSnapshot.getKey()).removeValue();
                            showMessage("Deleted");
                        }
                    }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    private void showMessage(String message){
        Toast.makeText(mContext,message,Toast.LENGTH_LONG).show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView userPhoto, deleteCommentBtn;
        private TextView userName, userComment, commentDate;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            userPhoto = itemView.findViewById(R.id.comment_user_photo);
            userName = itemView.findViewById(R.id.comment_username);
            userComment = itemView.findViewById(R.id.comment_user_here);
            commentDate = itemView.findViewById(R.id.comment_date);
            deleteCommentBtn = itemView.findViewById(R.id.delete_comment_btn);
        }
    }

    private String timeStampToString(long timeStamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timeStamp);
        String date = DateFormat.format("hh:mm",calendar).toString();
        return date;
    }

}
