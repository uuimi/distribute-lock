/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved.
 * Project: distributed-lock 
 * Author: zhanghaolin
 * Createdate: 3:51:02 PM
 */
package com.uuimi.redis;

import redis.clients.jedis.Jedis;

/**
 * @Description: redis连接器
 * @author zhanghaolin
 */
public class RedisConnector {

	public static Jedis getConnectedJedis() {
		Jedis jedis = new Jedis("127.0.0.1", 6379);
		jedis.connect();
		
		return jedis;
	}
	
}
