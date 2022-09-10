package com.github.cm360.pixadv.core.commands;

@FunctionalInterface
public interface SyntaxCallback {

	public String invoke(Object... arguments);

}
