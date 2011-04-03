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
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import com.sargant.bukkit.common.*;

public class OtherBlocks extends JavaPlugin
{
	protected List<OtherBlocksContainer> transformList;
	protected Map<Entity, String> damagerList;
	protected Random rng;
	private final OtherBlocksBlockListener blockListener;
	private final OtherBlocksEntityListener entityListener;
	protected final Logger log;
	protected Integer verbosity;
	protected Priority pri;

	public OtherBlocks() {

		transformList = new ArrayList<OtherBlocksContainer>();
		damagerList = new HashMap<Entity, String>();
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
		verbosity = CommonPlugin.getVerbosity(this);
		pri = CommonPlugin.getPriority(this);
		
		List <String> keys = CommonPlugin.getRootKeys(this);
		
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
						String blockString = getDataEmbeddedBlockString(s);
						String dataString = getDataEmbeddedDataString(s);
						
						bt.original = null;
						bt.setData(null);
						
						if(isCreature(blockString)) {
							// Sheep can be coloured - check here later if need to add data vals to other mobs
							bt.original = "CREATURE_" + CreatureType.valueOf(creatureName(blockString)).toString();
							if(blockString.contains("SHEEP")) {
							    setDataValues(bt, dataString, Material.WOOL);
							}
						} else if(isLeafDecay(blockString)) {
							bt.original = blockString;
							setDataValues(bt, dataString, Material.LEAVES);
						} else if(isSynonymString(blockString)) {
							if(!CommonMaterial.isValidSynonym(blockString)) {
								throw new IllegalArgumentException(blockString + " is not a valid synonym");
							} else {
								bt.original = blockString;
							}
						} else {
							bt.original = Material.valueOf(blockString).toString();
							setDataValues(bt, dataString, Material.valueOf(blockString));
						}

						// Tool used
						bt.tool = new ArrayList<String>();

						if(isLeafDecay(bt.original)) {
							
							bt.tool.add(null);
							
						} else if(m.get("tool") instanceof String) {

							String toolString = (String) m.get("tool");

							if(toolString.equalsIgnoreCase("DYE")) toolString = "INK_SACK";

							if(toolString.equalsIgnoreCase("ALL") || toolString.equalsIgnoreCase("ANY")) {
								bt.tool.add(null);
							} else if(CommonMaterial.isValidSynonym(toolString)) {
								bt.tool.add(toolString);
							} else if(isDamage(toolString) || isCreature(toolString)) {
							    bt.tool.add(toolString);
							} else {
								bt.tool.add(Material.valueOf(toolString).toString());
							}

						} else if (m.get("tool") instanceof List<?>) {

							for(Object listTool : (List<?>) m.get("tool")) {
								String t = (String) listTool;
								if(CommonMaterial.isValidSynonym(t)) {
									bt.tool.add(t);
								} else if(isDamage(t)) {
								    bt.tool.add(t);
								} else {
									bt.tool.add(Material.valueOf(t).toString());
								}
							}

						} else {
							throw new Exception("Not a recognizable type");
						}

						// Dropped item
						String dropString = String.valueOf(m.get("drop"));
						if(dropString.equalsIgnoreCase("DYE")) dropString = "INK_SACK";
						if(dropString.equalsIgnoreCase("NOTHING")) dropString = "AIR";

						if(isCreature(dropString)) {
							bt.dropped = "CREATURE_" + CreatureType.valueOf(creatureName(dropString)).toString();
						} else if(dropString.equalsIgnoreCase("CONTENTS")) {
						    bt.dropped = "CONTENTS";
						} else if(dropString.equalsIgnoreCase("DEFAULT")) {
						    bt.dropped = "DEFAULT";
						} else {
							bt.dropped = Material.valueOf(dropString).toString();
						}

						// Dropped color
						String dropColor = String.valueOf(m.get("color"));

						if(dropColor == "null") bt.color = 0;
						else {
							bt.color = CommonMaterial.getAnyDataShort(Material.valueOf(bt.dropped), dropColor);
						}

						// Dropped quantity
						bt.setQuantity(1);
						try {
						    Integer dropQuantity = Integer.class.cast(m.get("quantity"));
						    bt.setQuantity(dropQuantity);
						} catch(ClassCastException x) {
						    String dropQuantity = String.class.cast(m.get("quantity"));
						    String[] split = dropQuantity.split("-");
						    bt.setQuantity(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
						}

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
								(bt.getQuantityRange() + "x ") +
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
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, pri, this);

		log.info(getDescription().getName() + " " + getDescription().getVersion() + " loaded.");
	}
	
