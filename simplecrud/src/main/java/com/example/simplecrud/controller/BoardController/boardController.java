package com.example.simplecrud.controller.BoardController;

import com.example.simplecrud.Domain.Dto.*;

import com.example.simplecrud.Service.BoardService;
import com.example.simplecrud.Service.totalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class boardController {
    private final BoardService boardService;
    private final totalService totalService;

    @GetMapping("board/list")
    public ResponseEntity<Page<listDto>> getPosts(int page){
        //페이징 처리. 게시판 목록을 보여주는 메인페이지겠죠.
        Page<listDto> listDtos = boardService.listDto(page);
        return new ResponseEntity<>(listDtos, HttpStatus.ACCEPTED);
    }

    @GetMapping("board/search")
    public ResponseEntity<Page<listDto>> getPosts(String content,int page){ //페이징 처리. 게시판 목록을 보여주는 메인페이지겠죠.
        Page<listDto> listDtos = boardService.searchList(content,page);
        return new ResponseEntity<>(listDtos, HttpStatus.ACCEPTED);
    }

    @GetMapping("board/find/{id}")
    public  checkDto findBoard(@PathVariable Long id) {
        checkDto post = boardService.findPost(id);
        return post;
    }

    //댓글 작성
    @PostMapping("board/private/comment")
    public String commentPost(@Validated commentDto commentDto,HttpServletRequest request){
        String ide = (String)request.getAttribute("userId"); //정보 획득.
        //댓글id가 넘어오냐 안오냐에 따라서 대댓글인지 댓글인지 판단합니다.
        //일반 댓글이라면 아직 id가 없고, 대댓글이면 대상댓글에 id가 존재하겠죠.

        System.out.println(Thread.currentThread().getId());
        System.out.println("=========================================================================");
        if(commentDto.getComment_id()==null) {
            boardService.doComment(ide,commentDto.getContent(),commentDto.getBoard_id(),null);
        }
        else {
            boardService.doComment(ide,commentDto.getContent(),commentDto.getBoard_id(),commentDto.getComment_id());
        }
        return "OK";
    }

    @DeleteMapping("board/private/comment")   //댓글 삭제
    public String commentPost(Long id,HttpServletRequest request){
        System.out.println(Thread.currentThread().getId());
        System.out.println("=========================================================================");
        String ide = (String)request.getAttribute("userId"); //정보 획득.
        //댓글id가 넘어오냐 안오냐에 따라서 대댓글인지 댓글인지 판단합니다.
        //일반 댓글이라면 아직 id가 없고, 대댓글이면 대상댓글에 id가 존재하겠죠.
        boardService.deleteComment(id,ide);
        return "OK";
    }

    //인터셉터에서 로그인여부판단, 여기선 로그인이 됬다고 판단 진행
    @PostMapping("/board/private/post") //글 작성
    public String post(@Validated @ModelAttribute postDto postDto ,
                       HttpServletRequest request) throws IOException {
        System.out.println(Thread.currentThread().getId());
        System.out.println("=========================================================================");
        String ide = (String)request.getAttribute("userId"); //정보 획득.
        List<fileTransferDto> files = new ArrayList<>();
        if(postDto.getFiles() != null) {
            List<MultipartFile> lists = postDto.getFiles();
            for (MultipartFile list : lists) {
                String originalFilename = list.getOriginalFilename();
                byte[] bytes = list.getBytes();
                fileTransferDto dto = new fileTransferDto(originalFilename, bytes);
                files.add(dto);
            }
        }
        boardService.post(ide,postDto.getTitle(),postDto.getContent(),files);
        return "OK";
    }

    @PostMapping("/board/private/update")  //글 업데이트
    public String update(@Validated @ModelAttribute postDto postDto,HttpServletRequest request,
                         Long board_id) throws IOException {
        System.out.println(Thread.currentThread().getId());
        System.out.println("=========================================================================");
        //board_id가 파라미터로 필요할까?
        String ide = (String)request.getAttribute("userId"); //정보 획득.
        List<fileTransferDto> files = new ArrayList<>();
        if(postDto.getFiles() != null) {
            List<MultipartFile> lists = postDto.getFiles();
            for (MultipartFile list : lists) {
                String originalFilename = list.getOriginalFilename();
                byte[] bytes = list.getBytes();
                fileTransferDto dto = new fileTransferDto(originalFilename, bytes);
                files.add(dto);
            }
        }
        totalService.update(ide,postDto.getTitle(),postDto.getContent(),files,board_id);
        return "OK";
    }

    @GetMapping("/board/private/info") //게시글 수정 버튼을 눌럿을때 정보를 가져와야함.
    public updateDto beforeUpdate(HttpServletRequest request,
                         Long board_id) throws IOException {
        String ide = (String)request.getAttribute("userId"); //정보 획득.
        return boardService.getPost(board_id,ide);

    }

    @PostMapping("/board/private/delete")  //글 삭제
    public String delete(HttpServletRequest request,Long board_id) throws IOException {
        System.out.println(Thread.currentThread().getId());
        System.out.println("=========================================================================");
        String ide = (String)request.getAttribute("userId"); //정보 획득. 비동기메소드 전까지는 동기적으로 실행된다.
        boardService.deleteBoard(board_id,ide);
        return "OK";
    }
}
