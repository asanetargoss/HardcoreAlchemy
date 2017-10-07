Hardcore Alchemy
TO DO: actual markdown

Running with Minecraft
    This mod is for 1.10.2
    I am still setting things up so no documentation
    No license yet (ie do not distribute/all rights reserved)
    Metamorph is a required dependency
    Seriously though, I'm still setting everything up. I'm just putting things up on github so the actual release goes smoother.
Developing/building
    First thing you should probably do is run the gradle script that sets up your workspace, eg "./gradlew eclipse"
        Also run each time you add mods to libs/ (explained later)
    Build Forge for 1.10.2
        If you have a decompiled version of Tough as Nails for 1.9.4, you will need to compile my TaN-compat branch of Forge, otherwise use the latest Forge source
        After building Forge, put forge-userdev.jar in .gradle/minecraft
    Then run "./gradlew setupDecompWorkspace" to get Forge and access transformers working
        Run this again whenever you want to change access transformers/use a different Forge
    This mod contains coremod patches
        Don't forget the VM argument -Dfml.coreMods.load=targoss.hardcorealchemy.coremod.HardcoreAlchemyCoreMod
        Additional coremod class names can be added with commas if needed (eg Tough as Nails)
    This mod contains access transformers
        See src/main/resources/META-INF/hardcorealchemy_at.cfg
        If you plan on adding additional mods as source package folders (explained later), and those mods have access transformers, you will need to copy them to the hardcore alchemy access transformer
        As you can see, the Tough as Nails transformers are there, but adding Tough as Nails as a source package folder is not required
    Dependencies
        Workspace package sources linked to folders in subsources/
            metamorph, Nutrition sources must be available locally for this mod to compile properly with the current build.gradle (this will hopefully change)
            cd subsources, git clone the nutrition repo, git clone the metamorph repo
                Check my (asanetargoss) repo and clone that if it's more up-to-date, otherwise clone upstream
                Upstream Nutrition repo: https://github.com/WesCook/Nutrition.git
                Upstream metamorph repo: https://github.com/mchorse/metamorph.git
            IDE source package folders should be added for Nutrition and metamorph. It should look like this:
                MDKExample
                    src/main/java
                    src/main/resources
                    subsources/metamorph/src/main/java
                    subsources/metamorph/src/main/resources
                    subsources/Nutrition/src/main/java
                    subsources/Nutrition/src/main/resources
            When editing any source file from a source package folder, the changes should immediately take effect on the next launch within the workspace, the same as with Hardcore Alchemy itself
            To prepare for compiling, in each subsource folder, run "./gradlew setupCIWorkspace" then "./gradlew assemble". This will put the jars in "build/libs" that the gradle script wants. Verify the versions of these mods matches with the top-level build.gradle, and update build.gradle if needed to increment version numbers
        Local jars added to classpath via libs/
            Can be either dev jars, or regular obfuscated jars provided CodeChickenCore is among them
            Add any soft dependencies you want to test
                HardcoreAlchemy.java lists most of the soft dependencies
                Pam's Harvestcraft is also a soft dependency
            Add whatever else you want (eg JEI)
            Be sure to call "./gradlew eclipse" when you're done
        The usual, gradle-based method (maven and such)
            ie. Wawla
            No additional action needed