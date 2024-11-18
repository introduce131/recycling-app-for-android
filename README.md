# Recyling-app-for-android 

## 1. 테스트 방법
1. 먼저 코드를 최신으로 내려받는다 (git pull)
2. 그리고 firebase 프로젝트에 들어간다
3. `프로젝트 설정`에 들어가서 `google-services.json` 파일을 내려받는다
4. 내려받은 프로젝트를 안드로이드 스튜디오로 실행시켜준다.
5. 프로젝트의 구조를 `Android`에서 `Project`로 이동 후 내려받은 `google-services.json` 파일을 `app` 폴더에 옮겨넣는다
6. `strings.xml` 파일에 `client_id` 를 하나 추가한다
   ```
   <string name="client_id">여기</string>
   ```
