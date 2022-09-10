package com.github.cm360.pixadv.core.world.types;

public interface Tickable {

	public boolean shouldTickContinuously();
	
	public void continuousTick();
	
	public double getRandomTickChance();
	
	public void randomTick();

}
