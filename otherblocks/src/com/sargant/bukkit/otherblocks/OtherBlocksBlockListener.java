package com.sargant.bukkit.otherblocks;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockDamageLevel;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;

public class OtherBlocksBlockListener extends BlockListener
{	
	private OtherBlocks parent;
	
	public OtherBlocksBlockListener(OtherBlocks instance)
	{
		parent = instance;
	}
	
	public void onBlockDamage(BlockDamageEvent event)
	{
		
		if (event.isCancelled()) {
			return;
		}
		
		if(event.getDamageLevel() != BlockDamageLevel.BROKEN) {
			return;
		}
		
		for(OtherBlocksContainer obc : parent.transformList) {
			
			// Check held item matches
			ItemStack tool = event.getPlayer().getItemInHand();
			if(obc.tool != null && obc.tool != tool.getType()) {
				continue;
			}
			
			// Check target block matches
			Block target  = event.getBlock();
			if(obc.original != target.getType()) {
				continue;
			}
			
			// Check probability is great than the RNG
			if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)){
				continue;
			}
			
			Location location = new Location(target.getWorld(), target.getX(), target.getY(), target.getZ());
			
			// At this point, the tool and the target block match
			event.setCancelled(true);
			target.setType(Material.AIR);
			target.getWorld().dropItemNaturally(location, new ItemStack(obc.dropped, obc.quantity, obc.color));
			
			// Drop out now if item doesn't have a durability or is a block
			if(parent.getFixedMaxDurability(tool.getType()) < 0 || tool.getType().isBlock()) {
				continue;
			}
			
			// Now adjust the durability of the held tool
			tool.setDurability((short) (tool.getDurability() + obc.damage));
			
			// Manually check whether the tool has exceed its durability limit
			if(tool.getDurability() >= parent.getFixedMaxDurability(tool.getType())) {
				event.getPlayer().setItemInHand(null);
			}
		}
	}
}

