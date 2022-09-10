package com.github.cm360.pixadv.core.graphics.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.github.cm360.pixadv.core.graphics.gui.layouts.GuiLayer;
import com.github.cm360.pixadv.core.graphics.gui.layouts.GuiMenu;

public class GuiManager {

	private Stack<GuiMenu> menuStack;
	private List<GuiLayer> guiLayers;
	
	private String lastExceptionText = "";
	private long lastExceptionTime = -1;
	
	public GuiManager() {
		menuStack = new Stack<GuiMenu>();
		guiLayers = new ArrayList<GuiLayer>();
	}
	
	public List<GuiLayer> getGuiLayers() {
		return guiLayers;
	}
	
	public GuiLayer getTopGui() {
		if (menuStack.isEmpty()) {
			if (guiLayers.size() > 0)
				return guiLayers.get(guiLayers.size() - 1);
			else
				return null;
		} else {
			return menuStack.peek();
		}
	}
	
	public GuiMenu getCurrentMenu() {
		if (menuStack.size() > 0)
			return menuStack.peek();
		else
			return null;
	}
	
	public void openMenu(GuiMenu menu) {
//		clearInputs();
		menuStack.push(menu);
	}
	
	public GuiMenu closeMenu() {
		GuiMenu closedMenu =  menuStack.pop();
		closedMenu.onClose();
		return closedMenu;
	}
	
	public boolean closeMenu(GuiMenu menu) {
		menu.onClose();
		return menuStack.remove(menu);
	}
	
	public void closeAllMenus() {
		menuStack.clear();
	}
	
	public String getLastExceptionText() {
		return lastExceptionText;
	}
	
	public long getLastExceptionTime() {
		return lastExceptionTime;
	}
	
	public void setLastExceptionInfo(String lastExceptionText, long lastExceptionTime) {
		this.lastExceptionText = lastExceptionText;
		this.lastExceptionTime = lastExceptionTime;
	}

}
