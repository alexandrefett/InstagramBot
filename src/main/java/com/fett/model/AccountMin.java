
package com.fett.model;

import java.util.HashMap;
import java.util.Map;

public class AccountMin {
    Boolean followedByViewer;
    Boolean followsViewer;
    String fullName;
    long id;
    Boolean isVerified;
    String profilePictureUrl;
    Boolean requestedByViewer;
    String username;
    private long date;

    public AccountMin() {
    }

    public Map toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("followedByViewer", followedByViewer);
        map.put("followsViewer", followsViewer);
        map.put("fullName", fullName);
        map.put("id", id);
        map.put("isVerified", isVerified);
        map.put("profilePictureUrl", profilePictureUrl);
        map.put("requestedByViewer", requestedByViewer);
        map.put("username", username);
        map.put("long date", date);

        return map;
    }

    public Boolean getFollowedByViewer() {
        return followedByViewer;
    }

    public void setFollowedByViewer(Boolean followedByViewer) {
        this.followedByViewer = followedByViewer;
    }

    public Boolean getFollowsViewer() {
        return followsViewer;
    }

    public void setFollowsViewer(Boolean followsViewer) {
        this.followsViewer = followsViewer;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public Boolean getRequestedByViewer() {
        return requestedByViewer;
    }

    public void setRequestedByViewer(Boolean requestedByViewer) {
        this.requestedByViewer = requestedByViewer;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
