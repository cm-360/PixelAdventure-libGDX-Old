package com.github.cm360.pixadv.core.graphics.picasso;

import com.badlogic.gdx.math.Rectangle;
import com.github.cm360.pixadv.core.world.storage.world.World;

public class Precompute {

	private Rectangle viewportBounds;
	private Rectangle camBounds;
	private int centerX;
	private int centerY;
	private int minX;
	private int minY;
	private int maxX;
	private int maxY;
	private double scaledTileTextureSize;
	
	public Precompute(Rectangle screenBounds) {
		this.viewportBounds = screenBounds;
		camBounds = screenBounds;
	}
	
	public void update(World world, Picasso picasso) {
		scaledTileTextureSize = picasso.getWorldPainter().getTileTextureSize() * picasso.getWorldPainter().getTileScale();
		centerX = (int) (camBounds.x + (camBounds.width / 2) - scaledTileTextureSize / 2);
		centerY = (int) (camBounds.y + (camBounds.height / 2) - scaledTileTextureSize / 2);
		minX = (int) Math.round(((world.getCameraX() * scaledTileTextureSize - camBounds.width / 2)) / scaledTileTextureSize - 0.05);
		minY = (int) Math.round(((world.getCameraY() * scaledTileTextureSize - camBounds.height / 2)) / scaledTileTextureSize - 0.05);
		maxX = (int) Math.round(((world.getCameraX() * scaledTileTextureSize + camBounds.width / 2)) / scaledTileTextureSize + 1.05);
		maxY = (int) Math.round(((world.getCameraY() * scaledTileTextureSize + camBounds.height / 2)) / scaledTileTextureSize + 1.05);
	}
	
	public void updateCamBounds(int border) {
		if (border > 0) {
			camBounds = new Rectangle(
					viewportBounds.x + border,
					viewportBounds.y + border,
					viewportBounds.width - 2 * border,
					viewportBounds.height - 2 * border);
		} else {
			camBounds = viewportBounds;
		}
	}
	
	public Rectangle getViewportBounds() {
		return viewportBounds;
	}

	public Rectangle getCamBounds() {
		return camBounds;
	}

	public int getCenterX() {
		return centerX;
	}

	public int getCenterY() {
		return centerY;
	}

	public int getMinX() {
		return minX;
	}

	public int getMinY() {
		return minY;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	public double getScaledTileTextureSize() {
		return scaledTileTextureSize;
	}
	
}
