# 선택형 Component와 Sound 사용 가이드

`foundation`은 기능을 제공하고 `game`은 사용 시점과 게임 규칙을 결정합니다. Component를 찾는 Reflection, ECS, 범용 Component 목록은 없습니다. 필요한 세 필드만 붙입니다. 정의를 가리키는 이름은 영어로, 동작 설명은 자연스러운 한국어로 씁니다.

```text
MainActivity / GameSurfaceView
├─ Input → GameRenderer → ShooterScene
│                       └─ GameObject
│                          ├─ Transform (Position / Rotation / Scale)
│                          ├─ SpriteComponent? → Renderer2D → OpenGL ES → GPU
│                          ├─ MovementComponent?
│                          └─ CollisionComponent? → AABBCollisionComponent
└─ SoundManager → SoundPool
   ├─ Channel 0
   ├─ Channel 1
   ├─ Channel 2
   └─ Channel 3 (설정에 따라 최대 8개)
```

Unity의 Component, Unreal의 Actor/Component와 역할을 비교할 수 있지만 해당 엔진의 기능이나 수명 규칙을 그대로 구현한 것은 아닙니다.

## GameObject와 호출 순서

`position`과 `transform.position`은 동일한 Vec2입니다. `size`는 기존 NDC 표시 크기이며 `transform.rotation`은 degree, `transform.scale`은 X/Y 배율입니다. 위치를 복사해서 따로 갱신하지 않습니다.

Scene은 기존처럼 Input → Object Update → Collision → 점수/제거 등 Game State 변경 → Render 순서로 실행합니다. `GameObject.update()`는 부착된 Movement와 Sprite Animation을 갱신하며, `draw()`는 SpriteComponent에 그리기를 요청합니다. Override한 메서드에서는 필요한 `super.update()`/`super.draw()`를 한 번 호출하세요. 기존 Player는 터치 추종 코드를 유지하므로 새 Movement를 붙일 때 기존 위치 변경을 함께 실행하지 않도록 교체해야 합니다.

Enemy/Bullet은 기존과 같은 방향·초기 속도로 MovementComponent를 사용합니다. Player/Enemy/Bullet은 AABB를 명시적으로 부착합니다. Explosion은 Sprite만 사용하고 기존 수명 타이머를 유지합니다. Component는 Game Loop의 GL Thread에서 변경하세요. UI 입력은 기존 InputSnapshot 경로를 사용합니다.

## Sprite의 이름과 크기

이미 로드된 Texture/Frame을 공유하고 Animation 상태만 Object별로 만듭니다. `SpriteAsset`은 이름, 1~8개 Frame, 기본 Pixel Size와 재생 설정을 가집니다. 시트 전체 셀 개수에는 이 제한을 적용하지 않습니다.

아래는 GL Thread에서 사용할 구성 예입니다. `resourceId`에는 프로젝트에 추가한 **단일 Sprite 이미지**의 `R.drawable` ID를 전달하세요. 기존 시트 전체를 단일 이미지처럼 로드하면 전체 시트가 그려집니다.

```kotlin
fun makeSpriteObject(resources: ResourceManager, resourceId: Int): GameObject {
    val loader = SpriteLoader(resources)
    val idle = loader.loadSprite(resourceId, name = "Idle", width = 128f, height = 128f)
    val objectWithSprite = object : GameObject(Vec2(), Vec2(0.2f, 0.2f)) {}
    objectWithSprite.sprite = SpriteComponent(objectWithSprite).apply {
        addSprite(idle)
        setSprite("Idle")
        setScale(0.5f)
    }
    return objectWithSprite
}
```

대안으로 `loader.loadSprite(resourceId, scale = 0.5f)`를 사용하면 원본의 50%를 기본 표시 크기로 정합니다. 이름 생략 시 Android Resource entry name(파일 확장자가 없는 이름)을 사용합니다. Pixel Size의 width/height는 함께 지정하고, Pixel Size와 Load Scale은 동시에 지정하지 않습니다. 크기는 양의 유한값이어야 합니다. 잘못된 로드 설정은 개발 오류로 예외를 내므로 GL Thread에서 유효한 Drawable ID를 전달하세요.

| 크기 단계 | 예 | 의미 |
|---|---|---|
| Original Texture | 512×512 | GPU Texture 크기, 캐시 재사용 |
| Load Scale | 0.5 → 256×256 | Asset의 기본 Pixel Size |
| Runtime Sprite Scale | 0.5 → 128×128 | 그림에만 적용 |
| Transform Scale | (2, 2) → 256×256 | 그림과 부착된 AABB에 적용 |

