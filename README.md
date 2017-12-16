# Hardcore Alchemy

# Running with Minecraft
* This mod is for 1.10.2
* See the wiki for documentation for the modpack, which includes this mod
* No license yet (ie do not distribute/all rights reserved)
* The mod will be made available by itself at a later time

# Developing/building

## Dependencies
Download this zip file and add its contents to /libs:

http://www.mediafire.com/file/271ibuiu85xabo7/HcA_libs_0.2.1.zip

Please note: the following mods in the modpack above are custom forks:
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