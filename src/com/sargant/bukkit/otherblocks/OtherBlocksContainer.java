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

import java.util.List;
import java.util.Random;

public class OtherBlocksContainer
{
	public String original;
	public String dropped;
	public List<String> tool;
	public List<String> worlds;
	public Integer damage;
	public Double chance;
	public Short color;
	
	private Short originalDataMin;
    private Short originalDataMax;
	private Integer quantityMin;
    private Integer quantityMax;
	
	private static Random rng = new Random();
	
	// Quantity getters and setters
	
	public Integer getRandomQuantity() {
	    return (quantityMin + rng.nextInt(quantityMax - quantityMin + 1));
	}
	
	public String getQuantityRange() {
	    return (quantityMin.equals(quantityMax) ? quantityMin.toString() : quantityMin.toString() + "-" + quantityMax.toString());
	}
	
	public void setQuantity(Integer val) {
	    this.setQuantity(val, val);
	}
	
	public void setQuantity(Integer low, Integer high) {
	    if(low < high) {
	        this.quantityMin = low;
	        this.quantityMax = high;
	    } else {
	        this.quantityMax = low;
	        this.quantityMin = high;
	    }
	}
	
	// Data getters and setters
	
	public void setData(Short val) {
	    this.setData(val, val);
	}
	
	public void setData(Short low, Short high) {
	    if(low < high) {
	        this.originalDataMin = low;
	        this.originalDataMax = high;
	    } else {
	        this.originalDataMin = high;
	        this.originalDataMax = high;
	    }
	}
	
	public boolean isDataValid(Short test) {
	    if(this.originalDataMin == null) return true;
	    return (test >= this.originalDataMin && test <= this.originalDataMax);
	}
}
