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
- Update this file when new repository conventions become clear.
- Add short rationale in PRs or commit messages when touching config, auth, persistence, or scheduled jobs.

## Notes

- `codex init` could not be executed in this environment because `codex.exe` was blocked by a local access-permission issue.
- This file was added manually to provide the same practical bootstrap for future Codex sessions.
