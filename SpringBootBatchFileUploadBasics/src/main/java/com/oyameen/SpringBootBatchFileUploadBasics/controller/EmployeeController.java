package com.oyameen.SpringBootBatchFileUploadBasics.controller;

import com.oyameen.SpringBootBatchFileUploadBasics.service.FileService;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @Autowired
    private FileService fileService;

    @PostMapping("/importEmployeesToDB")
    public String importEmployeesToDB(@RequestParam("employeeCSV") MultipartFile multipartFile) throws IOException {
        File file = fileService.uploadCSVFile(multipartFile);
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .addString("filePath", file.getAbsolutePath()).toJobParameters();
        try {
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
            fileService.deleteCSVFile(jobExecution, file.getName());
        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException | JobRestartException e) {
            System.out.println("Exception occur while importing the employees = " + e.getMessage());
            throw new RuntimeException(e);
        }
        return "Importing the employees to db. done successfully.";
    }
}
