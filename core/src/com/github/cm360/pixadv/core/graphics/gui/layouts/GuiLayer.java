package com.github.cm360.pixadv.core.graphics.gui.layouts;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.github.cm360.pixadv.core.graphics.gui.GuiComponent;
import com.github.cm360.pixadv.core.registry.Registry;

public abstract class GuiLayer extends GuiComponent {

	public static float scale = 2.0f;
	
	public GuiLayer() {
		super(null);
	}
	
	@Override
	public void paint(SpriteBatch b, Rectangle vBounds, Registry registry) {
		updateBounds(vBounds);
		paintSelf(b, vBounds, registry);
		for (GuiComponent child : children)
			child.paint(b, vBounds, registry);
	}

}
