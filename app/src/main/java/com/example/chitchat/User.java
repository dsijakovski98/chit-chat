package com.example.chitchat;

public class User {
    public String name;
    public String status;
    public String image;
    public String thumb_image;
    User() { }

    public User(String name, String status, String image, String thumbImage) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.thumb_image = thumbImage;
    }

    public String getName() { return this.name; }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumbImage() {
        return this.thumb_image;
    }

    public void setThumbImage(String thumbImage) {
        this.thumb_image = thumbImage;
    }


}
