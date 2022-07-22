package com.example.simplecrud.Domain.Dto;

import lombok.Data;

import java.util.List;

@Data
public class updateDto {
    private String title;
    private String content;
    private List<fileAnotherDto> lists;
}
