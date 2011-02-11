package com.sargant.bukkit.otherblocks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
	class BlockTransform
	{
		public Material original;
		public Material dropped;
		public Material tool;
		public Integer quantity;
	}
	
	private List<BlockTransform> transformList = new ArrayList<BlockTransform>();

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
			try {
				yml.createNewFile();
				log.info("Created an empty file " + getDataFolder() +"/config.yml, please edit it!");
				getConfiguration().setProperty("otherblocks", null);
				getConfiguration().save();
			} catch (IOException ex){
				log.warning(getDescription().getName() + ": could not generate config.yml. Are the file permissions OK?");
			}
		}
		
		// Load in the values from the configuration file
		List <String> keys;
		try { 
			keys = getConfiguration().getKeys(null); 
		} catch(NullPointerException ex) {
			log.warning(getDescription().getName() + ": no parent key not found");
			return;
		}
		
		if(!keys.contains("otherblocks"))
		{
			log.warning(getDescription().getName() + ": no 'otherblocks' key found");
			return;
		}
		
		keys.clear();
		keys = getConfiguration().getKeys("otherblocks");

		if(null == keys)
		{
			log.info(getDescription().getName() + ": no values found in config file!");
			return;
		}

		for(String s : keys)
		{
			BlockTransform bt = new BlockTransform();

			try {
				bt.original = Material.valueOf(s);
				bt.dropped  = Material.valueOf(getConfiguration().getString("otherblocks."+s+".drop"));
				bt.tool     = Material.valueOf(getConfiguration().getString("otherblocks."+s+".tool"));
				bt.quantity = getConfiguration().getInt("otherblocks."+s+".quantity", 1);
			} catch(IllegalArgumentException ex) {
				log.warning("Illegal block or tool value: " + s);
				continue;
			}

			transformList.add(bt);

			log.info(getDescription().getName() + ": " + 
					bt.tool.toString() + " + " + 
					bt.original.toString() + " now drops " + 
					bt.quantity.toString() + "x " + bt.dropped.toString());

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

