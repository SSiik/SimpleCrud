# 게시판 REST API 작성하기

<p align="center">
  <br>
  <img src="./images/common/logo-sample.jpeg">
  <br>
</p>

## 프로젝트 소개

  ### 기본적인 CRUD 기능 첨부파일은 Amazon s3를 통해서 진행하였습니다.
  
      데이터베이스는 로컬에서 MySQL을 이용하였으며, 만들어가는데 있어서,
  
      JPQL,SQL 쿼리를 가능한 수준에서 최적화를 시도해보는 학습목적을 가지고 진행해보았습니다.


<p align="center">
  
  진행중 '22.07.22 ~
  
</p>

<br>

## 기술 스택

| SpringBoot | MySQL |  Spring data JPA   |  PostMan   | JMeter |
| :--------: | :--------: | :--------: | :--------: | :--------: |
|   ![sb]    |   ![my]    |   ![dj]    |   ![pos]    |   ![jm]    |

<br>

## 구현 기능

### 기능 1 ( CRUD 의 C(Create) )
- 사용자(user Entity) 등록 (spring data jpa)
- 게시글(board Entity) 등록 (spring data jpa) 
- 게시글에 대한 댓글(comment Entity) 등록
 
### 기능 2 ( CRUD 의 R(Read) )
- 게시글 상세확인
- 게시글 목록확인(페이징)
- 게시글 검색
- 게시글 정보 획득 (게시글 수정시에, 기존 입력된 게시글 정보를 가져옴)

### 기능 3 ( CRUD 의 U(Update) )
- 게시글 수정

### 기능 4 ( CRUD 의 D(Delete) )
- 댓글 삭제
- 게시글 삭제

### 기능 5 ( 부수적인 구현, 기능사용 )
- user table을 통한 로그인,로그아웃 -> HttpSession과 Interceptor 사용 </br>
  : Interceptor를 통해서 기능에 대한 요청이 들어올시 로그인여부를 판단하게 됩니다.
- Amazon s3 을 통해서 이미지를 저장할 이미지 서버로 사용하였습니다.

<br>

## 기능 설명

### 사용자 관련 처리
 1. 아이디 중복체크 : 불가피하게 모든 사용자들중에서 탐색을 해봐야함
 2. 로그인 로직 : 중복을 허용하지않는 id로 사용자를 찾고, 그 사용자의 password와 입력받은 password와 비교해 진행합니다.
 3. 로그아웃 로직 : 현재 서버에 세션자원이 있다면 로그아웃으로 처리(invalidate), 세션이 기존에 없다면 잘못된 요청일겁니다.

### 게시글 관련 처리

   **1. 게시글 등록 : (최대 쿼리 2번)** </br></br>
                 "첨부파일"이 존재하지 않을경우 1번의 쿼리로 동작. </br>
                 "첨부파일"이 존재할경우 2번의 쿼리로 동작 </br>
                 이제 board Entity의 pk생성전략이 IDENTITY입니다. save메소드를 통해서 바로 쿼리 진행(pk획득)(1번째 쿼리). </br>
                 이제 다음 파일관련정보 저장 쿼리(file Entity)에서 board의 pk를 통해서 저장합니다. (2번째 쿼리, bulk insert). </br>
                 이 과정에서 Amazon s3로의 저장도 진행합니다. </br>
                 
   **2. 게시글 수정 : (최대 쿼리 4번)** </br></br> 
                 기존에 등록된 게시글을 불러옵니다. (1번 쿼리) </br>
                 이후에는 이제 기존 게시글에 첨부파일이 있었냐 없었냐로 처리방식이 조금 달라집니다. </br>
                 첨부파일이 기존에 없었다면, 새로 수정된 내용에는 첨부파일이 있냐 없냐에 따라 나뉩니다. </br>
                 첨부파일이 기존에 있었다면, 새로 수정된 내용에는 첨부파일이 없냐, 첨부파일이 또 있냐에 따라 나뉘겠죠. </br>
                 최대의 경우에는, 기존 첨부파일 관련된 정보삭제(2번 쿼리) </br>
                 새로 들어온 첨부파일에 대한 정보 저장(3번 쿼리) </br>
                 이제 게시판 내용 자체에 대한 update쿼리 (4번 쿼리) </br>
                 
   **3. 게시글 조회 : (최대 쿼리 3번)** </br> 
                 게시글을 가져오는데, 거기에 연관된 파일들을 가져옵니다. (1번쿼리) </br>
                 그 다음으로는 이제 댓글을 가져옵니다. 이제 계층형 댓글 구조로 구현. (2번쿼리) </br>
                 마지막은 조회수 업데이트 에대한 update쿼리입니다 (3번 쿼리) </br>
                 근데 이제 조회수를 정확하게 update하기 위해서 여기서 PESSIMISTIC LOCK을 사용하였습니다(성능감소). </br>
                 
   **4. 게시글 삭제 : (최대 쿼리 3번)** </br></br>
                 특정게시글에 대한 파일정보 전부 삭제 (1번 쿼리 , bulk delete) </br>
                 특정게시글에 대한 댓글 전부 삭제 (2번 쿼리 , bulk delete) </br>
                 그 후 마지막에, 게시글 삭제 (3번 쿼리) </br>
                 
 5. 게시글 목록 페이징 쿼리 및 게시글 검색 쿼리는 이제 1번의 쿼리로 진행하였습니다.
 
