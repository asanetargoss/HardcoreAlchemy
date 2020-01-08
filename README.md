# Hardcore Alchemy
Hardcore Alchemy is two things, a mod and a modpack.

About the Hardcore Alchemy modpack:
* A 1.10.2 hardcore/survival/magic pack
* [See the wiki](https://github.com/asanetargoss/HardcoreAlchemy/wiki) for more information about the pack

About the Hardcore Alchemy mod:
* Adds survival and cross-mod compatibility features meant to complement shapeshifting and other magic/survival mods
* Currently requires, at minimum, [Changeling](https://github.com/asanetargoss/metamorph/releases), [Wawla](https://minecraft.curseforge.com/projects/wawla-what-are-we-looking-at), and some sort of mod with a random respawn mechanic, such as [Iberia](https://github.com/asanetargoss/iberia/releases) or [Better with Mods](https://minecraft.curseforge.com/projects/better-with-mods). See the links provided, as some are required custom versions.
* [Nutrition](https://github.com/asanetargoss/Nutrition) is not required, but the custom version provided must be used.
* Releases and changelogs [here](https://github.com/asanetargoss/HardcoreAlchemy/releases)
* License for the mod is LGPL 3

# Developing/building

## Dependencies

**NOTE: The new libs file contains a version of [AppleCore](https://github.com/asanetargoss/AppleCore) which fixes crashes in a dev environment.**

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

## Setup
* Run "./gradlew setupDecompWorkspace" to set up Minecraft Forge and access transformers.
* Then run "./gradlew eclipse" (or the equivalent for IntelliJ Idea as given in the [Forge Gradle docs](https://forgegradle.readthedocs.io/en/latest/))
* Add this VM argument to your run configuration to load the coremod: "-Dfml.coreMods.load=targoss.hardcorealchemy.coremod.HardcoreAlchemyCoremod"

## Compiling
* To compile, run "./gradlew assemble". Output will be in build/libs/. The jar name will be hardcorealchemy-[version].jar where [version] is defined in build.gradle.

## Development Tips
* Successfully used gradle commands before but they aren't working anymore because your internet is down? No problem! Just add the "-offline" flag to your gradle command and it should work normally again.
* If you add new mods to libs/, or otherwise update dependencies, you will need to re-run "./gradlew eclipse" or equivalent
* If you change the access transformers (found at "src/main/resources/META-INF/hardcorealchemy_at.cfg") you will need to re-run "./gradlew setupDecompWorkspace", and re-run "./gradlew eclipse" (or IntelliJ Idea equivalent)
* If you encounter an unexplained NoClassDefFoundError or NoSuchMethodError after doing development with a different mod, and your dependencies (mods in libs folder) are up-to-date, it may be an issue with CodeChickenCore. Simply re-run "./gradlew setupDecompWorkspace", and re-run "./gradlew eclipse" (or IntelliJ Idea equivalent).

## Other resources
* Decompiler plugin: http://jd.benow.ca/ (Why: Required to view source code for all the mods in /libs, since most do not have dev builds)
* MCP Mapping Viewer: https://github.com/bspkrs/MCPMappingViewer/ (Why: To figure out the meaning of func_12345 and their ilk, and translate to them when needed in a release environment)
* Bytecode outline for Eclipse: http://andrei.gmxhome.de/bytecode/index.html (Why: Only if you need to coremod. It will help you understand the Java bytecode better. Do not trust the stack feature.)
