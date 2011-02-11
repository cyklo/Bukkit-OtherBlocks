package com.sargant.bukkit.otherblocks;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Material;
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

		// Initialize and read in the YAML file
		
		folder.mkdirs();
		File yml = new File(getDataFolder(), "config.yml");

		if (!yml.exists())
		{
			try
			{
				yml.createNewFile();
				log.info("Created an empty file config.yml at " + getDataFolder() +", please edit it!");

				getConfiguration().setProperty("otherblocks", "");
				getConfiguration().save();

			}
			catch (IOException ex){}
		}
		
		// Load in the values from the configuration file
		List<String> originalBlocks = getConfiguration().getKeys("otherblocks");
		for(String s : originalBlocks)
		{
			Material originalMaterial;
			Material droppedMaterial;
			Material toolUsed;
			Integer dropQuantity;
			
			try
			{
				originalMaterial = Material.valueOf(s);
			}
			catch(IllegalArgumentException ex)
			{
				log.warning("Illegal original block type: " + s);
				continue;
			}
			
			try
			{
			  droppedMaterial = Material.valueOf(getConfiguration().getString("otherblocks."+s+".drop"));
			}
			catch(IllegalArgumentException ex)
			{
				log.warning("Illegal new drop block type: " + s);
				continue;
			}
			
			try
			{
				toolUsed = Material.valueOf(getConfiguration().getString("otherblocks."+s+".tool"));
			}
			catch(IllegalArgumentException ex)
			{
				log.warning("Illegal tool type: " + s);
				continue;
			}
			
			dropQuantity = getConfiguration().getInt("otherblocks."+s+".quantity", 1);
			
			log.info(getDescription().getName() + ": " + 
					toolUsed.toString() + " + " + 
					originalMaterial.toString() + " now drops " + 
					dropQuantity.toString() + "x " + droppedMaterial.toString());
		}
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

