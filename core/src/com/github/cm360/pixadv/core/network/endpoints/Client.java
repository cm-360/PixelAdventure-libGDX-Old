package com.github.cm360.pixadv.core.network.endpoints;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.github.cm360.pixadv.core.ClientApplication;
import com.github.cm360.pixadv.core.config.Config;
import com.github.cm360.pixadv.core.config.ConfigProvider;
import com.github.cm360.pixadv.core.graphics.gui.GuiManager;
import com.github.cm360.pixadv.core.graphics.gui.input.FunctionInputProcessor;
import com.github.cm360.pixadv.core.graphics.gui.input.GuiInputProcessor;
import com.github.cm360.pixadv.core.graphics.gui.input.MovementInputProcessor;
import com.github.cm360.pixadv.core.graphics.picasso.Picasso;
import com.github.cm360.pixadv.core.network.Connection;
import com.github.cm360.pixadv.core.network.packets.authentication.LoginAttemptPacket;
import com.github.cm360.pixadv.core.network.packets.authentication.LoginResponsePacket;
import com.github.cm360.pixadv.core.network.packets.universe.UniverseInfoPacket;
import com.github.cm360.pixadv.core.registry.Registry;
import com.github.cm360.pixadv.core.sound.beethoven.Beethoven;
import com.github.cm360.pixadv.core.util.Logger;
import com.github.cm360.pixadv.core.util.tasks.Task;
import com.github.cm360.pixadv.core.world.storage.universe.LocalUniverse;
import com.github.cm360.pixadv.core.world.storage.universe.RemoteUniverse;
import com.github.cm360.pixadv.core.world.storage.universe.Universe;

public class Client implements ConfigProvider {

	private final ClientApplication app;
	private final Registry registry;
	private final ExecutorService executor;
	
	private final InputProcessor inputProcessor;

	private final Picasso picasso;
	private final Beethoven beethoven;
	private final GuiManager guiManager;
	
	private Universe loadedUniverse;
	private UUID playerId;
	private UUID cameraFollowedId;
	private Set<UUID> controlledIds;
	private boolean paused;
	
	public Client(ClientApplication app, Registry registry) throws Exception {
		this.app = app;
		this.registry = registry;
		// Task executor and GUI manager
		executor = Executors.newFixedThreadPool(5);
		guiManager = new GuiManager();
		// Create input processors
		InputMultiplexer inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(new FunctionInputProcessor(this));
		inputMultiplexer.addProcessor(new GuiInputProcessor(this));
		inputMultiplexer.addProcessor(new MovementInputProcessor(this));
		inputProcessor = inputMultiplexer;
		// Create game engines
		picasso = new Picasso(this);
		beethoven = new Beethoven(this);
		// Game state variables
		playerId = UUID.randomUUID();
		cameraFollowedId = playerId;
		controlledIds = new HashSet<UUID>();
		controlledIds.add(playerId);
		paused = false;
	}
	
	/**
	 * Loads a local universe from the specified directory
	 * @param directory The directory to load from.
	 * @return True if the universe was loaded successfully, false otherwise.
	 */
	public boolean load(File directory) {
		try {
			LocalUniverse newUniverse = new LocalUniverse(registry, directory, this::getPlayerId);
			loadedUniverse = newUniverse;
			return newUniverse.load();
		} catch (Exception e) {
			Logger.logException("Failed to load to '%s'", e, directory);
		}
		return false;
	}
	
	/**
	 * Connects to a universe at the specified address.
	 * @param directory The address to connect to.
	 * @return True if the universe was connected successfully, false otherwise.
	 */
	public boolean connect(String address, int port) {
		Logger.logMessage(Logger.INFO, "Connecting to '%s:%d'...", address, port);
		try {
			// Connect to remote server
			Connection connection = new Connection(new Socket(address, port));
			// Perform handshake
			Logger.logMessage(Logger.DEBUG, "Performing handshake...");
			connection.send(new LoginAttemptPacket("Player1", "password1"));
			LoginResponsePacket loginResponse = (LoginResponsePacket) connection.await(LoginResponsePacket.class, 10000);
			if (loginResponse == null) {
				Logger.logMessage(Logger.ERROR, "Handshake response timeout!");
			} else {
				Logger.logMessage(Logger.DEBUG, "Handshake successful!");
				//
				UniverseInfoPacket universeInfo = (UniverseInfoPacket) connection.await(UniverseInfoPacket.class, 10000);
				if (universeInfo == null) {
					Logger.logMessage(Logger.ERROR, "Universe information response timeout!");
				} else {
					loadedUniverse = new RemoteUniverse(registry, connection, universeInfo, this::getPlayerId);
					return true;
				}
			}
			//connection.send(new TestPacket("hello from client 1\0com.github.cm360.pixadv.network.packets.TestPacket;inject good"));
			//connection.send(new TestPacket("hello from client 2\0com.github.cm360.pixadv.network.packets.FakePacket;inject bad"));
		} catch (IOException e) {
			String message = String.format("Failed to connect to '%s:%d'", address, port);
			Logger.logException(message, e);
		}
		closeUniverse();
		return false;
	}
	
	public ClientApplication getApplication() {
		return app;
	}
	
	public Registry getRegistry() {
		return registry;
	}
	
	public Executor getExecutor() {
		return executor;
	}
	
	public Config getConfig() {
		// TODO get config
		return null;
	}
	
	public InputProcessor getInputProcessor() {
		return inputProcessor;
	}
	
	public Picasso getRenderingEngine() {
		return picasso;
	}
	
	public Beethoven getSoundEngine() {
		return beethoven;
	}
	
	public GuiManager getGuiManager() {
		return guiManager;
	}
	
	public void addTask(Task task) {
		executor.submit(task::process);
	}
	
	public void addTask(Runnable task) {
		executor.submit(task);
	}
	
	public Universe getCurrentUniverse() {
		return loadedUniverse;
	}
	
	public UUID getPlayerId() {
		return playerId;
	}
	
	public UUID getCameraFollowedId() {
		return cameraFollowedId;
	}
	
	
	public Set<UUID> getControlledIds() {
		return Set.copyOf(controlledIds);
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	public void setPaused(boolean paused) {
		this.paused = paused;
		notifyAll();
	}
	
	public void closeUniverse() {
		picasso.getWorldPainter().clearCache();
		if (loadedUniverse != null) {
			try {
				// Close current universe
				loadedUniverse.close();
			} catch (Exception e) {
				Logger.logException("Failed to safely close universe '%s'!", e, loadedUniverse.getName());
			}
			loadedUniverse = null;
		}
		System.gc();
	}

}
