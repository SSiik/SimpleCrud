package com.example.simplecrud;

import com.example.simplecrud.Domain.Dto.fileDto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileStore {  //파일업로드 처리를 해주는 스프링빈.

    @Value("${spring.servlet.multipart.location}")  //springframework의 Value 어노테이션이다.
    private String fileDir;

    public String getFullPath(String filename){
        return fileDir + filename;
    }

    public String extractExt(String originalFilename){
        int pos = originalFilename.lastIndexOf("."); // .의 인덱스를 알아내고
        return originalFilename.substring(pos+1); // .이후의 인덱스부터 substring으로 뽑아냅니다.
    }

    public String createStoreFileName(String originalFilename){
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext; //확장자는 살리는방법 (앞에는 UUID를 이용합니다).
    }

    public fileDto storeFile(MultipartFile file) throws IOException { //메인함수.
        if(file.isEmpty()) return null;
        String originalFilename = file.getOriginalFilename();
        String storeFilename = createStoreFileName(originalFilename);
        file.transferTo(new File(storeFilename));
        return new fileDto(originalFilename,storeFilename);
    }



}
