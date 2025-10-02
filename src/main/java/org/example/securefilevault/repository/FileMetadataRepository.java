package org.example.securefilevault.repository;

import org.example.securefilevault.model.FileMetadata;
import org.example.securefilevault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    List<FileMetadata> findByOwner(User owner);

    Optional<FileMetadata> findByIdAndOwner(Long id, User owner);

    boolean existsByStoredFilename(String storedFilename);
}
