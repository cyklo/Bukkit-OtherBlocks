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
	//private final OtherBlocksEntityListener entityListener;
	protected final Logger log;
	protected Integer verbosity;
	protected Priority pri;
	
	//These are fixes for the broken getMaxDurability and getMaxStackSize in Bukkit
	public short getFixedMaxDurability(Material m) {
		// If the maxstacksize is -1, then the values are the wrong way round
		return (short) ((m.getMaxStackSize() < 1) ? m.getMaxStackSize() : m.getMaxDurability());
	}
	
	public int getFixedMaxStackSize(Material m) {
		return (int) ((m.getMaxStackSize() < 1) ? m.getMaxDurability() : m.getMaxStackSize());
	}
	
	public short getWoolColor(DyeColor color) {
		switch (color) {
			case WHITE: return 0x0;
			case ORANGE: return 0x1;
			case MAGENTA: return 0x2;
			case LIGHT_BLUE: return 0x3;
			case YELLOW: return 0x4;
			case LIME: return 0x5;
			case PINK: return 0x6;
			case GRAY: return 0x7;
			case SILVER: return 0x8;
			case CYAN: return 0x9;
			case PURPLE: return 0xA;
			case BLUE: return 0xB;
			case BROWN: return 0xC;
			case GREEN: return 0xD;
			case RED: return 0xE;
			case BLACK: return 0xF;
			default: return 0xF;
		}
	}
	
	public short getDyeColor(DyeColor color) {
		switch (color) {
			case WHITE: return 0xF;
			case ORANGE: return 0xE;
			case MAGENTA: return 0xD;
			case LIGHT_BLUE: return 0xC;
			case YELLOW: return 0xB;
			case LIME: return 0xA;
			case PINK: return 0x9;
			case GRAY: return 0x8;
			case SILVER: return 0x7;
			case CYAN: return 0x6;
			case PURPLE: return 0x5;
			case BLUE: return 0x4;
			case BROWN: return 0x3;
			case GREEN: return 0x2;
			case RED: return 0x1;
			case BLACK: return 0x0;
			default: return 0x0;
		}
	}
	
	public OtherBlocks() {
		
		transformList = new ArrayList<OtherBlocksContainer>();
		rng = new Random();
		blockListener = new OtherBlocksBlockListener(this);
		//entityListener = new OtherBlocksEntityListener(this);
		log = Logger.getLogger("Minecraft");
		verbosity = 2;
		pri = Priority.Lowest;
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
		
		if(keys.contains("priority")) {
			String priority_string = getConfiguration().getString("priority", "lowest");
			if(priority_string.equalsIgnoreCase("low")) { pri = Priority.Low; }
			else if(priority_string.equalsIgnoreCase("normal")) { pri = Priority.Normal; }
			else if(priority_string.equalsIgnoreCase("high")) { pri = Priority.High; }
			else if(priority_string.equalsIgnoreCase("highest")) { pri = Priority.Highest; }
			else { pri = Priority.Lowest; }
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
						
						// Source block
						bt.original = Material.valueOf(s);

						// Tool used
						String toolString = String.valueOf(m.get("tool"));
						
						if(toolString.equalsIgnoreCase("DYE")) {
							toolString = "INK_SACK"; 
						}
						
						if(toolString.equalsIgnoreCase("ALL") || toolString.equalsIgnoreCase("ANY")) {
							bt.tool = null;
						} else {
							bt.tool = Material.valueOf(toolString);
						}

						// Dropped item
						String dropString = String.valueOf(m.get("drop"));
						if(dropString.equalsIgnoreCase("DYE")) {
							dropString = "INK_SACK";
						}
						
						if(dropString.length() > 9 && dropString.substring(0, 9).equalsIgnoreCase("CREATURE_")) {
							bt.dropped = CreatureType.valueOf(dropString.substring(9)).toString();
							bt.droptype = "CREATURE";
						} else {
							bt.dropped = Material.valueOf(dropString).toString();
							bt.droptype = "MATERIAL";
						}
						
						// Dropped color
						String dropColor = String.valueOf(m.get("color"));
						
						if(dropColor == "null") {
							bt.color = 0;
						}
						else if(dropString.equalsIgnoreCase("WOOL")) {
							bt.color = getWoolColor(DyeColor.valueOf(dropColor));
						}
						else if(dropString.equalsIgnoreCase("INK_SACK")) {
							bt.color = getDyeColor(DyeColor.valueOf(dropColor));
						}
						else
						{
							bt.color = 0;
						}

						// Dropped quantity
						Integer dropQuantity = Integer.class.cast(m.get("quantity"));
						bt.quantity = (dropQuantity == null || dropQuantity <= 0) ? 1 : dropQuantity;

						// Tool damage
						Integer toolDamage = Integer.class.cast(m.get("damage"));
						bt.damage = (toolDamage == null || toolDamage < 0) ? 1 : toolDamage;

						// Drop probability
						Double dropChance;
						try {
							dropChance = Double.valueOf(String.valueOf(m.get("chance")));
							bt.chance = (dropChance < 0 || dropChance > 100) ? 100 : dropChance;
						} catch(NumberFormatException ex) {
							bt.chance = 100.0;
						}
						
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
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, pri, this);
		//pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, pri, this);

		log.info(getDescription().getName() + " " + getDescription().getVersion() + " loaded.");
	}
}
