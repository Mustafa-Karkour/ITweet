package com.example.itweetapp.Models;

import com.google.firebase.database.ServerValue;

public class PostModel {
    private String title, description, imageURL, userEmail, userPhoto,postID;
    private Object timeStamp;

    public PostModel(){

    }

    public PostModel(String title, String description, String imageURL, String userEmail, String userPhoto) {
        this.title = title;
        this.description = description;
        this.imageURL = imageURL;
        this.userEmail = userEmail;
        this.userPhoto = userPhoto;
        this.timeStamp = ServerValue.TIMESTAMP; //<-- TIMESTAMP From Firebase DB
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Object getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Object timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }
}
