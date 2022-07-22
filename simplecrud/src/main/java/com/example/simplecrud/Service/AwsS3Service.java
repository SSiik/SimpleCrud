package com.example.simplecrud.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.example.simplecrud.Domain.Dto.AwsS3;
import com.example.simplecrud.Domain.Dto.fileTransferDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsS3Service {

    private final AmazonS3 amazonS3; //우리가 원하는 반환형태.
    @Value("${spring.servlet.multipart.location}") String fileDir;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket; //내 s3 버킷이름.

    public AwsS3 uploads(fileTransferDto dto, String dirName, Long board_id) throws IOException {
        //s3에 업로드하려면 MultipartFile을 File로 바꾸어야 합니다.
        File file = convertMultipartFileToFile(dto)   //여기서 값이 없다는 뜻이 된다.
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File convert fail"));
        return upload(file, dirName, board_id); //File형태로 바꾸었다면 다음메소드 upload를 시작합니다.
    }

    private AwsS3 upload(File file, String dirName,Long board_id) { //MultipartFile -> File로 바꾼후 실행.
        String key = randomFileName(file, dirName, board_id);
        String path = putS3(file, key);
        removeFile(file);

        return AwsS3
                .builder()
                .key(key)   //key는 이제 파일명을 의미하는것 같습니다.
                .path(path) //저장한 url이 나오는것 같습니다.
                .build();
    }

    private String randomFileName(File file, String dirName,Long board_id) {
        return dirName + '/' + board_id.toString() + '/' + UUID.randomUUID() + file.getName();
        // upload/랜덤UUID이후 파일의 이름을 붙여서 바꿔줍니다 => 새로운 파일명생성.
    }

    private String putS3(File uploadFile, String fileName) {
        amazonS3.putObject(new PutObjectRequest(bucket, fileName, uploadFile)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return getS3(bucket, fileName); //S3에 업로드하고 getS3를 실행합니다.
    }

    private String getS3(String bucket, String fileName) {
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    private void removeFile(File file) {
        file.delete();
    }

    public Optional<File> convertMultipartFileToFile(fileTransferDto multipartFile) throws IOException {
        File file = new File(fileDir + "/" + UUID.randomUUID()+ multipartFile.getOriginalFileName());

        if (file.createNewFile()) {
            log.info("만들어지긴 하니??????????????");
            try (FileOutputStream fos = new FileOutputStream(file)){
                System.out.println("fos = " + fos);
                fos.write(multipartFile.getContents());
            }
            return Optional.of(file);
        }
        return Optional.empty();
    }

    public void remove(AwsS3 awsS3) {
        if (!amazonS3.doesObjectExist(bucket, awsS3.getKey())) {
            throw new AmazonS3Exception("Object " +awsS3.getKey()+ " does not exist!");
        }
        amazonS3.deleteObject(bucket, awsS3.getKey());
    }

    public void deleteAll(String dirName,Long id) {
        ListObjectsV2Result listObjectsV2Result = amazonS3.listObjectsV2(bucket, dirName + "/" + id.toString() + "/");
        List<S3ObjectSummary> objectSummaries = listObjectsV2Result.getObjectSummaries();
        for (S3ObjectSummary objectSummary : objectSummaries) {
            amazonS3.deleteObject(bucket,objectSummary.getKey()); //board_id에 관련된 파일들은 일단 다 삭제.
        }
    }
}
