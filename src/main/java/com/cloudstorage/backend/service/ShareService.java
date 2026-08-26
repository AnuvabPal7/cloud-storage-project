package com.cloudstorage.backend.service;

import com.cloudstorage.backend.dto.CreateShareRequest;
import com.cloudstorage.backend.dto.ShareResponse;
import com.cloudstorage.backend.dto.SharedFileResponse;
import com.cloudstorage.backend.model.FileItem;
import com.cloudstorage.backend.model.FileShare;
import com.cloudstorage.backend.model.User;
import com.cloudstorage.backend.repository.FileRepository;
import com.cloudstorage.backend.repository.FileShareRepository;
import com.cloudstorage.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final FileRepository fileRepository;
    private final FileShareRepository fileShareRepository;
    private final UserRepository userRepository;

    /**
     * Creates a share, or updates the permission if this file is already
     * shared with that person - so re-sharing with a new role just works,
     * instead of the caller needing to know whether a share already exists.
     */
    @Transactional
    public ShareResponse createOrUpdateShare(CreateShareRequest request, User owner) {
        FileItem file = fileRepository.findByIdAndOwner(request.fileId(), owner)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        User targetUser = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("No user found with that email"));

        if (targetUser.getId().equals(owner.getId())) {
            throw new IllegalArgumentException("You can't share a file with yourself");
        }

        FileShare share = fileShareRepository.findByFileAndSharedWithUser(file, targetUser)
                .orElseGet(() -> FileShare.builder().file(file).sharedWithUser(targetUser).build());

        share.setPermission(request.permission());
        fileShareRepository.save(share);

        return toResponse(share);
    }

    @Transactional(readOnly = true)
    public List<ShareResponse> listSharesForFile(UUID fileId, User owner) {
        FileItem file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        return fileShareRepository.findAllByFile(file).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void revokeShare(UUID shareId, User owner) {
        // findByIdAndFile_Owner ensures only the file's owner can revoke a
        // share - not the person it was shared with, and not a stranger.
        FileShare share = fileShareRepository.findByIdAndFile_Owner(shareId, owner)
                .orElseThrow(() -> new IllegalArgumentException("Share not found"));
        fileShareRepository.delete(share);
    }

    @Transactional(readOnly = true)
    public List<SharedFileResponse> listSharedWithMe(User user) {
        return fileShareRepository.findAllBySharedWithUserOrderByCreatedAtDesc(user).stream()
                .map(share -> new SharedFileResponse(
                        share.getFile().getId(),
                        share.getFile().getName(),
                        share.getFile().getSize(),
                        share.getFile().getMimeType(),
                        share.getPermission(),
                        share.getFile().getOwner().getEmail(),
                        share.getCreatedAt()
                ))
                .toList();
    }

    private ShareResponse toResponse(FileShare share) {
        return new ShareResponse(
                share.getId(),
                share.getFile().getId(),
                share.getFile().getName(),
                share.getSharedWithUser().getEmail(),
                share.getPermission(),
                share.getCreatedAt()
        );
    }
}
