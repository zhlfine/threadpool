

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhl.threadpool.ThreadPool;

public class Test {
	private static Logger logger = LoggerFactory.getLogger(Test.class);
	
	public static void main(String[] args) throws InterruptedException{
		ThreadPool<Task> pool = new ThreadPool<Task>("test", new TaskHandler(), 25, 20000);
		for(int i = 0; i < 10; i++){
			pool.execute(new Task());
		}
		for(int i = 0; i < 20; i++){
			pool.execute(new Task());
			Thread.sleep(1000);
		}
	}
	
	private static class Task {
		
	}
	
	public static class TaskHandler implements ThreadPool.ObjectHandler<Task> {

		public void handle(Task object) {
			logger.debug("task {} started", object.toString());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.debug("task {} stoped", object.toString());
		}
		
	}
	
}
