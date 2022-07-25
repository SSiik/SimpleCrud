package com.example.simplecrud.Interceptor;

import com.example.simplecrud.Domain.Entity.board;
import com.example.simplecrud.Repository.boardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoardInterceptor implements HandlerInterceptor {
    private final boardRepository boardRepository;
    //로그인 인증은 끝난 상태.

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //session 확인 , JSESSIONID라는 쿠키가 넘어오는지 확인하는것.
        log.info("게시글 확인 인터셉터가 동작합니다.");
        log.info("========================================================================================================");
        String id = request.getParameter("board_id");
        long l = Long.parseLong(id);
        String ide = (String)request.getAttribute("userId");
        board board = boardRepository.findById(l)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글입니다."));
        if(!board.getWriter().equals(ide)){
            throw new RuntimeException("권한이 없는 사용자입니다.");
        }
        request.setAttribute("board",board); //검증끝난 board를 request에 추가함.
        request.setAttribute("board_id",l);
        return true;
    }
}
