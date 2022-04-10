package cn.chuanwise.command.handler;

import cn.chuanwise.command.context.CompleteContext;
import cn.chuanwise.command.context.ParseContext;
import cn.chuanwise.common.space.Container;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 枚举类的补全器和解析器
 *
 * @param <T> 枚举类型
 * @author Chuanwise
 */
@Data
@SuppressWarnings("all")
public class EnumClassHandler<T extends Enum<T>>
    extends AbstractClassHandler<T> {
    
    /**
     * 枚举类型常量数组
     */
    protected final T[] values;

    /**
     * 枚举类型元素名集合，用于补全。
     */
    protected final Set<String> complete;

    /**
     * 通过枚举类型对象构造指定枚举类型处理器。
     *
     * @param enumClass 指定枚举类型对象
     * @throws IllegalArgumentException enumClass 为 null 时
     */
    public EnumClassHandler(Class<T> enumClass) {
        super(enumClass);
        
        this.values = enumClass.getEnumConstants();
        this.complete = Collections.unmodifiableSet(Arrays.stream(values)
                .map(Objects::toString)
                .collect(Collectors.toSet()));
    }

    /**
     * 自动从本类的类型参数读取枚举类型，并构造类型处理器。
     *
     * @throws IllegalStateException 无法通过现有信息获得泛型参数类型，必须显示提供时
     */
    @SuppressWarnings("all")
    public EnumClassHandler() {
        this.values = handledClass.getEnumConstants();
        this.complete = Collections.unmodifiableSet(Arrays.stream(values)
                .map(Objects::toString)
                .collect(Collectors.toSet()));
    }

    @Override
    protected Set<String> complete0(CompleteContext context) throws Exception {
        return complete;
    }

    @Override
    protected Container<T> parse0(ParseContext context) throws Exception {
        return Container.ofNonNull(cn.chuanwise.common.util.Arrays.firstIf(values, x -> Objects.equals(context.getParsingReferenceInfo().getString(), x.toString())));
    }
}