package com.fett.model;

import java.util.HashMap;
import java.util.Map;

public class Profile {
    private String uid;
    private String username;
    private String password;

    public Profile() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String id) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map toMap(){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("uid", uid);
        map.put("username", username);
        map.put("password", password);

        return map;
    }
}
