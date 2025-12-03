package voiidstudios.vct.utils;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import voiidstudios.vct.VoiidCountdownTimer;

public enum Formatter {
    MINIMESSAGE(
            (plugin, player, text) -> {
                ServerVersion serverVersion = VoiidCountdownTimer.serverVersion;
                if (plugin.getDependencyManager().isPaper() && serverVersion.serverVersionGreaterEqualThan(serverVersion, ServerVersion.v1_19_R3)) {
                    try {
                        Class<?> miniMessageClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
                        Object miniMessage = miniMessageClass.getMethod("miniMessage").invoke(null);

                        try {
                            return miniMessageClass.getMethod("deserialize", String.class).invoke(miniMessage, text);
                        } catch (NoSuchMethodException ignored) {}
                        try {
                            return miniMessageClass.getMethod("deserialize", CharSequence.class).invoke(miniMessage, text);
                        } catch (NoSuchMethodException ignored) {}
                        try {
                            Class<?> tagResolverClass = Class.forName("net.kyori.adventure.text.minimessage.tag.resolver.TagResolver");
                            Object emptyResolvers = java.lang.reflect.Array.newInstance(tagResolverClass, 0);
                            return miniMessageClass
                                    .getMethod("deserialize", String.class, emptyResolvers.getClass())
                                    .invoke(miniMessage, text, emptyResolvers);
                        } catch (NoSuchMethodException | ClassNotFoundException ignored) {}
                    } catch (Exception e) {
                        plugin.getLogger().warning("MiniMessage unavailable: " + e.getClass().getName() + ": " + e.getMessage());
                    }
                }
                return text.replace("&", "§"); // fallback
            },
            "MiniMessage"
    ),
    LEGACY(
            (plugin, player, text) -> {
                try {
                    Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
                } catch (ClassNotFoundException e) {
                    return text.replace("&", "§");
                }

                try {
                    Class<?> legacyCls = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
                    Class<?> builderPublicClass = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder");

                    Object builder = legacyCls.getMethod("builder").invoke(null);
                    builderPublicClass.getMethod("character", char.class).invoke(builder, '&');
                    try { builderPublicClass.getMethod("hexCharacter", char.class).invoke(builder, '#'); } catch (NoSuchMethodException ignored) {}
                    try { builderPublicClass.getMethod("useUnusualXRepeatedCharacterHexFormat").invoke(builder); } catch (NoSuchMethodException ignored) {}
                    try { builderPublicClass.getMethod("hexColors").invoke(builder); } catch (NoSuchMethodException ignored) {}
                    Object serializer = builderPublicClass.getMethod("build").invoke(builder);

                    return legacyCls.getMethod("deserialize", String.class).invoke(serializer, text);
                } catch (Exception e) {
                    plugin.getLogger().warning("Legacy unavailable: " + e.getClass().getName() + ": " + e.getMessage());

                    return text.replace("&", "§");
                }
            },
            "Legacy"
    ),
    UNIVERSAL(
            (plugin, player, text) -> {
                ServerVersion serverVersion = VoiidCountdownTimer.serverVersion;

                boolean miniCompatible = plugin.getDependencyManager().isPaper()
                        && serverVersion.serverVersionGreaterEqualThan(serverVersion, ServerVersion.v1_19_R3)
                        && hasMiniMessage();

                if (!hasLegacySerializer() || !miniCompatible) {
                    return text.replace("&", "§");
                }

                try {
                    Class<?> legacyCls = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
                    Class<?> builderPublicClass = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder");

                    Object builderLegacy = legacyCls.getMethod("builder").invoke(null);
                    builderPublicClass.getMethod("character", char.class).invoke(builderLegacy, '&');
                    builderPublicClass.getMethod("hexCharacter", char.class).invoke(builderLegacy, '#');
                    try {
                        builderPublicClass.getMethod("useUnusualXRepeatedCharacterHexFormat").invoke(builderLegacy);
                    } catch (NoSuchMethodException ignored) {}
                    try {
                        builderPublicClass.getMethod("hexColors").invoke(builderLegacy);
                    } catch (NoSuchMethodException ignored) {}
                    Object legacySerializer = builderPublicClass.getMethod("build").invoke(builderLegacy);

                    Object builderHex = legacyCls.getMethod("builder").invoke(null);
                    builderPublicClass.getMethod("character", char.class).invoke(builderHex, '&');
                    builderPublicClass.getMethod("hexCharacter", char.class).invoke(builderHex, '#');
                    try {
                        builderPublicClass.getMethod("hexColors").invoke(builderHex);
                    } catch (NoSuchMethodException ignored) {}
                    Object hexSerializer = builderPublicClass.getMethod("build").invoke(builderHex);

                    Object compFromLegacy = legacyCls.getMethod("deserialize", String.class).invoke(legacySerializer, text);

                    String legacySerialized = (String) legacyCls
                            .getMethod("serialize", Class.forName("net.kyori.adventure.text.Component"))
                            .invoke(hexSerializer, compFromLegacy);

                    Object component = legacyCls.getMethod("deserialize", String.class).invoke(hexSerializer, legacySerialized);

                    Class<?> miniCls = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
                    Object mini = miniCls.getMethod("miniMessage").invoke(null);

                    String miniSerialized = (String) miniCls
                            .getMethod("serialize", Class.forName("net.kyori.adventure.text.Component"))
                            .invoke(mini, component);

                    String cleaned = miniSerialized.replace("\\<", "<").replace("\\\\", "");

                    try {
                        return miniCls.getMethod("deserialize", String.class).invoke(mini, cleaned);
                    } catch (NoSuchMethodException ignored) {}

                    try {
                        return miniCls.getMethod("deserialize", CharSequence.class).invoke(mini, cleaned);
                    } catch (NoSuchMethodException ignored) {}

                    try {
                        Class<?> tagResolverClass = Class.forName("net.kyori.adventure.text.minimessage.tag.resolver.TagResolver");
                        Object emptyResolvers = java.lang.reflect.Array.newInstance(tagResolverClass, 0);
                        return miniCls.getMethod("deserialize", String.class, emptyResolvers.getClass()).invoke(mini, cleaned, emptyResolvers);
                    } catch (NoSuchMethodException | ClassNotFoundException ignored) {}

                } catch (Exception e) {
                    plugin.getLogger().warning("Universal unavailable: " + e.getClass().getName() + ": " + e.getMessage());
                }

                // fallback
                return text.replace("&", "§");
            },
            "Universal"
    );

    private static Boolean HAS_LEGACY_SERIALIZER = null;
    private static Boolean HAS_MINI_MESSAGE = null;

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean hasLegacySerializer() {
        if (HAS_LEGACY_SERIALIZER == null) {
            HAS_LEGACY_SERIALIZER = classExists("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
        }
        return HAS_LEGACY_SERIALIZER;
    }

    private static boolean hasMiniMessage() {
        if (HAS_MINI_MESSAGE == null) {
            HAS_MINI_MESSAGE = classExists("net.kyori.adventure.text.minimessage.MiniMessage");
        }
        return HAS_MINI_MESSAGE;
    }

    private final TriFunction<VoiidCountdownTimer, CommandSender, String, Object> formatter;
    private final String name;

    Formatter(TriFunction<VoiidCountdownTimer, CommandSender, String, Object> formatter, String name) {
        this.formatter = formatter;
        this.name = name;
    }

    public Object format(@NotNull VoiidCountdownTimer plugin, @NotNull CommandSender audience, @NotNull String text) {
        return formatter.apply(plugin, audience, text);
    }
}