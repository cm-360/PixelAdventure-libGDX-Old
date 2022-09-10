package com.github.cm360.pixadv.core.graphics.gui.input;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.GridPoint2;

public class AbstractInputProcessor implements InputProcessor {

	protected GridPoint2 mousePos;
	protected GridPoint2 mouseClickOrigin;
	
	public AbstractInputProcessor() {
		mousePos = new GridPoint2();
		mouseClickOrigin = new GridPoint2();
	}
	
	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		mousePos.x = screenX;
		mousePos.y = screenY;
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}

}
