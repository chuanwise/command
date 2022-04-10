package cn.chuanwise.command.util;

import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.StaticUtilities;
import cn.chuanwise.common.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 参数相关工具
 *
 * @author Chuanwise
 */
public class Arguments
        extends StaticUtilities {

    public static List<String> split(String text, char escapeChar) {
        return split(text, escapeChar, false);
    }

    public static List<String> split(String text, boolean includeDoubleQuotes) {
        return split(text, '\\', includeDoubleQuotes);
    }

    public static List<String> split(String text) {
        return split(text, '\\');
    }

    public static List<String> split(String text, char escapeChar, boolean includeDoubleQuotes) {
        Preconditions.namedArgumentNonNull(text, "text");

        if (text.isEmpty()) {
            return Collections.emptyList();
        }

        final StringBuilder stringBuilder = new StringBuilder();
        final List<String> arguments = new ArrayList<>();

        boolean escaped = false;
        boolean inDoubleQuote = false;
        boolean space = false;

        for (int i = 0; i < text.length(); i++) {
            final char ch = text.charAt(i);

            if (escaped) {
                stringBuilder.append(ch);
                escaped = false;
                continue;
            }
            if (ch == escapeChar) {
                escaped = true;
                continue;
            }

            if (ch == '\"') {
                inDoubleQuote = !inDoubleQuote;
                space = false;

                if (includeDoubleQuotes) {
                    stringBuilder.append('\"');
                }
                continue;
            }

            if (inDoubleQuote || ch != ' ') {
                space = false;
                stringBuilder.append(ch);
                continue;
            }
            if (!space) {
                space = true;
                final String argument = stringBuilder.toString();
                arguments.add(argument);
                stringBuilder.setLength(0);
            }
        }

        if (stringBuilder.length() > 0) {
            arguments.add(stringBuilder.toString());
        }

        return Collections.unmodifiableList(arguments);
    }

    public static String merge(String[] arguments) {
        Preconditions.namedArgumentNonNull(arguments, "arguments");

        if (arguments.length == 0) {
            return "";
        } else if (arguments.length == 1) {
            return arguments[0];
        } else {
            final StringBuilder stringBuilder = new StringBuilder(arguments[0]);
            for (int i = 1; i < arguments.length; i++) {
                final String argument = arguments[i];
                if (Strings.isEmpty(argument)) {
                    stringBuilder.append(" ");
                } else {
                    stringBuilder.append(" ").append(argument);
                }
            }
            return stringBuilder.toString();
        }
    }

    public static String serialize(String text) {
        if (text.contains(" ") || text.contains("\"")) {
            final StringBuilder stringBuilder = new StringBuilder(text.length());

            stringBuilder.append("\"");
            for (int i = 0; i < text.length(); i++) {
                final char ch = text.charAt(i);
                switch (ch) {
                    case '\"':
                        stringBuilder.append("\\\"");
                        break;
                    default:
                        stringBuilder.append(ch);
                }
            }
            stringBuilder.append("\"");
            return stringBuilder.toString();
        } else {
            return text;
        }
    }

    public static String deserialize(String text) {
        if (!text.startsWith("\"") || !text.endsWith("\"")) {
            return text;
        }

        final int smallerLength = text.length() - 1;
        final StringBuilder stringBuilder = new StringBuilder(text.length());
        boolean translated = false;

        for (int i = 1; i < smallerLength; i++) {
            final char ch = text.charAt(i);

            if (translated) {
                stringBuilder.append(ch);
                translated = false;
                continue;
            }

            switch (ch) {
                case '\\':
                    translated = true;
                    break;
                default:
                    stringBuilder.append(ch);
            }
        }
        return stringBuilder.toString();
    }
}
