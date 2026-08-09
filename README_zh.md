# Storage Drawers Visual Tweaks（storagedrawersvt）

Storage Drawers 附属 mod：把抽屉正面展示的方块/物品从“平面贴图”改成类似原版物品展示框（Item Frame）的 **3D 物品**。

## 版本要求

- Minecraft 1.20.1（Forge 47.x）
- Storage Drawers 12.11.4+（1.20.1 已按 12.14.3 验证）
- JDK 17 构建

## 安装

1. 确认 mods 文件夹里已有 `StorageDrawers-forge-1.20.1-12.14.3.jar`
2. 把 `storagedrawers-visual-tweaks-1.0.0.jar` 放进 mods 文件夹
3. 启动游戏

> 从源码构建需要 Storage Drawers 的 jar 放在 `libs/`（已 gitignore，需自行从 CurseForge 下载）。

## 配置

无配置文件。渲染参数为硬编码常量（见下方源码结构）。

## 原理

StorageDrawers 的 `BlockEntityDrawersRenderer.renderFastItem()` 用
`matrix.scale(scaleX, scaleY, 0.001f)` 把物品压扁成贴图。本 mod 用 Mixin 在该方法
HEAD 注入并 cancel，改为 `ItemDisplayContext.FIXED` + 3D 缩放渲染（原版展示框的渲染
模式），物品以立体形态显示在抽屉正面。抽屉的填充指示条、数量文字等不受影响。

## 源码结构

```
src/main/java/com/grakepch/storagedrawersvt/
├── StorageDrawersVT.java                      # @Mod 入口
└── mixin/BlockEntityDrawersRendererMixin.java # 核心 Mixin（替换物品渲染）
src/main/resources/
├── META-INF/mods.toml
└── storagedrawersvt.mixins.json               # Mixin 配置（含 refmap）
```

## 构建

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
./gradlew build --no-daemon
# 产物：build/libs/storagedrawers-visual-tweaks-1.0.0.jar
```

## 许可证

MIT —— 见 [LICENSE](LICENSE)。
