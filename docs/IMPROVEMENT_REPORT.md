# Sample Project 개선 작업 보고서

## 기존 구조 분석과 재사용 판단

2026-09-07 작업 시작 시 `main`의 작업 트리는 깨끗했습니다. 기준 Commit은 `f142dd9`이며 기존 프로젝트를 재작성하지 않았습니다.

| 요구 영역 | 작업 전 상태 | 처리 |
|---|---|---|
| Game / Game Loop / DeltaTime | 이미 있습니다 | GLSurfaceView → GameRenderer → ShooterScene 유지 |
| GameObject / Player / Enemy / Bullet | 이미 있습니다 | 생성자와 기존 샘플 규칙 유지, 선택형 Component 연결 |
| Transform | Position/Size는 이미 있습니다 | 동일 Position을 공유하는 Transform에 Rotation/Scale 추가 |
| Texture Load / 캐시 / GL Thread | 이미 있습니다 | ResourceManager 재사용, 밀도 자동 확대 해제와 이름 조회 추가 |
| Sprite Sheet / UV / Animation | 이미 있습니다 | 기존 Catalog/Clip/Animation 재사용, 이름·표시 크기·교체 기능 확장 |
| Renderer의 Draw Interface | 이미 있습니다 | drawRect/drawSprite 유지, Rotation과 Pixel→NDC 변환 추가 |
| Touch / Sensor / InputSnapshot | 이미 있습니다 | 입력 구조 유지, Sensor Thread 설명 정정 |
| AABB | 이미 있습니다 | Aabb 수학 판정은 그대로 사용, 독립 Size/Offset을 가진 Component 추가 |
| 일정 속도 이동 | 이미 있습니다 | Enemy/Bullet 초기 속도 유지, 재사용 가능한 가속·감속 추가 |
| Sound / Channel | 없음 | SoundPool 기반 최소 기능 추가 |
| foundation/game 분리·AI 사용 원칙 | 이미 있습니다 | 유지하고 새 API에도 같은 경계 적용 |
| 터치 이동·자동 발사·적 생성·충돌 결과·점수 | 이미 있습니다 | 기존 제공 샘플 보존, 새 과제 정답은 추가하지 않음 |

기존 좌표는 NDC(-1~1)를 월드 좌표로 사용하는 방식입니다. Draw 호출은 이미 Renderer2D에 모여 있었고 Object 내부에는 OpenGL API 호출이 없었습니다. Texture는 onSurfaceCreated() 경로의 GL Thread에서 생성하며 이 규칙을 유지했습니다.

## 변경한 파일

아래 Kotlin 경로는 `app/src/main/java/kr/ac/lecture/mobilegame/` 기준입니다.

| 파일 | 변경 내용 |
|---|---|
| `foundation/core/GameObject.kt` | Vec2는 같은 파일에 유지, Transform과 선택형 Component 필드 및 기본 Update/Draw |
| `foundation/core/GameRenderer.kt` | Viewport 크기 전달, Sound 전달, GameClock reset을 GL Thread로 이동 |
| `foundation/core/GameSurfaceView.kt` | SoundManager 소유와 Lifecycle 연결 |
| `foundation/graphics/Renderer2D.kt` | 기존 Draw API에 Rotation 선택 인자, Viewport2D 추가 |
| `foundation/graphics/ResourceManager.kt` | Resource 이름 조회와 원본 Pixel Size 기준 유지 |
| `foundation/graphics/sprite/SpriteSheet.kt` | UV Inset과 별개로 원본 셀 크기를 전달 |
| `foundation/collision/CollisionSystem.kt` | active 및 Component 존재 여부를 검사한 뒤 Shape 판정 |
| `foundation/input/InputController.kt` | Callback/Thread 설명 정정, 입력 로직은 유지 |
| `game/Player.kt` | 기존 터치 추종 유지, Sprite/AABB 연결 |
| `game/Enemy.kt`, `game/Bullet.kt` | 기존 속도·방향 유지, Sprite/Movement/AABB 연결 |
| `game/Explosion.kt` | SpriteComponent 사용, 기존 수명 유지, 충돌 미부착 |
| `game/ShooterScene.kt` | Sound 전달과 학생 실습 TODO, 기존 게임 규칙 유지 |
| `game/sprite/SpriteClip.kt` | 기존 Frame/재생 설정을 SpriteAsset으로 전달하는 toAsset() |

문서는 `README.md`, `docs/DESIGN_ROADMAP.md`, `docs/LECTURE_NOTES.md`, `docs/SPRITE_SHEET_GUIDE.md`를 갱신했습니다. `.gitignore`에는 이번 빌드에서 생성된 로컬 Kotlin 캐시 `.kotlin/`을 추가했습니다. SDK/Gradle/JDK 설정, PNG 파일, Aabb.kt, DebugHud 및 Debug 파일은 변경하지 않았습니다.

## 새로 추가한 파일

같은 Kotlin 경로 기준으로 다음 여섯 파일을 추가했습니다. 단순 데이터 선언은 관련 구현 파일과 함께 뒀습니다.

- `foundation/component/SpriteComponent.kt`
- `foundation/component/MovementComponent.kt`
- `foundation/collision/CollisionComponent.kt` — Interface와 AABBCollisionComponent
- `foundation/graphics/sprite/SpriteAsset.kt` — Asset과 Loader
- `foundation/audio/SoundManager.kt` — SoundAsset과 Logical Channel 관리
- `foundation/audio/SoundPoolBackend.kt` — Android SoundPool 호출만 분리

