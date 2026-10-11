# Fossify Launcher fork: App Drawer Tabs

## Goal
Fork https://github.com/FossifyOrg/Launcher and add **one feature only**: tabs in the app drawer to organize apps. Do not change, remove, or restyle anything else. Keep the diff self-contained so upstream Fossify changes can be merged later.

The reference design is **NeoLauncher**'s drawer tabs (also on GitHub, "Horizontal Tabs" layout mode). Match its behavior, but use Fossify's existing look (colors, fonts, corner radius, theming).

## 1. Drawer (Image: reference/drawer.jpg)
- A horizontal row of pill-shaped tab buttons sits directly **below the search bar**, above the app grid.
- The row scrolls sideways when tabs don't fit.
- **All** is always the first tab and is selected when the drawer opens.
- The selected tab is filled with the accent color; the others use a darker surface color.
- Tapping a tab filters the grid to that tab's apps, keeping the existing sort order (A→Z).
- Optional nice-to-have: swipe left/right on the grid to switch tabs.
- **Search** always searches all apps, regardless of the selected tab.
- If there are no custom tabs, hide the tab row entirely, so the drawer looks exactly like stock Fossify.

## 2. Settings (Image: reference/drawer-settings.jpg)
- Add a **Manage Tabs** item to Fossify's existing drawer settings section, with the subtitle "Create or edit tabs".

## 3. Manage Tabs screen (Image: reference/manage-tabs.jpg)
- A header hint reads: "Tap to edit, drag to reorder."
- Show a list of tab cards. Each card shows the tab name and its app count (e.g. "Social  13 apps").
- **All** card: always first. It can't be deleted, renamed, or moved, and it shows the hidden-app count if Fossify tracks hidden apps.
- **Custom tab cards** have:
  - a **trash** icon to delete the tab, with a confirmation dialog (deleting a tab never uninstalls or hides apps);
  - a **+** icon that opens an app picker (a checklist of all apps, searchable) to choose the tab's apps;
  - tap the card to rename it;
  - drag a card to reorder it (the All card stays pinned at the top).
- A floating **+** button creates a new tab (name dialog, then the app picker).
- Skip NeoLauncher's "Second Tab for Work Apps" toggle; it isn't needed.

## 4. Assigning from the drawer
- Add "Add to tab…" to an app's long-press menu in the drawer, opening a checklist of tabs.

## 5. Data and behavior
- Store tabs in Fossify's existing Room database: a `tabs` table (id, name, position) and a `tab_apps` table (tab_id, package name + activity name). Add a proper migration so existing installs keep their data.
- An app can belong to **multiple tabs**.
- Newly installed apps go to **All** only.
- Uninstalled apps are removed from every tab automatically.
- Hidden apps stay hidden in every tab.

## 6. Build / packaging
- Change `applicationId` to `org.fossify.home.tabs` and the app name to "Fossify Launcher Tabs", so it installs alongside the official app.
- Sign the debug or release build with a personal keystore (see `keystore.properties_sample`).
- Keep the GPL-3.0 license. NeoLauncher is open source too and can be read for reference, but write the implementation to fit Fossify's own code structure.

## Done when
- The project builds with `./gradlew assembleDebug` without errors.
- On a phone you can create tabs, add apps to them, reorder them, delete them, switch tabs in the drawer, and search across all apps.
- Data survives an app restart and a phone reboot.
