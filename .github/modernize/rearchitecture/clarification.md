---
schema: clarification/v1
generated_at: "2026-10-12T06:46:45Z"
scope:
  - frontend
  - backend
  - generic
clarity_score: 0.83
rounds: 1
gaps:
  - target.framework
  - target.component_library
  - visual.screenshots
blocking_gaps:
  - target.framework
  - target.component_library
  - visual.screenshots
---

# Scenario Clarification

## Frontend

- **Target framework**: React (version unspecified; using React 19 as a working assumption)
- **Component library**: None selected; retain existing CSS as a working assumption
- **Screenshots**: None supplied; use the existing HTML/CSS frontend as the visual reference
- **Design system**: Preserve current design using `frontend/style.css` tokens
- **Accessibility**: WCAG 2.1 AA
- **Browser targets**: Modern evergreen (latest two major versions of Chrome, Firefox, Safari, and Edge)
- **Responsive strategy**: Preserve current responsive behavior and breakpoints from `frontend/style.css`
- **i18n locales**: Preserve current locales and existing library, if any
- **State management**: Preserve existing pattern; use minimal component state
- **Routing**: Keep current hash-anchor navigation; no router

## Backend

- **Target framework**: Keep current Spring Boot 4.1.1 unchanged
- **API contract preservation**: Must preserve
- **Data migration strategy**: No migration
- **Auth framework**: Keep current demo policy unchanged; authentication remains out of scope
- **SLA targets**: Match current production baseline; no regression

## Generic

- **Success definition**: Feature parity with the current system; preserve all existing user-facing behavior
- **Out of scope**: Backend API, database/schema, and deployment configuration
- **Existing test posture**: Must pass
- **Output location**: New sibling directory `leave-management-new`
- **Additional constraints**: None beyond the decisions listed in this specification.

## Gaps & Defaults Applied

- id: target.framework
  resolution: blocking
  note: React was specified without a major version; React 19 is used as a working assumption.
- id: target.component_library
  resolution: blocking
  note: No component library was selected; the existing CSS is retained as a working assumption.
- id: visual.screenshots
  resolution: blocking
  note: No screenshots or recording were supplied; the current frontend source is used as the visual reference.

## Downstream Usage Notes

- Blocking gaps are recorded as risks. For this conversion, the implementation uses the working assumptions above and leaves the backend and deployment configuration unchanged.
