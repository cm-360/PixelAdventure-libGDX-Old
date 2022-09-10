package com.github.cm360.pixadv.core.builtin.pixadv.java.entities.types.terra;

import java.util.Map;

import com.badlogic.gdx.graphics.Texture;
import com.github.cm360.pixadv.core.world.types.entities.AbstractEntity;

public class Human extends AbstractEntity {

	public Human() {
		width = 0.8;
		height = 1.8;
		mass = 70;
		collidability = 0.5;
	}
	
	@Override
	public String getID() {
		return "terra/human";
	}

	@Override
	public String getDisplayName() {
		return "Human";
	}
	
	@Override
	public Texture getTexture() {
		return null;
	}
	
	@Override
	public Map<String, String> getData() {
		// TODO Auto-generated method stub
		return null;
	}

}
