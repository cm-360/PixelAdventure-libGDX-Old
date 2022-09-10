package com.github.cm360.pixadv.desktop;

import java.io.File;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.github.cm360.pixadv.core.ClientApplication;
import com.github.cm360.pixadv.core.util.Logger;

public class DesktopLauncher {

	public static void main(String[] args) {
		try {
			// Configure app window
			LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
			config.title = "A Pixel Adventure";
			config.addIcon("assets/textures/gui/icon.png", FileType.Internal);
			config.width = 800;
			config.height = 500;
			// Start app
			new LwjglApplication(new ClientApplication(
					new File(DesktopLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()),
					DesktopLauncher.class.getClassLoader()), config);
		} catch (Exception e) {
			Logger.logException("Uncaught exception!", e);
		}
	}

}
