package com.uuimi.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

/**
 * @Description: 锁队列监听器
 * @author zhanghaolin
 */
public class LockingWaitWatcher implements Watcher {

	private ZkLock 			zkLock;

	@Override
	public void process(WatchedEvent event) {
		
		EventType   eventType = event.getType();
		String 		path 	  = event.getPath();
		
		switch (eventType) {
	        case NodeDeleted:
	        	System.out.println("==》LockingWaitWatcher 节点下线：" + path);
	        	
	        	zkLock.tryLock(this);
	            break;
	        default:
	            break;
	    }
	}

	/**
	* @param lockingWaitCountDownLatch
	* @param currentNodePath
	* @param zkLock
	*/
	public LockingWaitWatcher(ZkLock zkLock) {
		super();
		this.zkLock = zkLock;
	}

}
