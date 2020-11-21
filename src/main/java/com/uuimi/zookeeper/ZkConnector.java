package com.uuimi.zookeeper;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.ZooKeeper;

/**
 * @Description: zk连接控制器
 * @author zhanghaolin
 */
public class ZkConnector {

	public static ZooKeeper getConnectedZooKeeper() {
		
		ZooKeeper zk = null;
		CountDownLatch connectedCountDownLatch = new CountDownLatch(1);
		CommonWatcher commonWatcher = new CommonWatcher(connectedCountDownLatch);
		
		try {
			zk = new ZooKeeper("127.0.0.1:2181", 3000, commonWatcher);
			connectedCountDownLatch.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return zk;
	}
	
	
}
