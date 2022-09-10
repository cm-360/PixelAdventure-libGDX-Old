package com.github.cm360.pixadv.core;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.cm360.pixadv.core.builtin.pixadv.java.gui.menus.StartMenu;
import com.github.cm360.pixadv.core.graphics.picasso.Precompute;
import com.github.cm360.pixadv.core.network.endpoints.Client;
import com.github.cm360.pixadv.core.registry.Identifier;
import com.github.cm360.pixadv.core.registry.Registry;
import com.github.cm360.pixadv.core.util.Logger;
import com.github.cm360.pixadv.core.util.Stopwatch;
import com.github.cm360.pixadv.core.world.storage.world.World;

public class ClientApplication extends ApplicationAdapter {

	private final Client client;
	
	private int viewportWidth;
	private int viewportHeight;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private List<Disposable> trash;
	
	private File screenshotsDir;
	private DateTimeFormatter screenshotNameFormatter;
	
	public ClientApplication(File rootDirectory, ClassLoader parentClassLoader) throws Exception {
		// Client object
		client = new Client(this, new Registry(rootDirectory, parentClassLoader));
		// Rendering things
		trash = new ArrayList<Disposable>();
	}

	@Override
	public void create() {
		// Screenshot directories
		screenshotsDir = new File(Gdx.files.getLocalStoragePath(), "screenshots");
		screenshotNameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.SSS");
		// Initialize registry
		try {
			client.getRegistry().initialize();
			//
			Gdx.input.setInputProcessor(client.getInputProcessor());
			// Open start menu
			client.getGuiManager().closeAllMenus();
			client.getGuiManager().openMenu(new StartMenu(client));
		} catch (Exception e) {
			Logger.logException("", e);
			dispose();
		}
		// Create camera
		camera = new OrthographicCamera();
		batch = new SpriteBatch();
//		img = new Texture("textures/gui/icon.png");
//		new Texture(new Pixmap(Gdx2DPixmap.newPixmap(0, 0, Gdx2DPixmap.GDX2D_FORMAT_RGBA8888)));
	}

	@Override
	public void render() {
		// Clear screen
		ScreenUtils.clear(0f, 0f, 0f, 1f);
		// Set camera projection matrix
		camera.setToOrtho(false, viewportWidth, viewportHeight);
		batch.setProjectionMatrix(camera.combined);
		// Render frame
		batch.begin();
		if (client.getRegistry().isInitialized()) {
			batch.draw(client.getRegistry().getTexture(Identifier.parse("pixadv:textures/gui/icon")), 10, 10);
			// Draw!
			World world = null;
			if (client.getCurrentUniverse() != null)
				world = client.getCurrentUniverse().getCurrentWorld();
			Stopwatch renderTimes = new Stopwatch();
			Precompute precomp = new Precompute(new Rectangle(0, 0, viewportWidth, viewportHeight));
			client.getRenderingEngine().paint(batch, client.getRegistry(), trash, world, precomp, null, renderTimes);
		} else {
			// Draw loading registry message
		}
		// Cleanup
		batch.end();
		trash.forEach(Disposable::dispose);
	}
	
	@Override
	public void resize(int width, int height) {
		this.viewportWidth = width;
		this.viewportHeight = height;
		client.getRenderingEngine().autoScale(width, height);
        Gdx.gl.glViewport(0, 0, width, height);
	}
	
	public int getViewportWidth() {
		return viewportWidth;
	}
	
	public int getViewportHeight() {
		return viewportHeight;
	}
	
	public void setFullscreen(boolean fullscreen) {
		if (fullscreen) {
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		} else {
			Gdx.graphics.setWindowedMode(800, 500);
		}
	}
	
	public boolean isFullscreen() {
		return Gdx.graphics.isFullscreen();
	}
	
	public void takeScreenshot() {
		Gdx.app.postRunnable(() -> {
			try {
				Pixmap screenshotPixmap = Pixmap.createFromFrameBuffer(0, 0, 
						Gdx.graphics.getBackBufferWidth(),
						Gdx.graphics.getBackBufferHeight());
				String screenshotFilePath = screenshotsDir.getPath() + File.separator
						+ LocalDateTime.now().format(screenshotNameFormatter) + ".png";
				PixmapIO.writePNG(
						Gdx.files.external(screenshotFilePath),
						screenshotPixmap, Deflater.DEFAULT_COMPRESSION, true);
				screenshotPixmap.dispose();
				Logger.logMessage(Logger.INFO, "Saved screenshot as %s", Gdx.files.external(screenshotFilePath).file().getCanonicalFile());
			} catch (Exception e) {
				Logger.logException("Exception while saving screenshot!", e);
			}
		});
	}

	@Override
	public void dispose() {
		batch.dispose();
		if (client.getRegistry().isInitialized())
			client.getRegistry().dispose();
	}
	
	public Client getClient() {
		return client;
	}

}
