package com.example.simplecrud.Repository;

import com.example.simplecrud.Domain.Entity.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface userRepository extends JpaRepository<user,Long> {
    user findByIde(String ide);

    @Query("select m from user m where m.ide in :ides")
    List<user> findByIdes(@Param("ides") List<String> ides);
}
