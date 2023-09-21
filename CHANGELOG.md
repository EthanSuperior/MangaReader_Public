## Future Versions

- Redone Edit menu to have hamburger menu (...)
- Add quick menu for update page to Mark as Read, Refresh, Edit, Snooze, etc
- Add progress bar for refreshing
- Add ability to modify UserAgents
- Redo information for sharing and the share button
- Add used-defaults flag/bitset for selector/sourceURL
- Add using link text for ch# if over 10k
- Modify Snooze Dialog default chapter snooze to work properly with .5 chapters and 103_2, 
should suggest 104 as next to calculate snoozing

# Version 1.9.1 \[17]

- Reverted changes made in 1.9.0 to fix low number marking error due to glitchiness
- Added a new fix using try-catch and ChapterManager.UpdateAll()
- Made Hiatus properly re-snooze
- Added additional save parameter for save date to keep track of un-snoozed date.
- Added second highlight color for things coming off of snooze for viewing clarity
- Flip colors for snooze and update to make them more visible
- Modified penguin to remove bad vector strokes
- Fixed Search bar from skipping first character typed
- Allowed searching within urls of chapters

# Version 1.9.0 \[16]

- Added penguin visible when no new updates are found
- Added secret messages, throughout the code
- Fixed error caused by marking or removing things from the list when there is low number
- Made un-snoozed chapter be highlighted in updates
