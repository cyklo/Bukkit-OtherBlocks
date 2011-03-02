package com.sargant.bukkit.otherblocks;


import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.CreatureType;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;

import com.sargant.bukkit.common.*;

public class OtherBlocksBlockListener extends BlockListener
{
	private OtherBlocks parent;

	public OtherBlocksBlockListener(OtherBlocks instance)
	{
		parent = instance;
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event)
	{

		if (event.isCancelled()) {
			return;
		}

		Block target  = event.getBlock();
		ItemStack tool = event.getPlayer().getItemInHand();
		Integer maxDamage = 0;
		boolean successfulConversion = false;

		for(OtherBlocksContainer obc : parent.transformList) {

			// Check held item matches
			if(!obc.tool.contains(null) && !obc.tool.contains(tool.getType())) {
				continue;
			}

			// Check target block matches

			if(!obc.originaltype.equalsIgnoreCase("MATERIAL") || Material.valueOf(obc.original) != event.getBlock().getType()) {
				continue;
			}

			// Check probability is great than the RNG
			if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)){
				continue;
			}

			Location location = new Location(target.getWorld(), target.getX(), target.getY(), target.getZ());

			// At this point, the tool and the target block match
			successfulConversion = true;
			try {
				if(obc.droptype.equalsIgnoreCase("MATERIAL")) {
					// Special exemption for AIR - breaks the map! :-/
					if(Material.valueOf(obc.dropped) != Material.AIR) {
						target.getWorld().dropItemNaturally(location, new ItemStack(Material.valueOf(obc.dropped), obc.quantity, obc.color));
					}
				} else if(obc.droptype.equalsIgnoreCase("CREATURE")) {
					target.getWorld().spawnCreature(new Location(target.getWorld(), location.getX() + 0.5, location.getY() + 1, location.getZ() + 0.5), CreatureType.valueOf(obc.dropped));
				} else {
					throw new Exception("InvalidDropType");
				}
			} catch(Exception e) {
				e.printStackTrace();
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

