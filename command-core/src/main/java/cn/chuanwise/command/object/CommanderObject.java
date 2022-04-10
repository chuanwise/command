package cn.chuanwise.command.object;

import cn.chuanwise.command.Commander;

/**
 * 有关 {@link Commander} 的对象
 */
public interface CommanderObject {
    
    /**
     * 获取 {@link Commander}
     * @return 和当前对象相关的 {@link Commander}
     */
    Commander getCommander();
}
