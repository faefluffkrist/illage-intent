# Validation — 1.0.0

Built with Java 25.0.4.1, Gradle 9.8.0, Loom 1.17.21, Minecraft 26.2, Fabric Loader 0.19.3, and Fabric API 0.155.2+26.2. Main and client source compilation, jar creation, and Gradle check/build tasks passed. No automated unit tests are included.

Dedicated-server combat checks used separate force-loaded stone platforms, Normal difficulty, nighttime, and stationary (`NoAI`) targets. Attackers were summoned with default equipment and were never hit by the tester. Targets were checked through entity health data, then checked again for death.

- With neither optional mod installed, all seven vanilla attacker types killed a zombie: pillager, vindicator, evoker, illusioner, vex, witch, and ravager.
- Zombie and skeleton horses retained full health (25 and 15) with pillagers nearby, through the later server restart as well.
- With both attached mods and ResourcefulLib 5.0.4 installed, both Friends&Foes illagers and all three It Takes a Pillage illagers killed zombies. Dedicated-server startup and mob/projectile creation succeeded.
- A witch killed a spider after the poison-immunity correction.
- Pillagers killed creeper, spider, and cave-spider targets. A vindicator killed a silverfish.
- After the small-target arrow correction, pillager and It Takes a Pillage archer killed silverfish. Both summoned illusioners also killed their silverfish targets in the final observation.

The Mod Menu screen compiles, but its appearance has not been visually checked in a running client. It reuses the supplied Trimworks deepslate viewport and reload-notice implementation. The complete pack's AI interactions, raids, all undead variants/bosses, every attacker/target combination, and each optional mod installed alone still need live testing. Config changes require reopening the world or restarting the server. Remote clients cannot edit server gameplay settings from this menu.

Optional mods and their dependencies are not distributed with this project.

## Configuration UI revision — 1.0.1

Main and client source compilation and Gradle build passed after rebuilding the menu from the supplied Keep Inventory Humbled and Trimworks sources. This revision changes the config menu and adds draft copy/comparison helpers; combat code remains as tested above. The menu uses centered ARGB text, pixel scrolling, wrapped descriptions, category tabs, save/discard controls, and mod-presence checks on compatibility navigation and submenu controls. Visual verification in a running client remains pending.
