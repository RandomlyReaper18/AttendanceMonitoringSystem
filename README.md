# ATTENDANCE MONITORING

The Attendance Monitoring System is a Java Swing desktop application developed to simplify and automate attendance tracking. It provides a secure login and registration system for users while allowing administrators to monitor attendance records in real time. User accounts are stored in a JSON file, and attendance data is automatically recorded whenever a user logs in or logs out.

The application includes an administrator dashboard that displays live attendance information, the current date and time, and the number of users currently logged in. It also provides search and user management functions, enabling administrators to efficiently monitor and manage attendance records. By replacing manual attendance recording with a digital system, the application improves accuracy, organization, and efficiency in managing attendance data.

BY randomlyreaper18/PRINCE SANEL B. OSORIO

#(ILOVENATHALIA)


# Student Attendance Management System

A Java Swing desktop app for managing student attendance: students log in/out to record attendance, an administrator manages accounts and reviews records, and teachers can bulk-create a whole class's accounts at once from a pasted name list.

---

## Requirements

- **Java 17+** (uses switch expressions)
- **Gson** library on the classpath (used for JSON persistence)
- NetBeans (optional — any IDE or `javac`/`java` on the command line works too)

---

## Running the App

1. Set the project's **Main Class** to `com.mycompany.login.MainFrame`.
2. Make sure these resources exist on the classpath:
   - `/com/mycompany/login/images/Icon.png` — window/taskbar icon
   - `/com/mycompany/login/images/signal.jpg` — login/signup background photo
3. Run it. The app opens **fullscreen (maximized)** automatically.

**Default administrator login:**
```
Username: admin
Password: admin123
```
(Hardcoded in `UserManager.adminLogin()` — not a stored account.)

---

## Architecture

The app is a **single `JFrame`** (`MainFrame`) whose content switches between screens using `CardLayout`, instead of opening separate windows per screen. This avoids window flicker/repositioning and keeps sizing logic in one place.

```
MainFrame
 ├── LoginPanel        (card: "login")
 ├── AdministratorPanel (card: "admin")
 └── StudentInfoFormPanel (card: "studentForm")
```

Navigate between them via `mainFrame.showCard(MainFrame.CARD_XXX)`. Each panel has an `onShow()` method, called automatically right before it becomes visible, which refreshes its data.

There is **no Sign Up screen** — students don't self-register. Accounts are created in bulk by a teacher/admin via the Student Info Form.

---

## Screens

### LoginPanel
- Student login (username/password) and hidden admin login (routes to Administrator on success).
- Live table of today's attendance.
- Mini stat cards: Total Students, Present Today, Absent Today, Late Today.
- On a **Late** login, immediately asks "why are you late?" (once per day).
- On login, if the student has attendance history but is missing a record for the last school day (weekends skipped), asks for an excuse letter (once per missed day). Neither prompt can block login — a blank/cancelled answer is logged as `(No reason provided)` / `(No excuse provided)`.

### AdministratorPanel
Reached by logging in with the admin credentials above.

- **Stat cards**: Today's Attendance, Present, Absent, Late, Registered Users.
- **Search bar** — filters by username or name (searches across both Attendance records and registered Users).
- **Tabs:**
  - **Attendance** — live table of today's records, auto-refreshes every second.
  - **Manage Users** — table of all registered students. Right-click a row for:
    - **Reset Password** — generates a new strong password, shown once (passwords are hashed, so they can never be displayed again after creation).
    - **View Attendance Calendar** — opens a month-view calendar for that student (see below).
    - **Edit User** — change username/name, optionally set a new password (leave blank to keep the current one).
    - **Delete User** — removes the account (their past attendance history is kept).
  - **Reason Log** — every late-reason and absence-excuse answer ever submitted, newest first.
- **Log Out User** — force-logs-out whoever is selected in the Attendance tab.
- **Add Students** button → opens the Student Info Form.
- **Login** button → returns to the Login screen.

### StudentInfoFormPanel ("Add Students")
Bulk-creates a class's worth of accounts:

1. Enter **Section** and **Grade**.
2. Paste student names — **one full name per line** (e.g. copied from a single Excel column). First word = first name, everything after = last name (so "Dela Cruz" style last names stay together).
3. Enter **No. of Students / Girls / Boys**. Both of these are validated before anything is generated:
   - Girls + Boys must equal No. of Students.
   - The number of successfully parsed names must also equal No. of Students.
