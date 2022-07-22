package com.example.simplecrud.Domain.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@NoArgsConstructor
@Getter
@Entity
public class user {
    @Id @GeneratedValue
    private Long id;

    @Column
    private String name;

    @Column
    private String ide;

    @Column
    private String password;

    public user(String name, String ide, String password) {
        this.name = name;
        this.ide = ide;
        this.password = password;
    }
}
