# VisualsFabric

> **An optimized Fabric 26.3/1.21.4 mod purely for visuals** — shaders, rendering enhancements, and visual effects.

[![Fabric](https://img.shields.io/badge/Fabric-0.20.1+-purple.svg)](https://fabricmc.net/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.4%20%7C%2026.3-green.svg)](https://www.minecraft.net/)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Build](https://github.com/Hexamania/visualsfabric/actions/workflows/build.yml/badge.svg)](https://github.com/Hexamania/visualsfabric/actions/workflows/build.yml)

---

## 🎨 Features

### Post-Processing Effects
| Effect | Key | Description |
|--------|-----|-------------|
| **Bloom** | `B` | Separable Gaussian blur for bright areas |
| **Chromatic Aberration** | `C` | RGB channel separation for lens distortion |
| **Film Grain** | `G` | Animated FBM noise overlay |
| **Vignette** | `V` | Smooth radial darkening at screen edges |
| **Color Grading** | `R` | ACES tone mapping + ASC CDL (Saturation, Contrast, Brightness, Gamma) |
| **Depth of Field** | `D` | Circle of confusion based blur (WIP) |
| **Motion Blur** | `M` | Velocity buffer based blur (WIP) |
| **Enhanced AO** | `A` | Screen-space ambient occlusion enhancement |

### Configuration
- **In-game GUI** — Press `F6` to open
- **Per-effect toggles** — Enable/disable individual effects
- **Intensity sliders** — Fine-tune each effect
- **Remappable key bindings** — Change keys in GUI
- **Reset to defaults** — One-click restore

---

## 📥 Installation

1. Install **Fabric Loader 0.20.1+** (0.20.2+ recommended for 26.3)
2. Install **Fabric API 0.166+** for your version
3. Download the latest `visualsfabric-1.0.0.jar` from [Releases](https://github.com/Hexamania/visualsfabric/releases)
4. Place in your `mods` folder
5. Launch Minecraft!

> ⚠️ **Note for 26.3 users:** Fabric Loader 0.20.1 has a known bug with intermediary mappings. Use **0.20.2 or newer**.

---

## ⌨️ Default Key Bindings

| Key | Action |
|-----|--------|
| `B` | Toggle Bloom |
| `C` | Toggle Chromatic Aberration |
| `G` | Toggle Film Grain |
| `V` | Toggle Vignette |
| `R` | Toggle Color Grading |
| `D` | Toggle Depth of Field |
| `M` | Toggle Motion Blur |
| `A` | Toggle Enhanced AO |
| `F6` | Open Configuration GUI |

All keys are remappable in the config screen.

---

## 🛠️ Building from Source

### Requirements
- JDK 21+
- Gradle 9.0 (wrapper included)

### Build
```bash
./gradlew clean build
```

Output: `build/libs/visualsfabric-1.0.0.jar`

### Run Dev Client
```bash
./gradlew runClient
```

---

## 📦 Dependencies

| Dependency | Version |
|------------|---------|
| Fabric Loader | ≥0.20.1 |
| Minecraft | 1.21.4 (26.3) |
| Fabric API | * |
| Java | 21+ |

---

## 🤖 AI-Assisted Development

This project was developed with assistance from **Claude Code** (Anthropic's AI coding assistant). The codebase, shaders, configuration system, and build pipeline were created through iterative AI-human collaboration.

> **Disclaimer:** AI-generated code has been reviewed and tested, but may contain imperfections. Please report issues on GitHub.

---

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.

---

## 🙏 Credits

- **FabricMC** — Modding platform
- **Yarn** — Mappings
- **Loom** — Build system
- **OpenGL/GLSL** — Shader reference

---

## 🔗 Links

- **GitHub:** https://github.com/Hexamania/visualsfabric
- **Issues:** https://github.com/Hexamania/visualsfabric/issues
- **Discord:** https://discord.gg/fabric