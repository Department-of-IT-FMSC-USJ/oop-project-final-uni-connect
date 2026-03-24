# UniConnect — Frontend Design System

> Platform: Mentoring & Student Guidance | Stack: Astro.js + Svelte
> Style target: Professional · Institutional · Modern SaaS Dashboard

---

## 1. Design Philosophy

**Core principle:** _Clarity builds trust. In an institutional platform, the UI should never compete with the content._

- Every visual decision earns its place: whitespace, color, weight, shadow
- Structure implies reliability — grids, alignment, hierarchy are the UI
- Calm confidence over startup energy — no gradients trying to "excite" users
- Designed for daily use: scannable, low-friction, keyboard-accessible
- Scalable from Day 1: consistent tokens, component-based, theme-ready

---

## 2. Visual Style Direction

| Axis     | Direction                                                      |
| -------- | -------------------------------------------------------------- |
| Layout   | Fixed left sidebar, content area, optional right panel         |
| Density  | Medium-compact — comfortable but information-dense             |
| Surfaces | White cards on light gray base, subtle borders                 |
| Depth    | Flat with lightweight elevation (shadow, not layers)           |
| Motion   | Minimal — only for state transitions and feedback              |
| Icons    | Lucide Icons (consistent stroke weight, no filled/outline mix) |
| Imagery  | Avatar/photo only — no decorative illustrations                |

---

## 3. Color System

### Base Palette (CSS Custom Properties)

```css
:root {
  /* --- Neutral Surface --- */
  --color-bg: #f5f6f8; /* Page background */
  --color-surface: #ffffff; /* Cards, panels, modals */
  --color-surface-2: #f0f1f3; /* Nested card bg, input bg */
  --color-border: #e2e4e9; /* Default border */
  --color-border-strong: #c8cbd4; /* Emphasis border, dividers */

  /* --- Typography --- */
  --color-text-primary: #1a1d23; /* Headings, primary content */
  --color-text-secondary: #52576a; /* Labels, secondary content */
  --color-text-muted: #8990a4; /* Placeholder, hints */
  --color-text-disabled: #b8bcca; /* Disabled states */
  --color-text-inverse: #ffffff; /* On dark/accent */

  /* --- Brand Accent (Single) --- */
  /* Deep Slate Blue — professional, calm, trustworthy */
  --color-accent: #2e4b8f; /* Primary accent */
  --color-accent-hover: #243d7a; /* Hover state */
  --color-accent-light: #eef1f9; /* Accent tint for backgrounds */
  --color-accent-subtle: #d6dcf0; /* Badge backgrounds, borders */

  /* --- Semantic Status --- */
  --color-success: #1a7a4a;
  --color-success-bg: #edfbf3;
  --color-success-border: #b3e6cc;

  --color-warning: #895d00;
  --color-warning-bg: #fff8e6;
  --color-warning-border: #ffd97a;

  --color-danger: #c0291f;
  --color-danger-bg: #fef0ee;
  --color-danger-border: #f5b9b5;

  --color-info: #1565a8;
  --color-info-bg: #eef5fd;
  --color-info-border: #b3d1f5;

  /* --- Special Role Colors (sidebar accent only) --- */
  --role-hod: #2e4b8f; /* HOD — primary accent */
  --role-academic: #1a7a4a; /* Academic Mentor — forest green */
  --role-industry: #895d00; /* Industry Mentor — amber */
  --role-student: #2e4b8f; /* Student — primary accent */
  --role-assistant: #52576a; /* Assistant — neutral slate */
}
```

### Usage Rules

- Accent color used ONLY on: primary button, active nav item, links, focus ring
- Never use accent as a background fill on large surfaces
- Role colors used only in the sidebar top bar accent strip (4px left border or top bar color)
- Max 3 colors visible on any single screen

---

## 4. Typography

### Font Stack

```css
:root {
  --font-sans: "Inter", "DM Sans", system-ui, -apple-system, sans-serif;
  --font-mono: "JetBrains Mono", "Fira Code", monospace;
}
```

**Load Inter from Google Fonts** — it is purpose-built for UI, has excellent legibility at small sizes, and reads as professional without feeling cold.

### Type Scale

