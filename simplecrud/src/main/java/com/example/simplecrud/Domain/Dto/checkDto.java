package com.example.simplecrud.Domain.Dto;

import lombok.Data;
import org.springframework.core.io.Resource;

import java.util.List;

@Data
public class checkDto {
    private Long id;
    private String title;
    private String writer;
    private String content;
    private int visited;
    private List<fileAnotherDto> files;
    private List<commentResponseDto> comments;

}
