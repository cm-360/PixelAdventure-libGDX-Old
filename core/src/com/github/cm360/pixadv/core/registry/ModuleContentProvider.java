package com.github.cm360.pixadv.core.registry;

import java.util.Map;

import com.github.cm360.pixadv.core.world.types.entities.Entity;
import com.github.cm360.pixadv.core.world.types.tiles.Tile;

public interface ModuleContentProvider {

	public Map<String, Class<? extends Tile>> getTiles();

	public Map<String, Class<? extends Entity>> getEntities();

}
