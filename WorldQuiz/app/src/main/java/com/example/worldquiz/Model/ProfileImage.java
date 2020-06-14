package com.example.worldquiz.Model;

public class ProfileImage {
    private String name;
    private String profileImage;

    public ProfileImage(){}

    public ProfileImage(String name, String profileImage) {
        this.name = name;
        this.profileImage = profileImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
