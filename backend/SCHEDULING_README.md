# Scheduling Module (Provider-Agnostic)

This module extends backend scheduling under `com.uniconnect.scheduling` while preserving existing architecture and auth.

## Core capabilities

- Internal calendar source of truth (`session_events`, `session_participants`)
- Full session lifecycle:
  - `CREATED`
  - `SCHEDULED`
  - `CANCELLED`
  - `COMPLETED`
  - `MISSED`
- Provider abstraction via `MeetingProvider`
- Real Jitsi provider support (`JITSI`) + default provider configuration
- Domain events for created/updated/cancelled/reminder notifications
- Reminder hooks prepared for `InAppNotificationService` and `EmailNotificationService`

## Meeting providers

Provider selection is configuration-driven:

- `uniconnect.scheduling.meeting.default-provider=JITSI` (or `INTERNAL_DEFAULT`)
- `uniconnect.scheduling.meeting.jitsi-base-url=https://meet.jit.si`
- `uniconnect.scheduling.meeting.jitsi-room-prefix=uniconnect`

To add another provider (Teams/Meet/custom), implement `MeetingProvider` and map a new `MeetingProviderType`.

## API endpoints

- `POST /api/scheduling/sessions`
- `PUT /api/scheduling/sessions/{sessionId}`
- `PUT /api/scheduling/sessions/{sessionId}/reschedule`
- `PATCH /api/scheduling/sessions/{sessionId}/status`
- `POST /api/scheduling/sessions/{sessionId}/cancel`
- `GET /api/scheduling/sessions/{sessionId}`
- `GET /api/scheduling/sessions/details/{sessionId}`
- `GET /api/scheduling/sessions/{sessionId}/join`
- `GET /api/scheduling/sessions/{sessionId}/ics`
- `GET /api/scheduling/calendar?from=...&to=...&userId=...`
- `GET /api/scheduling/sessions/upcoming`
- `GET /api/scheduling/sessions/my`
- `GET /api/scheduling/sessions/mentor/{mentorUserId}`
- `GET /api/scheduling/sessions/student/{studentUserId}`
- `GET /api/scheduling/sessions/list`

## Frontend-ready response fields

Session responses include:

- `id`, `title`, `topic`, `description`
- `start`, `end` (calendar-friendly)
- `startsAt`, `endsAt` (explicit aliases)
- `status`
- `joinUrl` and `meetingJoinUrl` (compatibility)
- `canJoin`
- `participants`

## Lifecycle behaviors

- `CANCELLED` sessions cannot be joined
- `COMPLETED` and `MISSED` sessions are closed for joining
- Past `CREATED`/`SCHEDULED` sessions are auto-marked as `MISSED`
- Overlapping mentor or student sessions are blocked with clear validation errors
