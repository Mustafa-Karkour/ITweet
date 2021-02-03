package com.example.itweetapp.Models;


import com.google.firebase.database.ServerValue;

public class CommentModel {

    private String comment, userEmail, userName, userPhoto, commentID;
    private Object timeStamp;

    public CommentModel() {
    }

    public CommentModel(String comment, String userEmail, String userName, String userPhoto) {
        this.comment = comment;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.timeStamp = ServerValue.TIMESTAMP;
    }

    public CommentModel(String comment, String userEmail, String userName,String userPhoto, Object timeStamp) {
        this.comment = comment;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.timeStamp = timeStamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Object getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Object timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }
}
