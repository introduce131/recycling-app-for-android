# Recyling-app-for-android 

## 테스트 방법
1. 먼저 코드를 develop 브랜치로 내려받는다 (git clone)
3. 그리고 firebase 프로젝트에 들어간다
4. `프로젝트 설정`에 들어가서 `google-services.json` 파일을 내려받는다
5. 내려받은 프로젝트를 안드로이드 스튜디오로 실행시켜준다.
6. 프로젝트의 구조를 `Android`에서 `Project`로 이동 후 내려받은 `google-services.json` 파일을 `app` 폴더에 옮겨넣는다
7. `strings.xml` 파일에 `client_id` 를 하나 추가한다
   ```
   <string name="client_id">여기</string>
   ```
8. Sync를 일단 한다
9. Build > Clean Project를 한다
10. Build > Rebuild Project를 한다
11. Github Projects에 들어가서 local.properties를 열고 내용을 복사하여 다운로드 받은 안드로이드 프로젝트 안에 있는 local.properties에 붙여넣기 한다.
12. 다시 sync를 하고, 에뮬레이터에서는 오류가 나니 안드로이드 기기를 직접 연결하여 실행한다.
13. 에러 나면 카톡으로 물어보세요.
14. 일단 기능은 거의 다 하긴했는데 지금 안되는 기능 목록
   - 처음에 앱을 설치하고 `환경 리포트`화면에 들어가면 오늘의 걸음수가 꽤 높게 찍히는 경우가 있을 수도 있음. (12-05 저녁에 해결 예정)
   - 처음에 한번에 권한을 입력받으려고 하는데 지금 비동기적으로 처리되고 있어서 앱을 여러번 껐다 켜서 권한 dialog를 통해서 권한을 활성화 해야함.
   - 그리고 Login 화면 좀 수정해야되고, 가능하면 표현식도 만져야됨
   - 그리고 매번 로그인하기가 번거로워서 쿠키같이 저장했다가 사용자가 들어왔을 때 사용자 ID가 유효하다면 로그인화면 건너뛰고 메인화면 바로 띄우는 기능 추가 할 예정
   - 그 외에 버그 발견하면 말해주세요
