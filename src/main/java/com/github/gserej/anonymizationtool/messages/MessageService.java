package com.github.gserej.anonymizationtool.messages;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;


@Service
public class MessageService {

    @Getter
    private final String WRONG_EXTENSION = "You have uploaded the file with a wrong file extension.";
    @Getter
    private final String SUCCESSFUL_UPLOAD = "You successfully uploaded ";
    @Getter
    private final String DOWNLOAD_LINK_READY = "Your file was converted, click the link below to download it.";


    @Setter
    @Getter
    private String currentMessage;


}
