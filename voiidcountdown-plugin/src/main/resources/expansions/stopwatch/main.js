'use strict';

var File = Java.type('java.io.File');
var Bukkit = Java.type('org.bukkit.Bukkit');
var VoiidCountdownTimer = Java.type('voiidstudios.vct.VoiidCountdownTimer');

var context = null;
var expansionFolder = null;

function loadScript(relative) {
    var target = new File(expansionFolder, relative);
    if (!target.exists()) {
        Bukkit.getLogger().warning('[Stopwatch] Missing script: ' + target.getAbsolutePath());
        return;
    }
    load(target.getPath());
}

function onEnable(ctx) {
    context = ctx;
    expansionFolder = new File(VoiidCountdownTimer.getInstance().getDataFolder(), 'expansions/stopwatch');

    loadScript('scripts/time.js');
    loadScript('scripts/stopwatch-manager.js');

    if (typeof StopwatchManager === 'undefined') {
        Bukkit.getLogger().severe('[Stopwatch] StopwatchManager was not loaded correctly.');
        return;
    }

    StopwatchManager.init(context);
    StopwatchManager.wireListeners();
    context.getLogger().info('[Stopwatch] Expansion enabled. Awaiting STOPWATCH timers.');
}

function onDisable() {
    if (typeof StopwatchManager !== 'undefined') {
        StopwatchManager.shutdown();
    }
    context = null;
}
