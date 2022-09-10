package com.github.cm360.pixadv.core.graphics.swing.components;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.Timer;

import com.badlogic.gdx.Gdx;
import com.github.cm360.pixadv.core.builtin.pixadv.java.entities.capabilities.ControllableEntity.Input;
import com.github.cm360.pixadv.core.builtin.pixadv.java.gui.huds.ChatHud;
import com.github.cm360.pixadv.core.builtin.pixadv.java.gui.menus.StartMenu;
import com.github.cm360.pixadv.core.builtin.pixadv.java.tiles.types.terra.Dirt;
import com.github.cm360.pixadv.core.graphics.gui.GuiComponent;
import com.github.cm360.pixadv.core.graphics.gui.input.OldInputProcessor;
import com.github.cm360.pixadv.core.graphics.gui.input.KeyCombo;
import com.github.cm360.pixadv.core.graphics.gui.layouts.GuiLayer;
import com.github.cm360.pixadv.core.graphics.gui.layouts.GuiMenu;
import com.github.cm360.pixadv.core.graphics.picasso.Picasso;
import com.github.cm360.pixadv.core.graphics.picasso.Precompute;
import com.github.cm360.pixadv.core.graphics.picasso.RenderStats;
import com.github.cm360.pixadv.core.main.PixelAdventure;
import com.github.cm360.pixadv.core.network.endpoints.Client;
import com.github.cm360.pixadv.core.registry.Identifier;
import com.github.cm360.pixadv.core.registry.Registry;
import com.github.cm360.pixadv.core.util.Logger;
import com.github.cm360.pixadv.core.util.Stopwatch;
import com.github.cm360.pixadv.core.world.storage.universe.Universe;
import com.github.cm360.pixadv.core.world.storage.world.World;
import com.github.cm360.pixadv.core.world.types.tiles.Tile;

public class GameCanvas extends JComponent {

	private static final long serialVersionUID = 1L;
	
	private final Client client;
	
	
	
	
	
	private double cameraXOld, cameraYOld;
	
	
	private Precompute lastPrecomp;
	
	private int fps;
	private int frames;
	private long framesResetTime;
	private int frameCap = 0;
	
	
	
	private List<String> chatMessageHistory;
	private List<String> chatSentHistory;

	public GameCanvas(Client client) {
		this.client = client;
		
		// Chat
		chatMessageHistory = new ArrayList<String>();
		chatSentHistory = new ArrayList<String>();
		
		
		// Repaint loop
		new Timer(0, event -> repaint()).start();
		// New thread to wait for registry to be built
		new Thread(() -> {
			try {
				Registry registry = client.getRegistry();
				synchronized (registry) {
					if (!registry.isInitialized())
						registry.wait();
					// Set default font
					Font font = client.getRegistry().getFont(Identifier.parse("pixadv:fonts/Style-7/PixelFont7"));
					if (font != null)
						defaultFont = font;
					// Finish loading game panel
					EventQueue.invokeLater(() -> {
						postRegistryInit();
					});
				}
			} catch (InterruptedException e) {
				Logger.logException("Interrupted!", e);
			}
		}, "GamePanel-PostRegistry").start();
	}
	
