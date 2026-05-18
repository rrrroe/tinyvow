# Tiny Vow Architecture Notes

This document records the current implementation boundaries for future human and AI agents. Keep it aligned with code when changing core flows.

## Core Boundaries

- Tiny Vow is local-first. User groups, rewards, custom themes, usage archives, points, block events, and activation state remain local unless a feature explicitly documents otherwise.
- `AppLimitAccessibilityService` owns foreground app detection, soft block overlay display, and a share of ENCOURAGE point settlement. Do not expand it into input capture, screen scraping, or unrelated automation.
- `GroupLimitEnforcer` owns real-time CONTROL limit evaluation. Real-time blocking means any positive overrun blocks immediately; the five-minute grace only applies to archived/statistical completion.
- `DailyArchiveRepository` owns historical facts. Existing group/app snapshots for an archived date should be reused when refreshing old archives so later group edits do not rewrite history.
- `AppLimitRepository` is the current facade for groups, rewards, inventory, reward effects, and achievements. New business rules should still be testable outside Compose before being called from UI.

## Data And Migration Rules

- Current Room database version is `19`; exported schemas live under `app/schemas/com.rrrrz.tinyvow.data.db.AppDatabase`.
- Entity/DAO/schema changes require a version bump, a `Migration` from the previous version, registration in `Room.databaseBuilder(...).addMigrations(...)`, updated exported schema JSON, and migration tests.
- Preserve user data by default: groups, cross refs, point ledger, redemption history, archives, custom themes, and activation state should not be dropped or rewritten without a documented recovery path.
- Soft-delete semantics are part of the model for groups and group-app relationships. Do not replace them with physical deletes unless every historical reference is accounted for.

## Performance Hot Paths

- Foreground app switching is latency sensitive. Keep database reads batched, UsageStats calls cached or aggregated, and overlay work on the main thread only when actually showing/removing UI.
- UsageStats queries are expensive. Prefer querying a period once and summing packages in memory over looping `queryAndAggregateUsageStats` per package.
- Compose screens should not perform large business calculations inline. Move reusable report, reward, and entitlement calculations into repository/domain helpers that can be unit tested.

## UI And Localization

- User-facing text in Compose, Canvas, notifications, services, dialogs, snackbars, and content descriptions must use `app_texts.xml` resources or `AppText`.
- User data is not translated: custom group names, custom rewards, custom theme names, installed app labels, and historical snapshots remain as entered or captured.
- Built-in content uses stable keys such as reward `builtinKey`, achievement requirement keys, and theme IDs so display names can localize without mutating stored history.

## Channel And Entitlement Rules

- `googlePlay` uses package `com.rrrrz.tinyvow`, Google login, Play Billing, and Play subscription management.
- `china` uses package `com.rrrrz.tinyvow.cn`, local activation, and must not trigger Google login or Play Billing flows.
- UI code should check `ProEntitlementState.isProActive` and `ProFeatureGate` rather than branching directly on Play Billing or local activation details.
- Free users keep existing over-limit data, but cannot continue editing/saving beyond the active entitlement limits.

## Manual Verification Checklist

- First-run permission disclosures for Usage Access, Accessibility, notifications, battery whitelist, and autostart.
- Fast foreground switching, returning home, returning to Tiny Vow, opening an over-limit app, and redeeming time before reopening an app.
- ENCOURAGE point settlement from app switch and periodic ticker paths.
- Reward purchase, inventory use, point ledger entries, and target-group restrictions.
- Daily archive refresh, empty stats states, PRO-locked report sections, and share image generation.
- Theme and language changes, including restart behavior and overlay/share-image theme consistency.
