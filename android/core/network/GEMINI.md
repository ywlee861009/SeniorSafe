# Network 모듈 지침

## 🌐 API 정의
- 모든 엔드포인트는 `ApiService.kt`에 정의합니다.
- 모든 네트워크 호출에는 `suspend` 함수를 사용합니다.
- 인증이 필요한 호출에는 명시적인 `@Header("Authorization") token: String`을 사용합니다 (MVP 패턴).

## 🛠 설정
- Retrofit 인스턴스는 Hilt의 `NetworkModule`을 통해 제공되어야 합니다.
- JSON 파싱에는 `Gson`을 사용합니다.
- 디버그 빌드에서는 항상 `HttpLoggingInterceptor`를 포함해야 합니다.