	private void postRegistryInit() {
		// For access inside lambda expressions
		GameCanvas self = this;
		// Open start menu
		menuStack.clear();
		menuStack.push(new StartMenu(client));
		// Mouse events
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				// Save current mouse info
				mouseClickOrigin = arg0.getPoint();
				mouseLocation = mouseClickOrigin;
				Universe universe = client.getCurrentUniverse();
				if (universe != null) {
					World world = universe.getCurrentWorld();
					if (world != null) {
						cameraXOld = world.getCameraX();
						cameraYOld = world.getCameraY();
					}
				}
				// Process gui interaction
				GuiLayer gui = getTopGui();
				if (gui == null) {
					if (arg0.getButton() == 3)
						interact();
				} else {
					// Attempt to focus component
					OldInputProcessor inputProcessor = gui.getInputProcessor();
					inputProcessor.mousePressed(mouseLocation, new KeyCombo(arg0.getButton(), getPressedKeys()));
					GuiComponent focused = inputProcessor.getFocusedComponent();
					if (focused == null && arg0.getButton() == 3)
						interact();
				}
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// Save current mouse info
				mouseLocation = arg0.getPoint();
				// Process gui interaction
				GuiLayer gui = getTopGui();
				if (gui != null)
					gui.getInputProcessor().mouseReleased(mouseLocation, new KeyCombo(arg0.getButton(), getPressedKeys()));
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent arg0) {
				// Save current mouse info
				mouseLocation = arg0.getPoint();
				// Process gui interaction
				GuiLayer gui = getTopGui();
				if (gui != null)
					gui.getInputProcessor().mouseMoved(mouseLocation, new KeyCombo(-1, getPressedKeys()));
				if (getCurrentMenu() == null) {
					updateControlledInputs();
				}
			}
			@Override
			public void mouseDragged(MouseEvent arg0) {
				// Save current mouse info
				mouseLocation = arg0.getPoint();
				// Process gui interaction
				GuiLayer gui = getTopGui();
				if (gui != null)
					gui.getInputProcessor().mouseMoved(mouseLocation, new KeyCombo(arg0.getButton(), getPressedKeys()));
				// Scroll camera
				Universe universe = client.getCurrentUniverse();
				if (universe != null) {
					World world = universe.getCurrentWorld();
					if (world != null) {
						Picasso picasso = client.getRenderingEngine();
						world.setCameraX(cameraXOld + ((mouseClickOrigin.getX() - mouseLocation.getX()) / (picasso.getTileTextureSize() * picasso.getTileScale())));
						world.setCameraY(cameraYOld + ((mouseLocation.getY() - mouseClickOrigin.getY()) / (picasso.getTileTextureSize() * picasso.getTileScale())));
					}
				}
			}
		});
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent arg0) {
				Picasso picasso = client.getRenderingEngine();
				double scaledTextureSize = picasso.getTileTextureSize() * picasso.getTileScale();
				if (arg0.getWheelRotation() > 0 && scaledTextureSize > 16) {
					picasso.setTileScale(picasso.getTileScale() - 0.25);
				} else if (arg0.getWheelRotation() < 0 && scaledTextureSize < 32 * 5) {
					picasso.setTileScale(picasso.getTileScale() + 0.25);
				}
			}
		});
		// Key events
		setFocusable(true);
		addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				synchronized (pressedKeys) {
					GuiMenu menu = getCurrentMenu();
					switch (arg0.getKeyCode()) {
					// Open pause menu
					case KeyEvent.VK_ESCAPE:
						if (menu != null) {
							if (!(client.getCurrentUniverse() == null && menu instanceof StartMenu && menuStack.size() == 1))
								closeMenu();
						} else {
							client.closeUniverse();
							openMenu(new StartMenu(client));
						}
						break;
					// Toggle UI
					case KeyEvent.VK_F1:
						showUI = !showUI;
						break;
					// Screenshot
					case KeyEvent.VK_F2:
						Gdx.app.postRunnable(() -> {
							takeScreenshot(mouseLocation);
						});
						break;
					// Toggle fullscreen
					case KeyEvent.VK_F11:
						client.toggleFullscreen(true);
						break;
					// Toggle debug menu
					case KeyEvent.VK_F12:
						showDebugMenu = !showDebugMenu;
						break;
					// Normal key press
					default:
						pressedKeys.add(arg0.getKeyCode());
						if (menu == null) {
							updateControlledInputs();
							// Handle additional special keypresses
							switch (arg0.getKeyCode()) {
							// Open chat UI
							case KeyEvent.VK_T:
								if (!(getCurrentMenu() instanceof ChatHud))
									openMenu(new ChatHud(client, self::closeMenu, chatMessageHistory, chatSentHistory));
								break;
							}
						} else {
							Set<Integer> keyWithModifiers = new HashSet<Integer>();
							keyWithModifiers.add(arg0.getKeyCode());
							keyWithModifiers.addAll(getPressedKeys().stream().filter(Character::isISOControl).toList());
							menu.interactKey(new KeyCombo(-1, keyWithModifiers));
						}
					}
				}
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				synchronized (pressedKeys) {
					pressedKeys.remove(arg0.getKeyCode());
					if (getCurrentMenu() == null) {
						updateControlledInputs();
					}
				}
			}
			@Override
			public void keyTyped(KeyEvent arg0) {
				// Do
			}
		});
	}
	
	private void updateControlledInputs() {
		Universe universe = client.getCurrentUniverse();
		if (universe != null) {
			World world = universe.getCurrentWorld();
			if (world != null) {
				Set<Input> inputDirections = getPressedKeys().stream()
						.map(keyCode -> inputMappings.get(keyCode))
						.collect(Collectors.toSet());
				world.getPhysicsEngine().updateControlInputs(
						client.getControlledIds(),
						inputDirections,
						mouseLocation);
			}
		}
	}
	
	private void interact() {
		client.getSoundEngine().playSound(Identifier.parse("pixadv:sounds/chop"));
		// Process click as block interaction instead
		Universe universe = client.getCurrentUniverse();
		if (universe != null) {
			World world = universe.getCurrentWorld();
			Picasso picasso = client.getRenderingEngine();
			Point rawPoint = picasso.getMouseTile(world, lastPrecomp, mouseLocation);
			Point correctedPoint = world.correctCoord(rawPoint.x, rawPoint.y);
			if (new Rectangle(world.getWidth() * world.getChunkSize(), world.getHeight() * world.getChunkSize()).contains(correctedPoint)) {
				Tile tile = world.getTile(correctedPoint.x, correctedPoint.y, 2);
				if (tile == null)
					world.setTile(new Dirt(), correctedPoint.x, correctedPoint.y, 2);
				else
					world.setTile(null, correctedPoint.x, correctedPoint.y, 2);
			}
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		try {
			Stopwatch renderTimes = new Stopwatch();
			RenderStats stats = null;
			lastPrecomp = new Precompute(g);
			renderTimes.mark("precompute");
			// Clear screen and set default mode
//			g.clearRect(0, 0, lastPrecomp.getGBounds().width, lastPrecomp.getGBounds().height);
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, lastPrecomp.getViewportBounds().width, lastPrecomp.getViewportBounds().height);
			renderTimes.mark("clear");
			g.setColor(Color.WHITE);
			g.setFont(getDefaultFont().deriveFont(20f));
			FontMetrics gfm = g.getFontMetrics();
			if (client.getRegistry().isInitialized()) {
				// Render current universe
				Universe universe = client.getCurrentUniverse();
				if (universe != null) {
					stats = client.getRenderingEngine().paint(g, universe.getCurrentWorld(), lastPrecomp, mouseLocation, renderTimes);
				} else {
					
				}
				if (showUI) {
					autoScale(lastPrecomp.getViewportBounds().width, lastPrecomp.getViewportBounds().height);
					// Render current GUI layers
					if (!guiLayers.isEmpty()) {
						// Draw all GUI layers
						for (GuiLayer layout : guiLayers) {
							layout.updateBounds(lastPrecomp.getViewportBounds());
							layout.paint(g, client.getRegistry());
						}
						renderTimes.mark("ui-layers");
					}
					// Draw current menu if any
					if (!menuStack.isEmpty()) {
						GuiMenu menu = getCurrentMenu();
						menu.updateBounds(lastPrecomp.getViewportBounds());
						menu.paint(g, client.getRegistry());
						renderTimes.mark("ui-menu");
					}
					if (menuStack.isEmpty() && guiLayers.isEmpty() && universe == null) {
						g.drawString("No content loaded", 5, gfm.getHeight());
					}
					paintOverlay(g, universe, lastPrecomp, renderTimes, stats);
				}
			} else {
				// Registry is not finished loading
				g.drawString("Building registry...", 5, gfm.getHeight());
			}
			// Delay next frame as to not pass the fps cap
			long frameTime = renderTimes.getTotalDuration();
			if (frameCap > 0) {
				int frameExpectedTime = 1000000000 / frameCap;
				if (frameTime < frameExpectedTime) {
					try {
						Thread.sleep((frameExpectedTime - frameTime) / 1000000);
					} catch (InterruptedException e) {
						Logger.logException("Interrupted!", e);
					}
				}
			}
			// Calculate FPS
			long frameFullTime = System.nanoTime();
			if (frameFullTime > framesResetTime + 1000000000) {
				fps = frames;
				frames = 0;
				framesResetTime = frameFullTime;
			}
			frames++;
		} catch (Exception e) {
			Logger.logException("Uncaught exception while rendering!", e);
			setLastExceptionInfo(e.getMessage(), System.nanoTime());
			g.setColor(Color.RED);
			g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
			g.drawString("%s: %s".formatted(e.getClass().getName(), e.getMessage()), 5, 15);
		}
	}
	
	protected void paintOverlay(Graphics g, Universe universe, Precompute precomp, Stopwatch renderTimes, RenderStats stats) {
		// Draw general debug info
		if (showDebugMenu) {
			paintDebugOverlay(g, universe, precomp, renderTimes, stats);
		} else {
			if (showFps) {
				// Draw FPS in corner
				g.setFont(client.getGamePanel().getDefaultFont().deriveFont(16f));
				FontMetrics gfm = g.getFontMetrics();
				g.setColor(Color.WHITE);
				g.drawString(client.getGamePanel().getFps() + " FPS", 5, gfm.getHeight());
			}
		}
	}
	
	
	
	public boolean takeScreenshot(Point mouseLocation) {
		return takeScreenshot(client.getGamePanel().getWidth(), client.getGamePanel().getHeight(), mouseLocation);
	}
	
	public boolean takeScreenshot(int width, int height, Point mouseLocation) {
		boolean success = false;
		BufferedImage screenshotImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = screenshotImage.createGraphics();
		g.setClip(0, 0, width, height);
		paintComponent(g);
		try {
			screenshotsDir.mkdirs();
			File screenshotFile = new File(screenshotsDir, "%s.png".formatted(LocalDateTime.now().format(screenshotNameFormatter)));
			success = ImageIO.write(screenshotImage, "PNG", screenshotFile);
			if (success)
				Logger.logMessage(Logger.INFO, "Screenshot saved as '%s'", screenshotFile.getName());
			else
				Logger.logMessage(Logger.ERROR, "Failed to save screenshot!");
		} catch (IOException e) {
			Logger.logException("Failed to save screenshot!", e);
		}
		return success;
	}
	
	public Set<Integer> getPressedKeys() {
		synchronized (pressedKeys) {
			return Set.copyOf(pressedKeys);
		}
	}
	
	private void clearInputs() {
		synchronized (pressedKeys) {
			pressedKeys.clear();
			updateControlledInputs();
		}
	}
	
	public Client getClient() {
		return client;
	}

}
