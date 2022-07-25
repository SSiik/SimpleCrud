package com.example.simplecrud.Interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
public class SessionInterceptor implements HandlerInterceptor {
    // alt + insert 자동완성으로 override 메소드를 구현할수 있다.
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //session 확인 , JSESSIONID라는 쿠키가 넘어오는지 확인하는것.
        log.info("========================================================================================================");
        log.info("로그인 확인 인터셉터가 동작합니다.");
        HttpSession session = request.getSession(false);
        if(session == null) throw new RuntimeException("로그인되지 않은 사용자입니다.");
        else{
            Object loginMember = session.getAttribute("loginMember");
            request.setAttribute("userId",(String)loginMember); //컨트롤러에서 여기서 꺼내서 쓴다?
            return true;
        }
    }
}
