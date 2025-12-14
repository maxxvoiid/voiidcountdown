# UltraAPI

UltraAPI es una librería de utilidades compartidas pensada para centralizar la configuración y las comprobaciones de actualizaciones de los proyectos de Voiid Studios.

## ¿Qué es UltraAPI?
- **Objetivo:** ofrecer una capa ligera y reutilizable para cargar configuraciones y validar actualizaciones sin duplicar código en cada plugin.
- **Por qué existe:** evita que cada plugin reimplemente wrappers de configuración o consumidores HTTP para Modrinth o GitHub.
- **Problemas que resuelve:** unifica la forma de acceder a `config.yml`, reduce llamadas redundantes a la red y simplifica el manejo de errores y logs.

## Funcionalidades actuales
- **Sistema de configuración:** `UConfig` envuelve `YamlConfiguration` y gestiona creación, recarga y guardado de archivos usando SIEMPRE la carpeta del plugin consumidor. UltraAPI no genera archivos propios.
- **Sistema de checkUpdates centralizado:** `UAPI.updates()` expone un servicio asíncrono con soporte para Modrinth (obligatorio) y GitHub Releases (opcional). Los resultados se loguean con mensajes claros en consola y pueden desactivarse con `Updates.enabled` dentro del `config.yml` del plugin consumidor.

## ¿Cómo funciona?
Otros plugins declaran dependencia de UltraAPI y consumen los helpers estáticos de `UAPI` apuntando SIEMPRE a su propia carpeta de datos:
```java
import dev.voiidstudios.ultraapi.UltraAPI;
import dev.voiidstudios.ultraapi.config.UConfig;

// Configuración compartida
UConfig config = UAPI.config(plugin, "config.yml");
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
UConfig config = UAPI.config(this, "config.yml");
boolean notifyUpdates = config.getBoolean("Config.update_notification", true);
UAPI.updates().check(this, "modrinth", "voiid-countdown-timer");
```

## Cambios importantes
- El `config.yml` vive en la carpeta del plugin consumidor (por ejemplo `plugins/VoiidCountdownTimer/config.yml`).
- VCT ya no maneja configuración ni checkUpdates directamente: delega en `UConfig` y `UAPI.updates()` apuntando a su propio data folder.

## Requisitos
- Servidores Paper o Spigot 1.21+.
- UltraAPI debe estar instalado y declarado como `depend` en el `plugin.yml` del consumidor.

## Ejemplo de uso
```java
UConfig config = UAPI.config(plugin, "config.yml");
UAPI.updates().check(plugin, "modrinth", "voiid-countdown-timer");
```

## ❗ Importante

UltraAPI actúa únicamente como una librería compartida. No crea carpetas, no genera archivos y no ejecuta tareas por sí misma. Todas las configuraciones, datos y ejecuciones pertenecen exclusivamente al plugin que la utiliza.
