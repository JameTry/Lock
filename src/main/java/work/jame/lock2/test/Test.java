package work.jame.lock2.test;

import work.jame.lock2.TimeOutLock;

/**
 * @author : Jame
 * @date : 2022-06-10 14:35
 **/
public class Test {

    private static int t = 0;

    public static void main(String[] args) throws InterruptedException {
        haveLock();
        //notLock();
    }


    public static void haveLock() {

        for (int oo = 0; oo < 20; oo++) {
            int finalO = oo;
            //正常添
            TimeOutLock lock = new TimeOutLock();
            for (int i = 0; i < 10; i++) {
                int finalI = i;
                Thread thread = new Thread(() -> {
                    //System.out.println(finalI);
                    lock.lock();
                    for (int i1 = 0; i1 < 10000; i1++) {
                        t++;
                    }
                    System.out.println("线程:"+Thread.currentThread().getName() + "===循环次数：" + finalO + "循环" + "结果" + t);
                    lock.unLock();
                });
                thread.setName(finalI + "");
                thread.start();
            }

            try {
                Thread.sleep(4000);
                System.out.println("\n\n");
                t = 0;
                lock.show();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //模拟10个超时的线程
//        for (int i = 0; i < 10; i++) {
//            Thread thread = new Thread(() -> {
//                lock.lock();
//                for (int j = 0; j < 999999999; j++) {
//                    for (int i1 = 0; i1 < 999999999; i1++) {
//                        for (int i2 = 0; i2 < 999999999; i2++) {
//                            i2 = i1 + j;
//                        }
//                    }
//                }
//                t++;
//                System.out.println(Thread.currentThread().getName() + "号线程执行完成");
//                lock.unLock();
//            });
//            thread.setName("timeOutThread");
//            thread.start();
//        }

    }

    public static void notLock() {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                //System.out.println(finalI);
                for (int i1 = 0; i1 < 10000; i1++) {
                    t++;
                }
                System.out.println(Thread.currentThread().getName() + "结果" + t);
            }).start();

        }
    }
}
