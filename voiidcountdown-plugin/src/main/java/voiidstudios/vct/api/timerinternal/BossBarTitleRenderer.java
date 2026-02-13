package voiidstudios.vct.api.timerinternal;

import org.bukkit.boss.BossBar;

public class BossBarTitleRenderer {
    public void setTitle(BossBar bossbar, Object formatted, String fallbackLegacyText) {
        try {
            Class<?> componentClass;
            try {
                componentClass = Class.forName("net.kyori.adventure.text.Component");
            } catch (ClassNotFoundException ignored) {
                componentClass = null;
            }

            if (componentClass != null && componentClass.isInstance(formatted)) {
                try {
                    bossbar.getClass().getMethod("setTitle", componentClass).invoke(bossbar, formatted);
                    return;
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException ignored) {}

                try {
                    Class<?> legacyCls = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
                    Object serializer;
                    try {
                        serializer = legacyCls.getMethod("legacySection").invoke(null);
                    } catch (NoSuchMethodException nsme) {
                        Class<?> builderPublicClass = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder");
                        Object builder = legacyCls.getMethod("builder").invoke(null);
                        builderPublicClass.getMethod("character", char.class).invoke(builder, '&');
                        try { builderPublicClass.getMethod("hexColors").invoke(builder); } catch (NoSuchMethodException ignored) {}
                        serializer = builderPublicClass.getMethod("build").invoke(builder);
                    }

                    String legacyTitle = (String) legacyCls.getMethod("serialize", componentClass).invoke(serializer, formatted);
                    bossbar.setTitle(legacyTitle);
                    return;
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}

        if (formatted instanceof String) {
            bossbar.setTitle(((String) formatted).replace('&', '§'));
        } else {
            bossbar.setTitle(fallbackLegacyText.replace('&', '§'));
        }
    }
}
