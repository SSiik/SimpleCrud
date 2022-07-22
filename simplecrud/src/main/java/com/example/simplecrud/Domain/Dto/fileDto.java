package com.example.simplecrud.Domain.Dto;

import lombok.Data;

@Data
public class fileDto {

    private String uploadFileName;
    private String storeFileName;


    public fileDto(String uploadFileName, String storeFileName) {
        this.uploadFileName = uploadFileName;
        this.storeFileName = storeFileName;
    }
}
