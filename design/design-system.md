# SeniorSafe Design System

## 개요

SeniorSafe는 하나의 앱에 두 가지 사용자 경험이 공존한다.

- **Senior 테마**: 고대비, 대형 컴포넌트, 접근성 최우선
- **Guardian 테마**: Material 3 기반, 정보 밀도 중심

두 테마는 동일한 컴포넌트를 사용하되, 토큰 값만 다르게 적용한다.

---

## 1. 색상 (Color)

### 팔레트

```
Primary
  primary-100   #E8F0FF
  primary-300   #7AAAF5
  primary-500   #1A6FCC   ← 기본 버튼, 링크, 탭 활성
  primary-700   #0F4A99
  primary-900   #062860

Success (보호 중 상태)
  success-100   #E6F5E6
  success-500   #1A8A1A   ← 활성 상태 표시
  success-700   #116611

Danger (낙상 / 경고)
  danger-100    #FFE8E8
  danger-500    #CC0000   ← 낙상 오버레이, 위험 버튼
  danger-700    #990000

Neutral
  neutral-000   #FFFFFF
  neutral-050   #F8F8F8   ← Guardian 배경
  neutral-100   #F0F0F0
  neutral-200   #E0E0E0   ← 구분선, 외곽선
  neutral-400   #AAAAAA   ← 비활성 텍스트, 꺼진 상태
  neutral-600   #666666   ← 보조 텍스트
  neutral-800   #333333   ← 본문 텍스트
  neutral-900   #1A1A1A   ← 제목 텍스트
  neutral-950   #111111   ← Senior 주요 텍스트
```

### 시맨틱 토큰

| 토큰 | Senior | Guardian |
|------|--------|----------|
| `color-background` | `#FFFFFF` | `#F8F8F8` |
| `color-surface` | `#FFFFFF` | `#FFFFFF` |
| `color-on-surface` | `#111111` | `#1A1A1A` |
| `color-primary` | `#1A6FCC` | `#1A6FCC` |
| `color-on-primary` | `#FFFFFF` | `#FFFFFF` |
| `color-secondary-text` | `#555555` | `#666666` |
| `color-divider` | `#E0E0E0` | `#E0E0E0` |
| `color-status-active` | `#1A8A1A` | `#1A8A1A` |
| `color-status-inactive` | `#888888` | `#AAAAAA` |
| `color-danger` | `#CC0000` | `#CC0000` |

---

## 2. 타이포그래피 (Typography)

### 폰트

- **기본 폰트**: Noto Sans KR (시스템 기본 한글 폰트)
- **숫자 전용**: Roboto (카운트다운 숫자, 코드 표시)

### 스케일

| 토큰 | Senior | Guardian | 용도 |
|------|--------|----------|------|
| `text-display` | 60sp / Bold | — | 카운트다운 숫자 |
| `text-code` | 40sp / Bold | — | 연결 코드 |
| `text-heading-1` | 28sp / Bold | 22sp / Bold | 상태 텍스트, 화면 제목 |
| `text-heading-2` | 24sp / Bold | 18sp / SemiBold | 섹션 제목 |
| `text-body-1` | 20sp / Regular | 16sp / Regular | 본문, 카드 내용 |
| `text-body-2` | 17sp / Regular | 14sp / Regular | 보조 설명 |
| `text-caption` | 15sp / Regular | 12sp / Regular | 타임스탬프, 힌트 |
| `text-button` | 22sp / Bold | 14sp / Medium | 버튼 레이블 |

### 줄 간격 (Line Height)

모든 텍스트: 해당 폰트 크기 × 1.5

---

## 3. 간격 (Spacing)

8dp 그리드 시스템 기반.

| 토큰 | 값 | 용도 |
|------|-----|------|
| `space-2` | 2dp | 아이콘-텍스트 최소 간격 |
| `space-4` | 4dp | 인라인 요소 간격 |
| `space-8` | 8dp | 컴포넌트 내부 소형 여백 |
| `space-12` | 12dp | 카드 내부 항목 간격 |
| `space-16` | 16dp | 기본 패딩, 카드 내부 여백 |
| `space-24` | 24dp | 섹션 간 간격 |
| `space-32` | 32dp | 화면 상단 여백 |
| `space-48` | 48dp | 대형 요소 간 간격 (Senior) |

### 화면 여백 (Screen Margin)

| | Senior | Guardian |
|---|--------|----------|
| 좌우 패딩 | 24dp | 16dp |
| 상단 패딩 | 32dp | 16dp |
| 하단 패딩 | 32dp | 16dp |

---

