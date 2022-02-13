package cn.chuanwise.commandlib.command;

import cn.chuanwise.commandlib.annotation.Format;
import cn.chuanwise.util.*;
import cn.chuanwise.util.Arrays;
import cn.chuanwise.util.Collections;
import lombok.Data;

import java.util.*;
import java.util.regex.Pattern;

@Data
@SuppressWarnings("all")
public class FormatInfo {
    public static final char SPACE = ' ';

    public static final char OR_OPERATOR = '|';

    public static final char DECIARE_PREFIX = '[';
    public static final char DECIARE_SUFFIX = ']';

    public static final char DEFAULT_OPERATOR = '?';
    public static final char OPTION_OPERATOR = '=';

    public static final char REMAIN_SUFFIX = '~';

    protected final String string;
    protected final Element[] elements;

    public abstract static class Element {
        @Data
        public static class PlainTexts extends Element {
            protected final List<String> texts;

            @Override
            public String getCompletedUsage() {
                return CollectionUtil.toString(texts, "|");
            }

            @Override
            public String getSimpleUsage() {
                return texts.get(0);
            }
        }

        @Data
        public static abstract class Reference extends Element {
            protected final String name;

            @Data
            public static class Simple extends Reference {
                public Simple(String name) {
                    super(name);
                }

                @Override
                public String getCompletedUsage() {
                    return "[" + name + "]";
                }

                @Override
                public String getSimpleUsage() {
                    return "[" + name + "]";
                }
            }

            @Data
            public static abstract class Remain extends Reference {
                public Remain(String name) {
                    super(name);
                }

                @Data
                public static class Nullable extends Remain {
                    protected final String defaultValue;

                    public Nullable(String name, String defaultValue) {
                        super(name);
                        this.defaultValue = defaultValue;
                    }

                    @Override
                    public String getCompletedUsage() {
                        return "[" + name + "?" + defaultValue + "~]";
                    }

                    @Override
                    public String getSimpleUsage() {
                        return "[" + name + "]";
                    }
                }

                @Data
                public static class NonNull extends Remain {
                    public NonNull(String name) {
                        super(name);
                    }

                    @Override
                    public String getCompletedUsage() {
                        return "[" + name + "~]";
                    }

                    @Override
                    public String getSimpleUsage() {
                        return getCompletedUsage();
                    }
                }
            }

            @Data
            public static class Option extends Reference {
                protected final Set<String> aliases;
                protected final String defaultValue;
                protected final Set<String> optionalValues;

                public Option(String name, Set<String> aliases, String defaultValue, Set<String> optionalValues) {
                    super(name);
                    this.aliases = aliases;
                    this.defaultValue = defaultValue;
                    this.optionalValues = optionalValues;
                }

                @Override
                public String getCompletedUsage() {
                    if (Collections.isEmpty(optionalValues)) {
                        return "[" + name + "=]";
                    } else {
                        return "[" + name + "=" + String.join("|", optionalValues) + "]";
                    }
                }

                @Override
                public String getSimpleUsage() {
                    return "[" + name + "=]";
                }
            }
        }

        public abstract String getCompletedUsage();

        public abstract String getSimpleUsage();
    }

    private enum CompileState {
        DEFAULT,
        PLAIN_TEXT,
        DECLARE,
        SIMPLE_PARAMETER_DECLARE,
        NULLABLE_PARAMETER_DECLARE,
        OPTION_DECLARE,
        OPTION_DEFAULT_DECLARE,
    }

