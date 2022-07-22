package com.example.simplecrud.Repository;

import com.example.simplecrud.Domain.Entity.board;
import com.example.simplecrud.Domain.Entity.file;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface fileRepository extends JpaRepository<file,Long> {
    List<file> findByBoard(board board);

    //@Modifying을 통해 bulk연산진행(다량 삭제). board에 해당하는 파일을 일단 다삭제.
    //게시글 수정때 발동.

    @Modifying
    @Query(nativeQuery = true,value = "delete from file where board_id =:id")
    void deleteAllByBoard(@Param("id") Long id);

    @Query("select f from file f " +
            "join fetch f.board b " +
            "where b.id =: board_id")    // <= 쿼리 수정해야함.
    Optional<List<file>> findFileByBoardId(@Param("board_id") Long board_id); //이렇게 fetch join대상의 별칭을 이용한다.

}
