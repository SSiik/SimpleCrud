package com.example.simplecrud.Repository;

import com.example.simplecrud.Domain.Entity.board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface boardRepository extends JpaRepository<board,Long>,boardRepositoryCustom{

    //컬렉션 페치 조인. 일대다 패치조인이므로 distinct 키워드 삽입.
    @EntityGraph(attributePaths = {"list"})
    @Query("select distinct b from board b where b.id =:id")
    Optional<board> findBoardEntitygraph(@Param("id") Long id); //data jpa는 자동으로 id를 인식으로 동작.

    @Query(value = "select b from board b")
    Page<board> findAllByPage(Pageable pageable);

    @Query(value = "select b from board b where b.title LIKE %:content% OR b.content LIKE %:content%")
    Page<board> findPostByContent(Pageable pageable,@Param("content") String content);


    void deleteById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select distinct b from board b left join fetch b.list where b.id =:id")
    Optional<board> findBoardWithFiles(@Param("id") Long id); //파일이없으면 안나오는걸 방지해 left를 붙였다.


}
