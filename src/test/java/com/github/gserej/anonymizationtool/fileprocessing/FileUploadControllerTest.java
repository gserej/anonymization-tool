package com.github.gserej.anonymizationtool.fileprocessing;

import com.github.gserej.anonymizationtool.filestorage.StorageFileNotFoundException;
import com.github.gserej.anonymizationtool.filestorage.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
class FileUploadControllerTest {


    @MockBean
    FileProcessingService fileProcessingService;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private StorageService storageService;


    @Test
    void should404WhenMissingFile() throws Exception {
        given(this.storageService.loadAsResource("test.pdf"))
                .willThrow(StorageFileNotFoundException.class);

        this.mvc.perform(get("/files/test.pdf")).andExpect(status().isNotFound());
    }

    @Test
    void shouldSaveUploadedFile() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.pdf",
                "text/plain", "Spring Framework".getBytes());

        this.mvc.perform(multipart("/").file(multipartFile))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/"));

        then(this.storageService).should().store(multipartFile);
    }
}
