package com.sargant.bukkit.otherblocks;


import java.util.List;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.CreatureType;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;

public class OtherBlocksBlockListener extends BlockListener
{
	private OtherBlocks parent;

	public OtherBlocksBlockListener(OtherBlocks instance) {
		parent = instance;
	}
	
	private static boolean containsValidMaterial(Material needle, List<Material> haystack) {
		return (haystack.contains(null) || haystack.contains(needle));
	}
	
	private static boolean containsValidString(String needle, List<String> haystack) {
		return (haystack.contains(null) || haystack.contains(needle));
	}
	
	private static void performDrop(Location target, OtherBlocksContainer dropData) {
		
		if(!OtherBlocks.isCreature(dropData.dropped)) {
			// Special exemption for AIR - breaks the map! :-/
			if(Material.valueOf(dropData.dropped) != Material.AIR) {
				target.getWorld().dropItemNaturally(target, new ItemStack(Material.valueOf(dropData.dropped), dropData.quantity, dropData.color));
			}
		} else {
			target.getWorld().spawnCreature(
					new Location(target.getWorld(), target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5), 
					CreatureType.valueOf(OtherBlocks.creatureName(dropData.dropped))
					);
		}
	}
	
	@Override
	public void onLeavesDecay(LeavesDecayEvent event) {
		
		boolean successfulConversion = false;
		Block target = event.getBlock();
		
		if(event.isCancelled()) return;
		
		for(OtherBlocksContainer obc : parent.transformList) {
			
			// Check it is leaf decay
			if(!OtherBlocks.isLeafDecay(obc.original)) continue;
			
			// Check worlds match
			if(!containsValidString(target.getWorld().getName(), obc.worlds)) continue;
			
			// Check RNG is OK
			if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)) continue;
			
			// Now drop OK
			successfulConversion = true;
			performDrop(target.getLocation(), obc);
		}
		
		if(successfulConversion) {
			// Convert the target block
			event.setCancelled(true);
			target.setType(Material.AIR);
		}
		
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event)
	{

		if (event.isCancelled()) return;

		Block target  = event.getBlock();
		ItemStack tool = event.getPlayer().getItemInHand();
		Integer maxDamage = 0;
		boolean successfulConversion = false;

		for(OtherBlocksContainer obc : parent.transformList) {

			// Check worlds match
			if(!containsValidString(target.getWorld().getName(), obc.worlds)) continue;
			
			// Check held item matches
			if(!containsValidMaterial(tool.getType(), obc.tool)) continue;
			
			// Check target block is not a creature
			if(OtherBlocks.isCreature(obc.original)) continue;
			
			// Check isn't leaf decay
			if(OtherBlocks.isLeafDecay(obc.original)) continue;
			
			// Check target block matches
			if(Material.valueOf(obc.original) != event.getBlock().getType()) continue;

			// Check probability is great than the RNG
			if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)) continue;

			// At this point, the tool and the target block match
			successfulConversion = true;
			performDrop(target.getLocation(), obc);
			maxDamage = (maxDamage < obc.damage) ? obc.damage : maxDamage;
		}

		if(successfulConversion) {

			// Convert the target block
			event.setCancelled(true);
			target.setType(Material.AIR);

			// Check the tool can take wear and tear
			if(tool.getType().getMaxDurability() < 0 || tool.getType().isBlock()) return;

			// Now adjust the durability of the held tool
			tool.setDurability((short) (tool.getDurability() + maxDamage));

			// Manually check whether the tool has exceed its durability limit
			if(tool.getDurability() >= tool.getType().getMaxDurability()) {
				event.getPlayer().setItemInHand(null);
			}
		}

	}
}

