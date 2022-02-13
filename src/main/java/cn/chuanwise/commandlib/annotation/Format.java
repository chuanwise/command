package cn.chuanwise.commandlib.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Format {
    /**
     * 指令格式
     *
     * <p>指令格式是由变量定义、变量引用、原文字符组成的字符串。
     * 注册时，小明将根据字符串将本函数作为指令处理器挂载到对应
     * 指令树上的节点上。</p>
     *
     * <p>变量定义是由 [] 包围的字符串。文法为：
     * <ol>
     *     <li>标准字符串：S -> S'</li>
     *     <li>标准字符串：S' -> C | S C</li>
     *     <li>字符串节点：C -> T | '[' V ']'</li>
     *     <li>原文字符串：T -> id | id '|' T</li>
     *     <li>变量节点：V -> D | D ':' O | D ':'</li>
     *     <li>变量定义：D -> id | id '?' | id "..." | id '?' "." | id '?' id</li>
     *     <li>选项属性：O -> id | id '|' O</li>
     * </ol></p>
     *
     * <p>例如：
     * <ol>
     *     <li>text：原文字符串</li>
     *     <li>text1|text2：具备缩写的原文字符串</li>
     *     <li>[id]：简单变量定义</li>
     *     <li>[id~]：剩余所有变量定义</li>
     *     <li>[id?~]：可空剩余变量定义</li>
     *     <li>[option|alias1:val1|val2]：选项型参数，全名为 option，别名包括 alias；取值为 val1、val2</li>
     *     <li>[option:]：使用自动填充的选项形参数</li>
     *     <li>[option?default:val1|val2]：带有默认值的选项，默认值可以不出现在后面的可选值列表中</li>
     *     <li>[option?default:]：自动填充的带有默认值的选项</li>
     * </ol></p>
     *
     * <p>例如
     * <ol>
     *     <li>/pex user [user] group set [group]</li>
     *     <li>/pex user [user] add [permission~]</li>
     *     <li>/co lookup|l [action:command|chat|build] [user:] [time|t:]</li>
     * </ol></p>
     */
    String value();
}