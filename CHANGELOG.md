# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-08-13

### Added

- Initial release: Storage Drawers Visual Tweaks for Minecraft 1.20.1 (Forge 47.x).
- Renders drawer front items as 3D items (like vanilla item frames) instead of flat sprites.
  - Mixin on `BlockEntityDrawersRenderer.renderFastItem()`: cancels the flat sprite pass and
    renders with `ItemDisplayContext.FIXED` + 3D scaling.
- Compatible with Storage Drawers 12.11.4+ (verified with 12.14.3 on 1.20.1).
- Fill indicators and item count text on drawer fronts are unaffected.
- No config file: render parameters are hardcoded constants.
- MIT licensed; English and Chinese README.
