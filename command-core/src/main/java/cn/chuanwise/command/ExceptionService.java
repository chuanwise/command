package cn.chuanwise.command;

import cn.chuanwise.command.exception.ExceptionHandler;
import cn.chuanwise.command.exception.SimpleExceptionHandler;
import cn.chuanwise.command.object.AbstractCommanderObject;
import cn.chuanwise.common.api.ExceptionConsumer;
import cn.chuanwise.common.api.ExceptionRunnable;
import cn.chuanwise.common.api.ExceptionSupplier;
import cn.chuanwise.common.util.Maps;
import cn.chuanwise.common.util.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExceptionService
    extends AbstractCommanderObject {
    
    /**
     * 异常处理器
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected final Map<Priority, List<ExceptionHandler>> exceptionHandlers = new HashMap<>();
    
    public ExceptionService(Commander commander) {
        super(commander);
    }
    
    /**
     * 处理某个异常
     *
     * @param cause 异常
     * @return 该异常是否被处理
     */
    public boolean handleException(Throwable cause) {
        Preconditions.namedArgumentNonNull(cause, "cause");
        
        final boolean highest = handleException0(Priority.HIGHEST, cause);
        final boolean high = handleException0(Priority.HIGH, cause);
        final boolean normal = handleException0(Priority.NORMAL, cause);
        final boolean low = handleException0(Priority.LOW, cause);
        final boolean lowest = handleException0(Priority.LOWEST, cause);
        
        return highest || high || normal || low || lowest;
    }
    
    /**
     * 调用特定优先级的异常处理器处理某个异常
     *
     * @param priority 优先级
     * @param cause    异常
     * @return 该异常是否被处理
     */
    private boolean handleException0(Priority priority, Throwable cause) {
        final List<ExceptionHandler> exceptionHandlers = this.exceptionHandlers.get(priority);
        
        if (cn.chuanwise.common.util.Collections.nonEmpty(exceptionHandlers)) {
            for (ExceptionHandler exceptionHandler : exceptionHandlers) {
                try {
                    if (exceptionHandler.handleException(cause)) {
                        return true;
                    }
                } catch (Throwable throwable) {
                    cause = throwable;
                }
            }
        }
        
        return false;
    }
    
    public void registerExceptionHandler(ExceptionHandler exceptionHandler) {
        registerExceptionHandler(exceptionHandler, Priority.NORMAL);
    }
    
    /**
     * 注册一个异常处理器
     *
     * @param exceptionHandler 异常处理器
     */
    public void registerExceptionHandler(ExceptionHandler exceptionHandler, Priority priority) {
        Preconditions.namedArgumentNonNull(exceptionHandler, "exception handler");
        Preconditions.namedArgumentNonNull(priority, "priority");
        
        final List<ExceptionHandler> exceptionHandlers = Maps.getOrPutGet(this.exceptionHandlers, priority, CopyOnWriteArrayList::new);
        exceptionHandlers.add(exceptionHandler);
    }
    
    /**
     * 注册一个异常处理器
     *
     * @param exceptionClass 异常类型
     * @param priority       优先级
     * @param action         处理行为
     * @param <T>            异常类型
     * @return 异常处理器
     */
    public <T extends Throwable> ExceptionHandler registerExceptionHandler(Class<T> exceptionClass, Priority priority, ExceptionConsumer<T> action) {
        Preconditions.namedArgumentNonNull(exceptionClass, "exception class");
        Preconditions.namedArgumentNonNull(priority, "priority");
        Preconditions.namedArgumentNonNull(action, "action");
        
        final List<ExceptionHandler> exceptionHandlers = Maps.getOrPutGet(this.exceptionHandlers, priority, CopyOnWriteArrayList::new);
        final SimpleExceptionHandler<T> exceptionHandler = new SimpleExceptionHandler<>(exceptionClass, action);
        exceptionHandlers.add(exceptionHandler);
        
        return exceptionHandler;
    }
    
    /**
     * 注册一个异常处理器
     *
     * @param exceptionClass 异常类型
     * @param action         处理行为
     * @param <T>            异常类型
     * @return 异常处理器
     */
    public <T extends Throwable> ExceptionHandler registerExceptionHandler(Class<T> exceptionClass, ExceptionConsumer<T> action) {
        return registerExceptionHandler(exceptionClass, Priority.NORMAL, action);
    }
    
    /**
     * 注销一个异常处理器
     *
     * @param exceptionHandler 异常处理器
     * @return 是否注销该异常处理器
     */
    public boolean unregisterExceptionHandler(ExceptionHandler exceptionHandler) {
        Preconditions.namedArgumentNonNull(exceptionHandler, "exception handler");
    
        final boolean highest = unregisterExceptionHandler(Priority.HIGHEST, exceptionHandler);
        final boolean high = unregisterExceptionHandler(Priority.HIGH, exceptionHandler);
        final boolean normal = unregisterExceptionHandler(Priority.NORMAL, exceptionHandler);
        final boolean low = unregisterExceptionHandler(Priority.LOW, exceptionHandler);
        final boolean lowest = unregisterExceptionHandler(Priority.LOWEST, exceptionHandler);
        
        return highest || high || normal || low || lowest;
    }
    
    /**
     * 注销一个指定优先级的异常处理器
     *
     * @param priority         优先级
     * @param exceptionHandler 异常处理器
     * @return 是否注销该异常处理器
     */
    private boolean unregisterExceptionHandler(Priority priority, ExceptionHandler exceptionHandler) {
        final List<ExceptionHandler> exceptionHandlers = this.exceptionHandlers.get(priority);
        
        boolean removed;
        if (cn.chuanwise.common.util.Collections.nonEmpty(exceptionHandlers)) {
            removed = exceptionHandlers.remove(exceptionHandler);
            if (exceptionHandlers.isEmpty()) {
                this.exceptionHandlers.remove(priority);
            }
        } else {
            removed = false;
        }
        
        return removed;
    }
    
    
    /**
     * 运行一段可能抛出异常的代码，并用已注册的异常处理器处理。
     *
     * @param action 可能抛出异常的代码
     */
    public void catching(ExceptionRunnable action) {
        Preconditions.namedArgumentNonNull(action, "action");
        
        try {
            action.exceptRun();
        } catch (Throwable cause) {
            handleException(cause);
        }
    }
    
    /**
     * 运行一段可能抛出异常的代码，并用已注册的异常处理器处理。
     *
     * @param action       可能抛出异常的代码
     * @param defaultValue 抛出异常后，代码的返回值
     * @return 代码的返回值
     */
    public <T> T catching(ExceptionSupplier<T> action, T defaultValue) {
        Preconditions.namedArgumentNonNull(action, "action");
        
        try {
            return action.exceptGet();
        } catch (Throwable cause) {
            handleException(cause);
            return defaultValue;
        }
    }
    
    /**
     * 运行一段可能抛出异常的代码，并用已注册的异常处理器处理。
     *
     * @param action 可能抛出异常的代码
     * @return 代码的返回值。抛出异常时返回 null
     */
    public <T> T catching(ExceptionSupplier<T> action) {
        return catching(action, null);
    }
}
