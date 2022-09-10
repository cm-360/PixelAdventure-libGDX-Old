package com.github.cm360.pixadv.core.graphics.picasso.painters;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.cm360.pixadv.core.graphics.gui.layouts.GuiLayer;
import com.github.cm360.pixadv.core.graphics.picasso.Picasso;
import com.github.cm360.pixadv.core.graphics.picasso.Precompute;
import com.github.cm360.pixadv.core.graphics.picasso.RenderStats;
import com.github.cm360.pixadv.core.util.Stopwatch;
import com.github.cm360.pixadv.core.world.storage.universe.Universe;
import com.github.cm360.pixadv.core.world.storage.world.World;

public class DebugPainter {

	private Picasso parent;
	
	private Map<String, Color> debugPieColors;
	
	public DebugPainter(Picasso parent) {
		this.parent = parent;
		// Debug pie chart colors
		debugPieColors = new HashMap<String, Color>();
		debugPieColors.put("precompute", Color.YELLOW);
		debugPieColors.put("clear", Color.RED);
		debugPieColors.put("entity-positions", new Color(0, 127, 127));
		debugPieColors.put("background", new Color(64, 64, 255));
		debugPieColors.put("chunkmap", new Color(0, 192, 0));
		debugPieColors.put("entities", Color.ORANGE);
		debugPieColors.put("mouse", Color.GRAY);
		debugPieColors.put("ui-layers", Color.PINK);
		debugPieColors.put("ui-menu", Color.MAGENTA);
		debugPieColors.put("overlay", Color.CYAN);
	}
	
	protected void paintDebugOverlay(SpriteBatch b, Universe universe, Precompute precomp, Stopwatch renderTimes, RenderStats stats) {
		paintCameraBounds(g, universe, precomp);
		// Left anchored text lines
		List<String> leftLines = new ArrayList<String>();
		leftLines.add(Gdx.graphics.getFramesPerSecond() + " FPS");
		if (universe == null) {
			leftLines.add("No universe loaded");
		} else {
			World world = universe.getCurrentWorld();
			if (world == null) {
				leftLines.add("Loading world...");
			} else {
				leftLines.add("World: '%s'".formatted(world.getName()));
				Point minChunk = world.getChunkOf(precomp.getMinX(), precomp.getMinY());
				Point maxChunk = world.getChunkOf(precomp.getMaxX(), precomp.getMaxY());
				leftLines.add("Tiles: (x%d,y%d)-(x%d,y%d)".formatted(
						precomp.getMinX(),
						precomp.getMinY(),
						precomp.getMaxX(),
						precomp.getMaxY()));
				leftLines.add("Chunks: (%d,%d)-(%d,%d)".formatted(
						minChunk.x,
						minChunk.y,
						maxChunk.x,
						maxChunk.y));
				leftLines.add("Entities: %d/%d".formatted(
						stats.getUniqueEntities(),
						stats.getTotalEntities()));
			}
		}
		// Right anchored text lines
		List<String> rightLines = new ArrayList<String>();
		rightLines.add("Heap: %d/%dMB".formatted(
				Runtime.getRuntime().totalMemory() / 1048576,
				Runtime.getRuntime().maxMemory() / 1048576));
		rightLines.add("Chunk Cache: " + parent.getWorldPainter().getCacheSize());
		rightLines.add("Modules: %d".formatted(parent.getClient().getRegistry().getModulesList().size()));
		// Debug info bar
		g.setFont(parent.getDefaultFont().deriveFont(16f));
		FontMetrics gfm = g.getFontMetrics();
		g.setColor(new Color(255, 255, 255, 127));
		g.fillRect(0, 0, precomp.getViewportBounds().width, (gfm.getHeight() * Math.max(leftLines.size(), rightLines.size())) + 5);
		g.setColor(Color.BLACK);
		// Draw left anchored info lines
		for (int i = 0; i < leftLines.size(); i++) {
			String line = leftLines.get(i);
			if (!line.isBlank()) {
				g.drawString(line,
						5,
						gfm.getHeight() * (i + 1));
			}
		}
		// Draw right anchored info lines
		for (int i = 0; i < rightLines.size(); i++) {
			String line = rightLines.get(i);
			if (!line.isBlank()) {
				g.drawString(line,
						(precomp.getViewportBounds().width - (gfm.stringWidth(line) + 5)),
						gfm.getHeight() * (i + 1));
			}
		}
		// Exceptions while rendering
		long lastExceptionTime = parent.getGuiPainter().getLastExceptionTime();
		String lastExceptionText = parent.getGuiPainter().getLastExceptionText();
		if (lastExceptionTime != -1 && System.currentTimeMillis() - lastExceptionTime < 15000) {
			g.clearRect(0, precomp.getViewportBounds().height - 20, precomp.getViewportBounds().width, 20);
			g.setColor(Color.RED);
			g.drawString(lastExceptionText, 5, precomp.getViewportBounds().height - 5);
		}
//		paintTickChart(g, precomp.getGBounds());
		renderTimes.mark("overlay");
		paintDebugPie(b, precomp.getViewportBounds(), renderTimes);
	}
	
