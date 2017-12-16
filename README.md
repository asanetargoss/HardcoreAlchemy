# Hardcore Alchemy
Hardcore Alchemy is two things, a mod and a modpack.

About the Hardcore Alchemy modpack:
* A 1.10.2 hardcore/survival/magic pack
* [See the wiki](https://github.com/asanetargoss/HardcoreAlchemy/wiki) for more information about the pack

About the Hardcore Alchemy mod:
* Add miscellaneous survival and cross-mod compatibility features meant to complement the modpack
* Will be released on its own at a later time. No license yet. (ie do not distribute/all rights reserved)

# Developing/building

## Dependencies
Download this zip file and add its contents to /libs:

http://www.mediafire.com/file/271ibuiu85xabo7/HcA_libs_0.2.1.zip

Please note: the following mods included in the zip file above are custom forks:
* Nutrition: https://github.com/asanetargoss/Nutrition
* metamorph: https://github.com/asanetargoss/metamorph

## Setup
* Run "./gradlew eclipse" (or equivalent) to set up your workspace. Re-run each time you add mods to /libs
* Then run "./gradlew setupDecompWorkspace" to set up Minecraft Forge and access transformers. Re-run if you change access transformers (Access transformers are here: "src/main/resources/META-INF/hardcorealchemy_at.cfg")
* Add this VM argument to your run configuration to load the coremod: "-Dfml.coreMods.load=targoss.hardcorealchemy.coremod.HardcoreAlchemyCoreMod"

## Other tips
I highly recommend installing these plugins in your IDE:
* Decompiler plugin: http://jd.benow.ca/ (Why: Required to view source code for all the mods in /libs, since most do not have dev builds (I may look into cross-compiling later))
* Bytecode outline for Eclipse: http://andrei.gmxhome.de/bytecode/index.html (Why: Only if you need to coremod. It will help you understand the Java bytecode better. Do not trust the stack feature.)