## 4. 크기 (Sizing)

### 터치 타겟 (Touch Target)

| | Senior | Guardian |
|---|--------|----------|
| 최소 터치 영역 | **72dp** | 48dp |
| 권장 터치 영역 | 80dp+ | 56dp |

> Material 3 기본값은 48dp. Senior는 고령자 손 떨림 고려하여 확대.

### 컴포넌트 높이

| 컴포넌트 | Senior | Guardian |
|----------|--------|----------|
| Primary Button | 72dp | 52dp |
| Secondary Button | 64dp | 48dp |
| Text Field | 64dp | 56dp |
| List Item | 72dp | 64dp |
| Bottom Tab Bar | — | 64dp |
| Top App Bar | — | 56dp |

---

## 5. 모서리 반경 (Border Radius)

| 토큰 | 값 | 용도 |
|------|-----|------|
| `radius-sm` | 8dp | 입력창, 소형 칩 |
| `radius-md` | 12dp | 버튼, 카드 |
| `radius-lg` | 16dp | 상태 카드, 모달 |
| `radius-xl` | 24dp | 바텀 시트 |
| `radius-full` | 999dp | 원형 버튼, 뱃지 |

---

## 6. 그림자 / 엘리베이션 (Elevation)

Material 3 엘리베이션 기준 적용.

| 레벨 | dp | 적용 대상 |
|------|-----|----------|
| 0 | 0dp | 배경, 구분선 |
| 1 | 1dp | 카드 (기본) |
| 2 | 3dp | 카드 (강조), 입력 포커스 |
| 3 | 6dp | 바텀 탭, 상단 바 |
| 4 | 8dp | 모달, 다이얼로그 |
| 5 | 12dp | 바텀 시트 |

---

## 7. 아이콘 (Iconography)

- **아이콘 세트**: Material Symbols (Rounded 스타일)
- **크기**:
  - Senior: 32dp
  - Guardian: 24dp
- **색상**: 맥락에 따라 `color-primary` 또는 `color-on-surface`

### 주요 아이콘 매핑

| 의미 | Material Symbol |
|------|----------------|
| 서비스 ON (보호 중) | `shield` |
| 서비스 OFF | `shield_off` |
| 낙상 감지 | `warning` |
| 보호자 | `supervisor_account` |
| 어르신 | `elderly` |
| 연결 코드 | `qr_code` |
| 알림 내역 | `notifications` |
| 낙상 이력 | `history` |
| 연결 추가 | `person_add` |
| 확인 (취소) | `check_circle` |

---

## 8. 컴포넌트

### 8-1. Button

#### Primary Button

```
┌──────────────────────────────┐
│         버튼 레이블            │  ← text-button, color-on-primary
└──────────────────────────────┘
배경: color-primary
radius: radius-md
높이: Senior 72dp / Guardian 52dp
```

**상태**

| 상태 | 배경 | 텍스트 |
|------|------|--------|
| Default | `color-primary` | `#FFFFFF` |
| Pressed | `primary-700` | `#FFFFFF` |
| Disabled | `neutral-200` | `neutral-400` |
| Loading | `color-primary` + 스피너 | — |

#### Secondary (Outlined) Button

```
┌──────────────────────────────┐  ← 외곽선 1.5dp, color-primary
│         버튼 레이블            │  ← text-button, color-primary
└──────────────────────────────┘
배경: transparent
```

#### Danger Button

```
┌──────────────────────────────┐
│     ✋  괜찮아요 (취소)        │
└──────────────────────────────┘
배경: color-danger
텍스트: #FFFFFF
Senior 전용 — 낙상 오버레이에서만 사용
```

#### Text Button

```
  버튼 레이블 (밑줄 없음)
  텍스트: color-primary
  배경: transparent
  용도: 회원가입 링크, 보조 액션
```

---

### 8-2. Text Field

```
┌──────────────────────────────┐
│  레이블                       │  ← text-caption, neutral-600
│  ┌────────────────────────┐  │
│  │  입력값                 │  │  ← text-body-1
│  └────────────────────────┘  │
│  힌트 또는 에러 메시지          │  ← text-caption
└──────────────────────────────┘
```

**상태**

| 상태 | 외곽선 | 레이블 색상 |
|------|--------|------------|
| Default | `neutral-200` 1dp | `neutral-600` |
| Focused | `color-primary` 2dp | `color-primary` |
| Error | `color-danger` 2dp | `color-danger` |
| Disabled | `neutral-100` | `neutral-400` |

**에러 메시지 위치**: 인풋 하단 `space-4` 아래, `text-caption`, `color-danger`

