A list of all the features this mod has to offer can be found here:
https://modrinth.com/mod/wynnextras/gallery

Please report any bugs on discord: https://discord.gg/UbC6vZDaD5

# 🐘 HOW TO COMPILE
- If you want to compile the mod yourself, follow these steps: clone and open the repo in IntelliJ, click on the Gradle icon on the right,
  go into Tasks>build and double-click build, then you should find a .jar file in {WynnExtras folder}/build/libs

  A more detailed explanation can be found here: https://youtu.be/5YmUIDY7dm8

## Minecraft 26.3 port

This checkout targets Minecraft 26.3, Java 25, Fabric Loader 0.19.5 and Fabric API
0.160.7+26.3. Mod Menu 21.0.0-beta.1 is optional. Build with a JDK 25 installation:

```powershell
$env:JAVA_HOME = 'D:\Java\openjdk-25.0.1'
.\gradlew.bat clean build
```

Install `build/libs/wynnextras-0.19.4+mc26.3.jar` in your 26.3 Fabric instance.
The `-dev.jar` is an intermediate artifact and does not bundle the mod's libraries.

The port follows Wynntils' [26.3 migration commit](https://github.com/Wynntils/Wynntils/commit/6acccda569348e198a3b6e456982d40536b664c8)
from its 26.4 history. Yarn names were first migrated to Mojang names on 1.21.11,
then the build switched to unobfuscated Fabric Loom and the 26.3 APIs.
HUD overlays, GUI extraction, render submission, banner rendering, chat/session
APIs and vanilla Mixin targets have been updated. SDL keyboard/mouse events are
converted to the previous integer codes so saved WynnExtras key bindings remain valid.

Validation on 2026-10-08: Java compilation and distribution build; successful 26.3
client startup and full Mixin audit with mandatory vanilla injections; rendered
configuration, list editor and container screens with item models/counts; checks
of saved key codes, modifier flags and mouse button conversion. GUI item fixtures
used isolated component holders because vanilla defaults are bound after world login.
Live Wynncraft gameplay and co-installation with Wynntils have not been tested.

Known external data issue observed during startup: the Wynncraft ingredient feed
contains entries absent from WynnBuilder's ingredient ID map (for example Feathered
Bricks and Permafrost Anomaly). The existing crafting data loader rejects that
inconsistent snapshot, so crafting data can remain unavailable until the feeds agree.
This is separate from Minecraft compatibility.

Bank wheel fix (0.19.1): bank scrolling uses Fabric screen-scoped allowMouseScroll
events instead of canceling the low-level SDL mouse callback. Removed the duplicate
after-scroll callback and ignored zero vertical input. Verified in a real 26.3
client via MouseHandler.onScroll: wheel down/up move one row, horizontal-only
scroll does not move the bank, and each input is handled once.

Bank tooltip fix (0.19.2): when the bank overlay cancels vanilla extractRenderState,
explicitly invoke vanilla extractTooltip after the shopping list has handled hover.
This retains container tooltip hooks and the carried-item rule. Verified in a real
26.3 client with a fixture overlay that cancels rendering: occupied slots schedule
a tooltip, empty slots and a carried item suppress it, and hover recovers afterward.

Bank wheel compatibility fix (0.19.3): the 0.19.1 screen callback could be skipped
when Wynntils canceled MouseHandler.onScroll first. Handle custom-bank vertical
scroll in a priority-500 HEAD injection, applied after and placed before the
default-priority Wynntils hook. Validate the window, active screen and loading
overlay before consuming input; retain the screen callback as a fallback.
Reproduced the failure and verified the fix in 26.3 using a default-priority
canceling Mixin matching Wynntils' event dispatch: down/up are handled once;
horizontal input, wrong windows and disabled overlays are not intercepted by
the bank. Full live gameplay with the tester's mod pack remains unverified.

Bank scroll compatibility redirect (0.19.4): restored the upstream mechanism that
returns no Wynntils scroll button while the custom bank overlay is enabled. The
optional redirect still referenced Yarn HandledScreen in its target descriptor,
so it silently missed Mojang AbstractContainerScreen in 26.3. Corrected the
descriptor and check the overlay toggle before suppressing Wynntils scrolling.
Verified actual Mixin application against a fixture using Wynntils main's exact
ScrollableContainerProperty signature, including delegation when disabled or
outside a bank. The 0.19.3 early mouse callback remains as a fallback.
