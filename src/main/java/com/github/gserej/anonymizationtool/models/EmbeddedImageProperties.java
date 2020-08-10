package com.github.gserej.anonymizationtool.models;


import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class EmbeddedImageProperties {

    private float positionX;
    private float positionY;
    private float sizeX;
    private float sizeY;
    private int pageNumber;
    private float pageHeight;
}