문서는 `docs/COMPONENT_GUIDE.md`와 이 보고서를 추가했습니다. 파일 삭제·이동·병합은 하지 않았습니다.

## 구조와 호환성

GameObject는 Transform을 항상 가지며 Sprite/Movement/Collision은 선택 사항입니다. 위치는 복제하지 않습니다. 기반 시스템은 GameObject의 기본 update/draw를 제공하고 학생은 이동 입력, 상태 전환, 충돌 대상과 결과, 사운드 재생 시점을 정합니다. ECS, DI Framework, Reflection, 범용 Audio Engine은 추가하지 않았습니다.

SpriteComponent는 이름으로 Asset을 선택하고 각 Object의 Animation 상태를 보관합니다. Loader는 Pixel Size 또는 원본 대비 배율을 기본 표시 크기로 정하며 Texture 캐시는 유지합니다. Renderer는 실제 화면 크기를 통해 Pixel Size를 NDC로 바꾸고 Rotation을 적용합니다. 기존 샘플은 useObjectSize=true로 기존 크기를 유지합니다.

MovementComponent는 목표 속도에 Acceleration/Deceleration으로 접근하고 시간 구간을 적분합니다. Enemy/Bullet은 초기 Velocity도 기존 속도로 설정하므로 새 가속 효과를 강제로 추가하지 않았습니다. 기존 Player의 터치 추종을 Movement로 교체하는 것은 학생 실습입니다.

CollisionComponent는 감지 Interface이고 실제 구현은 AABB입니다. Sprite와 독립적인 Size/Offset을 사용하며 Transform Scale만 따라갑니다. 기존 모든 GameObject가 암묵적으로 충돌하던 방식과 달리 **새 Object는 CollisionComponent를 부착해야 CollisionSystem에 참여**합니다. 기존 Player/Enemy/Bullet에는 명시적으로 부착했습니다.

SoundManager는 기본 4개, 최대 8개 Logical Channel을 관리합니다. 동일 Channel의 중복 Register를 거부하고, Play/Pause/Stop/Unregister를 구분합니다. 비동기 Load 대기와 Lifecycle을 처리하고 Default×Channel×Master Volume을 현재 Stream에 적용합니다. Sound는 GL Resource가 아니며 GL Context가 없어도 관리할 수 있습니다.

구조도, 정확한 API 예제, Thread·좌표·크기 규칙은 [Component/Sound 가이드](COMPONENT_GUIDE.md)에 정리했습니다.

## 미구현 항목과 이유

- OBB/Circle, Physics Tick/물리 엔진: 필수 AABB 범위를 넘는 심화 항목. OBB용 교체 지점만 제공했습니다.
- 긴 BGM/Streaming 및 Backend 혼합: 단일 SoundPool 범위를 벗어납니다. Audio Focus도 별도 정책 실습으로 남겼습니다.
- 실제 음원·재생 이벤트·Player 상태 전환 조건: 과제에서 학생이 구현할 영역입니다.
- High Score/Restart/Leaderboard, 새 Spawn Pattern, 새 Score 규칙: 과제 정답을 미리 구현하지 않습니다.
- 고정 가상 해상도/Camera, GL teardown, Context 재생성 시 게임 진행 보존: 기존 후속 과제 범위를 유지했습니다.
- Object Pool 적용: 기존 기반 타입은 이미 있지만 실제 Bullet/Enemy 재사용은 후속 실습입니다.

## 빌드 및 실행 검증

사용자의 마지막 요청에 따라 확인 범위는 Debug APK 빌드까지입니다. 실행 테스트와 새 단위 테스트 실행은 하지 않았습니다. 빌드 성공은 화면·입력·충돌·오디오의 실제 정상 동작을 확인했다는 뜻이 아닙니다.

- 명령: `./gradlew assembleDebug --offline --console=plain`
- 빌드 조합: AGP 9.3.1 / Gradle 9.5.0 / compileSdk 37.0 / 앱 JVM Target 17
- Gradle Launcher: 시스템 JDK 26, 실제 Daemon/컴파일: 기존 저장소 기준의 JDK 25.0.3
- JDK 25가 로컬에 없어 Gradle이 기존 설정의 URL에서 자동 다운로드했습니다. 의존성의 offline 옵션이 Daemon JVM 다운로드까지 막지는 않았습니다.
- 직전 Debug 빌드: `BUILD SUCCESSFUL` (2026-09-07). APK: `app/build/outputs/apk/debug/app-debug.apk`.
- 이후 SoundManager/SoundPoolBackend의 불필요한 Interface 연결 제거와 CollisionSystem의 가독성 정리가 추가되었습니다. 이 최종 변경 후 재빌드는 실행 승인이 거절되어 수행하지 못했습니다. 따라서 위 APK는 해당 마지막 소스 정리 이전 결과이며, 최종 소스 전체의 빌드 성공을 보장하는 결과로 해석하면 안 됩니다.
- `git diff --check`는 최종 변경 상태에서 통과했습니다. 실제 기기 실행이나 오디오 테스트는 하지 않았습니다.

실제 확인은 [가이드의 수동 확인 목록](COMPONENT_GUIDE.md#학생-실습-경계와-직접-확인할-항목)을 사용하세요. AI가 생성·수정한 코드도 학생이 실행 흐름과 역할을 설명할 수 있어야 한다는 원칙은 유지합니다.
