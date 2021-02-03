package com.example.itweetapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.itweetapp.Activities.PostDetailsActivity;
import com.example.itweetapp.Models.PostModel;
import com.example.itweetapp.R;

import java.util.ArrayList;
import java.util.List;

public class PostAdapterRV extends RecyclerView.Adapter<PostAdapterRV.ViewHolder> implements Filterable {


    private Context mContext;
    private ArrayList<PostModel> postsData;
    private ArrayList<PostModel> AllPostsData;

    public PostAdapterRV(Context mContext, ArrayList<PostModel> postsData) {
        this.mContext = mContext;
        this.postsData = postsData;
        this.AllPostsData = new ArrayList<>(postsData); // copy of postsData
    }

    @NonNull
    @Override
    public PostAdapterRV.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View postItem = LayoutInflater.from(mContext).inflate(R.layout.post_row_layout,parent,false);
        return new ViewHolder(postItem);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapterRV.ViewHolder holder, int position) {

        holder.postTitle.setText(postsData.get(position).getTitle());
        Glide.with(mContext).load(postsData.get(position).getImageURL()).into(holder.postImage);
        Glide.with(mContext).load(postsData.get(position).getUserPhoto()).apply(new RequestOptions().circleCrop()).into(holder.userPhoto);
    }

    @Override
    public int getItemCount() {
        return postsData.size();
    }

    @Override
    public Filter getFilter() {
        return searchFilter;
    }

    private Filter searchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence input) {
            ArrayList<PostModel> filteredList = new ArrayList<>();

            if(input == null || input.length() ==0){
                filteredList.addAll(AllPostsData);
            }else{
                String searchInput = input.toString().toLowerCase().trim();

                for(PostModel post : AllPostsData){
                    if(post.getTitle().toLowerCase().contains(searchInput) || post.getDescription().toLowerCase().contains(searchInput)){  //for Case sensitivity
                        filteredList.add(post);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            postsData.clear();
            postsData.addAll( (List) results.values );
            notifyDataSetChanged();

        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView postTitle;
        private ImageView postImage;
        private ImageView userPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            postTitle = itemView.findViewById(R.id.item_post_title);
            postImage = itemView.findViewById(R.id.item_post_img);
            userPhoto = itemView.findViewById(R.id.item_post_user_photo);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent toPostDetailsActivity = new Intent(mContext, PostDetailsActivity.class);
                    int position = getAdapterPosition();

                    toPostDetailsActivity.putExtra("title",postsData.get(position).getTitle());
                    toPostDetailsActivity.putExtra("desc",postsData.get(position).getDescription());
                    toPostDetailsActivity.putExtra("postImage",postsData.get(position).getImageURL());
                    toPostDetailsActivity.putExtra("postID",postsData.get(position).getPostID());
                    toPostDetailsActivity.putExtra("userPhoto",postsData.get(position).getUserPhoto());
                    //toPostDetailsActivity.putExtra("userName",postsData.get(position).getUserName());
                    long timeStamp = (long) postsData.get(position).getTimeStamp();
                    toPostDetailsActivity.putExtra("postTimeStamp",timeStamp);
                    mContext.startActivity(toPostDetailsActivity);
                }
            });
        }
    }
}
