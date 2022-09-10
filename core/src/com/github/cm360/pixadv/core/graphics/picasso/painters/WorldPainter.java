package com.github.cm360.pixadv.core.graphics.picasso.painters;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.github.cm360.pixadv.core.graphics.edison.Edison;
import com.github.cm360.pixadv.core.graphics.picasso.ChunkImage;
import com.github.cm360.pixadv.core.graphics.picasso.HashablePoint;
import com.github.cm360.pixadv.core.graphics.picasso.Picasso;
import com.github.cm360.pixadv.core.graphics.picasso.Precompute;
import com.github.cm360.pixadv.core.graphics.picasso.RenderStats;
import com.github.cm360.pixadv.core.registry.Identifier;
import com.github.cm360.pixadv.core.registry.Registry;
import com.github.cm360.pixadv.core.util.Logger;
import com.github.cm360.pixadv.core.util.Stopwatch;
import com.github.cm360.pixadv.core.util.tasks.types.ChunkLoadRequest;
import com.github.cm360.pixadv.core.util.tasks.types.ChunkRepaintTask;
import com.github.cm360.pixadv.core.world.storage.world.World;
import com.github.cm360.pixadv.core.world.types.entities.Entity;

public class WorldPainter {

	private final Picasso parent;
	
	private int tileTextureSize = 16;
	private double tileTextureScale = 4;
	
	private int chunkCacheTarget = 200;
	private int chunkCacheMax = 300;
	private Map<HashablePoint, ChunkImage> chunkCache;
	
	public WorldPainter(Picasso parent) {
		this.parent = parent;
		chunkCache = new HashMap<HashablePoint, ChunkImage>();
	}
	
	public void paint(SpriteBatch b, Registry registry, List<Disposable> trash, World world, Precompute precomp, Stopwatch renderTimes) {
		RenderStats stats = new RenderStats();
		if (world != null) {
			precomp.update(world, parent);
			// Save entity coordinates before rendering
			Map<UUID, Vector2> entityPositions = world.getEntities().entrySet().stream()
					.collect(Collectors.toMap(entry -> {
						return entry.getKey();
					}, entry -> {
						Entity entity = entry.getValue();
						return new Vector2((float) entity.getX(), (float) entity.getY());
					}));
			//
			Vector2 cameraFollowedEntityPos = entityPositions.get(parent.getClient().getCameraFollowedId());
			if (cameraFollowedEntityPos != null) {
				world.setCameraX(cameraFollowedEntityPos.x);
				world.setCameraY(cameraFollowedEntityPos.y);
			}
			renderTimes.mark("entity-positions");
			// Draw sky
			paintSky(b, world, precomp);
			renderTimes.mark("sky");
			// Draw parallax background
			paintBackgroundImages(b, world, precomp);
			renderTimes.mark("background");
			// Draw chunkmap
			paintChunkmap(b, registry, trash, world, precomp);
			renderTimes.mark("chunkmap");
			// Draw lightmap
	//		paintLightmap(b, world, precomp);
			renderTimes.mark("lightmap");
			// Draw entities
			paintEntities(b, world, precomp, stats, entityPositions);
			renderTimes.mark("entities");
			// Draw tile hover info
	//		if (parent.showUI && mouseLocation != null) {
	//			Point mouseTile = getMouseTile(world, precomp, mouseLocation);
	//			paintMouseHover(g, world, precomp, mouseLocation, mouseTile);
	//			renderTimes.mark("mouse");
	//		}
		}
	}
	
	protected void paintSky(SpriteBatch b, World world, Precompute precomp) {
		float[] horizonColor = {182, 206, 245};
		float[] zenithColor = {85, 127, 187};
		Rectangle vBounds = precomp.getViewportBounds();
		for (int h = 0; h < vBounds.height; h++) {
			float elevation = ((float) h) / vBounds.height;
			b.setColor(new Color(
					((horizonColor[0] * elevation) + (zenithColor[0] * (1 - elevation))),
					((horizonColor[1] * elevation) + (zenithColor[1] * (1 - elevation))),
					((horizonColor[2] * elevation) + (zenithColor[2] * (1 - elevation))),
					1f));
//			b.drawLine(
//					vBounds.x,
//					vBounds.y + h,
//					vBounds.x + vBounds.width,
//					vBounds.y + h);
		}
	}
	
	protected void paintBackgroundImages(SpriteBatch b, World world, Precompute precomp) {
		int[] powersOf2 = {1, 2, 4, 8, 16, 32, 64, 128};
		Identifier backgroundImageId = Identifier.parse("pixadv:textures/tiles/missing");
		for (int i = 3; i >= 1; i--) {
			b.draw(parent.getClient().getRegistry().getTexture(backgroundImageId),
					(float) (precomp.getViewportBounds().x - (world.getCameraX() * precomp.getScaledTileTextureSize()) / powersOf2[i]),
					(float) (precomp.getViewportBounds().y + (((world.getCameraY() * precomp.getScaledTileTextureSize()) / powersOf2[i]) - (world.getHeight() * world.getChunkSize()))),
					precomp.getViewportBounds().width,
					precomp.getViewportBounds().height);
		}
	}
	
