scoreboard players set Minute2 Timer 9
scoreboard players remove Minute1 Timer 1
scoreboard players remove TimerSecs Timer 600
scoreboard players remove MaxTimerSecs Timer 600

# Message changed time
playsound minecraft:entity.player.levelup ambient @s ~ ~ ~ 0.2 2
## EN
execute if score LanguageTimer Timer matches 0 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"10 minutes have been subtracted from the timer! It is now in","color":"gold"}," ",{"score":{"name":"Hour","objective":"Timer"},"color":"yellow"},{"text":":","color":"yellow"},{"score":{"name":"Minute1","objective":"Timer"},"color":"yellow"},{"score":{"name":"Minute2","objective":"Timer"},"color":"yellow"},{"text":":","color":"yellow"},{"score":{"name":"Second1","objective":"Timer"},"color":"yellow"},{"score":{"name":"Second2","objective":"Timer"},"color":"yellow"}]
## ES 
execute if score LanguageTimer Timer matches 1 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"Se han restado 10 minutos al temporizador! Ahora está en","color":"gold"}," ",{"score":{"name":"Hour","objective":"Timer"},"color":"yellow"},{"text":":","color":"yellow"},{"score":{"name":"Minute1","objective":"Timer"},"color":"yellow"},{"score":{"name":"Minute2","objective":"Timer"},"color":"yellow"},{"text":":","color":"yellow"},{"score":{"name":"Second1","objective":"Timer"},"color":"yellow"},{"score":{"name":"Second2","objective":"Timer"},"color":"yellow"}]