/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved.
 * Project: distributed-lock 
 * Author: zhanghaolin
 * Createdate: 4:00:38 PM
 */
package com.uuimi.redis;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

/**
 * @Description: redis分布式锁
 * @author zhanghaolin
 */
public class RedisLock {
	/** 
	 * 一定要有这行代码；
	 * 在集群模式下，一个应用程序会部署多个节点；
	 * 多个节点中的线程id肯定会有一样的；
	 * 所以在value中一定要有此应用的标识，但是不能写死，因为写死的话，部署多个节点还是一样的没意义；
	 * 所以要这种在应用启动的时候通过uuid去生成一个应用的唯一标识
	 * 
	 * 【分布式锁 Value必须要使用（APP_ID + ThreadId）的目的】
	 * 1、可重入，只有当前线程获取了锁，才可以重入，所以要有线程标识，而且要有应用标识
	 * 2、释放锁时用来校验（必须），是否锁被当前线程持有：
	 * 	a. 如果别的线程没有尝试获取锁而是直接执行了释放锁的代码unlock()，这把锁肯定是不应该被释放的；
	 *  b. 如果意外情况已经被别的线程占用了，当前线程就不要去释放锁了，否则更乱了；
	 *  c. 是不是就像ReentrantLock中为啥要有isHeldByCurrentThread呢；
	 *  
	 */
	public static final String APP_ID = UUID.randomUUID().toString();

	public static final String lock_key = "haolin-lock";
	public static final Integer timeout = 1;
	public static final Integer spin_time = 50;
	
	private String threadName = Thread.currentThread().getName();
	private Long   threadId = Thread.currentThread().getId();
	
	private Jedis jedis;
	
	public void lock() {
		if (isHeldByCurrentThread()) {
			return;
		}
		
		while (!tryLock()) {
			try {
				//System.out.println("	线程自旋中：" + threadName + " ....");
				Thread.sleep(spin_time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("	线程：" + threadName + "，占锁成功！★★★");
	}
	
	public void unlock() {
		
		/**
		 * 反例
		 */
		// jedis.del(encode(lock_key));
		// System.out.println("	线程：" + threadName + " 释放锁成功！☆☆☆");
		
		List<byte[]> keys = Arrays.asList(encode(lock_key));
		List<byte[]> args = Arrays.asList(encode(APP_ID + String.valueOf(threadId)));
		
		long eval = (Long) jedis.eval(encode(LuaScripts.RELEASE_LOCK.getScript()), keys, args);
		if (eval == 1) {
			System.out.println("	线程：" + threadName + " 释放锁成功！☆☆☆");
		} else {
			System.out.println("	线程：" + threadName + " 释放锁失败！该线程未持有锁！！！");
		}
		
	}
	
	private boolean tryLock() {
		SetParams setParams = new SetParams();
		setParams.ex(timeout);
		setParams.nx();
		String response = jedis.set(lock_key, APP_ID + String.valueOf(threadId), setParams);
		return "OK".equals(response);
	}

	private boolean isHeldByCurrentThread() {
		
		List<byte[]> keys = Arrays.asList(encode(lock_key));
		List<byte[]> args = Arrays.asList(encode(APP_ID + String.valueOf(threadId)), encode(String.valueOf(timeout)));
		
		long eval = (Long) jedis.eval(encode(LuaScripts.ADD_LOCK_LIFE.getScript()), keys, args);
		return eval == 1;
	}

	public RedisLock(Jedis jedis) {
		if (!jedis.isConnected()) {
			System.err.println("==》RedisLock(Jedis jedis) jedis 未连接！");
		}
		this.jedis = jedis;
		new RedisLockIdleThreadPool();
	}
	
	private byte[] encode(String param) {
		return param.getBytes();
	}
	
}
