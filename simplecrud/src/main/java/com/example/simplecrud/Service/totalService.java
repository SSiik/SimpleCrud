package com.example.simplecrud.Service;

import com.example.simplecrud.Domain.Dto.fileTransferDto;
import com.example.simplecrud.Domain.Entity.board;
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
}