	protected void paintChunkmap(SpriteBatch b, Registry registry, List<Disposable> trash, World world, Precompute precomp) {
		// Avoid wasting memory with old cached images
		if (getCacheSize() > chunkCacheMax) {
			GridPoint2 cameraPoint = world.correctCoord(
					(int) Math.round(world.getCameraX()),
					(int) Math.round(world.getCameraY()));
			GridPoint2 cameraChunk = world.getChunkOf(cameraPoint.x, cameraPoint.y);
			// TODO trim cache
//			client.getTaskQueueManager().addGenericTask(() -> trimCache(cameraChunk));
		}
		// Draw the different chunk images
		for (int cx = (int) Math.round((precomp.getMinX() + 0.5) / world.getChunkSize() - 0.5); cx <= (int) Math.round((precomp.getMaxX() - 0.5) / world.getChunkSize() - 0.5); cx++) {
			for (int cy = (int) Math.round((precomp.getMinY() + 0.5) / world.getChunkSize() - 0.5); cy <= (int) Math.round((precomp.getMaxY() - 0.5) / world.getChunkSize() - 0.5); cy++) {
				if (cy >= 0 && cy < world.getHeight()) {
					// Get actual chunk coordinates
					HashablePoint correctedChunk = new HashablePoint(world.correctChunkCoord(cx, cy));
					if (world.isChunkLoaded(correctedChunk.x, correctedChunk.y)) {
						ChunkImage chunkImage = chunkCache.get(correctedChunk);
						if (chunkImage == null || world.getChunkUpdates().contains(correctedChunk)) {
							// TODO repaint chunk
							Gdx.app.postRunnable(() -> {
								new ChunkRepaintTask(parent.getClient(), world, correctedChunk.x, correctedChunk.y).process();
							});
//							parent.getClient().addTask();
						}
						if (chunkImage != null) {
							// Draw entire chunk
							b.draw(chunkImage.getTexture(),
									(float) (precomp.getCenterX() + precomp.getScaledTileTextureSize()
											* (cx * world.getChunkSize() - world.getCameraX())),
									(float) (precomp.getCenterY() + precomp.getScaledTileTextureSize()
											* (cy * world.getChunkSize() - world.getCameraY())),
									(float) (precomp.getScaledTileTextureSize() * world.getChunkSize()),
									(float) (precomp.getScaledTileTextureSize() * world.getChunkSize()));
						}
					} else {
						// Request that this chunk be loaded
						parent.getClient().addTask(new ChunkLoadRequest(registry, world, correctedChunk.x, correctedChunk.y));
					}
					// Draw chunk-specific debug info
					if (parent.showUI && parent.showDebugMenu) {
//						b.setColor(Color.WHITE);
//						b.setFont(new Font(null, 0, 12));
//						g.drawRect(
//								precomp.getCenterX() + (int) Math.round(precomp.getScaledTileTextureSize() * (cx * world.getChunkSize() - world.getCameraX())),
//								precomp.getCenterY() - (int) Math.round(precomp.getScaledTileTextureSize() * ((cy + 1) * world.getChunkSize() - world.getCameraY() - 1)),
//								(int) (precomp.getScaledTileTextureSize() * world.getChunkSize()),
//								(int) (precomp.getScaledTileTextureSize() * world.getChunkSize()));
//						g.drawString(String.format("%d,%d (%d,%d)", correctedChunk.x, correctedChunk.y, cx, cy),
//								precomp.getCenterX() + (int) Math.round(precomp.getScaledTileTextureSize() * (cx * world.getChunkSize() - world.getCameraX())) + 5,
//								precomp.getCenterY() - (int) Math.round(precomp.getScaledTileTextureSize() * ((cy + 1) * world.getChunkSize() - world.getCameraY() - 1)) + 15);
					}
				}
			}
		}
	}
	
