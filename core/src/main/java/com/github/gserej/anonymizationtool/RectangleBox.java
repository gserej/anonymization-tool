package com.github.gserej.anonymizationtool;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.concurrent.atomic.AtomicInteger;

@JsonAutoDetect
public class RectangleBox {

    private static final AtomicInteger count = new AtomicInteger(-1);
    private int id;
    private boolean marked;
    private int typeOfData;
    private float x;
    private float y;
    private float w;
    private float h;
    private String word;

    RectangleBox(boolean marked, float x, float y, float w, float h, int typeOfData, String word) {
        this.id = count.incrementAndGet();
        this.marked = marked;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.typeOfData = typeOfData;
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getTypeOfData() {
        return typeOfData;
    }

    public void setTypeOfData(int typeOfData) {
        this.typeOfData = typeOfData;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getW() {
        return w;
    }

    public void setW(float w) {
        this.w = w;
    }

    public float getH() {
        return h;
    }

    public void setH(float h) {
        this.h = h;
    }


    @Override
    public String toString() {
        return "RectangleBox{" +
                "id=" + id +
                ", marked=" + marked +
                ", typeOfData=" + typeOfData +
                ", x=" + x +
                ", y=" + y +
                ", w=" + w +
                ", h=" + h +
                ", word='" + word + '\'' +
                '}';
    }
}
