# Messages Errors
## EN
execute if score EndedTimer Timer matches 1 if score LanguageTimer Timer matches 0 if score PausedTimer Timer matches 1 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"The timer has not been started.","color":"red"}]
execute if score EndedTimer Timer matches 0 if score LanguageTimer Timer matches 0 if score PausedTimer Timer matches 1 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"The timer is already paused.","color":"red"}]

## ES
execute if score EndedTimer Timer matches 1 if score LanguageTimer Timer matches 1 if score PausedTimer Timer matches 1 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"El temporizador no ha sido iniciado.","color":"red"}]
execute if score EndedTimer Timer matches 0 if score LanguageTimer Timer matches 1 if score PausedTimer Timer matches 1 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"El temporizador ya esta pausado.","color":"red"}]


# Message Successful
## ES
execute if score PausedTimer Timer matches 0 if score LanguageTimer Timer matches 0 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"Timer paused!","color":"gold"}]
## ES
execute if score PausedTimer Timer matches 0 if score LanguageTimer Timer matches 1 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"Temporizador pausado!","color":"gold"}]


# Sounds
execute if score EndedTimer Timer matches 1 if score PausedTimer Timer matches 1 run playsound minecraft:block.note_block.bass ambient @s ~ ~ ~ 0.7 1
execute if score EndedTimer Timer matches 0 if score PausedTimer Timer matches 1 run playsound minecraft:block.note_block.bass ambient @s ~ ~ ~ 0.7 1
execute if score PausedTimer Timer matches 0 run playsound minecraft:entity.player.levelup ambient @s ~ ~ ~ 0.2 2


# Successful System
execute if score PausedTimer Timer matches 0 run schedule clear vct:internal/api/addtick
execute if score PausedTimer Timer matches 0 run scoreboard players set PausedTimer Timer 1
execute if score PausedTimer Timer matches 0 run function vct:custom/on_pause