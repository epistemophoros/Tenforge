# Tenforge

A skill forging addon for **Tensura: Reincarnated** (Minecraft Forge 1.19.2)

## Features

### 🔨 Skill Forge System
Inspired by skills like Raphael and Creator from "That Time I Got Reincarnated as a Slime":

- **Break Down Skills** → Convert any skill into Skill Energy
- **Forge New Skills** → Use Skill Energy + EP to create any skill
- **Cross-Mod Compatible** → Works with all ManasCore-based skills

### Energy System

| Skill Tier | Breakdown Energy | Forge Cost |
|------------|-----------------|------------|
| Common | 500 | 750 |
| Intrinsic | 1,000 | 1,500 |
| Extra | 2,500 | 3,750 |
| Unique | 10,000 | 15,000 |
| Ultimate | 50,000 | 75,000 |

*Mastery level adds up to 100% bonus energy!*

### GUI Features
- Search/filter skills by name
- Filter by tier (Common, Unique, Ultimate, etc.)
- Preview energy yields before breakdown
- See creation costs before forging

## Installation

1. Install **Minecraft Forge 1.19.2**
2. Install **Tensura: Reincarnated** and **ManasCore**
3. Download the latest Tenforge JAR from releases
4. Place in your `mods` folder

## Development Setup

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/tenforge.git
cd tenforge

# Build
./gradlew build

# Run client for testing
./gradlew runClient
```

## Project Structure

```
tenforge/
├── ability/skill/          # Skill implementations
│   └── unique/
│       └── SkillForgeSkill.java
├── capability/             # Player data storage
│   └── SkillEnergyCapability.java
├── data/                   # Optimized registries
│   ├── SkillRegistry.java
│   └── SkillTransferManager.java
├── gui/                    # GUI system
│   ├── SkillForgeMenu.java
│   └── SkillForgeScreen.java
├── network/                # Packets
└── registry/               # Forge registries
```

## Adding Custom Skills

1. Create skill class extending `Skill`
2. Register in `AllSkills.java`
3. Add translations in `en_us.json`
4. Add texture in `textures/skill/`

## License

MIT
