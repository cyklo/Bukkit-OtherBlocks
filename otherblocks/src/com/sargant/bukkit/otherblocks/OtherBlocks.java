package com.sargant.bukkit.otherblocks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class OtherBlocks extends JavaPlugin
{
	protected List<OtherBlocksContainer> transformList;
	protected Random rng;
	private final OtherBlocksBlockListener blockListener;
	private final Logger log;
	protected Integer verbosity;
	
	//These are fixes for the broken getMaxDurability and getMaxStackSize in Bukkit
	public short getFixedMaxDurability(Material m) {
		// If the maxstacksize is -1, then the values are the wrong way round
		return (short) ((m.getMaxStackSize() < 1) ? m.getMaxStackSize() : m.getMaxDurability());
	}
	
	public int getFixedMaxStackSize(Material m) {
		return (int) ((m.getMaxStackSize() < 1) ? m.getMaxDurability() : m.getMaxStackSize());
	}
	
	public OtherBlocks() {
		
		transformList = new ArrayList<OtherBlocksContainer>();
		rng = new Random();
		blockListener = new OtherBlocksBlockListener(this);
		log = Logger.getLogger("Minecraft");
		verbosity = 2;
	}

	public void onDisable()
	{
		log.info(getDescription().getName() + " " + getDescription().getVersion() + " unloaded.");
	}

	public void onEnable()
	{
		// Initialize and read in the YAML file
		
		getDataFolder().mkdirs();
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
		
		if(keys.contains("verbosity")) {
			String verb_string = getConfiguration().getString("verbosity", "normal");
			
			if(verb_string.equalsIgnoreCase("low")) { verbosity = 1; }
			else if(verb_string.equalsIgnoreCase("high")) { verbosity = 3; }
			else { verbosity = 2; }
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

		for(String s : keys) {
			List<Object> original_children = getConfiguration().getList("otherblocks."+s);	
			
			if(original_children == null) {
				log.warning("Block \""+s+"\" has no children. Have you included the dash?");
				continue;
			}
			
			for(Object o : original_children) {
				if(o instanceof HashMap<?,?>) {
					
					OtherBlocksContainer bt = new OtherBlocksContainer();
					
					try {
						HashMap<?, ?> m = (HashMap<?, ?>) o;
						
						bt.original = Material.valueOf(s);

						String toolString = String.valueOf(m.get("tool"));
						if(toolString.equalsIgnoreCase("DYE")) { toolString = "INK_SACK"; }
						bt.tool = (toolString.equalsIgnoreCase("ALL") ? null : Material.valueOf(toolString));

						String dropString = String.valueOf(m.get("drop"));
						
						if(dropString.length() > 9 && dropString.substring(0, 9).equalsIgnoreCase("CREATURE_")) {
							bt.dropped = CreatureType.valueOf(dropString.substring(9)).toString();
							bt.droptype = "CREATURE";
						} else {
							bt.dropped = Material.valueOf(dropString).toString();
							bt.droptype = "MATERIAL";
						}

						Integer dropQuantity = Integer.class.cast(m.get("quantity"));
						bt.quantity = (dropQuantity == null || dropQuantity <= 0) ? 1 : dropQuantity;

						Integer toolDamage = Integer.class.cast(m.get("damage"));
						bt.damage = (toolDamage == null || toolDamage < 0) ? 1 : toolDamage;

						Double dropChance;
						try {
							dropChance = Double.valueOf(String.valueOf(m.get("chance")));
							bt.chance = (dropChance < 0 || dropChance > 100) ? 100 : dropChance;
						} catch(NumberFormatException ex) {
							bt.chance = 100.0;
						}
						
						String dropColor = String.valueOf(m.get("color"));
						bt.color = ((dropColor == "null") ? 0 : DyeColor.valueOf(dropColor).getData());
						
					} catch(Throwable ex) {
						if(verbosity > 1) {
						  log.warning("Error while processing block " + s + ": " + ex.getMessage());
						}
						continue;
					}
					
					transformList.add(bt);
					
					if(verbosity > 1) {
					  log.info(getDescription().getName() + ": " + 
							(bt.tool == null ? "ALL TOOLS" : bt.tool.toString()) + " + " + 
							bt.original.toString() + " now drops " + 
							bt.quantity.toString() + "x " + 
							bt.dropped.toString() + " with " + 
							bt.chance.toString() + "% chance");
					}
				}
			}
		}
		
		// Done setting up plugin
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Lowest, this);

		log.info(getDescription().getName() + " " + getDescription().getVersion() + " loaded.");
	}
}
