package com.fett.model;

import java.util.HashMap;
import java.util.Map;

public class Profile {
    private String uid;
    private String username;
    private String password;
<<<<<<< HEAD
=======
    private Plan plan;
>>>>>>> origin/master

    public Profile() {
    }

    public String getUid() {
        return uid;
    }

<<<<<<< HEAD
    public void setUid(String id) {
        this.uid = uid;
    }

=======
    public void setUid(String uid) {
        this.uid = uid;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

>>>>>>> origin/master
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
<<<<<<< HEAD
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("uid", uid);
        map.put("username", username);
        map.put("password", password);
=======
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("username", username);
        map.put("password", password);
        map.put("plan", plan);
>>>>>>> origin/master

        return map;
    }
}
