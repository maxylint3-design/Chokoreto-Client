# Chokoreto Client (Fabric, Minecraft 26.2)

A minimal, customizable all-in-one client mod for Donut SMP. Open the menu with **Right Shift** (or `/choko`).

## Modules
| Tab | What it does |
|---|---|
| **Auction** | 3-step wizard: pick an item, set min bid + count, set duration. A box shows the item, highest bid, bidder and time left. |
| **Recent Auctions** | History of finished auctions with a Re-list button. |
| **Transactions** | Money you got/sent in chat, filters (minimum, type, player), session profit with Reset, and the on-screen profit box (skin on/off, drag to move). |
| **Theme** | Accent colour presets or custom hex, menu + HUD opacity, text shadow. |
| **Mini Locker** | Save items you auction often with a usual min bid; two clicks to start. |
| **Music** | Plays Minecraft's music discs with a volume slider. |
| **Crosshair** | Cross, dot, cross+dot, circle, circle+dot, T-shape; colour, size, thickness, gap, outline, live preview. |
| **Utilities** | Info panel (FPS, ping, coords, direction, totems, crystals, gapples), nearby players + alerts, timers. |
| **Scoreboard** | Hide the sidebar scoreboard. |
| **Settings** | Defaults, HUD position editor, resets. |
| Cape / Sky | Reserved ("soon") - they need mixins. |

Drag any HUD box anywhere from *Settings -> Edit HUD positions*.

## Commands
`/choko` open - `/choko cancel` - `/choko test <name> <amount>` (fake payment, e.g. `/choko test Steve 5m`) - `/choko debug`

## Building
Upload this folder to GitHub (include the hidden `.github` folder), open **Actions**, wait for the green tick and download `chokoreto-jar`. Needs Java 25 if building locally: `./gradlew build`.

## If payments aren't detected
Edit the `RECEIVED` / `SENT` patterns at the top of `Transactions.java`. `/choko debug` prints lines containing "paid" that didn't match.

Settings are saved in `.minecraft/config/chokoreto/`.