---

### 8-3. Status Indicator (Senior 전용)

서비스 ON/OFF 상태를 나타내는 대형 원형 인디케이터.

```
          ┌─────────┐
          │    ●    │   ← 원형, 지름 80dp
          │         │
          └─────────┘
          보호 중
```

| 상태 | 원 색상 | 텍스트 |
|------|--------|--------|
| ON | `success-500` | 보호 중 |
| OFF | `neutral-400` | 보호 꺼짐 |

애니메이션: ON 상태에서 2초 주기로 바깥으로 펄스(ripple) 효과.

---

### 8-4. Senior Card (보호자 어르신 목록)

```
┌────────────────────────────────┐  ← radius-md, elevation-1
│                                │
│  👤  홍길동           ›        │  ← text-heading-2, text-body-1
│      🟢 보호 중                │  ← status dot + 텍스트
│      마지막 낙상: 없음          │  ← text-body-2, neutral-600
│                                │
└────────────────────────────────┘
패딩: space-16
간격: space-8
```

**Status Dot**

| 상태 | 색상 |
|------|------|
| 보호 중 | `success-500` |
| 서비스 꺼짐 | `neutral-400` |

---

### 8-5. Fall History Item

```
┌────────────────────────────────┐  ← elevation-1
│  🔴  2026.05.14  오후 3:22     │  ← text-body-1
│      알림 전송됨                │  ← text-body-2, neutral-600
└────────────────────────────────┘
```

| 타입 | 아이콘 색 | 레이블 |
|------|----------|--------|
| 알림 전송 | `danger-500` 🔴 | 알림 전송됨 |
| 본인 취소 | `neutral-400` ⚪ | 본인 취소 |

---

### 8-6. Pairing Code Display (Senior 전용)

```
┌────────────────────────────────┐  ← radius-lg, neutral-100 배경
│                                │
│           A3F9K2               │  ← text-code (40sp Bold), 자간 8sp
│                                │
└────────────────────────────────┘
패딩 상하: space-24
패딩 좌우: space-32
```

- 폰트: Roboto Bold (숫자/영문 가독성)
- 색상: `neutral-900`
- 유효시간 타이머: 하단 `space-16`, `text-body-2`, 만료 임박 시 `color-danger`

---

### 8-7. Countdown Timer (낙상 오버레이)

```
          [ 25 ]
```

- 폰트: Roboto Bold, `text-display` (60sp)
- 색상: `#FFFFFF`
- 1초마다 숫자 페이드 업데이트
- 배경 전체: `color-danger`

---

### 8-8. Bottom Navigation (Guardian 전용)

```
┌────────────────────────────────┐
│  🏠 내 어르신    🔔 알림 내역  │
└────────────────────────────────┘
높이: 64dp
활성 아이콘: color-primary
비활성 아이콘: neutral-600
레이블: text-caption
```

---

### 8-9. Top App Bar

| | Senior | Guardian |
|---|--------|----------|
| 표시 여부 | 미사용 (단순 화면) | 사용 |
| 높이 | — | 56dp |
| 배경 | — | `#FFFFFF` |
| 제목 | — | `text-heading-2` |
| 뒤로가기 | 필요한 화면만 | 기본 표시 |

---

### 8-10. Dialog / Alert

낙상 오버레이는 Dialog가 아닌 전체화면 레이어로 처리. 일반 에러/확인용 Dialog:

```
┌────────────────────────────────┐  ← radius-xl, elevation-4
│                                │
│  제목                           │  ← text-heading-2
│  설명 텍스트                     │  ← text-body-1
│                                │
│         [취소]     [확인]       │  ← 우측 정렬
└────────────────────────────────┘
```

---

### 8-11. Toast / Snackbar

```
┌────────────────────────────────┐  ← radius-md, neutral-900 배경
│  메시지 텍스트           [액션] │  ← text-body-2, #FFFFFF
└────────────────────────────────┘
하단 space-24 위치
자동 사라짐: 3초
```

---

### 8-12. Loading State

- **전체화면 로딩**: 중앙 CircularProgressIndicator, `color-primary`
- **버튼 로딩**: 버튼 내부 소형 스피너 (레이블 대체)
- **리스트 로딩**: Skeleton shimmer (neutral-100 → neutral-200 반복)

---

### 8-13. Empty State

```
          🛡️ (아이콘 48dp)

       아직 연결된 어르신이 없어요

      어르신 연결하기 버튼을 눌러
      첫 번째 어르신을 연결해보세요

      ┌──────────────────────┐
      │   + 어르신 연결하기   │
      └──────────────────────┘
```