`Renderer2D.pixelSizeToWorld()`가 실제 Surface 크기를 사용해 Pixel Size를 NDC 크기로 변환합니다. 고정 가상 해상도나 화면 비율에 따른 Letterbox는 구현하지 않았습니다. 화면이 달라지면 같은 Pixel Size가 차지하는 화면 비율도 달라집니다. 회전은 화면 비율을 반영하므로 직사각형 화면에서도 그림의 형태를 유지합니다.

기존 샘플은 `SpriteComponent(owner, useObjectSize = true)`를 사용하여 Asset의 Pixel Size 대신 기존 `owner.size`를 사용합니다. 이 옵션에서는 Load Size가 표시 크기를 바꾸지 않습니다. Runtime Scale과 Rotation은 적용됩니다.

시트의 기존 Clip은 `clip.toAsset("Damage")`로 변환해 등록하세요. `addSprite()`는 같은 이름의 중복 등록 시 false, `setSprite()`는 없는 이름에 false를 반환하며 현재 Sprite를 유지합니다. 같은 이름을 매 프레임 선택해도 재생이 초기화되지 않습니다. `setSprite("Damage", restart = true)`는 명시적으로 재시작합니다. 상태 전환 조건은 학생 실습입니다.

Texture 생성·삭제는 유효한 GL Context가 있는 GL Thread에서만 수행합니다. 기존 `GameRenderer.onSurfaceCreated()` → ResourceManager/Catalog 로딩 경로를 유지했습니다. Context가 재생성되면 Texture와 Scene도 다시 생성되며 게임 진행 상태를 보존하는 기능은 아직 없습니다. 명시적인 GL teardown은 기존 TODO로 남아 있습니다.

## Movement

```kotlin
val movement = MovementComponent(objectWithSprite.transform,
    maxSpeed = 1f, acceleration = 3f, deceleration = 4f)
objectWithSprite.movement = movement
movement.setMoveDirection(1f, 0f) // 입력이 있을 때 목표 방향
movement.setMoveDirection(0f, 0f) // 입력이 끝나면 감속
// Scene이 objectWithSprite.update(deltaTime)를 호출합니다.
```

두 입력 줄은 API 사용 예이며 실제로는 각 입력 상태에서 하나만 호출합니다. 길이 1보다 큰 방향은 정규화하고, 작은 입력의 세기는 유지합니다. Velocity/Max Speed는 NDC 월드 단위/초, Acceleration/Deceleration은 월드 단위/초²입니다. 입력이 유지되므로 입력 종료 시 반드시 (0, 0)을 전달하세요. 목표 방향은 Rotation과 독립적입니다.

한 프레임 동안 목표 속도와 가속도가 일정하다고 가정합니다. 목표 속도까지 접근하는 구간은 평균 속도×시간으로, 도달 후는 등속 이동으로 계산합니다. 동일한 시간·입력 조건에서 FPS에 따른 차이를 줄이지만 입력 샘플링 시점이나 GameClock의 DeltaTime 제한까지 없애는 것은 아닙니다. Acceleration/Deceleration=0이면 해당 상황에서 속도가 변하지 않습니다. Physics Tick이나 힘·질량·물리 충돌 응답은 구현하지 않습니다.

## Collision

```kotlin
objectWithSprite.collision = AABBCollisionComponent(
    objectWithSprite.transform, size = Vec2(0.12f, 0.08f), offset = Vec2(0f, -0.02f))
val hit = CollisionSystem.intersects(first, second)
```

Size/Offset은 NDC 월드 단위이며 그림보다 작은 Hitbox를 만들 수 있습니다. `size.copy()`로 부착된 기존 샘플의 AABB는 이후 그림의 `GameObject.size` 변경을 자동으로 따라가지 않습니다. 충돌 크기도 변경하려면 AABBCollisionComponent의 size를 별도로 수정하세요.

Transform Scale은 충돌 크기와 Offset에 적용합니다. SpriteComponent Scale은 충돌에 영향을 주지 않습니다. AABB는 축 정렬을 유지하며 Rotation을 무시합니다. 회전된 그림을 정확히 감싸거나 OBB처럼 판정하는 기능이 아닙니다. 경계만 닿으면 충돌이 아닙니다.

`CollisionSystem`은 active와 양쪽 Component 유무도 검사합니다. `component.intersects(other)` 직접 호출은 기하학적 판정만 하므로 active 검사를 대신하지 않습니다. 기존 `GameObject.bounds`는 Component가 없을 때 size 기반 상자를 반환하는 보조 API이지만, 이것만으로 충돌 대상이 되지는 않습니다.

OBB는 CollisionComponent를 구현해 교체할 수 있도록 TODO만 남겼습니다. OBB↔OBB뿐 아니라 AABB↔OBB 판정도 구현해야 하며, AABB bounds 중첩만으로 OBB 충돌을 확정하면 안 됩니다.

