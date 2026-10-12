---
schema: clarification-answers/v1
status: submitted
generated_at: "2026-10-12T06:45:56.164Z"
scope: [frontend, backend, generic]
questions_file: clarification-questions.json
---

# Rearchitecture Clarification Answers

## 🖥️ Frontend

- **F1** Which React major version should the converted frontend use?
  - answer: React (version unspecified)
  - source: user
- **F2** Which component or UI library should the React frontend use?
  - answer: _none_
  - source: skipped (required, no default — blocking gap)
- **F3** Provide screenshots, a screen recording, or a staging URL for the current UI. If none are available, enter 'none' and the existing frontend will be used as the reference.
  - answer: _none_
  - source: skipped (required, no default — blocking gap)
- **F4** What should be used as the visual design source for the React UI?
  - answer: Preserve current design using frontend/style.css tokens
  - source: user
- **F5** Which accessibility standard should the React frontend target?
  - answer: WCAG 2.1 AA
  - source: user
- **F6** Which browser and runtime compatibility targets should the frontend support?
  - answer: modern evergreen (Chrome, Firefox, Safari, Edge — latest 2 major versions)
  - source: user
- **F7** How should the React frontend handle responsive layouts and breakpoints?
  - answer: Preserve current responsive behavior and breakpoints from frontend/style.css
  - source: user
- **F8** Which locales should the converted frontend support?
  - answer: preserve current locales; keep existing i18n library if present
  - source: user
- **F9** What client-side state management approach should the React frontend use?
  - answer: preserve existing pattern if identifiable; otherwise recommend minimal (component state + server-state library)
  - source: user
- **F10** Which routing strategy should the React frontend use?
  - answer: Keep current hash-anchor navigation; no router
  - source: user

## ⚙️ Backend

- **B1** What backend framework target should apply while the frontend is converted?
  - answer: Keep current Spring Boot 4.1.1 unchanged
  - source: user
- **B2** How should the existing employee and leave-request API contracts be handled?
  - answer: must preserve
  - source: user
- **B3** What database or data migration approach should be used for this frontend-only conversion?
  - answer: no migration
  - source: user
- **B4** How should authentication and authorization be handled?
  - answer: Keep current demo policy unchanged; authentication remains out of scope
  - source: user
- **B5** Are there latency, throughput, availability, or other service-level targets to preserve?
  - answer: match current production baseline; no regression
  - source: user

## 📋 General

- **G1** What outcome will define success for the React conversion?
  - answer: feature parity with the current system; all existing user-facing behaviors preserved
  - source: user
- **G2** Which areas must remain untouched by this frontend conversion?
  - answer: Backend API, database/schema, and deployment configuration
  - source: user
- **G3** What must-pass policy should apply to the existing tests during the conversion?
  - answer: must pass
  - source: user
- **G4** Where should the React frontend code be created?
  - answer: new sibling directory: leave-management-new
  - source: user
- **G5** List any additional requirements, exclusions, dependencies, compliance rules, or operational constraints not covered above.
  - answer: None beyond the decisions listed in this specification.
  - source: user