    /**
     * 编译格式字符串
     *
     * @param configuration 编译设置
     * @param text          格式字符串
     * @return 编译后的格式字符串
     * @throws ForkCompileException 语法错误
     * @see Format#value()
     */
    public static FormatInfo compile(String text) {
        Preconditions.argument(StringUtil.notEmpty(text), "格式字符串为空！");

        // 结尾添加一个空格方便编译
        final String originalText = text;
        text = text + ' ';
        final int length = text.length();


        CompileState state = CompileState.DEFAULT;
        final StringBuilder stringBuffer1 = new StringBuilder();
        final StringBuilder stringBuffer2 = new StringBuilder();
        final StringBuilder stringBuffer3 = new StringBuilder();

        final List<Element> elementList = new ArrayList<>();
        final Map<String, Element> environment = new HashMap();
        boolean afterDefaultOperator;

        for (int i = 0; i < length; i++) {
            final char ch = text.charAt(i);

            switch (state) {
                case DEFAULT: {
                    if (Character.isSpaceChar(ch)) {
                        continue;
                    }
                    check(ch != OR_OPERATOR, originalText, i, "或运算符不能用于格式串元素开头！");
                    if (ch == DECIARE_PREFIX) {
                        state = CompileState.DECLARE;
                        continue;
                    } else {
                        stringBuffer1.append(ch);
                        state = CompileState.PLAIN_TEXT;
                        continue;
                    }
                }
                case PLAIN_TEXT: {
                    if (ch == SPACE) {
                        final String[] strings = stringBuffer1.toString().split(Pattern.quote("|"));
                        stringBuffer1.setLength(0);
                        check(Arrays.nonEmpty(strings), originalText, i, "解析器错误：普通文本为空！");

                        final List<String> stringList = new ArrayList<>(strings.length);
                        for (String string : strings) {
                            check(!stringList.contains(string), originalText, i, "重复定义普通文本：" + string);
                            stringList.add(string);
                        }
                        final Element element = new Element.PlainTexts(stringList);
                        elementList.add(element);

                        state = CompileState.DEFAULT;
                        continue;
                    } else {
                        stringBuffer1.append(ch);
                        continue;
                    }
                }
                case DECLARE: {
                    check(ch != DECIARE_SUFFIX, originalText, i, "变量定义为空");
                    switch (ch) {
                        case DECIARE_SUFFIX: {
                            check(false, originalText, i, "变量定义为空！");
                            continue;
                        }
                        default: {
                            stringBuffer1.append(ch);
                            state = CompileState.SIMPLE_PARAMETER_DECLARE;
                            continue;
                        }
                    }
                }
                case SIMPLE_PARAMETER_DECLARE: {
                    switch (ch) {
                        case OPTION_OPERATOR: {
                            check(stringBuffer1.length() > 0, originalText, i, "选项名不能为空！");
                            state = CompileState.OPTION_DECLARE;
                            break;
                        }
                        case DEFAULT_OPERATOR: {
                            state = CompileState.NULLABLE_PARAMETER_DECLARE;
                            afterDefaultOperator = false;
                            break;
                        }
                        case REMAIN_SUFFIX: {
                            final char nextChar = text.charAt(i + 1);
                            check(nextChar == DECIARE_SUFFIX, originalText, i, "剩余参数定义必须以 ~ 结尾");

                            final String name = stringBuffer1.toString();
                            stringBuffer1.setLength(0);
                            check(StringUtil.notEmpty(name), originalText, i, "参数名不能为空！");
                            check(!environment.containsKey(name), originalText, i, "参数 " + name + " 重定义！");
                            final Element.Reference reference = new Element.Reference.Remain.NonNull(name);
                            environment.put(name, reference);
                            elementList.add(reference);

                            state = CompileState.DEFAULT;
                            i++;
                            break;
                        }
                        case DECIARE_SUFFIX: {
                            final String name = stringBuffer1.toString();
                            stringBuffer1.setLength(0);

                            check(StringUtil.notEmpty(name), originalText, i, "参数名不能为空！");
                            check(!environment.containsKey(name), originalText, i, "参数 " + name + " 重定义！");
                            final Element.Reference reference = new Element.Reference.Simple(name);
                            environment.put(name, reference);
                            elementList.add(reference);

                            state = CompileState.DEFAULT;
                            break;
                        }
                        default: {
                            stringBuffer1.append(ch);
                            break;
                        }
                    }
                    continue;
                }
                case OPTION_DECLARE: {
                    // stringBuffer2
                    switch (ch) {
                        case DECIARE_SUFFIX: {
                            final String optionalString = stringBuffer2.toString();
                            stringBuffer2.setLength(0);
                            final Set<String> optionalValues = new HashSet<>();
                            if (StringUtil.notEmpty(optionalString)) {
                                final String[] strings = optionalString.split(Pattern.quote("|"));
                                for (String string : strings) {
                                    check(!optionalValues.contains(string), originalText, i, "选项重定义：" + string);
                                    optionalValues.add(string);
                                }
                            }

                            final String names = stringBuffer1.toString();
                            stringBuffer1.setLength(0);
                            final Set<String> aliases = new HashSet<>();
                            final String[] strings = names.split(Pattern.quote("|"));
                            for (String string : strings) {
                                check(StringUtil.notEmpty(string), originalText, i, "选项名不能为空！");
                                aliases.add(string);
                            }

                            // 前面已经断言过选项名非空了
                            final String name = strings[0];
                            check(!environment.containsKey(name), originalText, i, "参数 " + name + " 重定义！");
                            final Element.Reference.Option option = new Element.Reference.Option(name, aliases, "", optionalValues);
                            environment.put(name, option);
                            elementList.add(option);

                            state = CompileState.DEFAULT;
                            break;
                        }
                        case DEFAULT_OPERATOR: {
                            state = CompileState.OPTION_DEFAULT_DECLARE;
                            continue;
                        }
                        default: {
                            stringBuffer2.append(ch);
                            break;
                        }
                    }
                    break;
                }
                case NULLABLE_PARAMETER_DECLARE: {
                    switch (ch) {
                        case DECIARE_SUFFIX: {
                            check(false, originalText, i, "带有默认值的参数，只能是选项或剩余参数！");
                            break;
                        }
                        case REMAIN_SUFFIX: {
                            final String defaultValue = stringBuffer2.toString();
                            stringBuffer2.setLength(0);

                            final String name = stringBuffer1.toString();
                            check(StringUtil.notEmpty(name), originalText, i, "参数名不能为空！");
                            check(!environment.containsKey(name), originalText, i, "参数 " + name + " 重定义！");
                            check(i + 1 < text.length() || text.charAt(i + 1) == ']', originalText, i, "可空剩余参数未以 ] 结尾！");

                            i++;
                            final Element.Reference reference = new Element.Reference.Remain.Nullable(name, defaultValue);
                            environment.put(name, reference);
                            elementList.add(reference);

                            state = CompileState.DEFAULT;
                            break;
                        }
                        default: {
                            stringBuffer2.append(ch);
                            break;
                        }
                    }
                    break;
                }
                case OPTION_DEFAULT_DECLARE: {
                    // stringBuffer3
                    switch (ch) {
                        case DECIARE_SUFFIX: {
                            final String optionalString = stringBuffer3.toString();
                            stringBuffer3.setLength(0);
                            final Set<String> optionalValues = new HashSet<>();
                            if (StringUtil.notEmpty(optionalString)) {
                                final String[] strings = optionalString.split(Pattern.quote("|"));
                                for (String string : strings) {
                                    check(!optionalValues.contains(string), originalText, i, "选项重定义：" + string);
                                    optionalValues.add(string);
                                }
                            }

                            final String defaultValue = stringBuffer2.toString();

                            final String names = stringBuffer1.toString();
                            final Set<String> aliases = new HashSet<>();
                            final String[] strings = names.split(Pattern.quote("|"));
                            for (String string : strings) {
                                check(StringUtil.notEmpty(string), originalText, i, "选项名不能为空！");
                                aliases.add(string);
                            }

                            // 前面已经断言过选项名非空了
                            final String name = strings[0];
                            check(!environment.containsKey(name), originalText, i, "参数 " + strings[0] + " 重定义！");
                            final Element.Reference.Option option = new Element.Reference.Option(name, aliases, defaultValue, optionalValues);
                            environment.put(name, option);
                            elementList.add(option);

                            state = CompileState.DEFAULT;
                            break;
                        }
                        default: {
                            stringBuffer3.append(ch);
                            break;
                        }
                    }
                    break;
                }
                default:
                    check(false, originalText, i, "解析器错误：未知状态：" + state);
            }
        }

        check(state == CompileState.DEFAULT, originalText, originalText.length() - 1, "格式串不应在此结尾");

        return new FormatInfo(originalText, elementList.toArray(new Element[0]));
    }

    public String getCompletedUsage() {
        return Arrays.toString(elements, Element::getCompletedUsage, " ");
    }

    public String getSimpleUsage() {
        return Arrays.toString(elements, Element::getCompletedUsage, " ");
    }

    private static void check(boolean legal, String text, int charIndex, String message) {
        Preconditions.argument(legal, "编译错误：" + message + "（位于第 " + (charIndex + 1) + " 个字符附近）\n"
                + text + "\n" + StringUtil.repeat(" ", Math.max(0, charIndex - 1)) + "~~~");
    }
}
