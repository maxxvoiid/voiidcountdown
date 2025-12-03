# Messages errors
## EN
execute if score EndedTimer Timer matches 0 if score LanguageTimer Timer matches 0 if score PausedTimer Timer matches 0 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"The timer has already been started.","color":"red"}]
## ES 
execute if score EndedTimer Timer matches 0 if score LanguageTimer Timer matches 1 if score PausedTimer Timer matches 0 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"El temporizador ya fue iniciado.","color":"red"}]


# Message started
## EN
execute if score EndedTimer Timer matches 1 if score LanguageTimer Timer matches 0 if score PausedTimer Timer matches 1 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"Timer started!","color":"green"}]
## ES 
execute if score EndedTimer Timer matches 1 if score LanguageTimer Timer matches 1 if score PausedTimer Timer matches 1 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"Temporizador iniciado!","color":"green"}]


# Message resumed
## EN
execute if score EndedTimer Timer matches 0 if score LanguageTimer Timer matches 0 if score PausedTimer Timer matches 1 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"Timer resumed!","color":"green"}]
## ES 
execute if score EndedTimer Timer matches 0 if score LanguageTimer Timer matches 1 if score PausedTimer Timer matches 1 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"Temporizador reanudado!","color":"green"}]


# Sounds
execute if score EndedTimer Timer matches 0 if score PausedTimer Timer matches 0 run playsound minecraft:block.note_block.bass ambient @s ~ ~ ~ 0.7 1
execute if score EndedTimer Timer matches 0 if score PausedTimer Timer matches 1 run playsound minecraft:entity.player.levelup ambient @s ~ ~ ~ 0.2 2
execute if score EndedTimer Timer matches 1 if score PausedTimer Timer matches 1 run playsound minecraft:entity.player.levelup ambient @s ~ ~ ~ 0.2 2


# System
execute if score EndedTimer Timer matches 0 run function vct:custom/on_start
bossbar set voiidtimer:bar visible true
scoreboard players set PausedTimer Timer 0
scoreboard players set EndedTimer Timer 0
function vct:internal/api/addtick