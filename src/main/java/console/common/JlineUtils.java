package console.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.jline.builtins.Completers.FilesCompleter;
import org.jline.reader.Buffer;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Attributes;
import org.jline.terminal.Attributes.ControlChar;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

class StringsCompleterIgnoreCase implements Completer {

    protected final Collection<Candidate> candidates = new ArrayList<>();

    public StringsCompleterIgnoreCase() {}

    public StringsCompleterIgnoreCase(String... strings) {
        this(Arrays.asList(strings));
    }

    public StringsCompleterIgnoreCase(Iterable<String> strings) {
        assert strings != null;
        for (String string : strings) {
            candidates.add(
                    new Candidate(
                            AttributedString.stripAnsi(string),
                            string,
                            null,
                            null,
                            null,
                            null,
                            true));
        }
    }

    public void complete(
            LineReader reader, final ParsedLine commandLine, final List<Candidate> candidates) {
        assert commandLine != null;
        assert candidates != null;

        Buffer buffer = reader.getBuffer();
        String start = (buffer == null) ? "" : buffer.toString();
        int index = start.lastIndexOf(" ");
        String tmp = start.substring(index + 1, start.length()).toLowerCase();

        for (Iterator<Candidate> iter = this.candidates.iterator(); iter.hasNext(); ) {
            Candidate candidate = iter.next();
            String candidateStr = candidate.value().toLowerCase();
            if (candidateStr.startsWith(tmp)) {
                candidates.add(candidate);
            }
        }
    }
}

class ConsoleFilesCompleter extends FilesCompleter {
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
            System.out.println(e.getMessage());
        }
    }
}

public class JlineUtils {

    public static LineReader getLineReader() throws IOException {

        List<Completer> completers = new ArrayList<Completer>();

        List<String> commands =
                Arrays.asList(
                        "help",
                        "switch",
                        "getBlockNumber",
                        "getPbftView",
                        "getSealerList",
                        "getObserverList",
                        "getConsensusStatus",
                        "getSyncStatus",
                        "getNodeVersion",
                        "getPeers",
                        "getNodeIDList",
                        "getGroupPeers",
                        "getGroupList",
                        "getBlockByHash",
                        "getBlockByNumber",
                        "getBlockHashByNumber",
                        "getTransactionByHash",
                        "getTransactionByBlockHashAndIndex",
                        "getTransactionByBlockNumberAndIndex",
                        "getPendingTransactions",
                        "getPendingTxSize",
                        "getCode",
                        "getTotalTransactionCount",
                        "getDeployLog",
                        "addSealer",
                        "addObserver",
                        "removeNode",
                        "grantUserTableManager",
                        "revokeUserTableManager",
                        "listUserTableManager",
                        "grantDeployAndCreateManager",
                        "revokeDeployAndCreateManager",
                        "listDeployAndCreateManager",
                        "grantPermissionManager",
                        "revokePermissionManager",
                        "listPermissionManager",
                        "grantNodeManager",
                        "revokeNodeManager",
                        "listNodeManager",
                        "grantCNSManager",
                        "revokeCNSManager",
                        "listCNSManager",
                        "grantSysConfigManager",
                        "revokeSysConfigManager",
                        "listSysConfigManager",
                        "quit",
                        "exit",
                        "desc",
                        "create",
                        "select",
                        "insert",
                        "update",
                        "delete");

        for (String command : commands) {
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleterIgnoreCase(command),
                            new StringsCompleterIgnoreCase()));
        }

        Path path = FileSystems.getDefault().getPath("contracts/solidity/", "");
        commands = Arrays.asList("deploy", "call", "deployByCNS", "callByCNS", "queryCNS");

        for (String command : commands) {
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new ConsoleFilesCompleter(path),
                            new StringsCompleterIgnoreCase()));
        }
        commands = Arrays.asList("getTransactionReceipt");
        for (String command : commands) {
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new StringsCompleter("0x"),
                            new FilesCompleter(path)));
        }
        commands = Arrays.asList("setSystemConfigByKey", "getSystemConfigByKey");

        for (String command : commands) {
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new StringsCompleter(Common.TxCountLimit),
                            new StringsCompleterIgnoreCase()));
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new StringsCompleter(Common.TxGasLimit),
                            new StringsCompleterIgnoreCase()));
        }

        Terminal terminal =
                TerminalBuilder.builder()
                        .nativeSignals(true)
                        .signalHandler(Terminal.SignalHandler.SIG_IGN)
                        .build();
        Attributes termAttribs = terminal.getAttributes();
        // enable CTRL+D shortcut to exit
        // disable CTRL+C shortcut
        termAttribs.setControlChar(ControlChar.VEOF, 4);
        termAttribs.setControlChar(ControlChar.VINTR, -1);
        terminal.setAttributes(termAttribs);
        return LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new AggregateCompleter(completers))
                .build()
                .option(LineReader.Option.HISTORY_IGNORE_SPACE, false)
                .option(LineReader.Option.HISTORY_REDUCE_BLANKS, false);
    }
}
