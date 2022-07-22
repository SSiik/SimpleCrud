package com.example.simplecrud.controller.UserController;

import com.example.simplecrud.Domain.Dto.LoginDto;
import com.example.simplecrud.Domain.Dto.signupDto;
import com.example.simplecrud.Exception.DuplicateException;
import com.example.simplecrud.Service.userService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequiredArgsConstructor
public class userController {
     private final userService userService;

     @PostMapping("/user/signup")
     public String signup(@Validated @ModelAttribute signupDto signupDto){
          if(userService.duplicateCheck(signupDto.getId())){ //아이디 중복체크
               userService.join(signupDto); //중복되지 않는 아이디라면, 회원가입.
          }
          else{
               throw new IllegalArgumentException("동일한 ID가 존재합니다. 다른아이디로 진행해주십시오.");
          }

          //회원가입 데이터, name과 id 그리고 password가 넘어감.
          return "OK";
     }

     @PostMapping("/user/login")
     public String LogIn(@Validated @ModelAttribute LoginDto loginDto, HttpServletRequest request){
          //로그인 데이터, id와 password가 넘어감.
          //DB에서 맞는정보인지 비교해야합니다.
          String ide = userService.login(loginDto.getId(),loginDto.getPassword());
          if(ide != null){
               HttpSession session = request.getSession(); //기본값 true, 있다면 기존세션 반환 없다면 생성.
               session.setAttribute("loginMember",ide); //세션정보로 사용자의 ide를 들고있음. 회원가입때 검증로직으로 중복되지않을것.
          }
          else{
               throw new IllegalArgumentException("일치하지 않습니다. ID와 패스워드를 다시 확인해주십시오.");
          }
          return "OK";
     }

     @PostMapping("/user/logout")
     public String LogOut(HttpServletRequest request){
          //login요청을 한 이후에는 , postman에서는 자동으로 header에 쿠키를 추가를 해줍니다.
          HttpSession session = request.getSession(false);
          if(session != null){
               session.invalidate(); //세션저장소에서 삭제해야함.
               return "OK";
          }
          return "Fail";
     }




}
