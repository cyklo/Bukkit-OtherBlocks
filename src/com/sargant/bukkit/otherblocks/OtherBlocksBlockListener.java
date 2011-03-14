package com.sargant.bukkit.otherblocks;


import java.util.List;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.CreatureType;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;

import com.sargant.bukkit.common.*;

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
			
			// Check target block matches
			if(Material.valueOf(obc.original) != event.getBlock().getType()) continue;

			// Check probability is great than the RNG
			if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)) continue;

			Location location = new Location(target.getWorld(), target.getX(), target.getY(), target.getZ());

			// At this point, the tool and the target block match
			successfulConversion = true;
			if(!OtherBlocks.isCreature(obc.dropped)) {
				// Special exemption for AIR - breaks the map! :-/
				if(Material.valueOf(obc.dropped) != Material.AIR) {
					target.getWorld().dropItemNaturally(location, new ItemStack(Material.valueOf(obc.dropped), obc.quantity, obc.color));
				}
			} else {
				target.getWorld().spawnCreature(new Location(target.getWorld(), location.getX() + 0.5, location.getY() + 1, location.getZ() + 0.5), CreatureType.valueOf(OtherBlocks.creatureName(obc.dropped)));
			}
			maxDamage = (maxDamage < obc.damage) ? obc.damage : maxDamage;
		}

		if(successfulConversion) {

			// Convert the target block
			event.setCancelled(true);
			target.setType(Material.AIR);

			// Check the tool can take wear and tear
			if(Common.getFixedMaxDurability(tool.getType()) < 0 || tool.getType().isBlock()) {
				return;
			}

			// Now adjust the durability of the held tool
			tool.setDurability((short) (tool.getDurability() + maxDamage));

			// Manually check whether the tool has exceed its durability limit
			if(tool.getDurability() >= Common.getFixedMaxDurability(tool.getType())) {
				event.getPlayer().setItemInHand(null);
			}
		}

	}
}

