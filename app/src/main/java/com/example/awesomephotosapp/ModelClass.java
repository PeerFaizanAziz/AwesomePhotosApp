package com.example.awesomephotosapp;

import java.util.List;

import retrofit2.http.Url;

public class ModelClass {
    String id;
    String description;
    Urls urls;
    User user;

    public User getUser() {
        return user;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Urls getUrls() {
        return urls;
    }
}