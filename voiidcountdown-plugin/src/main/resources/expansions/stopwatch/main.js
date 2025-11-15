'use strict';

var Bukkit = Java.type('org.bukkit.Bukkit');
var BarColor = Java.type('org.bukkit.boss.BarColor');
var BarStyle = Java.type('org.bukkit.boss.BarStyle');
var MessagesManager = Java.type('voiidstudios.vct.managers.MessagesManager');
var VoiidCountdownTimer = Java.type('voiidstudios.vct.VoiidCountdownTimer');

var context = null;
var bossbar = null;
var stopwatchTask = null;
var elapsedSeconds = 0;
var running = false;

function formatTime(totalSeconds) {
    var hours = Math.floor(totalSeconds / 3600);
    var minutes = Math.floor((totalSeconds % 3600) / 60);
    var seconds = totalSeconds % 60;
    return ('0' + hours).slice(-2) + ':' + ('0' + minutes).slice(-2) + ':' + ('0' + seconds).slice(-2);
}

function ensureBossbar() {
    if (bossbar === null) {
        bossbar = Bukkit.createBossBar(MessagesManager.getColoredMessage('&dStopwatch &7| &f00:00:00'), BarColor.PURPLE, BarStyle.SEGMENTED_12);
    }
    return bossbar;
}

function updateBossbar() {
    if (bossbar === null) {
        return;
    }

    var formatted = formatTime(elapsedSeconds);
    bossbar.setTitle(MessagesManager.getColoredMessage('&dStopwatch &7| &f' + formatted));

    var iterator = Bukkit.getOnlinePlayers().iterator();
    while (iterator.hasNext()) {
        var player = iterator.next();
        bossbar.addPlayer(player);
    }

    var progress = Math.min((elapsedSeconds % 60) / 60.0, 1.0);
    bossbar.setProgress(progress);
}

function stopTask() {
    if (stopwatchTask !== null) {
        stopwatchTask.cancel();
        stopwatchTask = null;
    }
}

function sendUsage(sender) {
    sender.sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix + '&7Uso: &f/vct stopwatch <start|pause|resume|stop|lap>'));
}

function startStopwatch(sender) {
    if (running) {
        sender.sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix + '&cEl cronómetro ya está en marcha.'));
        return;
    }

    running = true;
    elapsedSeconds = 0;
    ensureBossbar();
    updateBossbar();
    stopTask();

    stopwatchTask = context.getScheduler().runTaskTimer(function () {
        elapsedSeconds += 1;
        updateBossbar();
    }, 20, 20);

    sender.sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix + '&aCronómetro iniciado.'));
}

function pauseStopwatch(sender) {
    if (!running) {
        sender.sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix + '&eEl cronómetro no está en marcha.'));
        return;
    }

    running = false;
    stopTask();
    sender.sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix + '&eCronómetro en pausa en &f' + formatTime(elapsedSeconds) + '&e.'));
}

function resumeStopwatch(sender) {
    if (running) {
        sender.sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix + '&cEl cronómetro ya está activo.'));
        return;
    }

    if (bossbar === null) {
        sender.sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix + '&eEl cronómetro no ha sido iniciado aún. Usa &f/vct stopwatch start&e.'));
        return;
    }

    running = true;
    updateBossbar();
    stopTask();
    stopwatchTask = context.getScheduler().runTaskTimer(function () {
        elapsedSeconds += 1;
        updateBossbar();
    }, 20, 20);

    sender.sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix + '&aCronómetro reanudado.'));
}

function stopStopwatch(sender) {
    if (bossbar === null) {
        sender.sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix + '&eNo hay un cronómetro activo.'));
        return;
    }

    stopTask();
    var finalTime = formatTime(elapsedSeconds);
    running = false;
    elapsedSeconds = 0;
    bossbar.removeAll();
    bossbar = null;

    sender.sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix + '&cCronómetro detenido en &f' + finalTime + '&c.'));
}

function lapStopwatch(sender) {
    if (bossbar === null) {
        sender.sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix + '&eNo hay un cronómetro activo.'));
        return;
    }

    sender.sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix + '&dVuelta actual: &f' + formatTime(elapsedSeconds)));
}

function onEnable(ctx) {
    context = ctx;
    context.getLogger().info('[Stopwatch] Expansion enabled.');

    context.getCommands().register({
        name: 'stopwatch',
        description: 'Controla un cronómetro simple desde comandos.',
        usage: '/vct stopwatch <start|pause|resume|stop|lap>',
        permission: 'voiidcountdowntimer.stopwatch',
        execute: function (sender, args) {
            if (args.length === 0) {
                sendUsage(sender);
                return true;
            }

            var sub = args[0].toLowerCase();
            if (sub === 'start') {
                startStopwatch(sender);
            } else if (sub === 'pause') {
                pauseStopwatch(sender);
            } else if (sub === 'resume') {
                resumeStopwatch(sender);
            } else if (sub === 'stop') {
                stopStopwatch(sender);
            } else if (sub === 'lap') {
                lapStopwatch(sender);
            } else {
                sendUsage(sender);
            }
            return true;
        },
        tabComplete: function (sender, args) {
            var options = ['start', 'pause', 'resume', 'stop', 'lap'];
            if (args.length === 1) {
                var partial = args[0].toLowerCase();
                var result = [];
                for (var i = 0; i < options.length; i++) {
                    var option = options[i];
                    if (partial.length === 0 || option.indexOf(partial) === 0) {
                        result.push(option);
                    }
                }
                return result;
            }
            return [];
        }
    });
}

function onDisable() {
    stopTask();
    if (bossbar !== null) {
        bossbar.removeAll();
        bossbar = null;
    }
    running = false;
    elapsedSeconds = 0;
}
