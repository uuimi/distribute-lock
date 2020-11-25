/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved.
 * Project: distributed-lock 
 * Author: zhanghaolin
 * Createdate: 4:00:38 PM
 */
package com.uuimi.redis;

import java.util.Arrays;
import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

/**
 * @Description: redis分布式锁
 * @author zhanghaolin
 */
public class RedisLock {

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
		List<byte[]> args = Arrays.asList(encode(String.valueOf(threadId)));
		
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
		String response = jedis.set(lock_key, String.valueOf(threadId), setParams);
		return "OK".equals(response);
	}

	private boolean isHeldByCurrentThread() {
		
		List<byte[]> keys = Arrays.asList(encode(lock_key));
		List<byte[]> args = Arrays.asList(encode(String.valueOf(threadId)), encode(String.valueOf(timeout)));
		
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
