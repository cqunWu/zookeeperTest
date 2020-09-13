package com.wcq.zookeeper;

import org.apache.zookeeper.*;

import java.util.concurrent.CountDownLatch;

/**
 * @Author: wucq
 * @Description: Java语言操作ZK
 * @Date: Create in 2020/5/7 21:06
 */
public class ZookeeperTest {
    // zk连接地址
    private static final String CONNECTSTRING = "127.0.0.1:2181";
    private static final int SESSIONTIMEOUT = 5000;
    //信号量,阻塞程序执行,用户等待zookeeper连接成功,发送成功信号，
    private static final CountDownLatch countDownLatch = new CountDownLatch(1);
    public static void main(String[] args) throws InterruptedException {
        // zk创建连接
        ZooKeeper zooKeeper = null;
        try {
                zooKeeper = new ZooKeeper(CONNECTSTRING, SESSIONTIMEOUT, new Watcher() {
                    public void process(WatchedEvent event) {
                        // 获取事件状态
                        Event.KeeperState keeperState = event.getState();
                        // 获取事件类型
                        Event.EventType eventType = event.getType();
                        if (Event.KeeperState.SyncConnected == keeperState) {
                            if (Event.EventType.None == eventType) {
                                //调用该方法会减一，如果为0
                                countDownLatch.countDown();
                                System.out.println("zk 启动连接...");
                            }

                        }
                    }
            });
            // CreateMode.EPHEMERAL 创建一个临时节点
            // EPHEMERAL_SEQUENTIAL 如果节点发生重复的情况下，会自动id 自增保证唯一性
            //计数器为0才会执行
            countDownLatch.await();
            String nodeResult = zooKeeper.create("/test", "wcq".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("节点名称" + nodeResult);
        } catch (Exception e) {

        } finally {
            if (zooKeeper != null) {
                zooKeeper.close();
            }
        }
    }
}
