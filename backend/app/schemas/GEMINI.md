# Schemas 지침

## ✅ Pydantic 모델 설계
- 데이터 검증 및 직렬화를 위해 `BaseModel`을 상속받은 스키마를 작성합니다.
- 필드 설명(`Field(..., description=...)`)을 추가하여 자동 생성 문서의 질을 높입니다.

## 🔄 모델 분리
- **Request**: 클라이언트로부터 받는 데이터 (`DeviceCreate`, `PairingRequest` 등).
- **Response**: 클라이언트에게 반환하는 데이터 (`DeviceResponse`, `PairingInfo` 등).
- **ORM 대응**: SQLAlchemy 모델과의 상호 운용을 위해 `config = ConfigDict(from_attributes=True)`를 설정합니다.
