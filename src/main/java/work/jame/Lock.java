package work.jame;


import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : Jame
 * @date : 2022-06-10 14:12
 **/
public class Lock {

    private final Object object = new Object();
    private static final Logger log = Logger.getLogger(Lock.class);

    private HashMap<String, ThreadEntity> threadMap;

    private Thread mainThread;

    /**
     * 存放所有添加的任务
     */
    private List<Task> taskList;

    /**
     * 任务数量
     */
    private int taskNumber;

    /**
     * 等待时间
     */
    private long waitTime = -1;

    private Lock() {

    }


    public Lock(int taskNumber) {
        if (taskNumber < 1) {
            throw new RuntimeException("初始化线程数量不能小于1");
        }
        this.taskNumber = taskNumber;
        this.taskList = new ArrayList<>(taskNumber);
        this.threadMap = new HashMap<>(taskNumber);
    }

    public Lock(int taskNumber, long waitTime) {
        if (taskNumber < 1) {
            throw new RuntimeException("初始化线程数量不能小于1");
        } else if (waitTime < 1) {
            throw new RuntimeException("等待时间不能小于1");
        }
        this.taskNumber = taskNumber;
        this.waitTime = waitTime;
        this.threadMap = new HashMap<>(taskNumber);
        this.taskList = new ArrayList<>(taskNumber);
        this.mainThread = Thread.currentThread();
    }


    public synchronized void addTask(Task task) {
        taskList.add(task);
        if (taskList.size() == taskNumber) {
            log.debug("所有线程都准备好了");
            final AtomicInteger finishedNumber = new AtomicInteger(0);
            if (waitTime == -1) {
                startTask(() -> {
                    if (finishedNumber.incrementAndGet() == taskNumber) {
                        wakeUpAllByNumber();
                    }
                });
                waitByNumber();
            } else {
                startTask(() -> {
                    if (finishedNumber.incrementAndGet() == taskNumber) {
                        wakeUpByTime();
                    }

                });
                waitByTime();
            }
        }
    }

    private void startTask(TaskFinishedPostProcess taskFinishedPostProcess) {
        for (int i = 0; i < taskNumber; i++) {
            String threadName = Integer.toString(i);
            Task task = taskList.get(i);
            Thread thread = new Thread(() -> {
                try {
                    task.run();
                    ThreadEntity threadEntity = threadMap.get(threadName);
                    threadEntity.setEndTime(System.currentTimeMillis());
                    threadEntity.setFinished(true);
                    taskFinishedPostProcess.postProcess();
                } catch (InterruptedException e) {

                    /**
                     * 疑问:为什么这里有时候放不进去?
                     * 就是threadEntity的endTime还是null
                     * 当换成ConcurrentHashMap时情况明显变少了,但是还是会有
                     * --解决--
                     * 因为主线程打断完后这个线程不一定能在主线程后打断后立刻执行
                     * 导致这个endTime还是为null,至于为什么换成ConcurrentHashMap就变少了的情况还需要研究研究
                     */
                    ThreadEntity threadEntity = threadMap.get(threadName);
                    threadEntity.setEndTime(System.currentTimeMillis());
                    //这个一定要放到所有流程的最后面,因为它决定了当前线程打断后操作是否完成
                    threadEntity.setFinished(true);
                    //threadMap.put(threadName, threadEntity);
                    //throw new RuntimeException(e);

                }
            });
            thread.setName(threadName);
            threadMap.put(threadName, new ThreadEntity(thread));
            thread.start();
        }
        log.debug("所有线程都正在运行");
    }

    private void waitByTime() {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            log.debug("所有任务提前结束");
            //throw new RuntimeException(e);
        }
        for (Map.Entry<String, ThreadEntity> entry : threadMap.entrySet()) {
            StringBuilder builder = new StringBuilder();
            ThreadEntity threadEntity = entry.getValue();

            if (!threadEntity.getFinished()) {
                threadEntity.getThread().interrupt();
                while (true) {
                    if (threadEntity.getFinished()) {
                        break;
                    }
                }
                builder.append(threadEntity.getThread().getName()).append("号线程被打断");
            } else {
                builder.append(threadEntity.getThread().getName()).append("号线程执行完成");
            }
            builder.append(",开始时间:")
                    .append(TimeUtil.timestampCastStringTime(threadEntity.getStartTime()))
                    .append(",结束时间:").append(TimeUtil.timestampCastStringTime(threadEntity.getEndTime()));
            log.debug(builder.toString());

        }
    }

    private void waitByNumber() {
        synchronized (object) {
            try {
                object.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void wakeUpAllByNumber() {
        synchronized (object) {
            object.notify();
        }
    }

    private void wakeUpByTime() {
        mainThread.interrupt();
    }

}
