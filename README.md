# Recyling-app-for-android 

!카카오맵이 x86/x64 환경에선 작동하지 않음!
- https://devtalk.kakao.com/t/v2-libk3fandroid-so/140053
- Window 환경의 안드로이드 에뮬레이터는 작동하지 않을 가능성이 높지만
- 실제 기기에선 작동할 가능성이 높음 왜냐면 현재 대다수의 android 기기는 ARM으로 동작하기 때문

## 테스트 방법
1. 먼저 코드를 최신으로 내려받는다
2. 그리고 firebase 프로젝트에 들어간다
3. `프로젝트 설정`에 들어가서 `google-services.json` 파일을 내려받는다
4. 내려받은 프로젝트를 안드로이드 스튜디오로 실행시켜준다.
5. 프로젝트의 구조를 `Android`에서 `Project`로 이동 후 내려받은 `google-services.json` 파일을 `app` 폴더에 옮겨넣는다
6. `strings.xml` 파일에 `client_id` 를 하나 추가한다
   ```
   <string name="client_id">여기</string>
   ```
7. Sync를 일단 한다
8. Build > Clean Project를 한다
9. Build > Rebuild Project를 한다
10. 그리고 실행을 하고 Runtime error 나면 카톡으로 물어보세요
