# This is the setup system, it adds everything needed from the datapack to work

## Add the score
scoreboard objectives add Timer dummy

## Add variables to the score
scoreboard players add Tick Timer 0
scoreboard players add Second1 Timer 0
scoreboard players add Second2 Timer 0
scoreboard players add Minute2 Timer 0
scoreboard players add Minute1 Timer 0
scoreboard players add Hour Timer 0
scoreboard players add RanEnabled Timer 0
scoreboard players add TimerSecs Timer 0
scoreboard players add MaxTimerSecs Timer 0
scoreboard players add Style Timer 0
scoreboard players add PausedTimer Timer 0
scoreboard players add EndedTimer Timer 0
scoreboard players add InstalledOneTime Timer 0
scoreboard players add LanguageTimer Timer 0
scoreboard players add McVersion Timer 0
scoreboard players add McVersionToNBTNew Timer 0
scoreboard players add McVersionIncompatible Timer 0

## Add the time bar
bossbar add voiidtimer:bar {"color": "white", "text": "VOIID TIMER"}
bossbar set voiidtimer:bar visible false

## Initializes the installation process
schedule function vct:internal/api/reset 1s
schedule function vct:internal/init/installer 3s