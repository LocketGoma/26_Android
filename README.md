# Android 게임 강의용 기반 프로젝트

Kotlin과 OpenGL ES 3.0으로 게임의 입력, 이동, 충돌, 그리기를 직접 연결하는 강의용 프로젝트입니다. 처음에는 `game` 폴더의 작은 슈팅 예제부터 읽어 보세요. 기반 시스템은 필요할 때 따라가며 확인하면 됩니다.

## 브랜치별 사용 범위

`Sample` 브랜치는 앱 첫 화면에서 **세로 슈팅**과 **가로 플랫포머**를 고를 수 있습니다. 기존 슈팅 전체 화면은 `ShooterActivity`, 신규 플랫포머는 `PlatformerActivity`에 있으며 `MainActivity`는 게임 선택만 담당합니다. 플랫포머는 화면 세 개 분량의 점프 구역, 이동 발판으로 건너는 큰 구덩이, 적과 아이템 더미가 있는 피니시로 구성했습니다. [조작 방법과 코드 안내](docs/PLATFORMER_SAMPLE.md)를 참고하세요.

## 핵심 구조

```text
Android OS
    └─ MainActivity (게임 선택)
        ├─ ShooterActivity (기존 슈팅 전체 화면과 메뉴)
        │   └─ GameSurfaceView → SceneSurfaceView → GameRenderer → ShooterScene
        └─ PlatformerActivity (플랫포머 전체 화면과 메뉴)
            └─ SceneSurfaceView → GameRenderer → PlatformerScene

두 게임이 공유하는 Game Loop (GameRenderer)
    ├─ 입력 읽기
    ├─ Update(deltaTime): 이동, 충돌, 게임 상태, 점수
    └─ Render: OpenGL ES로 그리기
```

`onDrawFrame()`이 프레임마다 호출되고, `GameClock`이 `System.nanoTime()`으로 DeltaTime을 계산합니다. 상태 변경은 `update`, 화면 출력은 `draw`에서 수행합니다. Android Main Thread에 직접 무한 반복문을 만들지 않습니다.

## 기반 시스템과 게임 고유 로직

| 구역 | 패키지 | 소유/수정 원칙 | 주요 개념 |
|---|---|---|---|
| 기반 시스템 | `foundation.*` | 강사가 우선 제공, 지정 실습에서만 수정 | Game Loop, DeltaTime, Renderer, Input, Sensor, Collision, Scene, UI, Texture, Animation, ResourceManager, ObjectPool |
| 게임 고유 로직 | `game.*` | 학생이 주로 수정 | Player, Enemy, Bullet, 자동 발사, 생성 규칙, 점수, 난이도, GameOver |
| Android 연결 | `MainActivity`, `ShooterActivity`, `PlatformerActivity` | 화면 전환과 생명주기 실습에서 확인 | onCreate/onResume/onPause/onDestroy |

패키지의 경계는 접근 제한을 위한 보안 장치가 아니라 **책임을 읽는 연습**을 위한 장치입니다. 기반 시스템을 바꿀 때는 그 변경이 모든 게임 객체에 미치는 영향을 먼저 설명할 수 있어야 합니다.

## 패키지 구조

```text
kr.ac.lecture.mobilegame
├─ MainActivity.kt
├─ foundation/                 # 강사 제공 중심
│  ├─ core/                    # Game Loop, GameObject, DeltaTime, GLSurfaceView
│  ├─ component/               # 선택형 SpriteComponent, MovementComponent
│  ├─ audio/                   # SoundPool, Sound Asset, 최대 8개 Logical Channel
│  ├─ graphics/                # OpenGL Renderer, Texture, Animation, ResourceManager
│  ├─ input/                   # Touch, 가속도계, 자이로스코프
│  ├─ collision/               # Aabb, CollisionComponent, CollisionSystem
│  ├─ scene/                   # Scene, SceneManager
│  └─ ui/                      # 상태를 그리는 Hud<T>, 충돌 경계 DebugHud
└─ game/                       # 학생 수정 중심
   ├─ Player.kt
   ├─ Enemy.kt
   ├─ Bullet.kt
   └─ ShooterScene.kt
```

## 현재 구현 범위

