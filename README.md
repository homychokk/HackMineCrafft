# HackMineCrafft

**HackMineCrafft** — Fabric-клиент для Minecraft **1.20 - 1.20.1** с меню в стиле liquid glass, модулями и кастомным UI.

Открытие меню: **Right Shift**

## Установка

### Для Fabric (1.20 - 1.20.1):
1. Установи [Fabric Loader](https://fabricmc.net/use/) для Minecraft 1.20 или 1.20.1
2. Положи в папку `mods`:
   - [Fabric API](https://modrinth.com/mod/fabric-api) (подходящей версии)
   - `hackminecrafft-1.0.0.jar` из [Releases](https://github.com/homychokk/HackMineCrafft/releases)
3. Запусти игру

### Для Forge (1.20.1):
1. Установи Forge для Minecraft 1.20.1
2. Положи в папку `mods`:
   - [Sinytra Connector](https://modrinth.com/mod/connector) (версия для 1.20.1)
   - [Forgified Fabric API](https://modrinth.com/mod/forgified-fabric-api) (версия для 1.20.1)
   - `hackminecrafft-1.0.0.jar` из [Releases](https://github.com/homychokk/HackMineCrafft/releases)
3. Запусти игру

## Возможности

- Liquid glass GUI с категориями, настройками и биндами
- Combat: KillAura, AutoCrit, Hitboxes, Velocity
- Movement: Fly, Speed, Scaffold, NoFall, FreeCam (лут сундуков)
- Render: XRay с подсветкой руд, StorageESP, ESP, Tracers, PlayerFX
- Misc: AutoFarm, Nuker, AutoTool
- HUD: Watermark, FPS, ArrayList и др.

## Сборка

```bash
# Java 17+
gradle build
```

Готовый jar: `build/libs/hackminecrafft-1.0.0.jar`

## Лицензия

MIT — см. [LICENSE](LICENSE)
