<div align="center">

<img src="https://github.com/user-attachments/assets/31ffc42e-ac93-4b74-8855-ed896ae12e5d" alt="VCTStamp2" height="120"/>

### As simple as the name implies.
Voiid Countdown Timer is a simple customizable countdown timer datapack & plugin.

[![Release VCT](https://img.shields.io/github/v/release/Voiid-Studios/voiidcountdown?style=flat&logo=github&label=Release&color=fe50dc "View the latest release")](https://github.com/Voiid-Studios/voiidcountdown/releases)
[![Modrinth](https://img.shields.io/modrinth/dt/N6N9gUha?style=flat&logo=modrinth&label=Modrinth&color=%2300AF5C "View Modrinth page")](https://modrinth.com/datapack/voiid-countdown-timer)
[![CurseForge](https://img.shields.io/curseforge/dt/1145327?style=flat&logo=curseforge&label=CurseForge&color=F16436 "View CurseForge page")](https://curseforge.com/minecraft/data-packs/vct-voiid-countdown-timer/)
[![SpigotMC](https://img.shields.io/spiget/downloads/127616?style=flat&logo=spigotmc&logoColor=ffd000&label=SpigotMC&color=ffd000 "View SpigotMC page")](https://www.spigotmc.org/resources/voiid-countdown-timer.127616/)

[![GitHub Support](https://img.shields.io/badge/support-github?style=for-the-badge&logo=github&label=GitHub&color=0390fc "Any problems? Create an issue on GitHub")](https://github.com/Voiid-Studios/voiidcountdown/issues)
[![License](https://img.shields.io/badge/license-example?style=for-the-badge&label=Voiid%20Studios&color=ffffff "View the Voiid Studios public license")](https://github.com/Voiid-Studios/voiidstudios/blob/main/LICENSE.md)
[![Ko-fi](https://img.shields.io/badge/Kofi-ff6433?logo=kofi&logoColor=fff&style=for-the-badge "Support us on Ko-fi!")](https://api.maxxvoiid.space/kofi)

![timer1](https://github.com/user-attachments/assets/d0181bbf-06ba-4d74-824e-8849eb49701f)

![timer2](https://github.com/user-attachments/assets/2287f6c8-72bb-48c1-a36f-be2625913a3d)

</div>

## ❓ What is VCT?
Voiid Countdown Timer (VCT) is a fully customizable timer for Minecraft.

It works in two different versions:
- 📦 **Datapack** → The Lite version of VCT, ideal for single-player worlds or realms.
- 🔌 **Plugin** → The full version of VCT, perfect for multiplayer servers with Spigot/Paper.
  
With both versions, you can:
- 🎨 Create visual timers with a progress bar.
- 🖌️ Customize color styles.
- 🛠️ Run custom events when the timer starts or ends.

## ✨ Features
Voiid Countdown Timer includes:
- 🎨 Custom Colors Styles
- ⚙️ Quick and easy setup
- ⏳ Progressive Time Bar
- 🎈 Custom Events
- 👥 SinglePlayer and Multiplayer Compatible
- 💻 Open Source and constantly updated

## 📋 Requirements
### 📦 Datapack
- Version: `1.15+`
  
### 🔌 Plugin
- Version: `1.13+`
- Server: Spigot, Paper, or Purpur (not CraftBukkit)

## 🚀 Installation
### 📦 Datapack
1. Download the [latest version here](https://modrinth.com/datapack/voiid-countdown-timer/versions?c=release&l=datapack)
2. Place the downloaded `.zip` file in the `datapacks` folder of your world.
3. Enter the world and you will receive a message confirming the installation! 🎉

### 🔌 Plugin
1. Download the [latest version here](https://modrinth.com/datapack/voiid-countdown-timer/versions?c=release&l=spigot)
2. Place the downloaded `.jar` file in the `plugins` folder of your server.
3. Restart the server and the plugin will be ready. ⚡

## 📖 How to use it
### 📦 Datapack
- Execute the command `/function vct:book` to receive the configuration book.
- From the book you will be able to:
  - ⏰ Set time
  - 🎛️ Timer controls
  - 🌈 Customize style of colors
  - 🗣️ Change language
  - ❌ Uninstall
 
### 🔌 Plugin
- Configuration using commands and configuration files (`config.yml`)
- To see all available commands, use `/vct help`.
- In `config.yml`, you can customize various timer options; all of them are detailed in the [official documentation.](https://vctdocs.mintlify.app/)

## 🔌 Add-on system (prototype)
Voiid Countdown Timer now exposes a lightweight registration API so that extra plugins can plug into the main timer features.

- Third-party plugins can declare a dependency on `VoiidCountdownTimer`, implement the `voiidstudios.vct.api.addon.VCTAddon` interface and register themselves through `VCTAPI.registerAddon("your-id", addon, this)` inside `onEnable`.
- Add-ons receive callbacks when they are registered/unregistered and can orchestrate timers through the existing `VCTActions` helpers.

### ⏱️ Stopwatch reference implementation
This repository now includes a sketch add-on (`voiidcountdown-addon-stopwatch`) showcasing the new API. It introduces `/vctstopwatch` commands that create and control count-up timers (with optional targets) via the core plugin.

- `/vctstopwatch start [hh:mm:ss] [timer-id]` — starts an ascending stopwatch, optionally ending at the provided target and/or using a custom timer profile.
- `/vctstopwatch pause`, `/vctstopwatch resume`, `/vctstopwatch stop` — control the active stopwatch.

The add-on doubles as documentation for developers that want to ship their own integrations.

## [📚 Documentation](https://vctdocs.mintlify.app/)

<img src="https://github.com/user-attachments/assets/19d69e0d-1e31-450f-aa13-835cc22c93de" height="120px"/>
<a href="https://maxxvoiid.space" target="_blank" rel="noopener noreferrer"><img src="https://github.com/user-attachments/assets/32d8ca57-5185-46e2-b8f6-d60354408a38" height="120px"></img></a>
<a href="https://api.maxxvoiid.space/kofi" target="_blank" rel="noopener noreferrer"><img src="https://github.com/user-attachments/assets/ec59134e-064f-4f7c-9a36-ff38815e2a9b" height="153px"></img></a>
