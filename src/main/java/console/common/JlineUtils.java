package console.common;

import java.io.IOException;
import java.nio.file.FileSystems;
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

        Path path = FileSystems.getDefault().getPath("solidity/contracts/", "");
        commands = Arrays.asList("deploy", "call", "deployByCNS", "callByCNS", "queryCNS");

        for (String command : commands) {
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new FilesCompleter(path),
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
        // enable CTRL+D shortcut
        termAttribs.setControlChar(ControlChar.VEOF, 4);
        // enable CTRL+C shortcut
        termAttribs.setControlChar(ControlChar.VINTR, 4);
        terminal.setAttributes(termAttribs);
        return LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new AggregateCompleter(completers))
                .build()
                .option(LineReader.Option.HISTORY_IGNORE_SPACE, false)
                .option(LineReader.Option.HISTORY_REDUCE_BLANKS, false);
    }
}
