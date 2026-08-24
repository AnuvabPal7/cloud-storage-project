package com.cloudstorage.backend.repository;

import com.cloudstorage.backend.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// Minimal for now - real queries (list children, move, rename) land Day 4.
public interface FolderRepository extends JpaRepository<Folder, UUID> {
}
