/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved.
 * Project: distributed-lock 
 * Author: zhanghaolin
 * Createdate: 7:02:46 PM
 */
package com.uuimi.redis;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;

/**
 * @Description: 续命线程
 * @author zhanghaolin
 */
public class RedisLockIdleThreadPool {

	private volatile ScheduledExecutorService scheduledThreadPool;
	
	public RedisLockIdleThreadPool() {
		
		if (scheduledThreadPool == null) {
			synchronized (this) {
				if (scheduledThreadPool == null) {
					scheduledThreadPool = Executors.newScheduledThreadPool(1);
					
					scheduledThreadPool.scheduleAtFixedRate(() -> {
						addLife();
					}, 0, 100, TimeUnit.MILLISECONDS);
				}
			}
		}
	}
	
	private void addLife() {
		
		Jedis jedis = RedisConnector.getConnectedJedis();
		
		List<byte[]> keys = Arrays.asList(RedisLock.lock_key.getBytes());
		List<byte[]> args = Arrays.asList(String.valueOf(RedisLock.timeout).getBytes());
		
		jedis.eval(LuaScripts.THREAD_ADD_LIFE.getScript().getBytes(), keys, args);
	}
	
}
