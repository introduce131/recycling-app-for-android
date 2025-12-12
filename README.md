<div align="center">
<img width="256" height="256" alt="ic_launcher-playstore" src="https://github.com/user-attachments/assets/76836ce5-7d12-4eea-adc8-6f6362c6638d" />
</div>

<br><br>


# ♻️ ECOBIN

## 기간
- 2024.10.14 ~ 2024.12.06

<br/>

## 프로젝트 소개
- 이 프로젝트는 사용자의 취향, 위치, 예산을 기반으로 최적의 데이트 코스를 자동 추천하는 모바일 애플리케이션입니다.
- 카테고리 기반 추천, 사용자 선호도 반영, 지도 기반 탐색 기능을 통해
"오늘 뭐하지?"라는 고민 없이 손쉽게 다양한 데이트 루트를 만들 수 있도록 설계했습니다.
- 본 프로젝트는 실제 사용자 페르소나 분석, 기능 정의, 화면 흐름 설계 등
  사용성이 높은 데이트 코스 추천 서비스를 목표로 개발되었습니다.

---

<br/>

## 🎯 핵심 목표

- 사용자의 취향 기반으로 다양한 데이트 코스 조합을 제공
- 주변 장소 데이터를 분석하여 실시간 데이트 루트 자동 생성
- 사용자 편의를 위한 지도 기반 UX, 즐겨찾기, 최근 본 장소 관리 제공
- 누구나 직관적으로 사용할 수 있는 인터페이스 제공

<br/>

## 주요 기능
### 1. Google 계정 로그인
  - 별도의 회원가입 없이 Google 계정으로 간편 로그인
  - 신규 사용자는 닉네임·지역·분리배출 요일을 입력 후 Home 화면으로 이동

### 2. 홈 화면
- 사용자의 닉네임, 오늘이 재활용 배출일인지 여부 표시
- 알림 권한 및 활동 권한 요청
- 주요 기능(재활용품 분류, 걸음 수/탄소저감량, FAQ, 탄소중립포인트 사용처)으로 이동하는 메뉴 제공

### 3. 촬영 후 재활용품 자동 분류
- 카메라로 촬영한 이미지를 TFLite 모델로 분석
- 재활용품 종류 및 올바른 분리배출 방법 제공
- 오분류 시 사용자 리포트 기능 제공
- 촬영 기록 리스트 제공 및 삭제 기능 지원

### 4. 실시간 걸음 수 & 탄소저감량 측정
- 활동 권한 허용 시 실시간 걸음 수 측정
- 걸음 수에 비례한 탄소저감량을 그래프로 시각화
- 그래프 클릭 시 상세 정보 표시
- 매일 자정 이전 걸음 수 데이터를 DB에 자동 저장

### 5. FAQ 검색 기능
- 사용자 질문 및 재활용 관련 정보를 텍스트 검색으로 쉽게 탐색 가능

### 6. 현재 위치 기반 탄소중립포인트 사용처 확인
- 정확한 위치 권한 요청
- 내 위치 표시 및 반경 3km 내 사용처 지도 표시
- ‘전체’, ‘전자영수증’, ‘텀블러/다회용컵’, ‘친환경상품’ 카테고리별 필터 제공

### 7. 사용자 정보 수정 & 로그아웃
- 닉네임·지역·분리배출 요일 수정 가능
- 메뉴에서 로그아웃 또는 앱 종료 기능 제공

<br/>

## Tech Stack

[![kotlin](https://img.shields.io/badge/kotlin-7F52FF?logo=kotlin&logoColor=white&style=flat-square)](https://kotlinlang.org)
[![android](https://img.shields.io/badge/android-3DDC84?logo=android&logoColor=white&style=flat-square)](https://developer.android.com)
[![firebase](https://img.shields.io/badge/firebase-FFCA28?logo=firebase&logoColor=black&style=flat-square)](https://firebase.google.com)
[![python](https://img.shields.io/badge/python-3776AB?logo=python&logoColor=white&style=flat-square)](https://www.python.org)

<br><br>

## 시작, 로그인 화면
<div align="center">
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/2a98904a-d58e-46d8-96d8-4a7c1caa3a7b" />
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/85ecd39a-ab82-460b-8e20-1d7c3fe944a8" />
</div>

<br>

# 메인 화면
<div align="center">
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/18f19e06-73eb-4deb-9d08-72a15cf6c9f9" />

</div>

<br>

# 검색(재활용품 촬영)
<div align="center">
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/c38a3ce2-51db-4595-98d2-68199c7d4c7c" />
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/15f6fc61-fffd-4726-aee0-f6696b67d648" />
</div>

<br>

# 검색 목록
<div align="center">
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/beeab1ad-42b6-45c7-a97b-17e23e235acb" />
</div>

<br>

# 검색 결과 오류 신고
<div align="center">
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/e76a3e57-c6c6-45d9-bf2a-58697e42ed3b" />
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/80e6b625-3d45-497d-a3de-206ec87b63dc" />
</div>

<br>

# 환경리포트
<div align="center">
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/5c6d83ea-8015-47a5-81b5-4b06a64483d0" />
</div>

<br>

# FAQ
<div align="center">
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/68a9f625-53e5-4b32-bac5-82748eabbc52" />
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/8a2c3608-da4c-421b-8fe5-08fee2f0e940" />
</div>

<br>

# 찾기(내 주변 탄소중립포인트 확인처) 
<div align="center">
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/b6cbf073-f079-47ed-a286-c1c718014ab5" />
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/ee425dca-cb43-4c1e-b7e4-c5190e91a163" />
</div>

<br>

# 사이드 바
<div align="center">
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/3506213e-b3c3-40b8-a437-eb8b31c5c753" />
</div>

<br>

# 설정 화면
<div align="center">
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/60363d9d-98e0-4d7a-95e6-76df53a4db56" />
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/11d7c1e9-c174-497e-99f7-a6bd14928a5a" />
</div>

<br>

# 회원가입 화면
<div align="center">
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/12923011-d04b-45b0-9d3b-b180cdfddd17" />
   <img width="250" height="650" alt="image" src="https://github.com/user-attachments/assets/8af3b9bc-5e8c-419a-ade0-a099d691aa12" />
</div>

