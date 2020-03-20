package com.example.quickchat;

import java.util.HashMap;

public class ChatUser {

    HashMap<String, Object> key;

    public ChatUser(HashMap<String, Object> key) {
        this.key = key;
    }

    public ChatUser() {
    }

    public HashMap<String, Object> getKey() {
        return key;
    }

    public void setKey(HashMap<String, Object> key) {
        this.key = key;
    }
}
