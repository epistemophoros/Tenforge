# Tensura: Reincarnated - Addon Example

## About
The following repository was created to assist in the creation of addons for Tensura: Reincarnated, a Minececraft Forge mod. 

This not only serves as an easily downloadable template to speed up the gradle setup, but also has examples of adding the following:

* Common Skills
* Intrinsic Skills
* Extra Skills
* Unique Skills
* Ultimate Skills
* Races
* Gamerules [Todo]
* Config Files [Todo]


## Installation [Todo - Add more detail]
This project should be easy to install and set up. All gradle configuration has been done, so simply open the project in Intellij, and build it.

**You will need to download the latest versions of both Tensura: Reincarnated and ManasCore, and place them inside**
```.\TensuraAddonExample\lib```

Once the build completes, I recommend testing that the client runs using the 'runClient' task. If it does, you can move to modifying the gradle.properties to change the mod_id and mod_name.

Then, update the project structure to your unique namespace, as well as renaming files away from the default "Example" and "tenaddex"

<br>

_More detailed explanations coming soon._

## Example Skills
Right now, the following skills have been added in this mod.

### Example Common
This skill adds in a toggleable effect for an absorption effect.

Explains:
- Toggleable Skills
- Kill Based Unlock Requirements

### Example Extra
This adds in a held skill, that when held increases your speed. When mastered, becomes toggleable

Explains:
- Held Skills
- Mastery Based Toggle Skills

### Example Intrinsic
This adds in a passive skill, that simply adds a permanent speed modifier to the player.
Right now, the code says it is intrinsic to Humans, but currently that doesn't work (not my fault)

_see HumanRaceMixin.java for adding skills to existing races_

Explains:
- Permanent Effects
- Movement Modifiers

### Example Ultimate
This adds a cool ultimate skill. Unlocked by mastering Water Blade, and having 100,000 EP. Shoots waterblades at all mobs in a radius.

Explains:
- Skill Unlock Requirements
- Projectile Usage

### Example Unique
This adds an explosion Unique skill, that has multiple modes.

Explains:
- Handling Skill Modes
- Explosions

## Example Race
Currently added a single race to show the basics. Currently the example does not add in the evolution lines, that may come later.


# Credits
Memoires - Provided an example base addon code that was used to create this repo <br>
Bader - Gradle Setup Support <br>
MinhEragon - Developer of Tensura: Reincarnated <br>
Eclipse - Asset art <br>

Check out the Tensura: Reincarnated mod here: https://www.curseforge.com/minecraft/mc-mods/tensura-reincarnated
