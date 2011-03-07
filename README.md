OtherBlocks
===========

OtherBlocks is a plugin for the Minecraft Bukkit API that lets you completely
customize what blocks and dead mobs drop when they are destroyed. Apples from
leaves, no more broken glass, you name it!

The related discussion thread for this plugin is located at
<http://forums.bukkit.org/threads/4072/>

Downloading
-----------

Please note that OtherBlocks contains submodules, so to checkout:

    git clone git://github.com/cyklo/Bukkit-OtherBlocks.git
    cd Bukkit-OtherBlocks
    git submodule update --init

Building
--------

An Ant makefile is included. Building this project requires a copy of
`bukkit.jar` in the top level directory.

    cd Bukkit-OtherBlocks
    wget -O bukkit.jar http://ci.bukkit.org/job/dev-Bukkit/lastSuccessfulBuild/artifact/target/bukkit-0.0.1-SNAPSHOT.jar
    ant
    ant jar