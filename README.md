#  boogi-on-and-on
> 대학생들을 위한 커뮤니티 IOS APP


## 작품개요
코로나 시국으로 인해, 대학교 수업이 약 2년간 비대면으로 진행되었다. 많은 학우들이 선/후배 및 동기와의 인적교류가 줄어들고 있다. 이러한 상황을 해소하기 위해서, 온라인 커뮤니티 애플리케이션을 제공한다. 부기온앤온은 다른 커뮤니티와 다르게, 실명제를 이용하여 인적교류를 더욱 활성화한다. 학생들은 자신이 원하는 커뮤니티에 가입하여 학우들과 일상을 공유하고 친분들 쌓을 수 있다.


## 서버 아키텍쳐
<img width="1092" alt="스크린샷 2022-11-14 오후 4 01 35" src="https://user-images.githubusercontent.com/42235949/201595589-b6517c75-9272-42c4-aab0-688972dfc707.png">


## API 문서
* [문서 열람하기 (클릭)](http://boogi-api-docs.s3-website.ap-northeast-2.amazonaws.com/)


## 기술스택 및 환경
### 벡엔드
* Java, Node.js
* Spring Boot, Spring Data JPA, QueryDSL, JUnit5
* MariaDB, Redis
* AWS EC2, RDS, ElastiCache, S3, Lambda, API Gateway

### Repository
* [boogi-api-server](https://github.com/boogi-on-and-on/boogi-api-server)
    * 메인 API 서버
* [boogi-lambda-api](https://github.com/boogi-on-and-on/boogi-lambda-api)
    * 푸시 알람을 위한 AWS lambda 코드
* [boogi-image-server](https://github.com/boogi-on-and-on/boogi-image-server)
    * 이미지 리사이징을 위한 서버


### iOS
* Xcode Swift5

### Repository
* [boogi-iOS](https://github.com/boogi-on-and-on/boogi-iOS)
    * 네이티브 iOS 코드

## 팀원
### 벡엔드
* 김선도 sdcodebase@gmail.com [github link](https://github.com/sdcodebase)
* 이용진 yjlee0235@gmail.com [github link](https://github.com/yjlee0235)

### iOS
* 김덕환 tiger1710p@gmail.com [github link](https://github.com/tiger1710)
* 이준복 junbok97@gmail.com [github link](https://github.com/junbok97)
