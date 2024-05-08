package org.project.scapdata.domain.file;


import jakarta.servlet.http.HttpServletRequest;
import org.project.scapdata.domain.file.dto.FileResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    FileResponse uploadSingleFile(MultipartFile file, HttpServletRequest request);
    List<String> uploadMultipleFiles(MultipartFile[] files);
    List<String> getAllFileNames();
    ResponseEntity<Resource> serveMultipleFiles(List<String> filenames, HttpServletRequest request);
    ResponseEntity<Resource> serveFile(String filename, HttpServletRequest request);
    void deleteFile(String filename);
}
