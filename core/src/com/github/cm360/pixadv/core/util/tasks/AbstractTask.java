package com.github.cm360.pixadv.core.util.tasks;

public abstract class AbstractTask implements Task {

	private String id;
	
	public AbstractTask(String id) {
		this.id = id;
	}
	
	public abstract void process();
	
	public String getId() {
		return id;
	}

}