	protected void paintCameraBounds(Graphics g, Universe universe, Precompute precomp) {
		if (universe != null) {
			World world = universe.getCurrentWorld();
			if (world != null) {
				// Camera position and bounding box
				g.setColor(Color.WHITE);
				g.drawRect(
						precomp.getCamBounds().x + (precomp.getCamBounds().width / 2) - 1,
						precomp.getCamBounds().y + (precomp.getCamBounds().height / 2) - 1,
						2, 2);
				if (!precomp.getCamBounds().equals(precomp.getViewportBounds())) {
					g.drawRect(
							precomp.getCamBounds().x,
							precomp.getCamBounds().y,
							precomp.getCamBounds().width,
							precomp.getCamBounds().height);
				}
			}
		}
	}
	
	protected void paintDebugPie(Graphics g, Rectangle gBounds, Stopwatch renderTimes) {
		double totalRenderTime = renderTimes.getTotalDuration();
		// Pie chart variables
		int padding = 10;
		int size = (int) Math.round(100 * GuiLayer.scale);
		int border = 2;
		int angle = 0;
		int line = 0;
		// Render pie chart border
		g.setColor(Color.WHITE);
		g.fillArc(padding, gBounds.height - (padding + size), size, size, 0, 360);
		// Render pie chart slices
		g.setFont(new Font("Consolas", Font.BOLD, 16));
		FontMetrics gfm = g.getFontMetrics();
		int pieKeyHeight = Math.max(renderTimes.getTimes().size() * gfm.getHeight(), size + padding);
		for (Entry<String, Long> entry : renderTimes.getTimes()) {
			double percent = entry.getValue() / totalRenderTime;
			int angleDelta = (int) Math.round(360 * percent);
			if (debugPieColors.containsKey(entry.getKey())) {
				g.setColor(debugPieColors.get(entry.getKey()));
			} else {
				int color = new Random(entry.getKey().hashCode()).nextInt(255 * 255 * 255);
				g.setColor(new Color(color));
			}
			g.fillArc(
					padding + border,
					gBounds.height - (size + padding - border),
					size - (2 * border),
					size - (2 * border),
					angle, angleDelta);
			g.drawString("%7.3f%% %9dns  %s".formatted(
					percent * 100,
					entry.getValue(),
					entry.getKey()),
					(size + padding) + 20,
					(gBounds.height - pieKeyHeight + padding) + (gfm.getHeight() * line));
			angle += angleDelta;
			line++;
		}
	}
	
	protected void paintTickChart(Graphics g, Rectangle vBounds) {
		Universe universe = parent.getClient().getCurrentUniverse();
		int width = 3;
		int baseHeight = 20;
		if (universe != null) {
			for (String worldName : universe.getWorldNames()) {
				Long[] physicsTickTimes = universe.getWorld(worldName).getPhysicsTickTimes();
				// Calculate tick average
				double average = 0;
				for (Long tick : physicsTickTimes)
					average += tick;
				average /= physicsTickTimes.length;
				// Draw a bar for each tick
				for (int i = 0; i < physicsTickTimes.length; i++) {
					long tick = physicsTickTimes[i];
					double percent = tick / average;
					int height = (int) Math.round(baseHeight * percent);
					g.setColor(new Color(
							Math.max(Math.min((int) Math.round(255 * percent), 255), 0),
							Math.max(Math.min((int) Math.round(255 * (1 / percent)), 255), 0),
							0));
					g.fillRect(
							vBounds.width - (i * width),
							vBounds.height - height,
							width,
							height);
				}
			}
		}
	}

}
