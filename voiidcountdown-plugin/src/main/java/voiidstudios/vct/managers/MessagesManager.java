package voiidstudios.vct.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.configs.ConfigsManager;
import voiidstudios.vct.configs.MainConfigManager;

import java.util.List;
import java.util.Map;

public class MessagesManager {
    private static String prefix;
    private final TranslationManager translations;
    private static ConfigsManager configsManager;

    public MessagesManager(VoiidCountdownTimer plugin) {
        configsManager = plugin.getConfigsManager();
        this.translations = new TranslationManager(plugin);

        this.translations.loadLanguage(
                configsManager.getMainConfigManager().getLanguage()
        );
    }

    public void loadLanguage(String lang) {
        translations.loadLanguage(lang);
    }

    public static void setPrefix(String p) {
        prefix = p;
    }

    private String applyPrefix(boolean usePrefix, String msg) {
        return (usePrefix ? prefix : "") + msg;
    }

    public String color(String msg) {
        if (msg == null) return "§cMissing message";
        return msg.replace("&", "§");
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, true, null);
    }

    public void send(CommandSender sender, String key, Map<String, String> repl) {
        send(sender, key, true, repl);
    }

    public void send(CommandSender sender, String key, boolean usePrefix) {
        send(sender, key, usePrefix, null);
    }

    public void send(CommandSender sender, String key, boolean usePrefix, Map<String, String> repl) {
        String msg = translations.formatKey(key, repl);
        if (msg == null) {
            msg = "§cMissing message: " + key;
        }
        sender.sendMessage(color(applyPrefix(usePrefix, msg)));
    }

    private void sendRaw(CommandSender sender, String msg, boolean usePrefix) {
        if (msg == null) return;
        sender.sendMessage(color(applyPrefix(usePrefix, msg)));
    }

    public void sendList(CommandSender sender, String key, boolean firstHasPrefix, Map<String, String> repl) {
        List<String> lines = translations.getStringList(key);
        if (lines.isEmpty()) return;

        boolean first = true;

        for (String lineRaw : lines) {
            String line = translations.formatRaw(lineRaw, repl);

            if (first) {
                sendRaw(sender, line, firstHasPrefix);
                first = false;
            } else {
                sendRaw(sender, line, false);
            }
        }
    }

    public void sendSection(CommandSender sender, String baseKey, boolean headerHasPrefix, Map<String, String> repl) {
        String headerRaw = translations.get(baseKey + ".header");
        if (headerRaw != null) {
            String header = translations.formatRaw(headerRaw, repl);
            sendRaw(sender, header, headerHasPrefix);
        }

        List<String> lines = translations.getStringList(baseKey + ".lines");
        for (String lineRaw : lines) {
            String line = translations.formatRaw(lineRaw, repl);
            sendRaw(sender, line, false);
        }
    }

    public List<String> getList(String key) {
        return translations.getStringList(key);
    }

    public TranslationManager.TranslationBundle getCustomTranslations(String namespace) {
        return translations.forCustomNamespace(namespace);
    }

    public void console(String msg) {
        Bukkit.getConsoleSender().sendMessage(color(msg));
    }

    public void debug(String message) {
        if (configsManager == null) return;

        MainConfigManager main = configsManager.getMainConfigManager();
        if (main != null && main.isDebug_mode()) {
            console(prefix + "&8[DEBUG] &r" + message);
        }
    }
}