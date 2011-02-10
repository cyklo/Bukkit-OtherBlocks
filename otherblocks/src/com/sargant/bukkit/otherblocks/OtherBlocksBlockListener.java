package com.sargant.bukkit.otherblocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockDamageLevel;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;

public class OtherBlocksBlockListener extends BlockListener
{
	@SuppressWarnings("unused")
	private OtherBlocks parent;

	public OtherBlocksBlockListener(OtherBlocks instance)
	{
		parent = instance;
	}

	public void onBlockDamage(BlockDamageEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		List<Material> goodTools = new ArrayList<Material>();

		goodTools.add(Material.GOLD_AXE);
		goodTools.add(Material.GOLD_HOE);
		goodTools.add(Material.GOLD_PICKAXE);
		goodTools.add(Material.GOLD_SPADE);
		goodTools.add(Material.GOLD_SWORD);

		// Check that the currently held tool is an appropriate one
		if(goodTools.contains(event.getPlayer().getItemInHand().getType()) == false)
		{
			return;
		}

		if (event.getDamageLevel() != BlockDamageLevel.BROKEN)
		{
			return;
		}

		Player p = event.getPlayer();
		ItemStack t = p.getItemInHand();
		Block b = event.getBlock();

		Location lx = new Location(b.getWorld(), b.getX(), b.getY(), b.getZ());

		if(b.getType() == Material.SAND && t.getType() == Material.GOLD_SPADE)
		{
			event.setCancelled(true);
			b.setType(Material.AIR);
			b.getWorld().dropItemNaturally(lx, new ItemStack(Material.GLOWSTONE_DUST, 4));
			t.setDurability((short) (t.getDurability()+1));
		}
		else if(b.getType() == Material.COAL_ORE && t.getType() == Material.GOLD_PICKAXE)
		{
			event.setCancelled(true);
			b.setType(Material.AIR);
			b.getWorld().dropItemNaturally(lx, new ItemStack(Material.NETHERRACK, 1));
			t.setDurability((short) (t.getDurability()+1));
		}

		if(t.getDurability() >= 32)
		{
			p.setItemInHand(null);
		}
	}
}

