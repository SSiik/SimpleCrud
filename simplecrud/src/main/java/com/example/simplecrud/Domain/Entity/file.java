package com.example.simplecrud.Domain.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Getter @Setter
@Entity
public class file {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private board board;
    //사실 Setter를 설정해주기보다는, 엔티티내부의 연관관계편의메소드로, OneToMany쪽의 컬렉션에 file을 추가해주고,
    //이 file의 board를 셋팅해주는걸 하나의 메소드로 묶는게 좋긴합니다. Setter를 열어두는건 위험성이 존재한다.

    @Column
    private String uploadFileName;  //이정도 데이터만 DB에서 저장하도록.

    @Column
    private String storeFileName;

    @Column
    private String filePath;

    public file(String uploadFileName, String storeFileName, String filePath) {
        this.uploadFileName = uploadFileName;
        this.storeFileName = storeFileName;
        this.filePath = filePath;
    }
}
