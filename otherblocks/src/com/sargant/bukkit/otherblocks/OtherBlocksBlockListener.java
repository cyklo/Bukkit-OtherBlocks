package com.sargant.bukkit.otherblocks;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;

public class OtherBlocksBlockListener extends BlockListener
{	
	private OtherBlocks parent;
	
	public OtherBlocksBlockListener(OtherBlocks instance)
	{
		parent = instance;
	}
	
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
			if(obc.tool != null && obc.tool != tool.getType()) {
				continue;
			}
			
			// Check target block matches
			
			if(obc.original != event.getBlock().getType()) {
				continue;
			}
			
			// Check probability is great than the RNG
			if(parent.rng.nextDouble() > (obc.chance/100)){
				continue;
			}
			
			Location location = new Location(target.getWorld(), target.getX(), target.getY(), target.getZ());
			
			// At this point, the tool and the target block match
			successfulConversion = true;
			target.getWorld().dropItemNaturally(location, new ItemStack(obc.dropped, obc.quantity, obc.color));
			maxDamage = (maxDamage < obc.damage) ? obc.damage : maxDamage;
			
		}
		
		if(successfulConversion) {
			
			// Convert the target block
			event.setCancelled(true);
			target.setType(Material.AIR);
			
			// Check the tool can take wear and tear
			if(parent.getFixedMaxDurability(tool.getType()) < 0 || tool.getType().isBlock()) {
				return;
			}
			
		  // Now adjust the durability of the held tool
			tool.setDurability((short) (tool.getDurability() + maxDamage));
			
			// Manually check whether the tool has exceed its durability limit
			if(tool.getDurability() >= parent.getFixedMaxDurability(tool.getType())) {
				event.getPlayer().setItemInHand(null);
			}
		}
		
	}
}

