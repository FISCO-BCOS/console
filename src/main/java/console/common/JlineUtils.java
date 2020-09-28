package console.common;

import console.account.Account;
import console.account.AccountManager;
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
import java.util.Set;
import org.fisco.bcos.web3j.abi.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.web3j.abi.wrapper.ContractABIDefinition;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LoadAccountCompleter extends StringsCompleterIgnoreCase {

    private static final Logger logger = LoggerFactory.getLogger(LoadAccountCompleter.class);

    @Override
    public void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {

        try {
            File accountsDir = new File(PathUtils.ACCOUNT_DIRECTORY);
            File[] accountFiles = accountsDir.listFiles();
            for (File file : accountFiles) {
                String fileName = file.getName();

                if (!(fileName.endsWith(".pem") || fileName.endsWith(".p12"))) {
                    continue;
                }
                // exclude public file
                if (fileName.contains("public.pem")) {
                    continue;
                }

                candidates.add(
                        new Candidate(
                                AttributedString.stripAnsi(fileName),
                                fileName,
                                null,
                                null,
                                null,
                                null,
                                true));
            }
        } catch (Exception e) {
            logger.debug("e:", e);
        }

        super.complete(reader, commandLine, candidates);
    }
}

class SwitchAccountCompleter extends StringsCompleterIgnoreCase {
    private static final Logger logger = LoggerFactory.getLogger(SwitchAccountCompleter.class);

    private AccountManager accountManager;

    public SwitchAccountCompleter(final AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public AccountManager getAccountManager() {
        return this.accountManager;
    }

    public void setAccountManager(final AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {

        Collection<Account> values = accountManager.getAccountMap().values();
        Account currentAccount = accountManager.getCurrentAccount();
        for (Account account : values) {
            if (account.getCredentials()
                    .getAddress()
                    .equals(currentAccount.getCredentials().getAddress())) {
                continue;
            }

            candidates.add(
                    new Candidate(
                            AttributedString.stripAnsi(account.getCredentials().getAddress()),
                            account.getCredentials().getAddress(),
                            null,
                            null,
                            null,
                            null,
                            true));
        }

        super.complete(reader, commandLine, candidates);
    }
}

class ContractAddressCompleter extends StringsCompleterIgnoreCase {

    private static final Logger logger = LoggerFactory.getLogger(ContractAddressCompleter.class);

    public ContractAddressCompleter(final DeployContractManager deployContractManager) {
        this.deployContractManager = deployContractManager;
    }

    private DeployContractManager deployContractManager;

    public DeployContractManager getDeployContractManager() {
        return this.deployContractManager;
    }

    public void setDeployContractManager(final DeployContractManager deployContractManager) {
        this.deployContractManager = deployContractManager;
    }

    @Override
    public void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {

        String buffer = reader.getBuffer().toString().trim();
        String[] ss = buffer.split(" ");

        if (ss.length >= 2) {
            try {
                File solFile = PathUtils.getSolFile(ss[1]);
                String contractName = solFile.getName().split("\\.")[0];

                List<DeployContractManager.DeployedContract> deployContractList =
                        deployContractManager.getDeployContractList(
                                deployContractManager.getGroupId(), contractName);

                int addressCount = 0;
                final int addressCompleterCount = 10;
                for (DeployContractManager.DeployedContract deployedContract : deployContractList) {
                    if (addressCount >= addressCompleterCount) {
                        break;
                    }
                    candidates.add(
                            new Candidate(
                                    AttributedString.stripAnsi(
                                            deployedContract.getContractAddress()),
                                    deployedContract.getContractAddress(),
                                    null,
                                    null,
                                    null,
                                    null,
                                    true));
                    addressCount++;
                }
            } catch (Exception e) {
                logger.error("e: {}", e);
            }
        }

        super.complete(reader, commandLine, candidates);
    }
}

class ContractMethodCompleter extends StringsCompleterIgnoreCase {

    private static final Logger logger = LoggerFactory.getLogger(ContractMethodCompleter.class);

    @Override
    public void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {

        String buffer = reader.getBuffer().toString().trim();
        String[] ss = buffer.split(" ");

        if (ss.length >= 3) {
            // TO DO
            try {
                File solFile = PathUtils.getSolFile(ss[1]);
                String abi =
                        ConsoleUtils.compileSolForABI(solFile.getName().split("\\.")[0], solFile);

                ContractABIDefinition contractABIDefinition = ABIDefinitionFactory.loadABI(abi);
                Set<String> functionNames = contractABIDefinition.getFunctions().keySet();

                for (String funName : functionNames) {
                    candidates.add(
                            new Candidate(
                                    AttributedString.stripAnsi(funName),
                                    funName,
                                    null,
                                    null,
                                    null,
                                    null,
                                    true));
                }

            } catch (Exception e) {
                logger.error("e: {}", e);
            }
        }

        super.complete(reader, commandLine, candidates);
    }
}

class StringsCompleterIgnoreCase implements Completer {

    private static final Logger logger = LoggerFactory.getLogger(StringsCompleterIgnoreCase.class);

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
            // System.out.println(e.getMessage());
            logger.error(" message: {}, e: {}", e.getMessage(), e);
        }
    }
}

