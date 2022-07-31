package com.example.simplecrud.Repository;

import com.example.simplecrud.Domain.Dto.commentTransferDto;
import com.example.simplecrud.Domain.Entity.board;
import com.example.simplecrud.Domain.Entity.comment;
import com.example.simplecrud.Domain.Entity.file;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface commentRepository extends JpaRepository<comment,Long> {
    @Query("select c from comment c left join fetch c.parent b where c.board.id =:id "+
            "ORDER BY c.parent.id ASC NULLS FIRST , c.createdDate ASC")
    List<comment> findCommentByBoard_id(@Param("id")Long id);

    @Modifying
    @Query(nativeQuery = true,value = "delete from comment where board_id =:id")
    void deleteAllByBoard(@Param("id") Long id);
    /* 계층형 댓글 삭제에 관련해서*/

    @Query(value = "select c from comment c join fetch c.board where c.id=:id")
    Optional<comment> findByIdWithBoard(@Param("id") Long id);

    @Query(value = "select c from comment c " +
            "left join fetch c.parent " +
            "join fetch c.board "+
            "left join fetch c.children "+
            "where c.id=:id")
    Optional<comment> findByIdWithBoardToDelete(@Param("id") Long id);




}
