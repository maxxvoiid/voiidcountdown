## This system normalizes the adding numbers in the same tick

execute if score Second2 Timer matches 10.. run scoreboard players add Second1 Timer 1
execute if score Second2 Timer matches 10.. run scoreboard players set Second2 Timer 0

execute if score Second1 Timer matches 6.. run scoreboard players add Minute2 Timer 1
execute if score Second1 Timer matches 6.. run scoreboard players set Second1 Timer 0

execute if score Minute2 Timer matches 10.. run scoreboard players add Minute1 Timer 1
execute if score Minute2 Timer matches 10.. run scoreboard players set Minute2 Timer 0

execute if score Minute1 Timer matches 6.. run scoreboard players add Hour Timer 1
execute if score Minute1 Timer matches 6.. run scoreboard players set Minute1 Timer 0