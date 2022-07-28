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

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class totalService {
    private final boardRepository boardRepository;
    private final fileRepository fileRepository;
    private final commentRepository commentRepository;
    private final BoardService boardService;

    @Transactional
    public void update(String writer, String title, String content,
                       @Nullable List<fileTransferDto> files, Long board_id) throws IOException {
        board board = boardService.validationUpdateBoard(writer, board_id);
        boardService.doUpdate(title, content, files, board);
    }
}