```css
:root {
  --text-xs: 0.75rem; /* 12px — Meta, captions, table labels */
  --text-sm: 0.875rem; /* 14px — Body standard, form labels, sidebar items */
  --text-base: 1rem; /* 16px — Body emphasis, card descriptions */
  --text-lg: 1.125rem; /* 18px — Section headings */
  --text-xl: 1.25rem; /* 20px — Page section titles */
  --text-2xl: 1.5rem; /* 24px — Page headings */
  --text-3xl: 1.875rem; /* 30px — Dashboard stat numbers only */

  --font-normal: 400;
  --font-medium: 500;
  --font-semibold: 600;
  --font-bold: 700;

  --leading-tight: 1.25;
  --leading-normal: 1.5;
  --leading-loose: 1.75;
}
```

### Typography Rules

- Page titles: `--text-2xl`, `--font-semibold`, `--color-text-primary`
- Section headings: `--text-lg`, `--font-semibold`
- Card/panel titles: `--text-base`, `--font-semibold`
- Body text: `--text-sm`, `--font-normal`
- Labels: `--text-sm`, `--font-medium`, `--color-text-secondary`
- Captions/meta: `--text-xs`, `--color-text-muted`
- Stat numbers: `--text-3xl`, `--font-bold` (dashboard only)

---

## 5. Spacing System

Based on a 4px base unit:

```css
:root {
  --space-1: 0.25rem; /* 4px */
  --space-2: 0.5rem; /* 8px */
  --space-3: 0.75rem; /* 12px */
  --space-4: 1rem; /* 16px */
  --space-5: 1.25rem; /* 20px */
  --space-6: 1.5rem; /* 24px */
  --space-8: 2rem; /* 32px */
  --space-10: 2.5rem; /* 40px */
  --space-12: 3rem; /* 48px */
  --space-16: 4rem; /* 64px */
}
```

### Layout Spacing Conventions

- Page padding (content area): `--space-8` horizontal, `--space-6` top
- Card inner padding: `--space-5` or `--space-6`
- Form field gap: `--space-4`
- Section gap (between cards): `--space-6`
- Table row padding: `--space-3` vertical, `--space-4` horizontal
- Sidebar item padding: `--space-3` vertical, `--space-4` horizontal

---

## 6. Borders, Radius, Shadows

```css
:root {
  /* Border radius */
  --radius-sm: 4px; /* Badges, tags, small controls */
  --radius-md: 8px; /* Cards, inputs, buttons — primary use */
  --radius-lg: 12px; /* Modals, panels, larger surfaces */
  --radius-xl: 16px; /* Drawers, full panels */
  --radius-full: 9999px; /* Pills, avatars */

  /* Shadows — restrained, purposeful */
  --shadow-xs: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-sm: 0 1px 4px rgba(0, 0, 0, 0.07), 0 1px 2px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08), 0 1px 4px rgba(0, 0, 0, 0.04);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.1), 0 2px 8px rgba(0, 0, 0, 0.05);
  --shadow-xl: 0 16px 48px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(0, 0, 0, 0.06);
}
```

### Shadow Usage Rules

- Cards: `--shadow-sm` by default, `--shadow-md` on hover
- Modals/Drawers: `--shadow-xl`
- Dropdowns/Tooltips: `--shadow-lg`
- Page body: no shadow — use background contrast
- Input focus: `0 0 0 3px var(--color-accent-subtle)` (ring style)

---

## 7. Component Specifications

### 7.1 Buttons

```
Primary:   bg=accent, text=white, hover=accent-hover, radius=md
Secondary: bg=surface, border=border-strong, text=text-primary, hover=bg=surface-2
Ghost:     bg=transparent, text=text-secondary, hover=bg=surface-2
Danger:    bg=danger, text=white, hover=darken 10%
Link:      text=accent, no background, underline on hover

All buttons: font-medium, text-sm, padding: space-2 space-4
Height: 36px (default), 32px (sm), 40px (lg)
Disabled: opacity-40, cursor-not-allowed
Loading: spinner replaces or precedes label text, maintains width
```

### 7.2 Forms