### 댓글 관련 처리
   사실 댓글관련 요청을 처리할때, 일단 그 댓글이 DB에 있는지 맨앞에 검증 로직도 필요합니다.
 
   **1. 댓글 등록 :  (최대 쿼리 4번)** </br>
                 comment_id가 로직에 넘어오느냐에 따라 '루트 댓글' 이냐 어떤 댓글에 대한 '대댓글'이냐로 나뉩니다. </br>
                 우선 게시글을 가져옵니다. 댓글수를 하나 올려주기 위해서 입니다.(1번 쿼리) </br>
                 댓글 작성이라면 이제 가져온 board Entity를 FK삼아 저장.
                 대댓글 작성이라면, 넘어온 comment_id에 해당하는 댓글을 불러옵니다. (2번 쿼리) </br>
                 이에 대해 부모 댓글 자식댓글 관계 처리후에 대댓글을 저장합니다. (3번 쿼리) </br>
                 마지막으로 게시글에다가 댓글수를 +1 해주므로 update쿼리가 한번 나갑니다. (4번 쿼리) </br>
 
   **2. 댓글 삭제 :  (최대 쿼리 2번)** </br></br>
                 사실 이제 삭제 개념이기 보다는, "임의의 데이터"로 바꿔서 넣도록 진행했습니다(isDeleted라는 flag사용). (1번 쿼리) </br>
                 그에 관련된 대댓글은 남기고 싶기때문에 이렇게 진행하였습니다. </br>
                 그리고 게시글의 댓글수를 -1하게 됩니다. (2번 쿼리) </br>
<br>

## 배운 점.

  1. 동시성 테스트로 "Apache JMeter"를 알게되었습니다. 이를통해서 게시글 조회(조회수 관련)를 테스트 했습니다. 
  2. 비동기 메소드 처리 (@Async 어노테이션) : 데이터만 검증하고 빨리 응답을 처리할수 있습니다.
  3. 트랜잭션 레벨 4단계 학습 -> MySql 사용시 default로 2단계 사용 (0,1,2,3 단계 존재)
  4. (07.24 추가) 게시글 삭제,수정시 권한검증을 서비스 계층 내부에서 진행. 하나의 트랜잭션함수 내부에서 검증함수를 호출하는방식.
     -> 내부 검증함수에서 획득한 Entity를 트랜잭션 함수에서 사용합니다.
  5. (07.25 추가) 게시글 수정,삭제 / 댓글 등록,삭제 시 서비스 계층내에서 동기적으로 검증까지 진행하고, 그 이후부터는 비동기 메소드로 진행.
  6. 계층형 댓글 삭제에 대해 고민해보자.
  7. (07.27 추가) 계층형 댓글 삭제 , @OnDelete 어노테이션을 사용. 부모댓글삭제시 자식댓글까지 1 query로 삭제.
  8. 다음 고민사항 => @Async 처리, 현재 같은 객체내에서 호출중이라, 비동기처리가 안되고있다. 이에 대한 해결책을 찾아봅시다.
     => "검증 서비스"를 하나 만드는건 어떨까?
  ※ '@Async가 동작하지 않는 경우?'
     1. 메소드가 public이 아닐때 -> @Async가 접근하지 못해서 발생한다.
     2. return type이 void가 아닐때 , main함수에서 호출시 값을 기다려야 한다고 합니다. (비동기처리 안됨)
     3. <strong>같은객체내</strong>에서 호출시도 안된다.  => (07.27) 현재 이문제에 직면 진행중.
  
<br>

## 아쉬운 점 & 혹은 진행중인 점.
  ### 쿼리를 너무 많이 사용합니다. 성능에 대한 중요성을 알고, 이에 대한 공부를 더 진행할것입니다.
  1. 댓글 관련 혹은 게시글 관련 로직을 진행할때 사전검증을 해야할 부분이 있습니다. 그 부분을 더 진행하려 합니다.
  2. @Lock 에 대한 심화적인 공부가 필요할것 같습니다.
  


<br>

## 라이센스

MIT &copy; [NoHack](mailto:lbjp114@gmail.com)

<!-- Stack Icon Refernces -->

[sb]: /images/stack/springboot.svg
[my]: /images/stack/mysql.svg
[dj]: /images/stack/datajpa.svg
[pos]: /images/stack/postman.svg
[jm]: /images/stack/apachejmeter.svg
