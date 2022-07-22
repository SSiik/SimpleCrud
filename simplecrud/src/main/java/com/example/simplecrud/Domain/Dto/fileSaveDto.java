package com.example.simplecrud.Domain.Dto;

import lombok.Data;

@Data
public class fileSaveDto {
    private String uploadFileName;
    private String storeFileName;
    private String filePath;

//    public fileSaveDto(String uploadFileName, String storeFileName, String filePath) {
//        this.uploadFileName = uploadFileName;
//        this.storeFileName = storeFileName;
//        this.filePath = filePath;
//    }
}