- OpenGL ES 3.0 컨텍스트와 셰이더를 이용한 삼각형 2개짜리 색 사각형 렌더링
- 동일 셀 격자 Sprite Sheet의 자동 UV 분할과 Texture Sprite 렌더링
- `GLSurfaceView.Renderer`의 생성/크기 변경/Frame Callback
- DeltaTime 기반 Player, Enemy, Bullet 이동
- UI Thread의 터치 입력을 불변 `InputSnapshot`으로 Render Thread에 전달
- 가속도계와 자이로스코프 등록/해제 및 `InputSnapshot` 연결
- Activity의 resume/pause를 게임과 센서에 연결
- 경계가 닿는 경우를 충돌로 세지 않는 AABB 판정과 단위 테스트
- 자동 발사, 시간 기반 적 생성/난이도, 점수, GameOver
- SpriteAnimation, SceneManager, ResourceManager, HUD, ObjectPool의 학습용 최소 구조
- Player 4×4, 탄환/폭발 4×4, Enemy 4×2 오리지널 투명 Sprite Sheet 샘플
- Player 피격/격추 4×2와 Enemy 피격/격추 4×4 별도 Texture 샘플
- 회색 바닥의 체력·폭탄 빈 케이스, 푸른 항공폭탄, 숫자 0~9, HEALTH/BOMB/SCORE를 담은 정적 UI 5×4 Texture
- 체력 회복/폭탄 획득용 정적 필드 아이템 2×1 Texture와 별도 `ItemSpriteCatalog`

슈팅 장면은 제공된 Sprite Sheet를 `ResourceManager`로 읽어 Texture와 Animation 흐름을 보여 줍니다. 각 슈팅 객체에는 Texture가 없을 때 색 사각형으로 대신 그리는 경로도 남겨 두어 초기 렌더링 실습에 사용할 수 있습니다. 플랫포머는 별도 도트 시트를 사용하며 이동 버튼과 점프 버튼을 동시에 누를 수 있습니다. OBB는 필수 범위에서 구현하지 않고 `CollisionComponent`의 선택 심화 TODO로 남겼습니다. 고정 Physics Tick, GL 텍스트/폰트 UI, 긴 BGM 재생, 저장 기능 확장, NDK(C++)는 후속 실습 항목입니다.

### 선택형 Component와 Sound

- `GameObject.transform`: 기존 `position`을 공유하며 Rotation(degree)과 Scale을 추가합니다.
- `SpriteComponent`: 이름으로 Asset 교체, Object별 Animation, Runtime Scale을 제공합니다.
- `SpriteLoader`: Resource ID로 로드하며 이름 생략 시 확장자 없는 Resource 이름을 사용합니다. Pixel Size 또는 원본 대비 Scale로 기본 크기를 지정합니다.
- `MovementComponent`: Velocity, Max Speed, Acceleration, Deceleration을 제공합니다. 입력 방향이 일정한 구간을 시간 기준으로 적분하며 물리 엔진은 아닙니다.
- `AABBCollisionComponent`: Sprite와 독립적인 Size/Offset을 사용합니다. Component가 없는 객체는 `CollisionSystem` 판정에서 제외합니다. Aabb 파일은 독립적으로 유지합니다.
- `SoundManager`: 기본 4개·최대 8개 Logical Channel, Register/Play/Pause/Stop/Unregister, 3단계 Volume과 Lifecycle 연결을 제공합니다. 샘플의 피격·폭탄 합성음은 연결되어 있으며 효과음 파일은 학생이 추가합니다.

사용 예제, 좌표 단위, 호환 옵션과 한계는 [Component/Sound 가이드](docs/COMPONENT_GUIDE.md)에, 기존 기능 대조와 변경 내역은 [개선 작업 보고서](docs/IMPROVEMENT_REPORT.md)에 정리했습니다. 터치 이동·자동 발사·적 생성·점수 흐름은 유지합니다. 공통 루프와 게임별 연결을 분리한 내용, main 반영 목록은 같은 보고서에 정리했습니다.

## FPS, Tick, DeltaTime, Physics Tick

- **FPS**는 1초 동안 화면을 몇 번 출력했는지 나타냅니다.
- **Tick**은 게임 상태를 한 번 갱신하는 단위이며 FPS와 같을 수도 있지만 같은 개념은 아닙니다.
- **DeltaTime**은 이전 갱신 이후 흐른 시간입니다. 이 프로젝트의 이동 속도는 `speed * deltaTime`으로 계산합니다.
- **Physics Tick**은 물리 계산을 일정한 간격으로 수행하는 고정 갱신입니다. Unity의 `FixedUpdate`, Unreal의 Physics Tick/Substepping과 연결되는 개념이지만, 이 강의에서는 차이만 다루고 물리 엔진 자체는 구현하지 않습니다.

## 4회 이론 강의와 코드 연결

