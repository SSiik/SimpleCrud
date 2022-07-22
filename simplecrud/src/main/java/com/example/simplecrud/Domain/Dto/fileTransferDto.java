package com.example.simplecrud.Domain.Dto;

import lombok.Data;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

@Data
public class fileTransferDto {
    private String originalFileName;
    private byte[] contents;

    public fileTransferDto(String originalFileName, byte[] contents) {
        this.originalFileName = originalFileName;
        this.contents = contents;
    }
}
