package com.github.gserej.anonymizationtool.fileprocessing;

import com.github.gserej.anonymizationtool.filestorage.StorageFileNotFoundException;
import com.github.gserej.anonymizationtool.filestorage.StorageService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    void shouldListFirstPdfFile() throws Exception {
        given(this.storageService.loadAll())
                .willReturn(Stream.of(Paths.get("first.pdf"),
                        Paths.get("second.pdf"),
                        Paths.get("third.txt")));

        this.mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(model().attribute("files",
                        Matchers.contains("http://localhost/files/first.pdf")))
                .andExpect(view().name("pageviewer"));
    }

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
        when(fileProcessingService.processUploadedFile(multipartFile.getOriginalFilename())).thenReturn(false);

        this.mvc.perform(multipart("/").file(multipartFile))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/"));

        then(this.storageService).should().store(multipartFile);
    }

    @Test
    void shouldReturnWrongExtensionInfo() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt",
                "text/plain", "Spring Framework".getBytes());
        when(fileProcessingService.processUploadedFile(multipartFile.getOriginalFilename())).thenReturn(true);

        this.mvc.perform(multipart("/").file(multipartFile))
                .andExpect(status().isFound())
                .andExpect(MockMvcResultMatchers.flash().attribute("message",
                        "You have uploaded the file with a wrong file extension."));

    }

    @Test
    void shouldReturnRightExtensionInfo() throws Exception {
        final String TEST_PDF = "test.pdf";
        MockMultipartFile multipartFile = new MockMultipartFile("file", TEST_PDF,
                "text/plain", "Spring Framework".getBytes());
        when(fileProcessingService.processUploadedFile(multipartFile.getOriginalFilename())).thenReturn(false);

        this.mvc.perform(multipart("/").file(multipartFile))
                .andExpect(status().isFound())
                .andExpect(MockMvcResultMatchers.flash().attribute("message",
                        "You successfully uploaded " + TEST_PDF + "!"));

    }
}