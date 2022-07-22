package com.example.simplecrud.controller.BoardController;

import com.example.simplecrud.Domain.Dto.*;

import com.example.simplecrud.Service.AwsS3Service;
import com.example.simplecrud.Service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class boardController {
    private final BoardService boardService;

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

    //댓글 입력 , 어떤정보를 줄거냐에 따라 요청을 상상해볼수 있습니다.
    @PostMapping("board/private/comment")
    public String commentPost(commentDto commentDto,HttpServletRequest request){
        String ide = (String)request.getAttribute("userId"); //정보 획득.
        //댓글id가 넘어오냐 안오냐에 따라서 대댓글인지 댓글인지 판단합니다.
        //일반 댓글이라면 아직 id가 없고, 대댓글이면 대상댓글에 id가 존재하겠죠.
        if(commentDto.getComment_id()==null) {
            boardService.doComment(ide,commentDto.getContent(),commentDto.getBoard_id(),null);
        }
        else {
            boardService.doComment(ide,commentDto.getContent(),commentDto.getBoard_id(),commentDto.getComment_id());
        }
        return "OK";
    }

    @DeleteMapping("board/private/comment")   //DeleteMapping을 이용.
    public String commentPost(Long id,HttpServletRequest request){
        String ide = (String)request.getAttribute("userId"); //정보 획득.
        //댓글id가 넘어오냐 안오냐에 따라서 대댓글인지 댓글인지 판단합니다.
        //일반 댓글이라면 아직 id가 없고, 대댓글이면 대상댓글에 id가 존재하겠죠.
        boardService.deleteComment(id,ide);
        return "OK";
    }

    //인터셉터에서 로그인여부판단, 여기선 로그인이 됬다고 판단 진행
    @PostMapping("/board/private/post")
    public String post(@Validated @ModelAttribute postDto postDto ,
                       HttpServletRequest request, @Nullable Long board_id) throws IOException {
        System.out.println("[post] Thread Name :: " + Thread.currentThread().getName());
        String ide = (String)request.getAttribute("userId"); //정보 획득.
        System.out.println("ide = " + ide);
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
        if(board_id==null){ //board_id에 null이라도 넣어줘야 합니다.
            boardService.post(ide,postDto.getTitle(),postDto.getContent(),files,null);
        }
        else{ //값이 있다면 수정으로 동작.
            boardService.post(ide,postDto.getTitle(),postDto.getContent(),files,board_id);
        }
        return "OK";
    }

    @PostMapping("/board/private/update") //게시글 수정 버튼을 눌럿을때 정보를 가져와야함.
    public updateDto update(HttpServletRequest request,
                         Long board_id) throws IOException {
        String ide = (String)request.getAttribute("userId"); //정보 획득.
        return boardService.getPost(board_id,ide);

    }

    @PostMapping("/board/private/delete")
    public String delete(HttpServletRequest request,String writer,Long board_id,boolean isHaveFile) throws IOException {
        String ide = (String)request.getAttribute("userId"); //정보 획득. 비동기메소드 전까지는 동기적으로 실행된다.
        if(ide.equals(writer)) boardService.deleteBoard(board_id,ide,isHaveFile);
        else{
            throw new RuntimeException("삭제권한이 없는 사용자입니다.");
        }
        return "OK";
    }
}
