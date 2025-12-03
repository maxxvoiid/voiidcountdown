## DO NOT DELETE THIS FILE, IT WILL BREAK THE WHOLE TIMER!!!
## DO NOT DELETE THIS FILE, IT WILL BREAK THE WHOLE TIMER!!!
## DO NOT DELETE THIS FILE, IT WILL BREAK THE WHOLE TIMER!!!

## This file is the manager of the timer styles, you can customize them to be your own styles!
## You can add, delete and customize whatever you want, as long as you follow the format :)

execute if score Style Timer matches 0 run function vct:custom/styles/white
execute if score Style Timer matches 1 run function vct:custom/styles/blue
execute if score Style Timer matches 2 run function vct:custom/styles/green
execute if score Style Timer matches 3 run function vct:custom/styles/pink
execute if score Style Timer matches 4 run function vct:custom/styles/purple
execute if score Style Timer matches 5 run function vct:custom/styles/red
execute if score Style Timer matches 6 run function vct:custom/styles/yellow


## To add your custom style, follow this format:
# execute if score Style Timer matches <number> run function vct:custom/styles/<your_style_name>
# You can use any number you want, but make sure it doesn't conflict with the existing ones!
## Example:
# execute if score Style Timer matches 7 run function vct:custom/styles/orange

## If you want to change the style in-game, you can use this commnand:
# /scoreboard players set Style Timer <number>