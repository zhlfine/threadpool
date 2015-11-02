package cn.zhl.threadpool;

public interface ObjectHandler<T> {

	void handle(T object);
	
}
