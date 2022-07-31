package com.example.simplecrud.Service;

import com.amazonaws.util.IOUtils;
import com.example.simplecrud.Domain.Dto.*;
import com.example.simplecrud.Domain.Entity.*;
import com.example.simplecrud.FileStore;
import com.example.simplecrud.Repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.file.ConfigurationSource;
import org.hibernate.StaleStateException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.validation.constraints.Null;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.*;

/*
* @Transactional을 사용하면, 트랜잭션 관련 문제가 발생시 rollback시킬수 있고,
* 무엇보다 "동시성 문제에 대한 걱정도 해결할수 있다".
* 이제 트랜잭션 고립레벨이라는게 존재합니다 0~3 까지 존재합니다.
* 레벨 0(read uncommited) : 아무런 락도 걸지않음. 동시에 접근할때 모든 sql문에 제한x.
* 레벨 1(read commited) : 해당레벨에서 변경중인 데이터의 값을 읽을수없다. 조회중일때는 변경은 가능함....
* 레벨 2(repeatable read) : 변경중인 데이터의값을 읽을수 없을뿐아니라, 특정 태랜잭션에서 조회중인 데이터의값을 변경불가!
* 레벨 3(serializable) : 트랜잭션을 다른 트랜잭션으로 부터 완전히 분리시킨다. 특정 트랜잭션이 작업을 하는동안, 타 트랜잭션은
*                        별도의 작업을 할수없다.
* @Transactional 만 그냥 붙이면 트랜잭션 고립레벨은 jdbc 기본 isolation 레벨로 이제 mysql이라면 level2가 적용됩니다.
*  이제 고립레벨이 높다고해서 좋은건아님, 고립성이 높아지는 만큼 성능은 떨어진다. 다른 트랜잭션에서 제한되는 점도 많다.
* 보통은 레벨1,2를 많이 쓴다고한다.
* 그리고 이제 @Transactional의 isolation 속성에서 트랜잭션 isolation level을 지정해줄수가 있습니다. 한번해보자.
*
* 그전에 트랜잭션 충돌에 관련되서 일어나는 일 3가지를 살펴봅시다.  "동시에 트랜잭션이 진행된다고 가정하는겁니다. 동일 row에".
* dirty read : 일단 write 트랜잭션이 공유데이터(겹치는 row)를 update하고 커밋되지않아 disk(db)미반영.
*              그리고 read트랜잭션은 커밋되지않은 dirty page data를 read합니다. 커밋되지않은걸 읽는다는겁니다.
*              커밋되지않은걸 읽는건 위험합니다. 에러가나서 롤백되면 원래 데이터로 돌아가기때문이죠.
*              그래서 다른트랜잭션에서 write한게 실패해서 원래값으로 돌아갑니다.
*              그리고 다시 re-read를 진행해서 원래값을 읽습니다.
* non-repeatable read : 한 트랜잭션 내에서 같은 쿼리 2번을 실행시키는데, 이제 달느 트랜잭션에서 변경되서 커밋됫을시,
*                        다시 조회할때 값이 달라질수 있습니다.
* phantom read : 이제 앞에 반복데이터를 n번 읽을때 다른걸 가져오는 현상은 없어지지만, 이제 동일쿼리를 2번실행할때,
*                처음에 없던 데이터가 2번째 쿼리에서 발생핧수잇다. 다른 transaction에서 insert가 들어왔다는말이됩니다.
* 그래서 이런상황들을 방지해주는 트랜잭션 isolation level을 우리가 설정할수 있다는겁니다.
* */
@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {
    private final AwsS3Service awsS3Service; //일단 서비스가 서비스에 의존하는형태.

    private final EntityManager em;
    private final userRepository userRepository;
    private final fileJDbcRepository fileJDbcRepository;
    private final boardRepository boardRepository;
    private final fileRepository fileRepository;
    private final commentRepository commentRepository;

    //Pessimistic Lock => 무조건 , 내가 건드릴거니까 나머지는 건들지마 느낌.
    /*
    * shared lock : 다른 사용자가 동시에 읽을수는 있지만, update delete를 방지함.
    * exclusive lock : 다른 사용자가 읽기,수정,삭제 모두를 불가능하게함.ㅎㅇ , PESSIMISTIC_WRITE, 다른곳에서 가져오는시점에서부터 터짐.
    * 수정이 많을시 -> PESSIMISTIC LOCK이 적절, 일반적인 웹 애플리케이션은 optimistic Lock을 주로 사용한다고 하네요.
    * 이제 게시글을 가져올때 댓글까지 락을 걸거냐 뭐 이런게 생각해볼만한 여지가 될것같습니다.
    * */

    public board validationUpdateBoard(String ide,Long id){
        System.out.println(Thread.currentThread().getId());
        System.out.println("=========================================================================");
        board board = boardRepository.findBoardWithValidation(id, ide)
                .orElseThrow(() -> new RuntimeException("게시글이 없거나 권한이 없는 사용자입니다."));
        log.info("게시글 , 사용자를 동시에 확인하는중입니다!!!!!!!!!!!!");
        return board;
    }

    public Object validationBoardWithComment(Long board_id , @Nullable Long comment_id){
        System.out.println(Thread.currentThread().getId());
        System.out.println("=========================================================================");
        if(comment_id == null){ //루트댓글을 위한 검증을 실시.
            board board = boardRepository.findById(board_id)
                    .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));
            return board;
        }
        else{
            comment comment = commentRepository.findByIdWithBoard(comment_id)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 댓글 혹은 게시글 입니다."));
            return comment;
        }
        //Object를 반환하는걸로 서로 다른 Entity를 반환하게 한다.
    }

    public comment validationBoardWithDelete(Long comment_id,String ide){
        System.out.println(Thread.currentThread().getId());
        System.out.println("=========================================================================");
        comment comment = commentRepository.findById(comment_id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 댓글 혹은 게시글입니다."));//여기서 오면 게시글까지 있다는거.
        if(!comment.getWriter().equals(ide)){
            throw new RuntimeException("삭제권한이 없는 사용자입니다.");
        }
        return comment;
    }


    @Transactional
    @Async   //비동기로 박아줫다, async메소드에선 파라미터로 MultipartFile이 리소스가 릴리즈되버려서, DTO로 받아온모습.
    public void post(String writer, String title, String content,
                     @Nullable List<fileTransferDto> files) throws IOException {
        board board = new board(writer, title, 0, 0, content, false);
        System.out.println(Thread.currentThread().getId());
        System.out.println("=========================================================================");
        if (files == null) {
            boardRepository.save(board);
        } else {
            board.setHaveFile(true); //이렇게 flag를 만들어보자.
            boardRepository.save(board);
            boardAndFileDto dto = new boardAndFileDto();
            dto.setWriter(writer);
            dto.setContent(content);
            dto.setTitle(title);
            List<file> lists = new ArrayList<>();
            for (fileTransferDto file : files) {
                fileSaveDto to = new fileSaveDto();
                String originalName = file.getOriginalFileName();
                log.info("실제 파일저장 시작입니다..");
                AwsS3 upload = awsS3Service.uploads(file, "upload", board.getId()); //awsS3에 저장한 이후에는 DB에 저장할것도 필요합니다.
                file fileInfo = new file(originalName, upload.getKey(), upload.getPath()); //원래파일이름, 바뀐파일(저장된)이름 , 파일의URL정도.
                board.getList().add(fileInfo);
                fileInfo.setBoard(board); //외래키 설정.
                lists.add(fileInfo);
            }
            System.out.println("실행흐름좀 봅시다 post함수 내부 세번째문장!!");
            fileJDbcRepository.saveAll(lists); //bulk insert를 IDENTITY전략속에서, JDBCtemplate을 이용.
        }
    }

//    @Transactional  //totalService로 이전.
//    public void update(String writer, String title, String content,
//                     @Nullable List<fileTransferDto> files,Long board_id) throws IOException {
//        board board = validationUpdateBoard(writer, board_id);
//        doUpdate(title, content, files, board_id,board);
//    }

    //게시글 수정시 적은글에 대한 정보를 반환.
    @Transactional
    public updateDto getPost(Long board_id, String ide) throws MalformedURLException {
        board board = boardRepository.findBoardEntitygraph(board_id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글입니다."));
        if (!board.getWriter().equals(ide)) throw new RuntimeException("수정권한이 없는 사용자입니다.");

        postDto postDto = new postDto();
        String title = board.getTitle();
        String body = board.getContent();
        List<file> contents = board.getList();  //fetch join을 통해서 데이터가 들어있을까?
        List<fileAnotherDto> lists = new ArrayList<>();
        for (file file : contents) {
//                    fileTransferDto fileTransferDto = new fileTransferDto();
            fileAnotherDto fileTransferDto = new fileAnotherDto();
            fileTransferDto.setOriginalFileName(file.getUploadFileName());
            fileTransferDto.setFilePath(file.getFilePath());
            lists.add(fileTransferDto);
        }
        updateDto updateDto = new updateDto();
        updateDto.setTitle(title);
        updateDto.setContent(body);
        updateDto.setLists(lists);
        return updateDto;

    }

    //글 삭제.
    //여기서 하나 잡을가정이, 글삭제를 뭐 글상세보기에서 하든, 마이페이지에서 하던간에. 이 2개의 페이지에는 글쓴사람의 ide가 있을거다.
    //이걸 같이 넘겨줘서 비교하는걸로 갑시다. 권한있는지 판단하는건 이건 솔직히 너무 힘든거 같긴합니다. 이거 찾으로 쿼리타는게.
//    @Transactional
//    public void deleteBoard(Long board_id, String ide) {
//        board board = validationUpdateBoard(ide, board_id);
//        doDelete(board);
//        /*
//         * 게시글 삭제 과정
//         * 1. s3에서 board관련 파일 전부삭제
//         * 2. board관련 파일들을 전부 삭제 (bulk delete 사용)
//         * 3. board 게시글 자체를 삭제.
//         * */
//    }

    public Page<listDto> listDto(int num) { //가져올때 5개씩 끊어서 페이징을 하겟다..
        //페이지번호는 이제 시작이 0번 페이지부터 시작.
        PageRequest pageRequest = PageRequest.of(num, 5, Sort.by(Sort.Direction.DESC, "id"));
        Page<board> page = boardRepository.findAllByPage(pageRequest);

        //일단 게시판에 보여줄 정보들만 보낸다. 여기서 id를 보내주고, 그 id기반으로 글 상세요청을 진행할것이다.
        Page<listDto> map = page.map(m -> new listDto(m.getId(), m.getTitle(), m.getVisited(),  m.getWriter(),m.getCommentNum(), m.isHaveFile()));
        return map;
    }

    public Page<listDto> searchList(String content,int num) { //가져올때 5개씩 끊어서 페이징을 하겟다..

        PageRequest pageRequest = PageRequest.of(num, 5, Sort.by(Sort.Direction.DESC, "id"));
        Page<board> page = boardRepository.findPostByContent(pageRequest,content);

        //일단 게시판에 보여줄 정보들만 보낸다. 여기서 id를 보내주고, 그 id기반으로 글 상세요청을 진행할것이다.
        Page<listDto> map = page.map(m -> new listDto(m.getId(), m.getTitle(), m.getVisited(), m.getWriter(),m.getCommentNum(), m.isHaveFile()));
        return map;
    }

    @Async @Transactional
    public void executeComment(String ide, String content, Object entity) {
        System.out.println(Thread.currentThread().getId());
        System.out.println("=========================================================================");
        if(entity instanceof board) {
            board board =(board)entity; //entity를 이제 board로 변환.
            comment comm = new comment(content, board, ide, false); //일단기본적인 값들 설정.
            commentRepository.save(comm);
            boardRepository.updateBoardWithCommentNumPlus(board.getId());
        }
        else if (entity instanceof comment){
            comment parentComment = (comment) entity;
            comment comm = new comment(content, parentComment.getBoard(), ide,false);
            comm.setRelationship(parentComment); //여기서 서로 부모를추가하고, 자식을 추가하는 관계를 맺음.
            commentRepository.save(comm);
            boardRepository.updateBoardWithCommentNumPlus(parentComment.getBoard().getId());
        }
    }

//    @Transactional
//    public void deleteComment(Long comment_id,String ide){ //댓글에 관련된 대댓글은 삭제하지 않도록.
//        comment comment = validationBoardWithDelete(comment_id, ide);
//        //실제로 우린 삭제하지 않고, "임의의 데이터로" 바꿔서 넣게 될겁니다. 다른게시판들처럼 삭제된 댓글입니다로 치환할 생각입니다.
//        doDelete(comment);
//    }

    @Async @Transactional
    public void doDeleteComment(comment comment1) {  //동시성에 대한 PESSIMISTIC LOCK을 걸어야할까?
        //일단 비동기로 처리되니 영속성컨텍스트에 번거롭지만 다시 초기화합니다 쿼리수+1
        comment comment = commentRepository.findByIdWithBoardToDelete(comment1.getId()).get();
        System.out.println(Thread.currentThread().getId());
        System.out.println("=========================================================================");
        comment.findDeletableComment().ifPresentOrElse(c -> commentRepository.delete(c), () -> comment.delete());
        //ifPresentOrElse로 Empty 일수도 그러면 그냥, 현재 댓글만 임시상태표시로. 삭제가능한 지점을 찾으면 Repository이용.
        comment.getBoard().setCommentNum(comment.getBoard().getCommentNum()-1);//어차피 불러오는김에 dirty checking으로 처리.
        //boardRepository.updateBoardWithCommentNumMinus(comment.getBoard().getId());
        //다른쓰레드 이므로 직접적인 수동 update쿼리를 이용한다.
    }


    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public checkDto findPost(Long id) {
        //조회수 초기화 최적화는 실패. 최대 15개 요청만 제대로 조회수반영.
        //동시접속자 수에 대한 테스트를 원한다면 ramp-up time을 0으로 설정해야한다.
        //일단 쿼리를 2번 씁시다. 이게 어쩔수없습니다. 1:n 을 2번 써야할것같습니다.
        // 응답은 차례차례로 옵니다.
        board board = boardRepository.findBoardWithFiles(id)
                .orElseThrow(() -> new RuntimeException("존재하는 게시글이 없습니다."));
        checkDto checkDto = new checkDto();
        checkDto.setWriter(board.getWriter());
        checkDto.setContent(board.getContent());
        checkDto.setTitle(board.getTitle());
        checkDto.setId(board.getId());
        checkDto.setVisited(board.getVisited());
        List<fileAnotherDto> files = new ArrayList<>();
        List<file> list = board.getList();
        if (!list.isEmpty()) {
            for (file file : list) {
                fileAnotherDto dto = new fileAnotherDto();
                dto.setOriginalFileName(file.getUploadFileName());
                dto.setFilePath(file.getFilePath());
                files.add(dto);
            }
            checkDto.setFiles(files);
        }
    // 최상위 계층의 댓글을 가져오고,

        List<comment> comments = commentRepository.findCommentByBoard_id(id);
        List<commentResponseDto> commentResponseDtos = convertNestedStructure(comments);
        checkDto.setComments(commentResponseDtos);
//        board.setVisited(board.getVisited()+1);

        try{
            board.setVisited(board.getVisited()+1);
        }
        catch(Exception e){
//            board entity = (board) e.getEntity(); //여기서 버전이 업데이트 됫을까?
//            entity.setVisited(entity.getVisited()+1);
            System.out.println("e.getMessage() = " + e.getMessage());
      }
        return checkDto;
    }

    private List<commentResponseDto> convertNestedStructure(List<comment> comments) {
        List<commentResponseDto> result = new ArrayList<>();
        Map<Long, commentResponseDto> map = new HashMap<>();
        comments.stream().forEach(c -> {
            commentResponseDto dto = convertCommentToDto(c);
            map.put(dto.getId(), dto);
            if(c.getParent() != null) map.get(c.getParent().getId()).getChildren().add(dto);
            else result.add(dto);
        });
        return result;
    }

    private commentResponseDto convertCommentToDto(comment c) {
        commentResponseDto commentDto = new commentResponseDto();
        commentDto.setId(c.getId());
        commentDto.setContent(c.getContent());
        commentDto.setWriter(c.getWriter());
        commentDto.setBoard_id(c.getBoard().getId());
        return commentDto;
    }


    @Async @Transactional
    public void doUpdate(String title, String content, @Nullable List<fileTransferDto> files, board board) throws IOException {
        System.out.println(Thread.currentThread().getId());
        System.out.println("=========================================================================");
//        board board = boardRepository.findBoardWithValidation(board_id, writer)
//                .orElseThrow(() -> new RuntimeException("게시글이 없거나 권한이 존재하지 않습니다."));
        if (files != null) { //새로 들어오는 첨부파일이 있다는 얘기.
            if (!board.isHaveFile()) {
                //1 . 게시글은 있지만 첨부파일은 없었던경우. 그러면 이제 들어온 파일은 다 넣어주면 됩니다.
                List<file> lists = new ArrayList<>();
                for (fileTransferDto file : files) {
                    String originalName = file.getOriginalFileName();
                    log.info("실제 파일저장 시작입니다..");
                    AwsS3 upload = awsS3Service.uploads(file, "upload", board.getId()); //awsS3에 저장한 이후에는 DB에 저장할것도 필요합니다.
                    file fileInfo = new file(originalName, upload.getKey(), upload.getPath()); //원래파일이름, 바뀐파일(저장된)이름 , 파일의URL정도.
                    fileInfo.setBoard(board); //파라미터로 받아오는거긴 하지만, 내부에 id는 있으니 설정은 될겁니다.
                    lists.add(fileInfo); //lists에 이제 저장하기 위해서 채웁니다.
                }
//                board.setTitle(title);
//                board.setContent(content);
//                board.setHaveFile(true);
                fileJDbcRepository.saveAll(lists); //bulk insert를 IDENTITY전략속에서, JDBCtemplate을 이용.
                boardRepository.updateBoardWithParam(board.getId(),title,content,true);
            } else {
                //2 . 게시글도 있고, 첨부파일도 기존에 있었던경우. 이러면 좀 갈아줘야합니다.

                awsS3Service.deleteAll("upload", board.getId()); //사전작업1, s3에 파일삭제
                fileRepository.deleteAllByBoard(board.getId()); //사전작업2 , DB에서 bulk delete.
                //자 이 bulk연산이후에 자동 flush를 하지 않을겁니다. 일부러.
                // 그리고 영속성컨텍스트도 안비울겁니다 일부러.
//                board.setTitle(title);
//                board.setContent(content);
                //설마 프록시와 충돌하려나?
                List<file> lists = new ArrayList<>();
                for (fileTransferDto file : files) {
                    String originalName = file.getOriginalFileName();
                    log.info("실제 파일저장 시작입니다..");
                    AwsS3 upload = awsS3Service.uploads(file, "upload", board.getId()); //awsS3에 저장한 이후에는 DB에 저장할것도 필요합니다.
                    file fileInfo = new file(originalName, upload.getKey(), upload.getPath()); //원래파일이름, 바뀐파일(저장된)이름 , 파일의URL정도.
//                    board.getList().add(fileInfo); //앞에서 비운후에 새롭게 넣어줍니다. 여기서 프록시관련 오류?
                    fileInfo.setBoard(board); //외래키 설정.
                    lists.add(fileInfo); //lists에 이제 저장하기 위해서 채웁니다.
                }
                fileJDbcRepository.saveAll(lists);
                boardRepository.updateBoardWithParam(board.getId(),title,content,true);
            }
        } else { //넘어온 첨부파일이 없는경우.
            if (board.isHaveFile()) { //기존에 파일이 있었다면?
                awsS3Service.deleteAll("upload", board.getId());
                fileRepository.deleteAllByBoard(board.getId());             //기존에 있던 파일정보를 DB에서 삭제.
            }
            boardRepository.updateBoardWithParam(board.getId(),title,content,false);
        }
    }

    @Async @Transactional
    public void doDelete(board board) {
        System.out.println(Thread.currentThread().getId());
        System.out.println("=========================================================================");
        List<file> parts = new ArrayList<>();
        if (board.isHaveFile()) {
            //일단 s3에서 파일들 다 삭제.
            fileRepository.deleteAllByBoard(board.getId());
            awsS3Service.deleteAll("upload", board.getId());
            //일대다 페치 조인(컬렉션 페치조인)으로 해서, 가져오고 cascade를 이용해서, '일'을 삭제하면 '다'를 삭제할수는있지만,
            //이걸 '다'쪽이 계속 단퀀커리가 계속 날라가서 너무 비효율적입니다. 사실상 그래서 네트워크를 또 타더라도, @Modifying으로 진행하자.
            //게시글 수정때 처럼, 그냥 bulk delete를 해서 board관련 file을 다 지우고, 그 이후에 board도 지우는 방식으로 갑시다.
            //쿼리를 2번이나 타긴하지만, 일대다 상황에서 risk를 감수하는거보단 날겁니다.
        }
        commentRepository.deleteAllByBoard(board.getId());
        boardRepository.deleteOne(board.getId()); // 직접접근하는 native query로 바꿔봣습니다.
                                                  // spring data jpa대신에 native query사용으로 변경.
    }
}