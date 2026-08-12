# 모바일 게임 프로그래밍 강의용 베이스 프로젝트

대학 2학년이 **게임이 왜 이런 구조로 동작하는지** 직접 확인하는 Android Studio용 Kotlin + OpenGL ES 3.0 프로젝트입니다. Unity/Unreal 같은 상용 엔진 없이 Android의 생명주기, 입력, Game Loop, Update, Render, Collision의 연결을 코드로 드러내는 것이 목표입니다.

반복해서 필요한 초기 코드를 흔히 **보일러플레이트**라고도 부릅니다. 엄밀히 말하면 보일러플레이트는 기반 시스템 자체보다 그것을 구성하고 연결하는 반복 코드에 가깝기 때문에 두 용어가 완전히 같지는 않습니다. 다만 이 강의와 프로젝트에서는 처음 한 번만 이 차이를 소개하고, 이후 공통 기반 기능을 이해하기 쉬운 **기반 시스템**이라는 표현으로 통일합니다.

현재 결과물은 완성 게임이 아니라 드래곤플라이트/1945 스타일 세로 자동 슈팅으로 확장할 수 있는 **구조 초안**입니다. 색 사각형만으로도 터치 이동, 자동 발사, 적 생성, AABB 충돌, 점수와 GameOver 흐름을 실행할 수 있습니다.

## 핵심 구조

```text
Android OS
    └─ MainActivity / Lifecycle
        └─ GameSurfaceView
            ├─ Touch + Sensor → InputSnapshot
            └─ GLSurfaceView.Renderer (Render Thread)
                └─ Game Loop
                    ├─ Update(deltaTime)
                    │   ├─ Player / Enemy / Bullet
                    │   ├─ AABB Collision
                    │   └─ Game State / Score
                    └─ Render
                        └─ OpenGL ES → GPU
```

`onDrawFrame()`이 프레임마다 호출되고, `GameClock`이 `System.nanoTime()`으로 DeltaTime을 계산합니다. 상태 변경은 `update`, 화면 출력은 `draw`에서 수행합니다. Android Main Thread에 직접 무한 반복문을 만들지 않습니다.

## 기반 시스템과 게임 고유 로직

| 구역 | 패키지 | 소유/수정 원칙 | 주요 개념 |
|---|---|---|---|
| 기반 시스템 | `foundation.*` | 강사가 우선 제공, 지정 실습에서만 수정 | Game Loop, DeltaTime, Renderer, Input, Sensor, Collision, Scene, UI, Texture, Animation, ResourceManager, ObjectPool |
| 게임 고유 로직 | `game.*` | 학생이 주로 수정 | Player, Enemy, Bullet, 자동 발사, 생성 규칙, 점수, 난이도, GameOver |
| Android 연결 | `MainActivity` | 생명주기 실습에서 확인 | onCreate/onResume/onPause/onDestroy |

패키지의 경계는 접근 제한을 위한 보안 장치가 아니라 **책임을 읽는 연습**을 위한 장치입니다. 기반 시스템을 바꿀 때는 그 변경이 모든 게임 객체에 미치는 영향을 먼저 설명할 수 있어야 합니다.

## 패키지 구조

```text
kr.ac.lecture.mobilegame
├─ MainActivity.kt
├─ foundation/                 # 강사 제공 중심
│  ├─ core/                    # Game Loop, GameObject, DeltaTime, GLSurfaceView
│  ├─ graphics/                # OpenGL Renderer, Texture, Animation, ResourceManager
│  ├─ input/                   # Touch, 가속도계, 자이로스코프
│  ├─ collision/               # 실제 동작하는 AABB, CollisionSystem
│  ├─ scene/                   # Scene, SceneManager
│  └─ ui/                      # 점수/GameOver HUD 경계
└─ game/                       # 학생 수정 중심
   ├─ Player.kt
   ├─ Enemy.kt
   ├─ Bullet.kt
   └─ ShooterScene.kt
```

