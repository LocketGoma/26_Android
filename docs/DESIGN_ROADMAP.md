# 전체 설계 초안과 리팩토링 로드맵

README의 실행 가능한 최소 뼈대 이후에 진행할 설계 항목을 빠짐없이 보존한 문서입니다. 처음부터 모든 관리자를 제공하지 않고, 반복과 책임 문제가 실제로 생긴 뒤 분리합니다.

## 최종 게임 규칙 후보

- 세로 화면, 하단 Player, 위에서 등장하는 Enemy, 자동 발사
- 터치/드래그 이동, 화면 경계 제한
- Player 체력, 피격, 짧은 무적 시간, 사망
- Player 탄환과 적 탄환, 공격력과 발사 주체
- 적 체력, 점수, 직선/좌우 흔들림/사인/추적 패턴
- 아이템, 중간 보스, 보스, 스테이지, 일시정지와 재시작
- 폭발/피격/비행 Animation, 배경 스크롤, 효과음과 배경 음악

## 단계별로 드러낼 구조

```text
Player가 직접 Bullet 생성
→ 생성 요청을 GameWorld가 처리
→ 반복 생성 비용을 측정
→ ObjectPool로 재사용

GameScene 내부 적 생성 타이머
→ EnemySpawner 분리
→ Difficulty 전략 분리
→ JSON/GameConfig로 수치 데이터 분리

리스트 반복 중 즉시 삭제 시도
→ active 플래그
→ 제거 예약 큐
→ 프레임 끝 일괄 제거
```

초기에는 적/탄환/이펙트 목록을 타입별로 유지합니다. 범용 객체 관리자나 ECS를 먼저 도입하지 않습니다. 적 패턴도 직선 이동으로 시작한 뒤 `StraightPattern`, `SinePattern`, `ChasePattern` 같은 전략으로 분리합니다.

## 좌표계와 입력

학생이 구분해야 할 좌표는 Android 터치 픽셀, 화면 픽셀, OpenGL NDC, 게임 월드, 텍스처 UV입니다. 현재 최소판은 NDC(-1~1)를 곧바로 월드 좌표로 사용합니다. 다음 리팩토링에서는 1080×1920 가상 해상도와 실제 화면 비율 보정을 도입합니다.

Component 확장판도 월드 좌표는 유지합니다. `Viewport2D`가 실제 화면 Pixel Size를 NDC 크기로 변환하고 화면 비율을 반영한 Sprite 회전을 담당합니다. 이는 고정 가상 해상도나 Orthographic Camera를 도입한 것과는 다릅니다.

입력은 다음 순서로 비교합니다.

1. 손가락 위치를 따라가는 절대 이동
2. 이전 터치와 현재 터치의 차이를 사용하는 상대 드래그
3. 멀티터치 상태
4. 가속도계 기울기를 속도로 변환
5. 자이로스코프 회전 입력을 선택 기능에 사용

입력 이벤트가 Player를 직접 바꾸지 않고 `InputController`가 상태를 저장한 뒤 Game Loop가 읽는 원칙은 유지합니다.

## 렌더링 성장 순서

```text
색 사각형
→ Vertex/Fragment Shader 분리
→ Vertex/Index Buffer 재사용
→ Texture + UV
→ 알파 블렌딩
→ Model/Projection Matrix
→ SpriteRenderer.draw(texture, position, size, rotation)
→ 렌더 순서와 배칭 관찰
```

Vertex Shader는 위치 변환과 UV 전달, Fragment Shader는 텍스처 샘플링과 알파/틴트를 담당합니다. 확장 효과는 피격 시 색상, 투명도, 페이드, 흑백, 간단한 발광과 화면 흔들림입니다. 학생이 셰이더를 직접 완성하지 않더라도 입력과 출력은 설명해야 합니다.

