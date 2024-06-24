package com.example.myapplication;

public class FireBaseModel {
    private String Title;
    private String Content;
    public FireBaseModel() {}

    public FireBaseModel(String Title, String content) {
        this.Title = Title;
        this.Content = Content;
    }

    public String getTitle() {
        return Title;
    }
    public void setTitle(String Title) {
        this.Title = Title;
    }

    public String getcontent() {
        return Content;
    }
    public void setcontent(String content) {
        this.Content = Content;
    }

}
