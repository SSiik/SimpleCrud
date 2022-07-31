package com.example.simplecrud.Repository;

import com.example.simplecrud.Domain.Entity.board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface boardRepository extends JpaRepository<board,Long>,boardRepositoryCustom{

    //컬렉션 페치 조인. 일대다 패치조인이므로 distinct 키워드 삽입.
    @EntityGraph(attributePaths = {"list"})
    @Query("select distinct b from board b where b.id =:id")
    Optional<board> findBoardEntitygraph(@Param("id") Long id); //data jpa는 자동으로 id를 인식으로 동작.


    @Query("select  b from board b where b.id =:id and b.writer =:writer")
    Optional<board> findBoardWithValidation(@Param("id") Long id,@Param("writer") String writer);

    @Modifying
    @Query("update board b " +
            "set b.title =:title, b.content =:content, b.haveFile =:haveFile " +
            "where b.id =:id")
    void updateBoardWithParam(@Param("id") Long id,@Param("title") String title
            ,@Param("content") String content,@Param("haveFile") boolean haveFile);

    @Modifying
    @Query("update board b " +
            "set b.commentNum = b.commentNum + 1" +
            "where b.id =:id")
    void updateBoardWithCommentNumPlus(@Param("id") Long id);

    @Modifying
    @Query("update board b " +
            "set b.commentNum = b.commentNum - 1" +
            "where b.id =:id")
    void updateBoardWithCommentNumMinus(@Param("id") Long id);

    @Query(value = "select b from board b")
    Page<board> findAllByPage(Pageable pageable);


    @Query(value = "select b from board b where b.title LIKE %:content% OR b.content LIKE %:content%")
    Page<board> findPostByContent(Pageable pageable,@Param("content") String content);


    @Modifying               // 삭제같은경우, @Modifying을 넣어줘야 에러가 안나는거 같습니다.
    @Query(nativeQuery = true,value = "delete from board where id =:id")
    void deleteOne(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select distinct b from board b left join fetch b.list where b.id =:id")
    Optional<board> findBoardWithFiles(@Param("id") Long id); //파일이없으면 안나오는걸 방지해 left를 붙였다.




}
