package com.github.cm360.pixadv.core.util.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.github.cm360.pixadv.core.network.endpoints.Client;
import com.github.cm360.pixadv.core.registry.Registry;
import com.github.cm360.pixadv.core.util.Logger;
import com.github.cm360.pixadv.core.util.tasks.types.ChunkLoadRequest;
import com.github.cm360.pixadv.core.util.tasks.types.ChunkRepaintTask;
import com.github.cm360.pixadv.core.world.storage.world.World;

public class TaskQueue {

	protected BlockingQueue<AbstractTask> tasks;
	protected Map<String, AbstractTask> requests;
	protected List<Thread> workers;
	
	public TaskQueue(int workerCount) {
		tasks = new LinkedBlockingQueue<AbstractTask>();
		requests = new HashMap<String, AbstractTask>();
		workers = new ArrayList<Thread>();
		for (int i = 0; i < workerCount; i++) {
			Thread workerThread = new Thread(null, () -> {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						AbstractTask task = tasks.take();
						try {
							task.process();
							Logger.logMessage(Logger.DEBUG, "Finished task %s", task.toString());
						} catch (Exception e) {
							Logger.logException("Uncaught exception while processing task!", e);
						}
					} catch (InterruptedException ie) {
						break;
					}
				}
			}, "%s-Worker-%d".formatted(this.toString(), i + 1));
			workerThread.start();
			workers.add(workerThread);
		}
	}
	
	public synchronized void repaintChunk(Client client, World world, int cx, int cy) {
		tasks.add(new ChunkRepaintTask(client, world, cx, cy));
	}
	
	public synchronized void requestChunk(Registry registry, World world, int cx, int cy) {
		tasks.add(new ChunkLoadRequest(registry, world, cx, cy));
	}
	
	public synchronized void addGenericTask(AbstractTask task) {
		tasks.add(task);
	}
	
	public synchronized void clear() {
		requests.clear();
	}
	
	public void shutdown() {
		
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
	}

}
