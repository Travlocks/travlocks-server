<div align="center">

# Travlocks Web

**UMC 9th Project 🚀**

</div>


<br/>

<div>
  
  ## ✈️ Travlocks : 여행 일정을 빠르게, 직관적으로, 재미있게!
  
  > **블록을 쌓듯 여행 일정을 만드는 새로운 방식의 웹 서비스 Travlocks**의 백엔드 저장소입니다.
</div>

<br/>

<div>

## 📌 Travlocks는 이런 서비스예요

> 작성 예정입니다.

</div>

<br/>

<div>

## 🙋🏻‍♀️ Travlocks의 BE Developer를 소개합니다!

| <a href="https://github.com/hyomee2"><img src="https://avatars.githubusercontent.com/u/108400640?v=4" width="120px;" alt=""/></a> | <a href="https://github.com/dh2e"><img src="https://avatars.githubusercontent.com/u/145524046?v=4" width="120px;" alt=""/></a> | <a href="https://github.com/kdhdd"><img src="https://avatars.githubusercontent.com/u/109668066?v=4" width="120px;" alt=""/></a> | <a href="https://github.com/dppfls"><img src="https://avatars.githubusercontent.com/u/107196183?v=4" width="120px;" alt=""/></a> | <a href="https://github.com/Suhyeon7"><img src="https://avatars.githubusercontent.com/u/157273486?v=4" width="120px;" alt=""/></a> | <a href="https://github.com/jeondain"><img src="https://avatars.githubusercontent.com/u/120189161?v=4" width="120px;" alt=""/></a> |
| --------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| 권형미                                                                                                                            | 권도희                                                                                                                               | 김도현                                                                                                                          | 김예린                                                                                                                            | 장수현                                                                                                                            | 전다인                                                                                                                            |

</div>

<br/>

<div>
  
  ## 💻 기술 스택

| **역할**             | **종류**                                                                                                                                                                                                                                                                                                                        | **선정 이유**                                                                                                                                |
| -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| -------------------------------------------------------------------------------------------------------------------------------------------- |
| Framework            | <img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=for-the-badge&logo=SpringBoot&logoColor=white"/>                                                                                                                                                                                                              | Java 기반으로, 강력한 타입 안정성과 Spring 생태계의 검증된 안정성으로 효율적인 서버 구축 가능                                                                |
| Database             | <img src="https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white"/>                                                                                                                                                                                                                     | 안전한 관계형 데이터 관리. 가볍고 최적화된 읽기 성능                          |
| Caching              | <img src="https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white">                                                                                                                                                                                                                   | 서비스 응답 속도 향상과 서버 부하 감소를 위해 도입                                               |
| Infra                | <img src="https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white">                                                                                                                                                                                                                 | 개발, 테스트, 운영 환경을 컨테이너를 통해 환경 일관성 확보                                                                             |
| Integration          | <img src="https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white">                                                                                                                                                                                                | 코드 통합, 빌드, 테스트를 자동화하여 개발 생산성 향상 |
| Deployment           | <img src="https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white">                                                                                                                                                                                                                | 서비스의 안정성과 확장성을 위한 클라우드 기반 인프라 구축. EC2, S3 등의 서비스를 함께 이용하여 관리 부담을 줄이고 효율적인 배포 환경 구축 가능
| Documentation        | <img src="https://img.shields.io/badge/-Swagger-%23Clojure?style=for-the-badge&logo=swagger&logoColor=white">                                                                                                                                                                                                                 | 원활한 협업을 위한 API 명세 자동화                                                                                                                                                                                                                            | 빠른 서버 시작과 모듈 번들링 성능으로 개발 생산성을 향상                                                                                     |

</div>

<br/>

<div>

## 🔗 Git Convention

### 📌 Git Flow

<img width="1280" height="720" alt="image" src="https://github.com/user-attachments/assets/b56c9fbc-f1de-47f0-a0a4-be14329652c4" />

- `main branch` : 운영 브랜치
- `develop branch` : 개발 브랜치, feature 브랜치가 merge됨
- `feature branch` : 기능 개발 브랜치

  <br>

  ### ✨ Flow
  - 이슈 생성
  - 이슈 번호에 맞게 `develop 브랜치`에서 새로운 브랜치를 생성
  - 작업을 완료하고 커밋 컨벤션에 맞게 커밋
  - Pull Request 생성
  - 코드 리뷰 후 `develop` 브랜치로 병합
    - 최소 2명 승인 후 develop 브랜치로 머지

  <br/>

### 🔥 Commit Message Convention

- **커밋 유형**
  - feat: 새로운 기능 추가
  - fix: 버그 수정
  - hotfix: 긴급 버그 수정
  - remove: 코드/파일 삭제
  - refactor: 로직 변경없이 코드 개선
  - chore: 자잘한 수정
  - test: 테스트 코드 추가
  - docs: 문서 작업
  - deploy: 배포

- **형식**: `[<커밋유형>]: #<이슈번호> <설명>`
- **예시**:
  - [feat] #2 템플릿 조회 API 구현

  <br/>

### 🌿 Branch Convention

- **브랜치 종류**
  - `init`: 프로젝트 세팅
  - `feat`: 새로운 기능 추가
  - `fix` : 버그 수정
  - `refactor` : 코드 리펙토링

- **형식**: `<브랜치종류>/#<이슈번호>-상세기능`
- **예시**: feat/#1-template
</div>

<br/>

<div>

## 📂 프로젝트 구조

```
📦travlockse-server
┣ 📂.github
┃ ┣ 📂ISSUE_TEMPLATE
┃ ┣ 📜pull_request_template.md
┃ ┗ 📂workflows
┣ 📂src/main/java/com.travlocks
┃ ┣ 📂 domain
┃ ┃ ┣ 📂 feature
┃ ┃ ┃ ┣ 📂 constant
┃ ┃ ┃ ┃ ┣ 📜 SuccessCode
┃ ┃ ┃ ┃ ┗ 📜 ErrorCode
┃ ┃ ┃ ┣ 📂 controller
┃ ┃ ┃ ┣ 📂 dto
┃ ┃ ┃ ┣ 📂 entity
┃ ┃ ┃ ┣ 📂 enums
┃ ┃ ┃ ┣ 📂 exception
┃ ┃ ┃ ┗ 📂 service
┃ ┣ 📂 global
┃ ┃ ┣ 📂 aws
┃ ┃ ┣ 📂 code
┃ ┃ ┣ 📂 common
┃ ┃ ┣ 📂 confing
┃ ┃ ┣ 📂 entity
┃ ┃ ┣ 📂 exception
┃ ┃ ┣ 📂 jwt
┃ ┃ ┣ 📂 mail
┃ ┃ ┣ 📂 response
┃ ┃ ┗ 📂 security
┃ ┗ 📂 infra
┃   ┣ 📂 kakao
┃   ┗ 📂 redis
┣ 📂 resources
┃ ┣ 📂 mail
┃ ┗ 📜 application.yml
┣ 📜 .env
┣ 📜 .gitignore
┣ 📜 build.gradle
┣ 📜 docker-compose.yml
┣ 📜 Dockerfile
┗ 📜 settings.gradle
```

## 📂 시스템 아키텍처
<img width="1116" height="676" alt="image" src="https://github.com/user-attachments/assets/64f90add-2c35-45f1-adc3-318e2375c9b1" />

</div>
