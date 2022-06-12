package work.jame.lock1;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author : Jame
 * @date : 2022-06-10 14:05
 **/
public class ThreadEntity {
    private Thread thread;
    /**
     * 当线程正常执行-表示是否完成
     * 当线程被打断-表示被打断后流程是否完成
     */
    private Boolean finished = false;
    private Long startTime = System.currentTimeMillis();
    private Long endTime;

    public ThreadEntity(Thread thread) {
        this.thread = thread;

    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