`ResourceManager`는 Bitmap 읽기, OpenGL Texture 생성, 리소스 ID별 캐시, 중복 로딩 방지, 해제와 OpenGL 컨텍스트 재생성 시 복원을 맡도록 성장시킵니다. 처음에는 TextureLoader 하나로 시작하고 리소스가 늘 때 관리자로 리팩토링하는 흐름도 실습 브랜치에서 비교할 수 있습니다.

## Collision과 객체 수명

필수 충돌 조합은 Player 탄환↔Enemy, Enemy↔Player, 적 탄환↔Player입니다. 감지와 결과 처리를 다음처럼 구분합니다.

```text
충돌 감지
→ 피해 적용
→ 제거 예약
→ 프레임 종료 시 실제 제거
```

AABB 이후 선택 항목은 원 충돌, 충돌 Layer/Mask, 공간 분할입니다. OBB는 회전 가능한 상자라는 개념과 AABB 차이까지만 필수로 다루고 직접 구현은 심화로 둡니다. 상용 엔진의 Collision Channel/Layer Mask와 연결해 설명합니다.

## UI, 저장, 사운드

UI 후보는 점수, 체력, 시작 문구, GameOver, 재시작, 일시정지, 최고 점수입니다. 초반에는 Android View/TextView를 OpenGL 위에 겹치는 혼합 방식을 비교하고, 값이 바뀔 때만 UI Thread에 전달합니다. 후반에는 Bitmap Font를 이용한 OpenGL HUD를 선택할 수 있습니다.

최고 점수는 `SharedPreferences`로 저장해 Android 시스템 기능과 게임 규칙의 경계를 학습합니다. 짧은 효과음은 `SoundPool`, 배경 음악은 `MediaPlayer` 후보이며 OpenGL 핵심 진도를 해치지 않도록 선택 기능으로 둡니다.

현재는 `SoundPool` 기반 `SoundManager`와 최대 8개 Logical Channel을 제공했습니다. 실제 효과음·재생 규칙·최고 점수 저장은 미구현 실습 영역입니다. 긴 BGM, 별도 Backend 혼합, Audio Focus 정책은 이번 범위에 포함하지 않았습니다. [Component/Sound 가이드](COMPONENT_GUIDE.md)를 참고하세요.

## 난이도와 데이터

- 시간/점수에 따른 생성 간격 감소
- Enemy 이동 속도와 체력 증가
- 새 Enemy와 적 탄환 등장
- 구간별 패턴과 보스 등장

먼저 `GameConfig` 상수로 숫자를 비교하고, 후속 단계에서 JSON 데이터로 분리합니다. 이는 Unity ScriptableObject, Unreal Data Asset/Data Table에 대응하는 축소 경험입니다.

## 모바일 생명주기 점검

- 홈/앱 전환/화면 잠금에서 update와 센서 정지
- 복귀 직후 GameClock 초기화로 큰 DeltaTime 차단
- OpenGL 컨텍스트가 다시 만들어질 때 Texture/Shader 복원
- 전화·알림 이후 게임 일시정지 화면 여부 결정
- `onDestroy()`만을 정상 종료처럼 가정하지 않기

## 디버그 기능 후보

- FPS, 객체 수, 좌표, 터치 위치 표시
- AABB 충돌 영역 표시
- Enemy 생성 정지, 무적 모드, 강제 GameOver
- 프레임별 로그 대신 상태 변화 시 로그

`DebugConfig`에서 시작하고 기능이 늘면 개발 빌드 전용 도구로 분리합니다.

## 학생별 확장 메뉴

- 기본: 그래픽, 세계관, 제목, 색, 배경, 사운드 교체
- 기능: 새 Enemy/아이템/무기/점수 규칙/난이도/보스
- 심화: 다중 발사, 유도탄, 스킬, 적 탄환, 스테이지, 데이터 파일, 파티클 흉내, 풀링

평가는 기능 수보다 Game Loop, DeltaTime, update/render 분리, 좌표 변환, 충돌, 객체 수명, Texture→GPU, Lifecycle을 본인 코드로 설명하는 능력을 우선합니다.
