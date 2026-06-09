# AGENTS.md

This repository is a Java 11 / Spring Boot 2.5.2 server project built with Gradle.

## Purpose

Use this file as the default operating guide for Codex or other coding agents working in this repository.

Primary goals:

- understand the current server structure before editing
- keep changes small and reviewable
- avoid breaking environment-specific configuration
- prefer safe local verification over broad refactors

## Project Snapshot

- Build tool: Gradle (`build.gradle`, `gradlew.bat`)
- Language: Java 11
- Frameworks: Spring Boot, Spring Security, JPA, MyBatis, QueryDSL
- Main application class:
  - `src/main/java/com/server/tourApiProject/TourApiProjectApplication.java`
- Resource layout:
  - `src/main/resources`
  - `src/main/resources-local`
- Test resources:
  - `src/test/resources`

## Working Rules

- Read `build.gradle` before making dependency or runtime changes.
- Always read this `AGENTS.md` before planning or changing code.
- Before making any code or document changes, share the implementation plan with the user first.
- Check `src/main/resources/application.yml` and any profile-specific files before changing configuration behavior.
- Keep profile handling intact. This project selects resources with the Gradle `profile` property.
- Do not commit secrets or overwrite environment-specific settings without confirmation.
- Prefer focused changes in one area at a time instead of broad package-wide rewrites.
- If text files look garbled in the terminal, assume an encoding or display issue first and inspect carefully before editing.

## Safe First Steps

When starting work, agents should usually inspect these files first:

1. `README.md`
2. `build.gradle`
3. `src/main/resources/application.yml`
4. the relevant controller, service, or repository package for the requested task

## Common Commands

Run from the repository root.

```powershell
.\gradlew.bat test
.\gradlew.bat bootRun
.\gradlew.bat clean build
```

If a specific profile is needed:

```powershell
.\gradlew.bat bootRun -Pprofile=local
.\gradlew.bat clean build -Pprofile=local
```

## Change Guidance

- When starting a new task in a new thread, create a new branch before making changes.
- Update this file when new repository conventions become clear.
- Add short rationale in PRs or commit messages when touching config, auth, persistence, or scheduled jobs.

## Git Workflow Rules

### Base Branch

- If the user asks to create a PR without specifying a base branch, use `develop`.
- Do not target `master`. This team does not merge work into `master`.

### Branch Naming

Always create branches with one of these prefixes:

```text
feature/<short-description>
fix/<short-description>
chore/<short-description>
hotfix/<short-description>
```

Recommended examples:

- `feature/login-api`
- `fix/signup-validation`
- `chore/update-eslint-config`

Guidelines:

- use lowercase letters, numbers, and hyphens only
- choose the prefix that best matches the task type
- keep the name focused on one task
- keep descriptions short and readable

### Pull Request Title

Use this format:

```text
[유형] 간단한 변경내용
```

Allowed PR title types:

- `fix`
- `feat`
- `docs`
- `refactor`
- `test`
- `chore`

Examples:

- `[docs] AGENTS.md에 Git 작업 가이드 추가`
- `[fix] 로그인 토큰 만료 처리 수정`
- `[feat] 공지사항 검색 API 추가`

### Pull Request Template

Write PR bodies in Korean using the repository PR template.
The template should be usable by both AI and human contributors.

Required sections:

```markdown
## 요약
- 이 PR에서 변경한 내용을 간단히 정리합니다.

## 변경 사항
- 주요 변경 사항 1
- 주요 변경 사항 2

## 작업 전 계획
- 작업 전에 사용자 또는 팀에 공유한 계획을 적습니다.

## 체크리스트
- [ ] 변경 내용을 스스로 검토했습니다.
- [ ] 필요한 문서 또는 주석을 업데이트했습니다.
- [ ] 브레이킹 체인지 여부를 확인했습니다.

## AI 사용 여부
- [ ] 코드 생성 또는 수정에 AI를 사용했습니다.
- [ ] PR 본문 작성에 AI를 사용했습니다.
- [ ] 테스트 코드 작성에 AI를 사용했습니다.

## 테스트
- [ ] 로컬 테스트를 실행했습니다.
- [ ] 수동 테스트를 실행했습니다.
- [ ] 테스트 결과를 PR 본문에 정리했습니다.

## 참고 사항
- 리뷰어가 알면 좋은 배경, 제약, 후속 작업 등을 적습니다.
```

Template guidance:

- keep the summary short and readable
- describe user-facing or developer-facing changes clearly
- mark separately whether AI was used for code changes and for PR writing
- keep the template suitable for both AI and non-AI contributors
- describe clearly which tests were executed
- include the pre-change plan that was shared with the user

## Notes

- `codex init` could not be executed in this environment because `codex.exe` was blocked by a local access-permission issue.
- This file was added manually to provide the same practical bootstrap for future Codex sessions.
