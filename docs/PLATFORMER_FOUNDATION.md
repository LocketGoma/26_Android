# 플랫포머와 근접공격 기반 코드

`main`에서 제공한 공통 기능은 아래와 같습니다. `Sample`은 이동·발판·충돌 기능을 사용합니다. 현재 플랫포머는 점프와 머리 밟기로 진행하며 `MeleeAttack`은 사용하지 않습니다. 실행 방법과 맵 구성은 [플랫포머 샘플 안내](PLATFORMER_SAMPLE.md)에 있습니다.

| 코드 (`app/src/main/java/kr/ac/lecture/mobilegame/` 기준) | 역할 |
|---|---|
| `foundation/graphics/FitViewport.kt` | 화면 비율 유지, 남는 여백 계산, 터치 위치를 게임 좌표로 변환 |
| `foundation/input/DigitalInput.kt` | 이동 버튼을 함께 누르기, 점프·근접공격 입력을 한 번씩 전달하기 |
| `foundation/physics/KinematicMotor.kt` | 가속·감속, 최대 속도, 중력, 지형 충돌, 움직이는 발판 따라가기 |
| `foundation/collision/MeleeAttack.kt` | 전방 공격 범위, 공격이 유효한 시간, 다음 공격까지의 대기 시간, 한 공격에서 같은 적을 여러 번 때리지 않도록 처리 |

## 함수 호출 순서

1. UI Thread에서 DigitalInput.set(action, pressed)를 호출합니다.
2. GL Thread에서 drain()으로 입력을 한 번 읽습니다. 짧은 탭도 다음 프레임까지 남습니다.
3. 물리 계산을 할 때는 모든 PlatformSurface.moveTo()를 먼저 호출합니다. 움직이지 않는 발판도 현재 위치로 호출하면 이동량이 0이 됩니다.
4. KinematicMotor.step()은 캐릭터가 이전에 어느 발판 위에 서 있었는지 확인합니다. 점프하면 그 발판을 떠나고, 계속 서 있으면 발판의 이동량만큼 캐릭터도 옮깁니다.
5. 캐릭터의 가속·이동, 가로 방향 충돌, 중력·세로 방향 충돌을 계산합니다. 새로 서 있는 발판의 번호를 supportId에 기록합니다.
6. MeleeAttack의 남은 시간을 갱신합니다. 공격 입력이 있으면 start()를 호출하고, bounds()/tryHit()으로 적에게 맞았는지 확인합니다. 공격 중 방향을 고정할지, 적의 체력을 얼마나 줄일지는 각 게임에서 정합니다.
7. Render는 계산이 끝난 위치에 그림을 그립니다. Sprite 크기를 바꾸어도 PlatformBody의 충돌 크기는 따로 유지합니다.

게임 좌표에서 +Y는 위쪽입니다. Aabb.top은 작은 Y 값(minY)을 뜻하지만, PlatformSurface.top은 발판의 윗면인 큰 Y 값(maxY)을 뜻하므로 구분해야 합니다. KinematicMotor는 짧은 시간 간격으로 나누어 호출해야 합니다. 예를 들어 한 번에 최대 1/120초씩 계산할 수 있습니다. 빠른 물체가 지형을 통과하는 문제, 발판 사이에 끼이는 상황, 경사면 이동은 추가 구현이 필요합니다. 발판에서 내릴 때 발판의 속도를 캐릭터에게 더하는 동작은 구현하지 않았습니다.

슈팅의 자유로운 이동과 플랫포머의 발판·중력·지형 충돌을 각각 읽기 쉽도록 기존 `MovementComponent`와 나누어 작성했습니다. `MeleeAttack`은 짧은 시간 동안 공격 범위를 확인합니다. 기존 `Bullet`은 탄환을 만들고 이동시킨 뒤 충돌하거나 화면 밖으로 나가면 없앱니다.