- 아이콘: 64dp, `neutral-300`
- 제목: `text-heading-2`, `neutral-600`
- 설명: `text-body-2`, `neutral-400`

---

### 8-14. Error State

```
          ⚠️

       오류가 발생했어요

       잠시 후 다시 시도해주세요

      ┌──────────────────────┐
      │       다시 시도       │
      └──────────────────────┘
```

---

## 9. 애니메이션 / 모션 (Motion)

최소한의 애니메이션만 사용. 고령자 대상 과도한 전환 효과 배제.

| 종류 | 지속시간 | Easing | 적용 대상 |
|------|---------|--------|----------|
| 화면 전환 | 200ms | FastOutSlowIn | 액티비티 이동 |
| 버튼 누름 | 100ms | Linear | 리플 효과 |
| 상태 변경 | 300ms | FastOutSlowIn | 서비스 ON/OFF |
| 오버레이 진입 | 250ms | FastOutSlowIn | 낙상 알림 등장 |
| 펄스 (보호중) | 2000ms | Sine | Status Indicator |
| Toast | 200ms | 진입 / 2700ms 유지 / 200ms 퇴장 | |

**⚠️ 규칙**
- 자동 재생 애니메이션은 사용자 설정에서 `reduceMotion` 확인 필요
- 낙상 감지 오버레이: 빠른 진입(150ms), 이후 정적 유지

---

## 10. 접근성 (Accessibility)

### 색상 대비

WCAG 2.1 기준.

| 조합 | 비율 | 기준 |
|------|------|------|
| 주요 텍스트 (`#111111`) on `#FFFFFF` | 19.7:1 | AAA ✅ |
| 버튼 텍스트 (`#FFFFFF`) on `#1A6FCC` | 5.2:1 | AA ✅ |
| 상태 활성 (`#1A8A1A`) on `#FFFFFF` | 5.6:1 | AA ✅ |
| 위험 (`#CC0000`) on `#FFFFFF` | 5.9:1 | AA ✅ |
| 보조 텍스트 (`#555555`) on `#FFFFFF` | 7.4:1 | AAA ✅ |

### 터치 타겟

- Senior: 최소 **72dp × 72dp** (손 떨림 고려)
- Guardian: 최소 **48dp × 48dp** (Material 기준)
- 터치 영역이 시각 영역보다 작을 경우 `TouchDelegate`로 확장

### TalkBack (스크린 리더)

- 모든 이미지/아이콘: `contentDescription` 필수
- 상태 인디케이터: 색상 외 텍스트로도 상태 전달 (색상에만 의존 금지)
- 낙상 오버레이: `IMPORTANCE_HIGH` 포커스 자동 이동

### 폰트 크기

- Senior: 시스템 폰트 크기 설정 무시 (`sp` 단위 사용하되 최소값 고정)
- Guardian: 시스템 폰트 크기 설정 반영

---

## 11. 테마 적용 방식 (Android)

`themes.xml`을 두 개로 분리.

```xml
<!-- res/values/themes.xml -->
<style name="Theme.SeniorSafe.Senior" parent="Theme.Material3.Light.NoActionBar">
    <item name="colorPrimary">@color/primary_500</item>
    <item name="android:textSize">20sp</item>
    <!-- ... -->
</style>

<style name="Theme.SeniorSafe.Guardian" parent="Theme.Material3.Light.NoActionBar">
    <item name="colorPrimary">@color/primary_500</item>
    <item name="android:textSize">14sp</item>
    <!-- ... -->
</style>
```

로그인 후 유저 타입에 따라 `Application.setTheme()` 또는 각 Activity에서 `setTheme()` 호출.

---

## 12. 파일 구조 (Android res/)

```
res/
├── values/
│   ├── colors.xml         ← 팔레트 전체 정의
│   ├── themes.xml         ← Senior / Guardian 테마
│   ├── strings.xml        ← 모든 문자열
│   ├── dimens.xml         ← spacing, sizing 토큰
│   └── type.xml           ← 타이포그래피 스타일
├── drawable/
│   ├── bg_button_primary.xml
│   ├── bg_button_outlined.xml
│   ├── bg_card.xml
│   ├── ic_shield.xml
│   └── ...
└── layout/
    ├── activity_login.xml
    ├── activity_senior_home.xml
    ├── activity_guardian_home.xml
    ├── overlay_fall_detected.xml
    ├── activity_pairing_code.xml
    ├── activity_fall_history.xml
    ├── activity_connect_senior.xml
    ├── item_senior_card.xml
    └── item_fall_history.xml
```
