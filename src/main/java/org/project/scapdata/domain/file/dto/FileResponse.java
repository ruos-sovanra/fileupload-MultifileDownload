package org.project.scapdata.domain.file.dto;

import lombok.Builder;
@Builder
public record FileResponse(String downloadUrl, String fileType, float size, String filename, String fullUrl, String content) {}
