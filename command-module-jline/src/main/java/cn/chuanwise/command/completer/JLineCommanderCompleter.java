package cn.chuanwise.command.completer;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.context.DispatchContext;
import cn.chuanwise.command.object.AbstractCommanderObject;
import cn.chuanwise.common.util.Strings;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

/**
 * 适配 JLine 的补全工具
 *
 * @author Chuanwise
 */
public class JLineCommanderCompleter
    extends AbstractCommanderObject
    implements Completer {

    public JLineCommanderCompleter(Commander commander) {
        super(commander);
    }

    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
        final String line = parsedLine.line().substring(0, parsedLine.cursor());

        // 检查最后一个是否是 ' '
        final boolean uncompleted;
        if (Strings.nonEmpty(line)) {
            uncompleted = !Character.isSpaceChar(line.charAt(line.length() - 1));
        } else {
            uncompleted = false;
        }

        commander.getCompleteService().sortedComplete(new DispatchContext(commander, null, line), uncompleted)
                .stream()
                .map(Candidate::new)
                .forEach(list::add);
    }
}
