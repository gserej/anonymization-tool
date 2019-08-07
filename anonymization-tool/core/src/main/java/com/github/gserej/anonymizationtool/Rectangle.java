package com.github.gserej.anonymizationtool;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class Rectangle {

    private String id;
    private String marked;
    private String x;
    private String y;
    private String w;
    private String h;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMarked() {
        return marked;
    }

    public void setMarked(String marked) {
        this.marked = marked;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getW() {
        return w;
    }

    public void setW(String w) {
        this.w = w;
    }

    public String getH() {
        return h;
    }

    public void setH(String h) {
        this.h = h;
    }

    @Override
    public String toString() {
        return "Rectangle{" +
                "id='" + id + '\'' +
                ", marked='" + marked + '\'' +
                ", x='" + x + '\'' +
                ", y='" + y + '\'' +
                ", w='" + w + '\'' +
                ", h='" + h + '\'' +
                '}';
    }
}
