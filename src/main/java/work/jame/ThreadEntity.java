package work.jame;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author : Jame
 * @date : 2022-06-10 14:05
 **/
public class ThreadEntity {
    private Thread thread;
    private Boolean finish = false;
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

    public Boolean getFinish() {
        return finish;
    }

    public void setFinish(Boolean finish) {
        this.finish = finish;
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
