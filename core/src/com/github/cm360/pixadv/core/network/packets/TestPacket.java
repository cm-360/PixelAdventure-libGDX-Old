package com.github.cm360.pixadv.core.network.packets;

import com.github.cm360.pixadv.core.network.Packet;

public class TestPacket implements Packet {

	private String data;
	
	public TestPacket() {
		// For deserialization
	}
	
	public TestPacket(String contents) {
		data = contents;
	}
	
	@Override
	public String serialize() {
		return data;
	}
	
	@Override
	public void deserialize(String serialized) {
		data = serialized;
	}

}
