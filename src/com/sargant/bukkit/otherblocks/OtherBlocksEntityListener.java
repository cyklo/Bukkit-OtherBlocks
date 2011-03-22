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

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

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
		if(event instanceof EntityDamageByEntityEvent) {
	        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
	        if(e.getDamager() instanceof Player) {
	            Player damager = (Player) e.getDamager();
	            parent.damagerList.put(event.getEntity(), damager.getItemInHand().getType());
	        } else parent.damagerList.put(event.getEntity(), CommonEntity.getCreatureType(e.getDamager()));
		} else {
		    DamageCause d = event.getCause();
		    switch(d) {
		    case VOID: // Pointless to handle this since the items probably would also fall into the void
                parent.damagerList.remove(event.getEntity());
	        break;
		    case FIRE_TICK: // No reason to distinguish between the two forms of fire damage in my opinion
                parent.damagerList.put(event.getEntity(), DamageCause.FIRE);
	        break;
		    default:
		        parent.damagerList.put(event.getEntity(), d);
	        break;
		    }
		}
	}
	
	@Override
	public void onEntityDeath(EntityDeathEvent event)
	{
		// At the moment, we only track creatures killed by humans
		if(!parent.damagerList.containsKey(event.getEntity())) {
			return;
		}
		
		Enum<?> weapon = parent.damagerList.get(event.getEntity());
		Entity victim = event.getEntity();
		CreatureType victimType = CommonEntity.getCreatureType(victim);
		
		parent.damagerList.remove(event.getEntity());
		
		if(parent.verbosity > 2)
		    System.out.println("A " + victimType.toString() + " died of " + weapon.toString());
		
		for(OtherBlocksContainer obc : parent.transformList) {
		    
			// Check world matches
			if(!obc.worlds.contains(null) && !obc.worlds.contains(event.getEntity().getWorld().getName())) continue;
			
			// Check held item matches
			if(!OtherBlocks.containsValidWeaponString(weapon.toString(), obc.tool)) continue;
			
			// Check target matches
			if(!OtherBlocks.isCreature(obc.original) || CreatureType.valueOf(OtherBlocks.creatureName(obc.original)) != victimType) {
				continue;
			}

            // Check colour of sheep matches
			if(obc.originalData != null && victim instanceof Sheep && 
			        (obc.originalData != ((Sheep) victim).getColor().getData())) continue;
			
			// Check probability is great than the RNG
			if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)) continue;
			
            obc.setQuantity(parent.rng);
			
			event.getDrops().clear();
			Location location = victim.getLocation();
			
			OtherBlocks.performDrop(location, obc);
		}
	}
}