```
Input:
  bg=surface, border=border, radius=md, padding: space-2 space-3
  height: 38px, text-sm, focus: border=accent + ring
  error state: border=danger, faint red bg tint

Label:   font-medium, text-sm, text-secondary, margin-bottom: space-1
Helper:  text-xs, text-muted, margin-top: space-1
Error:   text-xs, text-danger, margin-top: space-1

Select:  same as Input with a custom chevron icon
Textarea: same as Input, min-height: 96px, resize-y
Checkbox/Radio: custom styled, accent color when checked
File upload: dashed border card, hover accent-light bg, icon centered
```

### 7.3 Cards

```
Base Card:
  bg=surface, border=1px solid border, radius=md, shadow=shadow-sm
  padding: space-6

Stat Card (dashboard):
  padding: space-5 space-6
  label: text-xs, font-medium, text-muted, uppercase, letter-spacing: 0.06em
  number: text-3xl, font-bold, text-primary
  trend: text-xs, colored with green/red, flex with small arrow icon

Action Card (clickable):
  hover: shadow=shadow-md, border-color=border-strong, translateY(-1px)
  transition: 150ms ease

Section Card:
  includes header (title + optional action button), divider, body
```

### 7.4 Tables

```
Wrapper: card base, overflow-x-auto

Table:
  width: 100%, border-collapse: separate, border-spacing: 0

Header row:
  bg=surface-2, text-xs, font-semibold, text-muted, uppercase
  letter-spacing: 0.05em, padding row convention, sticky top

Body rows:
  border-bottom: 1px solid border, text-sm, text-primary
  hover: bg=surface-2

Cell alignment:
  Text: left
  Numbers/amounts: right
  Status/actions: center

Pagination:
  text-sm, prev/next buttons (secondary style), current page accent

Empty state:
  centered in the table area, soft icon, text-muted message
```

### 7.5 Badges / Status Labels

```
Shape: radius=sm, padding: 2px 8px, text-xs, font-medium

Variants:
  success:  text=success, bg=success-bg, border=success-border
  warning:  text=warning, bg=warning-bg, border=warning-border
  danger:   text=danger, bg=danger-bg, border=danger-border
  info:     text=info, bg=info-bg, border=info-border
  neutral:  text=text-secondary, bg=surface-2, border=border

Status dot option:
  4px circle before text, same color as text
  Used in tables for quick scanning

Session States:
  PENDING   → warning badge
  CONFIRMED → info badge
  ONGOING   → success badge with pulse dot
  COMPLETED → neutral badge
  CANCELLED → danger badge
```

### 7.6 Modals

```
Overlay: rgba(0,0,0,0.45), backdrop-blur: 4px (subtle only)
Panel:
  bg=surface, radius=lg, shadow=shadow-xl
  max-width: 480px (sm), 640px (md), 800px (lg)
  padding: space-6

Header: title (text-lg font-semibold) + close button (ghost icon)
Divider between header, body, footer
Footer: right-aligned actions, cancel (secondary) + confirm (primary/danger)

Animation: fade + scale from 0.97 to 1, 180ms ease-out
Dismiss: ESC key, click overlay
```

### 7.7 Navigation

#### Sidebar (Primary Navigation)

```
Width: 240px (expanded), 64px (collapsed icon-only)
bg=surface, border-right=border, height=100vh, position=fixed

Top section:
  Logo + Platform name (16px, font-semibold)
  Role indicator strip (4px left border, role-specific color)

Nav items:
  Icon (20px) + Label, text-sm, font-medium
  Active: bg=accent-light, text=accent, border-left=3px solid accent
  Hover: bg=surface-2
  padding: space-3 space-4, radius=sm (right side only)

Sections: grouped with tiny uppercase label (text-xs, text-muted)

Bottom section:
  Avatar + name + role label (text-xs)
  Settings link, Logout
```

#### Top Bar (Secondary)

```
height: 56px, bg=surface, border-bottom=border, shadow=shadow-xs
Contains: Page title (left), Notification bell + Avatar (right)
Breadcrumb (optional, for deep pages): text-sm, text-muted
```

---

## 8. Role-Based Dashboard Layouts

### 8.1 Dashboard Grid Patterns

All roles share the same shell: left sidebar + top bar + content area.
Content area uses a CSS grid layout that adapts per role.

**Stat row (top):** 4-column grid of stat cards (responsive: 2 on tablet, 1 on mobile)

**Content below:** 12-column grid with role-specific widget placement