## 현재 구현 범위

- OpenGL ES 3.0 컨텍스트와 셰이더를 이용한 삼각형 2개짜리 색 사각형 렌더링
- 동일 셀 격자 Sprite Sheet의 자동 UV 분할과 Texture Sprite 렌더링
- `GLSurfaceView.Renderer`의 생성/크기 변경/프레임 콜백
- DeltaTime 기반 Player, Enemy, Bullet 이동
- UI Thread의 터치 입력을 불변 `InputSnapshot`으로 Render Thread에 전달
- 가속도계와 자이로스코프 등록/해제 및 입력 스냅샷 연결
- Activity의 resume/pause를 게임과 센서에 연결
- 경계가 닿는 경우를 충돌로 세지 않는 AABB 판정과 단위 테스트
- 자동 발사, 시간 기반 적 생성/난이도 초안, 점수, GameOver
- SpriteAnimation, SceneManager, ResourceManager, HUD, ObjectPool의 학습용 최소 구조
- Player 4×4, 탄환/폭발 4×4, Enemy 4×2 오리지널 투명 Sprite Sheet 샘플
- Player 피격/격추 4×2와 Enemy 피격/격추 4×4 별도 Texture 샘플

기본 장면은 제공된 Sprite Sheet를 `ResourceManager`로 읽어 Texture와 Animation 흐름을 보여 줍니다. 각 게임 객체에는 Texture가 없을 때 색 사각형으로 대신 그리는 경로도 남겨 두어 초기 렌더링 실습에 사용할 수 있습니다. OBB는 축 투영과 회전 행렬이 필요하므로 필수 범위에서 구현하지 않고 `CollisionSystem`의 선택 심화 TODO로 남겼습니다. 고정 Physics Tick, 멀티터치, 텍스트/폰트 UI, 오디오, 저장, NDK(C++)도 후속 확장 항목입니다.

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

현재 `main` 초안은 구조를 한눈에 보여주기 위해 여러 단계의 최소 구현을 함께 포함합니다. 실제 수업에서는 단계별 브랜치 또는 태그로 분리하는 것을 권장합니다.

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

값은 `app/build.gradle.kts`의 `buildConfigField`에서 변경한 뒤 다시 빌드합니다. `ShooterScene.updateSampleRules()`가 실제 Sample Block 사용 예입니다.

전역 디버그 함수는 `foundation.debug.DebugFunctions.kt`에 있습니다.

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
| `GameRenderer.onDrawFrame` | Update/렌더 루프 | Tick/렌더 루프 | 프레임 반복의 실제 진입점을 확인 |
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
6. 화면을 누르거나 드래그하면 청록색 Player가 이동하고 자동으로 탄환을 발사합니다.

명령행에서는 프로젝트 루트에서 다음 명령으로 확인할 수 있습니다.

```bash
# macOS / Linux
./gradlew test assembleDebug

# Windows
gradlew.bat test assembleDebug
```

macOS에서 `permission denied: ./gradlew`가 나오면 저장소에 실행 권한이 반영되어 있는지 확인합니다. 센서 실습은 실제 기기를 권장합니다.

현재 프로젝트 빌드 조합은 Android Gradle Plugin 9.3.0과 그 공식 기본 조합인 Gradle 9.5.0입니다. Gradle 9.5.0은 JDK 17부터 26까지 실행할 수 있으며, 이 강의의 현재 설치 기준은 JDK 26입니다. AGP 9부터 Kotlin 지원이 내장되므로 별도의 `org.jetbrains.kotlin.android` 플러그인을 적용하지 않습니다. Gradle 실행 JDK와 앱 코드의 Java/Kotlin JVM Target은 별개입니다. 앱 코드는 JVM Target 17로 컴파일된 뒤 Android Build Tools가 DEX로 변환하며, 테스트 기기의 최소 Android 버전은 JVM Target이 아니라 `minSdk`와 사용 API에 의해 결정됩니다. 학생은 Gradle 버전을 따로 설치하지 않고 저장소의 Gradle Wrapper를 사용합니다.

