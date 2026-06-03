# 할일-006 로그인 없는 기기 보안 정책 구현

## 우선순위

P1

## 문제

로그인을 제거하더라도 API를 완전히 공개하면 아무 클라이언트나 잠금해제 이벤트, 서비스 실행 내역, 페어링, FCM 토큰을 조작할 수 있다. 사용자에게는 로그인 없는 경험을 제공하되, 서버는 기기 단위 인증과 남용 방지 장치를 가져야 한다.

2026-06-03 현재 백엔드(Supabase Edge Functions)는 `device-register`에서 커스텀 device JWT를 발급하고, 모든 보호 API에서 `Authorization: Bearer <device_access_token>`을 검증한다. RLS 정책으로 DB 레벨 보안도 강제한다. 활동/서비스 이벤트 API에도 역할별 권한 검증 구현 완료. Android도 device access token 저장과 OkHttp `Authorization` interceptor 경계가 구현됐다. 남은 핵심은 token 저장소 정리, rate limit, token rotation/폐기, 운영 로그/secret 정책이다.

## 작업 범위

### 백엔드 — ✅ 대부분 완료
- ✅ 커스텀 device JWT (HS256, 365일 만료) 발급/검증 구현
- ✅ RLS 정책으로 DB 레벨 접근 제어 (5개 테이블)
- ✅ 짧은 만료 시간(10분)과 일회성 사용 강제 (pairing-codes)
- ✅ FCM token 업데이트가 해당 device에만 가능 (auth.ts 검증)
- ✅ 활동/서비스 이벤트는 해당 device만 쓸 수 있도록 제한
- ✅ 보호자는 active pairing된 어르신의 활동 내역만 읽을 수 있도록 제한
- ⚠️ 페어링 코드 생성/입력 rate limit 정책 미구현
- ⚠️ token rotation/폐기 정책 미구현
- ⚠️ 운영 로그 민감 정보 점검 미완료

### Android — 일부 완료
- ✅ `TokenDataStore.saveDeviceAuth()`에 device JWT/device id 저장 경계 구현
- ✅ OkHttp 인증 interceptor가 저장된 device token을 Bearer header로 첨부
- ✅ 앱 삭제/앱 데이터 삭제 시 `DeviceDataStore`의 local install id가 사라져 새 기기로 취급하는 정책 구현
- ⚠️ `TokenDataStore`가 user JWT 호환 필드(`user_type`, `user_name`, `user_id`)를 함께 보관하므로 device auth 전용 모델로 정리 필요
- ⚠️ Android 보안 저장소(EncryptedSharedPreferences 등) 사용 여부 미결정
- 보안 정책 문서화

## 완료 조건

- 로그인 없이도 API 요청 주체가 기기 단위로 식별된다.
- Android가 device access token을 user JWT와 분리해 저장하고 모든 보호 API에 자동 첨부한다.
- 다른 기기의 FCM token, 페어링, 잠금해제 이력, 서비스 실행 이력을 임의로 수정할 수 없다.
- 페어링 코드 무차별 대입에 대한 기본 방어가 있다.
- 보안 정책이 문서화되어 있다.
