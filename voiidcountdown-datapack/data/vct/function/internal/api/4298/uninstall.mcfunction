# Message
## EN
execute if score LanguageTimer Timer matches 0 run tellraw @s ["-------------------------------------\n",{"text":"Voiid Countdown Timer","color":"light_purple","bold":true}," ",{"text":"has been successfully\nuninstalled","color":"green"},"\n\n",{"text":"To complete the uninstallation, ","color":"yellow"},{"text":"please remove\nthe datapack","color":"yellow","bold":true},{"text":" from your world folder.","color":"yellow"},"\n\n",{"text":"Thank you for using VCT Lite :)","color":"aqua"},"\n-------------------------------------"]
## ES
execute if score LanguageTimer Timer matches 1 run tellraw @s ["-------------------------------------\n",{"text":"Voiid Countdown Timer","color":"light_purple","bold":true}," ",{"text":"ha sido desinstalado\nexitosamente","color":"green"},"\n\n",{"text":"Para completar la desinstalacion, ","color":"yellow"},{"text":"por favor\nremueva el datapack","color":"yellow","bold":true},{"text":" de la carpeta de tu mundo.","color":"yellow"},"\n\n",{"text":"Gracias por usar VCT Lite :)","color":"aqua"},"\n-------------------------------------"]


# System
playsound minecraft:entity.player.levelup ambient @s ~ ~ ~ 0.2 2
clear @a minecraft:written_book[lore=[{"text":"vctc","italic":false}]]
function vct:internal/api/uninstall