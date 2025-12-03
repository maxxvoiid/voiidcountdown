package voiidstudios.vct.expansions;

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.expansions.command.ExpansionCommand;
import voiidstudios.vct.expansions.command.ExpansionCommandRegistry;
import voiidstudios.vct.expansions.exceptions.InvalidExpansionException;
import voiidstudios.vct.expansions.runtime.ExpansionScheduledTask;
import voiidstudios.vct.expansions.runtime.ScriptExpansionContext;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class ScriptExpansion {
    private final VoiidCountdownTimer plugin;
    private final ExpansionMetadata metadata;
    private final File directory;
    private final File scriptFile;
    private final ExpansionCommandRegistry commandRegistry;
    private final ClassLoader pluginClassLoader;

    private ScriptEngine engine;
    private Invocable invocable;
    private ScriptExpansionContext context;
    private final List<ExpansionScheduledTask> scheduledTasks = new CopyOnWriteArrayList<>();

    public ScriptExpansion(VoiidCountdownTimer plugin, ExpansionMetadata metadata, File directory, ExpansionCommandRegistry commandRegistry) throws InvalidExpansionException {
        this.plugin = plugin;
        this.metadata = metadata;
        this.commandRegistry = commandRegistry;
        this.directory = directory;
        this.scriptFile = new File(directory, metadata.getMainScript());
        this.pluginClassLoader = plugin.getClass().getClassLoader();

        if (!scriptFile.exists()) {
            throw new InvalidExpansionException(String.format(Locale.ROOT, "The script '%s' defined by expansion %s does not exist", metadata.getMainScript(), metadata.getName()));
        }
    }

    public VoiidCountdownTimer getPlugin() {
        return plugin;
    }

    public ExpansionMetadata getMetadata() {
        return metadata;
    }

    public File getDirectory() {
        return directory;
    }

    public boolean load() {
        ClassLoader previous = enterContextClassLoader();
        try {
            try {
                engine = new NashornScriptEngineFactory().getScriptEngine("--language=es6");
            } catch (Throwable throwable) {
                plugin.getLogger().log(Level.SEVERE, String.format(Locale.ROOT, "Failed to initialize script engine for expansion %s", metadata.getName()), throwable);
                return false;
            }

            if (engine == null) {
                plugin.getLogger().severe(String.format(Locale.ROOT, "No JavaScript engine available. Expansion %s will be skipped.", metadata.getName()));
                return false;
            }

            context = new ScriptExpansionContext(plugin, this);

            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("plugin", plugin);
            bindings.put("server", plugin.getServer());
            bindings.put("logger", plugin.getLogger());
            bindings.put("messages", VoiidCountdownTimer.getMessagesManager());

            try (Reader reader = new FileReader(scriptFile)) {
                engine.eval(reader);
            } catch (ScriptException | IOException exception) {
                plugin.getLogger().log(Level.SEVERE, String.format(Locale.ROOT, "Failed to evaluate script for expansion %s", metadata.getName()), exception);
                return false;
            }

            if (engine instanceof Invocable) {
                invocable = (Invocable) engine;
            } else {
                plugin.getLogger().severe(String.format(Locale.ROOT, "The script engine for expansion %s does not support Invocable", metadata.getName()));
                return false;
            }

            invokeIfPresent("onLoad", context);
            invokeIfPresent("onEnable", context);

            return true;
        } finally {
            exitContextClassLoader(previous);
        }
    }

    public void disable() {
        commandRegistry.unregisterCommands(this);

        for (ExpansionScheduledTask task : new ArrayList<>(scheduledTasks)) {
            task.cancel();
        }
        scheduledTasks.clear();

        invokeIfPresent("onDisable", context);
    }

    public void registerCommand(Object definitionObject) {
        ScriptObjectMirror mirror;

        try {
            mirror = requireObject(definitionObject);
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning(String.format(Locale.ROOT, "Unable to register command for expansion %s: %s", metadata.getName(), exception.getMessage()));
            return;
        }

        String name = optString(mirror, "name");
        if (name == null || name.trim().isEmpty()) {
            plugin.getLogger().warning(String.format(Locale.ROOT, "Skipping command registration for expansion %s: missing name", metadata.getName()));
            return;
        }

        Object executorObj = mirror.get("execute");
        ScriptObjectMirror executor;
        try {
            executor = requireFunction(executorObj);
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning(String.format(Locale.ROOT, "Skipping command '%s' for expansion %s: %s", name, metadata.getName(), exception.getMessage()));
            return;
        }

        Object tabCompleterObj = mirror.get("tabComplete");
        ScriptObjectMirror tabCompleter = null;
        if (tabCompleterObj instanceof ScriptObjectMirror && ((ScriptObjectMirror) tabCompleterObj).isFunction()) {
            tabCompleter = (ScriptObjectMirror) tabCompleterObj;
        }

        List<String> aliases = convertToStringList(mirror.get("aliases"));
        String description = optString(mirror, "description");
        String usage = optString(mirror, "usage");
        String permission = optString(mirror, "permission");

        ExpansionCommand command = new ExpansionCommand(this, name, aliases, description, usage, permission, executor, tabCompleter);
        commandRegistry.registerCommand(this, command);
    }

    public ExpansionScheduledTask trackTask(Object delegate) {
        ExpansionScheduledTask task = new ExpansionScheduledTask(this, delegate);
        scheduledTasks.add(task);
        return task;
    }

    public void unregisterTask(ExpansionScheduledTask task) {
        scheduledTasks.remove(task);
    }

    public ScriptObjectMirror requireFunction(Object obj) {
        if (obj instanceof ScriptObjectMirror && ((ScriptObjectMirror) obj).isFunction()) {
            return (ScriptObjectMirror) obj;
        }

        throw new IllegalArgumentException("Provided object is not a function");
    }

    public ScriptObjectMirror requireObject(Object obj) {
        if (obj instanceof ScriptObjectMirror) {
            return (ScriptObjectMirror) obj;
        }

        throw new IllegalArgumentException("Provided object is not a script object");
    }

    public Runnable toRunnable(final ScriptObjectMirror function) {
        return () -> {
            try {
                callFunction(function);
            } catch (Throwable throwable) {
                plugin.getLogger().log(Level.SEVERE, String.format(Locale.ROOT, "Error executing scheduled task for expansion %s", metadata.getName()), throwable);
            }
        };
    }

    public List<String> convertToStringList(Object raw) {
        if (raw == null) {
            return Collections.emptyList();
        }

        if (raw instanceof String) {
            return Collections.singletonList((String) raw);
        }

        if (raw instanceof ScriptObjectMirror) {
            ScriptObjectMirror mirror = (ScriptObjectMirror) raw;
            if (mirror.isArray()) {
                List<String> result = new ArrayList<>();
                for (Object value : mirror.values()) {
                    if (value != null) {
                        result.add(String.valueOf(value));
                    }
                }
                return result;
            }
        }

        if (raw instanceof Collection) {
            List<String> result = new ArrayList<>();
            for (Object value : (Collection<?>) raw) {
                if (value != null) {
                    result.add(String.valueOf(value));
                }
            }
            return result;
        }

        if (raw.getClass().isArray()) {
            List<String> result = new ArrayList<>();
            int length = java.lang.reflect.Array.getLength(raw);
            for (int i = 0; i < length; i++) {
                Object value = java.lang.reflect.Array.get(raw, i);
                if (value != null) {
                    result.add(String.valueOf(value));
                }
            }
            return result;
        }

        return Collections.emptyList();
    }

    public String optString(ScriptObjectMirror mirror, String key) {
        Object value = mirror.get(key);
        if (value == null) {
            return null;
        }
        String str = String.valueOf(value);
        return str.isEmpty() ? null : str;
    }

    private void invokeIfPresent(String function, Object... args) {
        if (invocable == null) {
            return;
        }

        ClassLoader previous = enterContextClassLoader();
        try {
            invocable.invokeFunction(function, args);
        } catch (NoSuchMethodException ignored) {
        } catch (Throwable throwable) {
            plugin.getLogger().log(Level.SEVERE, String.format(Locale.ROOT, "Error executing %s for expansion %s", function, metadata.getName()), throwable);
        } finally {
            exitContextClassLoader(previous);
        }
    }

    public List<ExpansionScheduledTask> getScheduledTasks() {
        return Collections.unmodifiableList(scheduledTasks);
    }

    public Object callFunction(ScriptObjectMirror function, Object... args) throws Throwable {
        ClassLoader previous = enterContextClassLoader();
        try {
            return function.call(null, args);
        } finally {
            exitContextClassLoader(previous);
        }
    }

    private ClassLoader enterContextClassLoader() {
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(pluginClassLoader);
        return previous;
    }

    private void exitContextClassLoader(ClassLoader previous) {
        Thread.currentThread().setContextClassLoader(previous);
    }
}