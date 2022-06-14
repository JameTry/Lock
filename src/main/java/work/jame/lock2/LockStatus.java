package work.jame.lock2;

/**
 * @author : Jame
 * @date : 2022-06-12 11:00
 **/
public enum LockStatus {
    /**
     * 未初始化根节点
     */
    NOT_INIT,
    /**
     * 当前有运行的线程,根节点已经初始化
     */
    INIT,
    /**
     * 当前有运行的线程,根节点未经初始化
     */
    RUNNING_NOT_INIT,
    ADDING

}
