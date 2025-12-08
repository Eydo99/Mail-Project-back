package com.example.backend.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/attachments")
@CrossOrigin(origins = "http://localhost:4200")
public class AttachmentController {

    private final String UPLOAD_DIR = "data/uploads/";

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            // Decode the filename to handle spaces and special characters
            String decodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8);

            System.out.println("=== ATTACHMENT REQUEST ===");
            System.out.println("Requested filename: " + decodedFilename);

            // Build the file path
            Path filePath = Paths.get(UPLOAD_DIR).resolve(decodedFilename).normalize();

            System.out.println("Full file path: " + filePath.toAbsolutePath());

            // Check if file exists
            if (!Files.exists(filePath)) {
                System.err.println("File not found: " + filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            // Load file as Resource
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                System.err.println("File not readable: " + filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            // Determine content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            System.out.println("Content type: " + contentType);
            System.out.println("File size: " + resource.contentLength() + " bytes");
            System.out.println("=== SERVING FILE ===");

            // Return file with proper headers
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (IOException e) {
            System.err.println("Error serving file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}