각 도구의 숫자가 가장 큰 버전을 무조건 섞는 것이 아니라, 최신 안정판 Android Studio가 공식 지원하는 AGP와 그 AGP가 요구하는 Gradle 조합을 사용합니다. 현재 Quail 3이 지원하는 AGP 상한은 9.3이므로 이 프로젝트도 AGP 9.3을 사용합니다.

## SDK 또는 테스트 기기 버전이 다를 때

먼저 다음 네 가지 버전을 구분합니다.

| 항목 | 의미 | 이 프로젝트 |
|---|---|---|
| `compileSdk` | 코드를 빌드할 때 사용할 Android API. 개발 PC에 해당 SDK Platform이 설치되어 있어야 함 | 37.0 |
| `targetSdk` | 앱이 테스트·대응한다고 선언한 API 수준. 해당 수준의 플랫폼 동작 변경을 적용하는 기준이며 테스트 기기의 최소 버전은 아님 | 37 |
| `minSdk` | Android OS 버전 관점에서 앱 설치를 허용할 최소 API 수준 | 24 |
| 테스트 기기 API | 실제 기기 또는 에뮬레이터가 실행하는 Android API 수준 | 24 이상 |

예를 들어 `targetSdk`가 37이어도 API 34 기기에서 실행할 수 있습니다. OS 버전 호환 범위의 하한은 `minSdk`이 결정합니다. 다만 기기 호환성은 API 수준만으로 끝나지 않습니다. 이 프로젝트는 Manifest에서 OpenGL ES 3.0을 필수로 선언하므로 **API 24 이상이면서 OpenGL ES 3.0을 지원하는 기기**가 필요합니다. 가속도계와 자이로스코프는 선택 기능으로 선언되어 없어도 기본 터치 게임은 실행할 수 있습니다.

`compileSdk`는 minor API를 포함할 수 있어 `37.0`으로 표기하지만, Android의 `minSdk`와 `targetSdk`는 정수 API 수준만 사용하므로 각각 `24`, `37`로 표기합니다. 서로 다른 표기는 의도된 것입니다.

### 상황별 대응

1. **Gradle Sync에서 `android-37.0`을 찾을 수 없다고 나오는 경우**
   - Android Studio의 **Tools → SDK Manager → SDK Platforms**에서 Android API 37.0을 설치합니다.
   - 수업 프로젝트에서는 각자 `compileSdk`를 낮추기보다, 강의에서 지정한 동일 SDK를 설치하는 방법을 우선합니다.

2. **SDK Build Tools가 없다는 오류가 나오는 경우**
   - **Tools → SDK Manager → SDK Tools → Show Package Details**에서 AGP 9.3의 공식 기본 버전인 Build Tools 36.0.0을 설치합니다.
   - AGP가 호환 버전을 자동 선택하므로 특별한 이유가 없다면 `buildToolsVersion`을 프로젝트에 직접 고정하지 않습니다.

3. **기기가 호환되지 않아 앱을 설치할 수 없는 경우**
   - 기기의 Android API가 `minSdk` 24 이상인지 확인합니다.
   - 기기가 OpenGL ES 3.0을 지원하는지도 확인합니다. 센서 유무는 설치 조건이 아닙니다.
   - 가장 안전한 해결책은 **Device Manager**에서 API 24 이상이며 OpenGL ES 3.0을 사용하는 에뮬레이터를 만들거나 조건을 만족하는 실제 기기를 사용하는 것입니다.
   - 오래된 기기를 반드시 사용해야 한다면 `app/build.gradle.kts`의 `minSdk`를 낮출 수 있지만, 사용 중인 모든 Android API가 그 버전에서도 동작하는지 코드와 실행 결과를 함께 확인해야 합니다.

