# UI 모듈 지침

## 🎨 디자인 시스템
- 모든 UI 컴포넌트는 `:core:ui`의 `AnbuTheme` 하에서 작동해야 합니다.
- 색상, 타이포그래피, 형상은 직접 하드코딩하지 말고 `MaterialTheme.colorScheme`, `MaterialTheme.typography` 등을 사용하십시오.

## 🧱 컴포넌트 설계
- **Stateless Composable**: 가능한 한 상태를 가지지 않는(Stateless) 컴포posable을 작성하고, 상태는 상위로 호이스팅(Hoisting)하십시오.
- **Preview**: 모든 컴포저블에는 `@Preview`를 작성하여 IDE에서 시각적으로 확인할 수 있도록 합니다.
- **재사용성**: 특정 기능에 종속적인 UI는 `feature` 모듈에 작성하고, 범용적인 컴포넌트만 여기에 작성합니다.

## 📱 레이아웃 가이드
- 다양한 화면 크기(Phone, Tablet)를 고려하여 유연한 레이아웃을 작성합니다.
- 접근성(Accessibility)을 위해 `contentDescription`을 적절히 제공하십시오.
