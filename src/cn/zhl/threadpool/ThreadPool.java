package cn.zhl.threadpool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPool<T> {
	private static Logger logger = LoggerFactory.getLogger(ThreadPool.class);
	
	private String name;
	private ObjectHandler<T> handler;
	private int maxThread;
	private long keepAliveTime;
	private BlockingQueue<T> queue;
	
	private static long poolIndex = 0;
	private long workerIndex = 0;
	private Set<Worker> workers = new HashSet<Worker>();
	
	public ThreadPool(ObjectHandler<T> handler, int maxThread){
		this(""+(poolIndex++), handler, maxThread, 30000);
	}
	
	public ThreadPool(ObjectHandler<T> handler, int maxThread, long keepAliveTime){
		this(""+(poolIndex++), handler, maxThread, keepAliveTime);
	}
	
	public ThreadPool(String name, ObjectHandler<T> handler, int maxThread){
		this(name, handler, maxThread, 30000);
	}
	
	public ThreadPool(String name, ObjectHandler<T> handler, int maxThread, long keepAliveTime){
		this(name, handler, maxThread, keepAliveTime, new LinkedBlockingQueue<T>());
	}
	
	public ThreadPool(String name, ObjectHandler<T> handler, int maxThread, long keepAliveTime, BlockingQueue<T> queue){
		this.name = "ThreadPool-"+name;
		this.handler = handler;
		this.maxThread = maxThread;
		this.keepAliveTime = keepAliveTime;
		this.queue = queue;
	}
	
	public int getQueueSize(){
		return queue.size();
	}
	
	public int getThreadCount(){
		synchronized(workers){
			return workers.size();
		}
	}
	
	public void execute(T object){
		queue.offer(object);
		tryCreatingThread();
	}
	
	private void tryCreatingThread(){
		if(queue.peek() != null){
			synchronized(workers){
				if(queue.peek() != null && workers.size() < maxThread){
					String threadName = name+"-"+(workerIndex++);
					logger.info("Creating new worker thread {}", threadName);
					Worker worker = new Worker(threadName);
					worker.start();
					workers.add(worker);
				}
			}
		}
	}
	
	private class Worker extends Thread {
		private boolean stop = false;
		private long waitingUnit = 10000;//10seconds
		
		public Worker(String name){
			super(name);
		}
		
		public void run(){
			long waitingTime = 0;
			while(!stop){
				T object = null;
				try{
					object = queue.poll(waitingUnit, TimeUnit.MILLISECONDS);
				}catch(InterruptedException e){
					continue;
				}
				
				if(object == null){
					waitingTime += waitingUnit;
					if(waitingTime >= keepAliveTime){
						stop = true;
						break;
					}
				}else{
					waitingTime = 0;
					tryCreatingThread();
					
					try{
						handler.handle(object);
					}catch(Exception e){
						logger.warn("Error:"+object.toString(), e);
					}
				}
			}
			
			synchronized(workers){
				workers.remove(this);
			}
			logger.info("worker thread {} stopped", Thread.currentThread().getName());
		}
	}
	
	public static interface ObjectHandler<T> {

		void handle(T object);
		
	}
	
}
