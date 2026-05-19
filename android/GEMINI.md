# Android 프로젝트 지침

## 📦 모듈 구조
- `:app`: 진입점, DI 컨테이너 설정 및 전역 내비게이션 담당.
- `:core:*`: 공통 로직, 데이터 및 UI 컴포넌트. 특정 기능에 종속된 로직은 포함하지 않음.
- `:feature:*`: 개별 사용자 흐름. 다른 기능 모듈에 의존해서는 안 됨.

## 🎨 UI 및 Compose
- `:core:ui`의 `SeniorSafeTheme`을 사용합니다.
- Material 3 컴포넌트 사용을 지향합니다.
- 상태 관리: `ViewModel`과 `StateFlow`, `collectAsStateWithLifecycle`을 사용합니다.

## 💉 의존성 주입 (DI)
- 모든 DI에는 Hilt를 사용합니다.
- `@Module` 내에서 `@Binds` 또는 `@Provides`를 사용하여 인터페이스를 바인딩합니다.

## ⚙️ 빌드 시스템
- 모든 버전 관리는 `gradle/libs.versions.toml`에서 수행합니다.
- 일관된 모듈 설정을 위해 `build-logic`의 컨벤션 플러그인을 사용합니다.
