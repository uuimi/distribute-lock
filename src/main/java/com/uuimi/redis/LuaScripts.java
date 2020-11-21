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
	
	ADD_LOCK_LIFE("if redis.call(\"get\", KEYS[1]) == ARGV[1]\n" + 
				"then\n" + 
				"    return redis.call(\"expire\", KEYS[1], ARGV[2])\n" + 
				"else\n" + 
				"    return 0\n" + 
				"end"),
	
	RELEASE_LOCK("if redis.call(\"get\",KEYS[1]) == ARGV[1] \n" + 
				"then\n" + 
				"    return redis.call(\"del\", KEYS[1])\n" + 
				"else\n" + 
				"    return 0\n" + 
				"end"),
	
	THREAD_ADD_LIFE("if redis.call(\"exists\", KEYS[1]) == 1\n" + 
				"then\n" + 
				"    return redis.call(\"expire\", KEYS[1], ARGV[1])\n" + 
				"else\n" + 
				"    return 0\n" + 
				"end\n" + 
				"")
	
	;
	
	private String script;

	public String getScript() {
		return script;
	}
	
	private LuaScripts(String script) {
		this.script = script;
	}
}
