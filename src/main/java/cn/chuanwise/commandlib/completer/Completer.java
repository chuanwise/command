package cn.chuanwise.commandlib.completer;

import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.function.ExceptionFunction;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

import java.util.Collections;
import java.util.Set;

@Data
public abstract class Completer {

    protected final Class<?> completedClass;

    public Completer(Class<?> completedClass) {
        Preconditions.argumentNonNull(completedClass, "completed class");

        this.completedClass = completedClass;
    }

    public static Completer of(String value) {
        return of(Collections.singleton(value));
    }

    public static Completer of(Set<String> values) {
        return new FixedCompleter(values);
    }

    public static <T> Completer of(Class<T> completedClass, ExceptionFunction<CompleteContext, Set<String>> function) {
        Preconditions.argumentNonNull(completedClass, "completed class");
        Preconditions.argumentNonNull(function, "function");

        return new Completer(completedClass) {
            @Override
            public Set<String> complete(CompleteContext context) throws Exception {
                return function.exceptApply(context);
            }
        };
    }

    public abstract Set<String> complete(CompleteContext context) throws Exception;
}
