Hardcore Alchemy
TO DO: actual markdown

Running with Minecraft
    This mod is for 1.10.2
    I am still setting things up, so no gameplay documentation
    No license yet (ie do not distribute/all rights reserved)
    Required dependencies for playing the mod:
        Latest official Forge
        Metamorph
Developing/building
    First thing you should probably do is run the gradle script that sets up your workspace, eg "./gradlew eclipse"
        Also run each time you add mods to libs/ (explained later)
    Build custom Forge for 1.10.2
        https://github.com/asanetargoss/MinecraftForge/tree/TaN-compat
        After building Forge, put forge-userdev.jar in .gradle/minecraft
        This stage makes it possible to use the decompiled 1.9.4 branch of Tough As Nails in a dev environment
    Then run "./gradlew setupDecompWorkspace" to get Forge and access transformers working
        You should see the message "Assuming custom Forge" if the Forge building process went well
        Run this again whenever you want to change access transformers/use a different Forge build
    This mod contains coremod patches
        Don't forget the VM argument -Dfml.coreMods.load=targoss.hardcorealchemy.coremod.HardcoreAlchemyCoreMod
        Additional coremod class names can be added with commas if needed (eg Tough as Nails)
    This mod contains access transformers
        See src/main/resources/META-INF/hardcorealchemy_at.cfg
        If you plan on adding additional mods as source package folders (explained later), and those mods have access transformers, you will need to copy them to the hardcore alchemy access transformer
        As you can see, the Tough as Nails transformers are there, but adding Tough as Nails as a source package folder is not required
    Dependencies
        The following mod(s) are required dependencies, but require no action due to being fully automated by the build script:
            Wawla: https://github.com/Darkhax-Minecraft/WAWLA
        Other mod(s) must be added as dependencies with one of two methods described later.
        Please note that the gradle build script is configured to compile Nutrition and metamorph via method #2. See method #2 for how to revert these dependencies to method #1:
        List of mods which can be dependencies by either method #1 or method #2:
            Nutrition: https://github.com/WesCook/Nutrition
                Please note: my (asanetargoss) repo must currently be used instead of the official repo, and also the implementation is subject to change. tl;dr you will have to compile Nutrition yourself until the changes get merged.
            metamorph: https://github.com/mchorse/metamorph
            Iron Backpacks: https://github.com/gr8pefish/IronBackpacks
            ProjectE: https://github.com/sinkillerj/ProjectE
            Ars Magica: https://github.com/Growlith1223/ArsMagica2
            Astral Sorcery: https://github.com/HellFirePvP/AstralSorcery
            Dissolution: https://github.com/Pyrofab/Dissolution
            Blood Magic: https://github.com/WayofTime/BloodMagic/
        Method #1: Local jars added to classpath via libs/
            The typical way to manage dependencies during both testing and during compilation is to add the mod to libs/
            Can be either dev jars, or regular obfuscated jars provided CodeChickenCore is among them
                You will almost definitely need CodeChickenCore, as many mods either don't have dev builds or are closed source, so it's much more convenient to just have the obfuscated mod jar in libs/
                    https://minecraft.curseforge.com/projects/codechicken-core-1-8
                    https://minecraft.curseforge.com/projects/codechicken-lib-1-8
                If you do that, it is also strongly recommended you install a decompiler plugin for your IDE so you can view "source code" for the mods:
                    http://jd.benow.ca/
            Can add any other mods you want to test or quality-of-life mods like JEI
            Pam's Harvestcraft is an especially recommended "soft dependency" for Nutrition and dietary restrictions
            Be sure to call "./gradlew eclipse" or equivalent when you're done to add the mods to your classpath
        Method #2: IDE source package folders
            Can be used as an alternative to placing mods in libs/ which allows you to edit code for mods and test the changes without compiling, the same as with Hardcore Alchemy itself
            Converting between method #1 and method #2: See comment in build.gradle marked "IDE source package folder dependencies". Comment out dependencies you want to be put in libs/ instead, or add new lines for dependencies you want. Make sure the version matches the jar file name.
            In build.gradle, the "flatDir" folder names must be present in order for corresponding dependencies to be discovered. Adding extra unused folders is not harmful.
            On the IDE's side, each line shown represents a source folder in the workspace linked to an actual folder relative to the top directory. Each mod should have two, one for the source code and one for the resources:
                MDKExample
                    src/main/java
                    src/main/resources
                    subsources/metamorph/src/main/java
                    subsources/metamorph/src/main/resources
                    subsources/Nutrition/src/main/java
                    subsources/Nutrition/src/main/resources
            It is also possible to add other mods in the workspace in this way which are not dependencies