## Sound와 Logical Channel

GameSurfaceView가 SoundManager를 생성하고 ShooterScene에 전달합니다. Scene의 기존 `sound`를 사용하세요. onResume/onPause/onDestroy는 재생 재개·정지 상태·해제를 연결합니다. 샘플에는 음원 파일과 자동 재생 이벤트를 추가하지 않았습니다.

```kotlin
// res/raw/shot.wav를 직접 추가한 뒤 ShooterScene 등에서 한 번 구성합니다.
val shot = sound?.loadSound(R.raw.shot, volume = 0.8f, loop = false)
sound?.register(1, shot)
sound?.setChannelVolume(1, 0.5f)
sound?.setMasterVolume(0.5f) // 최종 0.8 × 0.5 × 0.5 = 0.2
// 아래 호출은 해당 게임 이벤트에서 각각 사용합니다.
sound?.play(1)
sound?.pause(1)
sound?.play(1)       // Pause된 Stream을 이어서 재생
sound?.stop(1)       // 등록 유지. 다음 Play는 처음부터
sound?.unregister(1) // 등록 제거. 교체하려면 이후 Register
```

- 기본 ChannelCount는 4, 생성 시 1~8 범위만 허용합니다. Channel ID는 0부터 ChannelCount-1까지입니다. ChannelCount 설정 오류는 예외이며, 호출 중 잘못된 Channel ID/미등록 Sound는 Logcat Warning 후 무시합니다.
- Load는 Resource ID 방식입니다. 이름 생략 시 확장자 없는 Resource 이름, 지정 시 해당 이름을 Asset에 보관합니다. 같은 Resource의 Sample은 중복 디코딩하지 않습니다. 실패한 리소스 로드는 null과 Warning을 반환합니다.
- Register는 이미 찬 Channel을 덮어쓰지 않고 false와 Warning을 반환합니다. 다른 Manager의 Asset이나 null도 거부합니다.
- Load 완료 전 Play는 최대 한 번 대기합니다. Pause/Stop/Unregister는 대기 재생도 취소합니다. 앱이 background면 Load가 완료돼도 소리를 내지 않습니다.
- Play는 기존 Stream을 먼저 멈추고 재시작하므로 Channel당 최대 하나만 재생합니다. Pause 후에는 같은 Stream을 Resume합니다. 수동 Pause는 앱 복귀로 자동 해제되지 않습니다.
- 모든 Volume은 0~1로 제한하고 NaN은 0으로 처리합니다. Master/Channel 변경은 현재 Stream에도 즉시 전달합니다. 이 값은 시스템 미디어 음량을 변경하지 않습니다.
- Unregister는 Playback을 해제하되 공유 Sample 캐시는 Manager.release()까지 유지합니다. release() 후 호출은 Warning으로 무시합니다.

SoundPool은 짧은 효과음용입니다. 디코딩된 Sound 하나는 약 1 MB로 제한되며 초과분이 잘릴 수 있습니다. 긴 BGM/Streaming, Audio Focus 정책, 자연 재생 종료 Callback은 제공하지 않습니다. 이미 끝났거나 시스템이 제거한 Stream에 Pause/Resume을 호출해도 재생 위치를 복구할 수 없습니다. SoundPool은 짧은 Loop를 지원하지만 긴 BGM 지원을 뜻하지 않습니다. [Android SoundPool 공식 문서](https://developer.android.com/reference/android/media/SoundPool)

## 학생 실습 경계와 직접 확인할 항목

새로운 터치 추종 정답, Spawn Pattern, 충돌 결과, Score, High Score, Restart, Leaderboard를 구현하지 않았습니다. 기존 제공 샘플만 유지했습니다. AI 사용은 허용하되 학생이 Input/Update/Collision/State/Render와 Thread 및 Resource 수명을 본인 코드로 설명해야 합니다.

이번 확인 범위는 APK 빌드까지입니다. 실제 실행은 사용자가 진행합니다.

- 기존 Player/Enemy/Bullet/폭발 화면, 터치 이동, 충돌/점수 흐름
- 이름 전환·재시작 여부, Load Size 두 방식, Runtime Scale, 비정사각 화면에서 Rotation
- 일정한 입력 시간을 30/60 FPS 조건으로 주었을 때 가속·감속과 총 이동 거리 비교
- 작은 AABB/Offset, 경계 접촉, active=false와 Component 미부착 상태
- 짧은 음원을 추가한 뒤 Load/Register/Play/Pause/Stop/Unregister, 중복 등록과 잘못된 ID
- Default/Channel/Master Volume 곱셈, 재생 중 음량 변경, 홈 이동·복귀·종료
