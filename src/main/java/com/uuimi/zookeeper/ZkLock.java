package com.uuimi.zookeeper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;

/**
 * @Description:
 * @author zhanghaolin
 */
public class ZkLock {

	/** 成员变量常量集 **/
	private String lock_root_path = "/haolin-lock";
	private String lock_node_path = "/node";
	
	private ZooKeeper zk;
	private String	  waitLockNodePath;
	private String	  threadName = Thread.currentThread().getName();
	private CountDownLatch lockingWaitCountDownLatch = new CountDownLatch(1);
	
	public void lock() {
		try {
			
			// 创建排队节点
			waitLockNodePath = zk.create(path(), encode(""), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			System.out.println("lock: 创建临时节点，线程：" + getThreadName() + "，节点：" + waitLockNodePath);
			
			if (!tryLock(new LockingWaitWatcher(this))) {
				// 失败，等待
				lockingWaitCountDownLatch.await();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void unlock() {
		try {
			zk.delete(this.waitLockNodePath, -1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean tryLock(LockingWaitWatcher lockingWaitWatcher) {
		try {
			if (threadName.equals(decode(zk.getData(lock_root_path, false, new Stat())))) {
				System.out.println("当前持有锁线程是自己，直接重入！");
				return true;
			}
			
			// 所有的排队节点
			List<String> children = zk.getChildren(lock_root_path, false);
			
			// 排队节点从小到大排序
			Collections.sort(children);
			
			// 是否抢占成功？
			int index = children.indexOf(subPath());
			if (index == 0) {
				System.out.println("lock: 抢占成功!!!!!!!!!!!!!!!!!!!!!!，线程是：" + threadName + ", 自身path是: " + waitLockNodePath);
				
				// 抢锁成功，恢复线程执行
				lockingWaitCountDownLatch.countDown();
				
				zk.setData(lock_root_path, encode(threadName), -1);
				
				return true;
			} else {
				// 前一个节点path
				String prevNodePath = children.get(index - 1);
				
				// 监听前一个节点
				zk.exists(addPath(prevNodePath), lockingWaitWatcher);
				
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

	private String getThreadName() {
		return Thread.currentThread().getName();
	}
	
	private String path() {
		return lock_root_path + lock_node_path;
	}
	
	private String subPath() {
		return this.waitLockNodePath.replace(lock_root_path + "/", "");
	}
	
	private String addPath(String path) {
		return lock_root_path + "/" + path;
	}
	
	// constructor with orgs
	public ZkLock(ZooKeeper zk) {
		if (zk.getState() == States.CONNECTED) {
			this.zk = zk;
		} else {
			System.err.println("==》ZkLock（Zookeeper zk） is Exception: zk 未连接");
		}
		
		try {
			// 创建锁根目录节点
			Stat existStat = zk.exists(lock_root_path, false);
			if (existStat == null) {
				String createPath = zk.create(lock_root_path, encode(""), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				System.out.println("==》创建锁根节点：" + createPath);
			} else {
				System.out.println("==》创建锁根节点：已存在！");
			}
		} catch (Exception e) {
			if (e instanceof NodeExistsException) {
				
			} else {
				e.printStackTrace();
			}
		}
	}
	
	private byte[] encode(String param) {
		return param.getBytes();
	}
	
	private String decode(byte[] param) {
		return new String(param);
	}

}
