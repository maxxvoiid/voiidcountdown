# Messages errors
## EN
execute if score EndedTimer Timer matches 1 if score LanguageTimer Timer matches 0 if score PausedTimer Timer matches 1 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"The timer has already been reset.","color":"red"}]
## ES 
execute if score EndedTimer Timer matches 1 if score LanguageTimer Timer matches 1 if score PausedTimer Timer matches 1 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"El temporizador ya fue reiniciado.","color":"red"}]


# Message Successful
## EN
execute if score EndedTimer Timer matches 0 if score LanguageTimer Timer matches 0 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"Timer reseted!","color":"gold"}]
## ES
execute if score EndedTimer Timer matches 0 if score LanguageTimer Timer matches 1 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"Temporizador reiniciado!","color":"gold"}]


# Sounds
execute if score EndedTimer Timer matches 1 if score PausedTimer Timer matches 1 run playsound minecraft:block.note_block.bass ambient @s ~ ~ ~ 0.7 1
execute if score EndedTimer Timer matches 0 run playsound minecraft:entity.player.levelup ambient @s ~ ~ ~ 0.2 2


# System
execute if score EndedTimer Timer matches 0 run function vct:internal/api/reset
execute if score EndedTimer Timer matches 0 run function vct:custom/on_reset