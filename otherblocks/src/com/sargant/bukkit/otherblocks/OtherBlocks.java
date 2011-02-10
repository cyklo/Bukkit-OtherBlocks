package com.sargant.bukkit.otherblocks;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class OtherBlocks extends JavaPlugin
{
	public String testval;

	private final OtherBlocksBlockListener blockListener = new OtherBlocksBlockListener(this);
	private final Logger log = Logger.getLogger("Minecraft");

	public OtherBlocks(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader)
	{
		super(pluginLoader, instance, desc, folder, plugin, cLoader);

		folder.mkdirs();
		File yml = new File(getDataFolder(), "config.yml");

		if (!yml.exists())
		{
			try
			{
				yml.createNewFile();
				log.info("Created an empty file config.yml at " + getDataFolder() +", please edit it!");

				getConfiguration().setProperty("please", "edit-me");
				getConfiguration().save();

			}
		      catch (IOException ex){}
		}

		testval = getConfiguration().getString("please", "edit-me");
	}

	public void onDisable()
	{
		log.info(getDescription().getName() + " " + getDescription().getVersion() + " unloaded.");
	}

	public void onEnable()
	{
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Monitor, this);

		log.info(getDescription().getName() + " " + getDescription().getVersion() + " loaded.");
	}
}

