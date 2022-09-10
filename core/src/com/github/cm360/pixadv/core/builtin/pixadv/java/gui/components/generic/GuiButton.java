package com.github.cm360.pixadv.core.builtin.pixadv.java.gui.components.generic;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.github.cm360.pixadv.core.graphics.gui.BoundsMutator;
import com.github.cm360.pixadv.core.graphics.gui.GuiComponent;
import com.github.cm360.pixadv.core.registry.Registry;

public class GuiButton extends GuiImage {

	protected String buttonText;
	protected BitmapFont buttonFont;
	
	public GuiButton(GuiComponent parent, BoundsMutator boundsMutator, String text, BitmapFont font) {
		super(parent, boundsMutator);
		focusable = true;
		buttonText = text;
		buttonFont = font;
	}
	
	@Override
	protected void paintSelf(SpriteBatch b, Rectangle vBounds, Registry registry) {
		// Paint image
		if (hovered) {
			super.drawTexture(b, registry, textures[1]);
		} else {
			super.drawTexture(b, registry, textures[0]);
		}
		// Paint text
		if (!buttonText.isEmpty()) {
//			g.setFont(buttonFont.deriveFont((float) (buttonFont.getSize() * GuiLayer.scale)));
//			FontMetrics fontMetrics = g.getFontMetrics();
//			Point textBasePoint = new Point(
//					(int) (bounds.getX() + (bounds.getWidth() - fontMetrics.stringWidth(buttonText)) * 0.5),
//					(int) (bounds.getY() + (bounds.getHeight() + fontMetrics.getHeight() * 0.5) * 0.5)
//				);
//			g.setColor(Color.DARK_GRAY);
//			int offset = (int) Math.ceil(1.5 * GuiLayer.scale);
//			g.drawString(buttonText, textBasePoint.x + offset, textBasePoint.y + offset);
//			g.setColor(Color.WHITE);
//			g.drawString(buttonText, textBasePoint.x, textBasePoint.y);
		}
	}

}
