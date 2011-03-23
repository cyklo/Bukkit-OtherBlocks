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
	public Short originalData;
	public Short originalDataRangeMax;
	public String dropped;
	public List<String> tool;
	public List<String> worlds;
	public Integer quantity;
    public Integer quantityMin;
    public Integer quantityMax;
	public Integer damage;
	public Double chance;
	public Short color;
	
	public void setQuantity(Random rng) {
	    if(quantityMin == null) quantity = 1;
	    else if(quantityMax == null) quantity = quantityMin;
	    else quantity = quantityMin + rng.nextInt(quantityMax - quantityMin + 1);
	}
}
