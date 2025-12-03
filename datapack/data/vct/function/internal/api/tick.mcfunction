# All this is the countdown timer system by its raises, categorized as follows

## Check if the time has run out to stop the cycle
execute if score Second2 Timer matches 0 if score Second1 Timer matches 0 if score Minute2 Timer matches 0 if score Minute1 Timer matches 0 if score Hour Timer matches 0 run schedule clear vct:internal/api/addtick

## Updates the timer bar
execute store result bossbar voiidtimer:bar max run scoreboard players get MaxTimerSecs Timer
execute if score Tick Timer matches 20.. run scoreboard players remove TimerSecs Timer 1
execute store result bossbar voiidtimer:bar value run scoreboard players get TimerSecs Timer
bossbar set voiidtimer:bar players @a

## Update the styles
function vct:custom/styles_manager

## Countdown System
execute if score Tick Timer matches 20.. run function vct:internal/api/remove1second
execute if score Second2 Timer matches -1 run function vct:internal/api/remove10seconds
execute if score Second1 Timer matches -1 run function vct:internal/api/remove1minute
execute if score Minute2 Timer matches -1 run function vct:internal/api/remove10minutes
execute if score Minute1 Timer matches -1 run function vct:internal/api/remove1hour
execute if score Second1 Timer matches 6.. run scoreboard players add Minute2 Timer 1
execute if score Second1 Timer matches 6.. run scoreboard players set Second1 Timer 0
execute if score Minute2 Timer matches 10.. run scoreboard players add Minute1 Timer 1
execute if score Minute2 Timer matches 10.. run scoreboard players set Minute2 Timer 0

## Normalize Numbers
function vct:internal/api/normalize

## Prevent numbers from being negative
execute if score Hour Timer matches ..-1 run scoreboard players set Hour Timer 0
execute if score Second2 Timer matches ..-1 run scoreboard players set Second2 Timer 0
execute if score Second1 Timer matches ..-1 run scoreboard players set Second1 Timer 0
execute if score Minute2 Timer matches ..-1 run scoreboard players set Minute2 Timer 0
execute if score Minute1 Timer matches ..-1 run scoreboard players set Minute1 Timer 0

## Event ended if time is up
execute if score Hour Timer matches 0 if score Minute1 Timer matches 0 if score Minute2 Timer matches 0 if score Second2 Timer matches 1 if score Second1 Timer matches 0 run schedule function vct:internal/api/ended 2t replace