package org.project.scapdata.domain.file;


import jakarta.servlet.http.HttpServletRequest;
import org.project.scapdata.domain.file.dto.FileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
public class FileServiceImpl implements FileService {
    @Value("${file.storage-dir}")
    String fileStorageDir;
    private static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_GIF_VALUE,
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    //http://localhost:8888/api/v1/files/download/3f54df29-31f0-4c74-9552-62dfca9e4f1e.png
    private String generateImageUrl(HttpServletRequest request, String filename) {
        return String.format("%s://%s:%d/images/%s",
                request.getScheme(),
                request.getServerName(),
                request.getServerPort(),
                filename);
    }

    private String generateDownloadImageUrl(HttpServletRequest request, String filename) {
        return String.format("%s://%s:%d/api/v1/files/download/%s",
                request.getScheme(),
                request.getServerName(),
                request.getServerPort(),
                filename);
    }
    private String uploadFile(MultipartFile file) {
       String contentType = file.getContentType();
       if(!SUPPORTED_IMAGE_TYPES.contains(contentType)){
           throw new ResponseStatusException(
                   HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                   contentType + " not  allowed!! ");
       }
        try {
//       Check if the directory doesn't exist , we will create the directory
            Path fileStoragePath = Path.of(fileStorageDir);
            if (!Files.exists(fileStoragePath)) {
                Files.createDirectories(fileStoragePath);
            }
            String fileName = UUID.randomUUID() + "." +
                    Objects.requireNonNull(file.getOriginalFilename())
                            .split("\\.")[1];
            // handle if there are more than one dot !

            Files.copy(file.getInputStream(),
                    fileStoragePath.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

    }

    @Override
    public FileResponse uploadSingleFile(MultipartFile file, HttpServletRequest request) {
        String filename = uploadFile(file);
        String fullImageUrl = generateImageUrl(request, filename);
        return FileResponse.builder()
                .downloadUrl(generateDownloadImageUrl(request,filename))
                .fileType(file.getContentType())
                .size((float) file.getSize() / 1024) // in KB
                .filename(filename)
                .fullUrl(fullImageUrl).build();
    }
    @Override
    public List<String> uploadMultipleFiles(MultipartFile[] files) {
        var fileNames = new ArrayList<String>();
        for (var file : files) {
            fileNames.add(uploadFile(file));
        }
        return fileNames;
    }

    @Override
    public ResponseEntity<Resource> serveFile(String filename, HttpServletRequest request) {
        try {
            Path filePath = Path.of(fileStorageDir).resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()){
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            }else {
                throw new RuntimeException("Resources not found ! ");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error occurred while serving file", ex);
        }
    }

    @Override
    public ResponseEntity<Resource> serveMultipleFiles(List<String> filenames, HttpServletRequest request) {
        try {
            // Create a temporary zip file
            Path zipPath = Files.createTempFile("files", ".zip");
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                for (String filename : filenames) {
                    // Get the path to the file
                    Path filePath = Path.of(fileStorageDir).resolve(filename);
                    // Create a new zip entry
                    zipOutputStream.putNextEntry(new ZipEntry(filename));
                    // Copy the file into the zip entry
                    Files.copy(filePath, zipOutputStream);
                    // Close the zip entry
                    zipOutputStream.closeEntry();
                }
            }

            // Create a resource for the zip file
            Resource resource = new UrlResource(zipPath.toUri());
            if (resource.exists()) {
                String contentType = Files.probeContentType(zipPath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("Resources not found ! ");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error occurred while serving file", ex);
        }
    }
    @Override
    public void deleteFile(String filename) {

    }
}
