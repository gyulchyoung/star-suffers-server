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
- Before making any code changes, share the implementation plan with the user first.
- Check `src/main/resources/application.yml` and any profile-specific files before changing configuration behavior.
- Keep profile handling intact. This project selects resources with the Gradle `profile` property.
- Do not commit secrets or overwrite environment-specific settings without confirmation.
- Prefer focused changes in one area at a time instead of broad package-wide rewrites.
- If text files look garbled in the terminal, assume an encoding/display issue first and inspect carefully before editing.

## Safe First Steps

When starting work, agents should usually inspect these files first:

1. `README.md`
2. `build.gradle`
3. `src/main/resources/application.yml`
4. the relevant controller/service/repository package for the requested task

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

- For analysis/setup work, use a dedicated branch such as `codex/agents-bootstrap`.
- When starting a new task in a new thread, create a new branch before making changes.
- Update this file when new repository conventions become clear.
- Add short rationale in PRs or commit messages when touching config, auth, persistence, or scheduled jobs.

## Git Workflow Rules

### Base Branch

- If the user asks to create a PR without specifying a base branch, use `develop`.
- Do not target `master`. This team does not merge work into `master`.

### Branch Naming

Use short and readable branch names with this format:

```text
codex/<topic>
```

Recommended examples:

- `codex/git-pr-guidelines`
- `codex/auth-fix`
- `codex/docs-update`
- `codex/new-feature`

Guidelines:

- use lowercase letters
- separate words with hyphens
- keep the name focused on one task
- prefer concise names over ticket-style noise unless the user asks otherwise

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

Write PR bodies in Korean using this template:

```markdown
## 요약
- 이 PR에서 변경한 내용을 간단히 정리합니다.

## 변경 사항
- 주요 변경 사항 1
- 주요 변경 사항 2

## 작업 전 계획
- 작업 전에 사용자에게 공유한 계획을 적습니다.

## AI 변경 여부
- [ ] AI 도움 없이 직접 작성
- [ ] AI로 작성 또는 AI 도움을 받아 수정

## 테스트
- [ ] 테스트 미실행
- [ ] 로컬 테스트 완료
- [ ] 기타

## 참고 사항
- 리뷰어가 알면 좋은 배경, 제약, 후속 작업 등을 적습니다.
```

Template guidance:

- keep the summary short and readable
- describe user-facing or developer-facing changes clearly
- mark whether AI was used for the code or document changes
- if tests were not run, say so plainly
- include the pre-change plan that was shared with the user

## Notes

- `codex init` could not be executed in this environment because `codex.exe` was blocked by a local access-permission issue.
- This file was added manually to provide the same practical bootstrap for future Codex sessions.