---

### 8.2 Student Dashboard

```
┌─────────────────────────────────────────────────────┐
│  Stats Row: Sessions Attended | Points | Mentors | Due Tasks
├─────────────────────┬───────────────────────────────┤
│  Upcoming Sessions  │  My Mentors                   │
│  (next 3, list)     │  (assigned academic +         │
│                     │   industry mentor cards)      │
├─────────────────────┴───────────────────────────────┤
│  Recent Activity / Notifications (full width)        │
├─────────────────────┬───────────────────────────────┤
│  Study Materials    │  Points History               │
│  (recent uploads)   │  (mini leaderboard/log)       │
└─────────────────────┴───────────────────────────────┘
```

Sidebar nav items: Dashboard · My Mentors · Sessions · Materials · Points · Profile

### 8.3 Academic Mentor Dashboard

```
┌─────────────────────────────────────────────────────┐
│  Stats Row: Assigned Students | Sessions This Month | Pending | Avg Rating
├─────────────────────┬───────────────────────────────┤
│  My Students        │  Upcoming Sessions             │
│  (table: name,      │  (list with join action)       │
│   year, last seen)  │                               │
├─────────────────────┴───────────────────────────────┤
│  Pending Actions (feedback due, session notes due)   │
├─────────────────────┬───────────────────────────────┤
│  Session History    │  Student Alerts               │
│  (table, compact)   │  (at-risk, inactive flags)    │
└─────────────────────┴───────────────────────────────┘
```

Sidebar nav items: Dashboard · My Students · Sessions · Allocations · Contact Info · Profile

### 8.4 Industry Mentor Dashboard

```
┌─────────────────────────────────────────────────────┐
│  Stats Row: Students Mentored | Sessions | Rewards Given | Suggestions Submitted
├─────────────────────┬───────────────────────────────┤
│  My Students        │  Upcoming Sessions             │
├─────────────────────┴───────────────────────────────┤
│  Curriculum Suggestions (status tracker, table view) │
├─────────────────────┬───────────────────────────────┤
│  Reward Points Log  │  Session History               │
└─────────────────────┴───────────────────────────────┘
```

Sidebar nav items: Dashboard · My Students · Sessions · Suggestions · Rewards · Profile

### 8.5 HOD Dashboard

```
┌─────────────────────────────────────────────────────┐
│  Stats Row: Total Students | Mentors | Active Sessions | Pending Verifications
├─────────────────────┬───────────────────────────────┤
│  Mentor Verifications Pending │  Feedback Overview  │
│  (action-required list)       │  (avg ratings chart)│
├───────────────────────────────┴─────────────────────┤
│  Contribution Reviews (table — student, type, status)│
├─────────────────────┬───────────────────────────────┤
│  Session Overview   │  Curriculum Suggestions        │
│  (by status chart)  │  (recent, review button)       │
└─────────────────────┴───────────────────────────────┘
```

Sidebar nav items: Dashboard · Mentor Verification · Feedback · Contributions · Sessions · User Directory · Profile

### 8.6 Assistant Dashboard

```
┌─────────────────────────────────────────────────────┐
│  Stats Row: Students | Notifications Sent | Open Tasks | Sessions Today
├─────────────────────────────────────────────────────┤
│  System Notifications (send/manage, full width)      │
├─────────────────────┬───────────────────────────────┤
│  User Directory     │  Recent System Activity        │
│  (search+table)     │  (log feed)                   │
└─────────────────────┴───────────────────────────────┘
```

---

## 9. User Flow Recommendations

### 9.1 Student Sign-Up & Onboarding

```
Landing Page (/) → [Login | Register]
  ↓ Register
Step 1: Basic Info (name, MC number, CPM number)
  → email auto-derived from MC number (@mgt.sjp.ac.lk)
Step 2: Academic Details (department, year of study)
Step 3: Profile Picture upload
Step 4: Password creation
  ↓ Submit
→ Dashboard (PENDING allocation state — banner: "Your mentor will be assigned soon")
```

**Design note:** Multi-step form with a top progress indicator (numbered steps, not a full stepper). Each step is a single focused screen. Validation inline, not on submit only.

### 9.2 Session Scheduling Flow

