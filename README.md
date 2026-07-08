<div align="center">

# Nightlume Environment

**A visual environment & HUD enhancement client for Minecraft**

`MCP 1.16.5` · `OptiFine` · `Java`

Status: `ROLLING RELEASE` · Version: `v1.1.0`

</div>

---

## Overview

**Nightlume Environment** is a client-side Minecraft modification focused on visual environment enhancement and HUD augmentation. It layers a custom rendering and module system on top of MCP 1.16.5 + OptiFine, giving players a set of purely cosmetic, semi-Lunar-style HUD modules — ambience control, particle effects, custom fog, hit indicators, and more — without touching gameplay-affecting logic.

This is an independent research/hobby project. No packet manipulation, no combat automation, no gameplay advantages — visual and environmental only.

## Features

Nightlume ships as a set of independent, toggleable modules:

| Module | Description |
|---|---|
| `AmbienceModule` | Controls in-world lighting/time mood, with automatic daylight-cycle handling to prevent flicker |
| `NimbusModule` | Custom atmospheric cloud/sky rendering |
| `ChinaHatModule` | Stylized player headwear overlay with adjustable polygon resolution |
| `ParticlesModule` | Custom particle effect system |
| `TrailsModule` | Motion trail rendering |
| `JumpCirclesModule` | Ground-effect rings on player jump |
| `HitColorModule` | Custom hit-flash coloring |
| `HitboxModule` | Adjustable entity hitbox visualization |
| `FinalPostProcessRenderer` | Post-processing pipeline (bloom, color grading, vignette — motion blur currently disabled, see [Known Issues](#known-issues)) |

Every module is built on a shared `Module` base class (`onEnable()` / `onDisable()` hooks, typed settings) and rendered through a custom `ClickGuiScreen`.

## Requirements

- Minecraft **1.16.5**
- **MCP** (ModCoderPack) 1.16.5 environment
- **OptiFine** 1.16.5 (required — no Forge/Fabric mixin support)
- Java 8+

## Architecture

A few deliberate design decisions worth knowing before contributing:

- **Rendering** — All 3D visuals use GL11 immediate mode (`glBegin` / `glVertex3f` / `glEnd`) driven through `RenderSystem.pushMatrix()` / `translated()` / `rotatef()`. Blaze3D's `MatrixStack` is intentionally **not** used for manual vertex pre-transformation — mixing it with `Tessellator.draw()` causes a double camera-transform bug that misplaces geometry in world space.
- **Events** — A custom event bus (`@Subscribe`, not Forge's `@SubscribeEvent`) drives `Render3DEvent` and `TickEvent`. Rendering logic lives directly in each module.
- **Modules** — `ModuleManager` performs class-based lookup; settings are typed (`NumberSetting`, `ColorSetting`, `ModeSetting`, `BooleanSetting`).
- **No Mixins** — this is an OptiFine-only project. There is no packet interception layer, by design.

## Installation

Nightlume ships as a **static launcher** — no MCP dev environment or manual build required to run it.

1. Download the latest launcher build from [Releases](https://nightlume.pages.dev).
2. Run the launcher and select your Minecraft 1.16.5 + OptiFine installation.
3. Launch the game through the launcher.

**Linux** — a proper console-style launcher experience (packaged, terminal-driven) is in the works. Until then, use the standard static launcher above.

> Available Linux platforms can always be checked in our official Telegram channel, and console commands are initialized on our own website.

> Building from source (for contributors) still follows the MCP 1.16.5 + OptiFine workflow described in [Architecture](#architecture).

## Known Issues

- **Motion Blur** (`FinalPostProcessRenderer`) — currently broken and disabled pending a rewrite. Tracked for a future release.
- **Custom Fog** (`FinalPostProcessRenderer`) — currently broken and disabled pending a rewrite. Tracked for a future release.

## Roadmap

- [ ] Console-style launcher package for Linux
- [ ] Motion blur rewrite
- [ ] Additional HUD modules
- [ ] Configuration import/export

## Contributing

This is currently a small, mostly solo project. Issues and PRs are welcome — please open an issue describing the change before submitting larger PRs so it can be discussed against the architecture notes above (especially the GL11/immediate-mode rendering constraint).

## Disclaimer

Nightlume Environment is a visual/cosmetic client modification. It does not include combat automation, packet manipulation, or other gameplay-altering functionality. Use on any given server is subject to that server's own rules — check before use.

## Credits

- **vdubdevil** — System Architect / Core Developer

## License

No license has been set yet — all rights reserved by default until one is added.

---

<div align="center">
<sub>© 2026 Nightlume Project</sub>
</div>
