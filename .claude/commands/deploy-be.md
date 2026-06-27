---
description: Supabase 백엔드(마이그레이션 + Edge Functions) 자동 배포
argument-hint: "[function-name] (생략 시 마이그레이션 + 전체 함수 배포)"
allowed-tools: Bash(supabase:*), Bash(grep:*), Bash(find:*), Bash(ls:*), Bash(cat:*)
---

SeniorSafe Supabase 백엔드를 실제 프로젝트에 배포한다. 아래 절차를 **순서대로** 수행하고, 각 단계 결과를 사용자에게 간결히 보고하라. 실패하면 멈추고 원인을 보고하라 — 다음 단계로 넘어가지 마라.

인자(`$ARGUMENTS`):
- **비어 있으면**: 마이그레이션 + Edge Functions 전체 배포 (기본 모드)
- **함수 이름이 주어지면**: 해당 함수 1개만 배포 (마이그레이션은 건너뜀)

## 0. 사전 점검

```bash
supabase --version
cat supabase/.temp/project-ref 2>/dev/null || echo "NOT_LINKED"
```

- `supabase` CLI가 없으면 중단하고 설치를 안내하라.
- `project-ref`가 없으면(`NOT_LINKED`) `supabase link --project-ref <ref>`가 먼저 필요하다고 알리고 중단하라. 현재 링크된 ref는 `kntbhzkjudslrgtomxmg`이다.

## 1. 단일 함수 모드 (`$ARGUMENTS`에 함수 이름이 있을 때)

해당 디렉토리가 `supabase/functions/<name>`에 존재하는지 `find supabase/functions -maxdepth 1 -type d`로 확인한 뒤:

```bash
supabase functions deploy <name>
```

결과 보고 후 **종료**. (아래 2~3단계는 수행하지 않는다.)

## 2. 마이그레이션 배포 (기본 모드)

먼저 적용될 마이그레이션을 보여주고 push 한다:

```bash
find supabase/migrations -type f -name '*.sql' | sort
supabase db push
```

- `db push`는 원격 DB 스키마를 변경한다. 출력에 적용 대상 마이그레이션이 나오면 그대로 진행하되, "No schema changes found"면 그 사실만 보고하고 다음으로 넘어간다.

## 3. Edge Functions 전체 배포 (기본 모드)

`_shared`와 `tests`는 함수가 아니므로 제외한다. `supabase/config.toml`의 `[functions.*]` 섹션에 등록된 12개 함수만 배포 대상이다.

```bash
supabase functions deploy
```

- 위 명령은 `supabase/functions/` 하위의 배포 가능한 모든 함수를 한 번에 배포한다.
- 만약 `tests` 디렉토리 때문에 에러가 나면, config.toml에 등록된 함수만 이름을 나열해 개별 배포로 폴백하라:

```bash
grep '^\[functions\.' supabase/config.toml | sed -E 's/\[functions\.(.+)\]/\1/'
# 위 목록을 한 줄씩 supabase functions deploy <name> 로 배포
```

## 4. 최종 보고

- 적용된 마이그레이션 수
- 배포된 함수 수/목록
- 실패한 항목이 있으면 명확히 구분해서 보고
- 참고: secrets(`SUPABASE_JWT_SECRET`, `FIREBASE_SERVICE_ACCOUNT`, `CRON_SECRET` 등)는 이 커맨드가 건드리지 않는다. 미설정 시 `supabase secrets set ...`이 별도로 필요함을 마지막에 한 줄로 환기하라.
