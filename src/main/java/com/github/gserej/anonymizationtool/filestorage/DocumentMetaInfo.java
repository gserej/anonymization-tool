package com.github.gserej.anonymizationtool.filestorage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentMetaInfo {

    @Getter
    @Setter
    public List<String> imageList = new ArrayList<>();
    @Getter
    @Setter
    private String documentName;

    public void addImageName(String imageName) {
        this.imageList.add(imageName);
    }

}
