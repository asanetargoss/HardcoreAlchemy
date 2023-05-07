# Hardcore Alchemy Mod Suite

The Hardcore Alchemy Mod Suite is a collection of mods for Minecraft 1.10.2 with a focus on cross-mod compatibility in a hardcore/magic/survival setting.

## Hardcore Alchemy: Magic With Consequences

For more information about the Hardcore Alchemy modpack, [visit its post on the Minecraft Forum](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/mod-packs/2900247-hardcore-alchemy-0-7-3-inhumanity-hotfix).

## Using the mod suite

### Licenses

The Hardcore Alchemy mod suite is released under different licenses depending on the mod:

- core, tweaks, creatures, magic, survival - GNU LGPL v3 or later
- capstone - GNU GPL v3

In short: You are allowed to use any of these listed mods in your own modpack.

### Gameplay dependencies

The Hardcore Alchemy mod suite is tested to work with specific mods. Below are the recommended mods and config settings that work best.

#### Required/Recommended mods

* [Changeling](https://github.com/asanetargoss/Changeling/releases) - This powers Hardcore Alchemy's balanced morphing features. If you are using the `creatures` submod, you must have Changeling installed.
  * The following config options should be set in Changeling: `acquire_immediately=false`, `disable_morph_disguise=true`, and `keep_morphs=false`
* [Iberia HcA edition](https://github.com/asanetargoss/iberia/releases) - The Hardcore Alchemy mod suite is designed around hardcore respawn, which this mod will implement for you.

#### Optional mods

* [Nutrition HcA Edition](https://github.com/asanetargoss/Nutrition/releases) and/or [Spice of Life](https://www.curseforge.com/minecraft/mc-mods/the-spice-of-life) - For nutrition.
  * The official version of the Nutrition mod will not work, and HcA assumes the default 5 nutrients
* [Pam's Harvestcraft](https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft) - Right-click harvesting and food support
* [Tough as Nails](https://www.curseforge.com/minecraft/mc-mods/tough-as-nails) - Thirst
    - Reduced starting health should be disabled, to prevent feature overlap with the heart upgrade mechanic in the `tweaks` submod
    - Temperature should be disabled, as it does not play well with the instinct system in the `creatures` submod
* [Minecraft Comes Alive](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1280154-mc-1-12-x-minecraft-comes-alive-v6-0-0-millions) and/or [Village Box](https://www.curseforge.com/minecraft/mc-mods/village-box) - Villager alternatives
* Various magic mods according to one's personal tastes

### Game design notes

There is a lot more in terms of mod selection, configuration, and tweaking, in order for a hardcore modpack to work well. Please exercise discretion when selecting punishing mechanics for your modpack and be mindful of the player's time.

Also, since these mods were built for the Hardcore Alchemy modpack, config options are not planned, except as needed for compatibility with vanilla hardcore mode and serious accessibility concerns.

## Developing/building

### Dependencies

This branch targets the 0.7.2+ version of the modpack. Download the file `compilelibs-0.7.2.zip` from the link below and be ready to add its contents to `compilelibs/` in the Hardcore Alchemy repository folder:

http://www.mediafire.com/folder/grwn2vsjr2lce/Hardcore_Alchemy_Libs

The SHA-256 checksum of `compilelibs-0.7.2.zip` is: `220b5f8d0c9c1c507e78078c54dfce6ae14fddb18cd9a3bbe73c9dd2ce727cb9`

Please note: The following mods included in the HcA_compilelibs zip file above are custom forks:
* Nutrition: https://github.com/asanetargoss/Nutrition
* Changeling: https://github.com/asanetargoss/Changeling
* Dissolution ([permission](https://i.imgur.com/b7sN6lL.png))
* Iberia: https://github.com/asanetargoss/iberia
* Guide-API (Custom build for development use only. Do not distribute.)
* AppleCore: https://github.com/asanetargoss/AppleCore

### Overview of Projects

* `compilelibs/` - The place to put third-party mods that the various projects depend on. For example, files in `compilelibs/core/` are depended on by the core project and any mods which depend on the core project.
* `translations/` - Unlike most mod projects, translation files are stored in this folder, separately from other resources. Most of the translations are in `translations/core/`, but this may change in the future.
* `core/` - A mod project which contains shared code required by the other projects.
* `tweaks/` - A mod project containing magic and survival tweaks, aiming for a minimal number of dependencies.
* `creatures/` - A mod project for balanced morphing mechanics and tweaks to mob mods. It requires Changeling.
* `magic/` - A mod project containing features which make magic mods work better in a hardcore respawn gameplay context. It also contains various tweaks and fixes for magic mods.
* `survival/` - A mod project containing various features related to nutrition, thirst, and crop growth. It interacts with various survival and crop mods.
* `capstone/` - A mod project which handles magic/survival cross-mod compatibility for GPLv3-licensed mods. It also adds guidebooks and integration tests for the Hardcore Alchemy modpack.

### Notes on Windows and IntelliJ IDEA

The commands in the instructions below assume your command line is a unix-style shell (ex: bash, zsh) and that Eclipse is used as the IDE. If you're not in that situation, you may have to make adjustments.

* **Windows Users** - When you type `echo $SHELL` in your command line of choice, do you see a path with forward slashes, like `/bin/bash`?
    * If you answered **no**, to this question, then wherever you see `./gradlew`, you should instead type `gradle.bat`
    * If you answered **yes**, then proceed as normal
* **IntelliJ Users** - Unfortunately, I haven't tested this gradle setup with IntelliJ, so I don't know if it works or not. If you figure out how to get it working, please report back! That being said, it most likely involves the command `./gradlew idea`

### Workspace setup

* Clone this repository into a folder. If you are using Eclipse, it is recommended that you create this folder inside of another folder, so the higher-up folder can be used as your multi-project workspace.
* Navigate into the folder of your new personal copy of this repository. Create the folder `compilelibs/`. In that folder, you should copy the mod dependencies you have downloaded from the Dependencies step
* Run `./gradlew setupDecompWorkspace eclipse`
    * `setupDecompWorkspace` sets up dependencies for Minecraft, Forge, etc; and updates access transformers for all projects
    * `eclipse` sets up files for all projects that can be imported into the Eclipse IDE
    * During the very first setup, you may encounter an error where the forge jar is missing from your Eclipse classpath. To fix this, re-run `./gradlew eclipse` (this is a known bug with the gradle scripts)
<!-- TODO: Figure out what is preventing the eclipse .classpath for each submod from resolving the forge source jar correctly, when `./gradlew setupDecompWorkspace eclipse` is run for the first time -->

### Testing

* Create an Eclipse workspace in a folder of your choice (a higher-up folder containing this one is recommended)
* Import the `core` project, and any other projects you want to work with
* When testing any open project, the `core` project should stay open to provide the necessary dependencies

### Compiling

* To compile all mods, run `./gradlew build`. Outputs will be in `[PROJECT_NAME]/build/libs`
    * `[PROJECT_NAME]` is the name of each project that was built
    * The outputted release jar name will be `hardcorealchemy-[PROJECT_NAME]-[PROJECT_VERSION].jar` where `[PROJECT_VERSION]` is defined in `[PROJECT_NAME]/build.gradle`.
* To bundle all the mods into one convenient .zip file (excluding the modpack mod), run `./gradlew buildModSuite`. The output will be in `modpack/build/distributions`
* If you add new files to a project, you will likely need to add licenses to the files, or you will get errors. To fix this, run `./gradlew [PROJECT_NAME]:updateLicenses` for each project you have added files to.

### Development tips

* Internet down? Already ran gradle commands before? No problem! Just add the `-offline` flag to your gradle command and it should work normally again.
* `./gradlew setupDecompWorkspace eclipse` may need to be re-run under the following circumstances:
    * Adding/changing mods in `compilelibs/`
    * Updating dependencies in other places like a `build.gradle` file
    * Updating the access transformers (found at "core/src/main/resources/META-INF/hardcorealchemy_at.cfg") 
    * On rare occasions, when encountering unexplained crashes involving missing classes or methods. This is a bug with CodeChickenCore when used in a development environment.
* If you want to work with a specific project only, there are several ways to do this
    * If you only want to set up a certain `[PROJECT_NAME]`, you can run `./gradlew [PROJECT_NAME]:setupDecompWorkspace [PROJECT_NAME]:eclipse`
    * If you only want to build a certain `[PROJECT_NAME]`, you can run `./gradlew [PROJECT_NAME]:build`
    * You can comment out a project in the top-level `settings.gradle` and `build.gradle` to completely ignore it
* Assertions are enabled in all `targoss.hardcorealchemy...` packages. To debug assertions, add `AssertionError` as an exception breakpoint

### Other resources

* Decompiler plugin: http://jd.benow.ca/ (Why: Required to view source code for all the mods in `libs/`, since most do not have dev builds)
* MCP Mapping Viewer: https://github.com/bspkrs/MCPMappingViewer/ (Why: To figure out the meaning of `func_12345` and their ilk, and translate to them when needed in a release environment)
* Bytecode outline for Eclipse: http://andrei.gmxhome.de/bytecode/index.html (Why: Only if you need to coremod. It will help you understand the Java bytecode better. Do not trust the stack feature.)

