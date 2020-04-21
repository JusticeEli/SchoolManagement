package com.justice.schoolmanagement.blog;


import java.util.Date;

public class Blog {
    private String userId;
    private String id;
    private String description;
    private String photo;
    private String thumbnail;
    private Date date;

    public Blog() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Blog(String userId, String description, String photo, String thumbnail, Date date) {
        this.userId = userId;
        this.description = description;
        this.photo = photo;
        this.thumbnail = thumbnail;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


}
