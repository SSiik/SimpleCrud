package com.example.simplecrud.Service;

import com.example.simplecrud.Domain.Dto.signupDto;
import com.example.simplecrud.Domain.Entity.user;
import com.example.simplecrud.Repository.userRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class userService {
    private final userRepository userRepository;

    public boolean duplicateCheck(String id){ //아이디(ide) 중복체크 로직.
        List<user> all = userRepository.findAll();
        boolean flag=true;
        for (user user : all) {
            if(id.equals(user.getIde())){
                flag = false;
                break;
            }
        }
        return flag;
    }
    public void join(signupDto signupDto){
        user user = new user(signupDto.getName(),signupDto.getId(),signupDto.getPassword());
        userRepository.save(user);

    }

    public String login(String ide,String password){
        user user = userRepository.findByIde(ide); //ide는 id를 의미. spring data jpa의 쿼리메소드 기능사용.
        log.info("로그인 요청이 들어왔습니다.");
        if(user.getPassword().equals(password)){ //해당 ide에 비밀번호가 일치하는지 확인.
            return ide;
        }
        else{
            return null;
        }
    }


}
