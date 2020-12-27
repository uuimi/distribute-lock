/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved.
 * Project: distributed-lock 
 * Author: zhanghaolin
 * Createdate: 4:04:15 PM
 */
package com.uuimi.redis;


/**
 * @Description: lua脚本收集
 * @author zhanghaolin
 */
public enum LuaScripts {
	
	ADD_LOCK_LIFE("if redis.call(\"get\", KEYS[1]) == ARGV[1]\n" + 	// 判断是否是锁持有者
				"then\n" + 
				"    local thisLockMaxTimeKeepKey=KEYS[1] .. \":maxTime\"\n" +  // 记录锁最大时间的key是：锁名字:maxTime
				"    local nowTime=tonumber(ARGV[2])\n" +  // 当前传参进来的time
				"    local maxTime=redis.call(\"incr\", thisLockMaxTimeKeepKey)\n" + // 取出当前锁设置的最大的超时时间，如果这个保持时间的key不存在返回的是字符串nil，这里为了lua脚本的易读性，用incr操作，这样读出来的都是number类型的操作
				"    local bigerTime=maxTime\n" + // 临时变量bigerTime=maxTime
				"    if nowTime>maxTime-1\n" +    // 如果传参进来的时间>记录的最大时间
				"    then\n" + 
				"        bigerTime=nowTime\n" + // 则更新bigerTime
				"        redis.call(\"set\", thisLockMaxTimeKeepKey, tostring(bigerTime))\n" + // 设置超时时间为最大的time，是最安全的
				"    else \n" + 
				"        redis.call(\"decr\", thisLockMaxTimeKeepKey)\n" + // 当前传参time<maxTime，将刚才那次incr减回来
				"    end\n" + 
				"    return redis.call(\"expire\", KEYS[1], tostring(bigerTime))\n" + // 重新设置超时时间为当前锁过的最大的time
				"else\n" + 
				"    return 0\n" + 
				"end"),
	
	RELEASE_LOCK("if redis.call(\"get\",KEYS[1]) == ARGV[1] \n" + 
				"then\n" + 
				"    return redis.call(\"del\", KEYS[1])\n" + 
				"else\n" + 
				"    return 0\n" + 
				"end"),
	
	THREAD_ADD_LIFE("local v=redis.call(\"get\", KEYS[1]) \n" + 	// get key
				"if v==false \n" +  // 如果不存在key，读出结果v是false
				"then \n" + 		// 不存在不处理
				"else \n" + 
				"    local match = string.find(v, ARGV[1]) \n" + // 存在，判断是否能和APP_ID匹配，匹配不上时match是nil
				"    if match==\"nil\" \n" + 
				"    then \n" + 
				"    else  \n" + 
				"        return redis.call(\"expire\", KEYS[1], ARGV[2]) \n" + // 匹配上了返回的是索引位置，如果匹配上了意味着就是当前进程占有的锁，就延长时间
				"    end \n" + 
				"end")
	
	;
	
	private String script;

	public String getScript() {
		return script;
	}
	
	private LuaScripts(String script) {
		this.script = script;
	}
}
