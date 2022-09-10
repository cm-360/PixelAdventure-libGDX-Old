package com.github.cm360.pixadv.core.graphics.picasso;

import com.badlogic.gdx.graphics.Texture;

public class ChunkImage {

	private Texture texture;
	private long creationTime;
	
	/**
	 * @param image
	 * @param creationTime
	 */
	public ChunkImage(Texture texture, long creationTime) {
		this.texture = texture;
		this.creationTime = creationTime;
	}

	public Texture getTexture() {
		return texture;
	}
	
	public long getCreationTime() {
		return creationTime;
	}

}
