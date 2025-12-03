# Message error
execute if score LanguageTimer Timer matches 1 run tellraw @s ["",{"text":"[","color":"dark_purple"},{"text":"VCT","color":"light_purple","bold":true},{"text":"]","color":"dark_purple"}," ",{"text":"Actualmente el idioma de Voiid Countdown Timer es el Español.","color":"red"}]


# Sounds
execute if score LanguageTimer Timer matches 1 run playsound minecraft:block.note_block.bass ambient @s ~ ~ ~ 0.7 1
execute if score LanguageTimer Timer matches 0 run playsound minecraft:entity.player.levelup ambient @s ~ ~ ~ 0.2 2


# System
execute if score LanguageTimer Timer matches 0 run clear @s written_book{display:{Lore:['["",{"text":"vctc","italic":false}]']}}
execute if score LanguageTimer Timer matches 0 run function vct:internal/api/reset
execute if score LanguageTimer Timer matches 0 run tellraw @s ["-------------------------------------\n",{"text":"Voiid Countdown Timer","color":"light_purple","bold":true}," ",{"text":"se ha recargado para\ncambiar el idioma, utiliza o clickea el\ncomando para obtener el libro:","color":"green"},"\n",{"text":"/function vct:book","bold":true,"clickEvent":{"action":"run_command","value":"/function vct:book"},"hoverEvent":{"action":"show_text","contents":"Haz clic aquí para ejecutar este comando!"},"color":"aqua"},"\n-------------------------------------"]
execute if score LanguageTimer Timer matches 0 run scoreboard players set LanguageTimer Timer 1