```
Sessions Page → [+ Schedule Session] button (primary, top right)
  ↓
Modal opens:
  1. Select mentor (dropdown, if multiple)
  2. Proposed date + time (date picker + time selector)
  3. Topic / Agenda (textarea, optional)
  4. Session type (Online / In-person)
  ↓ Confirm
→ Row added to table: status=PENDING
→ Toast: "Session request sent to [Mentor name]"

Mentor side → notification → confirm/decline → student notified
```

### 9.3 Join Session Flow

```
Upcoming Sessions card / Sessions table
→ Row with status=CONFIRMED and time within 15 min:
  [Join Session] button becomes active (accent, enabled)
  → Opens meeting link or in-platform call page
  → Session status transitions to ONGOING
→ After session ends:
  → Prompt to submit feedback (modal or dedicated page)
→ Status → COMPLETED
```

### 9.4 Mentor Profile View (Student's perspective)

```
My Mentors page → Click mentor card
→ Profile panel (slide-in right drawer or dedicated page)
  - Avatar, name, designation, department
  - Specializations (tag list)
  - Stats: sessions completed, avg rating
  - Session history with this student
  - Contact info (if disclosed)
  - [Send Message] button → DirectMessage modal
  - [Request Session] button → Session scheduling modal
```

### 9.5 Feedback Submission Flow

```
After session completes → notification badge on nav
→ Feedback page or modal:
  - Session reference (read-only header)
  - Star rating (1–5, required)
  - Category ratings: Communication · Helpfulness · Punctuality
  - Comments (textarea, optional)
  - [Submit Feedback] → toast confirmation
```

### 9.6 Recommendations View

```
Mentors page → "Recommended for you" section (top of page)
→ Cards per recommendation:
  - Mentor name, specialization, match score (optional)
  - [Connect] or [View Profile] CTA
  - Reasoning tag: e.g., "Based on your year + engagement"
```

---

## 10. Component Architecture (Astro.js + Svelte)

### 10.1 File Structure

```
frontend/
├── src/
│   ├── layouts/
│   │   └── Layout.astro          # Base HTML shell, font imports
│   │
│   ├── components/
│   │   ├── shell/
│   │   │   ├── DashboardShell.svelte   # Sidebar + TopBar + content slot
│   │   │   ├── Sidebar.svelte
│   │   │   ├── TopBar.svelte
│   │   │   └── SidebarItem.svelte
│   │   │
│   │   ├── ui/                         # Primitive components
│   │   │   ├── Button.svelte
│   │   │   ├── Input.svelte
│   │   │   ├── Select.svelte
│   │   │   ├── Textarea.svelte
│   │   │   ├── Badge.svelte
│   │   │   ├── Avatar.svelte
│   │   │   ├── Card.svelte
│   │   │   ├── StatCard.svelte
│   │   │   ├── Modal.svelte
│   │   │   ├── Drawer.svelte
│   │   │   ├── Toast.svelte / ToastViewport.svelte
│   │   │   ├── Tooltip.svelte
│   │   │   ├── Spinner.svelte
│   │   │   ├── Divider.svelte
│   │   │   ├── EmptyState.svelte
│   │   │   └── ConfirmDialog.svelte
│   │   │
│   │   ├── data/                        # Data display components
│   │   │   ├── DataTable.svelte         # Generic sortable/paginated table
│   │   │   ├── TableRow.svelte
│   │   │   ├── Pagination.svelte
│   │   │   └── FilterBar.svelte
│   │   │
│   │   ├── forms/                       # Form composites
│   │   │   ├── FormField.svelte         # Label + Input + Error wrapper
│   │   │   ├── ProfilePictureUpload.svelte
│   │   │   ├── StarRating.svelte
│   │   │   └── StepIndicator.svelte
│   │   │
│   │   ├── sessions/                    # Session-specific components
│   │   │   ├── SessionCard.svelte
│   │   │   ├── SessionTable.svelte
│   │   │   ├── ScheduleSessionModal.svelte
│   │   │   ├── JoinSessionButton.svelte
│   │   │   └── SessionStatusBadge.svelte
│   │   │
│   │   ├── mentor/
│   │   │   ├── MentorCard.svelte
│   │   │   ├── MentorProfileDrawer.svelte
│   │   │   └── RecommendationCard.svelte
│   │   │
│   │   └── shared/
│   │       ├── LandingPage.svelte
│   │       ├── FeedbackForm.svelte
│   │       ├── NotificationFeed.svelte
│   │       └── DirectMessageModal.svelte
│   │
│   ├── pages/                           # Astro pages per role
│   │   ├── index.astro                  # Landing / auth gate
│   │   ├── student/
│   │   ├── academic-mentor/
│   │   ├── industry-mentor/
│   │   ├── hod/
│   │   └── assistant/
│   │
│   ├── lib/
│   │   ├── api.js                       # Fetch wrapper, auth header
│   │   ├── auth.js                      # getCurrentUser, getRolePath
│   │   ├── navigation.js                # Role-based nav configs
│   │   ├── toast.js                     # Toast state store
│   │   └── stores.js                    # Svelte writable stores
│   │
│   └── styles/
│       ├── global.css                   # CSS variables + resets
│       ├── typography.css
│       └── utilities.css               # Minimal utility classes
```