| 회차 | 핵심 질문 | 확인할 코드 | 미니 실습 |
|---|---|---|---|
| 1회차: Game Loop | 게임은 무엇을 반복하는가? | `GameRenderer.onDrawFrame`, `Scene.update/draw` | update와 draw에 로그/상태 표시 추가 |
| 2회차: FPS/Tick/DeltaTime | FPS가 달라도 속도가 같은 이유는? | `GameClock`, 각 객체의 `update(deltaTime)` | DeltaTime 곱셈 전후 이동 비교 |
| 3회차: Render/GameObject/Collision | 픽셀이 아닌 도형으로 어떻게 맞음을 판정하는가? | `Renderer2D`, `GameObject`, `Aabb`, `CollisionSystem` | 사각형 크기와 충돌 범위 변경 |
| 4회차: Android/Input/Sensor | OS 상태와 모바일 입력이 게임에 어떻게 연결되는가? | `MainActivity`, `GameSurfaceView`, `InputController`, `SensorInput` | 터치 이동을 기울기 이동으로 교체 |

설명 순서는 항상 **왜 필요한가 → 게임에서 어떻게 쓰는가 → 상용 엔진의 대응 개념 → Android Native 구현 → 이 프로젝트의 코드 위치**를 권장합니다.

## 권장 구현 순서

아래 번호는 브랜치나 실습 태그를 나눌 때 그대로 사용할 수 있습니다.

1. OpenGL 화면
2. 사각형
3. 텍스처
4. Player
5. 터치 이동
6. Game Loop / DeltaTime
7. Bullet
8. Enemy
9. Collision
10. Score / GameOver
11. Animation
12. Scene
13. ResourceManager
14. 오브젝트 제거 개선
15. Object Pool
16. Difficulty
17. 학생별 확장

`main`은 기반 시스템과 간단한 예제이고, 현재 `Sample`은 두 게임의 실행 예제입니다. 실제 수업에서는 단계별 브랜치 또는 태그로 분리하는 것을 권장합니다.

## 학생 수정 가능 영역

먼저 `game` 패키지에서 다음을 바꿉니다.

- Player 속도, 이동 제한, 터치 추종 방식
- Bullet 속도, 발사 간격, 다중 탄환
- Enemy 속도, 이동 패턴, 생성 위치
- `ShooterScene`의 점수, 충돌 결과, GameOver 및 난이도
- 자신만의 적/아이템/보스와 Scene

`foundation`은 강사가 지정한 실습에서 Renderer, 충돌 전략, 텍스처, 입력, Scene 전환, 풀링을 확장할 때 수정합니다. `TODO`를 검색하면 후속 과제 지점을 찾을 수 있습니다.

## AI 사용 원칙

AI 도구를 이용한 코드 생성, 설명, 오류 수정은 허용합니다. 다만 제출한 코드에 대해 학생 본인이 다음을 설명할 수 있어야 합니다.

- 이 코드는 Input, Update, Render, Collision 중 어디에 속하는가?
- 어떤 상태를 읽고 어떤 상태를 바꾸는가?
- DeltaTime을 왜 곱하는가?
- Android Lifecycle 및 어느 Thread와 연결되는가?
- AI가 제안한 변경이 기반 시스템과 게임 고유 로직 중 어디에 영향을 주는가?

설명하지 못하는 코드는 그대로 제출하지 말고, 더 작은 단위로 질문하고 실행 결과를 비교한 뒤 사용합니다.

## Sample Block과 Debug Block

Kotlin에는 C/C++의 `#if` 전처리기가 없습니다. 대신 `app/build.gradle.kts`에서 빌드 시 생성되는 두 개의 `static final` 상수를 정의했습니다. 실행 중에는 값을 바꿀 수 없습니다.

```kotlin
if (BuildConfig.SAMPLE_BLOCK) {
    // 강사가 제공한 완성 샘플
} else {
    // 학생 실습 코드 또는 TODO
}

if (BuildConfig.DEBUG_BLOCK) {
    // 디버그 전용 계산과 출력
}
```

- `SAMPLE_BLOCK`: 제공 샘플과 학생 실습 영역 구분. 기본값 `true`
- `DEBUG_BLOCK`: 디버그 함수와 출력 허용. Debug 빌드는 `true`, Release 빌드는 `false`

값은 `app/build.gradle.kts`의 `buildConfigField`에서 변경한 뒤 다시 빌드합니다. `ShooterScene.updateShootingRules()`가 실제 Sample Block 사용 예입니다.

현재 `SAMPLE_BLOCK=false`가 끄는 것은 `updateShootingRules()`의 자동 발사와 적 발사·생성 및 보스 생성입니다. Player의 터치 이동이나 충돌 처리 코드까지 제거하는 스위치는 아닙니다. 과제 배포 시 별도 실습 브랜치에서 제공 범위를 정하세요.

top-level Debug Function은 `foundation.debug.DebugFunctions.kt`에 있습니다.

