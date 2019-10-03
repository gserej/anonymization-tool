package com.github.gserej.anonymizationtool;
import com.github.gserej.anonymizationtool.storage.StorageFileNotFoundException;
import com.github.gserej.anonymizationtool.storage.StorageService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Setter
    @Getter
    private static Object modelObject;

    @Setter
    @Getter
    private static String tempName;

    @Getter
    @Setter
    private static List<String> tempImagesList;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) {

        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString()).filter(f -> f.endsWith(".pdf"))
                .collect(Collectors.toList()));

        model.addAttribute("rectListModel", getModelObject());

        return "pageviewer";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        storageService.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        File fileToProcess = storageService.loadAsFile(file.getOriginalFilename());
        String fileExtension = FilenameUtils.getExtension(fileToProcess.getName());

        if (fileExtension.equalsIgnoreCase("pdf")) {
            log.info("pdf file found");

            processPdfFile(fileToProcess, file);
        } else if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("png")) {
            log.info("image file found");
            processImageFile(fileToProcess);
        } else {
            redirectAttributes.addFlashAttribute("message", "You uploaded the file with a wrong file extension.");
        }
        return "redirect:/";
    }

    @GetMapping("/additionalRects")
    @ResponseBody
    public Object sendAdditionalRects() {
        return getModelObject();
    }

    private void processPdfFile(File fileToProcess, MultipartFile file) {
        setTempName(file.getOriginalFilename());
        try {
            WordsPrinterDrawer.printLocations(fileToProcess);
            setModelObject(RectangleParsers.parseRectangleBoxList(RectangleBoxLists.getRectangleBoxListOriginal()));
            log.info("Rectangles sent to the page: " + RectangleBoxLists.getRectangleBoxListParsed().toString());
            Runnable r = () -> {
                try {
                    ImageLocationsExtractor.extractImages(fileToProcess);
                    setModelObject(RectangleParsers.parseRectangleBoxList(RectangleBoxLists.getRectangleBoxListOriginal()));
                    log.info("Additional rectangles sent to the page: " + RectangleBoxLists.getRectangleBoxListParsed().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            new Thread(r).start();

        } catch (IOException e) {
            log.error("Error processing PDF file: " + e);
        }
    }

    private void processImageFile(File fileToProcess) {
        try {
            File imagePdfFile = ImageToPdfConversion.createPdfFromSingleImage(fileToProcess, fileToProcess.getName());
            MultipartFile multipartFile = new MockMultipartFile(imagePdfFile.getName(),
                    imagePdfFile.getName(), "text/plain", IOUtils.toByteArray(new FileInputStream(imagePdfFile)));
            storageService.store(multipartFile);
            setTempName(multipartFile.getOriginalFilename());

            Runnable r = () -> {
                boolean ocrSuccessful = TesseractOCR.doOcrOnSingleFile(fileToProcess, Ratio.getRatio());
                if (ocrSuccessful) {
                    log.info("OCR processing: done");
                    setModelObject(RectangleParsers.parseRectangleBoxList(RectangleBoxLists.getRectangleBoxListOriginal()));
                    log.info("Rectangles sent to the page: " + RectangleBoxLists.getRectangleBoxListParsed().toString());
                }
            };
            new Thread(r).start();

        } catch (IOException e) {
            log.error("Error processing image file: " + e);
        }
    }

    @PostMapping(value = "/api")
    public String postJson(@RequestBody List<RectangleBox> rectangleBoxesMarked, RedirectAttributes redirectAttributes) {
        RectangleBoxLists.setRectangleBoxListMarked(rectangleBoxesMarked);
        log.info("Marked rectangles received from the page: " + rectangleBoxesMarked.toString());

        File fileToProcess = storageService.loadAsFile(getTempName());
        log.info(fileToProcess.getName());

        try {
            WordsPrinterDrawer.drawLocations(fileToProcess);

            log.info(tempImagesList.toString());
            List<File> imageFilesList = new ArrayList<>();
            for (String s : tempImagesList) {
                imageFilesList.add(storageService.loadAsFile(s));
            }

            String pathToDonePdf = ImageToPdfConversion.createPdfFromMultipleImages(imageFilesList, getTempName(), fileToProcess);
            storageService.storeAsFile(new File(pathToDonePdf));
            setTempName(null);
        } catch (IOException e) {
            log.error("Error processing file" + e);
        }

        redirectAttributes.addFlashAttribute("fileReadyMessage",
                "Your file was converted, click the link below to download it.");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound() {
        return ResponseEntity.notFound().build();
    }

}