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
        Required dependencies for compiling
            A lot of new dependencies got added very recently, so I am in the process of updating the build script.
            The following mods must be present in subsources/[modname]/build/libs in order for Hardcore Alchemy to compile. This allows you to clone the mod into subsources/[modname] if needed
                Nutrition: https://github.com/WesCook/Nutrition
                    Please note: my (asanetargoss) repo must currently be used instead of the official repo, and also the implementation is subject to change. tl;dr you will have to compile Nutrition yourself until the changes get merged.
                metamorph: https://github.com/mchorse/metamorph
                Iron Backpacks: https://github.com/gr8pefish/IronBackpacks
            I suspect the following mods may also be needed by the time I am finished updating the build script, as they are referenced in the source code:
                Possibly ProjectE: https://github.com/sinkillerj/ProjectE
                Possibly Ars Magica: https://github.com/Growlith1223/ArsMagica2
                Possibly Astral Sorcery: https://github.com/HellFirePvP/AstralSorcery
                Possibly Dissolution: https://github.com/Pyrofab/Dissolution
            The following mod(s) are required dependencies, but require no action due to being fully automated by the build script:
                Wawla: https://github.com/Darkhax-Minecraft/WAWLA
            Tough As Nails is not a build dependency as I am writing this, but will likely become a dependency in the near future
            IN ADDITION to placing the compiled jar in the required location, you must either add all these mods as either "IDE source package folders" or add the compiled mod jar to the libs/ folder in the top directory
        Local jars added to classpath via libs/
            Can be either dev jars, or regular obfuscated jars provided CodeChickenCore is among them
            Add any other mods you want to test or quality-of-life mods like JEI
            Pam's Harvestcraft is an especially recommended "soft dependency" for Nutrition and dietary restrictions
            Be sure to call "./gradlew eclipse" or equivalent when you're done to add the mods to your classpath
        IDE source package folders
            Can be used as an alternative to placing mods in libs/ which allows you to edit code for mods and test the changes without compiling, the same as with Hardcore Alchemy itself
            They come in pairs and should look like this. Each line represents a source folder in the workspace linked to an actual folder relative to the top directory:
                MDKExample
                    src/main/java
                    src/main/resources
                    subsources/metamorph/src/main/java
                    subsources/metamorph/src/main/resources
                    subsources/Nutrition/src/main/java
                    subsources/Nutrition/src/main/resources
            To prepare for compiling Hardcore Alchemy with these sorts of dependencies, in each subsource folder, run "./gradlew setupCIWorkspace". Then you can run "./gradlew assemble". This will put the jars in "build/libs" that the gradle script wants. Verify the versions of these mods matches with the top-level build.gradle, and update build.gradle if needed to increment version numbers
            It is also possible to add other mods in the workspace in this way which are not dependencies