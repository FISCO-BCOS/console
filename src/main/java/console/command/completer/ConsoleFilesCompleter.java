package console.command.completer;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jline.builtins.Completers;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.StyleResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleFilesCompleter extends Completers.FilesCompleter {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleFilesCompleter.class);

    public final String SOL_STR = ".sol";
    private static final Set<String> EXCLUDE_SOL = new HashSet<>();
    private Path contractPath;
    private boolean solidityCompleter = true;
    private boolean isWasm = false;

    static {
        EXCLUDE_SOL.add("TableV320.sol");
        EXCLUDE_SOL.add("Crypto.sol");
    }

    public ConsoleFilesCompleter(File contractFile) {
        this(contractFile.toPath());
    }

    public ConsoleFilesCompleter(File contractFile, boolean isWasm) {
        this(contractFile);
        this.isWasm = isWasm;
        this.solidityCompleter = !isWasm;
    }

    public ConsoleFilesCompleter(Path contractPath) {
        super(new File("." + File.separator));
        this.contractPath = contractPath;
    }

    @Override
    protected String getDisplay(
            Terminal terminal, Path p, StyleResolver resolver, String separator) {
        String name = p.getFileName().toString();
        // do not display .sol
        if (name.endsWith(SOL_STR)) {
            name = name.substring(0, name.length() - SOL_STR.length());
        }
        if (Files.isDirectory(p)) {
            AttributedStringBuilder sb = new AttributedStringBuilder();
            sb.styled(AttributedStyle.BOLD.foreground(AttributedStyle.RED), name);
            sb.append(separator);
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
        String buffer = commandLine.word().substring(0, commandLine.wordCursor());
        // complete contract path
        complete(buffer, reader, candidates, true);
        // complete userDir
        complete(buffer, reader, candidates, false);
    }

    public void complete(
            String buffer,
            LineReader reader,
            final List<Candidate> candidates,
            boolean completeSol) {
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
                current = completeSol ? contractPath.resolve(curBuf) : getUserDir().resolve(curBuf);
            }
        } else {
            curBuf = "";
            current = completeSol ? contractPath : getUserDir();
        }

        try (DirectoryStream<Path> directoryStream =
                Files.newDirectoryStream(current, this::accept)) {
            if (!Files.exists(current)) {
                return;
            }
            directoryStream.forEach(
                    p -> {
                        if (isWasm) filterDisplayLiquid(reader, candidates, curBuf, p);
                        else filterDisplaySolidity(reader, candidates, curBuf, p);
                    });
        } catch (Exception e) {
            logger.error(" message: {}, e: {}", e.getMessage(), e);
        }
    }

    private void filterDisplaySolidity(
            LineReader reader, List<Candidate> candidates, String curBuf, Path p) {
        if (!Files.exists(p)) {
            return;
        }
        String value = curBuf + p.getFileName().toString();
        // filter sol interface file
        if (EXCLUDE_SOL.contains(value)) {
            return;
        }

        if (solidityCompleter && !Files.isDirectory(p) && !value.endsWith(SOL_STR)) {
            return;
        }

        if (Files.isDirectory(p)) {
            candidates.add(
                    new Candidate(
                            value
                                    + (reader.isSet(LineReader.Option.AUTO_PARAM_SLASH)
                                            ? File.separator
                                            : ""),
                            getDisplay(reader.getTerminal(), p, null, File.separator),
                            null,
                            null,
                            reader.isSet(LineReader.Option.AUTO_REMOVE_SLASH)
                                    ? File.separator
                                    : null,
                            null,
                            false));
        } else {
            value = value.substring(0, value.length() - SOL_STR.length());
            candidates.add(
                    new Candidate(
                            value,
                            getDisplay(reader.getTerminal(), p, null, File.separator),
                            null,
                            null,
                            null,
                            null,
                            true));
        }
    }

    private void filterDisplayLiquid(
            LineReader reader, List<Candidate> candidates, String curBuf, Path p) {
        if (!Files.exists(p)) {
            return;
        }
        String value = curBuf + p.getFileName().toString();
        if (!Files.isDirectory(p)) {
            return;
        }
        candidates.add(
                new Candidate(
                        value
                                + (reader.isSet(LineReader.Option.AUTO_PARAM_SLASH)
                                        ? File.separator
                                        : ""),
                        getDisplay(reader.getTerminal(), p, null, File.separator),
                        null,
                        null,
                        reader.isSet(LineReader.Option.AUTO_REMOVE_SLASH) ? File.separator : null,
                        null,
                        false));
    }
}