	protected void paintLightmap(SpriteBatch b, World world, Precompute precomp) {
		Edison edison = world.getLightingEngine();
		// Draw the different chunk images
		for (int cx = (int) Math.round((precomp.getMinX() + 0.5) / world.getChunkSize() - 0.5); cx <= (int) Math.round((precomp.getMaxX() - 0.5) / world.getChunkSize() - 0.5); cx++) {
			for (int cy = (int) Math.round((precomp.getMinY() + 0.5) / world.getChunkSize() - 0.5); cy <= (int) Math.round((precomp.getMaxY() - 0.5) / world.getChunkSize() - 0.5); cy++) {
				if (cy >= 0 && cy < world.getHeight()) {
					// Get actual chunk coordinates
					HashablePoint correctedChunk = new HashablePoint(world.correctChunkCoord(cx, cy));
					if (world.isChunkLoaded(correctedChunk.x, correctedChunk.y)) {
						if (edison.isLit(correctedChunk)) {
							Texture lightmap = edison.getLightmap(correctedChunk).getScaledMap();
							// Draw entire chunk
							b.draw(lightmap,
									precomp.getCenterX() + (float) (precomp.getScaledTileTextureSize()
											* (cx * world.getChunkSize() - world.getCameraX())),
									precomp.getCenterY() - (float) (precomp.getScaledTileTextureSize()
											* ((cy + 1) * world.getChunkSize() - world.getCameraY() - 1)),
									(float) (precomp.getScaledTileTextureSize() * world.getChunkSize()),
									(float) (precomp.getScaledTileTextureSize() * world.getChunkSize()));
						} else {
							edison.relight(correctedChunk);
						}
					}
					// Draw chunk-specific debug info
					if (parent.showUI && parent.showDebugMenu) {
//						b.setColor(Color.WHITE);
//						b.setFont(new Font(null, 0, 12));
//						b.drawRect(
//								precomp.getCenterX() + (int) Math.round(precomp.getScaledTileTextureSize() * (cx * world.getChunkSize() - world.getCameraX())),
//								precomp.getCenterY() - (int) Math.round(precomp.getScaledTileTextureSize() * ((cy + 1) * world.getChunkSize() - world.getCameraY() - 1)),
//								(int) (precomp.getScaledTileTextureSize() * world.getChunkSize()),
//								(int) (precomp.getScaledTileTextureSize() * world.getChunkSize()));
//						b.drawString(String.format("%d,%d (%d,%d)", correctedChunk.x, correctedChunk.y, cx, cy),
//								precomp.getCenterX() + (int) Math.round(precomp.getScaledTileTextureSize() * (cx * world.getChunkSize() - world.getCameraX())) + 5,
//								precomp.getCenterY() - (int) Math.round(precomp.getScaledTileTextureSize() * ((cy + 1) * world.getChunkSize() - world.getCameraY() - 1)) + 15);
					}
				}
			}
		}
	}
	
