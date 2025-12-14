# UltraAPI

UltraAPI es un plugin de utilidades compartidas pensado para centralizar la configuración y las comprobaciones de actualizaciones de los proyectos de Voiid Studios.

## ¿Qué es UltraAPI?
- **Objetivo:** ofrecer una capa ligera y reutilizable para cargar configuraciones y validar actualizaciones sin duplicar código en cada plugin.
- **Por qué existe:** evita que cada plugin gestione sus propios archivos `config.yml` y sus propios consumidores HTTP para Modrinth o GitHub.
- **Problemas que resuelve:** unifica el formato de configuración, reduce llamadas redundantes a la red y simplifica el manejo de errores y logs.

## Funcionalidades actuales
- **Sistema de configuración:** `UConfig` envuelve `YamlConfiguration` y gestiona creación, recarga y guardado de archivos. Las configuraciones compartidas viven en `plugins/UltraAPI/config.yml`.
- **Sistema de checkUpdates centralizado:** `UAPI.updates()` expone un servicio asíncrono con soporte para Modrinth (obligatorio) y GitHub Releases (opcional). Los resultados se loguean con mensajes claros en consola y pueden desactivarse con `Updates.enabled`.

## ¿Cómo funciona?
Otros plugins declaran dependencia de UltraAPI y consumen los helpers estáticos de `UAPI`:
```java
import dev.voiidstudios.ultraapi.UltraAPI;
import dev.voiidstudios.ultraapi.config.UConfig;

// Configuración compartida
UConfig config = UltraAPI.config("config.yml");
String textFormat = config.getString("Config.text_format", "LEGACY");

// Check de actualizaciones asíncrono
UltraAPI.updates()
    .check(plugin, "modrinth", "voiid-countdown-timer")
    .thenAccept(result -> {
        if (result.isUpdateAvailable()) {
            plugin.getLogger().info("Nueva versión: " + result.getLatestVersion());
        }
    });
```

### Ejemplo real usado en VCT
```java
UConfig config = UltraAPI.config("config.yml");
boolean notifyUpdates = config.getBoolean("Config.update_notification", true);
UltraAPI.updates().check(this, "modrinth", "voiid-countdown-timer");
```

## Cambios importantes
- El `config.yml` ahora pertenece a UltraAPI y se genera en `plugins/UltraAPI/config.yml`.
- VCT ya no maneja configuración ni checkUpdates directamente: delega en `UConfig` y `UAPI.updates()`.

## Requisitos
- Servidores Paper o Spigot 1.21+.
- UltraAPI debe estar instalado y declarado como `depend` en el `plugin.yml` del consumidor.

## Ejemplo de uso
```java
UConfig config = UAPI.config(plugin);
UAPI.updates().check(plugin, "modrinth", "voiid-countdown-timer");
```
