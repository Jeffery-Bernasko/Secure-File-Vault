package org.example.securefilevault.controller;

import jakarta.annotation.Resource;
import org.example.securefilevault.model.FileMetadata;
import org.example.securefilevault.model.User;
import org.example.securefilevault.service.FileStorageService;
import org.example.securefilevault.service.UserService;
import org.springframework.boot.autoconfigure.ssl.SslProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileStorageService fileStorageService;
    private final UserService userService;

    public FileController(FileStorageService fileStorageService, UserService userService){
        this.fileStorageService = fileStorageService;
        this.userService = userService;
    }

    //Upload file
    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("file")MultipartFile file, Authentication authentication){
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("🎯 Authenticated user uploading: " + authentication.getName());

        try {
            FileMetadata savedFile = fileStorageService.storeFile(file,user);
            return ResponseEntity.ok("File uploaded successfully: " + savedFile.getOriginalFilename());
        } catch (IOException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("FIle upload failed");
        }
    }

    @GetMapping
    public ResponseEntity<List<FileMetadata>> listUserFiles(Authentication authentication){
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        List<FileMetadata> files = fileStorageService.getUserFiles(user);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id, Authentication authentication){
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow(()-> new RuntimeException("User not found"));

        FileMetadata fileMetadata = fileStorageService.getFileByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("File not found"));

        try {
            byte[] fileData = fileStorageService.readFile(fileMetadata);
            ByteArrayResource resource = new ByteArrayResource(fileData);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachement; filename=\"" + fileMetadata.getOriginalFilename()
                            + "\"")
                    .contentType(MediaType.parseMediaType(fileMetadata.getFileType()))
                    .contentLength(fileData.length)
                    .body((Resource) resource);
        } catch (IOException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id, Authentication authentication){
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow(()-> new RuntimeException("User not found"));

        FileMetadata fileMetadata = fileStorageService.getFileByIdAndUser(id,user).orElseThrow(()->new RuntimeException("File not found"));

        try{
            fileStorageService.deleteFile(fileMetadata);
            return ResponseEntity.ok("File deleted successfully");
        } catch (IOException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
