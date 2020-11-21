package com.uuimi.zookeeper;

import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.ZooKeeper;

/**
 * 
 * @Description: 测试类
 * @author zhanghaolin
 */
public class TestDemo {

	/**
	 * 
	 * @Description: 测试方法
	 * @author zhanghaolin
	 */
	public static void main(String[] args) throws Exception {
		
		for (int i = 1; i <= 10; i++) {
			new Thread(() -> {
				doSomething();
			}, "thread-" + i).start();
		}
		
		TimeUnit.SECONDS.sleep(5);
	}

	public static void doSomething() {
		// connect & initial
		ZooKeeper instance = ZkConnector.getConnectedZooKeeper();
		ZkLock zk = new ZkLock(instance);
		
		try {
			zk.lock();
			
			// 模拟业务操作
			System.out.println(Thread.currentThread().getName() + "线程执行业务逻辑");
			
			Thread.sleep(1000);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			zk.unlock();
		}
		
	}
	
}
