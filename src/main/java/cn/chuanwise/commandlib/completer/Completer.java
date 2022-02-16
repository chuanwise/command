package cn.chuanwise.commandlib.completer;

import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.function.ExceptionFunction;
import cn.chuanwise.util.Preconditions;

import java.util.Collections;
import java.util.Set;

@FunctionalInterface
public interface Completer {
    Set<String> complete(CompleteContext context) throws Exception;

    static Completer of(String value) {
        return of(Collections.singleton(value));
    }

    static Completer of(Set<String> values) {
        return new FixedCompleter(values);
    }

    static <T> Completer of(Class<T> completedClass, ExceptionFunction<CompleteContext, Set<String>> function) {
        Preconditions.argumentNonNull(completedClass, "completed class");
        Preconditions.argumentNonNull(function, "function");

        return new SimpleCompleter(completedClass) {
            @Override
            protected Set<String> complete0(CompleteContext context) throws Exception {
                return function.exceptApply(context);
            }
        };
    }

    static Completer empty() {
        return EmptyCompleter.getInstance();
    }
}
