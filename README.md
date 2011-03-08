OtherBlocks
===========

OtherBlocks is a plugin for the Minecraft Bukkit API that lets you completely
customize what blocks and dead mobs drop when they are destroyed. Apples from
leaves, no more broken glass, you name it!

The related discussion thread for this plugin is located at
<http://forums.bukkit.org/threads/4072/>

Please [see the wiki](https://github.com/cyklo/Bukkit-OtherBlocks/wiki) for details on how to set up OtherBlocks

Building from source
-----------

Please note that OtherBlocks contains submodules, so to checkout:

    git clone git://github.com/cyklo/Bukkit-OtherBlocks.git
    cd Bukkit-OtherBlocks
    git submodule update --init

This projects includes an Ant make. To build, you need a copy of `bukkit.jar`
in the top-level folder. Then run `ant clean && ant build && ant jar` like so:

    cd Bukkit-OtherBlocks
    wget -O bukkit.jar http://ci.bukkit.org/job/dev-Bukkit/lastSuccessfulBuild/artifact/target/bukkit-0.0.1-SNAPSHOT.jar
    ant clean
    ant build
    ant jar