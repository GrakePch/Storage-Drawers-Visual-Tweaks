# Storage Drawers Visual Tweaks (storagedrawersvt)

A visual tweaks addon for Storage Drawers: renders the items/blocks shown on drawer fronts as **3D items** like vanilla item frames, instead of flat sprites.

[中文文档（Chinese README）](README_zh.md)

## Requirements

- Minecraft 1.20.1 (Forge 47.x)
- Storage Drawers 12.11.4+ (verified with 12.14.3 on 1.20.1)
- Built with JDK 17

## Installation

1. Make sure `StorageDrawers-forge-1.20.1-12.14.3.jar` is already in your mods folder.
2. Put `storagedrawers-visual-tweaks-0.1.0.jar` into the mods folder.
3. Launch the game.

> Building from source requires the Storage Drawers jar in `libs/` (gitignored — download it from CurseForge yourself).

## Configuration

No config file. Render parameters are hardcoded constants in `StorageDrawersVT.java` (see source structure below).

## How it works

Storage Drawers' `BlockEntityDrawersRenderer.renderFastItem()` flattens the item into a sprite with
`matrix.scale(scaleX, scaleY, 0.001f)`. This mod injects a Mixin at the HEAD of that method and
cancels it, replacing the render with `ItemDisplayContext.FIXED` + 3D scaling — the same rendering
mode vanilla item frames use — so the item appears as a solid 3D model on the drawer front.
Drawer fill indicators and item count text are unaffected.

## Source structure

```
src/main/java/com/grakepch/storagedrawersvt/
├── StorageDrawersVT.java                     # @Mod entry point
└── mixin/BlockEntityDrawersRendererMixin.java # Core Mixin (replaces item rendering)
src/main/resources/
├── META-INF/mods.toml
└── storagedrawersvt.mixins.json              # Mixin config (with refmap)
```

## Building

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
./gradlew build --no-daemon
# Output: build/libs/storagedrawers-visual-tweaks-0.1.0.jar
```

## License

MIT — see [LICENSE](LICENSE).
