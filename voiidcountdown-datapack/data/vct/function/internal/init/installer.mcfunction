# This is the installer, where it checks if you have a compatible version

## Get your version of MC
execute store result score McVersion Timer run data get entity @r DataVersion

## Check if it is an incompatible version
### Verify only if you have 1.21.5 or a version lower than 1.15.
execute if score McVersion Timer matches 4298.. run scoreboard players set McVersionToNBTNew Timer 3
execute if score McVersion Timer matches 3940..4297 run scoreboard players set McVersionToNBTNew Timer 2

execute if score McVersion Timer matches ..1975 run tellraw @a ["-------------------------------------\n",{"text":"Voiid Countdown Timer","bold":true,"color":"light_purple"}," ",{"text":"can not be installed on\nthis version of Minecraft!","color":"red"},"\n\nThe ",{"text":"version you are using","color":"yellow"}," is are ",{"text":"incompatible","color":"red"},"\nwith Voiid Countdown Timer, you can use it\nin other compatible versions.\n\n",{"text":"We are sorry! :(","color":"aqua"},"\n-------------------------------------"]
execute if score McVersion Timer matches ..1975 run scoreboard players add McVersionIncompatible Timer 1
execute if score McVersion Timer matches ..1975 run schedule function vct:internal/api/uninstall 3s

## If your version is compatible, you will be notified with a welcome message
execute if score McVersionIncompatible Timer matches 0 if score InstalledOneTime Timer matches 0 run execute as @a at @s run playsound minecraft:block.note_block.bell master @s ~ ~ ~ 0.7 2
execute if score McVersionIncompatible Timer matches 0 if score InstalledOneTime Timer matches 0 run tellraw @a ["-------------------------------------\n",{"text":"Voiid Countdown Timer","bold":true,"color":"light_purple"}," ",{"text":"has been installed\ncorrectly!","color":"green"},"\n\nGet the ",{"text":"config book","color":"yellow"}," using this command:\n",{"text":"/function vct:book","color":"aqua","bold":true,"clickEvent":{"action":"run_command","value":"/function vct:book"},"hoverEvent":{"action":"show_text","contents":"Click here to execute this command!"}},"\n\nYou can click on the command to\nexecute it quickly.\n\n",{"text":"Thank you for using VCT Lite!","color":"green"},"\n-------------------------------------"]
execute if score McVersionIncompatible Timer matches 0 if score InstalledOneTime Timer matches 0 run scoreboard players add InstalledOneTime Timer 1