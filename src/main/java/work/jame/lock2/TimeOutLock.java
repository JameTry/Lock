package work.jame.lock2;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author : Jame
 * @date : 2022-06-11 19:51
 * @description :
 **/
public class TimeOutLock {

    private Unsafe unsafe;

    private long timeOut;

    /**
     * 根节点
     */
    private volatile ThreadNode rootNode;

    /**
     * 描述当前锁的状态
     */
    private volatile int lockState = 0;

    /**
     * 节点偏移量
     */
    private long nodeOffset;

    /**
     * 锁状态偏移量
     */
    private long lockStateOffset;

    /**
     * 根节点偏移量
     */
    private long rootNodeOffset;


    public TimeOutLock() {
        this(0L);
    }

    public TimeOutLock(long timeOut) {
        this.timeOut = timeOut;
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            if (!unsafeField.isAccessible()) {
                unsafeField.setAccessible(true);
            }
            unsafe = (Unsafe) unsafeField.get(null);
            nodeOffset = unsafe.objectFieldOffset(ThreadNode.class.getDeclaredField("nextNode"));
            lockStateOffset = unsafe.objectFieldOffset(TimeOutLock.class.getDeclaredField("lockState"));
            rootNodeOffset = unsafe.objectFieldOffset(TimeOutLock.class.getDeclaredField("rootNode"));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void lock() {
        if (tryRunning() || addHeadNode()) {
            return;
        }
        while (true) {
            if (lockState == 1) {
                ThreadNode node = getLastNode(rootNode);
                if (node == null) {
                    if (addHeadNode()) {
                        return;
                    }
                } else if (casChangeLockStatus(1, 2)) {
                    ThreadNode newNode = new ThreadNode();
                    newNode.thread = Thread.currentThread();
                    node.nextNode = newNode;
                    absoluteCasChangeLockStatus(2, 1);
                    unsafe.park(false, 0L);
                    acquire(newNode);
                    return;

                }
            } else if (lockState == 0 && addHeadNode()) {
                return;
            }
        }

    }

    private void acquire(ThreadNode node) {
        if (rootNode != node) {
            absoluteCasChangeLockStatus(0, 1);
        }
    }

    /**
     * 添加头节点
     *
     * @return
     */
    private boolean addHeadNode() {
        if (lockState == 1) {
            if (rootNode == null && casChangeLockStatus(1, 2)) {
                ThreadNode node = new ThreadNode();
                node.thread = Thread.currentThread();
                rootNode = node;
                absoluteCasChangeLockStatus(2, 1);
                unsafe.park(false, 0L);
                acquire(node);
                return true;
            }
        } else if (lockState == 0) {
            return tryRunning();
        }
        return false;
    }

    /**
     * 首先尝试运行,不去初始化链表
     *
     * @return
     */
    private boolean tryRunning() {
        for (int i = 0; i < 3; i++) {
            if (rootNode == null && casChangeLockStatus(0, 1)) {
                monitor(Thread.currentThread());
                return true;
            }
        }
        return false;
    }

    public void unLock() {
        if (rootNode != null) {
            wakeUpFirstNode();
        }
    }

    public void show() {
        System.out.println(rootNode);
    }

    private void wakeUpFirstNode() {
        while (true) {
            if (lockState != 0) {
                ThreadNode headThreadNode = rootNode;
                if (casChangeLockStatus(1, 0)) {
                    while (true) {
                        if (casChangeNode(this, rootNodeOffset, rootNode, headThreadNode.nextNode)) {
                            unsafe.unpark(headThreadNode.thread);
                            headThreadNode.nextNode = null;
                            monitor(headThreadNode.thread);
                            return;
                        }
                    }
                }
            } else {
                return;
            }
        }
    }

    /**
     * 监视当前执行的线程是否超时,如果超时则打断执行
     */
    private void monitor(Thread currentRunningThread) {

        if (timeOut == 0L) {
            return;
        }
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(timeOut);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (currentRunningThread.isAlive()) {
                currentRunningThread.interrupt();
            }
        });
        thread.setName("monitor");
        thread.start();
    }

    /**
     * 使用cas替换对象
     *
     * @param node          操作的对象
     * @param offset        属性的偏移值
     * @param oldThreadNode 旧对象
     * @param newThreadNode 新对象
     * @return
     */
    private boolean casChangeNode(Object node, long offset, ThreadNode oldThreadNode, ThreadNode newThreadNode) {
        return unsafe.compareAndSwapObject(node, offset, oldThreadNode, newThreadNode);
    }

    /**
     * 使用cas替换锁状态
     *
     * @return
     */
    private boolean casChangeLockStatus(int oldStatus, int newStatus) {
        return unsafe.compareAndSwapInt(this, lockStateOffset, oldStatus, newStatus);
    }

    private void absoluteCasChangeLockStatus(int oldStatus, int newStatus) {
        while (true) {
            if (casChangeLockStatus(oldStatus, newStatus)) return;
        }
    }

    /**
     * 获取最后的一个节点
     *
     * @param node
     * @return
     */
    private ThreadNode getLastNode(ThreadNode node) {
        if (node != null) {
            if (node.nextNode != null) {
                return (getLastNode(node.nextNode));
            }
            return node;
        }
        return null;
    }

}

class ThreadNode {
    public Thread thread;

    public ThreadNode nextNode;

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public ThreadNode getNextNode() {
        return nextNode;
    }

    public void setNextNode(ThreadNode nextNode) {
        this.nextNode = nextNode;
    }

    @Override
    public String toString() {
        return "ThreadNode{" + "threadName=" + thread.getName() + ", nextNode=" + nextNode + '}';
    }
}
