package com.example.simplecrud.Domain.Dto;

import lombok.Data;

@Data
public class listDto {  //일단 이거 정도의 정보를 넘겨줍니다.
    private Long id;
    private String title;
    private int visited;
    private String writer;
    private int commentNum;
    private boolean haveFile;

    public listDto(Long id, String title,int visited, String writer,int commentNum, boolean haveFile) {
        this.id = id;
        this.title = title;
        this.visited = visited;
        this.writer = writer;
        this.commentNum = commentNum;
        this.haveFile = haveFile;
    }
}