```kotlin
debugPrint("Player position=$position")       // 화면 Toast + Logcat
debugPrint("frame=$frame", display = false)  // Logcat만
debugPrint(String.format("score=%d", score)) // printf 형식이 익숙할 때

val text = debugString(score)
val arrayText = debugString(enemyPositions)
```

Kotlin에서는 간단한 조합에 문자열 템플릿(`"score=$score"`)을 우선 사용하고, 자리표시자와 서식 지정이 필요할 때 `String.format()`을 사용합니다. `debugPrint()`는 어느 Thread에서 호출해도 화면 출력만 Main Thread로 전달합니다.

## Unity / Unreal 대응 개념

| 이 프로젝트 | Unity | Unreal | 직접 구현하는 이유 |
|---|---|---|---|
| `GameRenderer.onDrawFrame` | Update/Render Loop | Tick/Render Loop | Frame 반복의 실제 진입점을 확인 |
| `GameObject.update/draw` | MonoBehaviour | Actor/Component | 상태 갱신과 출력을 분리 |
| `SceneManager` | SceneManager | Level 전환 | 화면 상태의 수명 관리 학습 |
| `ResourceManager` | Resources/Addressables | Asset Manager | GPU 리소스 생성·해제 책임 확인 |
| `Aabb` | Collider2D | Collision Component | 충돌이 수학 계산임을 직접 확인 |

상용 엔진 비교는 API를 외우기 위한 것이 아니라 **엔진이 대신 처리하는 일을 Android Native에서 어디에 구현하는지** 찾기 위한 보조 지도입니다.

## 실행 방법

1. 최신 안정판 Android Studio(현재 Quail 3 / 2026.1.3)에서 이 폴더를 엽니다. AGP 9.3을 지원하는 최소 버전은 Quail 2 / 2026.1.2입니다.
2. 최신 안정판 JDK(현재 JDK 26), Android SDK Platform 37.0, SDK Build Tools 36.0.0과 최신 SDK Tools를 설치합니다. 다른 Build Tools 버전이 함께 설치되어 있어도 괜찮습니다. 프로젝트에 `buildToolsVersion`을 직접 고정하지 않으면 AGP가 호환 버전을 선택합니다.
3. Gradle Sync를 실행합니다.
4. OpenGL ES 3.0을 지원하는 에뮬레이터 또는 Android 7.0(API 24) 이상 기기를 선택합니다.
5. `app` 실행 구성을 실행합니다.
6. 첫 화면에서 슈팅을 고르고 시작하면 세로 화면에서 기존 터치 이동·자동 발사 게임을 플레이합니다. 플랫포머는 가로 전체 화면으로 열립니다. 게임 화면 위 왼쪽의 ◀·▶와 오른쪽의 ▲로 이동·점프하며, 적의 머리를 밟아 처리합니다.

명령행에서는 프로젝트 루트에서 다음 명령으로 확인할 수 있습니다.

```bash
# macOS / Linux
./gradlew test assembleDebug

# Windows
gradlew.bat test assembleDebug
```

macOS에서 `permission denied: ./gradlew`가 나오면 저장소에 실행 권한이 반영되어 있는지 확인합니다. 센서 실습은 실제 기기를 권장합니다.

현재 저장소의 빌드 조합은 Android Gradle Plugin 9.3.1 / Gradle 9.5.0입니다. AGP 9부터 Kotlin 지원이 내장되므로 별도의 `org.jetbrains.kotlin.android` 플러그인을 적용하지 않습니다. Gradle 실행 JDK와 앱 코드의 Java/Kotlin JVM Target은 별개입니다. 앱 코드는 JVM Target 17로 컴파일된 뒤 Android Build Tools가 DEX로 변환하며, 테스트 기기의 최소 Android 버전은 JVM Target이 아니라 `minSdk`와 사용 API에 의해 결정됩니다. 학생은 Gradle 버전을 따로 설치하지 않고 저장소의 Gradle Wrapper를 사용합니다.

