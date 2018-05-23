package com.example.divakrishna.wind;

public class Post {
    private String desc;

    private String username;

    private String userimage;

    private String timestamp;

    private String comment;

    public Post(){

    }

    public Post(String desc, String username, String userimage, String timestamp) {

        this.desc = desc;
        this.username = username;
        this.userimage = userimage;
        this.timestamp = timestamp;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getUserimage() {
        return userimage;
    }

    public void setUserimage(String userimage) {
        this.userimage = userimage;
    }


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }



    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
