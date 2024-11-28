package com.oyameen.SpringBootBatchFileUploadBasics.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Slf4j
public class FileService {

    private static final String uploadDir = new FileSystemResource("src/main/resources/server_files/").getFile().getAbsolutePath() + File.separator;

    public File uploadCSVFile(MultipartFile multipartFile) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();
        File file = new File(uploadDir + originalFileName);
        multipartFile.transferTo(file);
        return file;
    }

    public void deleteCSVFile(String fileName) throws IOException {
        Files.deleteIfExists(Paths.get(uploadDir + fileName));
    }

}
