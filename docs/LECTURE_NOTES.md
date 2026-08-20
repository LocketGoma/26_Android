# 교수자용 빠른 진행 메모

## 반복 질문

- 지금 보고 있는 코드는 Input / Update / Render / Collision 중 어디인가?
- 이 값을 프레임당 이동량으로 두면 30 FPS와 120 FPS에서 무엇이 달라지는가?
- `onPause()` 때 센서와 게임 갱신을 멈추지 않으면 어떤 문제가 생기는가?
- 총알과 적의 화면 픽셀을 비교하지 않아도 충돌을 판단할 수 있는 이유는 무엇인가?
- 이 기능을 상용 엔진이 제공한다면 Android Native에서는 어느 클래스가 그 책임을 맡는가?

## 자주 생기는 오개념

- FPS는 게임 속도가 아니다. 이동량이 DeltaTime에 비례해야 시간 기준 속도가 된다.
- `onDrawFrame()`은 Android Main Thread에서 실행되는 일반 UI Callback이 아니다.
- Render가 위치를 바꾸지 않는다. Update가 상태를 바꾸고 Render가 읽는다.
- Collision은 독립된 마법 기능이 아니라 Update 중 수행되는 계산이다.
- Activity는 PC 프로그램의 `main()`과 완전히 같은 개념이 아니다. OS가 생명주기를 관리한다.
- 가속도계와 자이로스코프는 같은 값을 주지 않는다.

## 150분 회차 운영 예시

각 회차를 75분씩 나누고, 설명 20분 → 코드 추적 15분 → 실행 비교 20분 → 질문/미니 실습 20분의 리듬을 반복합니다. 그래픽스 파이프라인, 물리 엔진, ECS는 필요한 지점까지만 소개하고 구현 범위를 넓히지 않습니다.

## 코드 읽기 출발점

1. `MainActivity`
2. `GameSurfaceView`
3. `GameRenderer.onDrawFrame()`
4. `ShooterScene.update()`
5. `GameObject.update/draw`
6. `CollisionSystem`과 `Aabb.overlaps()`

이 순서로 읽으면 Android OS에서 GPU 출력까지의 연결을 한 방향으로 추적할 수 있습니다.
