package com.example.simplecrud.Domain.Dto;

import lombok.Data;

import java.util.List;

@Data
public class commentTransferDto {
    private Long id;
    private String writer;
    private String content;
    private List<commentTransferDto> childs;

}
