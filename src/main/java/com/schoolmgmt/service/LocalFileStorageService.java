package com.schoolmgmt.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

    /**
     * Image extensions re-compressed + resized on upload. EXIF is stripped
     * implicitly because Thumbnailator re-encodes to a fresh JPEG/PNG.
     */
    private static final Set<String> IMAGE_EXT =
            Set.of(".jpg", ".jpeg", ".png", ".webp");

    /**
     * Max width/height for the stored original. Photos taken on phone (4000px+)
     * are downscaled here. Client already does `maxWidth: 2048`, this is the
     * server-side ceiling.
     */
    private static final int ORIGINAL_MAX_DIM = 1600;

    /**
     * Thumbnail size used for list-tile avatars / QR previews.
     */
    private static final int THUMB_DIM = 400;

    /**
     * JPEG quality for the stored original (0.0 – 1.0).
     */
    private static final float JPEG_QUALITY = 0.85f;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
            log.info("File upload directory initialized: {}", rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Override
    public String store(MultipartFile file, String tenantId) {
        try {
            Path tenantDir = rootLocation.resolve(tenantId);
            Files.createDirectories(tenantDir);

            String extension = getExtension(file.getOriginalFilename());
            String uuid = UUID.randomUUID().toString();

            if (IMAGE_EXT.contains(extension.toLowerCase(Locale.ROOT))) {
                // Images: re-encode to JPEG at capped resolution + a thumbnail.
                String storedName = uuid + ".jpg";
                Path target = tenantDir.resolve(storedName);

                try (InputStream in = file.getInputStream()) {
                    Thumbnails.of(in)
                            .size(ORIGINAL_MAX_DIM, ORIGINAL_MAX_DIM)
                            .outputFormat("jpg")
                            .outputQuality(JPEG_QUALITY)
                            .toFile(target.toFile());
                }

                // Thumbnail — best-effort, never block the upload if it fails.
                try {
                    Thumbnails.of(target.toFile())
                            .size(THUMB_DIM, THUMB_DIM)
                            .outputFormat("jpg")
                            .outputQuality(0.80f)
                            .toFile(tenantDir.resolve("thumb_" + uuid + ".jpg").toFile());
                } catch (Exception ex) {
                    log.warn("Thumbnail generation failed for {}: {}",
                            storedName, ex.getMessage());
                }

                log.info("Image stored (compressed): {} for tenant: {}",
                        storedName, tenantId);
                return tenantId + "/" + storedName;
            }

            // Non-image files go through untouched.
            String storedName = uuid + extension;
            Path targetPath = tenantDir.resolve(storedName);
            Files.copy(file.getInputStream(), targetPath,
                    StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored: {} for tenant: {}", storedName, tenantId);

            return tenantId + "/" + storedName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public Resource load(String storagePath) {
        return loadInternal(storagePath);
    }

    /**
     * Loads a thumbnail variant of an image if it exists, otherwise falls
     * back to the original. Used by controllers for fast list-tile previews.
     */
    public Resource loadVariant(String storagePath, String variant) {
        if ("thumb".equalsIgnoreCase(variant)) {
            int slash = storagePath.lastIndexOf('/');
            String dir = slash < 0 ? "" : storagePath.substring(0, slash + 1);
            String file = slash < 0 ? storagePath : storagePath.substring(slash + 1);
            String thumbPath = dir + "thumb_" + file;
            try {
                Resource thumb = loadInternal(thumbPath);
                if (thumb.exists()) return thumb;
            } catch (Exception ignored) {
                // fall back to original
            }
        }
        return loadInternal(storagePath);
    }

    private Resource loadInternal(String storagePath) {
        try {
            Path filePath = rootLocation.resolve(storagePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("File not found: " + storagePath);
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + storagePath, e);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Path filePath = rootLocation.resolve(storagePath).normalize();
            Files.deleteIfExists(filePath);

            // Also clean up thumbnail if it exists.
            int slash = storagePath.lastIndexOf('/');
            if (slash >= 0) {
                String thumbPath = storagePath.substring(0, slash + 1) +
                        "thumb_" + storagePath.substring(slash + 1);
                Files.deleteIfExists(rootLocation.resolve(thumbPath).normalize());
            }
            log.info("File deleted: {}", storagePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
