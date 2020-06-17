package com.example.worldquiz.Model;

public class Ranking {          //Ranking model to upload data in Ranking path
    private String name;
    private long score;
    private String profileImage;

    public Ranking() {

    }

    public Ranking(String name, long score, String profileImage) {
        this.name = name;
        this.score = score;
        this.profileImage = profileImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
