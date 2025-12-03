# Voiid Countdown Timer Plugin

The full VCT plugin brings the countdown experience to Spigot, Paper, and Purpur servers with richer customization and API hooks.

## Layout
- `pom.xml`: Maven project descriptor for building the plugin jar.
- `src/main/java/voiidstudios/vct/VoiidCountdownTimer.java`: Plugin entrypoint registered by `plugin.yml`.
- `src/main/java/voiidstudios/vct/api/`: Public API surface, including events and the `VCTAPI` facade.
- `src/main/java/voiidstudios/vct/managers/`: Runtime managers for timers, translations, dependencies, and dynamic behavior.
- `src/main/resources/plugin.yml`: Bukkit metadata plus command/permission declarations.
- `src/main/resources/config.yml`: Default configuration distributed with the jar.

## Building
Run `mvn clean package` from the `plugin` directory to produce the shaded jar in `target/`. Copy that jar into your server’s `plugins/` folder to test changes.

## Quick navigation
- Commands are handled in `src/main/java/voiidstudios/vct/commands/MainCommand.java`.
- PlaceholderAPI integration lives in `src/main/java/voiidstudios/vct/api/PAPIExpansion.java`.
- Event lifecycle hooks sit in `src/main/java/voiidstudios/vct/api/VCTEvent.java` and related managers under `managers/`.
