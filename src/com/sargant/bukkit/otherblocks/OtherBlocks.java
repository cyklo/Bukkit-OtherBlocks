// OtherBlocks - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.sargant.bukkit.otherblocks;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import com.sargant.bukkit.common.Common;

public class OtherBlocks extends JavaPlugin
{
	protected List<OtherBlocksContainer> transformList;
	protected Map<Entity, OtherBlocksDamager> damagerList;
	protected Random rng;
	private final OtherBlocksBlockListener blockListener;
	private final OtherBlocksEntityListener entityListener;
	protected final Logger log;
	protected Integer verbosity;
	protected Priority pri;

	public OtherBlocks() {

		transformList = new ArrayList<OtherBlocksContainer>();
		damagerList = new HashMap<Entity, OtherBlocksDamager>();
		rng = new Random();
		blockListener = new OtherBlocksBlockListener(this);
		entityListener = new OtherBlocksEntityListener(this);
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
		verbosity = Common.getVerbosity(this);
		pri = Common.getPriority(this);
		
		List <String> keys = Common.getRootKeys(this);
		
		if(keys == null) {
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
						String originalString = s;
						
						if(isCreature(originalString)) {
							bt.original = "CREATURE_" + CreatureType.valueOf(creatureName(originalString)).toString();
						} else if(isLeafDecay(originalString)) {
							bt.original = originalString;
						} else {
							bt.original = Material.valueOf(originalString).toString();
						}

						// Tool used
						bt.tool = new ArrayList<Material>();

						if(isLeafDecay(bt.original)) {
							
							bt.tool.add((Material) null);
							
						} else if(m.get("tool") instanceof String) {

							String toolString = (String) m.get("tool");

							if(toolString.equalsIgnoreCase("DYE")) {
								toolString = "INK_SACK";
							}

							if(toolString.equalsIgnoreCase("ALL") || toolString.equalsIgnoreCase("ANY")) {
								bt.tool.add((Material) null);
							} else {
								bt.tool.add(Material.valueOf(toolString));
							}

						} else if (m.get("tool") instanceof List<?>) {

							for(Object listTool : (List<?>) m.get("tool")) {
								bt.tool.add(Material.valueOf((String) listTool));
							}

						} else {
							throw new Exception("Not a recognizable type");
						}

						// Dropped item
						String dropString = String.valueOf(m.get("drop"));
						if(dropString.equalsIgnoreCase("DYE")) {
							dropString = "INK_SACK";
						}

						if(isCreature(dropString)) {
							bt.dropped = "CREATURE_" + CreatureType.valueOf(creatureName(dropString)).toString();
						} else {
							bt.dropped = Material.valueOf(dropString).toString();
						}

						// Dropped color
						String dropColor = String.valueOf(m.get("color"));

						if(dropColor == "null") {
							bt.color = 0;
						}
						else if(dropString.equalsIgnoreCase("WOOL")) {
							bt.color = Common.getWoolColor(DyeColor.valueOf(dropColor));
						}
						else if(dropString.equalsIgnoreCase("INK_SACK")) {
							bt.color = Common.getDyeColor(DyeColor.valueOf(dropColor));
						}
						else {
							bt.color = Short.valueOf(dropColor);
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
						
						// Applicable worlds
						// Tool used
						bt.worlds = new ArrayList<String>();

						if(m.get("world") == null) {
							bt.worlds.add((String) null);
						}
						else if(m.get("world") instanceof String) {

							String worldString = (String) m.get("world");

							if(worldString.equalsIgnoreCase("ALL") || worldString.equalsIgnoreCase("ANY")) {
								bt.worlds.add((String) null);
							} else {
								bt.worlds.add(worldString);
							}

						} else if (m.get("world") instanceof List<?>) {

							for(Object listWorld : (List<?>) m.get("world")) {
								bt.worlds.add((String) listWorld);
							}

						} else {
							throw new Exception("Not a recognizable type");
						}

					} catch(Throwable ex) {
						if(verbosity > 1) {
							log.warning("Error while processing block " + s + ": " + ex.getMessage());
						}

						ex.printStackTrace();
						continue;
					}

					transformList.add(bt);

					if(verbosity > 1) {
						log.info(getDescription().getName() + ": " +
								(bt.tool.contains(null) ? "ALL TOOLS" : (bt.tool.size() == 1 ? bt.tool.get(0).toString() : bt.tool.toString())) + " + " +
								creatureName(bt.original) + " now drops " +
								(bt.quantity != 1 ? bt.quantity.toString() + "x " : "") +
								creatureName(bt.dropped) +
								(bt.chance < 100 ? " with " + bt.chance.toString() + "% chance" : ""));
					}
				}
			}
		}

		// Done setting up plugin

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, pri, this);
		pm.registerEvent(Event.Type.LEAVES_DECAY, blockListener, pri, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, pri, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGED, entityListener, pri, this);

		log.info(getDescription().getName() + " " + getDescription().getVersion() + " loaded.");
	}
	
	//
	// Short functions
	//
	
	public static boolean isCreature(String s) {
		return s.startsWith("CREATURE_");
	}
	
	public static boolean isLeafDecay(String s) {
		return s.equalsIgnoreCase("SPECIAL_LEAFDECAY");
	}
	
	public static String creatureName(String s) {
		return (isCreature(s) ? s.substring(9) :s);
	}
}
