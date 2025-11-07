# CCPM Sync Status

Last full sync: 2025-11-07T23:42:27Z

## Synced Epics

### room-privacy-management
- GitHub Issue: #50
- Status: OPEN
- Local Epic: .claude/epics/room-privacy-management/epic.md
- Tasks: 0 (needs decomposition)
- Last Updated: 2025-11-07T11:09:57Z

### room-wishlist
- GitHub Issue: #18
- Status: OPEN
- Local Epic: .claude/epics/room-wishlist/epic.md
- Tasks: 0 (needs decomposition)
- Last Updated: 2025-10-21T02:46:20Z

## Local PRDs (Not Yet Synced as Epics)

The following PRDs exist locally but haven't been parsed into epics yet:
- daily-memo-app
- map-ui-enhancement
- memo-category-system
- profile-management

## Next Steps

1. To decompose existing epics into tasks:
   - `/pm:epic-decompose room-privacy-management`
   - `/pm:epic-decompose room-wishlist`

2. To sync new PRDs:
   - `/pm:prd-parse <prd-name>` (creates epic from PRD)
   - `/pm:epic-decompose <epic-name>` (creates tasks)
   - `/pm:epic-sync <epic-name>` (pushes to GitHub)

3. Or use one command:
   - `/pm:epic-oneshot <prd-name>` (parse + decompose + sync)
