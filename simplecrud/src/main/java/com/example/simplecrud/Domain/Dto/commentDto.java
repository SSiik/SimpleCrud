package com.example.simplecrud.Domain.Dto;

import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class commentDto {

    @Nullable
    private Long comment_id;

    private Long board_id;

    @NotBlank(message = "댓글작성시 빈내용 혹은 공백은 허용되지 않습니다.")
    private String content;


}
