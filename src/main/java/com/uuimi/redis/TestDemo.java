/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved.
 * Project: distributed-lock 
 * Author: zhanghaolin
 * Createdate: 3:57:31 PM
 */
package com.uuimi.redis;

import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;

/**
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
			//final int sleep = 200;
			final int sleep = i != 2 ? 200 : 2000;
			new Thread(() -> {
				doSomething(sleep);
			}, "thread-" + i).start();
		}
		
		TimeUnit.SECONDS.sleep(5);
	} 
	
	public static void doSomething(long sleep) {
		
		// initial
		Jedis jedis = RedisConnector.getConnectedJedis();
		
		RedisLock redisLock = new RedisLock(jedis);
		
		try {
			redisLock.lock();
			
			// 处理业务
			System.out.println("	" + Thread.currentThread().getName() + " 线程处理业务逻辑中 ...............");
			
			Thread.sleep(sleep);
			
			System.out.println("	" + Thread.currentThread().getName() + " 线程处理业务逻辑完毕！！！！！！！！！！！！");
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			redisLock.unlock();
		}
		
	}
	
}