public class JlineUtils {

    private static final Logger logger = LoggerFactory.getLogger(JlineUtils.class);

    public static LineReader getLineReader(
            DeployContractManager deployContractManager, AccountManager accountManager)
            throws IOException {

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
                        "getBlockHeaderByHash",
                        "getBlockHeaderByNumber",
                        "getBlockHashByNumber",
                        "getTransactionByHash",
                        "getTransactionByBlockHashAndIndex",
                        "getTransactionByBlockNumberAndIndex",
                        "getTransactionByHashWithProof",
                        "getTransactionReceiptByHashWithProof",
                        "getPendingTransactions",
                        "getPendingTxSize",
                        "getCode",
                        "getTotalTransactionCount",
                        "getDeployLog",
                        "listDeployContractAddress",
                        "listAbi",
                        "addSealer",
                        "addObserver",
                        "removeNode",
                        "grantContractWritePermission",
                        "revokeContractWritePermission",
                        "listContractWritePermission",
                        "grantUserTableManager",
                        "revokeUserTableManager",
                        "listUserTableManager",
                        "grantDeployAndCreateManager",
                        "revokeDeployAndCreateManager",
                        "listDeployAndCreateManager",
                        "grantNodeManager",
                        "revokeNodeManager",
                        "listNodeManager",
                        "grantCNSManager",
                        "revokeCNSManager",
                        "listCNSManager",
                        "grantSysConfigManager",
                        "revokeSysConfigManager",
                        "listSysConfigManager",
                        "listContractWritePermission",
                        "grantContractWritePermission",
                        "revokeContractWritePermission",
                        "freezeContract",
                        "unfreezeContract",
                        "grantContractStatusManager",
                        "getContractStatus",
                        "listContractStatusManager",
                        "grantCommitteeMember",
                        "revokeCommitteeMember",
                        "listCommitteeMembers",
                        "grantOperator",
                        "listOperators",
                        "revokeOperator",
                        "updateThreshold",
                        "updateCommitteeMemberWeight",
                        "queryThreshold",
                        "queryCommitteeMemberWeight",
                        "freezeAccount",
                        "unfreezeAccount",
                        "getAccountStatus",
                        "newAccount",
                        "listAccount",
                        "newAccount",
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

        Path solDefaultPath = FileSystems.getDefault().getPath(PathUtils.SOL_DIRECTORY, "");
        Path currentPath = new File("").toPath();

        // Path path = FileSystems.getDefault().getPath(PathUtils.SOL_DIRECTORY, "");

        commands =
                Arrays.asList(
                        "deploy",
                        "deployByCNS",
                        "callByCNS",
                        "queryCNS",
                        "listDeployContractAddress");

        for (String command : commands) {
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new ConsoleFilesCompleter(solDefaultPath),
                            new StringsCompleterIgnoreCase()));
        }

        commands = Arrays.asList("call");

        for (String command : commands) {
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new ConsoleFilesCompleter(solDefaultPath),
                            new ContractAddressCompleter(deployContractManager),
                            new ContractMethodCompleter(),
                            new StringsCompleterIgnoreCase()));
        }

        commands = Arrays.asList("registerCNS");

        for (String command : commands) {
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new ConsoleFilesCompleter(solDefaultPath),
                            new StringsCompleterIgnoreCase()));
        }

        commands = Arrays.asList("switchAccount");

        for (String command : commands) {
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new SwitchAccountCompleter(accountManager),
                            new StringsCompleterIgnoreCase()));
        }

        commands = Arrays.asList("listAbi");

        for (String command : commands) {
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new ConsoleFilesCompleter(solDefaultPath),
                            new StringsCompleterIgnoreCase()));
        }

        commands = Arrays.asList("getTransactionReceipt");
        for (String command : commands) {
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command), new StringsCompleter("0x")));
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
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new StringsCompleter(Common.RPBFTEpochSealerNum),
                            new StringsCompleterIgnoreCase()));
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new StringsCompleter(Common.RPBFTEpochBlockNum),
                            new StringsCompleterIgnoreCase()));
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new StringsCompleter(Common.ConsensusTimeout),
                            new StringsCompleterIgnoreCase()));
        }

        commands = Arrays.asList("loadAccount");
        for (String command : commands) {
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new LoadAccountCompleter(),
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
