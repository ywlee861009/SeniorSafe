# SeniorSafe Design System

## 개요

SeniorSafe는 하나의 앱에 두 가지 사용자 경험이 공존한다.

- **Senior 테마**: 고대비, 대형 컴포넌트, 접근성 최우선
- **Guardian 테마**: Material 3 기반, 정보 밀도 중심

현재 MVP는 낙상 감지 화면을 보류하고, 잠금해제 활동 기록과 미사용 알림 중심으로 설계한다.

---

## 1. 색상

### 팔레트

```text
Primary
  primary-100   #E8F0FF
  primary-300   #7AAAF5
  primary-500   #1A6FCC
  primary-700   #0F4A99
  primary-900   #062860

Success
  success-100   #E6F5E6
  success-500   #1A8A1A
  success-700   #116611

Warning
  warning-100   #FEF3C7
  warning-500   #D97706
  warning-700   #B45309

Danger
  danger-100    #FFE8E8
  danger-500    #CC0000
  danger-700    #990000

Neutral
  neutral-000   #FFFFFF
  neutral-050   #F8F8F8
  neutral-100   #F0F0F0
  neutral-200   #E0E0E0
  neutral-400   #AAAAAA
  neutral-600   #666666
  neutral-800   #333333
  neutral-900   #1A1A1A
  neutral-950   #111111
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
| `color-status-warning` | `#D97706` | `#D97706` |
| `color-status-inactive` | `#888888` | `#AAAAAA` |
| `color-danger` | `#CC0000` | `#CC0000` |

---

## 2. 타이포그래피

- 기본 폰트: Noto Sans KR 또는 시스템 기본 한글 폰트
- 숫자 전용: Roboto

| 토큰 | Senior | Guardian | 용도 |
|------|--------|----------|------|
| `text-code` | 40sp / Bold | — | 연결 코드 |
| `text-heading-1` | 28sp / Bold | 22sp / Bold | 상태 텍스트, 화면 제목 |
| `text-heading-2` | 24sp / Bold | 18sp / SemiBold | 섹션 제목 |
| `text-body-1` | 20sp / Regular | 16sp / Regular | 본문, 카드 내용 |
| `text-body-2` | 17sp / Regular | 14sp / Regular | 보조 설명 |
| `text-caption` | 15sp / Regular | 12sp / Regular | 타임스탬프, 힌트 |
| `text-button` | 22sp / Bold | 14sp / Medium | 버튼 레이블 |

모든 텍스트 줄 간격은 해당 폰트 크기 × 1.5를 기본으로 한다.

---

## 3. 간격

8dp 그리드 시스템 기반.

| 토큰 | 값 | 용도 |
|------|-----|------|
| `space-4` | 4dp | 인라인 요소 간격 |
| `space-8` | 8dp | 컴포넌트 내부 소형 여백 |
| `space-12` | 12dp | 카드 내부 항목 간격 |
| `space-16` | 16dp | 기본 패딩 |
| `space-24` | 24dp | 섹션 간 간격 |
| `space-32` | 32dp | 화면 상단 여백 |
| `space-48` | 48dp | 대형 요소 간 간격 |

| | Senior | Guardian |
|---|--------|----------|
| 좌우 패딩 | 24dp | 16dp |
| 상단 패딩 | 32dp | 16dp |
| 하단 패딩 | 32dp | 16dp |

---

## 4. 터치 타겟

| | Senior | Guardian |
|---|--------|----------|
| 최소 터치 영역 | 72dp | 48dp |
| 권장 터치 영역 | 80dp+ | 56dp |

---

## 5. 주요 아이콘

| 의미 | Material Symbol |
|------|----------------|
| 서비스 ON | `shield` |
| 서비스 OFF | `shield_off` |
| 최근 사용 확인 | `check_circle` |
| 미사용 주의 | `notifications_active` |
| 보호자 | `supervisor_account` |
| 어르신 | `elderly` |
| 연결 코드 | `qr_code` |
| 알림 내역 | `notifications` |
| 활동 이력 | `history` |
| 연결 추가 | `person_add` |
| 다시 보내기 | `sync` |

---

## 6. 컴포넌트

### Primary Button

```text
┌──────────────────────────────┐
│         버튼 레이블            │
└──────────────────────────────┘
```

- 배경: `color-primary`
- 텍스트: `color-on-primary`
- radius: 12dp
- 높이: Senior 72dp / Guardian 52dp

### Status Indicator

서비스 상태를 나타내는 대형 원형 인디케이터.

```text
          ●
      안부 확인 중
```

| 상태 | 원 색상 | 텍스트 |
|------|--------|--------|
| ON | `success-500` | 안부 확인 중 |
| OFF | `neutral-400` | 안부 확인 꺼짐 |
| 전송 대기 | `warning-500` | 전송 대기 중 |

### Senior Card

보호자 화면의 어르신 목록 카드.

```text
┌────────────────────────────────┐
│  👤  홍길동           ›        │
│      🟢 오늘 오전 8:30 사용     │
│      알림 기준: 2일 미사용      │
└────────────────────────────────┘
```

| 상태 | 색상 | 문구 |
|------|------|------|
| 최근 사용 | `success-500` | 오늘/어제/날짜 사용 |
| 미사용 기준 경과 | `warning-500` | N일 동안 사용 기록 없음 |
| 정보 없음 | `neutral-400` | 아직 사용 기록 없음 |

### Activity History Item

```text
┌────────────────────────────────┐
│  08:30 잠금해제 기록 전송 완료  │
│  user_present                   │
└────────────────────────────────┘
```

사용처:

- 어르신 MVP 진단 화면
- 보호자 어르신 상세
- 잠금해제 이력
- 서비스 실행 이력

### Pairing Code Display

```text
┌────────────────────────────────┐
│           482913               │
└────────────────────────────────┘
```

- 폰트: Roboto Bold
- 숫자 6자리
- 유효시간 타이머 표시

### Bottom Navigation

```text
┌────────────────────────────────┐
│  🏠 내 어르신    🔔 알림 내역  │
└────────────────────────────────┘
```

- Guardian 전용
- 높이: 64dp
- 활성 아이콘: `color-primary`
- 비활성 아이콘: `neutral-600`

---

## 7. 접근성

- Senior: 최소 72dp 터치 영역
- Guardian: 최소 48dp 터치 영역
- 상태는 색상만으로 표현하지 않고 텍스트를 함께 표시
- 알림 문구는 위험을 확정하지 않음
- 시스템 폰트 크기 확대 시 텍스트가 잘리지 않도록 세로 확장 허용

---

## 8. Deferred

낙상 감지 오버레이, 카운트다운, 낙상 이력 컴포넌트는 현재 MVP에서 보류한다. 향후 낙상 감지 검증을 재개할 때 별도 디자인 문서로 복원한다.
