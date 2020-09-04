package console.command.completer;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.jline.builtins.Completers;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleFilesCompleter extends Completers.FilesCompleter {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleFilesCompleter.class);

    public final String SOL_STR = ".sol";
    public final String TABLE_SOL = "Table.sol";

    public ConsoleFilesCompleter(File currentDir) {
        super(currentDir);
    }

    public ConsoleFilesCompleter(Path path) {
        super(path);
    }

    @Override
    protected String getDisplay(Terminal terminal, Path p) {
        String name = p.getFileName().toString();
        // do not display .sol
        if (name.endsWith(SOL_STR)) {
            name = name.substring(0, name.length() - SOL_STR.length());
        }
        if (Files.isDirectory(p)) {
            AttributedStringBuilder sb = new AttributedStringBuilder();
            sb.styled(AttributedStyle.BOLD.foreground(AttributedStyle.RED), name);
            sb.append("/");
            name = sb.toAnsi(terminal);
        } else if (Files.isSymbolicLink(p)) {
            AttributedStringBuilder sb = new AttributedStringBuilder();
            sb.styled(AttributedStyle.BOLD.foreground(AttributedStyle.RED), name);
            sb.append("@");
            name = sb.toAnsi(terminal);
        }
        return name;
    }

    @Override
    public void complete(
            LineReader reader, ParsedLine commandLine, final List<Candidate> candidates) {
        assert commandLine != null;
        assert candidates != null;

        String buffer = commandLine.word().substring(0, commandLine.wordCursor());

        Path current;
        String curBuf;
        String sep = getUserDir().getFileSystem().getSeparator();
        int lastSep = buffer.lastIndexOf(sep);
        if (lastSep >= 0) {
            curBuf = buffer.substring(0, lastSep + 1);
            if (curBuf.startsWith("~")) {
                if (curBuf.startsWith("~" + sep)) {
                    current = getUserHome().resolve(curBuf.substring(2));
                } else {
                    current = getUserHome().getParent().resolve(curBuf.substring(1));
                }
            } else {
                current = getUserDir().resolve(curBuf);
            }
        } else {
            curBuf = "";
            current = getUserDir();
        }

        try (DirectoryStream<Path> directoryStream =
                Files.newDirectoryStream(current, this::accept)) {

            directoryStream.forEach(
                    p -> {
                        String value = curBuf + p.getFileName().toString();
                        // filter not sol file and Table.sol
                        if (!value.endsWith(SOL_STR) || TABLE_SOL.equals(value)) {
                            return;
                        }
                        value = value.substring(0, value.length() - SOL_STR.length());
                        if (Files.isDirectory(p)) {
                            candidates.add(
                                    new Candidate(
                                            value
                                                    + (reader.isSet(
                                                                    LineReader.Option
                                                                            .AUTO_PARAM_SLASH)
                                                            ? sep
                                                            : ""),
                                            getDisplay(reader.getTerminal(), p),
                                            null,
                                            null,
                                            reader.isSet(LineReader.Option.AUTO_REMOVE_SLASH)
                                                    ? sep
                                                    : null,
                                            null,
                                            false));
                        } else {
                            candidates.add(
                                    new Candidate(
                                            value,
                                            getDisplay(reader.getTerminal(), p),
                                            null,
                                            null,
                                            null,
                                            null,
                                            true));
                        }
                    });
        } catch (IOException e) {
            logger.error(" message: {}, e: {}", e.getMessage(), e);
        }
    }
}
