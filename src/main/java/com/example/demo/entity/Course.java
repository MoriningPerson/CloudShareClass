package com.example.demo.entity;

import java.util.List;

public class Course {
    private int id;
    private String name;
    private String url;
    private String cover;
    private String origin;
    private int score;
    private String titleList;
    private String universityList;
    private String contentList;

    public Course() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getTitleList() {
        return titleList;
    }

    public void setTitleList(String titleList) {
        this.titleList = titleList;
    }

    public String getUniversityList() {
        return universityList;
    }

    public void setUniversityList(String universityList) {
        this.universityList = universityList;
    }

    public String getContentList() {
//        System.out.println("contentList " + contentList);
        return contentList;
    }

    public void setContentList(String contentList) {
        this.contentList = contentList;
    }
}