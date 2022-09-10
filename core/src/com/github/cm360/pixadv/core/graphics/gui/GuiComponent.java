package com.github.cm360.pixadv.core.graphics.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import com.github.cm360.pixadv.core.graphics.gui.input.KeyCombo;
import com.github.cm360.pixadv.core.registry.Identifier;
import com.github.cm360.pixadv.core.registry.Registry;
import com.github.cm360.pixadv.core.sound.beethoven.Beethoven;

public abstract class GuiComponent {
	
	protected GuiComponent parent;
	protected List<GuiComponent> children;
	
	protected Rectangle bounds;
	protected BoundsMutator boundsMutator;
	protected boolean focusable;
	protected boolean hovered;
	
	protected HashMap<KeyCombo, Runnable> events;
	
	protected Beethoven soundManager;
	protected Identifier hoverSound;
	protected Identifier selectSound;
	
	public GuiComponent(GuiComponent parent) {
		this(parent, dummy -> dummy);
	}
	
	public GuiComponent(GuiComponent parent, BoundsMutator boundsMutator) {
		this.parent = parent;
		this.children = new ArrayList<GuiComponent>();
		this.bounds = new Rectangle();
		this.boundsMutator = boundsMutator;
		this.focusable = false;
		this.hovered = false;
		this.events = new HashMap<KeyCombo, Runnable>();
		this.hoverSound = Identifier.parse("pixadv:sounds/ui/select/hover");
		this.selectSound = Identifier.parse("pixadv:sounds/ui/select/select");
	}
	
	// Rendering methods
	public void paint(SpriteBatch b, Rectangle vBounds, Registry registry) {
		updateBounds(parent.getBounds());
		paintSelf(b, vBounds, registry);
		for (GuiComponent child : children)
			child.paint(b, vBounds, registry);
	}
	
	protected void paintSelf(SpriteBatch b, Rectangle vBounds, Registry registry) {
		// Do nothing by default
	}
	
	public void updateBounds(Rectangle parentBounds) {
		bounds = boundsMutator.mutate(parentBounds);
	}
	
	public Rectangle getBounds() {
		return bounds;
	}
	
	public void setHovered(boolean hovered) {
		this.hovered = hovered;
	}
	
	public boolean isHovered() {
		return hovered;
	}
	
	// Interaction methods
	public void registerEvent(KeyCombo trigger, Runnable action) {
		events.put(trigger, action);
	}
	
	public boolean removeEvent(KeyCombo trigger) {
		return events.remove(trigger) != null;
	}
	
	public void interactClick(GridPoint2 mousePos, long clickDuration, KeyCombo keys) {
		for (KeyCombo combo : events.keySet())
			if (keys.containsAll(combo))
				events.get(combo).run();
//		soundManager.playSound(selectSound);
	}

	public void interactHover(GridPoint2 mousePos, KeyCombo keys) {

	}

	public void interactKey(KeyCombo keys) {

	}
	
	public GuiComponent attemptFocus(GridPoint2 mousePos) {
		for (GuiComponent child : children) {
			GuiComponent result = child.attemptFocus(mousePos);
			if (result != null)
				return result;
		}
		return (focusable && getBounds().contains(mousePos.x, mousePos.y)) ? this : null;
	}
	
	// Access methods
	public Identifier getHoverSoundId() {
		return hoverSound;
	}
	
	public Identifier getSelectSoundId() {
		return selectSound;
	}
	
	// Hierarchy methods
	public GuiComponent getParent() {
		return parent;
	}
	
	public List<GuiComponent> getChildren() {
		return children;
	}

}
