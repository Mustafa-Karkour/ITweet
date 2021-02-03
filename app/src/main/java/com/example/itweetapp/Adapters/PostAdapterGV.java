package com.example.itweetapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.itweetapp.Activities.PostDetailsActivity;
import com.example.itweetapp.Models.PostModel;
import com.example.itweetapp.R;

import java.util.ArrayList;

public class PostAdapterGV extends BaseAdapter {

    private Context mContext;
    private ArrayList<PostModel> myPosts;

    public PostAdapterGV(Context mContext, ArrayList<PostModel> myPosts) {
        this.mContext = mContext;
        this.myPosts = myPosts;
    }

    @Override
    public int getCount() {
        return myPosts.size();
    }

    @Override
    public Object getItem(int position) {
        return myPosts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.post_row_layout,parent,false);

        TextView postTitle = view.findViewById(R.id.item_post_title);
        ImageView postImage = view.findViewById(R.id.item_post_img);
        ImageView userPhoto = view.findViewById(R.id.item_post_user_photo);

        postTitle.setText(myPosts.get(position).getTitle());
        Glide.with(mContext).load(myPosts.get(position).getImageURL()).into(postImage);
        Glide.with(mContext).load(myPosts.get(position).getUserPhoto()).apply(new RequestOptions().circleCrop()).into(userPhoto);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toPostDetailsActivity = new Intent(mContext, PostDetailsActivity.class);

                toPostDetailsActivity.putExtra("title",myPosts.get(position).getTitle());
                toPostDetailsActivity.putExtra("desc",myPosts.get(position).getDescription());
                toPostDetailsActivity.putExtra("postImage",myPosts.get(position).getImageURL());
                toPostDetailsActivity.putExtra("postID",myPosts.get(position).getPostID());
                toPostDetailsActivity.putExtra("userPhoto",myPosts.get(position).getUserPhoto());
                //toPostDetailsActivity.putExtra("userName",postsData.get(position).getUserName());
                long timeStamp = (long) myPosts.get(position).getTimeStamp();
                toPostDetailsActivity.putExtra("postTimeStamp",timeStamp);
                mContext.startActivity(toPostDetailsActivity);
            }
        });
        return view;
    }


}