JDK 26 설치 안내와 별개로, 현재 저장소의 `gradle/gradle-daemon-jvm.properties`에는 **Gradle Daemon용 JDK 25**가 지정되어 있습니다. 이 파일의 기준은 `JAVA_HOME`과 `org.gradle.java.home`보다 우선하며, 로컬에 맞는 JDK가 없으면 자동 다운로드가 발생할 수 있습니다. 이번 Component 개선에서는 기존 설정을 변경하지 않았고, 실제 빌드도 JDK 25로 수행했습니다. IDE 설정만 바꿨는데 실행 JDK가 달라지지 않으면 이 파일도 확인하세요. 강의용 JDK 기준을 통일할 때는 강사가 이 설정까지 함께 갱신해야 합니다. [Gradle Daemon JVM 기준](https://docs.gradle.org/9.5.0/userguide/gradle_daemon.html#sec:daemon_jvm_criteria)

각 도구의 숫자가 가장 큰 버전을 무조건 섞는 것이 아니라, 최신 안정판 Android Studio가 공식 지원하는 AGP와 그 AGP가 요구하는 Gradle 조합을 사용합니다. 현재 Quail 3이 지원하는 AGP 상한은 9.3이므로 이 프로젝트도 AGP 9.3을 사용합니다.

## SDK 또는 테스트 기기 버전이 다를 때

먼저 다음 네 가지 버전을 구분합니다.

| 항목 | 의미 | 이 프로젝트 |
|---|---|---|
| `main` | 기반 시스템을 읽고 확장하는 출발점 | 터치 이동·자동 발사·적 생성·점수·게임 종료가 있는 작은 슈팅 예제 |
| `Sample` | 완성된 동작을 비교하는 실행 예제 | 게임 선택 화면, 세로 슈팅과 가로 플랫포머 |

현재 문서는 `main` 기준입니다. 플랫포머 이동·발판·근접 판정의 기반 코드는 제공하지만, main의 실행 화면에 플랫포머 게임을 연결하지는 않았습니다.

## 먼저 실행하기

1. Android Studio에서 저장소 폴더를 엽니다.
2. SDK Manager에서 Android SDK Platform 37.0을 설치하고 Gradle Sync를 실행합니다.
3. Gradle 실행에 쓰는 JDK를 확인합니다. 저장소의 `gradle/gradle-daemon-jvm.properties`에는 JDK 25가 지정되어 있습니다. 앱 코드의 Java/Kotlin 대상 버전은 17입니다.
4. Android 7.0(API 24) 이상이면서 OpenGL ES 3.0을 지원하는 기기를 선택합니다.
5. `app`을 실행합니다. 화면을 누르거나 드래그하면 플레이어가 이동하고 탄환을 자동으로 발사합니다.

적을 맞히면 위쪽의 노란 점수 막대가 늘어납니다. 적과 플레이어가 부딪히면 붉은 종료 표시가 나타납니다. 숫자 점수 UI와 재시작 메뉴는 이 작은 예제에 포함하지 않았습니다.

| 설정 | 현재 저장소 값 | 확인할 파일 |
|---|---|---|
| Android Gradle Plugin | 9.3.1 | `build.gradle.kts` |
| Gradle | 9.5.0 | `gradle/wrapper/gradle-wrapper.properties` |
| compileSdk / targetSdk / minSdk | 37.0 / 37 / 24 | `app/build.gradle.kts` |
| Gradle용 JDK | 25 | `gradle/gradle-daemon-jvm.properties` |
| 앱 코드 대상 버전 | 17 | `app/build.gradle.kts` |
| 기본 화면 방향 | 세로(portrait) | `app/src/main/AndroidManifest.xml` |

SDK 설치 경로는 각 PC의 `local.properties`에서 정합니다. 이 파일은 Git에서 제외되어 있습니다. 도구 버전이나 화면 방향을 바꿀 때는 위 설정 파일부터 확인하세요.

## 코드를 읽는 순서

Kotlin 경로는 모두 `app/src/main/java/kr/ac/lecture/mobilegame/` 아래입니다.

| 순서 | 파일·함수 | 여기서 확인할 내용 |
|---|---|---|
| 1 | `MainActivity.onCreate()` | 어떤 Scene을 실행하고 어떤 그림·소리를 연결하는가 |
| 2 | `game/ShooterScene.update()` | 입력 → 생성 → 이동 → 충돌 → 제거 순서 |
| 3 | `game/Player.kt`, `Enemy.kt`, `Bullet.kt` | 각 물체가 자신의 위치와 상태를 어떻게 바꾸는가 |
| 4 | `foundation/collision/CollisionSystem.kt`, `Aabb.kt` | 두 물체가 겹쳤는지 어떻게 확인하는가 |
| 5 | `foundation/core/GameSurfaceView.kt`, `GameRenderer.kt` | Android 입력·생명주기가 게임 루프로 어떻게 이어지는가 |
| 6 | `foundation/graphics/Renderer2D.kt` | 계산된 위치와 그림을 OpenGL에 어떻게 전달하는가 |

```text
MainActivity: 사용할 게임과 그림·소리 구성
    ↓
GameSurfaceView: 터치·센서·화면 크기·일시정지 연결
    ↓
GameRenderer: 매 프레임 반복
    ├─ GameClock.tick(): 지난 프레임 이후 시간
    ├─ 입력 읽기 → Scene.update(): 게임 계산
    └─ 화면 지우기 → Scene.draw(): 그리기

ShooterScene.update()
    ├─ 플레이어 입력과 이동
    ├─ 탄환·적 생성
    ├─ 탄환·적 이동
    ├─ 효과 갱신
    ├─ 충돌 결과와 점수·게임 종료
    └─ 비활성 물체 제거
```

- [x] PNG 스프라이트와 `ResourceManager.texture()` 및 UV 렌더링 연결
- [x] `SpriteAnimation.currentFrame`을 스프라이트 시트 UV에 반영
- [x] 선택형 Component와 이름 기반 Sprite 교체, 크기/회전, 가속·감속 기반 제공
- [x] 효과음용 Sound/Channel 기반과 피격·폭탄 합성음 연결 (음원 파일 연결은 실습)
- [ ] Title / Play / Result Scene 추가 및 전환
- [ ] 점수용 Bitmap Font 또는 Android Canvas overlay 구현
- [x] 슈팅의 비활성 객체 정리·충돌 단계 구분, 그리기 버퍼 재사용
- [ ] Bullet/Enemy를 `ObjectPool`로 재사용
- [ ] 고정 시간 간격 Physics Tick 선택 실습
- [ ] 난이도 전략을 Scene에서 별도 클래스로 분리
- [ ] 선택 과제로 Circle Collision 또는 OBB 연구
- [ ] 선택 과제로 NDK(C++) 모듈을 추가해 계산 코드 경계 비교

## 무엇을 바꾸고 싶은가요?

| 바꿀 내용 | 먼저 볼 곳 | 변경 예 |
|---|---|---|
| 플레이어 이동 속도 | `game/GameConfig.PLAYER_SPEED` | 2.4 → 1.2로 바꾸고 이동 시간 비교 |
| 플레이어가 화면 끝에서 멈추는 위치 | `game/Player.update()` | `coerceIn` 범위 변경 |
| 발사 간격·탄속 | `game/GameConfig.kt` | `FIRE_INTERVAL_SECONDS`, `BULLET_SPEED` 변경 |
| 시간이 지날 때 적이 나오는 빈도·속도 | `game/GameConfig.kt` | `enemySpawnInterval()`, `enemySpeed()` 변경 |
| 적 생성 위치·종류 | `game/ShooterScene.updateShootingRules()` | 생성 x 범위나 사용할 그림 변경 |
| 피격·점수·게임 종료 | `game/ShooterScene.resolveCollisions()` | 겹친 뒤 무엇을 할지 변경 |
| 캐릭터·적·효과 그림 | `game/sprite/ShootingSpriteCatalog.kt` | 시트의 행·열, 재생 시간 변경 |
| 충돌 크기 | 각 객체의 `AABBCollisionComponent` 설정 | 그림보다 작은 충돌 상자 지정 |
| 점수·게임 종료 표시 | `foundation/ui/DebugHud.kt` | main의 간단한 표시 예제를 다른 HUD로 교체 |
| 시작할 게임 | `MainActivity.onCreate()` | `GameSurfaceView`에 전달하는 Scene 변경 |
| 화면 방향 | `AndroidManifest.xml` | `screenOrientation` 변경 후 입력·그림 비율 함께 확인 |

## 코드 작성 기준

### 익숙한 이름으로 역할을 드러내기

- 함수는 하는 일을, 변수는 보관하는 값을 나타냅니다. `resolveCollisions`는 충돌 결과 처리, `enemySpawnElapsed`는 적 생성 이후 누적 시간입니다.
- 단위가 헷갈리는 값에는 `Seconds`, `Pixels` 등을 붙입니다. `deltaTime`과 이 프로젝트의 발사 간격은 초 단위입니다.
- 반복해서 설명해야 하는 계산은 이름을 붙여 함수로 나눕니다. 적 생성 간격은 `GameConfig.enemySpawnInterval()`에서 읽을 수 있습니다.
- 주석은 코드가 필요한 이유와 바꿔 볼 곳을 설명합니다. 어려운 한자어나 불필요한 영어를 새로 만들지 않습니다. Android·OpenGL의 실제 API 이름은 그대로 사용합니다.
- 여러 동작을 한 줄에 몰아 쓰기보다 실행 순서가 보이게 작성합니다. 조건이나 설정이 많으면 이름 있는 인자로 전달합니다.

### 게임 규칙과 기반 기능 나누기

`game`은 “무엇이 일어나는가”를 정합니다. 적 생성, 공격, 피격, 점수와 게임 종료가 여기에 해당합니다.

`foundation`은 반복해서 쓰는 기능을 제공합니다. 입력 상태 읽기, 시간 계산, 이동 계산, 도형 겹침 확인, 그림과 소리 재생이 여기에 해당합니다. 기반 코드에서 ShooterScene 같은 특정 게임을 직접 만들지 않습니다.

새 게임을 연결할 때는 MainActivity의 이 부분을 바꿉니다.

```kotlin
gameView = GameSurfaceView(this) { resources ->
    val sprites = ShootingSpriteCatalog.load(resources)
    ShooterScene(sprites = sprites, sound = sound)
}
```

이 구성 코드는 MainActivity에서 작성하지만, 전달한 함수는 GL 스레드에서 호출됩니다. Texture를 만드는 Catalog 로딩도 이 함수 안에 둡니다.

main은 기본적으로 View 전체에 그립니다. 고정 비율이 필요하면 `GameSurfaceView(this, contentAspectRatio = 9f / 16f) { ... }`처럼 가로/세로 비율을 전달하세요. 그리는 영역과 터치 좌표 변환에 같은 비율이 적용됩니다. 도트 그림은 `nearestFiltering = true`로 설정할 수 있습니다.

### 계산은 update, 출력은 draw

입력에 따른 위치 변경과 충돌 결과는 `update()`에서 처리합니다. `draw()`는 계산이 끝난 상태를 읽어 그림만 출력합니다. 그림을 그릴 때 점수나 위치까지 바꾸면 프레임 순서를 설명하기 어려워집니다.

`GameObject.update()`는 붙어 있는 MovementComponent와 SpriteComponent를 갱신합니다. 이를 재정의한 객체는 `super.update(deltaTime)`를 호출합니다. 같은 위치에 수동 이동 코드와 MovementComponent를 동시에 적용하면 두 번 움직이므로 둘 중 하나를 사용합니다.

### 시간·좌표·충돌 크기 구분하기

- 이동량은 `속도 × deltaTime`입니다. 가속도는 속도가 변하는 빠르기이며 가속도 센서와 다른 개념입니다.
- 슈팅은 화면에 정규화한 좌표(NDC)를 사용합니다. 가운데는 (0, 0), 오른쪽은 +X, 위쪽은 +Y입니다.
- Aabb의 가로 범위는 `left..right`, 세로 범위는 `minY..maxY`입니다. 두 도형의 경계만 닿으면 겹침으로 세지 않습니다.
- Sprite의 표시 크기와 충돌 크기는 따로 정합니다. Transform의 scale은 둘 다 바꾸고 SpriteComponent의 scale은 그림만 바꿉니다. AABB는 그림의 회전을 따라 회전하지 않습니다.
- 플랫포머 기반은 게임에서 정한 길이 단위를 사용합니다. KinematicMotor에 NDC 속도 값을 그대로 섞지 마세요.

### 입력과 리소스는 알맞은 스레드에서 다루기

Android UI 스레드에서는 터치와 센서를 받아 입력 상태를 만듭니다. GL 스레드는 프레임마다 `InputSnapshot`을 읽어 게임을 계산합니다. UI 콜백에서 게임 객체의 목록을 직접 변경하지 않습니다.

그림을 GPU에 올리는 일은 GL 스레드의 ResourceManager가 담당합니다. 이미 읽은 그림은 캐시를 사용하며 프레임마다 Bitmap을 다시 읽지 않습니다. GL 컨텍스트가 다시 생기면 기존 Texture의 GPU 번호(handle)를 갱신하고 Scene의 진행 상태는 유지합니다. Activity나 앱 프로세스가 종료된 뒤의 게임 저장·복구는 별도로 구현해야 합니다.

SoundManager는 MainActivity에서 만들고 Scene에 전달합니다. 앱을 멈추면 재생을 멈추고, Activity가 종료될 때 해제합니다. 효과음 파일은 한 번 읽어 채널에 등록한 뒤 필요한 이벤트에서 재생합니다.

## 기반 기능 사용 예

### 등속 이동과 가속 이동

적처럼 처음부터 같은 속도로 움직이는 설정입니다.

```kotlin
enemy.movement = MovementComponent.constantVelocity(
    transform = enemy.transform,
    x = 0f,
    y = -0.45f,
)
```

가속·감속을 실험할 때는 MovementComponent를 만들고 입력 상태에 따라 방향을 지정합니다.

```kotlin
val movement = MovementComponent(player.transform,
    maxSpeed = 1f, acceleration = 3f, deceleration = 4f)
player.movement = movement

// 이 예제의 Player.update()에 있는 수동 이동은 제거한 뒤 사용합니다.
movement.setMoveDirection(1f, 0f) // 오른쪽으로 가속
// 손을 떼는 입력에서 setMoveDirection(0f, 0f)를 호출하면 감속
```

위 방향 설정은 입력이 바뀌는 지점에서 호출하는 예입니다. GameObject.update가 매 프레임 실제 이동을 계산합니다.

### 그림과 애니메이션

SpriteSheetLoader는 시트를 같은 크기의 칸으로 나누고, SpriteClip은 프레임과 재생 설정을 묶습니다. 실제 재생 상태는 객체별 SpriteComponent 안의 SpriteAnimation 하나가 가집니다.

`setSprite(name)`는 그림을 선택하고 `setSprite(name, restart = true)`는 처음부터 재생합니다. 반복하지 않는 애니메이션은 `isFinished`로 종료를 확인합니다. Explosion이 이 방식을 사용합니다.

`SpriteRegion.crop()`은 셀 안의 0..1 범위를 잘라 사용하고, `drawSprite(..., flipX = true)`는 좌우로 뒤집어 그립니다. 어느 경우에도 원본 PNG나 충돌 크기를 바꾸지 않습니다. 한 애니메이션은 1~8프레임이고 시트 전체 칸 수에는 이 제한이 없습니다.

`drawRect`와 `drawSprite`에는 일반 RGBA 색을 전달합니다. 알파를 RGB에 미리 곱하지 않아도 렌더러가 처리합니다.

### 선택 실습으로 제공하는 코드

| 코드 | 연습할 기능 | main 슈팅에서 사용 여부 |
|---|---|---|
| `DigitalInput` | 버튼 유지와 점프·공격 탭을 프레임에 전달 | 미사용 |
| `KinematicMotor` | 가속·감속·중력·지형 충돌·이동 발판 따라가기 | 미사용 |
| `MeleeAttack` | 짧은 공격 범위, 유효 시간, 같은 대상의 중복 피격 방지 | 미사용 |
| `ObjectPool` | 매번 새로 만들지 않고 물체 재사용 | 미사용 |
| `SensorInput` | 가속도계·자이로 값을 InputSnapshot에 전달 | 수집만 연결, 플레이어 이동은 터치 사용 |

MeleeAttack과 Bullet은 별개의 처리입니다. 근접 공격은 잠시 활성화되는 범위를 확인하고, 탄환은 생성된 뒤 이동하다가 충돌하거나 화면 밖으로 나가면 비활성화됩니다. 현재 main의 화면 터치에는 별도 근접 공격을 연결하지 않았습니다.

KinematicMotor는 발판 이동량 적용 → 가로 이동·충돌 → 중력과 세로 충돌 순서로 읽습니다. 이를 사용할 게임에서는 발판의 새 위치와 이동량을 먼저 계산해야 합니다. 고속 이동의 지형 통과, 경사면, 발판 사이에 끼이는 상황은 추가 구현이 필요합니다.

## 샘플·디버그 설정

`app/build.gradle.kts`의 `buildConfigField`에서 정한 뒤 다시 빌드합니다.

| 설정 | 하는 일 |
|---|---|
| `SAMPLE_BLOCK` | true이면 자동 발사와 적 생성 규칙 실행. false여도 터치 이동과 충돌 처리 틀은 유지 |
| `DEBUG_BLOCK` | debugPrint 출력 허용. Debug 빌드는 true, Release는 false |

`DebugConfig`의 세부 상수는 확장 실습용 설정 예입니다. main의 실행 코드에는 FPS 표시·무적 모드·충돌 선을 연결하지 않았습니다. 값을 바꾸는 것만으로 기능이 생기는 설정은 아닙니다. main의 점수 막대와 종료 표시는 플레이 상태를 보여 주는 기본 HUD이므로 DEBUG_BLOCK과 별도로 표시합니다.

```kotlin
debugPrint("score=$score", display = false) // Logcat
debugPrint("Game Over")                    // Logcat과 짧은 화면 알림
```

## 수정 후 확인하기

먼저 직접 실행해 바꾼 부분과 관련된 동작을 확인합니다. 터치 이동·자동 발사·충돌·점수, 앱을 나갔다 돌아왔을 때의 입력과 그림을 함께 확인하세요. 화면 크기나 방향을 바꿨다면 그림·입력 위치·충돌 영역을 같은 기준으로 확인합니다.

기존 단위 테스트와 APK 빌드는 프로젝트 루트에서 실행할 수 있습니다.

```powershell
.\gradlew.bat test assembleDebug
```

macOS/Linux에서는 `./gradlew test assembleDebug`를 사용합니다. 이번 main 반영에서는 빌드·테스트 실행·기기 실행을 하지 않았습니다. 소스의 참조 검사만으로 실제 실행 결과를 확인했다고 보지는 않습니다.

AI 도구를 사용해도 좋습니다. 다만 수정한 코드가 입력·계산·충돌·그리기 중 어디에 해당하고, 어떤 값을 읽고 바꾸는지 직접 설명할 수 있도록 작은 단위로 바꾸고 결과를 비교하세요.
