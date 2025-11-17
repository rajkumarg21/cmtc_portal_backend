package com.cmtc.cms.service.impl;

import com.cmtc.cms.exception.GenericException;
import com.cmtc.cms.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    public static final String FILES = "/files/";
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String saveFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath); // Create directory if it doesn't exist

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return FILES + fileName; // Return a URL path
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + file.getOriginalFilename() + ". Please try again!", ex);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty() || !filePath.startsWith(FILES)) {
            return; // Not an uploaded file managed by this service
        }
        try {
            String fileName = filePath.substring(FILES.length());
            Path fileToDelete = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName);
            if (Files.exists(fileToDelete)) {
                Files.delete(fileToDelete);
            }
        } catch (IOException ex) {
            System.err.println("Could not delete file " + filePath + ": " + ex.getMessage());
            // Log the error but don't throw, as it might be a non-critical cleanup
        }
    }
    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            if (!Files.exists(filePath)) {
                throw new GenericException("File not found: " + filename,HttpStatus.NOT_FOUND);
            }
            return new UrlResource(filePath.toUri());
        } catch (GenericException e) {
            throw e;
        } catch (Exception e) {
            throw new GenericException("Could not load file: " + filename, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String saveFile(MultipartFile file, String customFileName) {
        try {

            // Ensure the file name has ".pdf" extension
//    		if(!customFileName.toLowerCase().endsWith(".pdf")) {
            customFileName = customFileName + ".pdf";
//    		}

            // Create the file path
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath); // Create directory if it doesn't exist

            // Save the file with the final name
            Path filePath = uploadPath.resolve(customFileName);
            System.out.println("Custom named file name: "+customFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("file path: "+filePath);
            System.out.println("Custom named file saved at: "+filePath);

            // Return the URL of the saved file
            return FILES + customFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file with custom name: "+customFileName, ex);
        }
    }

    @Override
    public String saveFile(MultipartFile file, String customFileName,String subDir, String url) {
        try {
            customFileName = customFileName + ".pdf";

            Path uploadPath = Paths.get(subDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath); // Create directory if it doesn't exist

            Path filePath = uploadPath.resolve(customFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return FILES + url + customFileName;
        } catch (IOException ex) {
            String message = "Failed to store file '" + customFileName + "' on disk. Reason: " + ex.getMessage();
            throw new GenericException(message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
