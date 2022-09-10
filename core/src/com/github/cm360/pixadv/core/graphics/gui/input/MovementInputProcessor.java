package com.github.cm360.pixadv.core.graphics.gui.input;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Input.Keys;
import com.github.cm360.pixadv.core.builtin.pixadv.java.entities.capabilities.ControllableEntity.Input;
import com.github.cm360.pixadv.core.network.endpoints.Client;
import com.github.cm360.pixadv.core.world.storage.universe.Universe;
import com.github.cm360.pixadv.core.world.storage.world.World;

public class MovementInputProcessor extends AbstractInputProcessor {

	private final Client client;
	
	private Set<Input> playerInputs;
	private Map<Integer, Input> inputMappings;
	
	public MovementInputProcessor(Client client) {
		super();
		this.client = client;
		// Keypress mappings
		playerInputs = new HashSet<Input>();
		inputMappings = new HashMap<Integer, Input>();
		inputMappings.put(Keys.W, Input.UP);
		inputMappings.put(Keys.S, Input.DOWN);
		inputMappings.put(Keys.A, Input.LEFT);
		inputMappings.put(Keys.D, Input.RIGHT);
		inputMappings.put(Keys.SPACE, Input.JUMP);
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if (inputMappings.containsKey(keycode)) {
			playerInputs.add(inputMappings.get(keycode));
			Universe universe = client.getCurrentUniverse();
			if (universe != null) {
				World world = universe.getCurrentWorld();
				if (world != null) {
					world.getPhysicsEngine().updateControlInputs(client.getControlledIds(), playerInputs, new Point());
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean keyUp(int keycode) {
		if (inputMappings.containsKey(keycode)) {
			playerInputs.remove(inputMappings.get(keycode));
			Universe universe = client.getCurrentUniverse();
			if (universe != null) {
				World world = universe.getCurrentWorld();
				if (world != null) {
					world.getPhysicsEngine().updateControlInputs(client.getControlledIds(), playerInputs, new Point());
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public Set<Input> getPlayerInputs() {
		return Set.copyOf(playerInputs);
	}

}
