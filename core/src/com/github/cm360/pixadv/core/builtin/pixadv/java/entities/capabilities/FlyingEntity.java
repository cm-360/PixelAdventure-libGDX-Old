package com.github.cm360.pixadv.core.builtin.pixadv.java.entities.capabilities;

import com.github.cm360.pixadv.core.world.types.entities.Entity;

public interface FlyingEntity extends Entity {

	public void setFlying(boolean flying);
	
	public boolean isFlying();
	
	public void setFlightSpeed(double flightSpeed);
	
	public double getFlightSpeed();
	
	public double getFlightDecayX();
	
	public double getFlightDecayY();

}
