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
public class comment extends TimeEntity{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private board board;

    @Column
    private String writer;

    @Column
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "parent_id")
    private comment parent;  //셀프조인을 ManyToOne 진행.

    @OneToMany(mappedBy = "parent", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<comment> children = new ArrayList<>();

    public comment(String content, board board, String writer, boolean isDeleted) {
        this.content = content;
        this.board = board;
        this.writer = writer;
        this.isDeleted = isDeleted;
    }

    public void setRelationship(comment parent){
        this.setParent(parent);
        parent.getChildren().add(this);
    }

}
