package com.example.simplecrud.Domain.Dto;

import lombok.Data;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class postDto {

    @NotBlank(message = "제목에 공백은 허용되지 않습니다.")
    private String title;

    @NotBlank(message = "내용에 공백은 허용되지 않습니다.")
    private String content;

    @Nullable
    private List<MultipartFile> files;


}
