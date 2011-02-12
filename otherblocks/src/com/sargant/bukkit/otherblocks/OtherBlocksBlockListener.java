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
			
			ItemStack tool = event.getPlayer().getItemInHand();
			if(obc.tool != tool.getType()) {
				continue;
			}
			
			Block target  = event.getBlock();
			if(obc.original != target.getType()) {
				continue;
			}
			
			Location lx = new Location(target.getWorld(), target.getX(), target.getY(), target.getZ());
			
			// At this point, the tool and the target block match
			event.setCancelled(true);
			target.setType(Material.AIR);
			target.getWorld().dropItemNaturally(lx, new ItemStack(obc.dropped, obc.quantity));
			
			// Now adjust the durability of the held tool
			tool.setDurability((short) (tool.getDurability() + obc.damage));
			
			// Manually check whether the tool has exceed its durability limit
			if(tool.getDurability() >= tool.getType().getMaxDurability()) {
				event.getPlayer().setItemInHand(null);
			}
		}
	}
}

