# Hardcore Alchemy
Hardcore Alchemy is a hardcore/magic/survival modpack built for Minecraft 1.10.2.

You are currently in the source code repository for the Hardcore Alchemy capstone mod, which powers many of Hardcore Alchemy's most unique features. The best way to learn more information about the modpack is to [visit the wiki](https://github.com/asanetargoss/HardcoreAlchemy/wiki).

## Contributing

Aside from feedback on the modpack itself, the following contributions are welcome:

- **Translations** - Are always welcome. There are several language files in the [resource folder](https://github.com/asanetargoss/HardcoreAlchemy/tree/master/src/main/resources/), for example `hardcorealchemy_modpack_guide/lang/en_US.lang`.
- **Code** - If it's a bugfix, and it's faster to write the code than it is to explain it, go right ahead. If it's a feature, open an issue first. For the rest, use your best judgement. Development information is lower down.

## Using the capstone mod

The Hardcore Alchemy capstone mod is released under the LGPL 3, so you are allowed to use it in your own modpack. However, the mod was built for the HcA modpack, and as such, was designed with a particular gameplay experience in mind.

What the Hardcore Alchemy mod is designed for:

* **Random respawn on death** - Hardcore Alchemy has many features that only work well when death is not just a minor setback. I recommend using either [my Iberia fork](https://github.com/asanetargoss/iberia/releases) or [Better with Mods](https://minecraft.curseforge.com/projects/better-with-mods). Hardcore mode should in principle also work.
* **Balanced shapeshifting** - [Changeling](https://github.com/asanetargoss/metamorph/releases)'s morphing code is a deeply integral part of Hardcore Alchemy. As such, is a required dependency. Morphing can be disabled if needed.
* **QoL/tweaks not included** - With the exception of non-vanilla maps (which trivialize random respawn deaths), all QoL mods/recipe changes should be included liberally, and are the responsibility of the pack developer.
    * Please note HcA only supports [Wawla](https://minecraft.curseforge.com/projects/wawla-what-are-we-looking-at) for in-game overlays.
* **Custom Nutrition integration** - If you use the Nutrition mod, please use [my custom Nutrition Build](https://github.com/asanetargoss/Nutrition). The official version of Nutrition will not work.
* **Opinionated** - I don't add config options to the capstone mod unless a feature has the potential to conflict severely with vanilla hardcore mode. Currently the only config option is to disable the instinct system. This allows me to focus more closely on providing a unified experience. 

Overall, the main draw of the HcA capstone mod currently is the hardcore-compatible balanced morphing features. Aside from that, the mod implements lots of small fixes and features across the board, mainly focused on adding a bit of challenge. See the [capstone mod releases page](https://github.com/asanetargoss/HardcoreAlchemy/releases) to browse the recent changelogs, and the [modpack wiki](https://github.com/asanetargoss/HardcoreAlchemy/wiki) to get a better feel for the mod's direction. Feel free to reach out if you have any questions.

## Developing/building

### Dependencies

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

### Coding guidelines

* Code style:
    * Tabs are four spaces
    * Brackets go like this:
```
if () {
    ...
}
else if {
    ...
}
else {
    ...
}
```

*
    * Spaces go between binary operators: `(x + 1)`
    * Prefer static utility functions which are either stateless, or only do what they are told
    * Prefer namespaced objects over lambdas and anonymous classes
    * Prefer Collections over arrays
    * Prefer Java's built-in libraries over third-party libraries
* Git
    * Branch off of master
    * Aim for one change per commit when possible
    * Use descriptive commit messages which describe what you changed
    * Don't change things that don't need to be changed
* Modding hacks
    * Avoid reflection, access transformers, and coremodding where possible
    * If you must use reflection, follow convention and use ObfuscatedName
    * If you must use access transformers, explain why you used them
    * If you must add a coremod patch...
        * Keep patch size VERY small. Use hooks. Never replace a function signature.
        * Follow convention and use ObfuscatedName, MethodPatcher
        * Only use ClassWriter flags if you need them. Be aware COMPUTE_FRAMES can trigger classloading.