### 10.2 DashboardShell Pattern

Each role's pages use a single `DashboardShell.svelte` wrapper:

```svelte
<!-- DashboardShell.svelte -->
<script>
  export let role = 'STUDENT';
  export let pageTitle = '';
</script>

<div class="shell">
  <Sidebar {role} />
  <div class="main">
    <TopBar {pageTitle} />
    <main class="content">
      <slot />
    </main>
  </div>
</div>
```

Then in each `.astro` page:

```astro
---
import Layout from '../../layouts/Layout.astro';
import DashboardShell from '../../components/shell/DashboardShell.svelte';
import StudentDashboard from '../../components/student/Dashboard.svelte';
---
<Layout>
  <DashboardShell role="STUDENT" pageTitle="Dashboard" client:load>
    <StudentDashboard client:load />
  </DashboardShell>
</Layout>
```

### 10.3 Navigation Config (navigation.js)

```js
export const NAV_CONFIG = {
  STUDENT: [
    {
      label: "Dashboard",
      href: "/student/dashboard",
      icon: "layout-dashboard",
    },
    { label: "My Mentors", href: "/student/mentors", icon: "users" },
    { label: "Sessions", href: "/student/sessions", icon: "calendar" },
    { label: "Materials", href: "/student/materials", icon: "book-open" },
    { label: "Points", href: "/student/points", icon: "star" },
  ],
  ACADEMIC_MENTOR: [
    {
      label: "Dashboard",
      href: "/academic-mentor/dashboard",
      icon: "layout-dashboard",
    },
    {
      label: "My Students",
      href: "/academic-mentor/students",
      icon: "graduation-cap",
    },
    { label: "Sessions", href: "/academic-mentor/sessions", icon: "calendar" },
    {
      label: "Allocations",
      href: "/academic-mentor/allocations",
      icon: "git-branch",
    },
  ],
  INDUSTRY_MENTOR: [
    {
      label: "Dashboard",
      href: "/industry-mentor/dashboard",
      icon: "layout-dashboard",
    },
    { label: "My Students", href: "/industry-mentor/students", icon: "users" },
    { label: "Sessions", href: "/industry-mentor/sessions", icon: "calendar" },
    {
      label: "Suggestions",
      href: "/industry-mentor/suggestions",
      icon: "lightbulb",
    },
    { label: "Rewards", href: "/industry-mentor/rewards", icon: "gift" },
  ],
  HOD: [
    { label: "Dashboard", href: "/hod/dashboard", icon: "layout-dashboard" },
    {
      label: "Mentor Verification",
      href: "/hod/mentor-verification",
      icon: "shield-check",
    },
    { label: "Feedback Review", href: "/hod/feedback", icon: "message-square" },
    { label: "Contributions", href: "/hod/contributions", icon: "award" },
    { label: "Sessions", href: "/hod/sessions", icon: "calendar" },
    { label: "User Directory", href: "/hod/users", icon: "book-user" },
  ],
  ASSISTANT: [
    {
      label: "Dashboard",
      href: "/assistant/dashboard",
      icon: "layout-dashboard",
    },
    { label: "Notifications", href: "/assistant/notifications", icon: "bell" },
    { label: "User Directory", href: "/assistant/users", icon: "users" },
  ],
};
```

