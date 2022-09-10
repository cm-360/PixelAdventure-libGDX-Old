package com.github.cm360.pixadv.core.util.tasks.types;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.github.cm360.pixadv.core.graphics.picasso.ChunkImage;
import com.github.cm360.pixadv.core.graphics.picasso.Picasso;
import com.github.cm360.pixadv.core.network.endpoints.Client;
import com.github.cm360.pixadv.core.registry.Identifier;
import com.github.cm360.pixadv.core.util.Logger;
import com.github.cm360.pixadv.core.util.tasks.AbstractTask;
import com.github.cm360.pixadv.core.world.storage.world.World;
import com.github.cm360.pixadv.core.world.types.tiles.Tile;

public class ChunkRepaintTask extends AbstractTask {

	private Client client;
	private World world;
	private int cx, cy;
	
	public ChunkRepaintTask(Client client, World world, int cx, int cy) {
		super(String.format("repaintChunk_%s_%s-%s", world, cx, cy));
		this.client = client;
		this.world = world;
		this.cx = cx;
		this.cy = cy;
	}
	
	@Override
	public void process() {
		Picasso picasso = client.getRenderingEngine();
		GridPoint2 chunkPos = new GridPoint2(cx, cy);
		String chunkName = chunkPos.toString();
		// Create a new image for the chunk cache
		int tileTextureSize = picasso.getWorldPainter().getTileTextureSize() * 2;
		int chunkTextureSize = tileTextureSize * world.getChunkSize();
		Pixmap chunkPixmap = new Pixmap(
				chunkTextureSize,
				chunkTextureSize,
				Pixmap.Format.RGBA8888);
		Texture chunkTexture = new Texture(chunkPixmap);
		// Draw tiles to chunk image
		for (int xc = 0; xc < world.getChunkSize(); xc++)
			for (int yc = 0; yc < world.getChunkSize(); yc++) {
				try {
					int x = cx * world.getChunkSize() + xc;
					int y = cy * world.getChunkSize() + yc;
					// Draw tile layers
					for (int l = 0; l < 3; l++) {
						Tile tile = world.getTile(x, y, l);
						if (tile != null) {
							for (Identifier textureId : tile.getTextures()) {
								Pixmap tilePixmap = client.getRegistry().getPixmap(textureId);
//								if (l == 0)
//									tileTexture = ImageUtil.applyBrightness(tileTexture, 0.5);
								// Texture coordinates in chunk image
								int tx = tileTextureSize * xc;
								int ty = tileTextureSize * yc;
								// Draw
								chunkTexture.draw(tilePixmap, tx, chunkTextureSize - (ty + tileTextureSize));
							} 
						}
					}
				} catch (Exception e) {
					// Save exception info
					client.getGuiManager().setLastExceptionInfo(
							"Exception: Chunk %s   %s: %s".formatted(
									chunkName,
									e.getClass().getName(),
									e.getMessage()),
							System.currentTimeMillis());
					Logger.logException("Exception while drawing chunk %s", e, chunkName);
				}
			}
		// Finalization
		chunkPixmap.dispose();
		picasso.getWorldPainter().cacheChunkImage(chunkPos, new ChunkImage(chunkTexture, System.nanoTime()));
		world.getChunkUpdates().remove(chunkPos);
	}

}
