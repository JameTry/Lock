package work.jame.lock1.test;

import work.jame.lock1.MultitaskTimeOutLock;
import work.jame.lock2.TimeOutLock;

/**
 * @author : Jame
 * @date : 2022-06-10 14:35
 **/
public class Test {


    public static void main(String[] args) throws InterruptedException {
        t1();
        //t2();
        //t3();
;
    }

    /**
     * 设置任务数量3个,最多等待3秒
     * 如果其他线程不到3秒就都走完了,那么主线程也会走,不会傻等3秒
     */
    public static void t1() {
        long startTime = System.currentTimeMillis();   //获取开始时间
        System.out.println("根据等待时间的锁");

        MultitaskTimeOutLock multitaskTimeOutLock = new MultitaskTimeOutLock(3, 3000);

        for (int i = 0; i < 3; i++) {
            multitaskTimeOutLock.addTask(() -> {
                //测试等待时间很充足的情况
                Thread.sleep(300);
                System.out.println(Thread.currentThread().getName() + "号线程完成");
            });
        }
        System.out.println("主线程:你给路达哟");
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("主线程运行时间： " + (endTime - startTime) + "ms");
    }


    /**
     * 设置任务数量3个,最多等待3秒
     * 3秒一到,无如果其他线程还没有完成那么主线程都会继续走
     */
    public static void t2() {
        long startTime = System.currentTimeMillis();   //获取开始时间
        System.out.println("根据等待时间的锁");

        MultitaskTimeOutLock multitaskTimeOutLock = new MultitaskTimeOutLock(3, 3000);

        for (int i = 0; i < 3; i++) {
            multitaskTimeOutLock.addTask(() -> {
                int sleepTime = (int) (Math.random() * 7000);
                System.out.println(Thread.currentThread().getName() + "号线程休眠时间" + sleepTime);
                //测试等待时间不够其他线程执行的情况
                Thread.sleep(sleepTime);
                System.out.println(Thread.currentThread().getName() + "号线程完成");
            });
        }
        System.out.println("主线程:你给路达哟");
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("主线程运行时间： " + (endTime - startTime) + "ms");
    }


    /**
     * 无限等,直到三个都走完
     */
    public static void t3() {
        long startTime = System.currentTimeMillis();   //获取开始时间
        System.out.println("根据完成线程数量的锁");
        MultitaskTimeOutLock multitaskTimeOutLock = new MultitaskTimeOutLock(3);
        for (int i = 0; i < 3; i++) {
            multitaskTimeOutLock.addTask(() -> {
                int sleepTime = (int) (Math.random() * 5000);
                System.out.println(Thread.currentThread().getName() + "号线程休眠时间" + sleepTime);
                Thread.sleep(sleepTime);
                System.out.println(Thread.currentThread().getName() + "号线程完成");
            });
        }
        System.out.println("主线程:你给路达哟");
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("主线程运行时间： " + (endTime - startTime) + "ms");
    }
}
