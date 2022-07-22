package com.example.simplecrud.Domain.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class board extends TimeEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String writer; //user의 ide와 매핑될것.

    @Column
    private String title;

    @Column
    private int visited;  //조회수.

    @Column
    private int commentNum;  //댓글수.

    // @Lob는 데이터베이스의 BLob 혹은 CLob로 매칭된다.
    //필드가 이제 String이면 CLOB으로 매핑 , 다른타입이면 BLOB으로 매핑.
    @Column @Lob
    private String content;

    @Column
    private boolean haveFile;


    @OneToMany(mappedBy = "board", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<comment> comments = new ArrayList<>();

    //여기에 들어가기만해도, 자동으로 영속성컨텍스트가 끝날때 persist가 되는걸까?
    //REMOVE만 적용시켰다 그래서, BOARD가 삭제될때, 내부의 file들도 삭제되도록.
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<file> list = new ArrayList<>();


    //직 첨부파일은 없다고 가정합시다. 있다면 데이터베이스의 BLOB형태로 연결 가능할까?


    public board(String writer, String title,int visited,int liked, String content, boolean haveFile ) {
        this.writer = writer;
        this.title = title;
        this.visited =visited;
        this.content = content;
        this.haveFile=haveFile;
    }
}
