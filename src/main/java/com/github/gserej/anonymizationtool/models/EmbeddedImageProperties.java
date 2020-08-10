package com.github.gserej.anonymizationtool.models;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmbeddedImageProperties {

    private float positionX;
    private float positionY;
    private float sizeX;
    private float sizeY;
    private int pageNumber;
    private float pageHeight;

    public EmbeddedImageProperties(float positionX, float positionY, float sizeX, float sizeY, int pageNumber, float pageHeight) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.pageNumber = pageNumber;
        this.pageHeight = pageHeight;
    }

    @Override
    public String toString() {
        return "EmbeddedImageProperties{" +
                "positionX=" + positionX +
                ", positionY=" + positionY +
                ", sizeX=" + sizeX +
                ", sizeY=" + sizeY +
                ", pageNumber=" + pageNumber +
                ", pageHeight=" + pageHeight +
                '}';
    }
}