	//
	// Short functions
	//
	
	public static boolean isCreature(String s) {
		return s.startsWith("CREATURE_");
	}
	
	public static boolean isDamage(String s) {
        return s.startsWith("DAMAGE_");
    }
	
	public static boolean isSynonymString(String s) {
		return s.startsWith("ANY_");
	}
	
	public static boolean isLeafDecay(String s) {
		return s.startsWith("SPECIAL_LEAFDECAY");
	}
	
	public static String creatureName(String s) {
		return (isCreature(s) ? s.substring(9) :s);
	}
	
	public static boolean hasDataEmbedded(String s) {
		return s.contains("@");
	}
	
	public static String getDataEmbeddedBlockString(String s) {
		if(!hasDataEmbedded(s)) return s;
		return s.substring(0, s.indexOf("@"));
	}
	
	public static String getDataEmbeddedDataString(String s) {
		if(!hasDataEmbedded(s)) return null;
		return s.substring(s.indexOf("@") + 1);
	}
	
	//
	// Useful longer functions
	//
	
	protected static void setDataValues(OtherBlocksContainer obc, String dataString, Material material) {
	    
	    if(dataString == null) return;
	    
	    if(dataString.startsWith("RANGE-")) {
            String[] dataStringRangeParts = dataString.split("-");
            if(dataStringRangeParts.length != 3) throw new IllegalArgumentException("Invalid range specifier");
            obc.setData(Short.parseShort(dataStringRangeParts[1]), Short.parseShort(dataStringRangeParts[2]));
        } else {
            obc.setData(CommonMaterial.getAnyDataShort(material, dataString));
        }
	}
	
	protected static void performDrop(Location target, OtherBlocksContainer dropData) {
		
		if(!isCreature(dropData.dropped)) {
		    if(dropData.dropped.equalsIgnoreCase("DEFAULT")) return;
		    else if(dropData.dropped.equalsIgnoreCase("CONTENTS")) {
		        Inventory inven = null;
                switch(Material.valueOf(dropData.original)) {
	            case FURNACE:
	            case BURNING_FURNACE:
	                Furnace oven = (Furnace) target.getBlock().getState();
	                // Next three lines make you lose one of the item being smelted
	                // Feel free to remove if you don't like that. -- Celtic Minstrel
	                inven = oven.getInventory();
	                ItemStack cooking = inven.getItem(0); // first item is the item being smelted
                    if(oven.getCookTime() > 0) cooking.setAmount(cooking.getAmount()-1);
                    if(cooking.getAmount() <= 0) inven.setItem(0, null);
                break;
	            case DISPENSER:
	                Dispenser trap = (Dispenser) target.getBlock().getState();
	                inven = trap.getInventory();
                break;
	            case CHEST: // Technically not needed, but included for completeness
	                Chest box = (Chest) target.getBlock().getState();
	                inven = box.getInventory();
                break;
		        }
                if(inven != null) {
                    for(ItemStack item : inven.getContents()) {
                    	if(item.getType() != Material.AIR) {
                    		target.getWorld().dropItemNaturally(target, item);
                    	}
                    }
                }
			// Special exemption for AIR - breaks the map! :-/
		    } else if(Material.valueOf(dropData.dropped) != Material.AIR) {
				target.getWorld().dropItemNaturally(target, new ItemStack(Material.valueOf(dropData.dropped), dropData.getRandomQuantity(), dropData.color));
			}
		} else {
		    Integer quantity = dropData.getRandomQuantity();
			for(Integer i = 0; i < quantity; i++) {
				target.getWorld().spawnCreature(
						new Location(target.getWorld(), target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5), 
						CreatureType.valueOf(OtherBlocks.creatureName(dropData.dropped))
						);
			}
		}
	}
}