---

## 11. Visual Premium Patterns

These are the micro-decisions that separate a polished product from a generic one:

### 11.1 Stat Cards — Subtle Left Border Accent

```css
.stat-card {
  border-left: 3px solid var(--color-accent);
}
.stat-card.warning {
  border-left-color: var(--color-warning);
}
.stat-card.success {
  border-left-color: var(--color-success);
}
```

### 11.2 Table Row Hover — Barely There

```css
tr:hover td {
  background: var(--color-surface-2);
}
```

### 11.3 Focus Ring — Consistent Everywhere

```css
:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px var(--color-accent-subtle);
}
```

### 11.4 Empty State — Informative, Not Decorative

```
Icon: Lucide icon (64px, text-muted color)
Heading: "No sessions yet" (text-lg, font-semibold, text-secondary)
Body: "Schedule your first session with your mentor." (text-sm, text-muted)
CTA button: if action is available
```

### 11.5 Page Transitions — Svelte's Native

```js
import { fade } from "svelte/transition";
// Each page main content: <main in:fade={{ duration: 150 }}>
```

### 11.6 Skeleton Loading

Use CSS skeleton loaders (animated shimmer) for all async content — never a full-page spinner. Cards and table rows get skeleton versions at the same dimensions as real content.

### 11.7 Toast Notifications

```
Position: bottom-right, stacked upward
Success: left border accent = success green
Error:   left border accent = danger red
Info:    left border accent = accent blue
Duration: 4 seconds auto-dismiss
Max: 3 visible at once
```

---

## 12. Implementation Guidance

### Package Recommendations

```json
{
  "dependencies": {
    "astro": "latest",
    "@astrojs/svelte": "latest",
    "svelte": "latest",
    "lucide-svelte": "latest"
  }
}
```

**Why Lucide:** Consistent 24px stroke-based icons, tree-shakable, maintained, used by Linear/Vercel. No mixing filled vs outline variants.

**No CSS framework required.** Use CSS custom properties + scoped Svelte styles. This gives you full control, zero bloat, and avoids fighting Tailwind's reset in Svelte scoped contexts.

If you prefer utility classes, use **UnoCSS** with a minimal preset — much lighter than Tailwind and plays better with Astro.

### Global CSS Entry Point

```css
/* global.css — imported in Layout.astro */
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap");

*,
*::before,
*::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  color: var(--color-text-primary);
  background: var(--color-bg);
  line-height: var(--leading-normal);
  -webkit-font-smoothing: antialiased;
}

a {
  color: var(--color-accent);
  text-decoration: none;
}
a:hover {
  text-decoration: underline;
}

img {
  max-width: 100%;
  display: block;
}
```

### Auth Guard Pattern (Astro SSR or Client-Side)

```astro
---
// In each page's frontmatter or a shared middleware:
const user = Astro.cookies.get('user')?.json();
if (!user || user.role !== 'STUDENT') {
  return Astro.redirect('/');
}
---
```

### API Layer Conventions

```js
// lib/api.js
const BASE = "http://localhost:8080/api";

async function request(path, options = {}) {
  const token = localStorage.getItem("token");
  const res = await fetch(`${BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    },
    ...options,
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw Object.assign(new Error(err.message || "Request failed"), {
      data: err,
      status: res.status,
    });
  }
  return res.status === 204 ? null : res.json();
}

export const api = {
  get: (path) => request(path),
  post: (path, body) =>
    request(path, { method: "POST", body: JSON.stringify(body) }),
  put: (path, body) =>
    request(path, { method: "PUT", body: JSON.stringify(body) }),
  delete: (path) => request(path, { method: "DELETE" }),
};
```

---

## 13. Scalability Notes

- All CSS values reference custom properties — retheming is one file change
- Role colors are tokens — adding a new role is a one-line token addition
- Navigation config is data-driven — adding a page is a single array entry
- `DataTable.svelte` should accept a `columns` config array — never hard-code table headers
- `Badge.svelte` should accept a `variant` prop — never hard-code colors from outside
- Design with 4 roles now, but the shell supports N roles without structural change

---

_Document version 1.0 — UniConnect Frontend Design System_
_To be implemented in Astro.js + Svelte_
