package com.example.simplecrud.Service;

import com.example.simplecrud.Domain.Dto.fileTransferDto;
import com.example.simplecrud.Domain.Entity.board;
import com.example.simplecrud.Domain.Entity.comment;
import com.example.simplecrud.Repository.boardRepository;
import com.example.simplecrud.Repository.commentRepository;
import com.example.simplecrud.Repository.fileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class totalService {

    private final BoardService boardService;
    private final EntityManager em;

    @Transactional
    public void update(String writer, String title, String content,
                       @Nullable List<fileTransferDto> files, Long board_id) throws IOException {
        board board = boardService.validationUpdateBoard(writer, board_id);
        boardService.doUpdate(title, content, files,board);
    }

    @Transactional
    public void delete(String writer, Long board_id) throws IOException {
        board board = boardService.validationUpdateBoard(writer, board_id);
        boardService.doDelete(board);
    }

    @Transactional
    public void deleteComment(Long comment_id,String ide){ //댓글에 관련된 대댓글은 삭제하지 않도록.
        comment comment = boardService.validationBoardWithDelete(comment_id, ide);
        //실제로 우린 삭제하지 않고, "임의의 데이터로" 바꿔서 넣게 될겁니다. 다른게시판들처럼 삭제된 댓글입니다로 치환할 생각입니다.
        boardService.doDeleteComment(comment);
    }

    @Transactional
    //등록을 하는과정은 비동기로 한번 처리해봅시다. 여기 검증로직부터 다시 해야함.
    public void doComment(String ide, String content, Long board_id, @Nullable Long comment_id) {
        Object entity = boardService.validationBoardWithComment(board_id, comment_id);
        boardService.executeComment(ide, content,entity);
    }
}
