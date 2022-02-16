package cn.chuanwise.commandlib.util;

import cn.chuanwise.commandlib.command.OptionInfo;
import cn.chuanwise.commandlib.configuration.CommandLibConfiguration;
import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.StaticUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Options
        extends StaticUtil {

    public static Set<String> complete(CommandLibConfiguration.Option option, OptionInfo optionInfo, Collection<String> values) {
        Preconditions.argumentNonNull(optionInfo, "option info");
        Preconditions.argumentNonNull(values, "values");

        if (values.isEmpty()) {
            return Collections.emptySet();
        }

        final Set<String> set = new HashSet<>();
        final String namePrefix = option.getPrefix() + optionInfo.getName();
        final String nameAssignPrefix = namePrefix + option.getSplitter();

        for (String value : values) {
            // name
            if (optionInfo.hasDefaultValue()) {
                set.add(namePrefix);
            }
            set.add(nameAssignPrefix + value);

            // aliases
            for (String alias : optionInfo.getAliases()) {
                final String aliasPrefix = option.getPrefix() + alias;
                final String aliasAssignPrefix = aliasPrefix + option.getSplitter();

                set.add(aliasAssignPrefix + value);
                if (optionInfo.hasDefaultValue()) {
                    set.add(aliasPrefix);
                }
            }
        }

        return Collections.unmodifiableSet(set);
    }
}
