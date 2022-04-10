package cn.chuanwise.command;

/**
 * 具备优先级的某种东西
 *
 * @author Chuanwise
 */
public interface Prioritized {
    
    /**
     * 获取优先级
     *
     * @return 优先级
     */
    Priority getPriority();
}
