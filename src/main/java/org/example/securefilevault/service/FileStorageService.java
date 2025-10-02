package org.example.securefilevault.service;

import jakarta.transaction.Transactional;
import org.example.securefilevault.model.FileMetadata;
import org.example.securefilevault.model.User;
import org.example.securefilevault.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class FileStorageService {
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${C:\\Users\\peterbe\\Documents\\Secure Vault}")
    private String storagePath;

    public FileStorageService(FileMetadataRepository fileMetadataRepository){
        this.fileMetadataRepository = fileMetadataRepository;
    }

    // Store a file
    public FileMetadata storeFile(MultipartFile file, User owner) throws IOException{
        String originalFilename = file.getOriginalFilename();
        String storedFilename  = UUID.randomUUID() + "-" + originalFilename;

        Path userDir = Paths.get(storagePath, String.valueOf(owner.getId()));
        Files.createDirectories(userDir);

        Path targetPath = userDir.resolve(storedFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        FileMetadata metadata = new FileMetadata();
        metadata.setOriginalFilename(originalFilename);
        metadata.setStoredFilename(storedFilename);
        metadata.setPath(targetPath.toString());
        metadata.setSize(file.getSize());
        metadata.setFileType(file.getContentType());
        metadata.setUploadedAt(LocalDateTime.now());
        metadata.setOwner(owner);

        return fileMetadataRepository.save(metadata);
    }

    // Delete a file
    public void deleteFile(FileMetadata fileMetadata) throws IOException{
        Path filePath = Paths.get(fileMetadata.getPath());
        Files.deleteIfExists(filePath);
        fileMetadataRepository.delete(fileMetadata);
    }

    // Read a file
    public byte[] readFile(FileMetadata fileMetadata) throws IOException{
        return Files.readAllBytes(Paths.get(fileMetadata.getPath()));
    }

    public List<FileMetadata> getUserFiles(User user) {
        return fileMetadataRepository.findByOwner(user);
    }

    public Optional<FileMetadata> getFileByIdAndUser(Long fileId, User user) {
        return fileMetadataRepository.findByIdAndOwner(fileId, user);
    }

    //
}
