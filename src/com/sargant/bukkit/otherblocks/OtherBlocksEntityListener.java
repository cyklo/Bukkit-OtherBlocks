package com.sargant.bukkit.otherblocks;


import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;

import com.sargant.bukkit.common.*;

public class OtherBlocksEntityListener extends EntityListener
{	
	private OtherBlocks parent;
	
	public OtherBlocksEntityListener(OtherBlocks instance)
	{
		parent = instance;
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		
		if(!(event instanceof EntityDamageByEntityEvent)) {
			parent.damagerList.remove(event.getEntity());
			return;
		}
		
		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
		
		if(!(e.getDamager() instanceof Player)) {
			parent.damagerList.remove(event.getEntity());
			return;
		}
		
		Player damager = (Player) e.getDamager();
		
		OtherBlocksDamager d = new OtherBlocksDamager();
		
		d.damager = damager;
		d.target = event.getEntity();
		d.tool = damager.getItemInHand().getType();
		
		parent.damagerList.put(event.getEntity(), d);
		return;
	}
	
	@Override
	public void onEntityDeath(EntityDeathEvent event)
	{
		// At the moment, we only track creatures killed by humans
		if(!parent.damagerList.containsKey(event.getEntity())) {
			return;
		}
		
		Material weapon = parent.damagerList.get(event.getEntity()).tool;
		Entity victim = event.getEntity();
		CreatureType victimType = Common.getCreatureType(victim);
		
		parent.damagerList.remove(event.getEntity());
		
		for(OtherBlocksContainer obc : parent.transformList) {
			
			// Check world matches
			if(!obc.worlds.contains(null) && !obc.worlds.contains(event.getEntity().getWorld().getName())) {
				continue;
			}
			
			// Check held item matches
			if(!obc.tool.contains(null) && !obc.tool.contains(weapon)) {
				continue;
			}

			// Check target matches
			if(!parent.isCreature(obc.original) || CreatureType.valueOf(parent.creatureName(obc.original)) != victimType) {
				continue;
			}

			// Check probability is great than the RNG
			if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)){
				continue;
			}

			event.getDrops().clear();
			Location location = victim.getLocation();
			
			if(!parent.isCreature(obc.dropped)) {
				// Special exemption for AIR - breaks the map! :-/
				if(Material.valueOf(obc.dropped) != Material.AIR) {
					victim.getWorld().dropItemNaturally(location, new ItemStack(Material.valueOf(obc.dropped), obc.quantity, obc.color));
				}
			} else  {
				victim.getWorld().spawnCreature(victim.getLocation(), CreatureType.valueOf(parent.creatureName(obc.dropped)));
			} 
		}
	}
}

