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
    Then run "./gradlew setupDecompWorkspace" to set up access transformers
        Run this again whenever you want to change access transformers
    This mod contains coremod patches
        Don't forget the VM argument -Dfml.coreMods.load=targoss.hardcorealchemy.coremod.HardcoreAlchemyCoreMod
        Additional coremod class names can be added with commas if needed (eg Metamorph as a source package folder)
    This mod contains access transformers
        See src/main/resources/META-INF/hardcorealchemy_at.cfg
        If you plan on adding additional mods as source package folders (explained later), and those mods have access transformers, you will need to copy them to the hardcore alchemy access transformer
        As you can see, the Tough as Nails transformers are there, but adding Tough as Nails as a source package folder is not required
    Dependencies
        The following mod(s) are required dependencies, but require no action due to being fully automated by the build script:
            Wawla: https://github.com/Darkhax-Minecraft/WAWLA
        Other mod(s) must be added as dependencies with one of two methods described later.
        List of mods which can be dependencies by either method #1 (easy) or method #2 (harder):
            These mods use method #2 by default:
                Nutrition: https://github.com/asanetargoss/Nutrition
                metamorph: https://github.com/asanetargoss/metamorph
                See method #2 for how to revert
                Hopefully I can eventually get these mods off of method #2 so setup is easier
            These mods use method #1:
                Iron Backpacks: https://minecraft.curseforge.com/projects/iron-backpacks
                ProjectE: https://minecraft.curseforge.com/projects/projecte
                Ars Magica: https://minecraft.curseforge.com/projects/ars-magica-2
                Astral Sorcery: https://minecraft.curseforge.com/projects/astral-sorcery
                Dissolution: https://minecraft.curseforge.com/projects/dissolution
                Blood Magic: https://minecraft.curseforge.com/projects/blood-magic
                Tough As Nails: https://minecraft.curseforge.com/projects/tough-as-nails
        Method #1: Local jars added to classpath via libs/
            The usual method where you add the mods to libs/ and then call ./gradlew eclipse or equivalent
            Works for both testing and compilation
            We're supposed to use dev jars, but obfuscated jars work when CodeChickenCore is among them
            You will definitely need CodeChickenCore because most mods don't have dev builds, so add both of these to libs/:
                https://minecraft.curseforge.com/projects/codechicken-core-1-8
                https://minecraft.curseforge.com/projects/codechicken-lib-1-8
            If you do that, it is also strongly recommended you install a decompiler plugin for your IDE so you can view "source code" for the mods:
                http://jd.benow.ca/
            Can add any other mods you want to test or quality-of-life mods like JEI
            Pam's Harvestcraft is an especially recommended "soft dependency" for Nutrition and dietary restrictions
        Method #2: IDE source package folders
            I use this method for Nutrition and Metamorph because I need to edit their code a lot without compiling
            Basically, a CI workspace for each of these mods is located in 'subsources/metamorph' and 'subsources/Nutrition'
            The Hardcore Alchemy gradle script expects the following jars in the following locations at compile time (unsure if '-dev.jar' or just '.jar'):
                'subsources/metamorph/build/libs/metamorph-1.1.4-1.10.2.jar'
                'subsources/Nutrition/build/libs/Nutrition-1.10.2-1.6.0.jar'
            On the IDE's side, I have manually linked each mod's source folders like so for the dev environment:
                MDKExample
                    src/main/java
                    src/main/resources
                    subsources/metamorph/src/main/java
                    subsources/metamorph/src/main/resources
                    subsources/Nutrition/src/main/java
                    subsources/Nutrition/src/main/resources
            It is also possible to add other mods in the workspace in this way which are not dependencies