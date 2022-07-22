package com.example.simplecrud.Repository;

import com.example.simplecrud.Domain.Entity.file;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class fileJDbcRepository {
    private final JdbcTemplate jdbcTemplate;
    private int batchSize = 5;
    private int batchCount=0;

    public void saveAll(List<file> files) {
        System.out.println("files.size() = " + files.size());
        List<file> file = new ArrayList<>(); //여기에 모아서 batch 쿼리를 날립니다.
        for(int i=0;i<files.size();i++){
            file.add(files.get(i));
            if( (i+1) % batchSize == 0){
//                batchCount = batchInsert(batchSize,batchCount,file); //여기서 자료구조 clear로직이 있을것.
                batchInsert(batchSize,file);
            }
        }
        if(!file.isEmpty()){
            batchInsert(batchSize,file);
        }
        System.out.println("batchCount : "+batchCount);
    }

    public void batchInsert(int batchSize,List<file> file){
        jdbcTemplate.batchUpdate("insert into file (`store_file_name`,`upload_file_name`,`file_path`,`board_id`) values (?,?,?,?)",
                new BatchPreparedStatementSetter(){ //batchUpdate에서 들어온 list를 한번에 처리해주는 느낌이다.

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1,file.get(i).getStoreFileName());
                        ps.setString(2,file.get(i).getUploadFileName());
                        ps.setString(3,file.get(i).getFilePath());
                        ps.setString(4,file.get(i).getBoard().getId().toString());
                        //외래키값을 넣어줘야한다. 객체그대로 넣을수없으니, 그 객체의 id를 뽑아서 String으로 만들어줘야함.
                    } //?에 파라미터를 설정해주는작업.

                    @Override
                    public int getBatchSize() {
                        return file.size();
                    }
                });
        file.clear(); //여기서 한번 비워주는것도 핵심.
        batchCount++;
    }
}
