package com.github.cm360.pixadv.core.graphics.gui.input;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.math.GridPoint2;
import com.github.cm360.pixadv.core.graphics.gui.GuiComponent;
import com.github.cm360.pixadv.core.graphics.gui.layouts.GuiMenu;
import com.github.cm360.pixadv.core.network.endpoints.Client;

public class GuiInputProcessor extends AbstractInputProcessor {

	private Client client;
	
	private Set<Integer> heldModifiers;
	
	public GuiInputProcessor(Client client) {
		super();
		this.client = client;
		heldModifiers = new HashSet<Integer>();
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if (Character.isISOControl(keycode)) {
			heldModifiers.add(keycode);
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (Character.isISOControl(keycode)) {
			heldModifiers.remove(keycode);
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		client.getGuiManager().getCurrentMenu();
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		GuiMenu menu = client.getGuiManager().getCurrentMenu();
		if (menu != null) {
			GridPoint2 clickPos = new GridPoint2(screenX, client.getApplication().getViewportHeight() - screenY);
			GuiComponent component = menu.attemptFocus(clickPos);
			if (component != null)
				component.interactClick(clickPos, 100, new KeyCombo(button, Set.copyOf(heldModifiers)));
			return true;
		} else {
			
		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

}
