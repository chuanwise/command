package cn.chuanwise.commandlib.completer;

import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

import java.util.Collections;
import java.util.Set;

@Data
public class FixedCompleter
        implements Completer {

    protected final Set<String> values;

    public FixedCompleter(Set<String> values) {
        Preconditions.argumentNonNull(values, "values");

        this.values = Collections.unmodifiableSet(values);
    }

    @Override
    public Set<String> complete(CompleteContext context) throws Exception {
        return values;
    }
}
