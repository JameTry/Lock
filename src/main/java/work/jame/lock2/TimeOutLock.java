package work.jame.lock2;

import com.sun.org.apache.bcel.internal.generic.ARETURN;
import com.sun.org.apache.xpath.internal.operations.Variable;
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
    private volatile LockStatus lockStatus = LockStatus.NOT_INIT;

    /**
     * 节点偏移量
     */
    private long nodeOffset;

    /**
     * 锁状态偏移量
     */
    private long lockStatusOffset;

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
            lockStatusOffset = unsafe.objectFieldOffset(TimeOutLock.class.getDeclaredField("lockStatus"));
            rootNodeOffset = unsafe.objectFieldOffset(TimeOutLock.class.getDeclaredField("rootNode"));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void lock() {
        if (tryRunning()) {
            return;
        }
        if (addHeadNode()) {
            return;
        }
        while (true) {
            if (lockStatus == LockStatus.RUNNING_INIT) {
                ThreadNode node = getLastNode(rootNode);
                if (node == null) {
                    addHeadNode();
                } else {
                    ThreadNode newNode = new ThreadNode();
                    newNode.thread = Thread.currentThread();
                    if (casAddNode(node, nodeOffset, node.nextNode, newNode)) {
                        unsafe.park(false, 0L);
                        break;
                    }
                }
            }
        }
    }

    private boolean addHeadNode() {
        if (lockStatus == LockStatus.RUNNING_NOT_INIT || lockStatus == LockStatus.NOT_INIT) {
            if (casChangeLockStatus(LockStatus.RUNNING_NOT_INIT, LockStatus.RUNNING_INIT) ||
                    casChangeLockStatus(LockStatus.NOT_INIT, LockStatus.RUNNING_INIT)) {
                ThreadNode node = new ThreadNode();
                node.thread = Thread.currentThread();
                if (casAddNode(this, rootNodeOffset, rootNode, node)) {
                    if (lockStatus == LockStatus.NOT_INIT) {
                        if (tryRunning()) {
                            return true;
                        }
                    }
                    unsafe.park(false, 0L);
                    return true;
                }

            }
        }
        return false;
    }

    /**
     * 首先尝试运行,不去初始化链表
     *
     * @return
     */
    public boolean tryRunning() {

        boolean running;
        for (int i = 0; i < 3; i++) {
            if (lockStatus == LockStatus.RUNNING_NOT_INIT || lockStatus == LockStatus.RUNNING_INIT) {
                continue;
            }
            running = casChangeLockStatus(LockStatus.NOT_INIT, LockStatus.RUNNING_NOT_INIT);
            if (running) {
                monitor(Thread.currentThread());
                return true;
            }
        }
        return false;
    }

    public void unLock() {
        while (true) {
            if (lockStatus == LockStatus.RUNNING_NOT_INIT) {
                if (casChangeLockStatus(LockStatus.RUNNING_NOT_INIT, LockStatus.NOT_INIT)) {
                    wakeUpFirstNode();
                    return;
                }
            } else if (lockStatus == LockStatus.RUNNING_INIT) {
                if (rootNode == null) {
                    if (casChangeLockStatus(LockStatus.RUNNING_INIT, LockStatus.NOT_INIT)) {
                        return;
                    }
                } else {
                    wakeUpFirstNode();
                    return;
                }

            }
        }
    }

    private void wakeUpFirstNode() {
        if (rootNode != null) {
            ThreadNode headThreadNode = rootNode;
            // if (casAddNode(this, rootNodeOffset, rootNode, headThreadNode.nextNode)) {
            rootNode = headThreadNode.nextNode;
            monitor(headThreadNode.thread);
            unsafe.unpark(headThreadNode.thread);
            headThreadNode.thread = null;
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
    private boolean casAddNode(Object node, long offset, ThreadNode oldThreadNode, ThreadNode newThreadNode) {
        return unsafe.compareAndSwapObject(node, offset, oldThreadNode, newThreadNode);
    }

    /**
     * 使用cas替换锁状态
     *
     * @return
     */
    private boolean casChangeLockStatus(LockStatus oldStatus, LockStatus newStatus) {
        return unsafe.compareAndSwapObject(this, lockStatusOffset, oldStatus, newStatus);
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
        return "ThreadNode{" +
                "threadName=" + thread.getName() +
                ", nextNode=" + nextNode +
                '}';
    }
}
