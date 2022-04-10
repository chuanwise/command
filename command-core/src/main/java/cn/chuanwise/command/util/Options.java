package cn.chuanwise.command.util;

import cn.chuanwise.command.command.OptionInfo;
import cn.chuanwise.command.configuration.CommanderConfiguration;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.StaticUtilities;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 选项相关工具
 *
 * @author Chuanwise
 */
public class Options
    extends StaticUtilities {

    public static Set<String> complete(CommanderConfiguration.Option option, OptionInfo optionInfo, Collection<String> values) {
        Preconditions.namedArgumentNonNull(optionInfo, "option info");
        Preconditions.namedArgumentNonNull(values, "values");

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
