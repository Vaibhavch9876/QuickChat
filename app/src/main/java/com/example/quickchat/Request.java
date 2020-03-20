package com.example.quickchat;

public class Request {

    private String type;

    public Request(String type) {
        this.type = type;
    }

    public Request() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
