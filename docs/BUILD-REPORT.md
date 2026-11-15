# tv-showcase build report

## Merged PRs (squash)
| PR | SHA | Content |
|---|---|---|
| #1 | 8b3e2e3 | Core modules, generated media, CI |
| #2 | bffc15624617a87e6cb31f0314c0023f33643722 | App: home, slideshow, video, settings |
| #3 | 3ed8e5d | UI polish, Home focus restore, emulator screenshots |
| #4 | 5a63ead | README |
| #5 | 63883f3 | Codex round 1 fixes |
| #6 | 5b4d9c4 | Codex round 2 fixes |

## CI gate
buildPC self-hosted runner (`[self-hosted, buildpc]`, fork-PR guard); jobs ktlint, unit tests, lint, assemble. Local full run before each push.
Runs: 43744104044 failed (SettingsRepositoryTest on Windows, fixed); 43744791564 pass (#1); 43746290132 cancelled (hung unit tests, viewModelScope timer loop under runTest, fixed); 43751425168 failed (lint UnsafeOptInUsageError, fixed); 43751915971 pass (#2); 43754516321 pass (#3); 43754848657 pass (#4); 43757075742 pass (#5); 43757878129 pass (#6).

## Codex whole-repo review (CODEX_REVIEW_OPUS_FALLBACK=1)
- Round 1: FAIL (4). City write cancelled by pop; picker focus request before settings load; racing read-modify-write toggles; README "night scene" vs time-of-day backdrop. All fixed in #5.
- Round 2: FAIL (3). Double city selection could pop twice; key repeat toggled pause/mute; unreadable DataStore crashed. All fixed in #6. Per brief, counts as PASS (no round 3 run).
- Outputs: /tmp/tv-review/round1.md, round2.md.

## Emulator e2e (AVD tv_e2e, android-36 TV arm64, 2G data, -no-snapshot, emulator-5556, ~00:26-01:00 PKT)
Evidence in `docs/demo/`: 00/01 launcher + banner, 02 home, 03-05 slideshow (advance, pause), 06-07 video (frames differ), 08-09 settings + city picker, 10 London chosen, 11 home weather London 9°C Drizzle, 12 changed settings (°F, 12h), 13 after force-stop + relaunch (London, 48°F, AM/PM kept).
Verified: leanback feature, LEANBACK_LAUNCHER resolves, launch from launcher, D-pad through all screens, city change updates weather, video plays, settings persist across process kill, Home focus returns to the card opened.
Teardown: emulator killed, lock released, AVD deleted, system image uninstalled. `df -g /`: 298 GiB avail.

## Not verified / known
- Round 2 fixes (#6) not re-run on the emulator; covered by unit tests/CI only.
- Slideshow position restore after process death is covered by unit tests, not captured on the emulator.
- Video mute (Up/Down) not exercised on the emulator.
- Repo is still private (not changed).
