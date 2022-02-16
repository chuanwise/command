package cn.chuanwise.commandlib.tree;

import cn.chuanwise.commandlib.CommandLib;
import cn.chuanwise.commandlib.context.CompleteContext;
import cn.chuanwise.util.CollectionUtil;
import cn.chuanwise.util.Preconditions;
import lombok.Data;

import java.util.*;

@Data
public class PlainTextsCommandTree
        extends CommandTree {

    protected final List<String> texts;

    public PlainTextsCommandTree(List<String> texts, CommandLib commandLib) {
        super(commandLib);

        Preconditions.argumentNonEmpty(texts, "texts");

        this.texts = texts;
    }

    @Override
    protected Optional<Element> accept(String argument) throws Exception {
        if (texts.contains(argument)) {
            return Optional.of(new PlainTextElement(argument));
        }
        return Optional.empty();
    }

    @Override
    public String getSimpleUsage() {
        return texts.get(0);
    }

    @Override
    public String getCompleteUsage() {
        return CollectionUtil.toString(texts, "|");
    }

    @Override
    public Set<String> complete(CompleteContext context) throws Exception {
        final Set<String> strings = super.complete(context);
        if (!texts.isEmpty()) {
            final Set<String> copied = new HashSet<>(strings);
            copied.addAll(texts);
            return Collections.unmodifiableSet(copied);
        } else {
            return strings;
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
