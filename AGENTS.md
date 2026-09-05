## Agent skills

### Issue tracker

Issues live as local markdown files under `.scratch/<feature>/`. See `docs/agents/issue-tracker.md`.

### Triage labels

Default five-role vocabulary (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context layout: root `CONTEXT.md` + `docs/adr/`. See `docs/agents/domain.md`.

## Repository Boundary

- All issues, pull requests, comments, labels, releases, and other tracker or GitHub write operations for this effort must stay in the user's own repository. The current owned repository is `Tki-sor/GregTech-Modern`.
- `GregTechCEu/GregTech-Modern` is an upstream read-only source for fetching, comparing, and synchronizing code. Never create or modify issues, pull requests, comments, labels, releases, or branches there.
- Draft and final PRs must target `Tki-sor/GregTech-Modern` only. Upstream changes are incorporated locally through the documented rebase/translation workflow.
