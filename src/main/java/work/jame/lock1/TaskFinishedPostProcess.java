package work.jame.lock1;

/**
 * @author : Jame
 * @date : 2022-06-10 16:26
 **/
@FunctionalInterface
public interface TaskFinishedPostProcess {

    /**
     * 线程执行完的后置处理
     */
    void postProcess();

}
