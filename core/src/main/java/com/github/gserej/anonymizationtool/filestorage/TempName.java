package com.github.gserej.anonymizationtool.filestorage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
public class TempName {

    @Getter
    @Setter
    private String tempFileName;


}
