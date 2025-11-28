(function () {
    var Bukkit = Java.type('org.bukkit.Bukkit');
    var BarColor = Java.type('org.bukkit.boss.BarColor');
    var BarStyle = Java.type('org.bukkit.boss.BarStyle');
    var EventPriority = Java.type('org.bukkit.event.EventPriority');
    var HandlerList = Java.type('org.bukkit.event.HandlerList');
    var Listener = Java.type('org.bukkit.event.Listener');
    var EventExecutor = Java.type('org.bukkit.plugin.EventExecutor');
    var VCTEvent = Java.type('voiidstudios.vct.api.VCTEvent');
    var VoiidCountdownTimer = Java.type('voiidstudios.vct.VoiidCountdownTimer');

    function clampProgress(value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    function applyFormattedTitle(bossbar, text) {
        var formatter = VoiidCountdownTimer.getConfigsManager().getMainConfigManager().getFormatter();
        var formatted = formatter.format(VoiidCountdownTimer.getInstance(), Bukkit.getConsoleSender(), text);

        var componentClass = null;
        var componentType = null;
        try {
            componentType = Java.type('net.kyori.adventure.text.Component');
            componentClass = componentType.class;
        } catch (ignored) {}

        if (componentType && Java.isType(formatted, componentType)) {
            try {
                bossbar.getClass().getMethod('setTitle', componentClass).invoke(bossbar, formatted);
                return;
            } catch (ignoredSet) {}

            try {
                var LegacySerializer = Java.type('net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer');
                var serializer;
                try {
                    serializer = LegacySerializer.legacySection();
                } catch (nsme) {
                    var builderCls = Java.type('net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder');
                    var builder = LegacySerializer.builder();
                    var charType = java.lang.Character.TYPE;
                    builderCls.getMethod('character', charType).invoke(builder, '&');
                    try { builderCls.getMethod('hexColors').invoke(builder); } catch (ignoredHex) {}
                    serializer = builderCls.getMethod('build').invoke(builder);
                }

                var legacyTitle = LegacySerializer.getMethod('serialize', componentClass).invoke(serializer, formatted);
                bossbar.setTitle(legacyTitle);
                return;
            } catch (ignoredLegacy) {}
        }

        if (typeof formatted === 'string') {
            bossbar.setTitle(formatted.replace(/&/g, '§'));
        } else {
            bossbar.setTitle(String(text).replace(/&/g, '§'));
        }
    }

    function StopwatchSession(timerId, config, scheduler, logger) {
        this.timerId = timerId;
        this.config = config;
        this.scheduler = scheduler;
        this.logger = logger;

        this.elapsedMillis = 0;
        this.targetSeconds = 0;
        this.running = false;
        this.task = null;
        this.lastUpdate = 0;

        var color = (config && config.getColor) ? config.getColor() : BarColor.WHITE;
        var style = (config && config.getStyle) ? config.getStyle() : BarStyle.SOLID;
        this.bossbar = Bukkit.createBossBar('', color, style);
    }

    StopwatchSession.prototype.start = function (targetSeconds) {
        this.stop();
        this.elapsedMillis = 0;
        this.targetSeconds = targetSeconds || 0;
        this.lastUpdate = java.lang.System.currentTimeMillis();
        this.running = true;
        this.task = this.scheduler.runTaskTimer(this.tick.bind(this), 1, 1);
        this.refreshPlayers();
        this.updateDisplay();
    };

    StopwatchSession.prototype.resume = function () {
        if (this.running) return;
        this.running = true;
        this.lastUpdate = java.lang.System.currentTimeMillis();
        this.task = this.scheduler.runTaskTimer(this.tick.bind(this), 1, 1);
    };

    StopwatchSession.prototype.pause = function () {
        if (!this.running) return;
        this.running = false;
        if (this.task) {
            this.task.cancel();
            this.task = null;
        }
    };

    StopwatchSession.prototype.stop = function () {
        if (this.task) {
            this.task.cancel();
            this.task = null;
        }
        this.running = false;
        if (this.bossbar) {
            this.bossbar.removeAll();
        }
    };

    StopwatchSession.prototype.tick = function () {
        if (!this.running) return;
        var now = java.lang.System.currentTimeMillis();
        var delta = now - this.lastUpdate;
        this.lastUpdate = now;
        this.elapsedMillis += delta;
        this.updateDisplay();
    };

    StopwatchSession.prototype.refreshPlayers = function () {
        var iterator = Bukkit.getOnlinePlayers().iterator();
        while (iterator.hasNext()) {
            var player = iterator.next();
            this.bossbar.addPlayer(player);
        }
    };

    StopwatchSession.prototype.updateDisplay = function () {
        var rawText = (this.config && this.config.getText) ? this.config.getText() : '%HH%:%MM%:%SS%';
        var formattedText = StopwatchTime.formatText(rawText, this.elapsedMillis);
        var phasedText = VoiidCountdownTimer.getPhasesManager().formatPhases(formattedText);
        applyFormattedTitle(this.bossbar, phasedText);

        var secondsElapsed = this.elapsedMillis / 1000.0;
        var progress;
        if (this.targetSeconds > 0) {
            progress = clampProgress(secondsElapsed / this.targetSeconds);
        } else {
            progress = clampProgress((secondsElapsed % 60) / 60.0);
        }
        this.bossbar.setProgress(progress);
        this.refreshPlayers();
    };

    var StopwatchManager = {
        context: null,
        sessions: {},
        listeners: [],
        init: function (context) {
            this.context = context;
            this.scheduler = context.getScheduler();
            if (typeof StopwatchLang !== 'undefined') {
                StopwatchLang.init(context);
            }
        },
        getConfig: function (timerId) {
            return VoiidCountdownTimer.getConfigsManager().getTimerConfig(timerId);
        },
        ensureSession: function (timerId, config) {
            if (this.sessions[timerId]) {
                this.sessions[timerId].stop();
            }
            this.sessions[timerId] = new StopwatchSession(timerId, config, this.scheduler, this.context.getLogger());
            return this.sessions[timerId];
        },
        updateSessionConfig: function (timerId, cfg) {
            var session = this.sessions[timerId];
            if (session && cfg) {
                session.config = cfg;
                try { session.bossbar.setColor(cfg.getColor()); } catch (ignored) {}
                try { session.bossbar.setStyle(cfg.getStyle()); } catch (ignoredStyle) {}
                session.updateDisplay();
            }
        },
        removeSession: function (timerId) {
            if (this.sessions[timerId]) {
                this.sessions[timerId].stop();
                delete this.sessions[timerId];
            }
        },
        startFromEvent: function (event) {
            if (!event || !event.getTimerId()) return;
            var timerId = event.getTimerId();
            var cfg = this.getConfig(timerId);
            if (!cfg || cfg.getFormat() !== "STOPWATCH") {
                this.removeSession(timerId);
                return;
            }

            try {
                var baseTimer = event.getTimer();
                if (baseTimer) {
                    baseTimer.stop();
                }
            } catch (ignoredTimerStop) {}

            var targetSeconds = StopwatchTime.parseTimeToSeconds(event.getInitialTime());
            var session = this.ensureSession(timerId, cfg);
            session.start(targetSeconds || 0);
        },
        pauseFromEvent: function (event) {
            if (!event || !event.getTimerId()) return;
            var session = this.sessions[event.getTimerId()];
            if (session) session.pause();
        },
        resumeFromEvent: function (event) {
            if (!event || !event.getTimerId()) return;
            var session = this.sessions[event.getTimerId()];
            if (session) session.resume();
        },
        stopFromEvent: function (event) {
            if (!event || !event.getTimerId()) return;
            this.removeSession(event.getTimerId());
        },
        applyModification: function (event) {
            if (!event || !event.getTimerId()) return;
            var timerId = event.getTimerId();
            var cfg = this.getConfig(timerId);
            if (cfg && cfg.getFormat() === "STOPWATCH") {
                this.updateSessionConfig(timerId, cfg);
            }
        },
        refreshPlayer: function (player) {
            for (var key in this.sessions) {
                if (!this.sessions.hasOwnProperty(key)) continue;
                var session = this.sessions[key];
                if (session && session.bossbar) {
                    session.bossbar.addPlayer(player);
                }
            }
        },
        shutdown: function () {
            for (var key in this.sessions) {
                if (!this.sessions.hasOwnProperty(key)) continue;
                this.sessions[key].stop();
            }
            this.sessions = {};
            for (var i = 0; i < this.listeners.length; i++) {
                HandlerList.unregisterAll(this.listeners[i]);
            }
            this.listeners = [];
        },
        registerListener: function (eventClass, priority, handler) {
            var listener = Java.extend(Listener, {});
            listener = new listener();
            var executor = new (Java.extend(EventExecutor, {
                execute: function (_, event) {
                    handler(event);
                }
            }))();
            VoiidCountdownTimer.getInstance().getServer().getPluginManager().registerEvent(
                eventClass,
                listener,
                priority,
                executor,
                VoiidCountdownTimer.getInstance(),
                false
            );
            this.listeners.push(listener);
        },
        wireListeners: function () {
            var self = this;
            this.registerListener(VCTEvent.class, EventPriority.NORMAL, function (event) {
                var type = event.getType();
                if (type === VCTEvent.VCTEventType.CREATE) {
                    self.startFromEvent(event);
                } else if (type === VCTEvent.VCTEventType.PAUSE) {
                    self.pauseFromEvent(event);
                } else if (type === VCTEvent.VCTEventType.RESUME) {
                    self.resumeFromEvent(event);
                } else if (type === VCTEvent.VCTEventType.STOP) {
                    self.stopFromEvent(event);
                } else if (type === VCTEvent.VCTEventType.MODIFY) {
                    self.applyModification(event);
                }
            });

            var PlayerJoinEvent = Java.type('org.bukkit.event.player.PlayerJoinEvent');
            this.registerListener(PlayerJoinEvent.class, EventPriority.MONITOR, function (event) {
                self.refreshPlayer(event.getPlayer());
            });
        }
    };

    this.StopwatchManager = StopwatchManager;
})();