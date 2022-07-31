package com.example.simplecrud.Domain.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    @OnDelete(action = OnDeleteAction.CASCADE) //너라는 FK에 걸겠다는 뜻이됩니다. 너가 삭제되면 나도 삭제되는 느낌.
    private comment parent;  //셀프조인을 ManyToOne 진행.
    /*
    *   동작방식, 현재댓글에 하위댓글이 있으면, 즉시 제거하면 안됨. Optional 반환.
    *            현재댓글에 하위댓글이 없으면, 실제로 제거해도 되는걸 찾기위해 상위로 거슬러올라가면서 검사.
    *            상위댓글이 실제로 제거해도 된다면, 다시 상위로 올라가면서 "삭제 가능 지점"을 찾아낸다.
    * *
    *            부모가 있고, 이미 삭제처리 됬고(isDeleted-true), 자식의 개수가 1이라면 제거해도 된다.
    *            자식의갯수가 1이라는건, 삭제요청을 받은 현재댓글외에, 다른 하위댓글들은 없는상황을 의미한다.
    *            내가 삭제처리됬고, 그때 내 상위도 살펴보는거임.
    *            1 <- 2(삭제) <- 3(삭제) <- 4
    *                        <- 5
    *            4를 삭제한다고 칩시다. 자식이 없음을 확인. 그리고 실제로 제거가 가능한 댓글을 찾는다.
    *            부모를 검사.  3입장에서 자식은 4밖에없습니다. 3은 삭제가능.
    *            그 다음에 2를 검사. 2는 근데 5를 가지고있다. 결국 삭제점인 3을 반환.
    *            4를 삭제한다고 쳤을때 연쇄적인 반응도 생각해야 하니 이렇게 짜는겁니다.
    * */

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
//        parent.getChildren().add(this);  //일단 여긴 주석처리 해봅시다
    }

    public void delete(){
        this.setContent("");
        this.setDeleted(true);
    }

    public Optional<comment> findDeletableComment() {
        return hasChildren() ? Optional.empty() : Optional.of(findDeletableCommentByParent());
    }

    private comment findDeletableCommentByParent() { // 1
        if (isDeletedParent()) {
            comment deletableParent = getParent().findDeletableCommentByParent();
            if(getParent().getChildren().size() == 1) return deletableParent; //계속받아옴.
        }
        return this;
    }

    private boolean hasChildren() {
        return getChildren().size() != 0;
    }

    private boolean isDeletedParent() { // 2
        return getParent() != null && getParent().isDeleted();
    }

}