4. Click **GENERATE ACCOUNTS**. Lines that can't be parsed (single word, stray junk) are skipped and listed by name rather than blocking the whole batch.
5. Review the generated **username / password / full name** table (nothing is saved yet).
   - Usernames: `firstname.lastname`, lowercased, auto-numbered on collision (`juan.delacruz`, `juan.delacruz2`, ...).
   - Passwords: random 4-digit PINs.
6. **Copy List to Clipboard** — exports as tab-separated text (pastes back into Excel).
7. **Save & Register All** — actually creates the accounts and hands the batch off to `sendSectionToApi()`.

> **`sendSectionToApi()` is currently a stub.** It just prints what it would send to the console. The real API (parameter-based, not REST, per your own design) isn't wired up yet — swap in the real HTTP call once the endpoint/parameter contract is defined.

### AttendanceCalendarDialog
A month-grid popup for one student, opened from Manage Users:
- 🟩 Green = Present · 🟧 Orange = Late · 🟥 Red = unexplained weekday absence · ⬜ Gray = weekend/future/no data
- Today's cell is outlined in blue.
- `‹ Prev` / `Next ›` to browse other months.

---

## Data Storage

No external database — everything is local JSON, saved **next to the running app** (not an unpredictable working directory), with **atomic writes** (temp file + atomic rename) so a crash mid-save can't corrupt the file.

| File | Managed by | Contents |
|---|---|---|
| `users.json` | `UserManager` | Registered accounts (username, hashed password, full name) |
| `attendance.json` | `AttendanceManager` | Daily login/logout records. Auto-deduplicated (one row per user per day) and sorted by date on every load. |
| `reason_log.json` | `ReasonLogManager` | Late-reason and absence-excuse answers |

---

## Security Notes

- **Passwords are hashed** using salted PBKDF2 (`PasswordHasher`, JDK-only, no extra dependencies) — not stored in plain text.
- ⚠️ **Migration note:** any accounts created *before* hashing was added have plain-text passwords in `users.json` and will fail to log in under the new scheme (they don't parse as a valid hash). Either delete `users.json` and start fresh, or reset each old account's password individually via Admin → Manage Users → Reset Password.
- Because passwords are hashed, they can never be *displayed* again after creation — only reset to a new one.
- The hardcoded admin login (`admin` / `admin123`) is a simple literal comparison, not a stored/hashed account.

---

## Known Limitations / Possible Next Steps

- **API integration** — `sendSectionToApi()` is a stub. Needs the real endpoint URL and parameter names to complete.
- **Teacher-facing website** — entirely separate from this app; nothing here depends on it existing yet.
- **Absence detection** is simplified: it checks only the single most recent school day (skipping weekends), not school holidays or multi-day gaps.
- **Name parsing** assumes `Firstname Lastname` order. Names in `Lastname, Firstname` format would need a different parsing rule.

---

## File Reference

| File | Purpose |
|---|---|
| `MainFrame.java` | Single window; hosts all screens via `CardLayout` |
| `LoginPanel.java` | Login screen, live attendance table, stat cards, late/absence prompts |
| `AdministratorPanel.java` | Admin dashboard: stats, Attendance/Manage Users/Reason Log tabs |
| `StudentInfoFormPanel.java` | Bulk student account creation from a pasted name list |
| `AttendanceCalendarDialog.java` | Monthly attendance calendar for one student |
| `BackgroundPanel.java` | Auto-scaling, auto-enhanced background image panel |
| `TranslucentCardPanel.java` | Semi-transparent white card for readable content over a photo |
| `StatMiniCard.java` | Small stat display card (title, colored bar, big number) |
| `AttendanceManager.java` | Attendance JSON persistence — atomic writes, dedupe, sort |
| `UserManager.java` | User JSON persistence — atomic writes, register/login/update/delete |
| `PasswordHasher.java` | Salted PBKDF2 password hashing |
| `ReasonLogManager.java` | Late-reason / absence-excuse JSON persistence |
| `LogEntry.java` | Model for one Reason Log answer |
