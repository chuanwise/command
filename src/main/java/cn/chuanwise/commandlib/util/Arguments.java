package cn.chuanwise.commandlib.util;

import cn.chuanwise.util.Preconditions;
import cn.chuanwise.util.StaticUtil;
import cn.chuanwise.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class Arguments
        extends StaticUtil {

    public static List<String> split(String text, char escapeChar) {
        Preconditions.argumentNonNull(text, "text");

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

    public static List<String> split(String text) {
        return split(text, '\\');
    }

    public static String merge(String[] arguments) {
        Preconditions.argumentNonNull(arguments, "arguments");

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
        if (text.contains(" ")) {
            return "\"" + text.replaceAll(Pattern.quote("\\"), "\\\\").replaceAll(Pattern.quote("\""), "\\\"") + "\"";
        } else {
            return text;
        }
    }

    public static String deserialize(String text) {
        if (!text.startsWith("\"") || !text.endsWith("\"")) {
            return text;
        }

        text = text.substring(1, text.length() - 1);
        return text.replaceAll(Pattern.quote("\\\""), "\"")
                .replaceAll(Pattern.quote("\\\\"), "\\");
    }
}
