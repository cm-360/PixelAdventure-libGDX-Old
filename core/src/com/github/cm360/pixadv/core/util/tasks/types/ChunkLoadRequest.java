package com.github.cm360.pixadv.core.util.tasks.types;

import com.github.cm360.pixadv.core.registry.Registry;
import com.github.cm360.pixadv.core.util.tasks.AbstractTask;
import com.github.cm360.pixadv.core.world.storage.world.LocalWorld;
import com.github.cm360.pixadv.core.world.storage.world.RemoteWorld;
import com.github.cm360.pixadv.core.world.storage.world.World;

public class ChunkLoadRequest extends AbstractTask {

	private Registry registry;
	private World world;
	private int cx, cy;
	
	public ChunkLoadRequest(Registry registry, World world, int cx, int cy) {
		super(String.format("requestChunk_%d_%s-%s", world.hashCode(), cx, cy));
		this.registry = registry;
		this.world = world;
		this.cx = cx;
		this.cy = cy;
	}
	
	@Override
	public void process() {
		if (world instanceof LocalWorld) {
			((LocalWorld) world).loadChunk(registry, cx, cy);
		} else if (world instanceof RemoteWorld) {
			// Send chunk request packet
		} else {
			
		}
	}

}
