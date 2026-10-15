# Android 게임 강의용 기반 프로젝트

Kotlin과 OpenGL ES 3.0으로 게임의 입력, 이동, 충돌, 그리기를 직접 연결하는 강의용 프로젝트입니다. 처음에는 `game` 폴더의 작은 슈팅 예제부터 읽어 보세요. 기반 시스템은 필요할 때 따라가며 확인하면 됩니다.

## 브랜치별 사용 범위

| 브랜치 | 용도 | 실행 화면 |
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

게임이 끝나면 이동과 충돌 계산은 멈추고, 이미 시작한 폭발만 끝까지 재생합니다.

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
