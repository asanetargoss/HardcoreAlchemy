# Hardcore Alchemy
Hardcore Alchemy is two things, a mod and a modpack.

About the Hardcore Alchemy modpack:
* A 1.10.2 hardcore/survival/magic pack
* [See the wiki](https://github.com/asanetargoss/HardcoreAlchemy/wiki) for more information about the pack

About the Hardcore Alchemy mod:
* Adds survival and cross-mod compatibility features meant to complement Metamorph and other magic/survival mods
* Information about the features of the latest release is available at the [Dry Feature List](https://github.com/asanetargoss/HardcoreAlchemy/wiki/Dry-Feature-List) on the wiki
* Releases [here](https://github.com/asanetargoss/HardcoreAlchemy/releases)
* License for the mod is LGPL 3

# Developing/building

## Dependencies
Download the latest zip file and add its contents to /libs (create the folder if it does not exist):

http://www.mediafire.com/folder/grwn2vsjr2lce/Hardcore_Alchemy_Libs

Please note: the following mods included in the zip file above are custom forks:
* Nutrition: https://github.com/asanetargoss/Nutrition
* metamorph: https://github.com/asanetargoss/metamorph

## Setup
* Run "./gradlew setupDecompWorkspace" to set up Minecraft Forge and access transformers. Re-run if you change access transformers (Access transformers are here: "src/main/resources/META-INF/hardcorealchemy_at.cfg")
* Then run "./gradlew eclipse" (or equivalent) to set up your workspace. Re-run each time you add mods to /libs, or otherwise update dependencies.
* Add this VM argument to your run configuration to load the coremod: "-Dfml.coreMods.load=targoss.hardcorealchemy.coremod.HardcoreAlchemyCoremod"

## Other resources
* Decompiler plugin: http://jd.benow.ca/ (Why: Required to view source code for all the mods in /libs, since most do not have dev builds (I may look into cross-compiling later))
* Mod Coder Pack: http://www.modcoderpack.com/ (Why: Use MCP mapping viewer to figure out the meaning of func_12345 and their ilk, and translate to them when needed in a release environment)
* Bytecode outline for Eclipse: http://andrei.gmxhome.de/bytecode/index.html (Why: Only if you need to coremod. It will help you understand the Java bytecode better. Do not trust the stack feature.)