	protected void paintEntities(SpriteBatch b, World world, Precompute precomp, RenderStats stats, Map<UUID, Vector2> entityPositions) {
		int renderCount = 0;
		Map<UUID, Entity> entities = world.getEntities();
		stats.setTotalEntities(entities.size());
		// Draw entities
		for (UUID uuid : entities.keySet()) {
			Entity entity = entities.get(uuid);
			try {
//				HashMap<String, String> data = new Gson().fromJson(entity.getData(), TypeToken.getParameterized(HashMap.class, String.class, String.class).getType());
				// Grab entity location info
				double x = entity.getX();
				double y = entity.getY();
				Vector2 entityPos = entityPositions.get(uuid);
				if (entityPos != null) {
					x = entityPos.x;
					y = entityPos.y;
				}
				
				int pixelsWidth = (int) Math.round(precomp.getScaledTileTextureSize() * entity.getWidth());
				int pixelsHeight = (int) Math.round(precomp.getScaledTileTextureSize() * entity.getHeight());
				// Check if entity is visible
				if ((x >= precomp.getMinX() && x < precomp.getMaxX()) && (y >= precomp.getMinY() && y < precomp.getMaxY())) {
					// Draw to panel
					b.draw(entity.getTexture(),
							(precomp.getCamBounds().x + (precomp.getCamBounds().width / 2) - pixelsWidth / 2) + (int) Math.round(precomp.getScaledTileTextureSize() * (x - world.getCameraX())),
							(precomp.getCamBounds().y + (precomp.getCamBounds().height / 2) - pixelsHeight / 2) - (int) Math.round(precomp.getScaledTileTextureSize() * (y - world.getCameraY())),
							pixelsWidth, pixelsHeight);
					if (parent.showUI && parent.showDebugMenu) {
//						// Draw bounding box
//						if (parent.getClient().getControlledIds().contains(uuid))
//							g.setColor(Color.CYAN);
//						else
//							g.setColor(Color.WHITE);
//						g.drawRect(
//								(precomp.getCamBounds().x + (precomp.getCamBounds().width / 2) - pixelsWidth / 2) + (int) Math.round(precomp.getScaledTileTextureSize() * (x - world.getCameraX())),
//								(precomp.getCamBounds().y + (precomp.getCamBounds().height / 2) - pixelsHeight / 2) - (int) Math.round(precomp.getScaledTileTextureSize() * (y - world.getCameraY())),
//								pixelsWidth - 1, pixelsHeight - 1);
//						// Draw velocity vector
//						double xVel = entity.getXVel();
//						double yVel = entity.getYVel();
//						g.setColor(Color.GREEN);
//						g.drawLine(
//								(precomp.getCamBounds().x + (precomp.getCamBounds().width / 2)) + (int) Math.round(precomp.getScaledTileTextureSize() * (x - world.getCameraX())),
//								(precomp.getCamBounds().y + (precomp.getCamBounds().height / 2)) - (int) Math.round(precomp.getScaledTileTextureSize() * (y - world.getCameraY())),
//								(precomp.getCamBounds().x + (precomp.getCamBounds().width / 2)) + (int) Math.round(precomp.getScaledTileTextureSize() * ((x + xVel) - world.getCameraX())),
//								(precomp.getCamBounds().y + (precomp.getCamBounds().height / 2)) - (int) Math.round(precomp.getScaledTileTextureSize() * ((y + yVel) - world.getCameraY())));
//						// Draw acceleration vector
//						double xAccel = entity.getXAccel();
//						double yAccel = entity.getYAccel();
//						g.setColor(Color.ORANGE);
//						g.drawLine(
//								(precomp.getCamBounds().x + (precomp.getCamBounds().width / 2)) + (int) Math.round(precomp.getScaledTileTextureSize() * (x - world.getCameraX())),
//								(precomp.getCamBounds().y + (precomp.getCamBounds().height / 2)) - (int) Math.round(precomp.getScaledTileTextureSize() * (y - world.getCameraY())),
//								(precomp.getCamBounds().x + (precomp.getCamBounds().width / 2)) + (int) Math.round(precomp.getScaledTileTextureSize() * ((x + xAccel) - world.getCameraX())),
//								(precomp.getCamBounds().y + (precomp.getCamBounds().height / 2)) - (int) Math.round(precomp.getScaledTileTextureSize() * ((y + yAccel) - world.getCameraY())));
//						// Draw coordinates label
//						if (pixelsWidth > 50) {
//							g.setColor(Color.WHITE);
//							g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
//							g.drawString("X: %.2f".formatted(x),
//									4 + (precomp.getCamBounds().x + (precomp.getCamBounds().width / 2) - pixelsWidth / 2) + (int) Math.round(precomp.getScaledTileTextureSize() * (x - world.getCameraX())),
//									pixelsHeight + 12 + (precomp.getCamBounds().y + (precomp.getCamBounds().height / 2) - pixelsHeight / 2) - (int) Math.round(precomp.getScaledTileTextureSize() * (y - world.getCameraY())));
//							g.drawString("Y: %.2f".formatted(y),
//									4 + (precomp.getCamBounds().x + (precomp.getCamBounds().width / 2) - pixelsWidth / 2) + (int) Math.round(precomp.getScaledTileTextureSize() * (x - world.getCameraX())),
//									pixelsHeight + 24 + (precomp.getCamBounds().y + (precomp.getCamBounds().height / 2) - pixelsHeight / 2) - (int) Math.round(precomp.getScaledTileTextureSize() * (y - world.getCameraY())));
//						}
					}
					renderCount++;
				}
			} catch (Exception e) {
				// TODO rendering exception
				String message = "Exception while rendering entity!  UUID: %s  Type: %s".formatted(entity.getID(), uuid);
				Logger.logException(message, e);
				parent.getClient().getGuiManager().setLastExceptionInfo("Exception while rendering entity Entity ID '%s'   %s: %s".formatted(
						entity.getID(),
						e.getClass().getName(),
						e.getMessage()), System.currentTimeMillis());
			}
		}
		stats.setUniqueEntities(renderCount);
	}
	
	public void cacheChunkImage(GridPoint2 location, ChunkImage chunkImage) {
		// Dispose of old texture
		ChunkImage oldChunkImage = chunkCache.get(location);
		if (oldChunkImage != null)
			oldChunkImage.getTexture().dispose();
		// Cache new texture
		chunkCache.put(new HashablePoint(location), chunkImage);
	}

	public int getCacheSize() {
		return chunkCache.size();
	}
	
	public void trimCache(GridPoint2 centerChunk) {
		chunkCache.keySet().retainAll(chunkCache.keySet().stream().sorted((point1, point2) -> {
			return (int) Math.round(point1.dst(centerChunk) - point2.dst(centerChunk));
		}).limit(chunkCacheTarget).toList());
	}
	
	public void clearCache() {
		chunkCache.clear();
	}
	
	// Info methods
	public int getTileTextureSize() {
		return tileTextureSize;
	}

	public void setTileScale(double scale) {
		tileTextureScale = scale;
	}

	public double getTileScale() {
		return tileTextureScale;
	}

}
