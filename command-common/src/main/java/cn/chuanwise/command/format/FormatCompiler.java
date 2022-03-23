package cn.chuanwise.command.format;

@FunctionalInterface
public interface FormatCompiler {
    FormatInfo compile(String input);

    static FormatCompiler defaultCompiler() {
        return DefaultFormatCompiler.getInstance();
    }
}
