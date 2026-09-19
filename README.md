# Minecraft: Gamble

Every step or jump rolls the wheel of fate.

Supported: **Minecraft 26.3** on **Fabric** and **NeoForge**.
(Minecraft Forge has no 26.x builds, so there is no Forge version.)

## Layout

| Folder      | What it is                                                        |
|-------------|-------------------------------------------------------------------|
| `common/`   | All gameplay code. Shared — compiled into both jars.              |
| `fabric/`   | Fabric entrypoints (`GambleMod`, `GambleModClient`) + `fabric.mod.json` |
| `neoforge/` | NeoForge entrypoints + `neoforge.mods.toml`                        |

Version numbers (Minecraft, Fabric API, NeoForge, plugins) all live in `gradle.properties`.

## Building

```bash
gradle wrapper --gradle-version 9.6.0   # first time only
./gradlew build
```

Output jars:

* `fabric/build/libs/gamble-mod-fabric-1.0.0.jar`
* `neoforge/build/libs/gamble-mod-neoforge-1.0.0.jar`

## Running in dev

```bash
./gradlew :fabric:runClient
./gradlew :neoforge:runClient
```
