# CLAUDE.md

Update this file only for architectural or structural changes.
Avoid full file rewrites unless unavoidable.

# Response Efficiency

- Default: concise.
- Max 250 words unless task requires more.
- No explanations unless explicitly requested.
- Do not restate the prompt.
- Prefer structured output over prose.
- Prefer diff patches for edits.
- Show only modified functions.
- No decorative formatting or unnecessary comments.

# Scope & Context Control

- Never scan the full repository unless explicitly asked.
- Operate only on provided files.
- If missing context, ask which file to inspect.
- Do not perform speculative refactors.
- Do not modify unrelated files.

# Execution Strategy

For complex tasks:
1. Provide short plan (max 6 bullets).
2. Wait for confirmation before implementation.

For bug fixing:
- Focus only on the relevant error.
- Ignore unrelated logs.
- Provide minimal corrective steps.

# App Context

WalkKittie (`com.mert.paticat`)
Virtual pet Android app linked to step count and water intake.

Tech stack:
- Clean Architecture + MVVM
- Hilt DI (modules in `di/`)
- Navigation Compose (routes in `ui/navigation/`)
- StateFlow + collectAsStateWithLifecycle()
- Room v11 (explicit migrations required)
- WorkManager + Foreground StepCounterService

Key rules:
- Add named Room migrations for schema changes.
- Update dependency versions only in `libs.versions.toml`.
- CatEntity is single-row (id = 1).
- Secrets stored in `local.properties`.
- Do not run build commands unless explicitly asked.