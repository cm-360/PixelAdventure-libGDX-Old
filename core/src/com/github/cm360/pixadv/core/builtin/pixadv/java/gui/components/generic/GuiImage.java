package com.github.cm360.pixadv.core.builtin.pixadv.java.gui.components.generic;

import java.util.stream.Stream;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.github.cm360.pixadv.core.graphics.gui.BoundsMutator;
import com.github.cm360.pixadv.core.graphics.gui.GuiComponent;
import com.github.cm360.pixadv.core.registry.Identifier;
import com.github.cm360.pixadv.core.registry.Registry;

public class GuiImage extends GuiComponent {

	protected Identifier[] textures;
	
	public GuiImage(GuiComponent parent) {
		super(parent);
	}
	
	public GuiImage(GuiComponent parent, BoundsMutator boundsMutator) {
		super(parent, boundsMutator);
	}
	
	public void setTextures(Identifier[] newTextures) {
		textures = newTextures;
	}
	
	@Override
	protected void paintSelf(SpriteBatch b, Rectangle vBounds, Registry registry) {
		Stream.of(textures).forEach(id -> drawTexture(b, registry, id));
	}
	
	protected void drawTexture(SpriteBatch b, Registry registry, Identifier textureId) {
		b.draw(registry.getTexture(textureId),
				bounds.getX(),
				bounds.getY(),
				bounds.getWidth(),
				bounds.getHeight());
	}

}
