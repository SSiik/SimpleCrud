package com.example.simplecrud.Domain.Dto;

import lombok.Data;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class boardAndFileDto {

    private String writer;
    private String title;
    private String content;
    private List<fileSaveDto> files;
}
