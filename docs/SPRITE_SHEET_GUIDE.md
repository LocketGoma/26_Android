# Sprite Sheet 로더와 실습 경계

## 규칙

- 한 이미지에는 동일 크기의 셀이 격자로 배치됩니다.
- 시트 전체 Sprite 개수는 제한하지 않습니다.
- 하나의 Animation 계열은 학생이 추적하기 쉽도록 1~8프레임으로 제한합니다.
- Bitmap을 셀마다 복사하지 않고, Texture 한 장과 UV 좌표만 공유합니다.

## 강사가 제공해야 하는 필수 기반 시스템

| 코드 | 책임 | 학생에게 제공하는 이유 |
|---|---|---|
| `ResourceManager.texture()` | Bitmap을 GPU Texture로 한 번 업로드하고 캐시/해제 | OpenGL 리소스 수명 오류가 실습 핵심을 가리지 않게 함 |
| `Texture` | GPU handle과 원본 크기 보관 | 반쪽 texel 계산에 필요 |
| `SpriteSheetLoader` | ResourceManager와 격자 정보를 연결 | GL Thread에서만 로딩하게 유도 |
| `SpriteSheet.region()` | 행·열 검사와 UV 계산 | Y축, 범위 오류, Texture Bleeding을 안전하게 처리 |
| `SpriteRegion` | Texture와 한 셀의 UV 영역 | Bitmap 복사 없이 Sprite를 객체로 전달 |
| `Renderer2D.drawSprite()` | Position/UV를 셰이더로 전달하고 알파 블렌딩 | 학생이 매 객체마다 GL 호출을 복제하지 않게 함 |

필수 기반 시스템은 `foundation.graphics`와 `foundation.graphics.sprite`에 있습니다. 학생이 원리를 확인하는 실습은 가능하지만, 게임 기능 과제에서 매번 다시 작성하게 하지는 않습니다.

## 학생들이 만들거나 수정해도 되는 부분

| 코드 | 실습 내용 |
|---|---|
| `ShootingSpriteCatalog` | “0행은 Player idle”처럼 셀에 게임 의미 부여 |
| `SpriteClip` | 1~8개 Region을 Animation 계열로 묶고 속도 지정 |
| `Player/Enemy/Bullet/Explosion` | 현재 상태에 맞는 Clip/Region 선택 |
| 학생별 Sprite Sheet | 행/열 구성, 이미지 교체, 이름과 상태 설계 |

다음 순서로 실습하면 됩니다.

1. `region(0, 0)` 하나를 Player에 출력
2. column을 직접 바꾸며 네 Sprite 확인
3. DeltaTime으로 frame index 전환
4. `SpriteClip`으로 중복 제거
5. Player idle, Enemy flight, Explosion처럼 의미 있는 이름 부여
6. 상태에 따라 idle/bank/damaged Clip 선택

## 제공된 샘플 이미지

모든 PNG는 투명 배경입니다. Player와 Effects는 4×4, Enemy는 4×2입니다. 로더의 시트 전체 칸 수에는 제한이 없습니다.

### `player_sprites.png`

- 0행: Player idle/thruster 4프레임
- 1행: bank-left 4프레임
- 2행: bank-right 4프레임
- 3행: damaged/power-up 상태 4종

### `effects_sprites.png`

- 0행: Player 탄환 계열 4종
- 1행: Enemy 탄환 계열 4종
- 2행: Impact 4프레임
- 3행: Explosion 4프레임

### `enemy_sprites.png`

- 0행: Crescent Drone flight 4프레임
- 1행: Armored Bomber flight 4프레임

### `player_damage_sprites.png`

- 0행: Player 피격 4프레임
- 1행: Player 격추 4프레임

### `enemy_damage_sprites.png`

- 0행: Crescent Drone 피격 4프레임
- 1행: Crescent Drone 격추 4프레임
- 2행: Armored Bomber 피격 4프레임
- 3행: Armored Bomber 격추 4프레임

피격/격추 시트는 기존 비행 시트와 별도 Texture입니다. 상태가 바뀔 때 `ShootingSprites.newPlayerHitClip()`처럼 해당 Clip을 선택하는 작업은 학생 실습 영역으로 둡니다.

이미지는 제공된 고전 슈팅게임 자료에서 “격자에 상태와 애니메이션을 배열한다”는 용도만 참고하고, 캐릭터 실루엣·색·픽셀을 복사하지 않은 새 강의용 시안입니다. 참고 이미지는 프로젝트에 포함하지 않았습니다.

## Unity / Unreal 비교

- Unity Sprite Editor의 Grid by Cell Count → `SpriteSheet(columns, rows)`
- Unity Sprite/Unreal PaperSprite → `SpriteRegion`
- Unity Animation Clip/PaperFlipbook → `SpriteClip`
- Unity Resources/Unreal Asset Manager → `ResourceManager`

상용 도구의 자동 Slice UI 대신, 여기서는 `(column, row) → UV` 계산이 실제로 무엇을 하는지 코드로 확인합니다.
