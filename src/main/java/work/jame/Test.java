package work.jame;

/**
 * @author : Jame
 * @date : 2022-06-10 14:35
 **/
public class Test {
    public static void main(String[] args) {
        t1();
        //t2();

    }

    public static void t1() {
        System.out.println("根据等待时间的锁");
        Lock lock = new Lock(3, 3000);
        for (int i = 0; i < 3; i++) {
            lock.addTask(() -> {
                int sleepTime = (int) (Math.random() * 5000);
                System.out.println(Thread.currentThread().getName() + "号线程休眠时间" + sleepTime);
                Thread.sleep(sleepTime);
                System.out.println(Thread.currentThread().getName() + "号线程完成");
            });
        }

        System.out.println("主线程:你给路达哟");
    }

    public static void t2() {
        System.out.println("根据完成线程数量的锁");
        Lock lock = new Lock(3);
        for (int i = 0; i < 3; i++) {
            lock.addTask(() -> {
                int sleepTime = (int) (Math.random() * 5000);
                System.out.println(Thread.currentThread().getName() + "号线程休眠时间" + sleepTime);
                Thread.sleep(sleepTime);
                System.out.println(Thread.currentThread().getName() + "号线程完成");
            });
        }

        System.out.println("主线程:你给路达哟");
    }
}
