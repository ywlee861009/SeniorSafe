# Model 모듈 지침

## 🏗 데이터 클래스 설계
- 모든 모델은 `data class`로 정의하며 불변성(Immutability)을 유지하기 위해 `val`을 사용합니다.
- 복잡한 객체 생성 로직이 필요한 경우 `Companion Object`의 팩토리 함수를 사용하십시오.

## serialization
- 네트워크 통신이나 데이터 저장에 사용되는 모델은 `kotlinx.serialization` 또는 `Gson` 어노테이션을 적절히 사용합니다.
- API 응답 모델(DTO)과 도메인 모델은 분리하여 관리하는 것을 지향합니다.

## 🔗 종속성
- 이 모듈은 다른 안드로이드 프레임워크나 외부 라이브러리에 대한 의존성을 최소화해야 합니다 (순수 Kotlin 모듈 지향).
