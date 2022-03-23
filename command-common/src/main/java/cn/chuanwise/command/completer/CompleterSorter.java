package cn.chuanwise.command.completer;

import cn.chuanwise.common.algorithm.LongestCommonSubstring;
import cn.chuanwise.common.util.Strings;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface CompleterSorter {
    List<String> sort(Set<String> completerSet, String uncompletedPart);

    CompleterSorter LONGEST_COMMON_SUBSTRING = (set, uncompletedPart) -> {
        // 如果是新的，则直接字典序
        if (Strings.isEmpty(uncompletedPart)) {
            return Collections.unmodifiableList(
                    set.stream()
                            .sorted()
                            .collect(Collectors.toList())
            );
        }

        return Collections.unmodifiableList(
                set.stream()
                        .sorted((left, right) -> {
                            final int leftPriority = LongestCommonSubstring.length(uncompletedPart, left);
                            final int rightPriority = LongestCommonSubstring.length(uncompletedPart, right);

                            final int substringCompare = Integer.compare(leftPriority, rightPriority);
                            if (substringCompare != 0) {
                                return -substringCompare;
                            }

                            return -left.compareTo(right);
                        })
                        .collect(Collectors.toList())
        );
    };
}