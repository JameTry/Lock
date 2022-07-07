package work.jame.note;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author : Jame
 * @date : 2022-07-07 16:04
 **/
public class Note {

    /**
     * Reentrant Lock
     * 存在多线程并发的地方
     * 1.两个线程同时进行执行tryAcquire
     * 2.A线程执行unlock B线程正在初始化(执行到addWaiter#if (compareAndSetTail(pred, node)) ) C线程进场,公平如何保证
     * 3.两个线程 A线程执行马上要unpack B线程刚完成链表的创建,马上就要park了,如何保证在park前B线程能获取到锁或者A线程能成功唤醒B线程
     * 其他问题
     * 头结点的空node的作用?
     * 重入锁体现
     * @param args
     */

    public static void main(String[] args) {
        ReentrantLock lock = new ReentrantLock(true);
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                lock.lock();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                lock.unlock();
            }).start();
        }

    }





}
