# Hardcore Alchemy
Hardcore Alchemy is a hardcore/magic/survival modpack built for Minecraft 1.10.2.

You are currently in the source code repository for the Hardcore Alchemy capstone mod, which powers many of Hardcore Alchemy's most unique features. The best way to learn more information about the modpack is to [visit the wiki](https://github.com/asanetargoss/HardcoreAlchemy/wiki).

## Contributing

Aside from feedback on the modpack itself, the following contributions are welcome:

- **Translations** - Are always welcome. There are several language files in the [resource folder](https://github.com/asanetargoss/HardcoreAlchemy/tree/master/src/main/resources/), for example `hardcorealchemy_modpack_guide/lang/en_US.lang`.
- **Code** - If it's a bugfix, and it's faster to write the code than it is to explain it, go right ahead. Otherwise, let's talk about it first

## Using the capstone mod

The Hardcore Alchemy capstone mod is released under the LGPL 3, so you are allowed to use it in your own modpack. However, the mod was built for the HcA modpack, and as such, was designed with a particular gameplay experience in mind. Config options will not be added except for compatibility with vanilla hardcore mode and serious accessibility concerns.

What the Hardcore Alchemy mod is designed for:

* **Balanced shapeshifting** - [Changeling](https://github.com/asanetargoss/Changeling/releases) powers Hardcore Alchemy's balanced morphing features, and is a required dependency.
  * Config settings: acquire_immediately=false, disable_morph_disguise=true, keep_morphs=false, others defaults
* **Random respawn on death** - The Hardcore Alchemy mod is designed around hardcore respawn. For this, I recommend [Iberia HcA edition](https://github.com/asanetargoss/iberia/releases).

The Hardcore Alchemy mod also has special integration with various other mods. These mods are not required dependencies, but should be used instead of the alternatives:

* [Hwyla](https://www.curseforge.com/minecraft/mc-mods/hwyla) - In-game overlays.
* [Nutrition HcA Edition](https://github.com/asanetargoss/Nutrition/releases) and/or [Spice of Life](https://www.curseforge.com/minecraft/mc-mods/the-spice-of-life) - For nutrition.
  * The official version of the Nutrition mod will not work, and HcA assumes the default 5 nutrients
* [Pam's Harvestcraft](https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft) - Right-click harvesting and food support
* [Tough as Nails](https://www.curseforge.com/minecraft/mc-mods/tough-as-nails) - Thirst
* [Minecraft Comes Alive](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1280154-mc-1-12-x-minecraft-comes-alive-v6-0-0-millions) and/or [Village Box](https://www.curseforge.com/minecraft/mc-mods/village-box) - Villager alternatives

Finally, the Hardcore Alchemy capstone mod integrates with many magic mods.

There is a lot more in terms of mod selection, configuration, and tweaking, in order for a hardcore modpack to work well. The stakes are so high, that issues which would be small annoyances in a typical modpack can become deal-breakers that ruin the experience. In the hardcore respawn case, there is an additional issue of balancing what the player gets to retain after death. So, keep that in mind. :)

## Developing/building

### Dependencies

This branch targets the 0.3.12+ version of the modpack. Download the 0.3.12 zip file from the link below and add its contents to libs/ (create the folder if it does not exist):

http://www.mediafire.com/folder/grwn2vsjr2lce/Hardcore_Alchemy_Libs

The SHA-256 checksum of the 0.3.12 zip is: a7dcf987ecf4eed69e05be29efe6fed9ccb98e9f18593500ee1bdb228d7c7696

Please note: the following mods included in the HcA_libs zip file above are custom forks:
* Nutrition: https://github.com/asanetargoss/Nutrition
* Changeling: https://github.com/asanetargoss/Changeling
* Dissolution ([permission](https://i.imgur.com/b7sN6lL.png))
* Iberia: https://github.com/asanetargoss/iberia
* Guide-API (Custom build for development use only. Do not distribute.)
* AppleCore: https://github.com/asanetargoss/AppleCore

In addition, you may also want to take the latest available config zip from the same folder and place its contents in run/config/. This will make some aspects of development easier, such as not losing your spawnpoint when testing deaths.

### Setup
* Run "./gradlew setupDecompWorkspace" to set up Minecraft Forge and access transformers.
* Then run "./gradlew eclipse" (or the equivalent for IntelliJ Idea as given in the [Forge Gradle docs](https://forgegradle.readthedocs.io/en/latest/))
* Add this VM argument to your run configuration to load the coremod: "-Dfml.coreMods.load=targoss.hardcorealchemy.coremod.HardcoreAlchemyCoremod"

### Compiling
* To compile, run "./gradlew assemble". Output will be in build/libs/. The jar name will be hardcorealchemy-[version].jar where [version] is defined in build.gradle.

### Development Tips
* Successfully used gradle commands before but they aren't working anymore because your internet is down? No problem! Just add the "-offline" flag to your gradle command and it should work normally again.
* If you add new mods to libs/, or otherwise update dependencies, you will need to re-run "./gradlew eclipse" or equivalent
* If you change the access transformers (found at "src/main/resources/META-INF/hardcorealchemy_at.cfg") you will need to re-run "./gradlew setupDecompWorkspace", and re-run "./gradlew eclipse" (or IntelliJ Idea equivalent)
* If you encounter an unexplained NoClassDefFoundError or NoSuchMethodError after doing development with a different mod, and your dependencies (mods in libs folder) are up-to-date, it may be an issue with CodeChickenCore. Simply re-run "./gradlew setupDecompWorkspace", and re-run "./gradlew eclipse" (or IntelliJ Idea equivalent).

### Other resources
* Decompiler plugin: http://jd.benow.ca/ (Why: Required to view source code for all the mods in /libs, since most do not have dev builds)
* MCP Mapping Viewer: https://github.com/bspkrs/MCPMappingViewer/ (Why: To figure out the meaning of func_12345 and their ilk, and translate to them when needed in a release environment)
* Bytecode outline for Eclipse: http://andrei.gmxhome.de/bytecode/index.html (Why: Only if you need to coremod. It will help you understand the Java bytecode better. Do not trust the stack feature.)

