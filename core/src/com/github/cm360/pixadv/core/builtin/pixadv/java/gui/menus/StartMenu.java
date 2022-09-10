package com.github.cm360.pixadv.core.builtin.pixadv.java.gui.menus;

import java.io.File;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.github.cm360.pixadv.core.builtin.pixadv.java.entities.types.terra.HumanPlayer;
import com.github.cm360.pixadv.core.builtin.pixadv.java.gui.components.generic.GuiButton;
import com.github.cm360.pixadv.core.builtin.pixadv.java.gui.components.generic.GuiImage;
import com.github.cm360.pixadv.core.graphics.gui.input.KeyCombo;
import com.github.cm360.pixadv.core.graphics.gui.layouts.GuiMenu;
import com.github.cm360.pixadv.core.network.endpoints.Client;
import com.github.cm360.pixadv.core.registry.Identifier;

public class StartMenu extends GuiMenu {

	public StartMenu(Client client) {
		super();
		// Main font
		BitmapFont menuFont = client.getRenderingEngine().getDefaultFont(); //.deriveFont(24f);
		// Logo
		GuiImage logoImage = new GuiImage(this, parentBounds -> {
			return new Rectangle(
					(parentBounds.getWidth() * 0.5f) - (155f * scale),
					(parentBounds.getHeight() * 0.5f) + (10f * scale),
					310f * scale,
					110f * scale
				);
		});
		logoImage.setTextures(new Identifier[] { Identifier.parse("pixadv:textures/gui/menu/title/logo") });
		children.add(logoImage);
		// Singleplayer button
		GuiButton singleplayerButton = new GuiButton(this, parentBounds -> {
			return new Rectangle(
					(parentBounds.getWidth() * 0.5f) - (120f * scale),
					(parentBounds.getHeight() * 0.5f) - (48f * scale),
					240f * scale,
					32f * scale
				);
		}, "Singleplayer", menuFont);
		singleplayerButton.setTextures(new Identifier[] { Identifier.parse("pixadv:textures/gui/menu/title/singleplayer") });
		singleplayerButton.registerEvent(new KeyCombo(Buttons.LEFT), () -> {
			// TODO load singleplayer menu
			client.getGuiManager().closeMenu();
			client.addTask(() -> {
				client.load(new File(".\\data\\saves\\Universe Zero"));
				// pixadv:textures/entities/girl
				HumanPlayer player = new HumanPlayer(client.getRegistry().getTexture(Identifier.parse("pixadv:mario")));
				client.getCurrentUniverse().getCurrentWorld().addEntity(client.getPlayerId(), player);
			});
		});
		children.add(singleplayerButton);
		// Multiplayer button
		GuiButton multiplayerButton = new GuiButton(this, parentBounds -> {
			return new Rectangle(
					(parentBounds.getWidth() * 0.5f) - (120f * scale),
					(parentBounds.getHeight() * 0.5f) - (84f * scale),
					240f * scale,
					32f * scale
				);
		}, "Multiplayer", menuFont);
		multiplayerButton.setTextures(new Identifier[] { Identifier.parse("pixadv:textures/gui/menu/title/multiplayer") });
		multiplayerButton.registerEvent(new KeyCombo(Buttons.LEFT), () -> {
			// TODO load multiplayer menu
			client.getGuiManager().closeMenu();
			client.connect("127.0.0.1", 43234);
		});
		children.add(multiplayerButton);
		// Options button
		GuiButton optionsButton = new GuiButton(this, parentBounds -> {
			return new Rectangle(
					(parentBounds.getWidth() * 0.5f) - (120f * scale),
					(parentBounds.getHeight() * 0.5f) - (120f * scale),
					118f * scale,
					32f * scale
				);
		}, "Options", menuFont);
		optionsButton.setTextures(new Identifier[] { Identifier.parse("pixadv:textures/gui/menu/title/options") });
		optionsButton.registerEvent(new KeyCombo(Buttons.LEFT), () -> {
			// TODO load options menu
		});
		children.add(optionsButton);
		// Quit button
		GuiButton quitButton = new GuiButton(this, parentBounds -> {
			return new Rectangle(
					(parentBounds.getWidth() * 0.5f) + (2f * scale),
					(parentBounds.getHeight() * 0.5f) - (120f * scale),
					118f * scale,
					32f * scale
				);
		}, "Quit", menuFont);
		quitButton.setTextures(new Identifier[] { Identifier.parse("pixadv:textures/gui/menu/title/quit") });
		quitButton.registerEvent(new KeyCombo(Buttons.LEFT), () -> {
			client.getApplication().dispose();
		});
		children.add(quitButton);
	}

	@Override
	public void onClose() {
		// TODO Auto-generated method stub
	}

}
