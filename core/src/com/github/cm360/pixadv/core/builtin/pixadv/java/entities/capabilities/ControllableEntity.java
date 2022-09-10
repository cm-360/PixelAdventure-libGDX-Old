package com.github.cm360.pixadv.core.builtin.pixadv.java.entities.capabilities;

import java.util.Set;

import com.github.cm360.pixadv.core.world.types.entities.Entity;

public interface ControllableEntity extends Entity {

	public enum Input { UP, DOWN, LEFT, RIGHT, JUMP };
	
	public void setInputs(Set<Input> inputs);

}
