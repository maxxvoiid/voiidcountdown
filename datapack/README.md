# Voiid Countdown Timer Datapack

The datapack edition of VCT ships the lightweight countdown experience for vanilla Minecraft worlds and realms.

## Layout
- `pack.mcmeta`: Declares the pack format and metadata shown in-game.
- `data/minecraft/tags/functions/load.json` and `data/minecraft/tags/functions/tick.json`: Hook the datapack into the vanilla load/tick cycles.
- `data/vct/functions`: Main logic for the timer, including the configuration book (`book.mcfunction`) and custom event hooks under `custom/`.
- `data/vct/function`: Additional function copies packaged with the download for older loaders.

## Editing tips
- Start with `data/vct/functions/book.mcfunction` to adjust the configuration book contents or flow.
- Customize the actions that fire on timer events in the `data/vct/functions/custom/` folder (for example `on_start.mcfunction` and `on_end.mcfunction`).
- After changing functions, run `/reload` in your world to pick up the updates.

## Packaging
Drop the `datapack` folder into a world’s `datapacks/` directory or zip its contents to distribute an updated build.