4. **설치된 SDK 버전에 맞춰 프로젝트를 임시로 조정해야 하는 경우**
   - 우선 프로젝트 설정을 낮추지 말고 SDK Manager에서 프로젝트가 요구하는 SDK Platform을 설치합니다.
   - 구형 테스트 기기 때문에 `compileSdk`나 `targetSdk`를 낮출 필요는 없습니다. 기기 OS 지원 범위는 `minSdk`로 결정합니다.
   - 요구 SDK를 설치할 수 없는 특별한 경우에만 `app/build.gradle.kts`의 `compileSdk`를 조정합니다. `targetSdk`는 `compileSdk`보다 높을 수 없으므로 함께 낮춰야 할 수도 있지만, 이는 플랫폼 동작 정책을 바꾸는 변경이므로 단순한 로컬 설정 변경으로 취급하지 않습니다.
   - 변경 후 **Sync Project with Gradle Files**를 실행합니다.
   - SDK 기준 변경은 강사가 저장소에서 한 번 결정하고 전체 학생에게 동일하게 배포합니다. 개인별 임시 변경은 Git에 커밋하지 않습니다.

`local.properties`의 `sdk.dir`은 각 PC의 Android SDK **설치 경로**만 나타냅니다. SDK 버전을 맞추기 위해 이 파일을 공유하거나 Git에 커밋하지 않습니다.

버전 판단에는 다음 공식 문서를 기준으로 사용합니다.

- [Android Studio와 AGP 호환 범위](https://developer.android.com/studio/releases)
- [Android Gradle Plugin 9.3 호환 정보](https://developer.android.com/build/releases/agp-9-3-0-release-notes)
- [Gradle과 Java 실행 버전 호환표](https://docs.gradle.org/current/userguide/compatibility.html)
- [`minSdk`와 `targetSdk`의 정확한 의미](https://developer.android.com/guide/topics/manifest/uses-sdk-element)
- [OpenGL ES 등 기기 기능 요구사항](https://developer.android.com/guide/topics/manifest/uses-feature-element)

## 다음 확장 체크리스트

- [ ] PNG 스프라이트를 추가하고 `ResourceManager.texture()`와 UV 렌더링 연결
- [ ] `SpriteAnimation.currentFrame`을 스프라이트 시트 UV에 반영
- [ ] Title / Play / Result Scene 추가 및 전환
- [ ] 점수용 Bitmap Font 또는 Android Canvas overlay 구현
- [ ] 제거 대상 수집과 충돌 순회 비용 개선
- [ ] Bullet/Enemy를 `ObjectPool`로 재사용
- [ ] 고정 시간 간격 Physics Tick 선택 실습
- [ ] 난이도 전략을 Scene에서 별도 클래스로 분리
- [ ] 선택 과제로 Circle Collision 또는 OBB 연구
- [ ] 선택 과제로 NDK(C++) 모듈을 추가해 계산 코드 경계 비교

더 긴 리팩토링 시점, 좌표계, UI/사운드/저장, 디버그 기능과 학생 확장 아이디어는 `docs/DESIGN_ROADMAP.md`에 정리되어 있습니다.

Sprite Sheet에서 강사가 제공할 필수 부분과 학생 실습 부분의 정확한 경계, 셀 배치는 `docs/SPRITE_SHEET_GUIDE.md`를 참고합니다. 시트 전체 칸 수는 제한하지 않으며, **애니메이션 계열 하나만 최대 8프레임**으로 제한합니다.

## 설계 근거

이 저장소는 프로젝트 공용 `가이드라인.txt`, `source.txt`와 “모바일 게임 샘플 설계” 대화 초안을 바탕으로 만들었습니다. 완성도보다 구조 이해, API 암기보다 필요성, 복잡한 엔진 설계보다 읽을 수 있는 최소 코드를 우선합니다.
