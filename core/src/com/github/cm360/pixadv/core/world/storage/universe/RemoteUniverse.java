package com.github.cm360.pixadv.core.world.storage.universe;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Supplier;

import com.github.cm360.pixadv.core.network.Connection;
import com.github.cm360.pixadv.core.network.packets.universe.UniverseInfoPacket;
import com.github.cm360.pixadv.core.registry.Registry;
import com.github.cm360.pixadv.core.world.storage.world.RemoteWorld;

public class RemoteUniverse extends Universe {

	protected Connection connection;
	
	public RemoteUniverse(Registry registry, Connection connection, UniverseInfoPacket info, Supplier<UUID> playerIdSupplier) {
		super(registry, playerIdSupplier);
		this.connection = connection;
		//
		this.info.put("name", info.getName());
		for (String worldName : info.getWorlds()) {
			this.worlds.put(worldName, new RemoteWorld(4, 3, 20, new HashMap<String, String>()));
		}
	}
	
	@Override
	public void close() {
		super.close();
		connection.close();
	}

}
