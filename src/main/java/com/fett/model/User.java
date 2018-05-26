package com.fett.model;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String uid;
    private String instagram;
    private String instapass;

    public User() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String id) {
        this.uid = uid;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getInstapass() {
        return instapass;
    }

    public void setInstapass(String instapass) {
        this.instapass = instapass;
    }

    public Map toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("instagram", instagram);
        map.put("instapass", instapass);

        return map;
    }
}
