# Ended event, causes the custom event to be executed and resets the timer

scoreboard players set RanEnabled Timer 0
function vct:custom/on_end
schedule function vct:internal/api/reset 7s