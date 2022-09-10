package com.github.cm360.pixadv.core.graphics.gui.input;

import com.badlogic.gdx.Input.Keys;
import com.github.cm360.pixadv.core.network.endpoints.Client;

public class FunctionInputProcessor extends AbstractInputProcessor {

	private Client client;
	
	public FunctionInputProcessor(Client client) {
		super();
		this.client = client;
	}
	
	@Override
	public boolean keyDown(int keycode) {
		switch(keycode) {
		// Toggle UI
		case Keys.F1:
			client.getRenderingEngine().showUI = !client.getRenderingEngine().showUI;
			break;
		// Take screenshot
		case Keys.F2:
			client.getApplication().takeScreenshot();
			break;
		// Toggle fullscreen
		case Keys.F11:
			client.getApplication().setFullscreen(!client.getApplication().isFullscreen());
			break;
		// Toggle debug UI
		case Keys.F12:
			client.getRenderingEngine().showDebugMenu = !client.getRenderingEngine().showDebugMenu;
			break;
		default:
			return false;
		}
		return false;
	}
	
}
