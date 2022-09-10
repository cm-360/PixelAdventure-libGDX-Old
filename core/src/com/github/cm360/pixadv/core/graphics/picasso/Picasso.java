package com.github.cm360.pixadv.core.graphics.picasso;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.github.cm360.pixadv.core.graphics.gui.layouts.GuiLayer;
import com.github.cm360.pixadv.core.graphics.picasso.painters.DebugPainter;
import com.github.cm360.pixadv.core.graphics.picasso.painters.GuiPainter;
import com.github.cm360.pixadv.core.graphics.picasso.painters.WorldPainter;
import com.github.cm360.pixadv.core.network.endpoints.Client;
import com.github.cm360.pixadv.core.registry.Registry;
import com.github.cm360.pixadv.core.util.Stopwatch;
import com.github.cm360.pixadv.core.world.storage.world.World;
import com.github.cm360.pixadv.core.world.types.entities.Entity;
import com.github.cm360.pixadv.core.world.types.tiles.Tile;

public class Picasso {

	private final Client client;
	
	private WorldPainter worldPainter;
	private GuiPainter guiPainter;
	private DebugPainter debugPainter;
	
	public boolean showUI = true;
	public boolean showFps = true;
	public boolean showDebugMenu = true;
	
	private BitmapFont defaultFont;
	
	
	// Constructor
	public Picasso(Client client) {
		this.client = client;
		// Painters
		worldPainter = new WorldPainter(this);
		guiPainter = new GuiPainter(client.getGuiManager());
		debugPainter = new DebugPainter(this);
		// 
		defaultFont = null; //new Font(Font.SANS_SERIF, Font.BOLD, 20);
	}
	
	// Main method
	public RenderStats paint(SpriteBatch b, Registry registry, List<Disposable> trash, World world, Precompute precomp, Point mouseLocation, Stopwatch renderTimes) {
		RenderStats stats = new RenderStats();
		if (world == null) {
			// Draw loading message
//			int shade = 64 + (int) (64 * (Math.sin(System.currentTimeMillis() / 500.0) + 1));
//			g.setColor(new Color(shade, shade, shade));
//			g.setFont(defaultFont.deriveFont(40f));
//			FontMetrics gfm = g.getFontMetrics();
//			String loadingMessage = "Loading world...";
//			g.drawString(loadingMessage,
//					(precomp.getViewportBounds().width - gfm.stringWidth(loadingMessage)) / 2,
//					(precomp.getViewportBounds().height + gfm.getAscent()) / 2);
//			renderTimes.mark("ui-loading");
		} else {
			
		}
		worldPainter.paint(b, registry, trash, world, precomp, renderTimes);
		guiPainter.paint(b, registry, trash, precomp);
		
		return stats;
	}

	protected void paintMouseHover(Graphics g, World world, Precompute precomp, Point mouseLocation, Point mouseTile) {
		if (mouseTile.y >= 0 && mouseTile.y < world.getHeight() * world.getChunkSize()) {
			// Tile outline
			int shade = 64 + (int) (64 * (Math.sin(System.currentTimeMillis() / 250.0) + 1));
			g.setColor(new Color(255, 255, 255, shade));
			g.drawRect(
					precomp.getCenterX() + (int) Math.round(precomp.getScaledTileTextureSize() * (mouseTile.x - world.getCameraX())),
					precomp.getCenterY() - (int) Math.round(precomp.getScaledTileTextureSize() * (mouseTile.y - world.getCameraY())),
					(int) precomp.getScaledTileTextureSize() - 1,
					(int) precomp.getScaledTileTextureSize() - 1);
			// Tile position
			if (showDebugMenu) {
				g.setColor(Color.WHITE);
				g.fillRect(mouseLocation.x, mouseLocation.y - 30, 150, 25);
				g.setColor(Color.BLACK);
				g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
				g.drawString(String.format("(x%d,y%d)", mouseTile.x, mouseTile.y), mouseLocation.x + 3, mouseLocation.y - 20);
				GridPoint2 correctedTileCoords = world.correctCoord(mouseTile.x, mouseTile.y);
				Tile tile0 = world.getTile(correctedTileCoords.x, correctedTileCoords.y, 0);
				Tile tile1 = world.getTile(correctedTileCoords.x, correctedTileCoords.y, 1);
				Tile tile2 = world.getTile(correctedTileCoords.x, correctedTileCoords.y, 2);
				g.drawString(String.format("%s, %s, %s",
						((tile0 == null) ? "air" : tile0.getID()),
						((tile1 == null) ? "air" : tile1.getID()),
						((tile2 == null) ? "air" : tile2.getID())),
						mouseLocation.x + 3, mouseLocation.y - 8);
			}
		}
	}
	
	public void autoScale(int viewportWidth, int viewportHeight) {
		int area = viewportWidth * viewportHeight;
		// Auto scale the menu
		if (area < 300000)
			GuiLayer.scale = 0.5f;
		else if (area >= 300000 && area < 500000)
			GuiLayer.scale = 1.0f;
		else if (area >= 500000 && area < 900000)
			GuiLayer.scale = 1.5f;
		else
			GuiLayer.scale = 2.0f;
	}
	
	// Utility methods
	public Vector2 getMouseTile(World world, Precompute precomp, int mouseX, int mouseY) {
		// Calculations
		int mouseTileX = (int) Math.round((double) (mouseX - (precomp.getCamBounds().x + precomp.getCamBounds().width / 2)) / precomp.getScaledTileTextureSize() + world.getCameraX());
		int mouseTileY = (int) Math.round((double) ((precomp.getCamBounds().y + precomp.getCamBounds().height / 2) - mouseY) / precomp.getScaledTileTextureSize() + world.getCameraY());
		// Return point
		return new Vector2(mouseTileX, mouseTileY);
	}
	
	public Client getClient() {
		return client;
	}
	
	public BitmapFont getDefaultFont() {
		return defaultFont;
	}
	
	public WorldPainter getWorldPainter() {
		return worldPainter;
	}
	
	public GuiPainter getGuiPainter() {
		return guiPainter;
	}
	
	public DebugPainter getDebugPainter() {
		return debugPainter;
	}

}
