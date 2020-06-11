package com.example.worldquiz.Model;

public class Score {
    private String Question_score;
    private String User;
    private String Score;
    private String CategoryId;
    private String CategoryName;
    private String UserName;
    public Score() {

    }

    public Score(String question_score, String user, String score, String categoryId, String categoryName, String userName) {
        Question_score = question_score;
        User = user;
        Score = score;
        CategoryId = categoryId;
        CategoryName = categoryName;
        UserName = userName;
    }

    public String getQuestion_score() {
        return Question_score;
    }

    public void setQuestion_score(String question_score) {
        Question_score = question_score;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }

    public String getScore() {
        return Score;
    }

    public void setScore(String score) {
        Score = score;
    }

    public String getCategoryId() {
        return CategoryId;
    }

    public void setCategoryId(String categoryId) {
        CategoryId = categoryId;
    }

    public String getCategoryName() {
        return CategoryName;
    }

    public void setCategoryName(String categoryName) {
        CategoryName = categoryName;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }
}
