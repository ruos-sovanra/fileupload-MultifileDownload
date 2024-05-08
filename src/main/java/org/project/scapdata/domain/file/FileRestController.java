package org.project.scapdata.domain.file;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.project.scapdata.domain.file.dto.FileResponse;
import org.project.scapdata.utils.BaseResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileRestController {
    private final FileService fileService;


    @GetMapping
    public BaseResponse<List<String>> getAllFileNames() {
        return BaseResponse
                .<List<String>>createSuccess()
                .setPayload(fileService.getAllFileNames());
    }

    @PostMapping(value = "", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<FileResponse> uploadSingleFile(
            @RequestPart("file") MultipartFile file, HttpServletRequest request
    ) {
        return BaseResponse
                .<FileResponse>createSuccess()
                .setPayload(fileService.uploadSingleFile(file, request));
    }

    @PostMapping(value = "/multiple", consumes = "multipart/form-data")
//    return payload as List<FileResponse>
    public BaseResponse<List<FileResponse>> uploadMultipleFiles(@RequestPart("files") MultipartFile[] files) {
//        return fileService.uploadMultipleFiles(files);
        return null;
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileName, HttpServletRequest request){
        return fileService.serveFile(fileName,request);
    }

    @GetMapping("/download/multiple")
    public ResponseEntity<Resource> downloadMultipleFiles(@RequestParam List<String> filenames, HttpServletRequest request){
        return fileService.serveMultipleFiles(filenames, request);
    }

    @DeleteMapping("{fileName}")
    public String deleteFile(@PathVariable String fileName) {
        fileService.deleteFile(fileName);
        return "file is deleted successfully!";
    }


}
