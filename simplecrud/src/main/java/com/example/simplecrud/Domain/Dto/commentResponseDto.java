package com.example.simplecrud.Domain.Dto;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class commentResponseDto {
    private Long id;
    private Long board_id;
    private String content;
    private String writer;
    private List<commentResponseDto> children = new ArrayList<>();

    @Override
    public String toString() {
        return "commentResponseDto{" +
                "id=" + id +
                ", board_id=" + board_id +
                ", content='" + content + '\'' +
                ", children=" + children +
                '}';
    }
}
