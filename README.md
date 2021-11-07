# Hardcore Alchemy
Hardcore Alchemy is a hardcore/magic/survival modpack built for Minecraft 1.10.2.

You are currently in the source code repository for the Hardcore Alchemy mod suite, which powers many of Hardcore Alchemy's most unique features. The best way to learn more information about the modpack is to [visit the wiki](https://github.com/asanetargoss/HardcoreAlchemy/wiki).

## Contributing

Aside from feedback on the modpack itself, the following contributions are welcome:

- **Translations** - Are always welcome. See the [translations folder](https://github.com/asanetargoss/HardcoreAlchemy/tree/master/translations/).
- **Code** - If it's a bugfix, and it's faster to write the code than it is to explain it, go right ahead. Otherwise, let's talk about it first

## Using the mod suite

The Hardcore Alchemy mod suite is released under the LGPL 3, so you are allowed to use any of the mods in your own modpack. However, since the mods were built for the HcA modpack, they were designed with a particular gameplay experience in mind. This means that certain mods work better with it than others. Also, config options are not planned, except as needed for compatibility with vanilla hardcore mode and serious accessibility concerns.

The Hardcore Alchemy mod suite is designed around the following mechanics:

* **Balanced shapeshifting** - [Changeling](https://github.com/asanetargoss/Changeling/releases) powers Hardcore Alchemy's balanced morphing features. If you are using the `creatures` submod, you must have Changeling installed.
  * The following config options should be set in Changeling: `acquire_immediately=false`, `disable_morph_disguise=true`, and `keep_morphs=false`
* **Random respawn on death** - The Hardcore Alchemy mod suite is designed around hardcore respawn. For this, I recommend [Iberia HcA edition](https://github.com/asanetargoss/iberia/releases).

There is also cross-mod compatibility with various other mods. These mods are not required dependencies, but should be used instead of the alternatives:

* [Hwyla](https://www.curseforge.com/minecraft/mc-mods/hwyla) - In-game overlays.
* [Nutrition HcA Edition](https://github.com/asanetargoss/Nutrition/releases) and/or [Spice of Life](https://www.curseforge.com/minecraft/mc-mods/the-spice-of-life) - For nutrition.
  * The official version of the Nutrition mod will not work, and HcA assumes the default 5 nutrients
* [Pam's Harvestcraft](https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft) - Right-click harvesting and food support
* [Tough as Nails](https://www.curseforge.com/minecraft/mc-mods/tough-as-nails) - Thirst
* [Minecraft Comes Alive](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1280154-mc-1-12-x-minecraft-comes-alive-v6-0-0-millions) and/or [Village Box](https://www.curseforge.com/minecraft/mc-mods/village-box) - Villager alternatives

Finally, there is integration with many magic mods.

There is a lot more in terms of mod selection, configuration, and tweaking, in order for a hardcore modpack to work well. Please exercise discretion when selecting punishing mechanics for your modpack and be mindful of the player's time. :)

## Developing/building

### Dependencies

This branch targets the 0.7.0+ version of the modpack. Download the 0.7.0 zip file from the link below and be ready to add its contents to `compilelibs/` in the Hardcore Alchemy repository folder:

http://www.mediafire.com/folder/grwn2vsjr2lce/Hardcore_Alchemy_Libs

The SHA-256 checksum of the 0.7.0 compilelibs zip is: 00f0904f21af4f11874bf1ae20e3af11d1a75c82dec358746821e1757c4460d8

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
* `magic/` - A mod project containing features which make magic mods work better in a hardcore respawn gameplay context. It also contains various tweaks and fixes for magic and utility mods.
* `survival/` - A mod project containing various features related to nutrition, thirst, and crop growth. It interacts with various survival and crop mods.
* `modpack/` - A mod project intended for the Hardcore Alchemy modpack. It adds guidebooks and integration tests. If you are building your own modpack, you most likely do not want to use this mod.

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
* (Optional but recommended) For the desired `[PROJECT_NAME]` you want to test with (ex: `core` or `tweaks`):
    * `[PROJECT_NAME]/run/config/` - Here you should copy the configs you have downloaded from the Dependencies step (this is optional, but recommended)

### Testing

* Create an Eclipse workspace in a folder of your choice (a higher-up folder containing this one is recommended)
* Import the `core` project, and any other projects you want to work with
* When testing any open project, the `core` project should stay open to provide the necessary dependencies

### Compiling

* To compile all projects, run `./gradlew build`. Outputs will be in `[PROJECT_NAME]/build/libs`
    * `[PROJECT_NAME]` is the name of each project that was built
    * The outputted release jar name will be `hardcorealchemy-[PROJECT_NAME]-[PROJECT_VERSION].jar` where `[PROJECT_VERSION]` is defined in `[PROJECT_NAME]/build.gradle`.

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
    * You can comment out a project in `settings.gradle` to completely ignore it

### Other resources

* Decompiler plugin: http://jd.benow.ca/ (Why: Required to view source code for all the mods in `libs/`, since most do not have dev builds)
* MCP Mapping Viewer: https://github.com/bspkrs/MCPMappingViewer/ (Why: To figure out the meaning of `func_12345` and their ilk, and translate to them when needed in a release environment)
* Bytecode outline for Eclipse: http://andrei.gmxhome.de/bytecode/index.html (Why: Only if you need to coremod. It will help you understand the Java bytecode better. Do not trust the stack feature.)

