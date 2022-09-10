package com.github.cm360.pixadv.core.graphics.gui;

import com.badlogic.gdx.math.Rectangle;

@FunctionalInterface
public interface BoundsMutator {

	public Rectangle mutate(Rectangle parentBounds);

}
