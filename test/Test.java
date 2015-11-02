

import cn.zhl.threadpool.ObjectHandler;
import cn.zhl.threadpool.ThreadPool;

public class Test {

	public static void main(String[] args){
		ThreadPool<Task> pool = new ThreadPool<Task>(new TaskHandler(), 3);
		pool.execute(new Task());
		pool.execute(new Task());
		pool.execute(new Task());
		pool.execute(new Task());
	}
	
	private static class Task {
		
	}
	
	public static class TaskHandler implements ObjectHandler<Task> {

		public void handle(Task object) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
