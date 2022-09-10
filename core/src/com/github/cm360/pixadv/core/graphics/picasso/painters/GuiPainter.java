package com.github.cm360.pixadv.core.graphics.picasso.painters;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.github.cm360.pixadv.core.graphics.gui.GuiManager;
import com.github.cm360.pixadv.core.graphics.gui.layouts.GuiLayer;
import com.github.cm360.pixadv.core.graphics.gui.layouts.GuiMenu;
import com.github.cm360.pixadv.core.graphics.picasso.Precompute;
import com.github.cm360.pixadv.core.registry.Registry;

public class GuiPainter {

	private GuiManager manager;
	
	public GuiPainter(GuiManager manager) {
		this.manager = manager;
	}
	
	public void paint(SpriteBatch b, Registry registry, List<Disposable> trash, Precompute precomp) {
		// Paint GUI layers
		for (GuiLayer layer : manager.getGuiLayers()) {
			layer.updateBounds(precomp.getViewportBounds());
			layer.paint(b, precomp.getViewportBounds(), registry);
		}
		// Paint current menu
		GuiMenu menu = manager.getCurrentMenu();
		if (menu != null) {
			menu.paint(b, precomp.getViewportBounds(), registry);
		}
	}

}
