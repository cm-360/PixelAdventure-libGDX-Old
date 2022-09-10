package com.github.cm360.pixadv.core.world.types.tiles;

import java.util.List;

import com.github.cm360.pixadv.core.registry.Identifier;

public interface Tile {
	
	public String getID();
	
	public void setData(String data);
	
	public String getData();
	
	public List<Identifier> getTextures();
	
	public double getFriction();

}
