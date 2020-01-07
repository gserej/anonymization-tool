package com.github.gserej.anonymizationtool.rectangles;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@ToString
public class RectangleBox {

    private static final AtomicInteger count = new AtomicInteger(-1);
    private int id;
    private int page;
    private boolean marked;
    private boolean parsed;
    private boolean drew;
    private int typeOfData;
    private float x;
    private float y;
    private float w;
    private float h;
    private String word;


    public RectangleBox(boolean marked, boolean drew, boolean parsed, float x, float y, float w, float h, int typeOfData, String word, int page) {
        this.id = count.incrementAndGet();
        this.marked = marked;
        this.parsed = parsed;
        this.drew = drew;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.typeOfData = typeOfData;
        this.word = word;
        this.page = page;
    }
}