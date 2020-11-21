package com.uuimi.zookeeper;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;

/**
 * @Description: 公共watcher
 * @author zhanghaolin
 */
public class CommonWatcher implements Watcher {

	private CountDownLatch connectedCountDownLatch;
	
	@Override
	public void process(WatchedEvent event) {
		
		KeeperState state = event.getState();
		
		switch (state) {
	        case SyncConnected:
	        	System.out.println("==》CommonWatcher 连接zk成功！");
	            connectedCountDownLatch.countDown();
	            break;
	        case Disconnected:
	            
	            break;
	        case Expired:
	            
	            break;
	        case AuthFailed:
	            
	            break;
	        default:
	            break;
        }
	}
	
	public CommonWatcher(CountDownLatch connectedCountDownLatch) {
		this.connectedCountDownLatch = connectedCountDownLatch;
	}
}
