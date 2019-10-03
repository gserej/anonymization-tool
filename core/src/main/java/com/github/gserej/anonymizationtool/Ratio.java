package com.github.gserej.anonymizationtool;

import lombok.Getter;

class Ratio {
    @Getter
    private static float ratio;


    public Ratio(float ratio) {
        Ratio.ratio = ratio;
    }
}
